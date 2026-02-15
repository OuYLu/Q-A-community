package com.community.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    @Select("""
        SELECT r.code
        FROM user_role ur
        JOIN role r ON ur.role_id = r.id
        WHERE ur.user_id = #{userId}
        """)
    List<String> selectRoleCodesByUserId(Long userId);

    @Select("""
        SELECT *
        FROM user
        WHERE username = #{value} OR phone = #{value}
        LIMIT 1
        """)
    User selectByUsernameOrPhone(String value);

    @Select("""
        SELECT DISTINCT p.code
        FROM user_role ur
        JOIN role_permission rp ON ur.role_id = rp.role_id
        JOIN permission p ON rp.permission_id = p.id
        WHERE ur.user_id = #{userId}
        """)
    List<String> selectPermCodesByUserId(Long userId);

    @Select("""
        <script>
        SELECT DISTINCT u.*
        FROM user u
        JOIN user_role ur ON u.id = ur.user_id
        JOIN role r ON ur.role_id = r.id
        WHERE r.code IN ('staff','customer','expert')
          AND u.id NOT IN (
            SELECT ur2.user_id
            FROM user_role ur2
            JOIN role r2 ON ur2.role_id = r2.id
            WHERE r2.code = 'admin'
          )
          <if test="username != null and username != ''">
            AND u.username LIKE CONCAT('%', #{username}, '%')
          </if>
          <if test="nickname != null and nickname != ''">
            AND u.nickname LIKE CONCAT('%', #{nickname}, '%')
          </if>
          <if test="status != null">
            AND u.status = #{status}
          </if>
          <if test="roleCode != null and roleCode != ''">
            AND r.code = #{roleCode}
          </if>
        ORDER BY u.created_at DESC
        </script>
        """)
    List<User> selectManageableUsers(String username, String nickname, Integer status, String roleCode);
}
