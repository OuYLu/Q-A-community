package com.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.community.common.BizException;
import com.community.common.ResultCode;
import com.community.dto.QaCategoryQueryDTO;
import com.community.dto.QaCategorySaveDTO;
import com.community.entity.QaCategory;
import com.community.mapper.QaCategoryMapper;
import com.community.service.QaCategoryAdminService;
import com.community.vo.CategoryListVO;
import com.community.vo.CategoryTreeVO;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QaCategoryAdminServiceImpl extends ServiceImpl<QaCategoryMapper, QaCategory> implements QaCategoryAdminService {

    @Override
    public PageInfo<CategoryListVO> list(QaCategoryQueryDTO query) {
        String name = query == null ? null : query.getName();
        Long parentId = query == null ? null : query.getParentId();
        Integer status = query == null ? null : query.getStatus();
        Integer pageNum = query == null || query.getPageNum() == null ? 1 : query.getPageNum();
        Integer pageSize = query == null || query.getPageSize() == null ? 10 : query.getPageSize();

        PageHelper.startPage(pageNum, pageSize);
        return new PageInfo<>(baseMapper.selectListWithQuestionCount(
            StringUtils.hasText(name) ? name.trim() : null,
            parentId,
            status
        ));
    }

    @Override
    public List<CategoryTreeVO> listTreeLazy(Long parentId) {
        LambdaQueryWrapper<QaCategory> wrapper = new LambdaQueryWrapper<QaCategory>()
            .eq(QaCategory::getDeleteFlag, 0);
        if (parentId == null) {
            wrapper.isNull(QaCategory::getParentId);
        } else {
            wrapper.eq(QaCategory::getParentId, parentId);
        }
        wrapper.orderByAsc(QaCategory::getSort).orderByDesc(QaCategory::getCreatedAt);

        List<QaCategory> categories = this.list(wrapper);
        List<CategoryTreeVO> result = new ArrayList<>(categories.size());
        for (QaCategory category : categories) {
            long childCount = this.count(new LambdaQueryWrapper<QaCategory>()
                .eq(QaCategory::getParentId, category.getId())
                .eq(QaCategory::getDeleteFlag, 0));

            CategoryTreeVO node = new CategoryTreeVO();
            node.setId(category.getId());
            node.setParentId(category.getParentId());
            node.setName(category.getName());
            node.setLabel(category.getName());
            node.setStatus(category.getStatus());
            node.setSort(category.getSort());
            node.setHasChildren(childCount > 0);
            node.setLeaf(childCount == 0);
            result.add(node);
        }
        return result;
    }

    @Override
    @Transactional
    public QaCategory create(QaCategorySaveDTO dto) {
        validateParent(dto.getParentId(), null);
        ensureNameUnique(dto.getName(), dto.getParentId(), null);

        QaCategory category = new QaCategory();
        category.setName(dto.getName().trim());
        category.setParentId(dto.getParentId());
        category.setIcon(dto.getIcon());
        category.setDescription(dto.getDescription());
        category.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());
        category.setSort(dto.getSort() == null ? 0 : dto.getSort());
        category.setDeleteFlag(0);
        this.save(category);
        return category;
    }

    @Override
    @Transactional
    public QaCategory update(Long id, QaCategorySaveDTO dto) {
        QaCategory category = getById(id);

        validateParent(dto.getParentId(), id);
        ensureNameUnique(dto.getName(), dto.getParentId(), id);

        category.setName(dto.getName().trim());
        category.setParentId(dto.getParentId());
        category.setIcon(dto.getIcon());
        category.setDescription(dto.getDescription());
        if (dto.getStatus() != null) {
            category.setStatus(dto.getStatus());
        }
        if (dto.getSort() != null) {
            category.setSort(dto.getSort());
        }
        this.updateById(category);
        return category;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        QaCategory category = getById(id);

        long childCount = this.count(new LambdaQueryWrapper<QaCategory>()
            .eq(QaCategory::getParentId, id)
            .eq(QaCategory::getDeleteFlag, 0));
        if (childCount > 0) {
            throw new BizException(ResultCode.BAD_REQUEST, "分类存在子分类，不能删除");
        }

        long questionRefs = baseMapper.countQuestionRefs(id);
        long answerRefs = baseMapper.countAnswerRefs(id);
        long topicRefs = baseMapper.countTopicRefs(id);
        if (questionRefs > 0 || answerRefs > 0 || topicRefs > 0) {
            throw new BizException(
                ResultCode.BAD_REQUEST,
                "category is referenced and cannot be deleted (questions: " + questionRefs
                    + ", answers: " + answerRefs + ", topics: " + topicRefs + ")"
            );
        }

        category.setDeleteFlag(1);
        this.updateById(category);
    }

    @Override
    public QaCategory getById(Long id) {
        QaCategory category = super.getById(id);
        if (category == null || Integer.valueOf(1).equals(category.getDeleteFlag())) {
            throw new BizException(ResultCode.BAD_REQUEST, "分类不存在");
        }
        return category;
    }

    private void validateParent(Long parentId, Long selfId) {
        if (parentId == null) {
            return;
        }
        if (selfId != null && selfId.equals(parentId)) {
            throw new BizException(ResultCode.BAD_REQUEST, "父分类不能是自己");
        }
        QaCategory parent = super.getById(parentId);
        if (parent == null || Integer.valueOf(1).equals(parent.getDeleteFlag())) {
            throw new BizException(ResultCode.BAD_REQUEST, "父分类不存在");
        }
    }

    private void ensureNameUnique(String rawName, Long parentId, Long excludeId) {
        String name = rawName == null ? null : rawName.trim();
        LambdaQueryWrapper<QaCategory> wrapper = new LambdaQueryWrapper<QaCategory>()
            .eq(QaCategory::getName, name)
            .eq(QaCategory::getDeleteFlag, 0);
        if (parentId == null) {
            wrapper.isNull(QaCategory::getParentId);
        } else {
            wrapper.eq(QaCategory::getParentId, parentId);
        }
        if (excludeId != null) {
            wrapper.ne(QaCategory::getId, excludeId);
        }
        QaCategory exist = this.getOne(wrapper.last("LIMIT 1"));
        if (exist != null) {
            throw new BizException(ResultCode.BAD_REQUEST, "同级下分类名称已存在");
        }
    }
}
