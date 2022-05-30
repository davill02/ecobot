package com.artuhanau.ecobot.services;

import com.artuhanau.ecobot.daos.models.User;
import com.artuhanau.ecobot.daos.models.UserData;
import edu.stanford.nlp.pipeline.CoreDocument;

public interface EntitiesAndRelationsService
{
    String getEntities(String englishText);

    UserData fillEntities(User user, String userText);

    boolean fillEducation(User user, String russianMessage);

    boolean findRefusalOrAgreement(final String userText, final CoreDocument coreDocument);
    boolean findRefusalOrAgreement(final String userText);
}
