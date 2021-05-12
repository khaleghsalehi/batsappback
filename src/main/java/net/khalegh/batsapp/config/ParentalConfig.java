package net.khalegh.batsapp.config;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.UUID;

@Entity
public class ParentalConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)

    @Getter
    @Setter
    private long id;

    @Getter
    @Setter
    private UUID uuid;

    @Getter
    @Setter
    private int imageQuality = 50;

    @Getter
    @Setter
    private int screenShotDelay = 60_000;
}
