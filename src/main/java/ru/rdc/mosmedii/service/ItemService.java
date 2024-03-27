package ru.rdc.mosmedii.service;

import org.springframework.stereotype.Service;
import ru.rdc.mosmedii.models.Item;
import ru.rdc.mosmedii.repository.ItemRepo;

import java.util.List;
import java.util.Optional;

@Service
public class ItemService {
    private final ItemRepo repo;

    public ItemService(ItemRepo repo) {
        this.repo = repo;
    }

    public Optional<Item> findById(Long id) {
        return repo.findById(id);
    }

    public List<Item> findAll() {
        return repo.findAll();
    }

    public Item save(Item item) {
        return repo.save(item);
    }

    public void delete(Item item) {
        repo.delete(item);
    }

    public Optional<Item> findByTaskId(String taskId) {
        return repo.findByTaskId(taskId);
    }
}
