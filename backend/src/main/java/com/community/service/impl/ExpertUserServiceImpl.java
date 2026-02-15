package com.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.community.common.BizException;
import com.community.common.ResultCode;
import com.community.dto.ExpertStatusUpdateDTO;
import com.community.dto.ExpertUserQueryDTO;
import com.community.entity.User;
import com.community.mapper.ExpertProfileMapper;
import com.community.mapper.UserMapper;
import com.community.service.ExpertUserService;
import com.community.vo.ExpertManageVO;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpertUserServiceImpl extends ServiceImpl<UserMapper, User> implements ExpertUserService {
    private final ExpertProfileMapper expertProfileMapper;

    @Override
    public PageInfo<ExpertManageVO> listExperts(ExpertUserQueryDTO query) {
        String realName = query == null ? null : query.getRealName();
        String organization = query == null ? null : query.getOrganization();
        String expertise = query == null ? null : query.getExpertise();
        Integer expertStatus = query == null ? null : query.getExpertStatus();
        Integer pageNum = query == null || query.getPageNum() == null ? 1 : query.getPageNum();
        Integer pageSize = query == null || query.getPageSize() == null ? 10 : query.getPageSize();

        PageHelper.startPage(pageNum, pageSize);
        List<ExpertManageVO> items = expertProfileMapper
            .selectExpertManageList(realName, organization, expertise, expertStatus);
        return new PageInfo<>(items);
    }

    @Override
    @Transactional
    public void updateExpertStatus(Long userId, ExpertStatusUpdateDTO dto) {
        User user = this.getById(userId);
        if (user == null) {
            throw new BizException(ResultCode.BAD_REQUEST, "User not found");
        }

        Integer status = dto == null ? null : dto.getExpertStatus();
        if (status == null || (status != 0 && status != 3)) {
            throw new BizException(ResultCode.BAD_REQUEST, "Invalid expert status");
        }

        if (user.getExpertStatus() == null || (user.getExpertStatus() != 3 && user.getExpertStatus() != 0)) {
            throw new BizException(ResultCode.BAD_REQUEST, "User is not a certified expert");
        }

        user.setExpertStatus(status);
        user.setUpdatedAt(LocalDateTime.now());
        this.updateById(user);
    }

    @Override
    public ExpertManageVO getExpertDetail(Long userId) {
        ExpertManageVO vo = expertProfileMapper.selectExpertManageByUserId(userId);
        if (vo == null) {
            throw new BizException(ResultCode.BAD_REQUEST, "Expert not found");
        }
        return vo;
    }
}
