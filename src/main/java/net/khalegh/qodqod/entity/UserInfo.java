package net.khalegh.qodqod.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.UUID;

@Entity
public class UserInfo {
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
    private String firstName;

    @Getter
    @Setter
    private String lastName;

    @Getter
    @Setter
    private String userName;

    @Getter
    @Setter
    private String password;

    @Getter
    @Setter
    private String phoneNumber;

    @Getter
    @Setter
    private String email;

    @Getter
    @Setter
    private String bio;

    @Getter
    @Setter
    private String profileImage;

    @Getter
    @Setter
    private String profileHeaderImage;

    @Getter
    @Setter
    private String city;

    @Getter
    @Setter
    private String country;

    @Getter
    @Setter
    private String status;

    @Getter
    @Setter
    private long lastSeen;

}
