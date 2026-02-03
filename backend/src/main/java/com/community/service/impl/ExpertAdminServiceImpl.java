package com.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.community.common.BizException;
import com.community.common.ResultCode;
import com.community.common.SecurityUser;
import com.community.dto.ExpertReviewDTO;
import com.community.entity.ExpertApply;
import com.community.entity.ExpertProfile;
import com.community.entity.Role;
import com.community.entity.User;
import com.community.entity.UserRole;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.community.mapper.ExpertApplyMapper;
import com.community.mapper.ExpertProfileMapper;
import com.community.mapper.RoleMapper;
import com.community.mapper.UserMapper;
import com.community.mapper.UserRoleMapper;
import com.community.service.ExpertAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
@Service
@RequiredArgsConstructor
public class ExpertAdminServiceImpl extends ServiceImpl<ExpertApplyMapper, ExpertApply> implements ExpertAdminService {
    private final UserMapper userMapper;
    private final ExpertProfileMapper expertProfileMapper;
    private final RoleMapper roleMapper;
    private final UserRoleMapper userRoleMapper;

    @Override
    public PageInfo<ExpertApply> listApplies(Integer status, int pageNum, int pageSize) {
        LambdaQueryWrapper<ExpertApply> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(ExpertApply::getStatus, status);
        }
        wrapper.orderByDesc(ExpertApply::getCreatedAt);
        PageHelper.startPage(pageNum, pageSize);
        return new PageInfo<>(this.list(wrapper));
    }

    @Override
    @Transactional
    public void review(ExpertReviewDTO dto) {
        SecurityUser reviewer = getCurrentSecurityUser();
        if (reviewer == null) {
            throw new BizException(ResultCode.UNAUTHORIZED, "请先登录");
        }

        ExpertApply apply = this.getById(dto.getApplyId());
        if (apply == null) {
            throw new BizException(ResultCode.BAD_REQUEST, "申请未找到");
        }
        if (apply.getStatus() == null || apply.getStatus() != 1) {
            throw new BizException(ResultCode.BAD_REQUEST, "你当前正在申请中，请勿重复提交");
        }

        Integer status = dto.getStatus();
        if (status == null || (status != 2 && status != 3)) {
            throw new BizException(ResultCode.BAD_REQUEST, "申请状态无效");
        }

        LocalDateTime now = LocalDateTime.now();
        apply.setStatus(status);
        apply.setReviewerId(reviewer.getId());
        apply.setReviewAt(now);
        apply.setUpdatedAt(now);

        User user = userMapper.selectById(apply.getUserId());
        if (user == null) {
            throw new BizException(ResultCode.BAD_REQUEST, "找不到用户");
        }

        if (status == 2) {
            apply.setRejectReason(null);
            user.setExpertStatus(3);
            user.setExpertVerifiedAt(now);
            user.setUpdatedAt(now);
            userMapper.updateById(user);

            Role expertRole = roleMapper.selectOne(new LambdaQueryWrapper<Role>()
                .eq(Role::getCode, "expert")
                .last("LIMIT 1"));
            if (expertRole == null) {
                throw new BizException(ResultCode.SERVER_ERROR, "暂未配置专家角色");
            }
            UserRole existing = userRoleMapper.selectOne(new LambdaQueryWrapper<UserRole>()
                .eq(UserRole::getUserId, user.getId())
                .eq(UserRole::getRoleId, expertRole.getId())
                .last("LIMIT 1"));
            if (existing == null) {
                UserRole userRole = new UserRole();
                userRole.setUserId(user.getId());
                userRole.setRoleId(expertRole.getId());
                userRole.setCreatedAt(now);
                userRoleMapper.insert(userRole);
            }

            ExpertProfile profile = expertProfileMapper.selectOne(new LambdaQueryWrapper<ExpertProfile>()
                .eq(ExpertProfile::getUserId, user.getId())
                .last("LIMIT 1"));
            if (profile == null) {
                profile = new ExpertProfile();
                profile.setUserId(user.getId());
                profile.setRealName(apply.getRealName());
                profile.setOrganization(apply.getOrganization());
                profile.setTitle(apply.getTitle());
                profile.setExpertise(apply.getExpertise());
                profile.setVerifiedAt(now);
                profile.setCreatedAt(now);
                profile.setUpdatedAt(now);
                expertProfileMapper.insert(profile);
            } else {
                profile.setRealName(apply.getRealName());
                profile.setOrganization(apply.getOrganization());
                profile.setTitle(apply.getTitle());
                profile.setExpertise(apply.getExpertise());
                profile.setVerifiedAt(now);
                profile.setUpdatedAt(now);
                expertProfileMapper.updateById(profile);
            }
        } else {
            if (!StringUtils.hasText(dto.getRejectReason())) {
                throw new BizException(ResultCode.BAD_REQUEST, "需要填写拒绝理由");
            }
            apply.setRejectReason(dto.getRejectReason());
            user.setExpertStatus(4);
            user.setExpertVerifiedAt(null);
            user.setUpdatedAt(now);
            userMapper.updateById(user);
        }

        this.updateById(apply);
    }

    private SecurityUser getCurrentSecurityUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof SecurityUser securityUser)) {
            return null;
        }
        return securityUser;
    }
}
