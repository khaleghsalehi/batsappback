package net.khalegh.batsapp.dao;

import net.khalegh.batsapp.entity.UserInfo;
import net.khalegh.batsapp.inspection.ContentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.UUID;

@Repository
public interface UserRepo extends JpaRepository<UserInfo, Long> {
    UserInfo findByUserName(String username);

    @Query("SELECT u.userName FROM UserInfo u WHERE u.uuid= :uuid")
    String getUserNameByUuid(@Param("uuid") UUID uuid);

    @Query("SELECT u FROM UserInfo u WHERE u.uuid= :uuid")
    UserInfo getUserByUuid(@Param("uuid") UUID uuid);

    @Query("SELECT u FROM UserInfo u WHERE u.userName= :userName")
    UserInfo getAuthKey(@Param("userName") String username);

    @Transactional
    @Modifying
    @Query("UPDATE UserInfo u set u.permitAIService=true WHERE u.uuid= :uuid")
    void permitAIService(@Param("uuid") UUID uuid);

}
