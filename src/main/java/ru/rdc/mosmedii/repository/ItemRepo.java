package ru.rdc.mosmedii.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.rdc.mosmedii.models.Item;

import java.util.Optional;

@Repository
public interface ItemRepo extends JpaRepository<Item, Long> {
    Optional<Item> findByTaskId(String taskId);
}
