package com.atruhanau.ecobot.services.standard;

import com.atruhanau.ecobot.daos.models.User;
import com.atruhanau.ecobot.services.MessageService;
import com.atruhanau.ecobot.services.UserManagerService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import javax.annotation.Resource;

@Component("messageService")
public class DefaultMessageService implements MessageService {
    @Resource
    private UserManagerService userManagerService;

    @Override
    public SendMessage createResponse(Message message) {
        User user = userManagerService.getUser(message.getFrom().getId());
        if (user.getName() == null) {
            user.setName(message.getFrom().getFirstName());
        }
        return SendMessage.builder().chatId(String.valueOf(message.getChatId()))
                .text(message.getFrom().getFirstName() + " " + message.getFrom().getId()).build();
    }
}
