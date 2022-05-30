package com.artuhanau.ecobot.services.standard;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import com.artuhanau.ecobot.aiml.service.ResponseService;
import com.artuhanau.ecobot.daos.models.City;
import com.artuhanau.ecobot.daos.models.DialogCommand;
import com.artuhanau.ecobot.daos.models.User;
import com.artuhanau.ecobot.daos.models.UserData;
import com.artuhanau.ecobot.daos.models.enums.EducationStep;
import com.artuhanau.ecobot.daos.models.repos.CityRepository;
import com.artuhanau.ecobot.services.EntitiesAndRelationsService;
import com.artuhanau.ecobot.services.UserManagerService;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class StandfordEntitiesAndRelationsService implements EntitiesAndRelationsService
{
    private static final Logger LOG = LoggerFactory.getLogger(StandfordEntitiesAndRelationsService.class);

    @Resource
    private UserManagerService userManagerService;

    private StanfordCoreNLP stanfordCoreNLP;

    private StanfordCoreNLP sentimentPipeline;

    @Resource
    private CityRepository cityRepository;

    private List<String> refusalWords;

    @PostConstruct
    public void initialize()
    {
        StanfordCoreNLP.clearAnnotatorPool();
        Properties properties = new Properties();
        properties.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner,parse,coref");
        stanfordCoreNLP = new StanfordCoreNLP(properties);
        Properties properties2 = new Properties();
        properties2.setProperty("annotators", "tokenize, ssplit, parse, sentiment");
        sentimentPipeline = new StanfordCoreNLP(properties2);
        try {
            LOG.info(new java.io.File(".").getCanonicalPath());
            refusalWords = Files.readAllLines(Paths.get("src/main/resources/templates/refusalWords.txt"));
        }
        catch (IOException ioException) {
            LOG.error("Can't load refusalWords", ioException);
        }
    }

    @Override
    public String getEntities(final String englishText)
    {
        CoreDocument coreDocument = new CoreDocument(englishText);
        stanfordCoreNLP.annotate(coreDocument);
        return coreDocument.entityMentions().stream().map(entity -> entity.entityType() + " " + entity.text())
            .collect(Collectors.joining(","));
    }

    @Override
    public UserData fillEntities(final User user, String userText)
    {
        UserData userData = user.getUserData();
        CoreDocument coreDocument = new CoreDocument(userText);
        stanfordCoreNLP.annotate(coreDocument);
        DialogCommand lastCommand = user.getHistory().get(user.getHistory().size() - 1).getCommand();
        handleCostRequest(userText, userData, coreDocument, lastCommand);
        if (lastCommand.getCommand().equals("EDUCATION")) {
            fillEducation(user, userText);
        }
        handleEntityMentions(user, userData, coreDocument, lastCommand);
        fallBackCity(userText, userData, lastCommand);
        user.setUserData(userData);
        userManagerService.saveUser(user);
        return user.getUserData();
    }

    private void handleEntityMentions(final User user, final UserData userData, final CoreDocument coreDocument,
        final DialogCommand lastCommand)
    {
        coreDocument.entityMentions().forEach(coreEntityMention ->
        {
            if (coreEntityMention.entityType().equals("CITY") || coreEntityMention.entityType().equals("LOCATION")) {
                if (!coreEntityMention.sentence().lemmas().contains("not")) {
                    userData.setCityName(coreEntityMention.text());
                    LOG.info("Set city for user {} to {}", user.getTelegramId(), coreEntityMention.entity());
                }
                else {
                    if (userData.getCityName().equals(coreEntityMention.text())) {
                        userData.setCityName(null);
                    }
                }
            }
            if (coreEntityMention.entityType().equals("PERSON") && "FAMILIARITY".equals(lastCommand.getCommand())) {
                user.setName(coreEntityMention.entity());
                LOG.info("Set name for user {} to {}", user.getTelegramId(), coreEntityMention.entity());
            }
            if (coreEntityMention.entityType().equals("DURATION") || (coreEntityMention.entityType().equals("NUMBER") && "TIME".equals(
                lastCommand.getCommand()))) {
                if (coreEntityMention.entityType().equals("DURATION")) {
                    userData.setHoursPerWeek(
                        (int) Math.floor(Double.parseDouble(
                            coreEntityMention.coreMap().get(CoreAnnotations.NormalizedNamedEntityTagAnnotation.class)
                                .replaceAll("[^0-9]", ""))));
                }
                if (coreEntityMention.entityType().equals("NUMBER")) {
                    userData.setHoursPerWeek(
                        (int) Math.floor(
                            Double.parseDouble(coreEntityMention.coreMap().get(CoreAnnotations.NormalizedNamedEntityTagAnnotation.class))));
                }
                LOG.info("Set Hours per week for user {} to {}", user.getTelegramId(), userData.getHoursPerWeek());
            }
        });
    }

    private void fallBackCity(final String userText, final UserData userData, final DialogCommand lastCommand)
    {
        if ("LOCATION".equals(lastCommand.getCommand()) && userData.getCityName() == null) {
            List<City> cities = Arrays.stream(StringUtils.split(userText)).flatMap(str -> {
                City city = cityRepository.findMostSimilarCity(str);
                if (city != null) {
                    return Stream.of(city);
                }
                else {
                    return Stream.empty();
                }
            }).collect(Collectors.toList());
            if (!cities.isEmpty()) {
                userData.setCityName(cities.get(0).getNameTranslitEn());
            }
        }
    }

    private void handleCostRequest(final String userText, final UserData userData, final CoreDocument coreDocument,
        final DialogCommand lastCommand)
    {
        if ("COST".equals(lastCommand.getCommand())) {
            findRefusalOrAgreementCost(userText, userData, coreDocument);
        }
    }

    private void findRefusalOrAgreementCost(final String userText, final UserData userData, final CoreDocument coreDocument)
    {
        userData.setPaid(findRefusalOrAgreement(userText, coreDocument));
        LOG.info("Set paid for user {} to {}", userData.getId(), userData.getPaid());
    }

    public boolean findRefusalOrAgreement(final String userText, final CoreDocument coreDocument)
    {
        int sentimentInt;
        long refusalWordsCount = coreDocument.sentences().stream().flatMap(coreSentence -> coreSentence.lemmas().stream())
            .filter(s -> refusalWords.contains(s))
            .count();
        int refusalCoefficient = 0;
        if (refusalWordsCount != 0 && refusalWordsCount % 2 == 0) {
            refusalCoefficient = -1;
        }
        if (refusalWordsCount % 2 == 1) {
            refusalCoefficient = 1;
        }
        Annotation annotation = sentimentPipeline.process(userText);
        for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
            Tree tree = sentence.get(SentimentCoreAnnotations.SentimentAnnotatedTree.class);
            sentimentInt = RNNCoreAnnotations.getPredictedClass(tree);
            return (sentimentInt - refusalCoefficient >= 2);
        }
        return false;
    }

    public boolean findRefusalOrAgreement(final String userText)
    {
        CoreDocument coreDocument = new CoreDocument(userText);
        stanfordCoreNLP.annotate(coreDocument);
        return findRefusalOrAgreement(userText, coreDocument);
    }

    @Override
    public boolean fillEducation(final User user, final String russianMessage)
    {
        if (user.getUserData().getEducationStep() != null) {
            return false;
        }
        if (ResponseService.HIGH_EDUCATION.equals(russianMessage)) {
            user.getUserData().setEducationStep(EducationStep.AFTER_HIGH);
        }
        else if (ResponseService.UNIVERSITY.equals(russianMessage)) {
            user.getUserData().setEducationStep(EducationStep.UNIVERSITY);
        }
        else if (ResponseService.SCHOOL.equals(russianMessage)) {
            user.getUserData().setEducationStep(EducationStep.SCHOOL);
        }
        user.setStep(user.getUserData().getEducationStep());
        userManagerService.saveUser(user);
        LOG.info("Education {} {}", user.getTelegramId(), user.getUserData().getEducationStep());
        return user.getUserData().getEducation() != null;
    }
}
