package com.artuhanau.ecobot.services;

import com.artuhanau.ecobot.daos.models.User;

public interface UserManagerService {
    User getUser(Long id);
    void saveUser(User user);

    void cleanUpUserHistory(User user);
}
