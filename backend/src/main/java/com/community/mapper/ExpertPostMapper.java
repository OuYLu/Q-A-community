package com.community.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.entity.KbEntry;
import com.community.vo.AppExpertPostDetailVO;
import com.community.vo.AppExpertPostItemVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ExpertPostMapper extends BaseMapper<KbEntry> {
    List<AppExpertPostItemVO> selectPublishedPage(@Param("keyword") String keyword,
                                                  @Param("sortBy") String sortBy,
                                                  @Param("categoryId") Long categoryId);

    List<AppExpertPostItemVO> selectMyPage(@Param("authorUserId") Long authorUserId,
                                           @Param("keyword") String keyword,
                                           @Param("status") Integer status);

    AppExpertPostDetailVO selectPublishedDetail(@Param("id") Long id);

    AppExpertPostDetailVO selectMyDetail(@Param("id") Long id, @Param("authorUserId") Long authorUserId);

    int increaseViewCount(@Param("id") Long id);

    Long countMyPosts(@Param("authorUserId") Long authorUserId);

    List<AppExpertPostItemVO> selectPublishedByAuthor(@Param("authorUserId") Long authorUserId);

    Long countPublishedByAuthor(@Param("authorUserId") Long authorUserId);
}
