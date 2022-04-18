package com.artuhanau.ecobot.services.standard;

import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;

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
    public List<String> getEntities(final String englishText)
    {
        CoreDocument coreDocument = new CoreDocument(englishText);
        stanfordCoreNLP.annotate(coreDocument);
        return coreDocument.entityMentions().stream()
            .map(entity -> entity.text() + " " + entity.entityType())
            .collect(Collectors.toList());
    }
}
