package com.atruhanau.ecobot.configs;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class BotConfigs {
    @Value("${bot.telegram.api.name}")
    private String botName;
    @Value("${bot.telegram.api.token}")
    private String botToken;
}
