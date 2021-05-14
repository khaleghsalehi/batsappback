package net.khalegh.batsapp.service;

import com.github.mfathi91.time.PersianDate;
import net.khalegh.batsapp.contorl.WebView;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

@Service
public class FileUploadService {
    private static final Logger log = LoggerFactory.getLogger(WebView.class);
    PersianDate today = PersianDate.now();

    public void uploadFile(MultipartFile file, String uuid) {

        try {
            String home = System.getProperty("user.home");
            String userDirPath = home + "/" + uuid + "/" + today;
            // check if dir exist or not
            File directory = new File(userDirPath);
            if (!directory.exists()) {
                boolean result = directory.mkdir();
                if (result)
                    log.info("make dir  " + directory);
                else
                    log.error(" error while make dir  " + directory);
            }
            file.transferTo(new File(FilenameUtils.normalize(userDirPath + "/" + file.getOriginalFilename())));
            log.info("Upload successfully done.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
