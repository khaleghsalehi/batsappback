package net.khalegh.batsapp.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
public class Experience {
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
    private long parentId;

    @Getter
    @Setter
    private LocalDateTime timeStamp;


    @Getter
    @Setter
    private String subject;


    @Getter
    @Setter
    @Column(length = 10_000)
    private String body;

    @Getter
    @Setter
    @ElementCollection
    private List<String> tags;


    @Getter
    @Setter
    @ElementCollection
    private List<String> links;

    @Getter
    @Setter
    @ElementCollection
    private List<String> photos;

    @Getter
    @Setter
    @ElementCollection
    private List<String> video;



    @Getter
    @Setter
    private String description;


    @Getter
    @Setter
    private String authorName;

    @Getter
    @Setter
    private UUID authorId;

    @Getter
    @Setter
    private int view;

    @Getter
    @Setter
    private int liked;

    @Getter
    @Setter
    private int disliked;

}
