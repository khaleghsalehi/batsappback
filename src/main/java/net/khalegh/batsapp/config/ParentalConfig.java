package net.khalegh.batsapp.config;

import lombok.Getter;
import lombok.Setter;

public class ParentalConfig {
    @Getter
    @Setter
    private int imageQuality = 50;

    @Getter
    @Setter
    private int screenShotDelay = 60_000;
}
