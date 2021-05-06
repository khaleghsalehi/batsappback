package net.khalegh.qodqod.dao;

import net.khalegh.qodqod.entity.PostReport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepo extends JpaRepository<PostReport, Long> {
}
