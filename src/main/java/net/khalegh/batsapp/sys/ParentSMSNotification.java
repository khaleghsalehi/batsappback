package net.khalegh.batsapp.sys;


import net.khalegh.batsapp.config.Service;
import net.khalegh.batsapp.dao.UserRepo;
import net.khalegh.batsapp.entity.UserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.TimerTask;
import java.util.UUID;


public class ParentSMSNotification extends TimerTask {
    @Autowired
    UserRepo userRepo;
    private static final Logger log = LoggerFactory
            .getLogger(ParentSMSNotification.class);

    private void sendSMS(String phoneNumber) {
        //todo implement
    }

    @Override
    public void run() {
        //todo send sms to parent ( from cache)
        if (Service.suspectedClients.size() <= 0) {
            log.info("empty sms notification queue");
            return;
        }
        Service.suspectedClients.asMap().forEach((uuid, count) -> {
            UserInfo userInfo = new UserInfo();
            userInfo = userRepo.getUserByUuid(UUID.fromString(uuid));

            if (userInfo.isSendSMS()) {
                log.info(" send sms to " + userInfo.getUserName()
                        + " phone" + userInfo.getPhoneNumber());
                // sms notification is an add on service
                sendSMS(userInfo.getPhoneNumber());
            }
        });

    }
}
