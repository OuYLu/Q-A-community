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
}
