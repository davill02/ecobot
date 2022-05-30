package com.artuhanau.ecobot.yandex.translate.standard;

import com.artuhanau.ecobot.yandex.translate.TranslationService;
import com.artuhanau.ecobot.yandex.translate.YandexResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

@Component
public class YandexTranslationService implements TranslationService {
    private static final Logger LOG = LoggerFactory.getLogger(YandexTranslationService.class);
    public static final String YANDEX_ADDITIONAL_INFO = "Russian Russian translation ";

    @Resource
    private YandexTranslationConfigs configs;

    @Override
    public String translateToEnglish(String text) {
        String translatedText = "";
        try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
            HttpGet getRequest = new HttpGet();
            getRequest.setURI(buildUri(text));
            HttpResponse response = client.execute(getRequest);
            translatedText = handleRequest(response);
        } catch (IOException exception) {
            LOG.error(exception.getMessage());
        }
        LOG.info("Translated text: {}", translatedText);
        return translatedText;
    }

    private String handleRequest(HttpResponse response) throws IOException {
        StatusLine statusLine = response.getStatusLine();
        if (statusLine.getStatusCode() > 300) {
            LOG.error(statusLine.getStatusCode() + " " + statusLine.getReasonPhrase());
        }
        String jsonBody = EntityUtils.toString(response.getEntity());
        ObjectMapper objectMapper = new ObjectMapper();
        YandexResponse translation = objectMapper.readValue(jsonBody, YandexResponse.class);
        return String.join("", translation.getText()).replaceAll(YANDEX_ADDITIONAL_INFO, "");
    }

    private URI buildUri(String text) {
        URI uri = null;
        try {
            uri = (new URIBuilder()).setScheme(configs.getYandexScheme())
                    .setPath(configs.getYandexUrl())
                    .setHost(configs.getHost())
                    .addParameter("key", configs.getYandexToken())
                    .addParameter("lang", configs.getLang())
                    .addParameter("text", text)
                    .build();
        } catch (URISyntaxException uriSyntaxException) {
            LOG.error(uriSyntaxException.getMessage());
        }
        return uri;
    }
}
