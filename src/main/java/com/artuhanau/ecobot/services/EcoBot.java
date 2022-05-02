package com.artuhanau.ecobot.services;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Resource;

import com.artuhanau.ecobot.commands.CommandService;
import com.artuhanau.ecobot.configs.BotConfigs;
import lombok.SneakyThrows;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Component
public class EcoBot extends TelegramLongPollingBot
{
    @Resource
    private MessageService messageService;

    @Resource
    private CommandService commandService;

    @Resource
    private BotConfigs botConfigs;

    @Resource
    private ApplicationContext context;

    @Override
    public String getBotUsername()
    {
        return botConfigs.getBotName();
    }

    @Override
    public String getBotToken()
    {
        return botConfigs.getBotToken();
    }

    @SneakyThrows
    @Override
    public void onUpdateReceived(Update update)
    {
        List<Object> executableResponse = new ArrayList<>();
        String fileName = "";
        if (update.hasMessage()) {
            Message message = update.getMessage();
            if (message.hasDocument()) {
                fileName = message.getDocument().getFileName();
            }
            if (commandService.isCommand(message) && commandService.isEligibleUser(message)) {
                executableResponse.add(commandService.getTelegramExecutableResponse(message));
            }
            else {
                executableResponse = messageService.createResponse(message);
            }
        }
        final String finalFileName = fileName;
        executableResponse.forEach(e -> {
            try {
                execute(e, finalFileName);
            }
            catch (TelegramApiException ex) {
                throw new RuntimeException(ex);
            }
        });
    }

    private void execute(Object executableAction, String fileName) throws TelegramApiException
    {
        if (executableAction == null) {
            return;
        }
        if (executableAction instanceof SendDocument) {
            execute((SendDocument) executableAction);
        }
        if (executableAction instanceof SendMessage) {
            execute((SendMessage) executableAction);
        }
        if (executableAction instanceof GetFile) {
            File file = execute((GetFile) executableAction);
            commandService.afterGetFileProcess(downloadFile(file), fileName);
        }
    }

    @EventListener({ ContextRefreshedEvent.class })
    public void initialize() throws TelegramApiException
    {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        try {
            telegramBotsApi.registerBot(this);
        }
        catch (TelegramApiRequestException ignored) {

        }
    }
}
