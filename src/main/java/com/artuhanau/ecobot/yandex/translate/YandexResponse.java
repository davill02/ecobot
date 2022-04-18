package com.artuhanau.ecobot.yandex.translate;

import java.util.List;

public class YandexResponse {
    private List<String> text;
    private String code;
    private String lang;

    public List<String> getText() {
        return text;
    }

    public void setText(List<String> text) {
        this.text = text;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

}
