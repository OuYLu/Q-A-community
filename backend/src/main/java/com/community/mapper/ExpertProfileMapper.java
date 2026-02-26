package com.community.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.entity.ExpertProfile;
import com.community.vo.AppExpertCardVO;
import com.community.vo.ExpertManageVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ExpertProfileMapper extends BaseMapper<ExpertProfile> {
    List<ExpertManageVO> selectExpertManageList(@Param("realName") String realName,
                                                @Param("organization") String organization,
                                                @Param("expertise") String expertise,
                                                @Param("expertStatus") Integer expertStatus);

    ExpertManageVO selectExpertManageByUserId(@Param("userId") Long userId);

    List<AppExpertCardVO> selectAppExpertCards(@Param("limit") Integer limit);
}
