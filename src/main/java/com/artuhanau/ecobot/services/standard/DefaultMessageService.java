package com.artuhanau.ecobot.services.standard;

import com.artuhanau.ecobot.aiml.service.ResponseService;
import com.artuhanau.ecobot.daos.models.DialogCommand;
import com.artuhanau.ecobot.daos.models.TrainingFormat;
import com.artuhanau.ecobot.daos.models.UserData;
import com.artuhanau.ecobot.daos.models.enums.EducationStep;
import com.artuhanau.ecobot.services.EntitiesAndRelationsService;
import com.artuhanau.ecobot.services.SearchService;
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
import java.util.List;

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
    public Object createResponse(Message message)
    {
        User user = getUserWithUserData(message);
        if (feedUsers.contains(user.getTelegramId().toString()) && message.hasDocument()) {
            if (message.getDocument().getMimeType().equals("text/csv")) {
                LOG.info("YEAP" + message.getDocument());
            }
            return SendMessage.builder().chatId(user.getTelegramId().toString()).text("Successful update").build();
        }
        else {
            UserData userData = user.getUserData();
            String partOfResponse = entitiesAndRelationsService.getEntities(translationService.translateToEnglish(message.getText()));
            entitiesAndRelationsService.fillEntities(user, translationService.translateToEnglish(message.getText()));
            if (searchService.isEligibleForSearch(userData, user.getHistory())) {
                List<TrainingFormat> trainingFormatList = searchService.search(userData);
                responseService.createResultResponse(trainingFormatList);
            }
            if (partOfResponse.isEmpty()) {
                return SendMessage.builder().text(responseService.createResponse(user, "", message.getText())).chatId(
                    String.valueOf(message.getChatId())).build();
            } else {
                return SendMessage.builder().chatId(
                    String.valueOf(message.getChatId())).text(responseService.createResponse(user, "", message.getText()) + " Found entries: " + partOfResponse ).build();
            }
        }
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
