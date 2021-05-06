package net.khalegh.batsapp.dao;

import net.khalegh.batsapp.entity.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserRepo extends JpaRepository<UserInfo, Long> {
    UserInfo findByUserName(String username);

    @Query("SELECT u.userName FROM UserInfo u WHERE u.uuid= :uuid")
    String getUserNameByUuid(@Param("uuid") UUID uuid);
}
