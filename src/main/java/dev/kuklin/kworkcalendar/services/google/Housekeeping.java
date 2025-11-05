package dev.kuklin.kworkcalendar.services.google;

import dev.kuklin.kworkcalendar.repositories.OAuthLinkRepository;
import dev.kuklin.kworkcalendar.repositories.OAuthStateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class Housekeeping {
    private final OAuthLinkRepository linkRepo;
    private final OAuthStateRepository stateRepo;

    @Scheduled(fixedDelay = 600_000) // каждые 10 минут
    public void cleanup() {
        var now = Instant.now();
        linkRepo.findAll().stream()
                .filter(l -> l.getExpireAt().isBefore(now))
                .forEach(l -> linkRepo.deleteById(l.getId()));
        stateRepo.findAll().stream()
                .filter(s -> s.getExpireAt().isBefore(now))
                .forEach(s -> stateRepo.deleteById(s.getId()));
    }
}