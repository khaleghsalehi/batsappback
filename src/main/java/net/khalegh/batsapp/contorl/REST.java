package net.khalegh.batsapp.contorl;

import com.github.mfathi91.time.PersianDate;
import com.google.gson.Gson;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.name.Rename;
import net.khalegh.batsapp.config.ParentalConfig;
import net.khalegh.batsapp.config.Service;
import net.khalegh.batsapp.dao.*;
import net.khalegh.batsapp.entity.*;
import net.khalegh.batsapp.kids.SuspectedAction;
import net.khalegh.batsapp.kids.SuspectedActivity;
import net.khalegh.batsapp.service.FileUploadService;
import net.khalegh.batsapp.tools.Video;
import net.khalegh.batsapp.utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@CrossOrigin(origins = "*")

@RestController
public class REST {
    private static final Logger log = LoggerFactory.getLogger(WebView.class);
    private static final int WE_GET_YOUR_MESSAGE = 200;
    private static final int NULL_OR_EMPTY_MESSAGE = 0;
    @Autowired
    ExperienceRepo experienceRepo;

    @Autowired
    ReportRepo reportRepo;

    @Autowired
    UserRepo userRepo;

    @Autowired
    ReplyRepo replyRepo;

    @Autowired
    LikeRepo likeRepo;

    @Autowired
    DisLikedRepo disLikedRepo;

    @Autowired
    Environment environment;

    @Autowired
    FlowerRepo flowerRepo;

    @Autowired
    MessageRepo messageRepo;

    @Autowired
    ContactRepo contactRepo;

    @Autowired
    CommandRepo commandRepo;


    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    FileUploadService fileUploadService;

    @Autowired
    ParentalConfigRepo parentalConfigRepo;

    @Autowired
    ActivityRepo activityRepo;

    @Autowired
    SuspectedActivityRepo suspectedActivityRepo;


    public final static String VERSION = "0.0.7";
    private final static String TYPE_INVALID_ERROR = "Error, Invalid or empty type";
    public final static int SPACE_ERROR_USERNAME = -9;
    public final static int PASSWORD_NOT_SAME = -8;
    public final static int FILE_SIZE_TOO_LARGE = -7;
    public final static int USER_ALREADY_LIKED = -6;
    public final static int OLD_NEW_PASSWORD_SAME = -5;
    public final static int USER_NOT_FOUND_OR_NULL_ERROR = -4;
    public final static int OLD_PASSWORD_NOT_MATCHED = -3;
    public final static int INPUT_IS_NOT_CORRECT = -2;
    public final static int USER_EXIST = -1;
    public final static int ERROR_USER_OR_PASSWORD = -1;
    public final static int RESPONSE_SUCCESS = 200;
    public static final int RESPONSE_ERROR = 500;
    private static final String DEFAULT_AVATAR = "avatar.png";
    private static final Gson gson = new Gson();
    private static final int MAX_PING_DIFF = 10; // 10 second


    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    DateTimeFormatter suspectedActivityFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");


    /////////////// BatsApp normal app

    private String getClientVersion(HttpServletRequest request) {
        Optional<String> status = Optional.ofNullable(request.getHeader("auth"));
        if (status.isPresent()) {
            return request.getHeader("ver");
        }
        return "unknown";
    }

    private boolean isValidRequest(HttpServletRequest request) {
        //fixme check in filter.
        Optional<String> status = Optional.ofNullable(request.getHeader("auth"));
        if (status.isPresent()) {
            log.info("==== header auth ->  " + request.getHeader("auth"));
            log.info("==== header ver  ->  " + request.getHeader("ver"));
            //todo check and return true
            return true;
        } else {
            log.info("==== header auth ERORR =========");
            return false;
        }
    }


