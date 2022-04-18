package com.artuhanau.ecobot.yandex.translate.standard;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class YandexTranslationConfigs {
    @Value("${yandex.translation.api.lang}")
    private String lang;
    @Value("${yandex.translation.key}")
    private String yandexToken;
    @Value("${yandex.translation.api.url}")
    private String yandexUrl;
    @Value("${yandex.translation.api.scheme}")
    private String yandexScheme;
    @Value("${yandex.translation.api.host}")
    private String host;
}
