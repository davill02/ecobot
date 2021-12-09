package com.atruhanau.ecobot.services.standard;

import com.atruhanau.ecobot.daos.models.User;
import com.atruhanau.ecobot.daos.models.UserRepo;
import com.atruhanau.ecobot.services.UserManagerService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component("userManagerService")
public class DefaultUserManagerService implements UserManagerService {
    @Resource
    private UserRepo userDao;

    @Override
    public User getUser(Long id) {
        return userDao.findById(id).orElseGet(() -> createUser(id));
    }

    private User createUser(Long id) {
        User user = new User();
        user.setTelegramId(id);
        userDao.save(user);
        return user;
    }
}
