package com.community.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.entity.ExpertProfile;
import com.community.vo.ExpertManageVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ExpertProfileMapper extends BaseMapper<ExpertProfile> {
    @Select("""
        <script>
        SELECT
          ep.user_id AS userId,
          ep.real_name AS realName,
          ep.organization AS organization,
          ep.title AS title,
          ep.expertise AS expertise,
          ep.verified_at AS verifiedAt,
          u.expert_status AS expertStatus
        FROM expert_profile ep
        JOIN user u ON ep.user_id = u.id
        WHERE u.expert_status IN (3, 0)
          <if test="realName != null and realName != ''">
            AND ep.real_name LIKE CONCAT('%', #{realName}, '%')
          </if>
          <if test="organization != null and organization != ''">
            AND ep.organization LIKE CONCAT('%', #{organization}, '%')
          </if>
          <if test="expertise != null and expertise != ''">
            AND ep.expertise LIKE CONCAT('%', #{expertise}, '%')
          </if>
          <if test="expertStatus != null">
            AND u.expert_status = #{expertStatus}
          </if>
        ORDER BY ep.created_at DESC
        </script>
        """)
    List<ExpertManageVO> selectExpertManageList(String realName, String organization, String expertise, Integer expertStatus);

    @Select("""
        SELECT
          ep.user_id AS userId,
          ep.real_name AS realName,
          ep.organization AS organization,
          ep.title AS title,
          ep.expertise AS expertise,
          ep.verified_at AS verifiedAt,
          u.expert_status AS expertStatus
        FROM expert_profile ep
        JOIN user u ON ep.user_id = u.id
        WHERE ep.user_id = #{userId}
        """)
    ExpertManageVO selectExpertManageByUserId(Long userId);
}
