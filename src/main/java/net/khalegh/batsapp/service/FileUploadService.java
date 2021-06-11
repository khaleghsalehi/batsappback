package net.khalegh.batsapp.service;

import com.github.mfathi91.time.PersianDate;
import com.google.common.hash.Hashing;
import net.khalegh.batsapp.contorl.WebView;
import net.khalegh.batsapp.dao.ScreenshotRepo;
import net.khalegh.batsapp.inspection.ContentType;
import net.khalegh.batsapp.inspection.ScreenShot;
import net.khalegh.batsapp.utils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.UUID;


@Service
public class FileUploadService {

    @Autowired
    ScreenshotRepo screenshotRepo;

    private static final Logger log = LoggerFactory.getLogger(WebView.class);
    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    SimpleDateFormat sdf = new SimpleDateFormat("HH");

    private static void checkOrCreateDirectory(String dirName) throws Exception {
        File directory = new File(dirName);
        if (!directory.exists()) {
            boolean result = directory.mkdir();
            if (result) {
                log.info("make dir  " + directory);
            } else
                log.error(" error while make  " + directory);
        }
    }

    public void uploadFile(MultipartFile file, String uuid) throws Exception {
        PersianDate today = PersianDate.now();
        String originalFilename = file.getOriginalFilename();

        log.info("screenshot from " + uuid + " filename " + originalFilename);

        String from = sdf.format(new Date());
        int kk = Integer.parseInt(from) + 1;
        String to = String.format("%2s", kk).replace(' ', '0');
        try {
            String home = System.getProperty("user.home");


            // Step 1, check if user home dir exists
            String userHomePath = home + "/" + utils.getUuidHash(uuid);
            checkOrCreateDirectory(userHomePath);

            // Step 2, check if user home_dir/date exists
            String userDateDirectory = userHomePath + "/" + today;
            checkOrCreateDirectory(userDateDirectory);


            // Step 2, check if user home_dir/date/from-to exists
            String timeDirectory = userDateDirectory + "/" + from + "-" + to;
            checkOrCreateDirectory(timeDirectory);


            String filename = userDateDirectory + "/" + from + "-" + to + "/" + originalFilename;
            file.transferTo(new File(FilenameUtils.normalize(filename)));
            net.khalegh.batsapp.config.Service.lastUpload.put(uuid,
                    LocalDateTime.now().format(timeFormatter));
            boolean containsKey = net.khalegh.batsapp.config.Service.uploadCount.asMap().containsKey(uuid);
            if (containsKey) {
                String s = net.khalegh.batsapp.config.Service.uploadCount.get(uuid);
                int value;
                if (s.equals(""))
                    value = 1;
                else {
                    value = Integer.parseInt(s);
                    value = value + 1;
                }
                net.khalegh.batsapp.config.Service.uploadCount.put(uuid, String.valueOf(value));
            } else {
                net.khalegh.batsapp.config.Service.uploadCount.put(uuid, String.valueOf(1));
            }


            ScreenShot screenShot = new ScreenShot();
            screenShot.setChecked(false);
            screenShot.setTimeStamp(LocalDateTime.now());
            screenShot.setFileName(filename);
            screenShot.setUuid(UUID.fromString(uuid));
            screenShot.setContentType(ContentType.UNKNOWN);
            screenshotRepo.save(screenShot);

            log.info("Upload successfully done.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
