package net.khalegh.batsapp.dao;

import net.khalegh.batsapp.config.ParentalConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

@Repository
public interface ParentalConfigRepo extends JpaRepository<ParentalConfig, Long> {

    @Query("SELECT u  FROM ParentalConfig u WHERE u.uuid= :uuid")
    List<ParentalConfig> findConfigByUuid(@Param("uuid") UUID uuid);
}
