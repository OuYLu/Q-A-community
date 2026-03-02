package com.community.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.entity.KbEntry;
import com.community.vo.AppSearchKbVO;
import com.community.vo.KbEntryPageItemVO;
import com.community.vo.SearchKbDoc;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface KbEntryMapper extends BaseMapper<KbEntry> {
    List<KbEntryPageItemVO> selectAdminPage(@Param("keyword") String keyword,
                                            @Param("status") Integer status,
                                            @Param("categoryId") Long categoryId,
                                            @Param("tagId") Long tagId,
                                            @Param("startTime") LocalDateTime startTime,
                                            @Param("endTime") LocalDateTime endTime,
                                            @Param("sortBy") String sortBy,
                                            @Param("sortOrder") String sortOrder);

    List<AppSearchKbVO> selectAppSearchKb(@Param("query") String query,
                                          @Param("limit") Integer limit,
                                          @Param("offset") Integer offset);

    List<AppSearchKbVO> selectAppSearchKbByIds(@Param("ids") List<Long> ids);

    List<SearchKbDoc> selectSearchKbDocs();
}