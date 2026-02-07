package com.community.service;

import com.community.vo.AdminMenuVO;

import java.util.List;

public interface MenuService {
    List<AdminMenuVO> listCurrentUserMenus();
}
