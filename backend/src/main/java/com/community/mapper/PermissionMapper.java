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
}
