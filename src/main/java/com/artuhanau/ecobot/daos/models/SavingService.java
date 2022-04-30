package com.artuhanau.ecobot.daos.models;

import com.artuhanau.ecobot.daos.models.City;
import com.artuhanau.ecobot.daos.models.EducationalOrganisation;
import com.artuhanau.ecobot.daos.models.TrainingFormat;

public interface SavingService
{
    void saveOrganisation(EducationalOrganisation organisation);

    void saveTrainingFormat(TrainingFormat trainingFormat);

    void saveCity(City city);
}
