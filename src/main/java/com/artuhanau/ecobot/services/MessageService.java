package com.artuhanau.ecobot.services;

import java.util.List;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

public interface MessageService
{
    List<Object> createResponse(Message message);

    List<Object> wrapSendMessage(SendMessage sendMessage);
}
