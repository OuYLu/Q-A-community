package com.community.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "es")
public class EsProperties {
    private boolean enabled = false;
    private String baseUrl = "http://localhost:9200";
    private String indexQuestion = "smart_question";
    private String indexKb = "smart_kb";
    private boolean reindexOnStartup = false;
}