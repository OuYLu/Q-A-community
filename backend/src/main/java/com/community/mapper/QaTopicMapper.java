package com.community.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.entity.QaTopic;
import com.community.vo.AdminTopicListItemVO;
import com.community.vo.TopicQuestionPageItemVO;
import com.community.vo.TopicRecentQuestionVO;
import com.community.vo.TopicTrendPointVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface QaTopicMapper extends BaseMapper<QaTopic> {
    @Select("""
        <script>
        SELECT
            t.id,
            t.title,
            t.subtitle,
            t.cover_img AS coverImg,
            t.status,
            t.follow_count AS followCount,
            t.question_count AS questionCount,
            t.today_new_count AS todayNewCount,
            COALESCE(tc.category_count, 0) AS categoryCount,
            t.created_by AS createdBy,
            t.created_at AS createdAt,
            t.updated_at AS updatedAt
        FROM qa_topic t
        LEFT JOIN (
            SELECT topic_id, COUNT(1) AS category_count
            FROM qa_topic_category
            GROUP BY topic_id
        ) tc ON tc.topic_id = t.id
        WHERE 1 = 1
          <if test="title != null and title != ''">
            AND t.title LIKE CONCAT('%', #{title}, '%')
          </if>
          <if test="status != null">
            AND t.status = #{status}
          </if>
          <if test="createdBy != null">
            AND t.created_by = #{createdBy}
          </if>
          <if test="dateStart != null">
            AND DATE(t.created_at) &gt;= #{dateStart}
          </if>
          <if test="dateEnd != null">
            AND DATE(t.created_at) &lt;= #{dateEnd}
          </if>
        ORDER BY
          <choose>
            <when test="sortBy == 'followCount'">t.follow_count</when>
            <when test="sortBy == 'questionCount'">t.question_count</when>
            <when test="sortBy == 'todayNewCount'">t.today_new_count</when>
            <otherwise>t.created_at</otherwise>
          </choose>
          <choose>
            <when test="sortOrder == 'asc'">ASC</when>
            <otherwise>DESC</otherwise>
          </choose>,
          t.id DESC
        </script>
        """)
    List<AdminTopicListItemVO> selectAdminTopicPage(@Param("title") String title,
                                                    @Param("status") Integer status,
                                                    @Param("createdBy") Long createdBy,
                                                    @Param("dateStart") LocalDate dateStart,
                                                    @Param("dateEnd") LocalDate dateEnd,
                                                    @Param("sortBy") String sortBy,
                                                    @Param("sortOrder") String sortOrder);

    @Select("""
        SELECT
            q.id,
            q.title,
            q.status,
            q.created_at AS createdAt,
            q.user_id AS userId,
            u.nickname AS authorName
        FROM qa_question q
        LEFT JOIN user u ON u.id = q.user_id
        WHERE q.topic_id = #{topicId}
          AND q.delete_flag = 0
        ORDER BY q.created_at DESC, q.id DESC
        LIMIT #{limit}
        """)
    List<TopicRecentQuestionVO> selectRecentQuestionsByTopicId(@Param("topicId") Long topicId,
                                                                @Param("limit") Integer limit);

    @Select("""
        <script>
        SELECT
            q.id,
            q.title,
            q.status,
            q.created_at AS createdAt,
            q.user_id AS userId
        FROM qa_question q
        WHERE q.topic_id = #{topicId}
          AND q.delete_flag = 0
          <if test="status != null">
            AND q.status = #{status}
          </if>
          <if test="title != null and title != ''">
            AND q.title LIKE CONCAT('%', #{title}, '%')
          </if>
        ORDER BY
          <choose>
            <when test="sortBy == 'createdAt'">q.created_at</when>
            <when test="sortBy == 'viewCount'">q.view_count</when>
            <when test="sortBy == 'answerCount'">q.answer_count</when>
            <otherwise>q.created_at</otherwise>
          </choose>
          <choose>
            <when test="sortOrder == 'asc'">ASC</when>
            <otherwise>DESC</otherwise>
          </choose>,
          q.id DESC
        </script>
        """)
    List<TopicQuestionPageItemVO> selectTopicQuestionPage(@Param("topicId") Long topicId,
                                                           @Param("status") Integer status,
                                                           @Param("title") String title,
                                                           @Param("sortBy") String sortBy,
                                                           @Param("sortOrder") String sortOrder);

    @Select("""
        WITH RECURSIVE date_series AS (
            SELECT DATE_SUB(CURDATE(), INTERVAL #{daysMinusOne} DAY) AS d
            UNION ALL
            SELECT DATE_ADD(d, INTERVAL 1 DAY)
            FROM date_series
            WHERE d < CURDATE()
        ),
        stat AS (
            SELECT DATE(q.created_at) AS d, COUNT(1) AS cnt
            FROM qa_question q
            WHERE q.topic_id = #{topicId}
              AND q.delete_flag = 0
              AND q.created_at >= DATE_SUB(CURDATE(), INTERVAL #{daysMinusOne} DAY)
              AND q.created_at < DATE_ADD(CURDATE(), INTERVAL 1 DAY)
            GROUP BY DATE(q.created_at)
        )
        SELECT
            DATE_FORMAT(ds.d, '%Y-%m-%d') AS date,
            COALESCE(s.cnt, 0) AS cnt
        FROM date_series ds
        LEFT JOIN stat s ON s.d = ds.d
        ORDER BY ds.d ASC
        """)
    List<TopicTrendPointVO> selectTopicTrend(@Param("topicId") Long topicId,
                                             @Param("daysMinusOne") Integer daysMinusOne);
}
