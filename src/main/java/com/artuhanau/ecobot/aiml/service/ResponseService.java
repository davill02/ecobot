package com.artuhanau.ecobot.aiml.service;

import com.artuhanau.ecobot.daos.models.DialogCommand;
import com.artuhanau.ecobot.daos.models.TrainingFormat;
import com.artuhanau.ecobot.daos.models.User;
import com.artuhanau.ecobot.daos.models.repos.DialogCommandRepository;
import com.artuhanau.ecobot.services.UserManagerService;
import org.alicebot.ab.Bot;
import org.alicebot.ab.Chat;
import org.alicebot.ab.configuration.BotConfiguration;
import org.checkerframework.checker.units.qual.C;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.naming.ldap.PagedResultsControl;
import java.nio.file.Paths;
import java.util.List;

@Component
public class ResponseService
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ResponseService.class);

    private static final String UNKNOWN_COMMAND = "UNKNOWN";

    @Resource
    private UserManagerService userManagerService;

    @Resource
    private DialogCommandRepository dialogCommandRepository;

    @Value("${max.user.history.size}")
    private Integer maxUserHistory;

    private DialogCommand unknownCommand;

    private Bot bot;

    @PostConstruct
    public void init()
    {
        BotConfiguration botConfigs = BotConfiguration.builder().maxHistory(10)
            .path(Paths.get("src/main/resources/").toAbsolutePath().toString()).name("aimlbot").build();
        bot = new Bot(botConfigs);
        unknownCommand = dialogCommandRepository.getFirstByCommand(UNKNOWN_COMMAND)
            .orElseThrow(() -> new IllegalArgumentException(UNKNOWN_COMMAND + " is not found"));
    }

    public String createResponse(User user, String info, String command)
    {
        Chat chat = new Chat(bot);
        String responseCommand = "";
        if("NEW_JOIN".equals(info)){
            responseCommand = "INTRODUCTION";
        }
        if(responseCommand.isEmpty()){
            responseCommand = command;
        }
        user.getHistory().add(dialogCommandRepository.getFirstByCommand(responseCommand).orElse(unknownCommand));
        if (user.getHistory().size() > maxUserHistory) {
            user.getHistory().remove(0);
        }
        userManagerService.saveUser(user);
        String response = chat.multisentenceRespond(responseCommand);
        LOGGER.info("{}: {}", user.getTelegramId(), response);
        return response.replace("I have no answer for that.","");
    }

    public SendMessage createResultResponse(List<TrainingFormat> trainingFormatList)
    {
        Chat chat = new Chat(bot);
        String title = chat.multisentenceRespond("RESULT");
        StringBuffer stringBuffer = new StringBuffer(title);
        for (int i = 0; i < trainingFormatList.size(); i++) {
            TrainingFormat trainingFormat = trainingFormatList.get(i);
            stringBuffer.append("\n ").append(i + 1).append(".")
                .append(trainingFormat.getDescription()).append("\n")
                .append(trainingFormat.getNote()).append("\n");
            printLine(stringBuffer, "Организация:", trainingFormat.getOrganisation().getName());
            printLine(stringBuffer, "Адрес:", trainingFormat.getOrganisation().getName());
            printLine(stringBuffer, "Сайт:", trainingFormat.getOrganisation().getSite());
        }
        return SendMessage.builder().text(stringBuffer.toString()).build();
    }

    private StringBuffer printLine(StringBuffer stringBuffer, String textTitle, Object info)
    {
        if (info == null) {
            return stringBuffer;
        }
        return stringBuffer.append(textTitle).append(info).append("\n");
    }
}

