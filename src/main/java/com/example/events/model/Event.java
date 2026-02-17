package com.example.events.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * Event-entiteetti – edustaa yksittäistä tapahtumaa.
 */
@Entity
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Nimi on pakollinen")
    @Column(nullable = false)
    private String name;

    @NotNull(message = "Päivämäärä on pakollinen")
    @Column(nullable = false)
    private LocalDate date;

    @NotBlank(message = "Sijainti on pakollinen")
    @Column(nullable = false)
    private String location;

    public Event() {
    }

    public Event(String name, LocalDate date, String location) {
        this.name = name;
        this.date = date;
        this.location = location;
    }

    // ── Getterit ja setterit ──

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
