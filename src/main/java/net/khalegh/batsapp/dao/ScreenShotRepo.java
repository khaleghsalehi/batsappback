package net.khalegh.batsapp.dao;

import net.khalegh.batsapp.entity.ScreenShot;
import net.khalegh.batsapp.inspection.ContentType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ScreenShotRepo extends
        CrudRepository<ScreenShot, Long> {

    @Query("SELECT u FROM ScreenShot u WHERE u.uuid= :uuid AND u.isChecked=false")
    Optional<List<ScreenShot>> getScreenshot(@Param("uuid") UUID uuid, Pageable pageable);


    @Transactional
    @Modifying
    @Query("UPDATE ScreenShot u set u.contentType= :contentType , u.probability= :probability , u.isChecked=true " +
            " WHERE u.uuid= :uuid AND u.fileName= :fileName")
    void updateContentType(@Param("uuid") UUID uuid,
                           @Param("fileName") String fileName,
                           @Param("contentType") ContentType contentType,
                           @Param("probability") double probability);


}
