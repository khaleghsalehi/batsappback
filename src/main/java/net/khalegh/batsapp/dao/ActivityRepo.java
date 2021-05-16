package net.khalegh.batsapp.dao;

import net.khalegh.batsapp.entity.Activity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ActivityRepo extends JpaRepository<Activity, Long> {
    @Query("SELECT u  FROM Activity u WHERE u.uuid= :uuid")
    List<Activity> findActivityByUuid(@Param("uuid") UUID uuid);
}
