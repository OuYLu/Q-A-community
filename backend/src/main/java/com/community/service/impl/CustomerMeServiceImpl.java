package com.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.community.common.BizException;
import com.community.common.ResultCode;
import com.community.common.SecurityUser;
import com.community.dto.AppMeCancelRequestDTO;
import com.community.dto.AppMePasswordChangeDTO;
import com.community.dto.AppMePasswordSetFirstDTO;
import com.community.dto.AppMePrivacyUpdateDTO;
import com.community.dto.AppMeProfileUpdateDTO;
import com.community.dto.AppPageQueryDTO;
import com.community.entity.ExpertProfile;
import com.community.entity.UserPrivacySetting;
import com.community.entity.User;
import com.community.entity.UserStat;
import com.community.mapper.ExpertPostMapper;
import com.community.mapper.ExpertProfileMapper;
import com.community.mapper.QaAnswerMapper;
import com.community.mapper.QaFavoriteMapper;
import com.community.mapper.QaQuestionMapper;
import com.community.mapper.QaTopicFollowMapper;
import com.community.mapper.UserBrowseHistoryMapper;
import com.community.mapper.UserFollowMapper;
import com.community.mapper.UserMapper;
import com.community.mapper.UserPrivacySettingMapper;
import com.community.mapper.UserStatMapper;
import com.community.service.CustomerMeService;
import com.community.vo.AppDocVO;
import com.community.vo.AppFollowTopicItemVO;
import com.community.vo.AppFollowUserItemVO;
import com.community.vo.AppMeCancelRequestVO;
import com.community.vo.AppMeDataExportVO;
import com.community.vo.AppMeOverviewVO;
import com.community.vo.AppMePrivacyVO;
import com.community.vo.AppMyAnswerItemVO;
import com.community.vo.AppMyFavoriteItemVO;
import com.community.vo.AppMyHistoryItemVO;
import com.community.vo.AppMyQuestionItemVO;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

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
    private final QaTopicFollowMapper qaTopicFollowMapper;
    private final ExpertProfileMapper expertProfileMapper;
    private final ExpertPostMapper expertPostMapper;
    private final UserPrivacySettingMapper userPrivacySettingMapper;
    private final PasswordEncoder passwordEncoder;

    @Value("${qa.history-retention-days:30}")
    private int historyRetentionDays;

    @Override
    public AppMeOverviewVO overview() {
        Long userId = requireUserId();
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException(ResultCode.BAD_REQUEST, "user not found");
        }

        UserStat stat = userStatMapper.selectById(userId);
        int questionCount = Math.toIntExact(qaQuestionMapper.selectCount(new LambdaQueryWrapper<com.community.entity.QaQuestion>()
            .eq(com.community.entity.QaQuestion::getUserId, userId)
            .eq(com.community.entity.QaQuestion::getDeleteFlag, 0)
            .in(com.community.entity.QaQuestion::getStatus, 1, 5)));
        Long effectiveAnswerCount = qaAnswerMapper.countMyEffectiveAnswers(userId);
        int answerCount = effectiveAnswerCount == null ? 0 : Math.toIntExact(effectiveAnswerCount);
        int likeReceivedCount = Math.toIntExact(qaAnswerMapper.sumLikeCountByUserId(userId));
        int followerCount = stat != null && stat.getFollowerCount() != null
            ? stat.getFollowerCount()
            : (user.getFollowerCount() == null ? 0 : user.getFollowerCount());
        int followingCount = stat != null && stat.getFollowingCount() != null
            ? stat.getFollowingCount()
            : (user.getFollowingCount() == null ? 0 : user.getFollowingCount());
        int topicFollowCount = Math.toIntExact(qaTopicFollowMapper.selectCount(
            new LambdaQueryWrapper<com.community.entity.QaTopicFollow>()
                .eq(com.community.entity.QaTopicFollow::getUserId, userId)
        ));

        int favoriteCount = Math.toIntExact(qaFavoriteMapper.selectCount(new LambdaQueryWrapper<com.community.entity.QaFavorite>()
            .eq(com.community.entity.QaFavorite::getUserId, userId)));
        LocalDateTime retainedFrom = LocalDateTime.now().minusDays(Math.max(1, historyRetentionDays));
        Long historyCountValue = userBrowseHistoryMapper.countMyHistory(userId, retainedFrom);
        int historyCount = historyCountValue == null ? 0 : Math.toIntExact(historyCountValue);

        AppMeOverviewVO vo = new AppMeOverviewVO();
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setPhone(user.getPhone());
        vo.setEmail(user.getEmail());
        vo.setExpertStatus(user.getExpertStatus());
        if (user.getExpertStatus() != null && user.getExpertStatus() == 3) {
            ExpertProfile profile = expertProfileMapper.selectOne(new LambdaQueryWrapper<ExpertProfile>()
                .eq(ExpertProfile::getUserId, userId)
                .last("LIMIT 1"));
            if (profile != null) {
                vo.setExpertTitle(profile.getTitle());
                vo.setExpertExpertise(profile.getExpertise());
            }
        }
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
        vo.setTopicFollowCount(topicFollowCount);
        vo.setFavoriteCount(favoriteCount);
        vo.setHistoryCount(historyCount);
        if (user.getExpertStatus() != null && user.getExpertStatus() == 3) {
            Long postCountValue = expertPostMapper.countMyPosts(userId);
            int postCount = postCountValue == null ? 0 : Math.toIntExact(postCountValue);
            vo.setExpertPostCount(postCount);
        } else {
            vo.setExpertPostCount(0);
        }
        return vo;
    }

    @Override
    @Transactional
    public void updateProfile(AppMeProfileUpdateDTO dto) {
        Long userId = requireUserId();
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException(ResultCode.BAD_REQUEST, "user not found");
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
            throw new BizException(ResultCode.BAD_REQUEST, "user not found");
        }
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new BizException(ResultCode.BAD_REQUEST, "password confirm mismatch");
        }
        if (user.getPasswordSet() == null || user.getPasswordSet() != 0) {
            throw new BizException(ResultCode.BAD_REQUEST, "password already set");
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
            throw new BizException(ResultCode.BAD_REQUEST, "user not found");
        }
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new BizException(ResultCode.BAD_REQUEST, "password confirm mismatch");
        }
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new BizException(ResultCode.BAD_REQUEST, "old password incorrect");
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
        LocalDateTime retainedFrom = LocalDateTime.now().minusDays(Math.max(1, historyRetentionDays));
        PageHelper.startPage(resolvePage(query), resolvePageSize(query));
        return new PageInfo<>(userBrowseHistoryMapper.selectMyHistory(userId, retainedFrom));
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
    public PageInfo<AppFollowTopicItemVO> followedTopics(AppPageQueryDTO query) {
        Long userId = requireUserId();
        PageHelper.startPage(resolvePage(query), resolvePageSize(query));
        return new PageInfo<>(qaTopicFollowMapper.selectMyFollowedTopics(userId));
    }

    @Override
    @Transactional
    public AppMePrivacyVO privacy() {
        Long userId = requireUserId();
        UserPrivacySetting setting = findOrCreatePrivacySetting(userId);
        return toPrivacyVO(setting);
    }

    @Override
    @Transactional
    public void updatePrivacy(AppMePrivacyUpdateDTO dto) {
        Long userId = requireUserId();
        validateBoolFlag(dto.getProfileVisible(), "profileVisible");
        validateBoolFlag(dto.getStatsVisible(), "statsVisible");
        validateBoolFlag(dto.getPersonalizedRecommend(), "personalizedRecommend");

        UserPrivacySetting setting = findOrCreatePrivacySetting(userId);
        setting.setProfileVisible(dto.getProfileVisible());
        setting.setStatsVisible(dto.getStatsVisible());
        setting.setPersonalizedRecommend(dto.getPersonalizedRecommend());
        userPrivacySettingMapper.updateById(setting);
    }

    @Override
    public AppMeDataExportVO exportData() {
        Long userId = requireUserId();
        LocalDateTime retainedFrom = LocalDateTime.now().minusDays(Math.max(1, historyRetentionDays));

        AppMeDataExportVO vo = new AppMeDataExportVO();
        vo.setExportedAt(LocalDateTime.now());
        vo.setOverview(overview());
        vo.setPrivacy(toPrivacyVO(findOrCreatePrivacySetting(userId)));
        vo.setRecentQuestions(limitList(qaQuestionMapper.selectMyQuestions(userId), 200));
        vo.setRecentAnswers(limitList(qaAnswerMapper.selectMyAnswers(userId), 200));
        vo.setRecentFavorites(limitList(qaFavoriteMapper.selectMyFavorites(userId), 200));
        vo.setRecentHistory(limitList(userBrowseHistoryMapper.selectMyHistory(userId, retainedFrom), 200));
        vo.setFollowing(limitList(userFollowMapper.selectMyFollowing(userId), 200));
        vo.setFollowers(limitList(userFollowMapper.selectMyFollowers(userId), 200));
        vo.setFollowedTopics(limitList(qaTopicFollowMapper.selectMyFollowedTopics(userId), 200));
        return vo;
    }

    @Override
    @Transactional
    public void submitCancelRequest(AppMeCancelRequestDTO dto) {
        Long userId = requireUserId();
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException(ResultCode.BAD_REQUEST, "user not found");
        }
        if (user.getStatus() != null && user.getStatus() == User.STATUS_DISABLED) {
            return;
        }

        user.setStatus(User.STATUS_DISABLED);
        user.setPhone(null);
        user.setEmail(null);
        user.setAvatar(null);
        user.setSlogan(null);
        user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        user.setPasswordSet(1);
        userMapper.updateById(user);

        UserPrivacySetting setting = findOrCreatePrivacySetting(userId);
        setting.setProfileVisible(0);
        setting.setStatsVisible(0);
        setting.setPersonalizedRecommend(0);
        userPrivacySettingMapper.updateById(setting);
    }

    @Override
    public AppMeCancelRequestVO latestCancelRequest() {
        requireUserId();
        return null;
    }

    @Override
    public AppDocVO doc(String type) {
        String normalized = type == null ? "" : type.trim().toLowerCase();
        AppDocVO vo = new AppDocVO();
        vo.setType(normalized);
        switch (normalized) {
            case "settings" -> {
                vo.setTitle("settings");
                vo.setContent("settings placeholder");
            }
            case "help" -> {
                vo.setTitle("help");
                vo.setContent("help placeholder");
            }
            case "user-agreement" -> {
                vo.setTitle("user agreement");
                vo.setContent("user agreement placeholder");
            }
            case "privacy-policy" -> {
                vo.setTitle("privacy policy");
                vo.setContent("privacy policy placeholder");
            }
            default -> throw new BizException(ResultCode.BAD_REQUEST, "unsupported doc type");
        }
        return vo;
    }
    private UserPrivacySetting findOrCreatePrivacySetting(Long userId) {
        UserPrivacySetting setting = userPrivacySettingMapper.selectOne(new LambdaQueryWrapper<UserPrivacySetting>()
            .eq(UserPrivacySetting::getUserId, userId)
            .last("LIMIT 1"));
        if (setting != null) {
            return setting;
        }
        UserPrivacySetting created = new UserPrivacySetting();
        created.setUserId(userId);
        created.setProfileVisible(1);
        created.setStatsVisible(1);
        created.setPersonalizedRecommend(1);
        userPrivacySettingMapper.insert(created);
        return userPrivacySettingMapper.selectOne(new LambdaQueryWrapper<UserPrivacySetting>()
            .eq(UserPrivacySetting::getUserId, userId)
            .last("LIMIT 1"));
    }

    private AppMePrivacyVO toPrivacyVO(UserPrivacySetting setting) {
        AppMePrivacyVO vo = new AppMePrivacyVO();
        if (setting == null) {
            vo.setProfileVisible(1);
            vo.setStatsVisible(1);
            vo.setPersonalizedRecommend(1);
            vo.setUpdatedAt(null);
            return vo;
        }
        vo.setProfileVisible(setting.getProfileVisible());
        vo.setStatsVisible(setting.getStatsVisible());
        vo.setPersonalizedRecommend(setting.getPersonalizedRecommend());
        vo.setUpdatedAt(setting.getUpdatedAt());
        return vo;
    }

    private void validateBoolFlag(Integer value, String field) {
        if (value == null || (value != 0 && value != 1)) {
            throw new BizException(ResultCode.BAD_REQUEST, field + " only supports 0 or 1");
        }
    }

    private <T> List<T> limitList(List<T> source, int maxSize) {
        if (source == null || source.isEmpty()) {
            return List.of();
        }
        if (source.size() <= maxSize) {
            return source;
        }
        return source.subList(0, maxSize);
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
            throw new BizException(ResultCode.UNAUTHORIZED, "unauthorized");
        }
        return securityUser.getId();
    }
}


