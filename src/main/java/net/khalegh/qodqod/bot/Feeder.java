package net.khalegh.qodqod.bot;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.name.Rename;
import net.khalegh.qodqod.contorl.WebView;
import net.khalegh.qodqod.dao.ExperienceRepo;
import net.khalegh.qodqod.dao.UserRepo;
import net.khalegh.qodqod.entity.Experience;
import net.khalegh.qodqod.entity.UserInfo;
import net.khalegh.qodqod.tools.Video;
import net.khalegh.qodqod.utils;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.khalegh.qodqod.contorl.REST.saveFile;

@CrossOrigin(origins = "*")
@RestController
public class Feeder {
    private static final Logger log = LoggerFactory.getLogger(WebView.class);

    @Autowired
    ExperienceRepo experienceRepo;

    @Autowired
    UserRepo userRepo;

    @Autowired
    Environment environment;

    @GetMapping("/v1/startbot")
    public String importExperience(HttpServletResponse response) throws ParserConfigurationException, IOException, SAXException {
        if (Objects.equals(environment.getProperty("bot.status"), "disable")) {
            log.error("bot is disabled, return -1");
            return "bot disabled by now";
        }
        String dump = environment.getProperty("dump.path");
        String username = environment.getProperty("bot.name");
        CharSequence password = environment.getProperty("bot.password");
        @Nullable UserInfo checkUserInfo = userRepo.findByUserName(username);

        if (checkUserInfo == null) {
            UserInfo userInfo = new UserInfo();
            try {
                BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12); // Strength set as 12

                String encodedPassword = encoder.encode(password);
                userInfo.setUserName(username);
                userInfo.setUuid(UUID.randomUUID());
                userInfo.setPassword(encodedPassword);
                userInfo.setCountry("ایران");
                userInfo.setCity("تهران");
                userInfo.setBio("بات رسمی اکسبورد");
                userInfo.setStatus("فعال");
                userInfo.setFirstName("بات");
                userInfo.setLastName("اکسبورد");
                userInfo.setEmail("bot@exbord.i");
                userInfo.setPhoneNumber("+989126349698");
                userRepo.save(userInfo);

            } catch (Exception e) {
                e.printStackTrace();
            }

            File input = new File(dump + "/data.xml");
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(input);
            doc.getDocumentElement().normalize();
            System.out.println(doc.getDocumentElement().getNodeName());
            NodeList nodeList = doc.getElementsByTagName("exp");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                System.out.println(node.getNodeName());
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    try {
                        Experience experience = new Experience();
                        experience.setSubject(element.getElementsByTagName("subject").item(0).getTextContent());
                        experience.setBody(element.getElementsByTagName("body").item(0).getTextContent());
                        LocalDateTime now = LocalDateTime.now();
                        experience.setTimeStamp(now);
                        experience.setUuid(UUID.randomUUID());
                        experience.setTags(utils.getTags(experience.getBody()));
                        experience.setAuthorId(userInfo.getUuid());
                        experience.setAuthorName(userInfo.getUserName());
                        File userDir = new File(environment.getProperty("upload.path") + "user-photos/" + experience.getUuid());
                        if (userDir.mkdir()) {
                            log.info("create user profile dir success." + experience.getUuid());
                            if (!element.getElementsByTagName("photo").item(0).getTextContent().isEmpty()) {
                                experience.setPhotos(Collections.singletonList(element.getElementsByTagName("photo").item(0).getTextContent()));

                                log.info("copy image to exp path" + experience.getUuid());
                                File file = new File(dump + "/" + element.getElementsByTagName("photo").item(0).getTextContent());
                                String absolutePath = file.getAbsolutePath();
                                Path src = Paths.get(absolutePath);
                                String picturePathPrefix = userDir + "/" + element.getElementsByTagName("photo").item(0).getTextContent();
                                Path dst = Paths.get(picturePathPrefix);
                                Files.copy(src, dst);
                                Thumbnails.of(new File(picturePathPrefix))
                                        .size(250, 250)
                                        .toFiles(Rename.PREFIX_DOT_THUMBNAIL);
                            }
                            if (!element.getElementsByTagName("video").item(0).getTextContent().isEmpty()) {
                                experience.setVideo(Collections.singletonList(element.getElementsByTagName("video").item(0).getTextContent()));

                                log.info("copy video to exp path" + experience.getUuid());
                                File file = new File(dump + "/" + element.getElementsByTagName("video").item(0).getTextContent());
                                String absolutePath = file.getAbsolutePath();
                                Path src = Paths.get(absolutePath);
                                String picturePathPrefix = userDir + "/" + element.getElementsByTagName("video").item(0).getTextContent();
                                Path dst = Paths.get(picturePathPrefix);
                                Files.copy(src, dst);
                                try {
                                    String res = Video.randomGrabberFFmpegImage(userDir + "/" + element.getElementsByTagName("video").item(0).getTextContent(), 2);
                                    log.info("video thumbnail result" + res);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                            }

                        }
                        experienceRepo.save(experience);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            response.sendRedirect("/");
            return "Import done.";

        }
        response.sendRedirect("/");
        return "Data already imported.";
    }
}
