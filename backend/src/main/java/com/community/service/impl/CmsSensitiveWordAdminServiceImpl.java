package com.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.community.common.BizException;
import com.community.common.ResultCode;
import com.community.dto.CmsSensitiveWordQueryDTO;
import com.community.dto.CmsSensitiveWordSaveDTO;
import com.community.entity.CmsSensitiveWord;
import com.community.mapper.CmsSensitiveWordMapper;
import com.community.service.CmsSensitiveWordAdminService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class CmsSensitiveWordAdminServiceImpl extends ServiceImpl<CmsSensitiveWordMapper, CmsSensitiveWord>
    implements CmsSensitiveWordAdminService {

    @Override
    public PageInfo<CmsSensitiveWord> list(CmsSensitiveWordQueryDTO query) {
        int pageNum = query == null || query.getPageNum() == null || query.getPageNum() <= 0 ? 1 : query.getPageNum();
        int pageSize = query == null || query.getPageSize() == null || query.getPageSize() <= 0 ? 10 : query.getPageSize();

        LambdaQueryWrapper<CmsSensitiveWord> wrapper = new LambdaQueryWrapper<>();
        if (query != null) {
            if (StringUtils.hasText(query.getKeyword())) {
                String keyword = query.getKeyword().trim();
                wrapper.and(w -> w.like(CmsSensitiveWord::getWord, keyword)
                    .or()
                    .like(CmsSensitiveWord::getCategory, keyword));
            }
            if (query.getLevel() != null) {
                wrapper.eq(CmsSensitiveWord::getLevel, query.getLevel());
            }
            if (query.getEnabled() != null) {
                wrapper.eq(CmsSensitiveWord::getEnabled, query.getEnabled());
            }
            if (StringUtils.hasText(query.getCategory())) {
                wrapper.like(CmsSensitiveWord::getCategory, query.getCategory().trim());
            }
        }
        wrapper.orderByDesc(CmsSensitiveWord::getCreatedAt).orderByDesc(CmsSensitiveWord::getId);

        PageHelper.startPage(pageNum, Math.min(pageSize, 100));
        return new PageInfo<>(this.list(wrapper));
    }

    @Override
    @Transactional
    public CmsSensitiveWord create(CmsSensitiveWordSaveDTO dto) {
        String word = normalizeWord(dto.getWord());
        ensureWordUnique(word, null);
        CmsSensitiveWord entity = new CmsSensitiveWord();
        applyDto(entity, dto, word);
        this.save(entity);
        return entity;
    }

    @Override
    @Transactional
    public CmsSensitiveWord update(Long id, CmsSensitiveWordSaveDTO dto) {
        CmsSensitiveWord entity = getById(id);
        String word = normalizeWord(dto.getWord());
        ensureWordUnique(word, id);
        applyDto(entity, dto, word);
        this.updateById(entity);
        return entity;
    }

    @Override
    public CmsSensitiveWord getById(Long id) {
        CmsSensitiveWord row = super.getById(id);
        if (row == null) {
            throw new BizException(ResultCode.BAD_REQUEST, "敏感词不存在");
        }
        return row;
    }

    @Override
    @Transactional
    public void batchEnable(List<Long> ids) {
        batchUpdateEnabled(ids, 1);
    }

    @Override
    @Transactional
    public void batchDisable(List<Long> ids) {
        batchUpdateEnabled(ids, 0);
    }

    private void batchUpdateEnabled(List<Long> ids, int enabled) {
        if (ids == null || ids.isEmpty()) {
            throw new BizException(ResultCode.BAD_REQUEST, "编号列表不能为空");
        }
        List<Long> validIds = ids.stream().filter(id -> id != null && id > 0).distinct().toList();
        if (validIds.isEmpty()) {
            throw new BizException(ResultCode.BAD_REQUEST, "编号列表不能为空");
        }
        CmsSensitiveWord patch = new CmsSensitiveWord();
        patch.setEnabled(enabled);
        this.update(patch, new LambdaQueryWrapper<CmsSensitiveWord>().in(CmsSensitiveWord::getId, validIds));
    }

    private void applyDto(CmsSensitiveWord target, CmsSensitiveWordSaveDTO dto, String normalizedWord) {
        target.setWord(normalizedWord);
        target.setLevel(dto.getLevel());
        target.setCategory(trimOrNull(dto.getCategory()));
        target.setHitActionDesc(trimOrNull(dto.getHitActionDesc()));
        target.setReasonTemplate(trimOrNull(dto.getReasonTemplate()));
        target.setEnabled(dto.getEnabled() == null ? 1 : dto.getEnabled());
    }

    private String normalizeWord(String raw) {
        String word = raw == null ? "" : raw.trim();
        if (!StringUtils.hasText(word)) {
            throw new BizException(ResultCode.BAD_REQUEST, "敏感词不能为空");
        }
        if (word.length() > 100) {
            throw new BizException(ResultCode.BAD_REQUEST, "敏感词长度不能超过100");
        }
        return word;
    }

    private void ensureWordUnique(String word, Long excludeId) {
        List<CmsSensitiveWord> exists = this.list(new LambdaQueryWrapper<CmsSensitiveWord>()
            .eq(CmsSensitiveWord::getWord, word));
        if (exists == null || exists.isEmpty()) {
            return;
        }
        for (CmsSensitiveWord item : exists) {
            if (item == null || item.getId() == null) {
                continue;
            }
            if (excludeId == null || !excludeId.equals(item.getId())) {
                throw new BizException(ResultCode.BAD_REQUEST, "敏感词已存在");
            }
        }
    }

    private String trimOrNull(String text) {
        if (!StringUtils.hasText(text)) {
            return null;
        }
        return text.trim().replaceAll("\\s+", " ");
    }
}
