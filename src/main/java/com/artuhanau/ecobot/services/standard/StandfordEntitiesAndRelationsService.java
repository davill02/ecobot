package com.artuhanau.ecobot.services.standard;

import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;

import com.artuhanau.ecobot.daos.models.User;
import com.artuhanau.ecobot.daos.models.UserData;
import com.artuhanau.ecobot.services.EntitiesAndRelationsService;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import org.springframework.stereotype.Component;

@Component
public class StandfordEntitiesAndRelationsService implements EntitiesAndRelationsService
{
    private StanfordCoreNLP stanfordCoreNLP;

    private Properties properties;

    @PostConstruct
    public void initialize()
    {
        properties = new Properties();
        properties.getProperty("annotators", "tokenize,ssplit,pos,lemma,ner");
        stanfordCoreNLP = new StanfordCoreNLP(properties);
    }

    @Override
    public String getEntities(final String englishText)
    {
        CoreDocument coreDocument = new CoreDocument(englishText);
        stanfordCoreNLP.annotate(coreDocument);
        return  coreDocument.entityMentions().stream().map(entity -> entity.entityType() + " " + entity.text()).collect(Collectors.joining(","));
    }

    @Override
    public void fillEntities(final User user, String userText)
    {
        UserData userData = user.getUserData();
        CoreDocument coreDocument = new CoreDocument(userText);
        stanfordCoreNLP.annotate(coreDocument);
        coreDocument.entityMentions().forEach(coreEntityMention ->
        {
            if (coreEntityMention.entityType().equals("CITY")) {
                userData.setCityName(coreEntityMention.entity());
            }
            if (coreEntityMention.entityType().equals("PERSON")) {
                user.setName(coreEntityMention.entity());
            }
            if (coreEntityMention.entityType().equals("DURATION")) {
                userData.setHoursPerWeek(Integer.parseInt(coreEntityMention.text().replaceAll("[^0-9]", "")));
            }
        });
    }
}
