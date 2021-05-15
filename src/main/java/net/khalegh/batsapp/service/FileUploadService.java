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
    PersianDate today = PersianDate.now();
    SimpleDateFormat sdf = new SimpleDateFormat("HH");

    public void uploadFile(MultipartFile file, String uuid) {
        String from = sdf.format(new Date());
        int kk = Integer.parseInt(from) + 1;
        String to = String.format("%2s", kk).replace(' ', '0');
        try {
            String home = System.getProperty("user.home");


            String userDirPath = home + "/" + uuid + "/" + today;
            // check if dir exist or not
            File dateDirectory = new File(userDirPath);
            if (!dateDirectory.exists()) {
                boolean result = dateDirectory.mkdir();
                if (result) {
                    log.info("make dir  " + dateDirectory);
                } else
                    log.error(" error while make dir  " + dateDirectory);
            }

            String timeDirectory = dateDirectory + "/" + from + "-" + to;
            File subChildDir = new File(timeDirectory);
            boolean subStatus;
            if (!subChildDir.exists()) {
                subStatus = subChildDir.mkdir();
                if (!subStatus)
                    log.error("error while mkdir() subDir");
            }

            file.transferTo(new File(FilenameUtils.normalize(userDirPath + "/" + from + "-" + to + "/" + file.getOriginalFilename())));
            log.info("Upload successfully done.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
