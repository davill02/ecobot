package com.artuhanau.ecobot.commands;

import org.springframework.context.ApplicationContext;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.objects.Message;

public class ImportTelegramCommand implements TelegramCommand
{
    public ImportTelegramCommand(ApplicationContext context)
    {
    }

    @Override
    public CommandResult execute(Message message, String parameter)
    {
        CommandResult commandResult = new CommandResult(null, Status.ERROR, "");
        if (message.hasDocument()) {
            GetFile getFile = new GetFile();
            getFile.setFileId(message.getDocument().getFileId());
            commandResult.setSendAction(getFile);
            commandResult.setStatus(Status.SUCCESS);
            return commandResult;
        }
        return null;
    }

    @Override
    public void setContext(ApplicationContext context)
    {
    }
}
