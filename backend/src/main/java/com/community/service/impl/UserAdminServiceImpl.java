package com.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.community.common.BizException;
import com.community.common.ResultCode;
import com.community.dto.UserQueryDTO;
import com.community.dto.UserStatusDTO;
import com.community.dto.UserUpdateDTO;
import com.community.entity.User;
import com.community.mapper.UserMapper;
import com.community.service.UserAdminService;
import com.community.vo.UserManageVO;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserAdminServiceImpl extends ServiceImpl<UserMapper, User> implements UserAdminService {

    @Override
    public PageInfo<UserManageVO> listManageableUsers(UserQueryDTO query) {
        String username = query == null ? null : query.getUsername();
        String nickname = query == null ? null : query.getNickname();
        Integer status = query == null ? null : query.getStatus();
        String roleCode = query == null ? null : query.getRoleCode();
        Integer pageNum = query == null || query.getPageNum() == null ? 1 : query.getPageNum();
        Integer pageSize = query == null || query.getPageSize() == null ? 10 : query.getPageSize();

        PageHelper.startPage(pageNum, pageSize);
        List<User> users = this.baseMapper.selectManageableUsers(username, nickname, status, roleCode);
        List<UserManageVO> items = new ArrayList<>();
        for (User user : users) {
            List<String> roleCodes = this.baseMapper.selectRoleCodesByUserId(user.getId());
            items.add(toVO(user, roleCodes));
        }
        return new PageInfo<>(items);
    }

    @Override
    @Transactional
    public void updateUser(Long id, UserUpdateDTO dto) {
        User user = this.getById(id);
        if (user == null) {
            throw new BizException(ResultCode.BAD_REQUEST, "User not found");
        }
        ensureNotAdmin(user.getId());

        if (StringUtils.hasText(dto.getPhone())) {
            User phoneExist = this.getOne(new LambdaQueryWrapper<User>().eq(User::getPhone, dto.getPhone()));
            if (phoneExist != null && !phoneExist.getId().equals(user.getId())) {
                throw new BizException(ResultCode.BAD_REQUEST, "Phone already used");
            }
        }

        user.setNickname(dto.getNickname());
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setAvatar(dto.getAvatar());
        user.setUpdatedAt(LocalDateTime.now());
        this.updateById(user);
    }

    @Override
    @Transactional
    public void updateStatus(Long id, UserStatusDTO dto) {
        User user = this.getById(id);
        if (user == null) {
            throw new BizException(ResultCode.BAD_REQUEST, "User not found");
        }
        ensureNotAdmin(user.getId());

        // Keep expert status in sync with user freeze/unfreeze state.
        if (dto.getStatus() != null && dto.getStatus() == 0
            && user.getExpertStatus() != null && user.getExpertStatus() == 3) {
            user.setExpertStatus(0);
        } else if (dto.getStatus() != null && dto.getStatus() == 1
            && user.getExpertStatus() != null && user.getExpertStatus() == 0) {
            user.setExpertStatus(3);
        }

        user.setStatus(dto.getStatus());
        user.setUpdatedAt(LocalDateTime.now());
        this.updateById(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = this.getById(id);
        if (user == null) {
            throw new BizException(ResultCode.BAD_REQUEST, "User not found");
        }
        ensureNotAdmin(user.getId());
        this.removeById(id);
    }

    @Override
    public UserManageVO getUserDetail(Long id) {
        User user = this.getById(id);
        if (user == null) {
            throw new BizException(ResultCode.BAD_REQUEST, "User not found");
        }
        ensureNotAdmin(user.getId());
        List<String> roleCodes = this.baseMapper.selectRoleCodesByUserId(user.getId());
        return toVO(user, roleCodes);
    }

    private void ensureNotAdmin(Long userId) {
        List<String> roles = this.baseMapper.selectRoleCodesByUserId(userId);
        if (roles.stream().anyMatch(role -> "admin".equalsIgnoreCase(role))) {
            throw new BizException(ResultCode.BAD_REQUEST, "Admin user cannot be modified");
        }
    }

    private UserManageVO toVO(User user, List<String> roleCodes) {
        UserManageVO vo = new UserManageVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setPhone(user.getPhone());
        vo.setEmail(user.getEmail());
        vo.setStatus(user.getStatus());
        vo.setRoleCodes(roleCodes);
        vo.setAvatar(user.getAvatar());
        vo.setCreatedAt(user.getCreatedAt());
        vo.setDisplayRole(resolveDisplayRole(roleCodes));
        vo.setCreatedAt(user.getCreatedAt());
        return vo;
    }

    private String resolveDisplayRole(List<String> roleCodes) {
        if (roleCodes == null) {
            return "customer";
        }
        if (roleCodes.contains("expert")) {
            return "expert";
        }
        if (roleCodes.contains("staff")) {
            return "staff";
        }
        if (roleCodes.contains("customer")) {
            return "customer";
        }
        return "customer";
    }
}