    @GetMapping("/v1/checkPass")
    public String checkPassword(@RequestParam(required = true) String uuid,
                                @RequestParam(required = true) String password,
                                HttpServletRequest request) {
        log.info("===================================== checkPass ======================");
        log.info(" uuid and password" + uuid + " == " + password);
        if (!isValidRequest(request)) {
            log.info("request is not valid, return null");
            return "null";
        }
        Optional<UserInfo> user = Optional.ofNullable(userRepo.getUserByUuid(UUID.fromString(uuid)));
        if (user.isPresent()) {
            if (passwordEncoder.matches(password, user.get().getPassword())) {
                log.info("password matched for  uuid " + uuid);
                return "AUTHORIZED";
            } else {
                log.error("password not matched for uuid " + uuid);
                return "NULL";
            }
        } else {
            return "NULL";
        }
    }

    @GetMapping("/v1/ws")
    public String whatsUp(@RequestParam(required = true) String uuid,
                          HttpServletRequest request) {
        if (!isValidRequest(request))
            return "null";

        if (!(uuid.matches("[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}")) || (uuid.isEmpty())) {
            log.error("ws uuid is null,empty or invalid , return null.");
            return null;
        } else {
            Optional<UserInfo> user = Optional.ofNullable(userRepo.getUserByUuid(UUID.fromString(uuid)));
            ParentalConfig parentalConfig = new ParentalConfig();
            try {
                if (user.isPresent()) {
                    log.info("incoming ws, username -> " + user.get().getUserName() + ", uuid " + user.get().getUuid());
                    Service.versionList.put(String.valueOf(user.get().getUuid()), getClientVersion(request));
                    // log ws ping history

                    //todo store in cache or queue for bulk save?
                    Activity activity = new Activity();
                    activity.setUuid(user.get().getUuid());
                    activity.setPingTime(LocalDateTime.now().format(timeFormatter));
                    activity.setPingDate(PersianDate.now().format(dtf));
                    activityRepo.save(activity);

                    // set ping time to cache
                    if (Service.lastPing.get(uuid).isEmpty()) {
                        log.info("LastPing cache is null or empty for " + uuid);
                        Service.lastPing.put(uuid, activity.getPingTime());

                    }
                    int diff = utils.getTimeDiff(activity.getPingTime(), Service.lastPing.get(uuid));
                    if (diff > MAX_PING_DIFF) {
                        log.error("ping diff " + diff + " from " + uuid);
                        log.info(" =====  suspected action =====");
                        SuspectedActivity suspectedActivity = new SuspectedActivity();
                        suspectedActivity.setUuid(UUID.fromString(uuid));
                        suspectedActivity.setLocalDateTime(LocalDateTime
                                .now()
                                .format(suspectedActivityFormat));
                        suspectedActivity.setSuspectedAction(SuspectedAction.NO_PING);

                        suspectedActivityRepo.save(suspectedActivity);
                        int count;
                        if (!Service.suspectedClients.asMap().containsKey(uuid))
                            count = 1;
                        else {
                            count = Integer.parseInt(Service.suspectedClients.get(uuid));
                            count++;
                        }
                        Service.suspectedClients.put(uuid, String.valueOf(count));

                        log.info("log suspected count " + count + " type " + SuspectedAction.NO_PING + " for " + uuid);

                    } else {
                        log.info("ping diff " + diff + " is ok " + uuid);
                    }
                    Service.lastPing.put(uuid, activity.getPingTime());

                    List<ParentalConfig> baseUser = parentalConfigRepo.findConfigByUuid(UUID.fromString(uuid));
                    int imageQuality = baseUser.get(baseUser.size() - 1).getImageQuality();
                    parentalConfig.setImageQuality(imageQuality);
                    int screenShotDelay = baseUser.get(baseUser.size() - 1).getScreenShotDelay();
                    parentalConfig.setScreenShotDelay(screenShotDelay);
                    // String command = baseUser.get(baseUser.size() - 1).getCommand();
                    // fetch latest command
                    List<Command> command;
                    command = commandRepo.getLastCommand(UUID.fromString(uuid));
                    String commandName = command.get(command.size() - 1).getCommandName();
                    parentalConfig.setCommand(commandName);
                    log.info("send command to client " + commandName);
                    return gson.toJson(parentalConfig);
                } else {
                    log.error("user null or empty, return default config");
                    return "ERROR";
                }
            } catch (Exception e) {
                e.printStackTrace();
                log.error("Oppooos, exception. return default config");
                return gson.toJson(parentalConfig);
            }
        }
    }

