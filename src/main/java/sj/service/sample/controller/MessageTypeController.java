package sj.service.sample.controller;

import org.springframework.security.access.prepost.PreAuthorize;
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

    @PreAuthorize("hasAuthority('user')")
    @GetMapping
    public List<MessageType> getAll() {
        return repository.findAll();
    }

    @PreAuthorize("hasAuthority('user')")
    @GetMapping("/{id}")
    public ResponseEntity<MessageType> getById(@PathVariable Integer id) {
        Optional<MessageType> mt = repository.findById(id);
        return mt.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAuthority('user')")
    @PostMapping
    public MessageType create(@RequestBody MessageType messageType) {
        return repository.save(messageType);
    }

    @PreAuthorize("hasAuthority('user')")
    @PutMapping("/{id}")
    public ResponseEntity<MessageType> update(@PathVariable Integer id, @RequestBody MessageType messageType) {
        return repository.findById(id)
                .map(existing -> {
                    existing.setName(messageType.getName());
                    return ResponseEntity.ok(repository.save(existing));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAuthority('user')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}

