package net.khalegh.qodqod.dao;

import net.khalegh.qodqod.entity.Liked;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LikeRepo extends JpaRepository<Liked, Long> {


    @Query("SELECT u FROM Liked u WHERE u.userId= :userId and u.postId= :postId")
    Liked isLiked(@Param("userId") UUID userId, @Param("postId") UUID postId);

    @Query("SELECT u FROM Liked u WHERE u.postId= :postId")
    List<Liked> whoLiked(@Param("postId") UUID postId, Pageable pageable);
}
