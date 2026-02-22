package com.community.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.entity.CmsAudit;
import com.community.vo.CmsAuditDetailRowVO;
import com.community.vo.CmsAuditPageItemVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface CmsAuditMapper extends BaseMapper<CmsAudit> {
    @Select("""
        <script>
        SELECT
            a.id,
            a.biz_type AS bizType,
            a.biz_id AS bizId,
            a.trigger_source AS triggerSource,
            a.audit_type AS auditType,
            a.audit_status AS auditStatus,
            a.created_at AS createdAt,
            a.model_label AS modelLabel,
            a.model_score AS modelScore,
            a.hit_detail AS hitDetail,
            a.submit_user_id AS submitUserId,
            su.nickname AS submitUserName,
            a.auditor_id AS auditorId,
            a.audited_at AS auditedAt,
            a.reject_reason AS rejectReason,
            COALESCE(q.title, kb.title, CONCAT('Answer#', ans.id), CONCAT('Comment#', c.id)) AS contentTitle,
            LEFT(COALESCE(q.content, ans.content, c.content, kb.summary, kb.content), 200) AS contentSnippet,
            COALESCE(q.status, ans.status, c.status, kb.status) AS contentStatus
        FROM cms_audit a
        LEFT JOIN qa_question q ON a.biz_type = 1 AND q.id = a.biz_id
        LEFT JOIN qa_answer ans ON a.biz_type = 2 AND ans.id = a.biz_id
        LEFT JOIN qa_comment c ON a.biz_type = 3 AND c.id = a.biz_id
        LEFT JOIN kb_entry kb ON a.biz_type = 4 AND kb.id = a.biz_id
        LEFT JOIN `user` su ON su.id = a.submit_user_id
        WHERE 1 = 1
          <if test="bizType != null">
            AND a.biz_type = #{bizType}
          </if>
          <if test="auditStatus != null">
            AND a.audit_status = #{auditStatus}
          </if>
          <if test="triggerSource != null">
            AND a.trigger_source = #{triggerSource}
          </if>
          <if test="keyword != null and keyword != ''">
            AND (
              q.title LIKE CONCAT('%', #{keyword}, '%')
              OR q.content LIKE CONCAT('%', #{keyword}, '%')
              OR ans.content LIKE CONCAT('%', #{keyword}, '%')
              OR c.content LIKE CONCAT('%', #{keyword}, '%')
              OR kb.title LIKE CONCAT('%', #{keyword}, '%')
              OR kb.summary LIKE CONCAT('%', #{keyword}, '%')
              OR kb.content LIKE CONCAT('%', #{keyword}, '%')
            )
          </if>
          <if test="startTime != null">
            AND a.created_at &gt;= #{startTime}
          </if>
          <if test="endTime != null">
            AND a.created_at &lt;= #{endTime}
          </if>
        ORDER BY
          <choose>
            <when test="sortBy == 'modelScore'">a.model_score</when>
            <otherwise>a.created_at</otherwise>
          </choose>
          <choose>
            <when test="sortOrder == 'asc'">ASC</when>
            <otherwise>DESC</otherwise>
          </choose>,
          a.id DESC
        </script>
        """)
    List<CmsAuditPageItemVO> selectAdminAuditPage(@Param("bizType") Integer bizType,
                                                  @Param("auditStatus") Integer auditStatus,
                                                  @Param("triggerSource") Integer triggerSource,
                                                  @Param("keyword") String keyword,
                                                  @Param("startTime") LocalDateTime startTime,
                                                  @Param("endTime") LocalDateTime endTime,
                                                  @Param("sortBy") String sortBy,
                                                  @Param("sortOrder") String sortOrder);

    @Select("""
        SELECT
            a.id,
            a.biz_type AS bizType,
            a.biz_id AS bizId,
            a.trigger_source AS triggerSource,
            a.audit_type AS auditType,
            a.audit_status AS auditStatus,
            a.action,
            a.model_label AS modelLabel,
            a.model_score AS modelScore,
            a.hit_detail AS hitDetail,
            a.reject_reason AS rejectReason,
            a.submit_user_id AS submitUserId,
            a.auditor_id AS auditorId,
            a.audited_at AS auditedAt,
            a.created_at AS createdAt,
            a.updated_at AS updatedAt,
            COALESCE(q.title, kb.title, CONCAT('Answer#', ans.id), CONCAT('Comment#', c.id)) AS contentTitle,
            COALESCE(q.content, ans.content, c.content, kb.content) AS contentText,
            COALESCE(q.status, ans.status, c.status, kb.status) AS contentStatus,
            COALESCE(q.reject_reason, ans.reject_reason, c.reject_reason) AS contentRejectReason,
            COALESCE(q.created_at, ans.created_at, c.created_at, kb.created_at) AS contentCreatedAt,
            u.id AS authorId,
            u.username AS authorUsername,
            u.nickname AS authorNickname,
            u.status AS authorStatus
        FROM cms_audit a
        LEFT JOIN qa_question q ON a.biz_type = 1 AND q.id = a.biz_id
        LEFT JOIN qa_answer ans ON a.biz_type = 2 AND ans.id = a.biz_id
        LEFT JOIN qa_comment c ON a.biz_type = 3 AND c.id = a.biz_id
        LEFT JOIN kb_entry kb ON a.biz_type = 4 AND kb.id = a.biz_id
        LEFT JOIN `user` u ON u.id = COALESCE(q.user_id, ans.user_id, c.user_id, kb.author_user_id)
        WHERE a.id = #{id}
        LIMIT 1
        """)
    CmsAuditDetailRowVO selectAdminAuditDetail(@Param("id") Long id);
}
