package com.artuhanau.ecobot.daos.models.repos;

import com.artuhanau.ecobot.daos.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
}
