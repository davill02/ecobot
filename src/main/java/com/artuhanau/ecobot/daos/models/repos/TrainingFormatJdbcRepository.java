package com.artuhanau.ecobot.daos.models.repos;

import java.util.List;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.sql.DataSource;

import com.artuhanau.ecobot.daos.models.UserData;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

public class TrainingFormatJdbcRepository
{
    @Resource
    private DataSource dataSource;

    private NamedParameterJdbcTemplate jdbcTemplate;

    @PostConstruct
    public void init()
    {
        jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    public List<Integer> relevantSearch(UserData userData)
    {
        StringBuilder stringBuilder = new StringBuilder();
        MapSqlParameterSource sqlParameterSource = new MapSqlParameterSource();
        stringBuilder.append(
            "SELECT tf.id FROM training_format as tf JOIN educational_organisation_formats as eof ON eof.formats = tf.id JOIN educational_organisation as eo ON eof.educational_organisation_id = eo.id JOIN city ON eo.city_id = city.id WHERE "
        ).append(" (tf.is_only_high_education = :high or tf.is_only_middle_education = :middle) ");
        sqlParameterSource = sqlParameterSource.addValue("high", true).addValue("middle", true);
        if (userData.getPaid() != null) {
            stringBuilder.append(" and tf.is_paid = :paid ");
            sqlParameterSource = sqlParameterSource.addValue("paid", userData.getPaid());
        }
        if (userData.getCityName() != null) {
            stringBuilder.append(" and (city.name = :city or eo.city_id IS NULL) ");
            sqlParameterSource = sqlParameterSource.addValue("city", userData.getCityName());
        }
        if (userData.getHoursPerWeek() != null) {
            stringBuilder.append(" AND tf.hours_per_week =< :hour ");
            sqlParameterSource = sqlParameterSource.addValue("hour", userData.getHoursPerWeek());
        }
        stringBuilder.append(" ORDER BY tf.boost_value DESC ");
        return jdbcTemplate.queryForList(stringBuilder.toString(), sqlParameterSource, Integer.class);
    }
}

