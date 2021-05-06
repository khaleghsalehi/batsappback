package net.khalegh.qodqod.dao;

import net.khalegh.qodqod.entity.Experience;
import net.khalegh.qodqod.entity.Reply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ReplyRepo extends JpaRepository<Reply, Long> {
    @Query("SELECT u FROM Reply u WHERE u.relatedPostUuid=:uuid ORDER BY u.id DESC")
    List<Reply> findByString(@Param("uuid") UUID uuid);


}
