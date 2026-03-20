package com.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.community.common.BizException;
import com.community.common.ResultCode;
import com.community.common.SecurityUser;
import com.community.dto.AppPageQueryDTO;
import com.community.entity.NotifyMessage;
import com.community.entity.QaQuestion;
import com.community.entity.User;
import com.community.entity.UserFollow;
import com.community.entity.UserPrivacySetting;
import com.community.entity.UserStat;
import com.community.mapper.ExpertPostMapper;
import com.community.mapper.NotifyMessageMapper;
import com.community.mapper.QaAnswerMapper;
import com.community.mapper.QaQuestionMapper;
import com.community.mapper.UserFollowMapper;
import com.community.mapper.UserMapper;
import com.community.mapper.UserPrivacySettingMapper;
import com.community.mapper.UserStatMapper;
import com.community.service.CustomerUserService;
import com.community.vo.AppExpertPostItemVO;
import com.community.vo.AppMyAnswerItemVO;
import com.community.vo.AppUserHomeVO;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerUserServiceImpl implements CustomerUserService {
    private final UserMapper userMapper;
    private final UserFollowMapper userFollowMapper;
    private final UserStatMapper userStatMapper;
    private final QaQuestionMapper qaQuestionMapper;
    private final QaAnswerMapper qaAnswerMapper;
    private final NotifyMessageMapper notifyMessageMapper;
    private final ExpertPostMapper expertPostMapper;
    private final UserPrivacySettingMapper userPrivacySettingMapper;

    @Override
    public AppUserHomeVO home(Long userId) {
        Long currentUserId = requireUserId();
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException(ResultCode.BAD_REQUEST, "用户不存在");
        }
        boolean self = currentUserId.equals(userId);
        boolean profileVisible = self || isProfileVisible(userId);
        boolean statsVisible = profileVisible;

        int questionCount = 0;
        int answerCount = 0;
        int followerCount = 0;
        int followingCount = 0;
        if (statsVisible) {
            LambdaQueryWrapper<QaQuestion> questionCountQuery = new LambdaQueryWrapper<QaQuestion>()
                .eq(QaQuestion::getUserId, userId)
                .eq(QaQuestion::getDeleteFlag, 0);
            if (self) {
                questionCountQuery.in(QaQuestion::getStatus, QaQuestion.STATUS_PUBLISHED, QaQuestion.STATUS_SELF_ONLY);
            } else {
                questionCountQuery.eq(QaQuestion::getStatus, QaQuestion.STATUS_PUBLISHED);
            }
            questionCount = Math.toIntExact(qaQuestionMapper.selectCount(questionCountQuery));
            Long effectiveAnswerCount = self
                ? qaAnswerMapper.countMyEffectiveAnswers(userId)
                : qaAnswerMapper.countUserEffectiveAnswers(userId);
            answerCount = effectiveAnswerCount == null ? 0 : Math.toIntExact(effectiveAnswerCount);
            followerCount = Math.toIntExact(userFollowMapper.selectCount(new LambdaQueryWrapper<UserFollow>()
                .eq(UserFollow::getFolloweeId, userId)));
            followingCount = Math.toIntExact(userFollowMapper.selectCount(new LambdaQueryWrapper<UserFollow>()
                .eq(UserFollow::getFollowerId, userId)));
        }

        boolean followed = !self && userFollowMapper.selectCount(new LambdaQueryWrapper<UserFollow>()
            .eq(UserFollow::getFollowerId, currentUserId)
            .eq(UserFollow::getFolloweeId, userId)) > 0;

        AppUserHomeVO vo = new AppUserHomeVO();
        vo.setUserId(user.getId());
        vo.setNickname(resolveNickname(user));
        vo.setAvatar(user.getAvatar());
        vo.setSlogan(user.getSlogan());
        vo.setExpertStatus(user.getExpertStatus());
        if (statsVisible && user.getExpertStatus() != null && user.getExpertStatus() == 3) {
            Long postCountValue = expertPostMapper.countPublishedByAuthor(userId);
            int postCount = postCountValue == null ? 0 : Math.toIntExact(postCountValue);
            vo.setExpertPostCount(postCount);
        } else {
            vo.setExpertPostCount(0);
        }
        vo.setQuestionCount(questionCount);
        vo.setAnswerCount(answerCount);
        vo.setFollowerCount(followerCount);
        vo.setFollowingCount(followingCount);
        vo.setFollowed(followed);
        vo.setSelf(self);
        return vo;
    }

    @Override
    @Transactional
    public void follow(Long userId) {
        Long currentUserId = requireUserId();
        if (currentUserId.equals(userId)) {
            throw new BizException(ResultCode.BAD_REQUEST, "不能关注自己");
        }
        User target = userMapper.selectById(userId);
        if (target == null) {
            throw new BizException(ResultCode.BAD_REQUEST, "用户不存在");
        }

        boolean exists = userFollowMapper.selectCount(new LambdaQueryWrapper<UserFollow>()
            .eq(UserFollow::getFollowerId, currentUserId)
            .eq(UserFollow::getFolloweeId, userId)) > 0;
        if (exists) {
            return;
        }

        UserFollow follow = new UserFollow();
        follow.setFollowerId(currentUserId);
        follow.setFolloweeId(userId);
        follow.setCreatedAt(LocalDateTime.now());
        userFollowMapper.insert(follow);

        adjustUserFollowCount(currentUserId, 1, true);
        adjustUserFollowCount(userId, 1, false);
        adjustUserStat(currentUserId, 1, true);
        adjustUserStat(userId, 1, false);
        createFollowNotify(userId, currentUserId);
    }

    @Override
    @Transactional
    public void unfollow(Long userId) {
        Long currentUserId = requireUserId();
        if (currentUserId.equals(userId)) {
            throw new BizException(ResultCode.BAD_REQUEST, "不能取消关注自己");
        }

        int deleted = userFollowMapper.delete(new LambdaQueryWrapper<UserFollow>()
            .eq(UserFollow::getFollowerId, currentUserId)
            .eq(UserFollow::getFolloweeId, userId));
        if (deleted <= 0) {
            return;
        }

        adjustUserFollowCount(currentUserId, -1, true);
        adjustUserFollowCount(userId, -1, false);
        adjustUserStat(currentUserId, -1, true);
        adjustUserStat(userId, -1, false);
    }

    @Override
    public PageInfo<AppMyAnswerItemVO> answers(Long userId, AppPageQueryDTO query) {
        Long currentUserId = requireUserId();
        User target = userMapper.selectById(userId);
        if (target == null) {
            throw new BizException(ResultCode.BAD_REQUEST, "用户不存在");
        }
        if (!currentUserId.equals(userId) && !isProfileVisible(userId)) {
            return new PageInfo<>(List.of());
        }
        int page = query == null || query.getPage() == null || query.getPage() <= 0 ? 1 : query.getPage();
        int pageSize = query == null || query.getPageSize() == null || query.getPageSize() <= 0 ? 10 : query.getPageSize();
        PageHelper.startPage(page, Math.min(pageSize, 50));
        return new PageInfo<>(qaAnswerMapper.selectUserEffectiveAnswers(userId));
    }

    @Override
    public PageInfo<AppExpertPostItemVO> expertPosts(Long userId, AppPageQueryDTO query) {
        Long currentUserId = requireUserId();
        User target = userMapper.selectById(userId);
        if (target == null) {
            throw new BizException(ResultCode.BAD_REQUEST, "用户不存在");
        }
        if (!currentUserId.equals(userId) && !isProfileVisible(userId)) {
            return new PageInfo<>(List.of());
        }
        if (target.getExpertStatus() == null || target.getExpertStatus() != 3) {
            return new PageInfo<>(List.of());
        }
        int page = query == null || query.getPage() == null || query.getPage() <= 0 ? 1 : query.getPage();
        int pageSize = query == null || query.getPageSize() == null || query.getPageSize() <= 0 ? 10 : query.getPageSize();
        PageHelper.startPage(page, Math.min(pageSize, 50));
        return new PageInfo<>(expertPostMapper.selectPublishedByAuthor(userId));
    }

    private void adjustUserFollowCount(Long userId, int delta, boolean following) {
        String field = following ? "following_count" : "follower_count";
        userMapper.update(null, new LambdaUpdateWrapper<User>()
            .eq(User::getId, userId)
            .setSql(field + " = GREATEST(IFNULL(" + field + ", 0) + (" + delta + "), 0)"));
    }

    private void adjustUserStat(Long userId, int delta, boolean following) {
        UserStat stat = userStatMapper.selectById(userId);
        if (stat == null) {
            stat = new UserStat();
            stat.setUserId(userId);
            stat.setQuestionCount(0);
            stat.setAnswerCount(0);
            stat.setLikeReceivedCount(0);
            stat.setFollowerCount(following ? 0 : Math.max(0, delta));
            stat.setFollowingCount(following ? Math.max(0, delta) : 0);
            stat.setUpdatedAt(LocalDateTime.now());
            userStatMapper.insert(stat);
            return;
        }
        int oldValue = following
            ? (stat.getFollowingCount() == null ? 0 : stat.getFollowingCount())
            : (stat.getFollowerCount() == null ? 0 : stat.getFollowerCount());
        int next = Math.max(0, oldValue + delta);
        if (following) {
            stat.setFollowingCount(next);
        } else {
            stat.setFollowerCount(next);
        }
        stat.setUpdatedAt(LocalDateTime.now());
        userStatMapper.updateById(stat);
    }

    private void createFollowNotify(Long receiverId, Long followerId) {
        if (receiverId == null || receiverId.equals(followerId)) {
            return;
        }
        NotifyMessage notify = new NotifyMessage();
        notify.setReceiverId(receiverId);
        notify.setType(NotifyMessage.TYPE_FOLLOW);
        notify.setBizType(4);
        notify.setBizId(followerId);
        notify.setTitle("新关注");
        notify.setContent(actorName(followerId) + " 关注了你");
        notify.setIsRead(0);
        notifyMessageMapper.insert(notify);
    }

    private String actorName(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return "用户";
        }
        if (StringUtils.hasText(user.getNickname())) {
            return user.getNickname().trim();
        }
        if (StringUtils.hasText(user.getUsername())) {
            return user.getUsername().trim();
        }
        return "用户";
    }

    private String resolveNickname(User user) {
        if (user == null) {
            return "用户";
        }
        if (StringUtils.hasText(user.getNickname())) {
            return user.getNickname().trim();
        }
        if (StringUtils.hasText(user.getUsername())) {
            return user.getUsername().trim();
        }
        return "用户";
    }

    private boolean isProfileVisible(Long userId) {
        UserPrivacySetting setting = userPrivacySettingMapper.selectOne(new LambdaQueryWrapper<UserPrivacySetting>()
            .eq(UserPrivacySetting::getUserId, userId)
            .last("LIMIT 1"));
        if (setting == null || setting.getProfileVisible() == null) {
            return true;
        }
        return setting.getProfileVisible() == 1;
    }

    private Long requireUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof SecurityUser securityUser)) {
            throw new BizException(ResultCode.UNAUTHORIZED, "未授权");
        }
        return securityUser.getId();
    }
}
