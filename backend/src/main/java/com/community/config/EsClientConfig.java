package com.community.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EsClientConfig {

    @Bean(destroyMethod = "close")
    public RestClient restClient(EsProperties esProperties) {
        return RestClient.builder(
                HttpHost.create(esProperties.getBaseUrl())
        ).build();
    }
}