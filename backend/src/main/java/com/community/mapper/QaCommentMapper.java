package com.community.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.entity.QaComment;
import com.community.vo.AppAnswerCommentVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface QaCommentMapper extends BaseMapper<QaComment> {
    List<AppAnswerCommentVO> selectAnswerComments(@Param("answerId") Long answerId);
}
