package com.artuhanau.ecobot.commands;

import org.telegram.telegrambots.meta.api.objects.Message;

import java.io.File;

public interface CommandService {
    CommandResult parseCommandAndExecute(Message message);

    boolean isCommand(Message message);

    boolean isEligibleUser(Message message);

    Object getTelegramExecutableResponse(Message message);

    void afterGetFileProcess(File file, String fileName);
}
