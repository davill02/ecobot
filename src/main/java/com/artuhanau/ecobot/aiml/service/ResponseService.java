package com.artuhanau.ecobot.aiml.service;

import com.artuhanau.ecobot.daos.models.DialogCommand;
import com.artuhanau.ecobot.daos.models.User;
import com.artuhanau.ecobot.daos.models.repos.DialogCommandRepository;
import com.artuhanau.ecobot.services.UserManagerService;
import org.alicebot.ab.Bot;
import org.alicebot.ab.Chat;
import org.alicebot.ab.configuration.BotConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.naming.ldap.PagedResultsControl;
import java.nio.file.Paths;

@Component
public class ResponseService {
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
    public void init() {
        BotConfiguration botConfigs = BotConfiguration.builder().maxHistory(10).path(Paths.get("src/main/resources/").toAbsolutePath().toString()).name("aimlbot").build();
        bot = new Bot(botConfigs);
        unknownCommand = dialogCommandRepository.getFirstByCommand(UNKNOWN_COMMAND).orElseThrow(() -> new IllegalArgumentException(UNKNOWN_COMMAND + " is not found"));
    }

    public String createResponse(String responseCommand, User user) {
        Chat chat = new Chat(bot);
        user.getHistory().add(dialogCommandRepository.getFirstByCommand(responseCommand).orElse(unknownCommand));
        if (user.getHistory().size() > maxUserHistory) {
            user.getHistory().remove(0);
        }
        userManagerService.saveUser(user);
        String response = chat.multisentenceRespond(responseCommand);
        LOGGER.info("{}: {}", user.getTelegramId(), response);
        return response;
    }
}
