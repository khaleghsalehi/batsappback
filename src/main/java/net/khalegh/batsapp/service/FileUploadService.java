package net.khalegh.batsapp.service;

import net.khalegh.batsapp.contorl.WebView;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Service
public class FileUploadService {
    private static final Logger log = LoggerFactory.getLogger(WebView.class);
    public void uploadFile(MultipartFile file) {
        try {
            String home = System.getProperty("user.home");
            file.transferTo(new File(FilenameUtils.normalize(home + "/DD/" + file.getOriginalFilename())));
            log.info("Upload successfully done.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
