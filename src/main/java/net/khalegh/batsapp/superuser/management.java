package net.khalegh.batsapp.superuser;

import com.google.gson.Gson;
import net.khalegh.batsapp.config.Service;
import net.khalegh.batsapp.contorl.REST;
import net.khalegh.batsapp.contorl.WebView;
import net.khalegh.batsapp.dao.UserRepo;
import net.khalegh.batsapp.entity.LiveUser;
import net.khalegh.batsapp.entity.Statistical;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@RestController
@CrossOrigin(origins = "*")
public class management {
    private static final Logger log = LoggerFactory.getLogger(WebView.class);
    //todo get from DB
    private static final String ADMIN_TOKEN = "202020_303030_404040_505050_abcedf";

    @Autowired
    UserRepo userRepo;

    private static final Gson gson = new Gson();

    @GetMapping("/v1/userList")
    public String getActiveUserList(@RequestParam(required = true) String adminToken) {
        if (!adminToken.equals(ADMIN_TOKEN))
            return null;
        List<LiveUser> list = new ArrayList<>();
        Service.LastPing.asMap().forEach((k, v) -> {
            LiveUser liveUser = new LiveUser();
            liveUser.setUserName(userRepo.getUserNameByUuid(UUID.fromString(k)));
            try {
                liveUser.setLastUpload(Service.LastUpload.get(k));
            } catch (ExecutionException e) {
                e.printStackTrace();
                liveUser.setLastUpload("unknown");
            }
            liveUser.setUuid(UUID.fromString(k));
            liveUser.setLastPing(v);
            list.add(liveUser);
        });

        Statistical statistical = new Statistical();
        statistical.setLiveUsers(list);
        statistical.setVersion(REST.VERSION);
        statistical.setUserCount(list.size());

        log.info(gson.toJson(statistical));
        return gson.toJson(statistical);
    }
}
