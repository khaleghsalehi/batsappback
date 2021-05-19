package net.khalegh.batsapp.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class Statistical {
    @Getter
    @Setter
    int userCount;

    @Getter
    @Setter
    String version;

    @Getter
    @Setter
    List<LiveUser> liveUsers;


}
