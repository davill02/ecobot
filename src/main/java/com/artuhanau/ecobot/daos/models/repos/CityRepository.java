package com.artuhanau.ecobot.daos.models.repos;

import java.util.Optional;

import com.artuhanau.ecobot.daos.models.City;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CityRepository extends JpaRepository<City,Long> {
    @Query(value = "SELECT c.id,c.name,c.name_english, c.name_translit_en FROM city AS c WHERE SIMILARITY(name_english,?1) > 0.6 OR SIMILARITY(name_translit_en, ?1) > 0.6 ORDER BY SIMILARITY(name_english,?1) DESC, SIMILARITY(name_translit_en, ?1) DESC LIMIT 1", nativeQuery = true)
    City findMostSimilarCity(String name);

    Optional<City> findCityByName(String cityName);
}
