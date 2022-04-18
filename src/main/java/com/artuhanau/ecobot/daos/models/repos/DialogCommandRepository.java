package com.artuhanau.ecobot.daos.models.repos;

import com.artuhanau.ecobot.daos.models.DialogCommand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DialogCommandRepository extends JpaRepository<DialogCommand, Long> {
    Optional<DialogCommand> getFirstByCommand(String command);
}
