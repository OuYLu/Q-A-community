package com.community.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.entity.KbEntryTag;
import com.community.vo.KbTagSimpleVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface KbEntryTagMapper extends BaseMapper<KbEntryTag> {
    List<KbTagSimpleVO> selectTagsByEntryId(@Param("entryId") Long entryId);
}