    @GetMapping("/v1/getAuthKey")
    public String getAthKey(@RequestParam(required = true) String username,
                            @RequestParam(required = true) String password,
                            HttpServletRequest request) {
        if (!isValidRequest(request))
            return "null";
        log.info("authentication request");
        //todo username uppercase/lowercase issue, fix it
        // cause nullPointerException
        Optional<UserInfo> user = Optional.ofNullable(userRepo.findByUserName(username));
        if (user.isPresent() && passwordEncoder.matches(password, user.get().getPassword())) {
            log.info("user matched ");
            return user.get().getUuid().toString();
        } else {
            return "";
        }
    }

    @PostMapping("/v1/getPic")
    public void uploadFile(@RequestParam("file") MultipartFile file,
                           @RequestParam(required = true) String uuid,
                           HttpServletRequest request) {
        if (!isValidRequest(request))
            return;
        log.info("incoming upload request, check authentication code");
        UserInfo user = userRepo.getUserByUuid(UUID.fromString(uuid));
        try {
            if (user != null) {
                log.info("user matched ");
                fileUploadService.uploadFile(file, uuid);
            } else {
                log.error("error uploading, uuid nor found");
            }
        } catch (Exception e) {
            log.error("error uploading, uuid nor found");
            e.printStackTrace();
        }
    }


    ////////////// WebView

