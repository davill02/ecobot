package com.artuhanau.ecobot.daos.models.repos;

import java.util.List;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.sql.DataSource;

import com.artuhanau.ecobot.daos.models.UserData;
import com.artuhanau.ecobot.daos.models.enums.EducationStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class TrainingFormatJdbcRepository
{
    private static final Logger LOG = LoggerFactory.getLogger(TrainingFormatJdbcRepository.class);

    @Resource
    private DataSource dataSource;

    private NamedParameterJdbcTemplate jdbcTemplate;

    @PostConstruct
    public void init()
    {
        jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    public List<Long> relevantSearch(UserData userData)
    {
        StringBuilder stringBuilder = new StringBuilder();
        MapSqlParameterSource sqlParameterSource = new MapSqlParameterSource();
        stringBuilder.append(
            "SELECT tf.id  FROM training_format as tf JOIN educational_organisation AS eo ON eo.id = tf.organisation_id JOIN city ON eo.city_id = city.id WHERE "
        ).append(" (tf.is_only_high_education = :high or tf.is_only_middle_education = :middle) ");
        sqlParameterSource = getMapSqlParameterSourceWithEducationParams(userData, sqlParameterSource);
        if (userData.getPaid() != null) {
            stringBuilder.append(" and tf.is_paid = :paid ");
            sqlParameterSource = sqlParameterSource.addValue("paid", userData.getPaid());
        }
        if (userData.getCityName() != null) {
            stringBuilder.append(" and (city.name_english = :city or eo.city_id IS NULL) ");
            sqlParameterSource = sqlParameterSource.addValue("city", userData.getCityName());
        }
        if (userData.getHoursPerWeek() != null) {
            stringBuilder.append(" AND tf.hours_per_week =< :hour ");
            sqlParameterSource = sqlParameterSource.addValue("hour", userData.getHoursPerWeek());
        }
        stringBuilder.append(" ORDER BY tf.boost_value DESC ");
        LOG.info(stringBuilder.toString());
        return jdbcTemplate.queryForList(stringBuilder.toString(), sqlParameterSource, Long.class);
    }

    private MapSqlParameterSource getMapSqlParameterSourceWithEducationParams(final UserData userData, MapSqlParameterSource sqlParameterSource)
    {
        boolean middle = false, high = false;
        if (userData.getEducationStep() == EducationStep.UNIVERSITY || userData.getEducationStep() == EducationStep.AFTER_HIGH
            || userData.getEducationStep() == EducationStep.ENTRANT) {
            high = true;
        }
        if (userData.getEducationStep() == EducationStep.SCHOOL) {
            high = true;
            middle = true;
        }
        if (userData.getEducationStep() == EducationStep.OTHER) {
            high = false;
            middle = false;
        }
        sqlParameterSource = sqlParameterSource.addValue("high", high ).addValue("middle", middle);
        return sqlParameterSource;
    }
}

