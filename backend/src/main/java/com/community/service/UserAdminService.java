package com.community.service;

import com.community.dto.UserStatusDTO;
import com.community.dto.UserUpdateDTO;
import com.community.dto.UserQueryDTO;
import com.community.vo.UserManageVO;
import com.github.pagehelper.PageInfo;

public interface UserAdminService {
    PageInfo<UserManageVO> listManageableUsers(UserQueryDTO query);

    void updateUser(Long id, UserUpdateDTO dto);

    void updateStatus(Long id, UserStatusDTO dto);

    void deleteUser(Long id);

    UserManageVO getUserDetail(Long id);
}
