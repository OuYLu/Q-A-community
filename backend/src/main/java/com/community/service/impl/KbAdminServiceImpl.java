package com.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.community.common.BizException;
import com.community.common.ResultCode;
import com.community.common.SecurityUser;
import com.community.dto.KbCategorySaveDTO;
import com.community.dto.KbCategoryStatusDTO;
import com.community.dto.KbEntryPageQueryDTO;
import com.community.dto.KbEntrySaveDTO;
import com.community.dto.KbEntryStatusDTO;
import com.community.entity.KbCategory;
import com.community.entity.KbEntry;
import com.community.entity.KbEntryTag;
import com.community.entity.QaTag;
import com.community.mapper.KbCategoryMapper;
import com.community.mapper.KbEntryMapper;
import com.community.mapper.KbEntryTagMapper;
import com.community.mapper.QaTagMapper;
import com.community.service.KbAdminService;
import com.community.vo.KbCategoryTreeVO;
import com.community.vo.KbEntryDetailVO;
import com.community.vo.KbEntryPageItemVO;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class KbAdminServiceImpl implements KbAdminService {
    private final KbCategoryMapper kbCategoryMapper;
    private final KbEntryMapper kbEntryMapper;
    private final KbEntryTagMapper kbEntryTagMapper;
    private final QaTagMapper qaTagMapper;

    @Override
    public List<KbCategoryTreeVO> categoryTree() {
        List<KbCategory> all = kbCategoryMapper.selectList(new LambdaQueryWrapper<KbCategory>()
            .orderByAsc(KbCategory::getSort)
            .orderByAsc(KbCategory::getId));
        Map<Long, KbCategoryTreeVO> nodeMap = new HashMap<>();
        List<KbCategoryTreeVO> roots = new ArrayList<>();
        for (KbCategory c : all) {
            KbCategoryTreeVO node = toTreeNode(c);
            nodeMap.put(c.getId(), node);
        }
        for (KbCategory c : all) {
            KbCategoryTreeVO node = nodeMap.get(c.getId());
            if (c.getParentId() == null) {
                roots.add(node);
                continue;
            }
            KbCategoryTreeVO parent = nodeMap.get(c.getParentId());
            if (parent == null) {
                roots.add(node);
            } else {
                parent.getChildren().add(node);
            }
        }
        return roots;
    }

    @Override
    @Transactional
    public Long createCategory(KbCategorySaveDTO dto) {
        validateCategoryParent(dto.getParentId(), null);
        ensureCategoryNameUnique(dto.getName(), dto.getParentId(), null);
        KbCategory row = new KbCategory();
        row.setParentId(dto.getParentId());
        row.setName(dto.getName().trim());
        row.setDescription(dto.getDescription());
        row.setIcon(dto.getIcon());
        row.setSort(dto.getSort() == null ? 0 : dto.getSort());
        row.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());
        kbCategoryMapper.insert(row);
        return row.getId();
    }

    @Override
    @Transactional
    public void updateCategory(Long id, KbCategorySaveDTO dto) {
        KbCategory row = getCategoryOrThrow(id);
        validateCategoryParent(dto.getParentId(), id);
        ensureCategoryNameUnique(dto.getName(), dto.getParentId(), id);
        row.setParentId(dto.getParentId());
        row.setName(dto.getName().trim());
        row.setDescription(dto.getDescription());
        row.setIcon(dto.getIcon());
        if (dto.getSort() != null) {
            row.setSort(dto.getSort());
        }
        if (dto.getStatus() != null) {
            row.setStatus(dto.getStatus());
        }
        kbCategoryMapper.updateById(row);
    }

    @Override
    @Transactional
    public void updateCategoryStatus(Long id, KbCategoryStatusDTO dto) {
        KbCategory row = getCategoryOrThrow(id);
        row.setStatus(dto.getStatus());
        kbCategoryMapper.updateById(row);
    }

    @Override
    public PageInfo<KbEntryPageItemVO> entryPage(KbEntryPageQueryDTO query) {
        int page = query == null || query.getPage() == null ? 1 : query.getPage();
        int pageSize = query == null || query.getPageSize() == null ? 10 : query.getPageSize();
        PageHelper.startPage(page, pageSize);
        return new PageInfo<>(kbEntryMapper.selectAdminPage(
            query == null ? null : query.getKeyword(),
            query == null ? null : query.getStatus(),
            query == null ? null : query.getCategoryId(),
            query == null ? null : query.getTagId(),
            query == null ? null : query.getStartTime(),
            query == null ? null : query.getEndTime(),
            query == null ? null : query.getSortBy(),
            query == null ? null : query.getSortOrder()
        ));
    }

    @Override
    public KbEntryDetailVO entryDetail(Long id) {
        KbEntry row = getEntryOrThrow(id);
        KbEntryDetailVO vo = new KbEntryDetailVO();
        vo.setId(row.getId());
        vo.setCategoryId(row.getCategoryId());
        vo.setTitle(row.getTitle());
        vo.setSummary(row.getSummary());
        vo.setContent(row.getContent());
        vo.setSource(row.getSource());
        vo.setStatus(row.getStatus());
        vo.setViewCount(row.getViewCount());
        vo.setLikeCount(row.getLikeCount());
        vo.setFavoriteCount(row.getFavoriteCount());
        vo.setCreatedAt(row.getCreatedAt());
        vo.setUpdatedAt(row.getUpdatedAt());
        vo.setTags(kbEntryTagMapper.selectTagsByEntryId(id));
        return vo;
    }

    @Override
    @Transactional
    public Long createEntry(KbEntrySaveDTO dto) {
        validateCategoryForEntry(dto.getCategoryId());
        validateTagIds(dto.getTagIds());

        KbEntry row = new KbEntry();
        row.setCategoryId(dto.getCategoryId());
        row.setTitle(dto.getTitle().trim());
        row.setSummary(dto.getSummary());
        row.setContent(dto.getContent());
        row.setSource(dto.getSource());
        row.setAuthorUserId(currentUserId());
        row.setStatus(resolveCreateStatusByRole());
        row.setViewCount(0);
        row.setLikeCount(0);
        row.setFavoriteCount(0);
        kbEntryMapper.insert(row);
        replaceEntryTags(row.getId(), dto.getTagIds());
        return row.getId();
    }

    @Override
    @Transactional
    public void updateEntry(Long id, KbEntrySaveDTO dto) {
        KbEntry row = getEntryOrThrow(id);
        validateCategoryForEntry(dto.getCategoryId());
        validateTagIds(dto.getTagIds());

        row.setCategoryId(dto.getCategoryId());
        row.setTitle(dto.getTitle().trim());
        row.setSummary(dto.getSummary());
        row.setContent(dto.getContent());
        row.setSource(dto.getSource());
        kbEntryMapper.updateById(row);
        replaceEntryTags(id, dto.getTagIds());
    }

    @Override
    @Transactional
    public void updateEntryStatus(Long id, KbEntryStatusDTO dto) {
        if (dto.getStatus() != 1 && dto.getStatus() != 4) {
            throw new BizException(ResultCode.BAD_REQUEST, "状态必须为1或4");
        }
        KbEntry row = getEntryOrThrow(id);
        row.setStatus(dto.getStatus());
        kbEntryMapper.updateById(row);
    }

    private KbCategoryTreeVO toTreeNode(KbCategory c) {
        KbCategoryTreeVO node = new KbCategoryTreeVO();
        node.setId(c.getId());
        node.setParentId(c.getParentId());
        node.setName(c.getName());
        node.setDescription(c.getDescription());
        node.setIcon(c.getIcon());
        node.setSort(c.getSort());
        node.setStatus(c.getStatus());
        return node;
    }

    private KbCategory getCategoryOrThrow(Long id) {
        KbCategory row = kbCategoryMapper.selectById(id);
        if (row == null) {
            throw new BizException(ResultCode.BAD_REQUEST, "知识库分类不存在");
        }
        return row;
    }

    private KbEntry getEntryOrThrow(Long id) {
        KbEntry row = kbEntryMapper.selectById(id);
        if (row == null) {
            throw new BizException(ResultCode.BAD_REQUEST, "知识库条目不存在");
        }
        return row;
    }

    private void validateCategoryParent(Long parentId, Long selfId) {
        if (parentId == null) {
            return;
        }
        if (selfId != null && selfId.equals(parentId)) {
            throw new BizException(ResultCode.BAD_REQUEST, "父分类不能是自己");
        }
        getCategoryOrThrow(parentId);
    }

    private void ensureCategoryNameUnique(String name, Long parentId, Long excludeId) {
        String normalized = name == null ? null : name.trim();
        if (!StringUtils.hasText(normalized)) {
            throw new BizException(ResultCode.BAD_REQUEST, "名称不能为空");
        }
        LambdaQueryWrapper<KbCategory> wrapper = new LambdaQueryWrapper<KbCategory>()
            .eq(KbCategory::getName, normalized);
        if (parentId == null) {
            wrapper.isNull(KbCategory::getParentId);
        } else {
            wrapper.eq(KbCategory::getParentId, parentId);
        }
        if (excludeId != null) {
            wrapper.ne(KbCategory::getId, excludeId);
        }
        KbCategory exists = kbCategoryMapper.selectOne(wrapper.last("LIMIT 1"));
        if (exists != null) {
            throw new BizException(ResultCode.BAD_REQUEST, "同级下分类名称已存在");
        }
    }

    private void validateCategoryForEntry(Long categoryId) {
        KbCategory category = getCategoryOrThrow(categoryId);
        if (category.getStatus() == null || category.getStatus() == 0) {
            throw new BizException(ResultCode.BAD_REQUEST, "分类已禁用");
        }
    }

    private void validateTagIds(List<Long> tagIds) {
        if (CollectionUtils.isEmpty(tagIds)) {
            return;
        }
        List<Long> ids = tagIds.stream().filter(Objects::nonNull).filter(id -> id > 0).distinct().toList();
        if (ids.size() != tagIds.stream().filter(Objects::nonNull).filter(id -> id > 0).count()) {
            throw new BizException(ResultCode.BAD_REQUEST, "标签编号列表包含无效编号");
        }
        long count = qaTagMapper.selectCount(new LambdaQueryWrapper<QaTag>().in(QaTag::getId, ids));
        if (count != ids.size()) {
            throw new BizException(ResultCode.BAD_REQUEST, "标签编号列表包含不存在的编号");
        }
    }

    private void replaceEntryTags(Long entryId, List<Long> tagIds) {
        kbEntryTagMapper.delete(new LambdaQueryWrapper<KbEntryTag>().eq(KbEntryTag::getEntryId, entryId));
        if (CollectionUtils.isEmpty(tagIds)) {
            return;
        }
        List<Long> ids = tagIds.stream().filter(Objects::nonNull).filter(id -> id > 0).distinct().toList();
        for (Long tagId : ids) {
            KbEntryTag row = new KbEntryTag();
            row.setEntryId(entryId);
            row.setTagId(tagId);
            kbEntryTagMapper.insert(row);
        }
    }

    private Long currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof SecurityUser su)) {
            return null;
        }
        return su.getId();
    }

    private int resolveCreateStatusByRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof SecurityUser su)) {
            return 2;
        }
        List<String> roles = su.getRoleCodes();
        if (roles.contains("admin") || roles.contains("staff")) {
            return 1;
        }
        if (roles.contains("expert")) {
            return 2;
        }
        return 2;
    }
}
