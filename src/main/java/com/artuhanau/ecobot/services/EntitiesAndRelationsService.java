package com.artuhanau.ecobot.services;

import java.util.List;

import com.artuhanau.ecobot.daos.models.User;
import com.artuhanau.ecobot.daos.models.UserData;

public interface EntitiesAndRelationsService
{
    String getEntities(String englishText);

    void fillEntities(User user, String userText);
}
