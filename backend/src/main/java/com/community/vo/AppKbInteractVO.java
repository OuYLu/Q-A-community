package com.community.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "KB interaction summary")
public class AppKbInteractVO {
    @Schema(description = "KB entry id")
    private Long kbEntryId;

    @Schema(description = "Like count")
    private Integer likeCount;

    @Schema(description = "Favorite count")
    private Integer favoriteCount;

    @Schema(description = "Comment count")
    private Integer commentCount;

    @Schema(description = "Current user liked")
    private Boolean liked;

    @Schema(description = "Current user favorited")
    private Boolean favorited;
}
