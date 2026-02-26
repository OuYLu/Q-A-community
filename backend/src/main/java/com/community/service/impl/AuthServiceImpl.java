package com.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.community.common.BizException;
import com.community.common.ResultCode;
import com.community.common.SecurityUser;
import com.community.config.WechatProperties;
import com.community.dto.LoginDTO;
import com.community.dto.WechatBindPhoneDTO;
import com.community.dto.WechatLoginDTO;
import com.community.entity.Role;
import com.community.entity.User;
import com.community.entity.UserOauth;
import com.community.entity.UserRole;
import com.community.entity.UserStat;
import com.community.mapper.RoleMapper;
import com.community.mapper.UserMapper;
import com.community.mapper.UserOauthMapper;
import com.community.mapper.UserRoleMapper;
import com.community.mapper.UserStatMapper;
import com.community.service.AuthService;
import com.community.service.UserService;
import com.community.util.JwtUtil;
import com.community.vo.LoginVO;
import com.community.vo.WechatLoginVO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {
    private static final String WECHAT_PROVIDER = "wechat_mp";
    private static final long BIND_TICKET_EXPIRE_SECONDS = 10 * 60;
    private static final long CODE_RECENT_WINDOW_MILLIS = TimeUnit.MINUTES.toMillis(2);

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final UserRoleMapper userRoleMapper;
    private final UserOauthMapper userOauthMapper;
    private final UserStatMapper userStatMapper;
    private final PasswordEncoder passwordEncoder;
    private final WechatProperties wechatProperties;
    private final ObjectMapper objectMapper;

    private final Map<String, PendingWechatBind> pendingWechatBindMap = new ConcurrentHashMap<>();
    private final Map<String, Long> recentWechatCodeMap = new ConcurrentHashMap<>();

    @Override
    public LoginVO login(LoginDTO loginDTO) {
        User user = userService.findByUsernameOrPhone(loginDTO.getUsername());
        if (user == null) {
            throw new BizException(ResultCode.BAD_REQUEST, "用户名或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BizException(ResultCode.UNAUTHORIZED, "账号已被禁用，请联系管理员");
        }

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDTO.getUsername(), loginDTO.getPassword())
            );
        } catch (AuthenticationException ex) {
            throw new BizException(ResultCode.BAD_REQUEST, "用户名或密码错误");
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);

        SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
        String token = jwtUtil.generateToken(securityUser.getId(), securityUser.getUsername(), securityUser.getRoleCodes());
        long expiresAt = Instant.now().plusSeconds(jwtUtil.getExpireMinutes() * 60).toEpochMilli();

        return new LoginVO(token, expiresAt, securityUser.getId());
    }

    @Override
    public WechatLoginVO wechatLogin(WechatLoginDTO wechatLoginDTO) {
        cleanupExpiredTickets();
        cleanupRecentCodes();
        String code = wechatLoginDTO.getCode();
        if (isRecentDuplicateCode(code)) {
            log.warn("wechat login duplicate code detected, appIdSuffix={}, codeFingerprint={}",
                maskTail(wechatProperties.getAppId(), 6), maskHeadAndTail(code));
        }
        log.info("wechat login start, appIdSuffix={}, codeFingerprint={}",
            maskTail(wechatProperties.getAppId(), 6), maskHeadAndTail(code));

        WechatSession session = getWechatSession(code);

        UserOauth oauth = userOauthMapper.selectOne(new LambdaQueryWrapper<UserOauth>()
            .eq(UserOauth::getProvider, WECHAT_PROVIDER)
            .eq(UserOauth::getOpenid, session.openid()));
        if (oauth != null) {
            User user = userMapper.selectById(oauth.getUserId());
            if (user == null) {
                throw new BizException(ResultCode.SERVER_ERROR, "微信账号绑定数据异常");
            }
            assertUserEnabled(user);
            return buildWechatLoginSuccess(user, false);
        }

        String bindTicket = UUID.randomUUID().toString().replace("-", "");
        long expireAt = Instant.now().plusSeconds(BIND_TICKET_EXPIRE_SECONDS).toEpochMilli();
        pendingWechatBindMap.put(bindTicket, new PendingWechatBind(
            session.openid(),
            session.unionid(),
            session.sessionKey(),
            wechatLoginDTO.getNickname(),
            wechatLoginDTO.getAvatar(),
            expireAt
        ));

        return new WechatLoginVO(null, 0L, null, null, true, true, bindTicket);
    }

    @Override
    @Transactional
    public WechatLoginVO wechatBindPhone(WechatBindPhoneDTO bindPhoneDTO) {
        cleanupExpiredTickets();
        PendingWechatBind pending = pendingWechatBindMap.remove(bindPhoneDTO.getBindTicket());
        if (pending == null || pending.expireAt() <= System.currentTimeMillis()) {
            throw new BizException(ResultCode.BAD_REQUEST, "绑定凭证无效或已过期，请重新微信登录");
        }

        UserOauth existOauth = userOauthMapper.selectOne(new LambdaQueryWrapper<UserOauth>()
            .eq(UserOauth::getProvider, WECHAT_PROVIDER)
            .eq(UserOauth::getOpenid, pending.openid()));
        if (existOauth != null) {
            User existUser = userMapper.selectById(existOauth.getUserId());
            if (existUser == null) {
                throw new BizException(ResultCode.SERVER_ERROR, "微信账号绑定数据异常");
            }
            assertUserEnabled(existUser);
            return buildWechatLoginSuccess(existUser, false);
        }

        String phone = bindPhoneDTO.getPhone();
        if (StringUtils.hasText(phone)) {
            User phoneUser = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getPhone, phone));
            if (phoneUser != null) {
                throw new BizException(ResultCode.BAD_REQUEST, "该手机号已被使用");
            }
        }

        User user = new User();
        user.setUsername(generateWechatUsername());
        user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        user.setPasswordSet(0);
        user.setStatus(1);
        user.setPhone(phone);
        user.setNickname(resolveNickname(bindPhoneDTO.getNickname(), pending.nickname()));
        user.setAvatar(resolveAvatar(bindPhoneDTO.getAvatar(), pending.avatar()));
        user.setFollowingCount(0);
        user.setFollowerCount(0);
        user.setLikeReceivedCount(0);
        user.setExpertStatus(1);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.insert(user);

        bindCustomerRole(user.getId());

        UserOauth userOauth = new UserOauth();
        userOauth.setUserId(user.getId());
        userOauth.setProvider(WECHAT_PROVIDER);
        userOauth.setOpenid(pending.openid());
        userOauth.setUnionid(pending.unionid());
        userOauth.setAppId(wechatProperties.getAppId());
        userOauth.setSessionKey(pending.sessionKey());
        userOauth.setCreatedAt(LocalDateTime.now());
        userOauth.setUpdatedAt(LocalDateTime.now());
        userOauthMapper.insert(userOauth);
        initUserStat(user.getId());

        return buildWechatLoginSuccess(user, true);
    }

    private void assertUserEnabled(User user) {
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BizException(ResultCode.UNAUTHORIZED, "账号已被禁用，请联系管理员");
        }
    }

    private WechatLoginVO buildWechatLoginSuccess(User user, boolean newUser) {
        List<String> roleCodes = userService.getRoleCodes(user.getId());
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), roleCodes);
        long expiresAt = Instant.now().plusSeconds(jwtUtil.getExpireMinutes() * 60).toEpochMilli();
        return new WechatLoginVO(token, expiresAt, user.getId(), user.getUsername(), newUser, false, null);
    }

    private void bindCustomerRole(Long userId) {
        Role customerRole = roleMapper.selectOne(new LambdaQueryWrapper<Role>().eq(Role::getCode, "customer"));
        if (customerRole == null) {
            log.warn("customer role not found when bind wechat user, userId={}", userId);
            return;
        }
        UserRole userRole = new UserRole();
        userRole.setUserId(userId);
        userRole.setRoleId(customerRole.getId());
        userRole.setCreatedAt(LocalDateTime.now());
        userRoleMapper.insert(userRole);
    }

    private void initUserStat(Long userId) {
        if (userId == null) {
            return;
        }
        if (userStatMapper.selectById(userId) != null) {
            return;
        }
        UserStat stat = new UserStat();
        stat.setUserId(userId);
        stat.setQuestionCount(0);
        stat.setAnswerCount(0);
        stat.setLikeReceivedCount(0);
        stat.setFollowerCount(0);
        stat.setFollowingCount(0);
        stat.setUpdatedAt(LocalDateTime.now());
        userStatMapper.insert(stat);
    }

    private String generateWechatUsername() {
        for (int i = 0; i < 10; i++) {
            String username = "wx_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
            User exists = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
            if (exists == null) {
                return username;
            }
        }
        throw new BizException(ResultCode.SERVER_ERROR, "生成微信账号失败，请稍后重试");
    }

    private String resolveNickname(String dtoNickname, String cachedNickname) {
        if (StringUtils.hasText(dtoNickname)) {
            return dtoNickname.trim();
        }
        if (StringUtils.hasText(cachedNickname)) {
            return cachedNickname.trim();
        }
        return "微信用户";
    }

    private String resolveAvatar(String dtoAvatar, String cachedAvatar) {
        if (StringUtils.hasText(dtoAvatar)) {
            return dtoAvatar.trim();
        }
        if (StringUtils.hasText(cachedAvatar)) {
            return cachedAvatar.trim();
        }
        return null;
    }

    private void cleanupExpiredTickets() {
        long now = System.currentTimeMillis();
        pendingWechatBindMap.entrySet().removeIf(entry -> entry.getValue().expireAt() <= now);
    }

    private void cleanupRecentCodes() {
        long now = System.currentTimeMillis();
        recentWechatCodeMap.entrySet().removeIf(entry -> now - entry.getValue() > CODE_RECENT_WINDOW_MILLIS);
    }

    private boolean isRecentDuplicateCode(String code) {
        long now = System.currentTimeMillis();
        Long last = recentWechatCodeMap.put(code, now);
        return last != null && now - last <= CODE_RECENT_WINDOW_MILLIS;
    }

    private WechatSession getWechatSession(String code) {
        String appId = wechatProperties.getAppId();
        String appSecret = wechatProperties.getAppSecret();
        if (!StringUtils.hasText(appId) || !StringUtils.hasText(appSecret)) {
            throw new BizException(ResultCode.SERVER_ERROR,
                "微信登录未配置 appId/appSecret，请检查 WECHAT_APP_ID 和 WECHAT_APP_SECRET");
        }
        if ("the code is a mock one".equalsIgnoreCase(String.valueOf(code).trim())) {
            throw new BizException(ResultCode.BAD_REQUEST,
                "当前拿到的是开发者工具模拟 code，请使用真机调试后重试微信登录");
        }

        try {
            String url = "https://api.weixin.qq.com/sns/jscode2session"
                + "?appid=" + encode(appId)
                + "&secret=" + encode(appSecret)
                + "&js_code=" + encode(code)
                + "&grant_type=authorization_code";

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            JsonNode json = objectMapper.readTree(response.body());

            int errCode = json.path("errcode").asInt(0);
            if (errCode != 0) {
                String errMsg = json.path("errmsg").asText("微信接口调用失败");
                log.warn("wechat jscode2session failed, appIdSuffix={}, codeFingerprint={}, errcode={}, errmsg={}",
                    maskTail(appId, 6), maskHeadAndTail(code), errCode, errMsg);
                if (errCode == 40029) {
                    throw new BizException(ResultCode.BAD_REQUEST,
                        "微信登录失败：code 无效或已过期。若持续出现，请检查后端 WECHAT_APP_ID 是否与小程序 appid 一致（当前后端 appId 后缀 "
                            + maskTail(appId, 6) + "）");
                }
                if (errCode == 40125) {
                    throw new BizException(ResultCode.BAD_REQUEST,
                        "微信登录失败：appSecret 错误，请检查 WECHAT_APP_SECRET 是否与 WECHAT_APP_ID 匹配");
                }
                if (errCode == 40013) {
                    throw new BizException(ResultCode.BAD_REQUEST,
                        "微信登录失败：appid 非法，请检查后端 WECHAT_APP_ID 配置");
                }
                throw new BizException(ResultCode.BAD_REQUEST, "微信登录失败: " + errMsg + " (errcode=" + errCode + ")");
            }

            String openid = json.path("openid").asText(null);
            if (!StringUtils.hasText(openid)) {
                throw new BizException(ResultCode.BAD_REQUEST, "微信登录失败：未获取到 openid");
            }

            String unionid = json.path("unionid").asText(null);
            String sessionKey = json.path("session_key").asText(null);
            log.info("wechat jscode2session success, appIdSuffix={}, codeFingerprint={}, openidSuffix={}",
                maskTail(appId, 6), maskHeadAndTail(code), maskTail(openid, 6));
            return new WechatSession(openid, unionid, sessionKey);
        } catch (BizException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("wechat jscode2session request error, appIdSuffix={}, codeFingerprint={}",
                maskTail(appId, 6), maskHeadAndTail(code), ex);
            throw new BizException(ResultCode.SERVER_ERROR, "微信登录请求失败，请稍后重试");
        }
    }

    private String encode(String text) {
        return URLEncoder.encode(text, StandardCharsets.UTF_8);
    }

    private String maskTail(String text, int keep) {
        if (!StringUtils.hasText(text)) {
            return "empty";
        }
        if (text.length() <= keep) {
            return text;
        }
        return "***" + text.substring(text.length() - keep);
    }

    private String maskHeadAndTail(String text) {
        if (!StringUtils.hasText(text)) {
            return "empty";
        }
        if (text.length() <= 8) {
            return text;
        }
        return text.substring(0, 4) + "***" + text.substring(text.length() - 4);
    }

    private record WechatSession(String openid, String unionid, String sessionKey) {
    }

    private record PendingWechatBind(
        String openid,
        String unionid,
        String sessionKey,
        String nickname,
        String avatar,
        long expireAt
    ) {
    }
}
