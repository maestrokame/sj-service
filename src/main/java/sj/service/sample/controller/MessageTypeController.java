package sj.service.sample.controller;

import sj.service.sample.entity.MessageType;
import sj.service.sample.repository.MessageTypeRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/message-types")
public class MessageTypeController {

    private final MessageTypeRepository repository;

    public MessageTypeController(MessageTypeRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<MessageType> getAll() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<MessageType> getById(@PathVariable Integer id) {
        Optional<MessageType> mt = repository.findById(id);
        return mt.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public MessageType create(@RequestBody MessageType messageType) {
        return repository.save(messageType);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MessageType> update(@PathVariable Integer id, @RequestBody MessageType messageType) {
        return repository.findById(id)
                .map(existing -> {
                    existing.setName(messageType.getName());
                    return ResponseEntity.ok(repository.save(existing));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}

