package net.khalegh.batsapp.dao;

import net.khalegh.batsapp.entity.DisLiked;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DisLikedRepo extends JpaRepository<DisLiked, Long> {

    @Query("SELECT u FROM DisLiked u WHERE u.userId= :userId and u.postId= :postId")
    DisLiked isDisLiked(@Param("userId") UUID userId, @Param("postId") UUID postId);
}
