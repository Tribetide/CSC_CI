package com.example.events.controller;

import com.example.events.model.Event;
import com.example.events.repository.EventRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * REST-ohjain tapahtumien CRUD-operaatioille.
 *
 * GET    /api/events       – listaa kaikki
 * GET    /api/events/{id}  – hae yksittäinen
 * POST   /api/events       – luo uusi
 * PUT    /api/events/{id}  – päivitä olemassa oleva
 * DELETE /api/events/{id}  – poista
 */
@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventRepository repository;

    public EventController(EventRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Event> getAll() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public Event getById(@PathVariable Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Tapahtumaa ei löydy: " + id));
    }

    @PostMapping
    public ResponseEntity<Event> create(@Valid @RequestBody Event event) {
        Event saved = repository.save(event);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public Event update(@PathVariable Long id, @Valid @RequestBody Event updated) {
        return repository.findById(id)
                .map(event -> {
                    event.setName(updated.getName());
                    event.setDate(updated.getDate());
                    event.setLocation(updated.getLocation());
                    return repository.save(event);
                })
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Tapahtumaa ei löydy: " + id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Tapahtumaa ei löydy: " + id);
        }
        repository.deleteById(id);
    }
}
