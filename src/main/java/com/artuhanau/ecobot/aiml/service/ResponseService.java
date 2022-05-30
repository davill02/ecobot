package com.artuhanau.ecobot.aiml.service;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import com.artuhanau.ecobot.daos.models.DialogCommand;
import com.artuhanau.ecobot.daos.models.DialogCommandHistoryEntry;
import com.artuhanau.ecobot.daos.models.TrainingFormat;
import com.artuhanau.ecobot.daos.models.User;
import com.artuhanau.ecobot.daos.models.repos.DialogCommandRepository;
import com.artuhanau.ecobot.services.UserManagerService;
import com.google.common.collect.Maps;
import org.alicebot.ab.Bot;
import org.alicebot.ab.Chat;
import org.alicebot.ab.configuration.BotConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

@Component
public class ResponseService
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ResponseService.class);

    public static final String SCHOOL = "I go to school";

    public static final String UNIVERSITY = "I am studying at a university";

    public static final String HIGH_EDUCATION = "I have a higher or vocational education";

    @Resource
    private UserManagerService userManagerService;

    @Resource
    private DialogCommandRepository dialogCommandRepository;

    private Bot bot;

    private Map<String, Long> countableCommandsAndMaxCount;

    private List<String> questionCommands;

    @Resource
    private List<TelegramCommandHandler> telegramCommandHandlers;

    private ReplyKeyboardMarkup replyKeyboardMarkupForEducation;

    @Value("${question.commands}")
    private String questions;

    @PostConstruct
    public void init()
    {
        createEducationalKeyboard();
        countableCommandsAndMaxCount = new HashMap<>();
        countableCommandsAndMaxCount.put("COST", 2L);
        countableCommandsAndMaxCount.put("LOCATION", 3L);
        questionCommands = Arrays.asList(StringUtils.splitPreserveAllTokens(questions, ','));
        BotConfiguration botConfigs = BotConfiguration.builder().maxHistory(10)
            .path(Paths.get("src/main/resources/").toAbsolutePath().toString()).name("aimlbot").build();
        bot = new Bot(botConfigs);
    }

    private void createEducationalKeyboard()
    {
        List<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardRow keyboardButtons = new KeyboardRow();
        keyboardButtons.add("Я учусь в школе");
        keyboardButtons.add("Я учусь в университете");
        keyboardButtons.add("У меня есть высшее или профессиональное образование");
        keyboardRows.add(keyboardButtons);
        replyKeyboardMarkupForEducation = ReplyKeyboardMarkup.builder().oneTimeKeyboard(true).keyboard(keyboardRows).selective(true)
            .build();
    }

    public List<Object> createResponse(User user, String info, String command, Long chatId)
    {
        Chat chat = new Chat(bot);
        List<String> responseCommands = new ArrayList<>();
        List<SendMessage> additionalSendMessages = new ArrayList<>();
        telegramCommandHandlers.forEach(handler -> {
                if (handler.shouldHandle(command, user)) {
                    handler.handle(user, command, responseCommands, additionalSendMessages);
                }
            }
        );

        if (responseCommands.isEmpty()) {
            responseCommands.add(getRandomDialogCommand(user));
        }

        userManagerService.saveUser(user);
        List<Object> sendMessages = responseCommands.stream()
            .peek(saveHistoryEntry(user))
            .map(s -> Maps.immutableEntry(s, chat.multisentenceRespond(s)))
            .peek(s -> LOGGER.info("{}: {} chatID: {}", user.getTelegramId(), s.getValue(), chatId))
            .map(s -> getSendMessage(chatId, s))
            .collect(Collectors.toList());
        sendMessages.addAll(additionalSendMessages);
        return sendMessages;
    }

    private SendMessage getSendMessage(final Long chatId, final Map.Entry<String, String> s)
    {
        SendMessage.SendMessageBuilder sendMessageBuilder = SendMessage.builder().chatId(String.valueOf(chatId)).text(s.getValue());
        if ("EDUCATION".equals(s.getKey())) {
            sendMessageBuilder.replyMarkup(replyKeyboardMarkupForEducation);
        }
        return sendMessageBuilder.build();
    }

    private Consumer<String> saveHistoryEntry(final User user)
    {
        return s -> {
            String commandName = StringUtils.splitByWholeSeparatorPreserveAllTokens(s, " ")[0];
            LOGGER.info("{}: COMMAND {}", user.getTelegramId(), s);

            userManagerService.saveUserHistory(commandName, user);
        };
    }

    public SendMessage createResultResponse(List<TrainingFormat> trainingFormatList, Long chatId)
    {
        Chat chat = new Chat(bot);
        String title = chat.multisentenceRespond("RESULT");
        StringBuffer stringBuffer = new StringBuffer(title);
        for (int i = 0; i < trainingFormatList.size(); i++) {
            TrainingFormat trainingFormat = trainingFormatList.get(i);
            stringBuffer.append("\n\t").append(i + 1).append(".").append("\t").append(trainingFormat.getFormatName()).append("\n")
                .append("\t").append(trainingFormat.getFormatKeyWords()).append("\n");
            printLine(stringBuffer, "\tОрганизация:  ", trainingFormat.getOrganisation().getName());
            printLine(stringBuffer, "\tАдрес:  ", trainingFormat.getOrganisation().getName());
            printLine(stringBuffer, "\tСайт:  ", trainingFormat.getOrganisation().getSite());
        }
        return SendMessage.builder().text(stringBuffer.toString()).chatId(String.valueOf(chatId)).build();
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
            .map(DialogCommandHistoryEntry::getCommand)
            .map(DialogCommand::getCommand)
            .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
            .entrySet()
            .stream()
            .filter(commandAndCount -> {
                Long maxCount = countableCommandsAndMaxCount.get(commandAndCount.getKey());
                if (maxCount == null) {
                    return true;
                }
                return maxCount >= commandAndCount.getValue();
            })
            .map(Map.Entry::getKey)
            .filter(command -> ifCommandResultIsNotEmpty(user.getUserData().getCityName(), command, "LOCATION"))
            .filter(command -> ifCommandResultIsNotEmpty(user.getUserData().getPaid(), command, "COST"))
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
            long count = user.getHistory().stream()
                .map(DialogCommandHistoryEntry::getCommand)
                .map(DialogCommand::getCommand)
                .filter(s -> s.equals(finalRandomCommand))
                .count();
            randomCommand = String.format("%s %d @", randomCommand, count + 1);
        }
        return randomCommand;
    }

    private boolean ifCommandResultIsNotEmpty(Object field, final String command, String commandName)
    {
        if (commandName.equals(command)) {
            return StringUtils.isNotBlank(String.valueOf(field));
        }
        return true;
    }
}