    @GetMapping("/v1/passwd")
    public int changePassword(Model model,
                              @RequestParam(required = true) String uuid,
                              @RequestParam(required = true) String oldPassword,
                              @RequestParam(required = true) String newPassword,
                              @RequestParam(required = true) String reNewPassword,

                              HttpServletRequest request,
                              HttpServletResponse response) throws IOException {
        UserInfo user = userRepo.getUserByUuid(UUID.fromString(uuid));
        try {
            if (user != null) {
                if (oldPassword.equals(newPassword)) {
                    log.warn("old and new password are same");
                    response.sendRedirect("/setting?error=" + OLD_NEW_PASSWORD_SAME);
                    return OLD_NEW_PASSWORD_SAME;
                }

                if (!newPassword.equals(reNewPassword)) {
                    log.error("user not found!, Internal error.");
                    response.sendRedirect("/setting?error=" + INPUT_IS_NOT_CORRECT);
                    return INPUT_IS_NOT_CORRECT;
                }

                try {
                    assert false;
                    if (passwordEncoder.matches(oldPassword, user.getPassword())) {
                        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12); // Strength set as 12
                        user.setPassword(encoder.encode(newPassword));
                        userRepo.save(user);
                        log.warn("password changed successfully.");
                        response.sendRedirect("/logout");
                        return RESPONSE_SUCCESS;
                    } else {
                        log.warn("error, password not matched.");
                        response.sendRedirect("/setting?error=" + OLD_PASSWORD_NOT_MATCHED);
                        return OLD_PASSWORD_NOT_MATCHED;
                    }

                } catch (Exception e) {
                    return RESPONSE_ERROR;
                }

            }
        } catch (Exception e) {
            log.warn("user not found!, Internal error.");
            response.sendRedirect("/setting?error=" + USER_NOT_FOUND_OR_NULL_ERROR);
            return USER_NOT_FOUND_OR_NULL_ERROR;
        }
        return -1;
    }


    @GetMapping("/v1/getConfig")
    public String getConfig(@RequestParam(required = true) String uuid,
                            HttpServletRequest request) {
        log.info("incoming getConfig, check authentication code");
        UserInfo user = userRepo.getUserByUuid(UUID.fromString(uuid));
        ParentalConfig parentalConfig = new ParentalConfig();
        try {
            if (user != null) {
                List<ParentalConfig> baseUser = parentalConfigRepo.findConfigByUuid(UUID.fromString(uuid));


                int imageQuality = baseUser.get(baseUser.size() - 1).getImageQuality();
                parentalConfig.setImageQuality(imageQuality);
                int screenShotDelay = baseUser.get(baseUser.size() - 1).getScreenShotDelay();
                parentalConfig.setScreenShotDelay(screenShotDelay);
                return gson.toJson(parentalConfig);
            } else {
                log.error("user null or empty, return default config");
                return "ERROR";
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Oppooos, exception. return default config");
            return gson.toJson(parentalConfig);
        }
    }

    @GetMapping("/v1/getCommand")
    public String getCommand(@RequestParam(required = true) String uuid,
                             HttpServletRequest request) {

        log.info("incoming getCommand, check authentication code");
        UserInfo user = userRepo.getUserByUuid(UUID.fromString(uuid));
        try {
            if (user != null) {
                List<Command> command;
                command = commandRepo.getLastCommand(UUID.fromString(uuid));
                String commandName = command.get(command.size() - 1).getCommandName();
                log.info("send command to client " + commandName);
                return commandName;
            } else {
                log.error("user null or empty, return stop command, parent should start manually.");
                return "stop";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "NOTHING";
        }
    }


    @PostMapping("/v1/kidsSetting")
    public int kidsControlSettings(@RequestParam(required = true) String screenShotDelay,
                                   @RequestParam(required = true) String imageQuality,
                                   @RequestParam(required = true) String uuid,
                                   HttpServletRequest request,
                                   HttpServletResponse response) throws IOException {
        UserInfo user = userRepo.getUserByUuid(UUID.fromString(uuid));
        try {
            if (user != null) {
                if (!screenShotDelay.isEmpty() && !imageQuality.isEmpty()) {
                    try {
                        log.info("new setting value imageQuality " + imageQuality + " screenShotDelay " + screenShotDelay);
                        ParentalConfig parentalConfig = new ParentalConfig();
                        parentalConfig.setScreenShotDelay(Integer.parseInt(screenShotDelay));
                        //todo change fixed number or dont show in UI.
                        parentalConfig.setImageQuality(15);
                        //parentalConfig.setImageQuality(Integer.parseInt(imageQuality));
                        parentalConfig.setUuid(user.getUuid());
                        parentalConfigRepo.save(parentalConfig);
                        response.sendRedirect("/");
                        return RESPONSE_SUCCESS;

                    } catch (Exception e) {
                        response.sendRedirect("/");
                        return RESPONSE_ERROR;
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("/");
            return RESPONSE_ERROR;
        }
        response.sendRedirect("/");
        return RESPONSE_ERROR;
    }

    // TODO: 1/28/21 change GET to POST, also control the input size
    // FIXME: 1/28/21  body size is a large in DB, cause 500 error, fix it
    @GetMapping("/v1/reg")
    public int registerNewUser(Model model,
                               @RequestParam(required = true) String username,
                               @RequestParam(required = true) String password,
                               @RequestParam(required = true) String rePassword,

                               HttpServletRequest request,
                               HttpServletResponse response) throws IOException {

        // TODO: 1/19/21 load/update user in memory cache for performance issue
        //todo check if password and repassword are same
        UserInfo qodQoDUserInfo = userRepo.findByUserName(username);
        // check for withspace
        Pattern pattern = Pattern.compile("\\s");
        Matcher matcher = pattern.matcher(username);
        boolean found = matcher.find();
        username = username.toLowerCase();
        if (found) {
            response.sendRedirect("/signup?error=" + SPACE_ERROR_USERNAME);
            return SPACE_ERROR_USERNAME;
        }
        if (qodQoDUserInfo != null || username.isEmpty()) {
            // TODO: 1/19/21  alert in signup page
            log.warn("user already registered.");
            response.sendRedirect("/signup?error=" + USER_EXIST);
            return USER_EXIST;
        }
        try {
            if (!password.equals(rePassword)) {
                response.sendRedirect("/signup?error=" + PASSWORD_NOT_SAME);
                return PASSWORD_NOT_SAME;
            }
            UserInfo userInfo = new UserInfo();
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12); // Strength set as 12
            String encodedPassword = encoder.encode(password);
            userInfo.setUserName(username);
            userInfo.setUuid(UUID.randomUUID());
            userInfo.setPassword(encodedPassword);
            userInfo.setCountry("نامشخص");
            userInfo.setCity("نامشخص");
            userInfo.setBio("این کاربر هنوز بیوگرافی را مشخص نکرده‌اند");
            userInfo.setStatus("نامشخص");
            userInfo.setFirstName("نامشخص");
            userInfo.setLastName("نامشخص");
            userInfo.setEmail("نامشخص");
            userInfo.setPhoneNumber("نامشخص");
            File userDir = new File(environment.getProperty("upload.path") + "user-photos/" + userInfo.getUuid());
            if (userDir.mkdir()) {
                log.info("create user profile dir success.");
                File file = new File(DEFAULT_AVATAR);
                String absolutePath = file.getAbsolutePath();
                Path src = Paths.get(absolutePath);
                String picturePathPrefix = userDir + "/picture";
                Path dst = Paths.get(picturePathPrefix);
                Files.copy(src, dst);
                Thumbnails.of(new File(picturePathPrefix))
                        .size(100, 100)
                        .outputFormat("jpg")
                        .toFiles(Rename.PREFIX_DOT_THUMBNAIL);

                // set profile picture
                Thumbnails.of(new File(picturePathPrefix))
                        .size(250, 250)
                        .outputFormat("jpg")
                        .toFiles(Rename.SUFFIX_HYPHEN_THUMBNAIL);
            }
            userRepo.save(userInfo);
//            User.initUser(environment.getProperty("upload.path"), userInfo.getUuid());

            // init user
            ParentalConfig parentalConfig = new ParentalConfig();
            parentalConfig.setCommand("start");
            parentalConfig.setUuid(userInfo.getUuid());
            parentalConfig.setImageQuality(15);
            parentalConfig.setScreenShotDelay(60);
            parentalConfigRepo.save(parentalConfig);

            Command command = new Command();
            command.setCommandName("start");
            command.setUserId(userInfo.getUuid());
            commandRepo.save(command);

            log.info("user register and initialized successfully.");
            response.sendRedirect("/login");
            return RESPONSE_SUCCESS;
        } catch (Exception e) {
            e.printStackTrace();
            return RESPONSE_SUCCESS;
        }
    }


    @PostMapping("/v1/get")
    public int newPost(@RequestParam(required = true) String subject,
                       @RequestParam(required = true) String body,
                       @RequestParam(required = false) MultipartFile video,
                       @RequestParam(required = false) MultipartFile image,
                       HttpServletRequest request,
                       HttpServletResponse response) throws IOException {
        // TODO: 2/6/21  get video thumbnail via background task inorder to  QoS enactment
        // TODO: 2/6/21 video thumbnail watermark

        if (video.getSize() > 150_000_000 || image.getSize() > 25_000_000) {
            log.error("file size max, reject request");
            response.sendRedirect("/");
            return RESPONSE_SUCCESS;
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserInfo userInfo = new UserInfo();
        userInfo = userRepo.findByUserName(auth.getName());
        try {
            Experience experience = new Experience();
            experience.setSubject(subject);
            experience.setBody(body);
            experience.setAuthorName(auth.getName());

            LocalDateTime now = LocalDateTime.now();
            experience.setTimeStamp(now);
            experience.setUuid(UUID.randomUUID());
            experience.setTags(utils.getTags(body));
            experience.setAuthorId(userInfo.getUuid());
            if (!video.isEmpty()) {
                if (video.getContentType().contains("video")) {
                    String fileName = StringUtils.cleanPath(Objects.requireNonNull(video.getOriginalFilename()));
                    experience.setVideo(Collections.singletonList(fileName));
                    String uploadPath = environment.getProperty("upload.path");
                    log.info("uploading video ... >>> " + uploadPath);

                    String uploadDir = uploadPath + "user-photos/" + experience.getUuid();
                    saveFile(uploadDir, video.getOriginalFilename(), video);
                    try {
                        String pp = Video.randomGrabberFFmpegImage(uploadDir + "/" + video.getOriginalFilename(), 2);
                        System.out.println(pp);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            if (!image.isEmpty()) {
                if (image.getContentType().contains("image")) {
                    String fileName = StringUtils.cleanPath(Objects.requireNonNull(image.getOriginalFilename()));
                    experience.setPhotos(Collections.singletonList(fileName));
                    String uploadPath = environment.getProperty("upload.path");
                    log.info("uploading image ... >>> " + uploadPath);
                    String uploadDir = uploadPath + "user-photos/" + experience.getUuid();
                    saveFile(uploadDir, image.getOriginalFilename(), image);

                    // set post thumbnail
                    Thumbnails.of(new File(uploadDir + "/" + image.getOriginalFilename()))
                            .size(250, 250)
                            .outputFormat("jpg")
                            .toFiles(Rename.PREFIX_DOT_THUMBNAIL);

                }
            }


            experienceRepo.save(experience);
            response.sendRedirect("/");
            return RESPONSE_SUCCESS;


        } catch (Exception e) {
            return RESPONSE_ERROR;
        }
    }

    @GetMapping("/v1/reply")
    public String replyPost(@RequestParam(required = true) String body,
                            @RequestParam(required = true) UUID uuid,
                            HttpServletRequest request,
                            HttpServletResponse response) throws IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserInfo userInfo = new UserInfo();
        userInfo = userRepo.findByUserName(auth.getName());
        if (uuid != null && !body.isEmpty()) {
            Reply reply = new Reply();
            reply.setUuid(UUID.randomUUID());
            reply.setRelatedPostUuid(uuid);
            reply.setRepliedUserId(userInfo.getUuid());
            reply.setTimeStamp(LocalDateTime.now().toString());
            reply.setBody(body);
            replyRepo.save(reply);
            // TODO: 1/19/21  show related reply and  load by message Id
            response.sendRedirect("/");
            return String.valueOf(RESPONSE_SUCCESS);
        } else {
            return TYPE_INVALID_ERROR;
        }
    }

    @GetMapping("/v1/like")
    public String likePost(@RequestParam(required = true) UUID uuid,
                           HttpServletRequest request,
                           HttpServletResponse response) throws IOException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!auth.isAuthenticated())
            log.info("user not authenticated.");
        if (uuid != null) {
            UserInfo userInfo = userRepo.findByUserName(auth.getName());
            if (likeRepo.isLiked(userInfo.getUuid(), uuid) != null) {
                response.sendRedirect("/search");
                log.info("user already liked the post");
                return String.valueOf(USER_ALREADY_LIKED);
            }
            Experience experience = new Experience();
            experience = experienceRepo.getPostByUUID(uuid);
            int likeValue = experience.getLiked();
            experienceRepo.updateLike(uuid, likeValue + 1);
            Liked liked = new Liked();
            liked.setPostId(uuid);
            liked.setUserId(userInfo.getUuid());
            likeRepo.save(liked);
            response.sendRedirect("/");
            return String.valueOf(RESPONSE_SUCCESS);
        } else {
            response.sendRedirect("/login");
            return TYPE_INVALID_ERROR;
        }
    }

    @GetMapping("/v1/dislike")
    public String disLikePost(@RequestParam(required = true) UUID uuid,
                              HttpServletRequest request,
                              HttpServletResponse response) throws IOException {
        if (uuid != null) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            UserInfo userInfo = userRepo.findByUserName(auth.getName());
            if (disLikedRepo.isDisLiked(userInfo.getUuid(), uuid) != null) {
                response.sendRedirect("/search");
                log.info("user already disliked the post");
                return String.valueOf(USER_ALREADY_LIKED);
            }
            Experience experience = new Experience();
            experience = experienceRepo.getPostByUUID(uuid);
            int likeValue = experience.getDisliked();
            experienceRepo.updateDisLike(uuid, likeValue + 1);
            DisLiked disLiked = new DisLiked();
            disLiked.setPostId(uuid);
            disLiked.setUserId(userInfo.getUuid());
            disLikedRepo.save(disLiked);
            response.sendRedirect("/");
            return String.valueOf(RESPONSE_SUCCESS);

        } else {
            response.sendRedirect("/login");
            return TYPE_INVALID_ERROR;
        }

    }

    @GetMapping("/v1/report")
    public String report(@RequestParam(required = true) UUID uuid,
                         @RequestParam(required = true) ReportType reportType,
                         HttpServletRequest request,
                         HttpServletResponse response) throws IOException {

        if (uuid != null && reportType != null) {
            log.info("get new report -> " + reportType);
            PostReport postReport = new PostReport();
            postReport.setUuid(uuid);
            postReport.setReportType(reportType.getReportCode());
            reportRepo.save(postReport);
            response.sendRedirect("/");
            return String.valueOf(RESPONSE_SUCCESS);
        } else {
            response.sendRedirect("/login");
            return TYPE_INVALID_ERROR;
        }
    }


    public static void saveFile(String uploadDir, String fileName,
                                MultipartFile multipartFile) throws IOException {

        Path uploadPath = Paths.get(uploadDir);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        try (InputStream inputStream = multipartFile.getInputStream()) {
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ioe) {
            throw new IOException("Could not save image file: " + fileName, ioe);
        }
    }

    @PostMapping("/v1/photo")
    public String uploadPhoto(@RequestParam(required = true) MultipartFile photo,
                              @RequestParam(required = false) MultipartFile headerPhoto,
                              HttpServletResponse response) throws IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        //todo check size and file type
        if (photo.getSize() >= 25_000_000) {
            log.error("uploaded image size too large.");
            // TODO: 1/29/21  redirect to custom error page for invalid data
            response.sendRedirect("/setting");
            return String.valueOf(FILE_SIZE_TOO_LARGE);
        }

        UserInfo userInfo = new UserInfo();
        userInfo = userRepo.findByUserName(auth.getName());
        String photoName = StringUtils.cleanPath(Objects.requireNonNull(photo.getOriginalFilename()));
        String headerPhotoName = StringUtils.cleanPath(Objects.requireNonNull(photo.getOriginalFilename()));
        userInfo.setProfileImage("picture");
        userInfo.setProfileHeaderImage("headerPhoto");
        userRepo.save(userInfo);
        String uploadPath = environment.getProperty("upload.path");
        log.info("path for uploading... >>> " + uploadPath);

        String uploadDir = uploadPath + "user-photos/" + userInfo.getUuid();
        if (headerPhoto != null && !headerPhoto.isEmpty()) {
            saveFile(uploadDir, "headerPhoto", headerPhoto);
//            Thumbnails.of(new File(uploadPath + "user-photos/" + userInfo.getUuid() + "/headerPhoto"))
//                    .size(1920, 300)
//                    .outputFormat("jpg")
//                    .toFiles(Rename.NO_CHANGE);
        }
        if (!photo.isEmpty()) {
            saveFile(uploadDir, "picture", photo);
            // TODO: 1/29/21 in order to service QoS enactment, do it stateless mode
            // set profile thumbnail
            Thumbnails.of(new File(uploadPath + "user-photos/" + userInfo.getUuid() + "/picture"))
                    .size(100, 100)
                    .outputFormat("jpg")
                    .toFiles(Rename.PREFIX_DOT_THUMBNAIL);

            // set profile picture
            Thumbnails.of(new File(uploadPath + "user-photos/" + userInfo.getUuid() + "/picture"))
                    .size(250, 250)
                    .outputFormat("jpg")
                    .toFiles(Rename.SUFFIX_HYPHEN_THUMBNAIL);
        }
        response.sendRedirect("/setting");
        return String.valueOf(RESPONSE_SUCCESS);
    }


    @PostMapping("/v1/saveMessage")
    public int postMan(@RequestParam(required = false) String subject,
                       @RequestParam(required = false) String body,
                       @RequestParam(required = false) UUID uuid,
                       HttpServletRequest request,
                       HttpServletResponse response) throws IOException {
        if (subject.isEmpty() && body.isEmpty() && uuid.toString().isEmpty()) {
            log.error("postmen error, null or empty content");
            response.sendRedirect("/");
            return INPUT_IS_NOT_CORRECT;
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        //todo inorder to QoS enhancement, service need a custom K,V that keep the all username and related UUID, update frequently
        UserInfo userInfo = userRepo.findByUserName(auth.getName());

        PrivateMessage privateMessage = new PrivateMessage();
        privateMessage.setSubject(subject);
        privateMessage.setBody(body);
        privateMessage.setReciever(uuid);
        privateMessage.setSenderUserName(userInfo.getUserName());
        privateMessage.setRead(false);
        privateMessage.setTimeStamp(LocalDateTime.now().toString());
        privateMessage.setSender(userInfo.getUuid());
        messageRepo.save(privateMessage);
        log.info("new message saved for " + uuid);
        response.sendRedirect("/");
        return RESPONSE_SUCCESS;
    }

    @GetMapping("/v1/flow")
    public int flowUser(@RequestParam(required = true) UUID uuid,
                        HttpServletRequest request,
                        HttpServletResponse response) throws IOException {
        if (uuid == null) {
            log.error("null or empty uuid.");
            response.sendRedirect("/");
            return USER_NOT_FOUND_OR_NULL_ERROR;
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        //todo inorder to QoS enhancement, service need a custom K,V that keep the all username and related UUID, update frequently
        UserInfo userInfo = userRepo.findByUserName(auth.getName());
        Flower flower = new Flower();
        if (userInfo.getUuid().equals(uuid)) {
            log.warn("user can not flow itself.");
            response.sendRedirect("/");
            return INPUT_IS_NOT_CORRECT;
        }
        if (flowerRepo.isFlowedBy(userInfo.getUuid()) != null) {
            log.warn("user already flowed this persian");
            response.sendRedirect("/");
            return INPUT_IS_NOT_CORRECT;

        }
        flower.setFlowerId(userInfo.getUuid());
        flower.setUserId(uuid);
        flower.setTimeStamp(LocalDateTime.now().toString());
        flower.setActive(true);
        flowerRepo.save(flower);
        response.sendRedirect("/");
        return RESPONSE_SUCCESS;
    }

    @PostMapping("/v1/contact")
    public String getContact(@RequestParam(required = true) String firstName,
                             @RequestParam(required = false) String lastName,
                             @RequestParam(required = true) String subject,
                             @RequestParam(required = true) String body,
                             @RequestParam(required = true) String phoneOrMail,
                             HttpServletResponse response,
                             HttpServletRequest request,
                             Model model) throws IOException {
        if (firstName == null &&
                lastName == null &&
                subject == null &&
                body == null &&
                phoneOrMail == null) {
            response.sendRedirect("/exbord?response=" + NULL_OR_EMPTY_MESSAGE);
            return String.valueOf(RESPONSE_SUCCESS);
        }

        Contact contactReq = new Contact();
        contactReq.setFirstName(firstName);
        contactReq.setLastName(lastName);
        contactReq.setSubject(subject);
        contactReq.setBody(body);
        contactReq.setTimeStamp(LocalDateTime.now());
        contactReq.setContact(phoneOrMail);
        contactReq.setUuid(UUID.randomUUID());
        contactReq.setDone(false);
        contactRepo.save(contactReq);

        //todo alert user if exbord get him/her contact request
        response.sendRedirect("/exbord?response=" + WE_GET_YOUR_MESSAGE + "&resCode=" + contactReq.getUuid());
        return String.valueOf(RESPONSE_SUCCESS);

    }

    @GetMapping("/v1/ver")
    public String version(HttpServletRequest request) {
        return VERSION;
    }


}
