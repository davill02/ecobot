package com.artuhanau.ecobot.services.standard;

import java.sql.Timestamp;
import java.util.ArrayList;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import com.artuhanau.ecobot.daos.models.DialogCommand;
import com.artuhanau.ecobot.daos.models.DialogCommandHistoryEntry;
import com.artuhanau.ecobot.daos.models.User;
import com.artuhanau.ecobot.daos.models.UserData;
import com.artuhanau.ecobot.daos.models.repos.DialogCommandHistoryEntryRepository;
import com.artuhanau.ecobot.daos.models.repos.DialogCommandRepository;
import com.artuhanau.ecobot.daos.models.repos.UserRepository;
import com.artuhanau.ecobot.services.UserManagerService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component("userManagerService")
public class DefaultUserManagerService implements UserManagerService
{
    private static final String UNKNOWN_COMMAND = "UNKNOWN";

    @Resource
    private DialogCommandRepository dialogCommandRepository;

    @Resource
    private DialogCommandHistoryEntryRepository commandHistoryEntryRepository;


    @Value("${max.user.history.size}")
    private Integer maxUserHistory;

    @Resource
    private UserRepository userDao;

    private DialogCommand unknownCommand;

    @Override
    public User getUser(Long id)
    {
        return userDao.findById(id).orElseGet(() -> createUser(id));
    }

    @Override
    public void saveUser(User user)
    {
        userDao.saveAndFlush(user);
    }

    @Override
    public void cleanUserData(final User user)
    {
        user.setUserData(new UserData());
        saveUser(user);
    }

    @Override
    public void cleanUpUserHistory(final User user)
    {
        user.setHistory(new ArrayList<>());
        saveUser(user);
        commandHistoryEntryRepository.deleteAll(user.getHistory());
    }

    @Override
    public void saveUserHistory(final String command, final User user)
    {
        DialogCommandHistoryEntry dialogCommandHistoryEntry = new DialogCommandHistoryEntry(null,
            dialogCommandRepository.getFirstByCommand(command).orElse(unknownCommand), new Timestamp(System.currentTimeMillis()));
        commandHistoryEntryRepository.saveAndFlush(dialogCommandHistoryEntry);
        user.getHistory().add(dialogCommandHistoryEntry);
        if (user.getHistory().size() > maxUserHistory) {
            user.getHistory().remove(0);
            saveUser(user);
            commandHistoryEntryRepository.delete(user.getHistory().get(user.getHistory().size() - 1));
        }
        saveUser(user);
    }

    private User createUser(Long id)
    {
        User user = new User();
        user.setTelegramId(id);
        userDao.save(user);
        return user;
    }

    @PostConstruct
    private void init()
    {
        unknownCommand = dialogCommandRepository.getFirstByCommand(UNKNOWN_COMMAND)
            .orElseThrow(() -> new IllegalArgumentException(UNKNOWN_COMMAND + " is not found"));
    }
}
