package com.community.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.entity.QaCategory;
import com.community.vo.CategoryListVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface QaCategoryMapper extends BaseMapper<QaCategory> {
    @Select("""
        <script>
        WITH RECURSIVE category_tree AS (
            SELECT c.id AS ancestor_id, c.id AS descendant_id
            FROM qa_category c
            WHERE c.delete_flag = 0

            UNION ALL

            SELECT ct.ancestor_id, child.id AS descendant_id
            FROM category_tree ct
            JOIN qa_category child
              ON child.parent_id = ct.descendant_id
             AND child.delete_flag = 0
        ),
        category_question_count AS (
            SELECT
                ct.ancestor_id AS category_id,
                COUNT(q.id) AS question_count
            FROM category_tree ct
            LEFT JOIN qa_question q
              ON q.category_id = ct.descendant_id
             AND q.delete_flag = 0
            GROUP BY ct.ancestor_id
        )
        SELECT
            c.id,
            c.name,
            c.parent_id AS parentId,
            c.icon,
            c.description,
            c.status,
            c.sort,
            c.created_at AS createdAt,
            c.updated_at AS updatedAt,
            COALESCE(cqc.question_count, 0) AS questionCount
        FROM qa_category c
        LEFT JOIN category_question_count cqc
          ON cqc.category_id = c.id
        WHERE c.delete_flag = 0
          <if test="name != null and name != ''">
            AND c.name LIKE CONCAT('%', #{name}, '%')
          </if>
          <if test="parentId != null">
            AND c.parent_id = #{parentId}
          </if>
          <if test="status != null">
            AND c.status = #{status}
          </if>
        ORDER BY c.sort ASC, c.created_at DESC
        </script>
        """)
    List<CategoryListVO> selectListWithQuestionCount(@Param("name") String name,
                                                     @Param("parentId") Long parentId,
                                                     @Param("status") Integer status);

    @Select("""
        SELECT COUNT(1)
        FROM qa_question q
        WHERE q.category_id = #{categoryId}
          AND q.delete_flag = 0
        """)
    long countQuestionRefs(Long categoryId);

    @Select("""
        SELECT COUNT(1)
        FROM qa_answer a
        JOIN qa_question q ON a.question_id = q.id
        WHERE q.category_id = #{categoryId}
          AND q.delete_flag = 0
          AND a.delete_flag = 0
        """)
    long countAnswerRefs(Long categoryId);

    @Select("""
        SELECT COUNT(1)
        FROM qa_topic_category tc
        WHERE tc.category_id = #{categoryId}
        """)
    long countTopicRefs(Long categoryId);
}
