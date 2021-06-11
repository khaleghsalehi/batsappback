package net.khalegh.batsapp.dao;

import net.khalegh.batsapp.inspection.ScreenShot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScreenshotRepo extends JpaRepository<ScreenShot, Long> {


}
