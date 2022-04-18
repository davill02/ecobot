package com.artuhanau.ecobot.services.standard;

import java.util.List;
import javax.annotation.Resource;

import com.artuhanau.ecobot.daos.models.EducationalOrganisation;
import com.artuhanau.ecobot.daos.models.TrainingFormat;
import com.artuhanau.ecobot.daos.models.UserData;
import com.artuhanau.ecobot.daos.models.enums.EducationStep;
import com.artuhanau.ecobot.daos.models.repos.CityRepository;
import com.artuhanau.ecobot.daos.models.repos.TrainingFormatRepository;
import com.artuhanau.ecobot.services.SearchService;

public class DefaultSearchService implements SearchService
{
    @Resource
    private CityRepository cityRepository;

    @Resource
    private TrainingFormatRepository trainingFormatRepository;

    @Resource
    private EducationalOrganisation educationalOrganisation;

    @Override
    public List<TrainingFormat> search(final UserData userData)
    {
        prepareData(userData);

        return null;
    }

    private void prepareData(final UserData userData)
    {
        if (userData.getCityName() != null) {
            findSimilarCity(userData);
        }
        if (userData.getEducation() != null) {
            mapEducationToEducationStep(userData);
        }
    }

    private void mapEducationToEducationStep(final UserData userData)
    {
        userData.setEducationStep(EducationStep.OTHER);
    }

    private void findSimilarCity(UserData userData)
    {
        userData.setCityName(cityRepository.findMostSimilarCity(userData.getCityName()).getName());
    }
}
