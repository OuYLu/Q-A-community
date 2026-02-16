package com.community.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.entity.QaTag;
import com.community.vo.TagRecentQuestionVO;
import com.community.vo.TagUsageTrendPointVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface QaTagMapper extends BaseMapper<QaTag> {
    @Select("""
        SELECT
            q.id,
            q.title,
            q.status,
            q.created_at AS createdAt
        FROM qa_question_tag qt
        JOIN qa_question q ON q.id = qt.question_id
        WHERE qt.tag_id = #{tagId}
          AND q.delete_flag = 0
        ORDER BY q.created_at DESC, q.id DESC
        LIMIT 10
        """)
    List<TagRecentQuestionVO> selectRecentQuestions(@Param("tagId") Long tagId);

    @Select("""
        WITH RECURSIVE date_series AS (
            SELECT DATE_SUB(CURDATE(), INTERVAL #{daysMinusOne} DAY) AS d
            UNION ALL
            SELECT DATE_ADD(d, INTERVAL 1 DAY)
            FROM date_series
            WHERE d < CURDATE()
        ),
        ref_stat AS (
            SELECT DATE(qt.created_at) AS d, COUNT(1) AS ref_count
            FROM qa_question_tag qt
            JOIN qa_question q ON q.id = qt.question_id
            WHERE qt.tag_id = #{tagId}
              AND q.delete_flag = 0
              AND qt.created_at >= DATE_SUB(CURDATE(), INTERVAL #{daysMinusOne} DAY)
              AND qt.created_at < DATE_ADD(CURDATE(), INTERVAL 1 DAY)
            GROUP BY DATE(qt.created_at)
        )
        SELECT
            DATE_FORMAT(ds.d, '%Y-%m-%d') AS statDate,
            COALESCE(rs.ref_count, 0) AS refCount
        FROM date_series ds
        LEFT JOIN ref_stat rs ON rs.d = ds.d
        ORDER BY ds.d ASC
        """)
    List<TagUsageTrendPointVO> selectUsageTrend(@Param("tagId") Long tagId,
                                                @Param("daysMinusOne") Integer daysMinusOne);
}
