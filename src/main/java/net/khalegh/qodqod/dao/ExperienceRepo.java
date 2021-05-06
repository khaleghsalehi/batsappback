package net.khalegh.qodqod.dao;

import net.khalegh.qodqod.entity.Experience;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.UUID;

@Repository
public interface ExperienceRepo extends CrudRepository<Experience, Long> {
    // TODO: 1/29/21 enhancement, search by subject and body
    @Query("SELECT u FROM Experience u WHERE u.body like %:q% ORDER BY u.id DESC")
    List<Experience> findByString(@Param("q") String q, Pageable pageable);

    @Query("SELECT u FROM Experience u WHERE u.subject like %:subject% ORDER BY u.id DESC")
    List<Experience> findBySubject(@Param("subject") String subject, Pageable pageable);

    @Query("SELECT u FROM Experience u WHERE u.authorId= :uuid ORDER BY u.id DESC")
    List<Experience> findExperienceByUserId(@Param("uuid") UUID uuid, Pageable pageable);

    @Query("SELECT u FROM Experience u WHERE u.uuid= :uuid")
    Experience getPostByUUID(@Param("uuid") UUID uuid);

    @Transactional
    @Modifying
    @Query("UPDATE Experience u set u.liked= :count WHERE u.uuid= :uuid")
    void updateLike(@Param("uuid") UUID uuid, @Param("count") int value);

    @Transactional
    @Modifying
    @Query("UPDATE Experience u set u.disliked= :count WHERE u.uuid= :uuid")
    void updateDisLike(@Param("uuid") UUID uuid, @Param("count") int value);


    @Query("SELECT COUNT(u) FROM Experience u WHERE u.body like %:q% ")
    int countMatchedItem(@Param("q") String q);



    @Query("SELECT COUNT(u) FROM Experience u WHERE u.authorId= :uuid")
    int countUserPost(@Param("uuid") UUID uuid);


    @Query("SELECT u FROM Experience u ORDER BY u.id DESC")
    List<Experience> getAllItems(Pageable pageable);

    @Query(value = "SELECT tags, COUNT(DISTINCT experience_id) as experience_id FROM experience_tags GROUP BY tags order by  experience_id desc limit 8;", nativeQuery = true)
    List<Object[]> getHotTags();
//    SELECT tags, COUNT(DISTINCT experience_id) as experience_id FROM experience_tags GROUP BY tags order by  experience_id desc;
}
