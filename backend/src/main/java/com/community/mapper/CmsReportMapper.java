package com.community.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.entity.CmsReport;
import com.community.vo.CmsReportDetailRowVO;
import com.community.vo.CmsReportPageItemVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface CmsReportMapper extends BaseMapper<CmsReport> {
    @Select("""
        <script>
        SELECT
            r.id,
            r.biz_type AS bizType,
            r.biz_id AS bizId,
            r.reason_type AS reasonType,
            r.reason_code AS reasonCode,
            r.reason_detail AS reasonDetail,
            r.reporter_id AS reporterId,
            r.created_at AS createdAt,
            r.status,
            r.handler_id AS handlerId,
            r.handled_at AS handledAt,
            r.handle_action AS handleAction,
            r.handle_result AS handleResult,
            COALESCE(q.title, kb.title, CONCAT('Answer#', ans.id), CONCAT('Comment#', c.id)) AS contentTitle,
            LEFT(COALESCE(q.content, ans.content, c.content, kb.summary, kb.content), 200) AS contentSnippet,
            COALESCE(q.status, ans.status, c.status, kb.status) AS contentStatus,
            u.id AS authorId,
            u.nickname AS authorName
        FROM cms_report r
        LEFT JOIN qa_question q ON r.biz_type = 1 AND q.id = r.biz_id
        LEFT JOIN qa_answer ans ON r.biz_type = 2 AND ans.id = r.biz_id
        LEFT JOIN qa_comment c ON r.biz_type = 3 AND c.id = r.biz_id
        LEFT JOIN kb_entry kb ON r.biz_type = 4 AND kb.id = r.biz_id
        LEFT JOIN `user` u ON u.id = COALESCE(q.user_id, ans.user_id, c.user_id, kb.author_user_id)
        WHERE 1 = 1
          <if test="bizType != null">
            AND r.biz_type = #{bizType}
          </if>
          <if test="status != null">
            AND r.status = #{status}
          </if>
          <if test="reasonType != null">
            AND r.reason_type = #{reasonType}
          </if>
          <if test="keyword != null and keyword != ''">
            AND (
              COALESCE(q.title, kb.title, '') LIKE CONCAT('%', #{keyword}, '%')
              OR r.reason_detail LIKE CONCAT('%', #{keyword}, '%')
              OR r.reason_code LIKE CONCAT('%', #{keyword}, '%')
            )
          </if>
          <if test="startTime != null">
            AND r.created_at &gt;= #{startTime}
          </if>
          <if test="endTime != null">
            AND r.created_at &lt;= #{endTime}
          </if>
        ORDER BY r.created_at DESC, r.id DESC
        </script>
        """)
    List<CmsReportPageItemVO> selectAdminReportPage(@Param("bizType") Integer bizType,
                                                    @Param("status") Integer status,
                                                    @Param("reasonType") Integer reasonType,
                                                    @Param("keyword") String keyword,
                                                    @Param("startTime") LocalDateTime startTime,
                                                    @Param("endTime") LocalDateTime endTime);

    @Select("""
        SELECT
            r.id,
            r.biz_type AS bizType,
            r.biz_id AS bizId,
            r.reason_type AS reasonType,
            r.reporter_id AS reporterId,
            r.reason_code AS reasonCode,
            r.reason_detail AS reasonDetail,
            r.status,
            r.handler_id AS handlerId,
            r.handle_action AS handleAction,
            r.handle_result AS handleResult,
            r.handled_at AS handledAt,
            r.created_at AS createdAt,
            r.updated_at AS updatedAt,
            COALESCE(q.title, kb.title, CONCAT('Answer#', ans.id), CONCAT('Comment#', c.id)) AS contentTitle,
            COALESCE(q.content, ans.content, c.content, kb.content) AS contentText,
            COALESCE(q.status, ans.status, c.status, kb.status) AS contentStatus,
            COALESCE(q.reject_reason, ans.reject_reason, c.reject_reason) AS contentRejectReason,
            COALESCE(q.created_at, ans.created_at, c.created_at, kb.created_at) AS contentCreatedAt,
            u.id AS authorId,
            u.username AS authorUsername,
            u.nickname AS authorNickname,
            u.status AS authorStatus
        FROM cms_report r
        LEFT JOIN qa_question q ON r.biz_type = 1 AND q.id = r.biz_id
        LEFT JOIN qa_answer ans ON r.biz_type = 2 AND ans.id = r.biz_id
        LEFT JOIN qa_comment c ON r.biz_type = 3 AND c.id = r.biz_id
        LEFT JOIN kb_entry kb ON r.biz_type = 4 AND kb.id = r.biz_id
        LEFT JOIN `user` u ON u.id = COALESCE(q.user_id, ans.user_id, c.user_id, kb.author_user_id)
        WHERE r.id = #{id}
        LIMIT 1
        """)
    CmsReportDetailRowVO selectAdminReportDetail(@Param("id") Long id);
}
