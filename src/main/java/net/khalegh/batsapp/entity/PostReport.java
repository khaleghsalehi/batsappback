package net.khalegh.batsapp.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.UUID;

@Entity
public class PostReport {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)

    @Getter
    @Setter
    private long id;

    @Setter
    @Getter
    private UUID uuid;

    @Getter
    @Setter
    private int reportType;

}
