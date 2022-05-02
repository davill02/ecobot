package com.artuhanau.ecobot.aiml.service;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import com.artuhanau.ecobot.daos.models.DialogCommand;
import com.artuhanau.ecobot.daos.models.TrainingFormat;
import com.artuhanau.ecobot.daos.models.User;
import com.artuhanau.ecobot.daos.models.repos.DialogCommandRepository;
import com.artuhanau.ecobot.services.UserManagerService;
import org.alicebot.ab.Bot;
import org.alicebot.ab.Chat;
import org.alicebot.ab.configuration.BotConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

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

    private Map<String, Long> countableCommandsAndMaxCount;

    private List<String> questionCommands;

    @Value("${question.commands}")
    private String questions;

    @PostConstruct
    public void init()
    {
        countableCommandsAndMaxCount = new HashMap<>();
        countableCommandsAndMaxCount.put("COST", 2L);
        countableCommandsAndMaxCount.put("LOCATION", 3L);
        questionCommands = Arrays.asList(StringUtils.splitPreserveAllTokens(questions, ','));
        BotConfiguration botConfigs = BotConfiguration.builder().maxHistory(10)
            .path(Paths.get("src/main/resources/").toAbsolutePath().toString()).name("aimlbot").build();
        bot = new Bot(botConfigs);
        unknownCommand = dialogCommandRepository.getFirstByCommand(UNKNOWN_COMMAND)
            .orElseThrow(() -> new IllegalArgumentException(UNKNOWN_COMMAND + " is not found"));
    }

    public List<Object> createResponse(User user, String info, String command, Long chatId)
    {
        Chat chat = new Chat(bot);
        List<String> responseCommands = new ArrayList<>();
        if (command.equals("/start")) {
            responseCommands.add("INTRODUCTION");
            responseCommands.add("AIM");
            userManagerService.cleanUpUserHistory(user);
            responseCommands.add(getRandomDialogCommand(user));
        }
        if (responseCommands.isEmpty()) {
            responseCommands.add(getRandomDialogCommand(user));
        }
        userManagerService.saveUser(user);
        return responseCommands.stream()
            .peek(s -> {
                String commandName = StringUtils.splitByWholeSeparatorPreserveAllTokens(s, " ")[0];
                LOGGER.info("{}: COMMAND {}", user.getTelegramId(), s);
                user.getHistory().add(dialogCommandRepository.getFirstByCommand(commandName).orElse(unknownCommand));
                if (user.getHistory().size() > maxUserHistory) {
                    user.getHistory().remove(0);
                }
                userManagerService.saveUser(user);
            })
            .map(chat::multisentenceRespond)
            .peek(s -> LOGGER.info("{}: {}", user.getTelegramId(), s))
            .map(s -> SendMessage.builder().chatId(String.valueOf(chatId)).text(s).build())
            .collect(Collectors.toList());
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

    public String getRandomDialogCommand(User user)
    {
        List<String> commandsToExclude = user.getHistory().stream()
            .map(DialogCommand::getCommand)
            .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
            .entrySet()
            .stream()
            .filter(commandAndCount -> {
                Long maxCount = countableCommandsAndMaxCount.get(commandAndCount.getKey());
                if (maxCount == null) {
                    return true;
                }
                return maxCount <= commandAndCount.getValue();
            })
            .map(Map.Entry::getKey)
            .filter(command -> !("LOCATION".equals(command) && user.getUserData().getCityName() != null))
            .filter(command -> !("COST".equals(command) && user.getUserData().getPaid() != null))
            .collect(Collectors.toList());
        List<String> relevantCommands = new ArrayList<>(questionCommands);
        relevantCommands.removeAll(commandsToExclude);
        String randomCommand = "RESULT";
        if (!relevantCommands.isEmpty()) {
            Random rand = new Random();
            int random = rand.nextInt(relevantCommands.size());
            randomCommand = relevantCommands.get(random);
        }
        if (countableCommandsAndMaxCount.get(randomCommand) != null) {
            final String finalRandomCommand = randomCommand;
            long count = user.getHistory().stream().map(DialogCommand::getCommand)
                .filter(s -> s.equals(finalRandomCommand))
                .count();
            randomCommand = String.format("%s %d @", randomCommand, count + 1);
        }
        return randomCommand;
    }
}

