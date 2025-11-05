package dev.kuklin.kworkcalendar.repositories;

import dev.kuklin.kworkcalendar.entities.OAuthState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface OAuthStateRepository extends JpaRepository<OAuthState, UUID> {
}
