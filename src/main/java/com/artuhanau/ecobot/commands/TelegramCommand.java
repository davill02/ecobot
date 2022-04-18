package com.artuhanau.ecobot.commands;

import org.springframework.context.ApplicationContext;
import org.telegram.telegrambots.meta.api.objects.Message;

public interface TelegramCommand {
    CommandResult execute(Message message, String parameter);

    void setContext(ApplicationContext context);
}
