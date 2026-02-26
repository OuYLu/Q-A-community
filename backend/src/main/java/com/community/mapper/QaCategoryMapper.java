package com.community.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.entity.QaCategory;
import com.community.vo.AppCategoryVO;
import com.community.vo.CategoryListVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface QaCategoryMapper extends BaseMapper<QaCategory> {
    List<CategoryListVO> selectListWithQuestionCount(@Param("name") String name,
                                                     @Param("parentId") Long parentId,
                                                     @Param("status") Integer status);

    long countQuestionRefs(@Param("categoryId") Long categoryId);

    long countAnswerRefs(@Param("categoryId") Long categoryId);

    long countTopicRefs(@Param("categoryId") Long categoryId);

    List<AppCategoryVO> selectAppCategoryList();
}
