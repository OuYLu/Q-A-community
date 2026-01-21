package com.community.dto.admin;

public class PermissionItemDTO {
    private Long id;
    private String code;
    private String name;
    private String description;
    private String type;
    private String pathOrApi;
    private String method;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPathOrApi() {
        return pathOrApi;
    }

    public void setPathOrApi(String pathOrApi) {
        this.pathOrApi = pathOrApi;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }
}
