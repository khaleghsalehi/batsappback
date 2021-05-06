package net.khalegh.qodqod.dao;

import net.khalegh.qodqod.entity.Story;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StoryRepo extends JpaRepository<Story, Long> {
}
