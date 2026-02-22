package com.community.mapper;

import com.community.vo.DashboardContentTrendVO;
import com.community.vo.DashboardGovernanceTrendVO;
import com.community.vo.DashboardHotTagVO;
import com.community.vo.DashboardHotTopicVO;
import com.community.vo.DashboardNewTagVO;
import com.community.vo.DashboardNewTagTrendPointVO;
import com.community.vo.DashboardUserActivityTrendVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DashboardStatMapper {
    @Select("""
        SELECT COUNT(1)
        FROM qa_question
        WHERE delete_flag = 0
          AND created_at >= CURDATE()
          AND created_at < DATE_ADD(CURDATE(), INTERVAL 1 DAY)
        """)
    long countTodayQuestion();

    @Select("""
        SELECT COUNT(1)
        FROM qa_answer
        WHERE delete_flag = 0
          AND created_at >= CURDATE()
          AND created_at < DATE_ADD(CURDATE(), INTERVAL 1 DAY)
        """)
    long countTodayAnswer();

    @Select("""
        SELECT COUNT(1)
        FROM qa_comment
        WHERE delete_flag = 0
          AND created_at >= CURDATE()
          AND created_at < DATE_ADD(CURDATE(), INTERVAL 1 DAY)
        """)
    long countTodayComment();

    @Select("""
        SELECT COUNT(1)
        FROM cms_report
        WHERE created_at >= CURDATE()
          AND created_at < DATE_ADD(CURDATE(), INTERVAL 1 DAY)
        """)
    long countTodayReport();

    @Select("""
        SELECT COUNT(1)
        FROM cms_audit
        WHERE audit_status = 1
        """)
    long countPendingAudit();

    @Select("""
        SELECT COUNT(1)
        FROM cms_report
        WHERE status = 1
        """)
    long countPendingReport();

    @Select("""
        WITH RECURSIVE date_series AS (
            SELECT DATE_SUB(CURDATE(), INTERVAL #{daysMinusOne} DAY) AS d
            UNION ALL
            SELECT DATE_ADD(d, INTERVAL 1 DAY)
            FROM date_series
            WHERE d < CURDATE()
        ),
        q AS (
            SELECT DATE(created_at) d, COUNT(1) cnt
            FROM qa_question
            WHERE delete_flag = 0
              AND created_at >= DATE_SUB(CURDATE(), INTERVAL #{daysMinusOne} DAY)
              AND created_at < DATE_ADD(CURDATE(), INTERVAL 1 DAY)
            GROUP BY DATE(created_at)
        ),
        a AS (
            SELECT DATE(created_at) d, COUNT(1) cnt
            FROM qa_answer
            WHERE delete_flag = 0
              AND created_at >= DATE_SUB(CURDATE(), INTERVAL #{daysMinusOne} DAY)
              AND created_at < DATE_ADD(CURDATE(), INTERVAL 1 DAY)
            GROUP BY DATE(created_at)
        ),
        c AS (
            SELECT DATE(created_at) d, COUNT(1) cnt
            FROM qa_comment
            WHERE delete_flag = 0
              AND created_at >= DATE_SUB(CURDATE(), INTERVAL #{daysMinusOne} DAY)
              AND created_at < DATE_ADD(CURDATE(), INTERVAL 1 DAY)
            GROUP BY DATE(created_at)
        )
        SELECT
            DATE_FORMAT(ds.d, '%Y-%m-%d') AS date,
            COALESCE(q.cnt, 0) AS questionCount,
            COALESCE(a.cnt, 0) AS answerCount,
            COALESCE(c.cnt, 0) AS commentCount
        FROM date_series ds
        LEFT JOIN q ON q.d = ds.d
        LEFT JOIN a ON a.d = ds.d
        LEFT JOIN c ON c.d = ds.d
        ORDER BY ds.d ASC
        """)
    List<DashboardContentTrendVO> selectContentTrend(@Param("daysMinusOne") Integer daysMinusOne);

    @Select("""
        WITH RECURSIVE date_series AS (
            SELECT DATE_SUB(CURDATE(), INTERVAL #{daysMinusOne} DAY) AS d
            UNION ALL
            SELECT DATE_ADD(d, INTERVAL 1 DAY)
            FROM date_series
            WHERE d < CURDATE()
        ),
        r AS (
            SELECT DATE(created_at) d, COUNT(1) cnt
            FROM cms_report
            WHERE created_at >= DATE_SUB(CURDATE(), INTERVAL #{daysMinusOne} DAY)
              AND created_at < DATE_ADD(CURDATE(), INTERVAL 1 DAY)
            GROUP BY DATE(created_at)
        ),
        a AS (
            SELECT DATE(created_at) d, COUNT(1) cnt
            FROM cms_audit
            WHERE created_at >= DATE_SUB(CURDATE(), INTERVAL #{daysMinusOne} DAY)
              AND created_at < DATE_ADD(CURDATE(), INTERVAL 1 DAY)
            GROUP BY DATE(created_at)
        )
        SELECT
            DATE_FORMAT(ds.d, '%Y-%m-%d') AS date,
            COALESCE(r.cnt, 0) AS reportCount,
            COALESCE(a.cnt, 0) AS auditCount
        FROM date_series ds
        LEFT JOIN r ON r.d = ds.d
        LEFT JOIN a ON a.d = ds.d
        ORDER BY ds.d ASC
        """)
    List<DashboardGovernanceTrendVO> selectGovernanceTrend(@Param("daysMinusOne") Integer daysMinusOne);

    @Select("""
        WITH RECURSIVE date_series AS (
            SELECT DATE_SUB(CURDATE(), INTERVAL #{daysMinusOne} DAY) AS d
            UNION ALL
            SELECT DATE_ADD(d, INTERVAL 1 DAY)
            FROM date_series
            WHERE d < CURDATE()
        ),
        nu AS (
            SELECT DATE(created_at) d, COUNT(1) cnt
            FROM user
            WHERE created_at >= DATE_SUB(CURDATE(), INTERVAL #{daysMinusOne} DAY)
              AND created_at < DATE_ADD(CURDATE(), INTERVAL 1 DAY)
            GROUP BY DATE(created_at)
        ),
        au AS (
            SELECT DATE(created_at) d, COUNT(DISTINCT user_id) cnt
            FROM rec_user_behavior
            WHERE created_at >= DATE_SUB(CURDATE(), INTERVAL #{daysMinusOne} DAY)
              AND created_at < DATE_ADD(CURDATE(), INTERVAL 1 DAY)
            GROUP BY DATE(created_at)
        ),
        sq AS (
            SELECT DATE(created_at) d, COUNT(1) cnt
            FROM search_query_log
            WHERE created_at >= DATE_SUB(CURDATE(), INTERVAL #{daysMinusOne} DAY)
              AND created_at < DATE_ADD(CURDATE(), INTERVAL 1 DAY)
            GROUP BY DATE(created_at)
        )
        SELECT
            DATE_FORMAT(ds.d, '%Y-%m-%d') AS date,
            COALESCE(nu.cnt, 0) AS newUserCount,
            COALESCE(au.cnt, 0) AS activeUserCount,
            COALESCE(sq.cnt, 0) AS searchCount
        FROM date_series ds
        LEFT JOIN nu ON nu.d = ds.d
        LEFT JOIN au ON au.d = ds.d
        LEFT JOIN sq ON sq.d = ds.d
        ORDER BY ds.d ASC
        """)
    List<DashboardUserActivityTrendVO> selectUserActivityTrend(@Param("daysMinusOne") Integer daysMinusOne);

    @Select("""
        SELECT
            id,
            name,
            source,
            status,
            use_count AS useCount,
            created_at AS createdAt
        FROM qa_tag
        ORDER BY use_count DESC, id DESC
        LIMIT #{limit}
        """)
    List<DashboardHotTagVO> selectHotTags(@Param("limit") Integer limit);

    @Select("""
        SELECT
            id,
            title,
            status,
            follow_count AS followCount,
            question_count AS questionCount,
            today_new_count AS todayNewCount,
            created_at AS createdAt
        FROM qa_topic
        ORDER BY follow_count DESC, question_count DESC, id DESC
        LIMIT #{limit}
        """)
    List<DashboardHotTopicVO> selectHotTopics(@Param("limit") Integer limit);

    @Select("""
        SELECT COUNT(1)
        FROM qa_tag
        WHERE source = 2
          AND created_at >= DATE_SUB(CURDATE(), INTERVAL #{daysMinusOne} DAY)
          AND created_at < DATE_ADD(CURDATE(), INTERVAL 1 DAY)
        """)
    long countNewUserTags(@Param("daysMinusOne") Integer daysMinusOne);

    @Select("""
        SELECT
            id,
            name,
            source,
            status,
            created_at AS createdAt
        FROM qa_tag
        WHERE source = 2
          AND created_at >= DATE_SUB(CURDATE(), INTERVAL #{daysMinusOne} DAY)
          AND created_at < DATE_ADD(CURDATE(), INTERVAL 1 DAY)
        ORDER BY created_at DESC, id DESC
        LIMIT #{limit}
        """)
    List<DashboardNewTagVO> selectNewUserTags(@Param("daysMinusOne") Integer daysMinusOne,
                                              @Param("limit") Integer limit);

    @Select("""
        WITH RECURSIVE date_series AS (
            SELECT DATE_SUB(CURDATE(), INTERVAL #{daysMinusOne} DAY) AS d
            UNION ALL
            SELECT DATE_ADD(d, INTERVAL 1 DAY)
            FROM date_series
            WHERE d < CURDATE()
        ),
        t AS (
            SELECT DATE(created_at) d, COUNT(1) cnt
            FROM qa_tag
            WHERE source = 2
              AND created_at >= DATE_SUB(CURDATE(), INTERVAL #{daysMinusOne} DAY)
              AND created_at < DATE_ADD(CURDATE(), INTERVAL 1 DAY)
            GROUP BY DATE(created_at)
        )
        SELECT
            DATE_FORMAT(ds.d, '%Y-%m-%d') AS date,
            COALESCE(t.cnt, 0) AS count
        FROM date_series ds
        LEFT JOIN t ON t.d = ds.d
        ORDER BY ds.d ASC
        """)
    List<DashboardNewTagTrendPointVO> selectNewUserTagTrend(@Param("daysMinusOne") Integer daysMinusOne);
}
