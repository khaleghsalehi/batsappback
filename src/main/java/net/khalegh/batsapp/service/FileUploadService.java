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
import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class FileUploadService {
    private static final Logger log = LoggerFactory.getLogger(WebView.class);
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
        log.info("screenshot from " + uuid + " filename " + file.getOriginalFilename());
        String from = sdf.format(new Date());
        int kk = Integer.parseInt(from) + 1;
        String to = String.format("%2s", kk).replace(' ', '0');
        try {
            String home = System.getProperty("user.home");

            // Step 1, check if user home dir exists
            String userHomePath = home + "/" + uuid;
            checkOrCreateDirectory(userHomePath);

            // Step 2, check if user home_dir/date exists
            String userDateDirectory = userHomePath + "/" + today;
            checkOrCreateDirectory(userDateDirectory);


            // Step 2, check if user home_dir/date/from-to exists
            String timeDirectory = userDateDirectory + "/" + from + "-" + to;
            checkOrCreateDirectory(timeDirectory);


            file.transferTo(new File(FilenameUtils.normalize(userDateDirectory + "/" + from + "-" + to + "/" + file.getOriginalFilename())));
            log.info("Upload successfully done.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
