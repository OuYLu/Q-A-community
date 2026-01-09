package com.community.service;

import java.util.Set;

public interface PermissionService {
    Set<String> getUserPermissions(Long userId);

    void require(Long userId, String permission);
}
