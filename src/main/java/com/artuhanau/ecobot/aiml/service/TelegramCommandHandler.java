package com.artuhanau.ecobot.aiml.service;

import java.util.List;

import com.artuhanau.ecobot.daos.models.User;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

public interface TelegramCommandHandler
{
    boolean shouldHandle(final String command, final User user);

    void handle(final User user, final String command, final List<String> responseCommands, List<SendMessage> sendMessageList);
}
