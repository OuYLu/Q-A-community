package com.community.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.entity.QaFavorite;
import com.community.vo.AppMyFavoriteItemVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface QaFavoriteMapper extends BaseMapper<QaFavorite> {
    List<AppMyFavoriteItemVO> selectMyFavorites(@Param("userId") Long userId);
}
