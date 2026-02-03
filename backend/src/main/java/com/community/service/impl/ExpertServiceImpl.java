package com.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.community.common.BizException;
import com.community.common.ResultCode;
import com.community.common.SecurityUser;
import com.community.dto.ExpertApplyDTO;
import com.community.entity.ExpertApply;
import com.community.entity.User;
import com.community.mapper.ExpertApplyMapper;
import com.community.mapper.UserMapper;
import com.community.service.ExpertService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ExpertServiceImpl extends ServiceImpl<ExpertApplyMapper, ExpertApply> implements ExpertService {
    private final UserMapper userMapper;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public Long applyExpert(ExpertApplyDTO dto) {
        SecurityUser securityUser = getCurrentSecurityUser();
        if (securityUser == null) {
            throw new BizException(ResultCode.UNAUTHORIZED, "请先登录");
        }

        User user = userMapper.selectById(securityUser.getId());
        if (user == null) {
            throw new BizException(ResultCode.BAD_REQUEST, "找不到用户");
        }

        Integer expertStatus = user.getExpertStatus();
        if (expertStatus != null) {
            if (expertStatus == 2) {
                throw new BizException(ResultCode.BAD_REQUEST, "专家认证正在审核中");
            }
            if (expertStatus == 3) {
                throw new BizException(ResultCode.BAD_REQUEST, "专家认证已通过");
            }
        }

        ExpertApply pending = this.getOne(new LambdaQueryWrapper<ExpertApply>()
            .eq(ExpertApply::getUserId, user.getId())
            .eq(ExpertApply::getStatus, 1)
            .last("LIMIT 1"));
        if (pending != null) {
            throw new BizException(ResultCode.BAD_REQUEST, "你当前正在申请中，请勿重复提交");
        }

        LocalDateTime now = LocalDateTime.now();
        ExpertApply apply = new ExpertApply();
        apply.setUserId(user.getId());
        apply.setRealName(dto.getRealName());
        apply.setOrganization(dto.getOrganization());
        apply.setTitle(dto.getTitle());
        apply.setExpertise(dto.getExpertise());
        apply.setProofUrls(objectMapper.valueToTree(dto.getProofUrls()));
        apply.setStatus(1);
        apply.setCreatedAt(now);
        apply.setUpdatedAt(now);
        this.save(apply);

        user.setExpertStatus(2);
        user.setUpdatedAt(now);
        userMapper.updateById(user);

        return apply.getId();
    }

    private SecurityUser getCurrentSecurityUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof SecurityUser securityUser)) {
            return null;
        }
        return securityUser;
    }
}
