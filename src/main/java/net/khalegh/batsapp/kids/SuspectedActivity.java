package net.khalegh.batsapp.kids;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

@Entity
public class SuspectedActivity {

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
    LocalDateTime localDateTime;

    @Getter
    @Setter
    SuspectedAction suspectedAction;

}
