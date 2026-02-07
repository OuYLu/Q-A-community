package com.community.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.entity.Permission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PermissionMapper extends BaseMapper<Permission> {
    @Select("""
        SELECT p.*
        FROM role_permission rp
        JOIN permission p ON rp.permission_id = p.id
        WHERE rp.role_id = #{roleId}
        ORDER BY p.created_at DESC
        """)
    List<Permission> selectByRoleId(Long roleId);

    @Select("""
        SELECT p.*
        FROM user_role ur
        JOIN role_permission rp ON ur.role_id = rp.role_id
        JOIN permission p ON rp.permission_id = p.id
        WHERE ur.user_id = #{userId}
          AND p.type = 'menu'
        ORDER BY p.parent_id ASC, p.sort ASC, p.id ASC
        """)
    List<Permission> selectMenusByUserId(Long userId);
}
