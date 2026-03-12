package com.community.controller.test;

import com.community.config.EsProperties;
import lombok.RequiredArgsConstructor;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/es")
@RequiredArgsConstructor
public class EsTestController {

    private final RestClient restClient;
    private final EsProperties esProperties;

    @GetMapping("/ping")
    public Map<String, Object> ping() throws IOException {
        Map<String, Object> result = new HashMap<>();
        result.put("enabled", esProperties.isEnabled());
        result.put("baseUrl", esProperties.getBaseUrl());

        if (!Boolean.TRUE.equals(esProperties.isEnabled())) {
            result.put("success", false);
            result.put("message", "ES 功能未开启，请检查 es.enabled 配置");
            return result;
        }

        Request request = new Request("GET", "/");
        Response response = restClient.performRequest(request);

        result.put("success", true);
        result.put("statusCode", response.getStatusLine().getStatusCode());
        result.put("message", "ES 连接成功");
        return result;
    }
}