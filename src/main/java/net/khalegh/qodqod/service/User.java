package net.khalegh.qodqod.service;

import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.name.Rename;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service
public class User {

    public static void initUser(String path, UUID uuid) throws IOException {
        Thumbnails.of(new File("picture.png"))
                .size(100, 100)
                .outputFormat("jpg")
                .toFiles(new File(path+"user-photos/" + uuid + "/picture"), Rename.PREFIX_DOT_THUMBNAIL);
        Thumbnails.of(new File("picture.png"))
                .size(250, 250)
                .outputFormat("jpg")
                .toFiles(new File(path+"user-photos/" + uuid + "/picture"), Rename.SUFFIX_HYPHEN_THUMBNAIL);

    }
}
