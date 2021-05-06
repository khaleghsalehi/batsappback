package net.khalegh.batsapp.dao;

import net.khalegh.batsapp.entity.PostReport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepo extends JpaRepository<PostReport, Long> {
}
