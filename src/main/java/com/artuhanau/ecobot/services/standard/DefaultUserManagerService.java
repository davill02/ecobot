package com.artuhanau.ecobot.services.standard;

import java.util.ArrayList;

import com.artuhanau.ecobot.daos.models.User;
import com.artuhanau.ecobot.daos.models.repos.UserRepository;
import com.artuhanau.ecobot.services.UserManagerService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component("userManagerService")
public class DefaultUserManagerService implements UserManagerService
{
    @Resource
    private UserRepository userDao;

    @Override
    public User getUser(Long id)
    {
        return userDao.findById(id).orElseGet(() -> createUser(id));
    }

    @Override
    public void saveUser(User user)
    {
        userDao.save(user);
    }

    @Override
    public void cleanUpUserHistory(final User user)
    {
        user.setHistory(new ArrayList<>());
        saveUser(user);
    }

    private User createUser(Long id)
    {
        User user = new User();
        user.setTelegramId(id);
        userDao.save(user);
        return user;
    }
}
