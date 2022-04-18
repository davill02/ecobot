package com.artuhanau.ecobot.services.standard;

import com.artuhanau.ecobot.aiml.service.ResponseService;
import com.artuhanau.ecobot.daos.models.DialogCommand;
import com.artuhanau.ecobot.services.EntitiesAndRelationsService;
import com.artuhanau.ecobot.yandex.translate.TranslationService;
import com.artuhanau.ecobot.daos.models.User;
import com.artuhanau.ecobot.services.MessageService;
import com.artuhanau.ecobot.services.UserManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import javax.annotation.Resource;
import java.util.stream.Collectors;

@Component("messageService")
public class DefaultMessageService implements MessageService
{
    private final static Logger LOG = LoggerFactory.getLogger(DefaultMessageService.class);

    @Resource
    EntitiesAndRelationsService entitiesAndRelationsService;

    @Resource
    private ResponseService responseService;

    @Resource
    private UserManagerService userManagerService;

    @Resource
    private TranslationService translationService;

    @Value("${bot.telegram.feed.users}")
    private String feedUsers;

    @Override
    public Object createResponse(Message message)
    {
        User user = userManagerService.getUser(message.getFrom().getId());
        if (feedUsers.contains(user.getTelegramId().toString()) && message.hasDocument()) {
            if (message.getDocument().getMimeType().equals("text/csv")) {
                LOG.info("YEAP" + message.getDocument());
            }
            return SendMessage.builder().chatId(user.getTelegramId().toString()).text("Successful update").build();
        }
        else {
            if (user.getName() == null) {
                user.setName(message.getFrom().getFirstName());
            }
            if (message.getText().startsWith("SHOW HISTORY")) {
                return SendMessage.builder()
                    .chatId(String.valueOf(message.getChatId()))
                    .text(user.getHistory().stream().map(DialogCommand::getCommand).collect(Collectors.joining("\n"))).build();
            }
            String messageText = String.join(" ",
                entitiesAndRelationsService.getEntities(translationService.translateToEnglish(message.getText())));
            return SendMessage.builder()
                .chatId(String.valueOf(message.getChatId()))
                .text(messageText).build();
        }
    }
}
