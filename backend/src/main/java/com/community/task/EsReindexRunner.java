package com.community.task;

import com.community.config.EsProperties;
import com.community.service.EsSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EsReindexRunner implements CommandLineRunner {
    private final EsSearchService esSearchService;
    private final EsProperties esProperties;

    @Override
    public void run(String... args) {
        if (esProperties != null && esProperties.isReindexOnStartup() && esSearchService.isEnabled()) {
            esSearchService.reindexAll();
        }
    }
}