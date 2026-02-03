package com.community.service;

import com.community.dto.AdminCreateStaffDTO;
import com.community.dto.CustomerRegisterDTO;
import com.community.entity.User;
import com.community.vo.UserVO;

import java.util.List;

public interface UserService {
    User findByUsername(String username);

    User findByUsernameOrPhone(String usernameOrPhone);

    List<String> getRoleCodes(Long userId);

    UserVO registerCustomer(CustomerRegisterDTO dto);

    UserVO createStaff(AdminCreateStaffDTO dto);

    UserVO getCurrentUser();

    List<String> getPermCodes(Long id);
}
