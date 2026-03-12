package com.community.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "es")
public class EsProperties {

    /**
     * 是否启用 ES
     */
    private boolean enabled = false;

    /**
     * ES 地址
     */
    private String baseUrl;

    /**
     * 问题索引名
     */
    private String indexQuestion;

    /**
     * 知识库索引名
     */
    private String indexKb;

    /**
     * 启动时是否重建索引
     */
    private boolean reindexOnStartup = false;

    /**
     * 搜索策略: mysql/es_lexical/es_hybrid
     */
    private String searchStrategy = "es_lexical";
}
