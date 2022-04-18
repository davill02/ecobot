package com.artuhanau.ecobot.commands;

import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.repository.JpaRepository;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.*;
import java.util.stream.Collectors;

public class ExportTelegramCommand implements TelegramCommand
{
    private ApplicationContext context;

    private String helpMessage;

    public ExportTelegramCommand()
    {
    }

    public ExportTelegramCommand(ApplicationContext context)
    {
        this.context = context;
        helpMessage = buildHelpMessage();
    }

    @Override
    public CommandResult execute(Message message, String parameter)
    {
        CommandResult commandResult = new CommandResult(null, Status.ERROR, "");
        CommandResult helpCommandResult = getHelpCommandResult(message, parameter, commandResult);
        if (helpCommandResult != null) {
            return helpCommandResult;
        }
        if (context != null) {
            JpaRepository repository = (JpaRepository) context.getBean(parameter + "Repository");
            List entities = repository.findAll();
            if (!entities.isEmpty()) {
                return importFile(message, commandResult, entities);
            }
            commandResult.setStatus(Status.SUCCESS);
            commandResult.setSendAction(TelegramUtils.getSendMessageBuilder(message).text("Table is empty").build());
            return commandResult;
        }
        else {
            commandResult.setMessage("Import command doesn't have ApplicationContext");
            commandResult.setSendAction(TelegramUtils.getSendMessageBuilder(message).text(commandResult.getMessage()).build());
        }

        return commandResult;
    }

    private CommandResult getHelpCommandResult(Message message, String parameter, CommandResult commandResult)
    {
        if (parameter == null || parameter.trim().isEmpty()) {
            commandResult.setStatus(Status.SUCCESS);
            commandResult.setSendAction(TelegramUtils.getSendMessageBuilder(message).text(helpMessage).build());
            return commandResult;
        }
        return null;
    }

    private String buildHelpMessage()
    {
        String possibleParameters = Arrays.stream(context.getBeanDefinitionNames())
            .filter(name -> name.endsWith("Repository"))
            .map(name -> name.replace("Repository", ""))
            .collect(Collectors.joining(", "));
        return "Command #export have next possible arguments: " + possibleParameters;
    }

    private CommandResult importFile(Message message, CommandResult commandResult, List entities)
    {
        String filename = "Export_" + message.getText() + "_" + message.getMessageId() + "_" + message.getChatId() + ".csv";
        File file = new File(filename);
        try (Writer writer = new FileWriter(file)) {
            StatefulBeanToCsv beanToCsv = new StatefulBeanToCsvBuilder(writer).build();
            beanToCsv.write(entities);
        }
        catch (CsvRequiredFieldEmptyException | CsvDataTypeMismatchException | IOException e) {
            commandResult.setSendAction(TelegramUtils.getSendMessageBuilder(message).text(e.getMessage()).build());
            commandResult.setMessage(e.getMessage());
            return commandResult;
        }
        commandResult.setStatus(Status.SUCCESS);
        commandResult.setSendAction(
            SendDocument.builder().chatId(message.getChatId().toString()).document(new InputFile(file)).caption("Result").build());
        return commandResult;
    }

    public ApplicationContext getContext()
    {
        return context;
    }

    public void setContext(ApplicationContext context)
    {
        this.context = context;
    }
}
