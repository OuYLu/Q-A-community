package com.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.community.common.BizException;
import com.community.common.ResultCode;
import com.community.dto.QaTagQueryDTO;
import com.community.dto.QaTagSaveDTO;
import com.community.entity.CmsSensitiveWord;
import com.community.entity.QaTag;
import com.community.mapper.CmsSensitiveWordMapper;
import com.community.mapper.QaTagMapper;
import com.community.service.QaTagAdminService;
import com.community.vo.TagDetailExtraVO;
import com.community.vo.TagRecentQuestionVO;
import com.community.vo.TagUsageTrendPointVO;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Locale;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QaTagAdminServiceImpl extends ServiceImpl<QaTagMapper, QaTag> implements QaTagAdminService {
    private final CmsSensitiveWordMapper sensitiveWordMapper;

    @Override
    public PageInfo<QaTag> list(QaTagQueryDTO query) {
        String name = query == null ? null : query.getName();
        Integer source = query == null ? null : query.getSource();
        Integer status = query == null ? null : query.getStatus();
        Integer useCountMin = query == null ? null : query.getUseCountMin();
        Integer useCountMax = query == null ? null : query.getUseCountMax();
        Integer pageNum = query == null || query.getPageNum() == null ? 1 : query.getPageNum();
        Integer pageSize = query == null || query.getPageSize() == null ? 10 : query.getPageSize();

        if (useCountMin != null && useCountMax != null && useCountMin > useCountMax) {
            throw new BizException(ResultCode.BAD_REQUEST, "useCountMin cannot be greater than useCountMax");
        }

        LambdaQueryWrapper<QaTag> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(name)) {
            wrapper.like(QaTag::getName, name.trim());
        }
        if (source != null) {
            wrapper.eq(QaTag::getSource, source);
        }
        if (status != null) {
            wrapper.eq(QaTag::getStatus, status);
        }
        if (useCountMin != null) {
            wrapper.ge(QaTag::getUseCount, useCountMin);
        }
        if (useCountMax != null) {
            wrapper.le(QaTag::getUseCount, useCountMax);
        }
        wrapper.orderByDesc(QaTag::getUseCount).orderByDesc(QaTag::getCreatedAt);

        PageHelper.startPage(pageNum, pageSize);
        return new PageInfo<>(this.list(wrapper));
    }

    @Override
    @Transactional
    public QaTag create(QaTagSaveDTO dto) {
        String name = normalizeTagName(dto.getName());
        blockIfSensitive(name);
        ensureNameUnique(name, null);

        QaTag tag = new QaTag();
        tag.setName(name);
        tag.setSource(dto.getSource());
        tag.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());
        tag.setUseCount(0);
        this.save(tag);
        return tag;
    }

    @Override
    @Transactional
    public QaTag update(Long id, QaTagSaveDTO dto) {
        QaTag tag = getById(id);
        String name = normalizeTagName(dto.getName());
        blockIfSensitive(name);
        ensureNameUnique(name, id);

        tag.setName(name);
        tag.setSource(dto.getSource());
        if (dto.getStatus() != null) {
            tag.setStatus(dto.getStatus());
        }
        this.updateById(tag);
        return tag;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        QaTag tag = getById(id);
        tag.setStatus(0);
        this.updateById(tag);
    }

    @Override
    @Transactional
    public void batchEnable(List<Long> ids) {
        batchUpdateStatus(ids, 1);
    }

    @Override
    @Transactional
    public void batchDisable(List<Long> ids) {
        batchUpdateStatus(ids, 0);
    }

    @Override
    public QaTag getById(Long id) {
        QaTag tag = super.getById(id);
        if (tag == null) {
            throw new BizException(ResultCode.BAD_REQUEST, "tag not found");
        }
        return tag;
    }

    @Override
    public TagDetailExtraVO getDetailExtra(Long id) {
        getById(id);
        List<TagRecentQuestionVO> recentQuestions = baseMapper.selectRecentQuestions(id);

        TagDetailExtraVO detail = new TagDetailExtraVO();
        detail.setTagId(id);
        detail.setQuestionManagePath("/content/audit");
        detail.setQuestionManageQueryKey("tagId");
        detail.setQuestionManageQueryValue(id);
        detail.setQuestionManageUrl("/content/audit?tagId=" + id);
        detail.setRecentQuestions(recentQuestions);
        return detail;
    }

    @Override
    public List<TagUsageTrendPointVO> usageTrend(Long id, Integer days) {
        getById(id);
        int resolvedDays = (days == null || days <= 0) ? 7 : days;
        if (resolvedDays > 30) {
            resolvedDays = 30;
        }
        return baseMapper.selectUsageTrend(id, resolvedDays - 1);
    }

    private void ensureNameUnique(String name, Long excludeId) {
        LambdaQueryWrapper<QaTag> wrapper = new LambdaQueryWrapper<QaTag>()
            .eq(QaTag::getName, name);
        if (excludeId != null) {
            wrapper.ne(QaTag::getId, excludeId);
        }
        QaTag existed = this.getOne(wrapper.last("LIMIT 1"));
        if (existed != null) {
            throw new BizException(ResultCode.BAD_REQUEST, "tag name already exists");
        }
    }

    private String normalizeTagName(String raw) {
        String value = raw == null ? "" : raw.trim().replaceAll("\\s+", " ");
        value = value.toLowerCase(Locale.ROOT);
        if (value.isEmpty()) {
            throw new BizException(ResultCode.BAD_REQUEST, "tag name cannot be empty");
        }
        if (value.length() > 50) {
            throw new BizException(ResultCode.BAD_REQUEST, "tag name is too long");
        }
        return value;
    }

    private void blockIfSensitive(String name) {
        List<CmsSensitiveWord> words = sensitiveWordMapper.selectList(new LambdaQueryWrapper<CmsSensitiveWord>()
            .eq(CmsSensitiveWord::getEnabled, 1));
        for (CmsSensitiveWord word : words) {
            String hit = word.getWord();
            if (!StringUtils.hasText(hit)) {
                continue;
            }
            if (name.contains(hit)) {
                throw new BizException(ResultCode.BAD_REQUEST, "tag contains sensitive word: " + hit);
            }
        }
    }

    private void batchUpdateStatus(List<Long> ids, int status) {
        if (ids == null || ids.isEmpty()) {
            throw new BizException(ResultCode.BAD_REQUEST, "ids cannot be empty");
        }
        List<Long> validIds = ids.stream().filter(id -> id != null && id > 0).distinct().toList();
        if (validIds.isEmpty()) {
            throw new BizException(ResultCode.BAD_REQUEST, "ids cannot be empty");
        }
        QaTag updateEntity = new QaTag();
        updateEntity.setStatus(status);
        this.update(updateEntity, new LambdaQueryWrapper<QaTag>().in(QaTag::getId, validIds));
    }
}
