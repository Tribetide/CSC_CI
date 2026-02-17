package com.example.events.controller;

import com.example.events.model.Event;
import com.example.events.repository.EventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integraatiotestit EventController-luokalle.
 * Käytetään test-profiilia (H2 in-memory -tietokanta).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EventRepository repository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    // ── GET /api/events ──

    @Test
    void getAllEvents_returnsEmptyList() throws Exception {
        mockMvc.perform(get("/api/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getAllEvents_returnsSavedEvents() throws Exception {
        repository.save(new Event("Seminaari", LocalDate.of(2026, 3, 15), "Helsinki"));
        repository.save(new Event("Workshop", LocalDate.of(2026, 4, 10), "Tampere"));

        mockMvc.perform(get("/api/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("Seminaari")))
                .andExpect(jsonPath("$[1].name", is("Workshop")));
    }

    // ── GET /api/events/{id} ──

    @Test
    void getById_returnsEvent() throws Exception {
        Event saved = repository.save(
                new Event("Konferenssi", LocalDate.of(2026, 5, 20), "Turku"));

        mockMvc.perform(get("/api/events/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Konferenssi")))
                .andExpect(jsonPath("$.location", is("Turku")));
    }

    @Test
    void getById_notFound() throws Exception {
        mockMvc.perform(get("/api/events/999"))
                .andExpect(status().isNotFound());
    }

    // ── POST /api/events ──

    @Test
    void createEvent_returnsCreated() throws Exception {
        Event event = new Event("Hackathon", LocalDate.of(2026, 6, 1), "Oulu");

        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(event)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", is("Hackathon")))
                .andExpect(jsonPath("$.location", is("Oulu")));
    }

    @Test
    void createEvent_validationFails_whenNameBlank() throws Exception {
        Event event = new Event("", LocalDate.of(2026, 6, 1), "Oulu");

        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(event)))
                .andExpect(status().isBadRequest());
    }

    // ── PUT /api/events/{id} ──

    @Test
    void updateEvent_returnsUpdated() throws Exception {
        Event saved = repository.save(
                new Event("Vanha nimi", LocalDate.of(2026, 1, 1), "Vanha paikka"));

        Event updated = new Event("Uusi nimi", LocalDate.of(2026, 12, 31), "Uusi paikka");

        mockMvc.perform(put("/api/events/" + saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Uusi nimi")))
                .andExpect(jsonPath("$.location", is("Uusi paikka")));
    }

    @Test
    void updateEvent_notFound() throws Exception {
        Event updated = new Event("Ei ole", LocalDate.of(2026, 1, 1), "Ei mitään");

        mockMvc.perform(put("/api/events/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isNotFound());
    }

    // ── DELETE /api/events/{id} ──

    @Test
    void deleteEvent_returnsNoContent() throws Exception {
        Event saved = repository.save(
                new Event("Poistettava", LocalDate.of(2026, 1, 1), "Jossain"));

        mockMvc.perform(delete("/api/events/" + saved.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteEvent_notFound() throws Exception {
        mockMvc.perform(delete("/api/events/999"))
                .andExpect(status().isNotFound());
    }
}
