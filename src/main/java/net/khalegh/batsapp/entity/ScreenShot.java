package net.khalegh.batsapp.entity;

import lombok.Getter;
import lombok.Setter;
import net.khalegh.batsapp.inspection.ContentType;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
public class ScreenShot {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Getter
    @Setter
    private long id;

    @Getter
    @Setter
    UUID uuid;

    @Getter
    @Setter
    String fileName;

    @Getter
    @Setter
    LocalDateTime timeStamp;

    @Getter
    @Setter
    ContentType contentType;

    @Getter
    @Setter
    double probability;

    @Getter
    @Setter
    boolean isChecked;
}
