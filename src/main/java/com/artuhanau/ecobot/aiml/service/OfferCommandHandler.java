package com.artuhanau.ecobot.aiml.service;

import java.util.List;
import javax.annotation.Resource;

import com.artuhanau.ecobot.daos.models.User;
import com.artuhanau.ecobot.services.EntitiesAndRelationsService;
import com.artuhanau.ecobot.yandex.translate.TranslationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

@Component
public class OfferCommandHandler implements TelegramCommandHandler
{
    public static final String OFFER = "OFFER";

    public static final String CONTINUE = "CONTINUE";

    @Value("${chat.id.for.offers}")
    private Long chatId;

    @Resource
    private EntitiesAndRelationsService entitiesAndRelationsService;

    @Resource
    private TranslationService translationService;

    @Override
    public boolean shouldHandle(final String command, final User user)
    {
        return isEquals(command) || isOffer(user) || isContinue(user);
    }

    private boolean isEquals(final String command)
    {
        return "/offer".equals(command);
    }

    private boolean isOffer(final User user)
    {
        return !user.getHistory().isEmpty() && user.getHistory().get(user.getHistory().size() - 1).getCommand().getCommand().equals(OFFER);
    }

    private boolean isContinue(final User user)
    {
        return !user.getHistory().isEmpty() && user.getHistory().get(user.getHistory().size() - 1).getCommand().getCommand()
            .equals(CONTINUE);
    }

    @Override
    public void handle(final User user, final String command, final List<String> responseCommands, List<SendMessage> sendMessageList)
    {
        if (isEquals(command)) {
            responseCommands.add(OFFER);
        }
        if (isOffer(user)) {
            sendMessageList.add(SendMessage.builder().chatId(String.valueOf(chatId)).text(command).build());
            responseCommands.add("THANKS FOR OFFER");
            responseCommands.add(CONTINUE);
        }
        if (isContinue(user) && !entitiesAndRelationsService.findRefusalOrAgreement(translationService.translateToEnglish(command))) {
            responseCommands.add("CONTINUE_FALSE");
        }
    }
}
