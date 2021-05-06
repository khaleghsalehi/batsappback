package net.khalegh.qodqod.entity;


import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;
import java.util.UUID;

@Entity
public class Reply {
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
    private UUID relatedPostUuid;

    @Getter
    @Setter
    private UUID repliedUserId;

    @Getter
    @Setter
    private String body;

    @Getter
    @Setter
    private String timeStamp;

    @Getter
    @Setter
    @ElementCollection
    private List<String> tags;

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
