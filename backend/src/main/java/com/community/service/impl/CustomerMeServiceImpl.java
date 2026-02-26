package com.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.community.common.BizException;
import com.community.common.ResultCode;
import com.community.common.SecurityUser;
import com.community.dto.AppMePasswordChangeDTO;
import com.community.dto.AppMePasswordSetFirstDTO;
import com.community.dto.AppMeProfileUpdateDTO;
import com.community.dto.AppPageQueryDTO;
import com.community.entity.User;
import com.community.entity.UserStat;
import com.community.mapper.QaAnswerMapper;
import com.community.mapper.QaFavoriteMapper;
import com.community.mapper.QaQuestionMapper;
import com.community.mapper.UserBrowseHistoryMapper;
import com.community.mapper.UserFollowMapper;
import com.community.mapper.UserMapper;
import com.community.mapper.UserStatMapper;
import com.community.service.CustomerMeService;
import com.community.vo.AppDocVO;
import com.community.vo.AppFollowUserItemVO;
import com.community.vo.AppMeOverviewVO;
import com.community.vo.AppMyAnswerItemVO;
import com.community.vo.AppMyFavoriteItemVO;
import com.community.vo.AppMyHistoryItemVO;
import com.community.vo.AppMyQuestionItemVO;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class CustomerMeServiceImpl implements CustomerMeService {
    private final UserMapper userMapper;
    private final UserStatMapper userStatMapper;
    private final QaFavoriteMapper qaFavoriteMapper;
    private final UserBrowseHistoryMapper userBrowseHistoryMapper;
    private final QaQuestionMapper qaQuestionMapper;
    private final QaAnswerMapper qaAnswerMapper;
    private final UserFollowMapper userFollowMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public AppMeOverviewVO overview() {
        Long userId = requireUserId();
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException(ResultCode.BAD_REQUEST, "用户不存在");
        }

        UserStat stat = userStatMapper.selectById(userId);
        int questionCount = stat != null && stat.getQuestionCount() != null
                ? stat.getQuestionCount()
                : Math.toIntExact(qaQuestionMapper.selectCount(new LambdaQueryWrapper<com.community.entity.QaQuestion>()
                .eq(com.community.entity.QaQuestion::getUserId, userId)
                .eq(com.community.entity.QaQuestion::getDeleteFlag, 0)));
        int answerCount = stat != null && stat.getAnswerCount() != null
                ? stat.getAnswerCount()
                : Math.toIntExact(qaAnswerMapper.selectCount(new LambdaQueryWrapper<com.community.entity.QaAnswer>()
                .eq(com.community.entity.QaAnswer::getUserId, userId)
                .eq(com.community.entity.QaAnswer::getDeleteFlag, 0)));
        int likeReceivedCount = stat != null && stat.getLikeReceivedCount() != null
                ? stat.getLikeReceivedCount()
                : (user.getLikeReceivedCount() == null ? 0 : user.getLikeReceivedCount());
        int followerCount = stat != null && stat.getFollowerCount() != null
                ? stat.getFollowerCount()
                : (user.getFollowerCount() == null ? 0 : user.getFollowerCount());
        int followingCount = stat != null && stat.getFollowingCount() != null
                ? stat.getFollowingCount()
                : (user.getFollowingCount() == null ? 0 : user.getFollowingCount());

        int favoriteCount = Math.toIntExact(qaFavoriteMapper.selectCount(new LambdaQueryWrapper<com.community.entity.QaFavorite>()
                .eq(com.community.entity.QaFavorite::getUserId, userId)));
        int historyCount = Math.toIntExact(userBrowseHistoryMapper.selectCount(new LambdaQueryWrapper<com.community.entity.UserBrowseHistory>()
                .eq(com.community.entity.UserBrowseHistory::getUserId, userId)));

        AppMeOverviewVO vo = new AppMeOverviewVO();
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setPhone(user.getPhone());
        vo.setEmail(user.getEmail());
        vo.setExpertStatus(user.getExpertStatus());
        vo.setPasswordSet(user.getPasswordSet());
        vo.setNickname(StringUtils.hasText(user.getNickname()) ? user.getNickname() : user.getUsername());
        vo.setAvatar(user.getAvatar());
        vo.setSlogan(user.getSlogan());
        vo.setJoinedAt(user.getCreatedAt());
        vo.setQuestionCount(questionCount);
        vo.setAnswerCount(answerCount);
        vo.setLikeReceivedCount(likeReceivedCount);
        vo.setFollowerCount(followerCount);
        vo.setFollowingCount(followingCount);
        vo.setFavoriteCount(favoriteCount);
        vo.setHistoryCount(historyCount);
        return vo;
    }

    @Override
    @Transactional
    public void updateProfile(AppMeProfileUpdateDTO dto) {
        Long userId = requireUserId();
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException(ResultCode.BAD_REQUEST, "用户不存在");
        }

        if (StringUtils.hasText(dto.getNickname())) {
            user.setNickname(dto.getNickname().trim());
        }
        if (StringUtils.hasText(dto.getAvatar())) {
            user.setAvatar(dto.getAvatar().trim());
        }
        if (dto.getSlogan() != null) {
            user.setSlogan(dto.getSlogan().trim());
        }
        if (StringUtils.hasText(dto.getEmail())) {
            user.setEmail(dto.getEmail().trim());
        }
        userMapper.updateById(user);
    }

    @Override
    @Transactional
    public void setFirstPassword(AppMePasswordSetFirstDTO dto) {
        Long userId = requireUserId();
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException(ResultCode.BAD_REQUEST, "用户不存在");
        }
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new BizException(ResultCode.BAD_REQUEST, "两次密码输入不一致");
        }
        if (user.getPasswordSet() == null || user.getPasswordSet() != 0) {
            throw new BizException(ResultCode.BAD_REQUEST, "首次密码已设置");
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        user.setPasswordSet(1);
        userMapper.updateById(user);
    }

    @Override
    @Transactional
    public void changePassword(AppMePasswordChangeDTO dto) {
        Long userId = requireUserId();
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException(ResultCode.BAD_REQUEST, "用户不存在");
        }
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new BizException(ResultCode.BAD_REQUEST, "两次密码输入不一致");
        }
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new BizException(ResultCode.BAD_REQUEST, "旧密码不正确");
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        user.setPasswordSet(1);
        userMapper.updateById(user);
    }

    @Override
    public PageInfo<AppMyFavoriteItemVO> favorites(AppPageQueryDTO query) {
        Long userId = requireUserId();
        PageHelper.startPage(resolvePage(query), resolvePageSize(query));
        return new PageInfo<>(qaFavoriteMapper.selectMyFavorites(userId));
    }

    @Override
    public PageInfo<AppMyHistoryItemVO> history(AppPageQueryDTO query) {
        Long userId = requireUserId();
        PageHelper.startPage(resolvePage(query), resolvePageSize(query));
        return new PageInfo<>(userBrowseHistoryMapper.selectMyHistory(userId));
    }

    @Override
    public PageInfo<AppMyQuestionItemVO> myQuestions(AppPageQueryDTO query) {
        Long userId = requireUserId();
        PageHelper.startPage(resolvePage(query), resolvePageSize(query));
        return new PageInfo<>(qaQuestionMapper.selectMyQuestions(userId));
    }

    @Override
    public PageInfo<AppMyAnswerItemVO> myAnswers(AppPageQueryDTO query) {
        Long userId = requireUserId();
        PageHelper.startPage(resolvePage(query), resolvePageSize(query));
        return new PageInfo<>(qaAnswerMapper.selectMyAnswers(userId));
    }

    @Override
    public PageInfo<AppFollowUserItemVO> following(AppPageQueryDTO query) {
        Long userId = requireUserId();
        PageHelper.startPage(resolvePage(query), resolvePageSize(query));
        return new PageInfo<>(userFollowMapper.selectMyFollowing(userId));
    }

    @Override
    public PageInfo<AppFollowUserItemVO> followers(AppPageQueryDTO query) {
        Long userId = requireUserId();
        PageHelper.startPage(resolvePage(query), resolvePageSize(query));
        return new PageInfo<>(userFollowMapper.selectMyFollowers(userId));
    }

    @Override
    public AppDocVO doc(String type) {
        String normalized = type == null ? "" : type.trim().toLowerCase();
        AppDocVO vo = new AppDocVO();
        vo.setType(normalized);
        switch (normalized) {
            case "settings" -> {
                vo.setTitle("设置");
                vo.setContent("设置页占位内容：用于配置账号安全、通知和隐私偏好。");
            }
            case "help" -> {
                vo.setTitle("帮助与反馈");
                vo.setContent("帮助页占位内容：请通过应用内反馈入口提交问题。");
            }
            case "user-agreement" -> {
                vo.setTitle("用户协议");
                vo.setContent("用户协议占位内容：请替换为正式法律文本。");
            }
            case "privacy-policy" -> {
                vo.setTitle("隐私政策");
                vo.setContent("隐私政策占位内容：请替换为正式法律文本。");
            }
            default -> throw new BizException(ResultCode.BAD_REQUEST, "不支持的文档类型");
        }
        return vo;
    }

    private int resolvePage(AppPageQueryDTO query) {
        if (query == null || query.getPage() == null || query.getPage() <= 0) {
            return 1;
        }
        return query.getPage();
    }

    private int resolvePageSize(AppPageQueryDTO query) {
        if (query == null || query.getPageSize() == null || query.getPageSize() <= 0) {
            return 10;
        }
        return Math.min(query.getPageSize(), 50);
    }

    private Long requireUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof SecurityUser securityUser)) {
            throw new BizException(ResultCode.UNAUTHORIZED, "未授权");
        }
        return securityUser.getId();
    }
}
