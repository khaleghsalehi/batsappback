package net.khalegh.batsapp.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

public class LiveUser {

    @Getter
    @Setter
    String userName;

    @Getter
    @Setter
    UUID uuid;

    @Getter
    @Setter
    String lastPing;

    @Getter
    @Setter
    String lastUpload;

    @Getter
    @Setter
    int uploadCount;

    @Getter
    @Setter
    String installedVersion;
}
