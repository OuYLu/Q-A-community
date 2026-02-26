package com.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.community.common.BizException;
import com.community.common.ResultCode;
import com.community.common.SecurityUser;
import com.community.dto.AppTopicPageQueryDTO;
import com.community.dto.AppTopicQuestionQueryDTO;
import com.community.entity.QaTopic;
import com.community.entity.QaTopicFollow;
import com.community.mapper.QaQuestionMapper;
import com.community.mapper.QaTopicFollowMapper;
import com.community.mapper.QaTopicMapper;
import com.community.service.CustomerTopicService;
import com.community.vo.AppTopicDetailVO;
import com.community.vo.AppTopicListItemVO;
import com.community.vo.AppTopicQuestionItemVO;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class CustomerTopicServiceImpl implements CustomerTopicService {
    private final QaTopicMapper qaTopicMapper;
    private final QaTopicFollowMapper qaTopicFollowMapper;
    private final QaQuestionMapper qaQuestionMapper;

    @Override
    public PageInfo<AppTopicListItemVO> page(AppTopicPageQueryDTO query) {
        Integer page = query == null || query.getPage() == null ? 1 : query.getPage();
        Integer pageSize = query == null || query.getPageSize() == null ? 10 : query.getPageSize();
        PageHelper.startPage(page, pageSize);
        return new PageInfo<>(qaTopicMapper.selectAppTopicPage(
                query == null ? null : query.getKeyword(),
                query == null ? null : query.getSortBy(),
                query == null ? null : query.getSortOrder()
        ));
    }

    @Override
    public AppTopicDetailVO detail(Long id) {
        QaTopic topic = qaTopicMapper.selectById(id);
        if (topic == null || topic.getStatus() == null || topic.getStatus() != 1) {
            throw new BizException(ResultCode.BAD_REQUEST, "话题不存在");
        }
        AppTopicDetailVO vo = new AppTopicDetailVO();
        vo.setId(topic.getId());
        vo.setTitle(topic.getTitle());
        vo.setSubtitle(topic.getSubtitle());
        vo.setCoverImg(topic.getCoverImg());
        vo.setIntro(topic.getIntro());
        vo.setFollowCount(topic.getFollowCount());
        vo.setQuestionCount(topic.getQuestionCount());
        vo.setTodayNewCount(topic.getTodayNewCount());
        vo.setFollowed(isFollowed(topic.getId(), currentUserId()));
        return vo;
    }

    @Override
    public PageInfo<AppTopicQuestionItemVO> topicQuestions(Long topicId, AppTopicQuestionQueryDTO query) {
        QaTopic topic = qaTopicMapper.selectById(topicId);
        if (topic == null || topic.getStatus() == null || topic.getStatus() != 1) {
            throw new BizException(ResultCode.BAD_REQUEST, "话题不存在");
        }
        Integer page = query == null || query.getPage() == null ? 1 : query.getPage();
        Integer pageSize = query == null || query.getPageSize() == null ? 10 : query.getPageSize();
        String sortBy = query == null ? null : query.getSortBy();
        boolean onlyUnsolved = "unsolved".equalsIgnoreCase(sortBy);
        String resolvedSort = StringUtils.hasText(sortBy) ? sortBy : "hot";
        PageHelper.startPage(page, pageSize);
        return new PageInfo<>(qaQuestionMapper.selectAppTopicQuestions(topicId, resolvedSort, onlyUnsolved));
    }

    @Override
    @Transactional
    public void follow(Long topicId) {
        Long userId = currentUserId();
        if (userId == null) {
            throw new BizException(ResultCode.UNAUTHORIZED, "未授权");
        }
        QaTopic topic = qaTopicMapper.selectById(topicId);
        if (topic == null || topic.getStatus() == null || topic.getStatus() != 1) {
            throw new BizException(ResultCode.BAD_REQUEST, "话题不存在");
        }
        boolean exists = qaTopicFollowMapper.selectCount(new LambdaQueryWrapper<QaTopicFollow>()
                .eq(QaTopicFollow::getTopicId, topicId)
                .eq(QaTopicFollow::getUserId, userId)) > 0;
        if (exists) {
            return;
        }
        QaTopicFollow follow = new QaTopicFollow();
        follow.setTopicId(topicId);
        follow.setUserId(userId);
        qaTopicFollowMapper.insert(follow);
        qaTopicMapper.updateFollowCount(topicId, 1);
    }

    @Override
    @Transactional
    public void unfollow(Long topicId) {
        Long userId = currentUserId();
        if (userId == null) {
            throw new BizException(ResultCode.UNAUTHORIZED, "未授权");
        }
        int deleted = qaTopicFollowMapper.delete(new LambdaQueryWrapper<QaTopicFollow>()
                .eq(QaTopicFollow::getTopicId, topicId)
                .eq(QaTopicFollow::getUserId, userId));
        if (deleted > 0) {
            qaTopicMapper.updateFollowCount(topicId, -1);
        }
    }

    private boolean isFollowed(Long topicId, Long userId) {
        if (userId == null) {
            return false;
        }
        return qaTopicFollowMapper.selectCount(new LambdaQueryWrapper<QaTopicFollow>()
                .eq(QaTopicFollow::getTopicId, topicId)
                .eq(QaTopicFollow::getUserId, userId)) > 0;
    }

    private Long currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof SecurityUser securityUser) {
            return securityUser.getId();
        }
        return null;
    }
}
