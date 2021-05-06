package net.khalegh.batsapp.dao;

import net.khalegh.batsapp.entity.Story;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StoryRepo extends JpaRepository<Story, Long> {
}
