package com.artuhanau.ecobot.commands;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

public class TelegramUtils {
    public static SendMessage.SendMessageBuilder getSendMessageBuilder(Message message) {
        return SendMessage.builder().chatId(message.getChatId().toString());
    }
}
