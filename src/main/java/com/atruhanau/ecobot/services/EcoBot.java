package com.atruhanau.ecobot.services;

import com.atruhanau.ecobot.configs.BotConfigs;
import lombok.SneakyThrows;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import javax.annotation.Resource;

@Component
public class EcoBot extends TelegramLongPollingBot {
    @Resource
    private MessageService messageService;
    @Resource
    private BotConfigs botConfigs;

    @Override
    public String getBotUsername() {
        return botConfigs.getBotName();
    }

    @Override
    public String getBotToken() {
        return botConfigs.getBotToken();
    }

    @SneakyThrows
    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && !update.getMessage().getText().isEmpty()) {
            execute(messageService.createResponse(update.getMessage()));
        }
    }

    @EventListener({ContextRefreshedEvent.class})
    public void initialize() throws TelegramApiException {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        try {
            telegramBotsApi.registerBot(this);
        } catch (TelegramApiRequestException ignored) {

        }
    }
}
