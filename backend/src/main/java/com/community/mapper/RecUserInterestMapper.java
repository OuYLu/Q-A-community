package com.community.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.entity.RecUserInterest;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Mapper;

import java.math.BigDecimal;

@Mapper
public interface RecUserInterestMapper extends BaseMapper<RecUserInterest> {
    int upsertInterestDelta(@Param("userId") Long userId,
                            @Param("categoryId") Long categoryId,
                            @Param("delta") BigDecimal delta);
}
