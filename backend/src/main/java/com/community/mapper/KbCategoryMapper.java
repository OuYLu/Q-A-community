package com.community.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.entity.KbCategory;
import com.community.vo.AppKbCategoryVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface KbCategoryMapper extends BaseMapper<KbCategory> {
    List<AppKbCategoryVO> selectAppRootCategoryList(@Param("limit") Integer limit);

    List<AppKbCategoryVO> selectAppPreferredRootCategories(@Param("userId") Long userId,
                                                           @Param("limit") Integer limit);
}
