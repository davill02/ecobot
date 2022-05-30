package com.artuhanau.ecobot.commands;

import com.artuhanau.ecobot.daos.models.City;
import com.artuhanau.ecobot.daos.models.EducationalOrganisation;
import com.artuhanau.ecobot.daos.models.SavingService;
import com.artuhanau.ecobot.daos.models.TrainingFormat;
import com.artuhanau.ecobot.daos.models.repos.UserRepository;
import com.opencsv.bean.CsvToBeanBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;
import java.util.function.Supplier;

@Component
public class DefaultCommandService implements CommandService
{
    private static final Logger LOG = LoggerFactory.getLogger(DefaultCommandService.class);

    private Map<String, Supplier<TelegramCommand>> stringToCommand;

    @Value("${bot.telegram.feed.users}")
    private String eligibleUsers;

    @Resource
    private ApplicationContext context;

    @Resource
    private UserRepository userRepository;

    @Resource
    private SavingService savingService;

    @PostConstruct
    public void initialize()
    {
        stringToCommand = new HashMap<>();
        stringToCommand.put("export", () -> new ExportTelegramCommand(context));
        stringToCommand.put("import", () -> new ImportTelegramCommand(context));
    }

    @Override
    public CommandResult parseCommandAndExecute(Message message)
    {
        CommandResult commandResult = new CommandResult(TelegramUtils.getSendMessageBuilder(message).text("Can't parse command"),
            Status.ERROR, "Can't parse command");
        Supplier<TelegramCommand> command = getCommand(message);
        if (command != null) {
            commandResult = command.get().execute(message, getParameter(message));
        }
        return commandResult;
    }

    @Override
    public boolean isCommand(Message message)
    {
        String commandName = extractCommandName(message);
        return stringToCommand.containsKey(commandName);
    }

    private String extractCommandName(Message message)
    {
        String commandName = "";
        String[] args = getArgs(message);
        if (args.length > 0 && args[0].length() > 1) {
            return args[0].substring(1);
        }
        return commandName;
    }

    private String getParameter(Message message)
    {
        String[] args = getArgs(message);
        if (args.length > 1) {
            return args[1];
        }
        else {
            return "";
        }
    }

    private String[] getArgs(Message message)
    {
        String commandLine = Optional.ofNullable(message.getText()).orElse(message.getCaption());
        if (commandLine != null && commandLine.startsWith("#")) {
            return commandLine.split(" ");
        }
        return new String[0];
    }

    private Supplier<TelegramCommand> getCommand(Message message)
    {
        return stringToCommand.get(extractCommandName(message));
    }

    @Override
    public boolean isEligibleUser(Message message)
    {
        return eligibleUsers.contains(message.getFrom().getId().toString());
    }

    @Override
    public Object getTelegramExecutableResponse(Message message)
    {
        CommandResult commandResult = this.parseCommandAndExecute(message);
        if (commandResult.getStatus() == Status.ERROR) {
            LOG.error(commandResult.getMessage() + " user: " + message.getFrom().getId());
        }
        else {
            LOG.info(commandResult.getMessage());
        }
        return commandResult.getSendAction();
    }

    @Override
    public void afterGetFileProcess(File file, String fileName)
    {
        try {
            if (fileName.toLowerCase(Locale.ROOT).contains("user")) {
                List<com.artuhanau.ecobot.daos.models.User> users = getParse(file, com.artuhanau.ecobot.daos.models.User.class);
                userRepository.saveAll(users);
            }
            if (fileName.toLowerCase(Locale.ROOT).contains("city")) {
                List<City> cities = getParse(file, City.class);
                cities.forEach(city -> savingService.saveCity(city));
            }
            if (fileName.toLowerCase(Locale.ROOT).contains("organisation")) {
                List<EducationalOrganisation> organisations = getParse(file, EducationalOrganisation.class);
                organisations.forEach(organisation -> savingService.saveOrganisation(organisation));
            }
            if (fileName.toLowerCase(Locale.ROOT).contains("trainingformat")) {
                List<TrainingFormat> trainingFormats = getParse(file, TrainingFormat.class);
                trainingFormats.forEach(trainingFormat -> savingService.saveTrainingFormat(trainingFormat));
            }
            LOG.info("Update is ended");
        }
        catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private List getParse(File file, Class<?> clazz) throws FileNotFoundException
    {
        return new CsvToBeanBuilder(new FileReader(file)).withType(clazz).build().parse();
    }
}
