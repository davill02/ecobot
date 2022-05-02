package com.artuhanau.ecobot.services.standard;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Resource;

import com.artuhanau.ecobot.aiml.service.ResponseService;
import com.artuhanau.ecobot.daos.models.TrainingFormat;
import com.artuhanau.ecobot.daos.models.User;
import com.artuhanau.ecobot.daos.models.UserData;
import com.artuhanau.ecobot.services.EntitiesAndRelationsService;
import com.artuhanau.ecobot.services.MessageService;
import com.artuhanau.ecobot.services.SearchService;
import com.artuhanau.ecobot.services.UserManagerService;
import com.artuhanau.ecobot.yandex.translate.TranslationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component("messageService")
public class DefaultMessageService implements MessageService
{
    private final static Logger LOG = LoggerFactory.getLogger(DefaultMessageService.class);

    @Resource
    private SearchService searchService;

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
    public List<Object> createResponse(Message message)
    {
        User user = getUserWithUserData(message);
        if (feedUsers.contains(user.getTelegramId().toString()) && message.hasDocument()) {
            if (message.getDocument().getMimeType().equals("text/csv")) {
                LOG.info("YEAP" + message.getDocument());
            }
            return wrapSendMessage(SendMessage.builder().chatId(user.getTelegramId().toString()).text("Successful update").build());
        }
        else {
            UserData userData = user.getUserData();
            entitiesAndRelationsService.fillEntities(user, translationService.translateToEnglish(message.getText()));
            if (searchService.isEligibleForSearch(userData, user.getHistory())) {
                List<TrainingFormat> trainingFormatList = searchService.search(userData);
                return wrapSendMessage(responseService.createResultResponse(trainingFormatList));
            }
            return responseService.createResponse(user, "", message.getText(), message.getChatId());
        }
    }

    @Override
    public List<Object> wrapSendMessage(final SendMessage sendMessage)
    {
        List<Object> responses = new ArrayList<>();
        responses.add(sendMessage);
        return responses;
    }

    private User getUserWithUserData(final Message message)
    {
        User user = userManagerService.getUser(message.getFrom().getId());
        if (user.getUserData() == null) {
            UserData userData = new UserData();
            user.setUserData(userData);
            userManagerService.saveUser(user);
        }
        if (user.getName() == null) {
            user.setName(message.getFrom().getFirstName());
        }
        return user;
    }
}
