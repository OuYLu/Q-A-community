package com.community.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AdminMenuVO {
    private Long id;
    private String code;
    private String name;
    private String type;
    private Long parentId;
    private String pathOrApi;
    private String component;
    private String icon;
    private Integer sort;
    private Integer visible;
    private List<AdminMenuVO> children = new ArrayList<>();
}
