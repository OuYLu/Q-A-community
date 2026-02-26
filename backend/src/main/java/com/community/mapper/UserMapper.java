package com.community.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    List<String> selectRoleCodesByUserId(@Param("userId") Long userId);

    User selectByUsernameOrPhone(@Param("value") String value);

    List<String> selectPermCodesByUserId(@Param("userId") Long userId);

    List<User> selectManageableUsers(@Param("username") String username,
                                     @Param("nickname") String nickname,
                                     @Param("status") Integer status,
                                     @Param("roleCode") String roleCode);
}
