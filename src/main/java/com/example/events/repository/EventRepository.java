package com.example.events.repository;

import com.example.events.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * JPA-repositorio Event-entiteetille.
 * Spring Data JPA generoi CRUD-toteutuksen automaattisesti.
 */
@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
}
