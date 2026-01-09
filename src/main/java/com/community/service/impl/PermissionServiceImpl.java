package com.community.service.impl;

import com.community.common.exception.BizException;
import com.community.common.result.ErrorCode;
import com.community.mapper.PermissionMapper;
import com.community.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PermissionServiceImpl implements PermissionService {

    private static class CacheEntry {
        private final Set<String> permissions;
        private final long expireAt;

        CacheEntry(Set<String> permissions, long expireAt) {
            this.permissions = permissions;
            this.expireAt = expireAt;
        }
    }

    private static final long CACHE_TTL_MILLIS = 5 * 60 * 1000;

    private final Map<Long, CacheEntry> cache = new ConcurrentHashMap<>();

    @Autowired
    private PermissionMapper permissionMapper;

    @Override
    public Set<String> getUserPermissions(Long userId) {
        if (userId == null) {
            return Set.of();
        }
        CacheEntry entry = cache.get(userId);
        long now = Instant.now().toEpochMilli();
        if (entry != null && entry.expireAt > now) {
            return entry.permissions;
        }
        List<String> codes = permissionMapper.findCodesByUserId(userId);
        Set<String> result = new HashSet<>(codes);
        cache.put(userId, new CacheEntry(result, now + CACHE_TTL_MILLIS));
        return result;
    }

    @Override
    public void require(Long userId, String permission) {
        Set<String> permissions = getUserPermissions(userId);
        if (!permissions.contains(permission)) {
            throw new BizException(ErrorCode.FORBIDDEN.getCode(), "无权限");
        }
    }
}
