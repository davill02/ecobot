package com.atruhanau.ecobot.services;

import com.atruhanau.ecobot.daos.models.User;

public interface UserManagerService {
    User getUser(Long id);
}
