package com.srb.backend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "resume.recall")
public class ResumeRecallProperties {

    private boolean enabled = true;

    private String scanCron = "0 * * * * ?";

    private long staleThresholdMinutes = 1;

    private long cooldownMinutes = 5;
}
