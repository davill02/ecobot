package com.artuhanau.ecobot.services;

import java.util.List;

import com.artuhanau.ecobot.daos.models.DialogCommand;
import com.artuhanau.ecobot.daos.models.TrainingFormat;
import com.artuhanau.ecobot.daos.models.UserData;

public interface SearchService
{
    List<TrainingFormat> search(UserData userData);

    boolean isEligibleForSearch(UserData userData, List<DialogCommand> history);

}
