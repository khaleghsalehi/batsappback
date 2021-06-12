package net.khalegh.batsapp.sys;

import net.khalegh.batsapp.dao.ScreenShotRepo;
import net.khalegh.batsapp.dao.UserRepo;
import net.khalegh.batsapp.entity.ScreenShot;
import net.khalegh.batsapp.entity.UserInfo;
import net.khalegh.batsapp.inspection.ContentType;
import net.khalegh.batsapp.inspection.FilterImage;
import net.khalegh.batsapp.inspection.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.TimerTask;
import java.util.UUID;

@Component
public class ImageAnalyzer extends TimerTask {

    @Autowired
    ScreenShotRepo screenShotRepo;

    @Autowired
    UserRepo userRepo;

    private static final Logger log =
            LoggerFactory.getLogger(ImageAnalyzer.class);

    public ImageAnalyzer(ScreenShotRepo screenShotRepo,
                         UserRepo userRepo) {
        this.screenShotRepo = screenShotRepo;
        this.userRepo = userRepo;
    }

    void checkImage(UUID uuid, String fileName) {
        try {
            FilterImage filterImage = new FilterImage();
            JsonObject checkResponse = filterImage.checkMe(fileName);
            switch (checkResponse.getMClassName()) {
                case "Porn":
                    screenShotRepo.updateContentType(uuid, fileName
                            , ContentType.PORN
                            , checkResponse.getMProbability());

                    break;
                case "Sexy":
                    screenShotRepo.updateContentType(uuid, fileName
                            , ContentType.SEXY
                            , checkResponse.getMProbability());
                    break;
                case "Hentai":
                    screenShotRepo.updateContentType(uuid, fileName
                            , ContentType.HENTAI
                            , checkResponse.getMProbability());
                    break;
                case "Neutral":
                    screenShotRepo.updateContentType(uuid, fileName
                            , ContentType.NEUTRAL
                            , checkResponse.getMProbability());
                    break;
                case "Drawing":
                    screenShotRepo.updateContentType(uuid, fileName
                            , ContentType.DRAWING
                            , checkResponse.getMProbability());
                    break;
                default:
                    screenShotRepo.updateContentType(uuid, fileName
                            , ContentType.UNKNOWN
                            , checkResponse.getMProbability());
                    break;


            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void inspectNow(UUID uuid, int min, int max) {

        Pageable pageable = PageRequest.of(min, max);
        Optional<List<ScreenShot>> list = screenShotRepo.getScreenshot(uuid, pageable);
        list.ifPresent(shots -> shots.forEach(screenShots -> {
            log.info("image check filename  " + screenShots.getFileName());
            checkImage(uuid, screenShots.getFileName());
        }));
        if (list.isPresent())
            log.info("image analyzed  count " + list.get().size());
        else
            log.info("image analyzed count  0");


    }

    @Override
    public void run() {
        try {

            List<UserInfo> users = userRepo.findAll();
            log.info("image processing prepared for user count " + users.size());
            users.forEach(userInfo -> {
                log.info("image analyzing for user " + userInfo.getUuid());
                inspectNow(userInfo.getUuid(), 0, 20);
            });


        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
