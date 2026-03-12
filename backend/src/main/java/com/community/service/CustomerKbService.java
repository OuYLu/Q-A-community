package com.community.service;

import com.community.dto.AppAnswerCommentCreateDTO;
import com.community.vo.AppKbCommentVO;
import com.community.vo.AppKbInteractVO;

import java.util.List;

public interface CustomerKbService {
    AppKbInteractVO interaction(Long kbEntryId);

    AppKbInteractVO toggleLike(Long kbEntryId);

    AppKbInteractVO toggleFavorite(Long kbEntryId);

    List<AppKbCommentVO> comments(Long kbEntryId);

    Long createComment(Long kbEntryId, AppAnswerCommentCreateDTO dto);
}
