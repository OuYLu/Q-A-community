package com.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.community.common.BizException;
import com.community.common.ResultCode;
import com.community.common.SecurityUser;
import com.community.dto.QaTopicCategoryUpdateDTO;
import com.community.dto.QaTopicPageQueryDTO;
import com.community.dto.QaTopicQuestionPageQueryDTO;
import com.community.dto.QaTopicSaveDTO;
import com.community.dto.QaTopicStatusUpdateDTO;
import com.community.entity.QaCategory;
import com.community.entity.QaTopic;
import com.community.entity.QaTopicCategory;
import com.community.mapper.QaCategoryMapper;
import com.community.mapper.QaTopicCategoryMapper;
import com.community.mapper.QaTopicMapper;
import com.community.service.QaTopicAdminService;
import com.community.vo.AdminTopicDetailVO;
import com.community.vo.AdminTopicListItemVO;
import com.community.vo.TopicCategoryVO;
import com.community.vo.TopicQuestionPageItemVO;
import com.community.vo.TopicRecentQuestionVO;
import com.community.vo.TopicStatsVO;
import com.community.vo.TopicTrendPointVO;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class QaTopicAdminServiceImpl extends ServiceImpl<QaTopicMapper, QaTopic> implements QaTopicAdminService {
    private final QaTopicCategoryMapper qaTopicCategoryMapper;
    private final QaCategoryMapper qaCategoryMapper;

    @Override
    public PageInfo<AdminTopicListItemVO> page(QaTopicPageQueryDTO query) {
        Integer page = query == null || query.getPage() == null ? 1 : query.getPage();
        Integer pageSize = query == null || query.getPageSize() == null ? 10 : query.getPageSize();
        PageHelper.startPage(page, pageSize);
        return new PageInfo<>(baseMapper.selectAdminTopicPage(
            query == null ? null : query.getTitle(),
            query == null ? null : query.getStatus(),
            query == null ? null : query.getCreatedBy(),
            query == null ? null : query.getDateStart(),
            query == null ? null : query.getDateEnd(),
            query == null ? null : query.getSortBy(),
            query == null ? null : query.getSortOrder()
        ));
    }

    @Override
    public AdminTopicDetailVO detail(Long id) {
        QaTopic topic = getTopicOrThrow(id);
        AdminTopicDetailVO vo = toDetailVO(topic);
        vo.setCategories(qaTopicCategoryMapper.selectCategoriesByTopicId(id));
        vo.setRecentQuestions(baseMapper.selectRecentQuestionsByTopicId(id, 10));
        return vo;
    }

    @Override
    @Transactional
    public Long create(QaTopicSaveDTO dto) {
        QaTopic topic = new QaTopic();
        topic.setTitle(dto.getTitle().trim());
        topic.setSubtitle(dto.getSubtitle());
        topic.setCoverImg(dto.getCoverImg());
        topic.setIntro(dto.getIntro());
        topic.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());
        topic.setFollowCount(0);
        topic.setQuestionCount(0);
        topic.setTodayNewCount(0);
        topic.setCreatedBy(currentUserId());
        this.save(topic);
        replaceCategories(topic.getId(), dto.getCategoryIds());
        return topic.getId();
    }

    @Override
    @Transactional
    public void update(Long id, QaTopicSaveDTO dto) {
        QaTopic topic = getTopicOrThrow(id);
        topic.setTitle(dto.getTitle().trim());
        topic.setSubtitle(dto.getSubtitle());
        topic.setCoverImg(dto.getCoverImg());
        topic.setIntro(dto.getIntro());
        if (dto.getStatus() != null) {
            topic.setStatus(dto.getStatus());
        }
        this.updateById(topic);
        if (dto.getCategoryIds() != null) {
            replaceCategories(id, dto.getCategoryIds());
        }
    }

    @Override
    @Transactional
    public void updateStatus(Long id, QaTopicStatusUpdateDTO dto) {
        QaTopic topic = getTopicOrThrow(id);
        topic.setStatus(dto.getStatus());
        this.updateById(topic);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        QaTopic topic = getTopicOrThrow(id);
        topic.setStatus(4);
        this.updateById(topic);
    }

    @Override
    public List<TopicCategoryVO> listCategories(Long id) {
        getTopicOrThrow(id);
        return qaTopicCategoryMapper.selectCategoriesByTopicId(id);
    }

    @Override
    @Transactional
    public void updateCategories(Long id, QaTopicCategoryUpdateDTO dto) {
        getTopicOrThrow(id);
        replaceCategories(id, dto == null ? null : dto.getCategoryIds());
    }

    @Override
    public List<TopicRecentQuestionVO> recentQuestions(Long id, Integer limit) {
        getTopicOrThrow(id);
        int resolvedLimit = (limit == null || limit <= 0) ? 10 : limit;
        if (resolvedLimit > 100) {
            resolvedLimit = 100;
        }
        return baseMapper.selectRecentQuestionsByTopicId(id, resolvedLimit);
    }

    @Override
    public PageInfo<TopicQuestionPageItemVO> questionPage(Long id, QaTopicQuestionPageQueryDTO query) {
        getTopicOrThrow(id);
        Integer page = query == null || query.getPage() == null ? 1 : query.getPage();
        Integer pageSize = query == null || query.getPageSize() == null ? 10 : query.getPageSize();
        PageHelper.startPage(page, pageSize);
        return new PageInfo<>(baseMapper.selectTopicQuestionPage(
            id,
            query == null ? null : query.getStatus(),
            query == null ? null : query.getTitle(),
            query == null ? null : query.getSortBy(),
            query == null ? null : query.getSortOrder()
        ));
    }

    @Override
    public List<TopicTrendPointVO> trend(Long id, Integer days) {
        getTopicOrThrow(id);
        int resolvedDays = (days == null || days <= 0) ? 7 : days;
        if (resolvedDays > 30) {
            resolvedDays = 30;
        }
        return baseMapper.selectTopicTrend(id, resolvedDays - 1);
    }

    @Override
    public TopicStatsVO stats(Long id) {
        QaTopic topic = getTopicOrThrow(id);
        TopicStatsVO vo = new TopicStatsVO();
        vo.setFollowCount(topic.getFollowCount());
        vo.setQuestionCount(topic.getQuestionCount());
        vo.setTodayNewCount(topic.getTodayNewCount());
        return vo;
    }

    private QaTopic getTopicOrThrow(Long id) {
        QaTopic topic = this.getById(id);
        if (topic == null) {
            throw new BizException(ResultCode.BAD_REQUEST, "topic not found");
        }
        return topic;
    }

    private AdminTopicDetailVO toDetailVO(QaTopic topic) {
        AdminTopicDetailVO vo = new AdminTopicDetailVO();
        vo.setId(topic.getId());
        vo.setTitle(topic.getTitle());
        vo.setSubtitle(topic.getSubtitle());
        vo.setCoverImg(topic.getCoverImg());
        vo.setIntro(topic.getIntro());
        vo.setStatus(topic.getStatus());
        vo.setFollowCount(topic.getFollowCount());
        vo.setQuestionCount(topic.getQuestionCount());
        vo.setTodayNewCount(topic.getTodayNewCount());
        vo.setCreatedBy(topic.getCreatedBy());
        vo.setCreatedAt(topic.getCreatedAt());
        vo.setUpdatedAt(topic.getUpdatedAt());
        return vo;
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

    private void replaceCategories(Long topicId, List<Long> categoryIds) {
        qaTopicCategoryMapper.delete(new LambdaQueryWrapper<QaTopicCategory>()
            .eq(QaTopicCategory::getTopicId, topicId));

        if (CollectionUtils.isEmpty(categoryIds)) {
            return;
        }
        List<Long> distinctIds = categoryIds.stream()
            .filter(Objects::nonNull)
            .filter(id -> id > 0)
            .distinct()
            .toList();
        if (distinctIds.isEmpty()) {
            return;
        }

        long validCount = qaCategoryMapper.selectCount(new LambdaQueryWrapper<QaCategory>()
            .in(QaCategory::getId, distinctIds)
            .eq(QaCategory::getDeleteFlag, 0));
        if (validCount != distinctIds.size()) {
            throw new BizException(ResultCode.BAD_REQUEST, "contains invalid category id");
        }

        List<QaTopicCategory> rows = new ArrayList<>(distinctIds.size());
        for (Long categoryId : distinctIds) {
            QaTopicCategory row = new QaTopicCategory();
            row.setTopicId(topicId);
            row.setCategoryId(categoryId);
            rows.add(row);
        }
        for (QaTopicCategory row : rows) {
            qaTopicCategoryMapper.insert(row);
        }
    }
}
