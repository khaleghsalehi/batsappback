package net.khalegh.batsapp.dao;

import net.khalegh.batsapp.kids.SuspectedActivity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SuspectedActivityRepo extends JpaRepository<SuspectedActivity, Long> {
}
