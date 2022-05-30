package com.artuhanau.ecobot.aiml.service;

import java.util.List;
import javax.annotation.Resource;

import com.artuhanau.ecobot.daos.models.User;
import com.artuhanau.ecobot.services.UserManagerService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

@Component
public class StartCommandHandler implements TelegramCommandHandler
{
    public static final String START = "/start";

    @Resource
    private UserManagerService userManagerService;

    @Override
    public boolean shouldHandle(final String command, final User user)
    {
        return START.equals(command);
    }

    @Override
    public void handle(final User user, final String command, final List<String> responseCommands, List<SendMessage> sendMessageList)
    {
        responseCommands.add("INTRODUCTION");
        responseCommands.add("AIM");
        user.setStep(null);
        userManagerService.cleanUserData(user);
        userManagerService.cleanUpUserHistory(user);
        responseCommands.add("FAMILIARITY");
    }
}
