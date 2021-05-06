package net.khalegh.qodqod.dao;

import net.khalegh.qodqod.entity.Flower;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface FlowerRepo extends JpaRepository<Flower, Long> {
    @Query("SELECT COUNT(u) FROM Flower u WHERE u.userId= :userId")
    int getFlowerCount(@Param("userId") UUID uuid);

    @Query("SELECT u FROM Flower u WHERE u.flowerId= :flowerId")
    Flower isFlowedBy(@Param("flowerId") UUID flowerId);

}
