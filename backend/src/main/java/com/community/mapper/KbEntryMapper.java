package com.community.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.entity.KbEntry;
import com.community.vo.KbEntryPageItemVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface KbEntryMapper extends BaseMapper<KbEntry> {
    @Select("""
        <script>
        SELECT
            e.id,
            e.title,
            e.category_id AS categoryId,
            c.name AS categoryName,
            e.status,
            e.created_at AS createdAt,
            e.updated_at AS updatedAt,
            e.view_count AS viewCount,
            e.like_count AS likeCount,
            e.favorite_count AS favoriteCount
        FROM kb_entry e
        LEFT JOIN kb_category c ON c.id = e.category_id
        WHERE 1 = 1
          <if test="keyword != null and keyword != ''">
            AND e.title LIKE CONCAT('%', #{keyword}, '%')
          </if>
          <if test="status != null">
            AND e.status = #{status}
          </if>
          <if test="categoryId != null">
            AND e.category_id = #{categoryId}
          </if>
          <if test="tagId != null">
            AND EXISTS (
              SELECT 1
              FROM kb_entry_tag ket
              WHERE ket.entry_id = e.id
                AND ket.tag_id = #{tagId}
            )
          </if>
          <if test="startTime != null">
            AND e.created_at &gt;= #{startTime}
          </if>
          <if test="endTime != null">
            AND e.created_at &lt;= #{endTime}
          </if>
        ORDER BY
          <choose>
            <when test="sortBy == 'viewCount'">e.view_count</when>
            <when test="sortBy == 'likeCount'">e.like_count</when>
            <when test="sortBy == 'favoriteCount'">e.favorite_count</when>
            <otherwise>e.created_at</otherwise>
          </choose>
          <choose>
            <when test="sortOrder == 'asc'">ASC</when>
            <otherwise>DESC</otherwise>
          </choose>,
          e.id DESC
        </script>
        """)
    List<KbEntryPageItemVO> selectAdminPage(@Param("keyword") String keyword,
                                            @Param("status") Integer status,
                                            @Param("categoryId") Long categoryId,
                                            @Param("tagId") Long tagId,
                                            @Param("startTime") LocalDateTime startTime,
                                            @Param("endTime") LocalDateTime endTime,
                                            @Param("sortBy") String sortBy,
                                            @Param("sortOrder") String sortOrder);
}
