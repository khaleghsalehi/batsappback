package net.khalegh.qodqod.dao;

import net.khalegh.qodqod.entity.PrivateMessage;
import org.springframework.data.annotation.Persistent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

@Persistent
public interface MessageRepo extends JpaRepository<PrivateMessage, Long> {
    @Query("SELECT u FROM PrivateMessage u WHERE u.reciever= :uuid")
    List<PrivateMessage> getMessage(@Param("uuid") UUID uuid);


}
