package net.khalegh.batsapp.dao;

import net.khalegh.batsapp.entity.Command;
import net.khalegh.batsapp.entity.DisLiked;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CommandRepo extends JpaRepository<Command, Long> {
    @Query(value = "SELECT u FROM Command u WHERE u.userId= :uuid")
    List<Command> getLastCommand(@Param("uuid") UUID uuid);
}
