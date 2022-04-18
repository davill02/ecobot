package com.artuhanau.ecobot.services;

import org.telegram.telegrambots.meta.api.objects.Message;

public interface MessageService {
    Object createResponse(Message message);
}
