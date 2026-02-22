package com.community.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.entity.KbEntryTag;
import com.community.vo.KbTagSimpleVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface KbEntryTagMapper extends BaseMapper<KbEntryTag> {
    @Select("""
        SELECT t.id, t.name
        FROM kb_entry_tag ket
        JOIN qa_tag t ON t.id = ket.tag_id
        WHERE ket.entry_id = #{entryId}
        ORDER BY t.id ASC
        """)
    List<KbTagSimpleVO> selectTagsByEntryId(@Param("entryId") Long entryId);
}
