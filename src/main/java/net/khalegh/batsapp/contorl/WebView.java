package net.khalegh.batsapp.contorl;

import com.github.mfathi91.time.PersianDate;
import net.khalegh.batsapp.config.ParentalConfig;
import net.khalegh.batsapp.config.Service;
import net.khalegh.batsapp.dao.*;
import net.khalegh.batsapp.entity.*;
import net.khalegh.batsapp.utils;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
public class WebView {
    private static final Logger log = LoggerFactory.getLogger(WebView.class);
    private static final String ALL_TAGS = "allTags";
    private static final String ALL_USERS = "allUsers";
    private static final String ALL_COMMUNES = "allComments";
    private static final String IS_EMPTY_GAP = "IS_EMPTY_GAP";
    private static final int PAGINATION_COUNT = 128;
    private static final Object WE_NEED_YOUR_MESSAGE = "لطفا پیغام بگذارید...";
    private static final Object WE_GET_YOUR_MESSAGE = "متشکریم. پیغام شما با موفقیت ثبت شد.";
    private static final String FILL_REQUIRED_FILED = "لطفا فیلدهای درخواست شده در فرم را پر کنید";
    private static final String USERNAME_ALREADY_USED = "خطا: این نام کاربری قبلا استفاده شده است";
    private static final String SPACE_ERROR_USERNAME = "خطا: امکان استفاده از فاصله در نام کاربری مجاز نیست.";
    private static final String PASSWORD_ARE_NOT_MATCHED = "خطا: رمز عبور یکسان نیست.";
    private static final String INCORRECT_USERNAME_OR_PASSWORD = "خطا: نام کاربری یا کلمه عبور اشتباه است.";

    private static final SimpleDateFormat sdf = new SimpleDateFormat("HH");


    @Autowired
    ExperienceRepo experienceRepo;

    @Autowired
    UserRepo userRepo;

    @Autowired
    ReplyRepo replyRepo;

    @Autowired
    FlowerRepo flowerRepo;

    @Autowired
    MessageRepo messageRepo;

    @Autowired
    CommandRepo commandRepo;

    @Autowired
    ParentalConfigRepo parentalConfigRepo;

    @RequestMapping("/setCommand")
    public void setCommand(@RequestParam(required = true) String cmd,
                           HttpServletResponse response) throws IOException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!auth.isAuthenticated()) {
            response.sendRedirect("/login");
            return;
        }
        UserInfo userInfo = new UserInfo();
        userInfo = userRepo.findByUserName(auth.getName());
        log.info("incoming setCommand from " + userInfo.getUuid());
        Command command = new Command();
        if (cmd.equals("start") || cmd.equals("stop")) {
            command.setCommandName(cmd);
            command.setUserId(userInfo.getUuid());
            commandRepo.save(command);
            response.sendRedirect("/");
        }
    }

    @RequestMapping(value = {"", "/", "/index"})
    public String index(@RequestParam(required = false, defaultValue = "") String q,
                        @RequestParam(required = false, defaultValue = "0") int page,
                        @RequestParam(required = false) UUID userId,
                        @RequestParam(required = false) String subject,
                        HttpServletRequest request,
                        Model model) throws ExecutionException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        PersianDate today = PersianDate.now();

        String from = sdf.format(new Date());
        int kk = Integer.parseInt(from) + 1;
        String to = String.format("%2s", kk).replace(' ', '0');


        UserInfo userInfo = new UserInfo();
        UUID uuid;
        if (auth.isAuthenticated()) {
            model.addAttribute("username", auth.getName());
            userInfo = userRepo.findByUserName(auth.getName());
            uuid = userInfo.getUuid();
            log.info("user uuid -> " + uuid);
            List<Command> command;
            // initialize
            model.addAttribute("controlStatus", "unknown");
            try {
                command = commandRepo.getLastCommand(userInfo.getUuid());
                if (command != null) {
                    if (command.get(command.size() - 1).getCommandName().equals("start"))
                        model.addAttribute("controlStatus", "on");
                    else
                        model.addAttribute("controlStatus", "off");
                }
            } catch (Exception e) {
                //fixme nullPointerException
                e.printStackTrace();
                model.addAttribute("controlStatus", "off");
            }
            // get config
            List<ParentalConfig> config = parentalConfigRepo.findConfigByUuid(uuid);
            try {
                model.addAttribute("imageQuality", config.get(config.size() - 1).getImageQuality());
                model.addAttribute("screenShotDelay", config.get(config.size() - 1).getScreenShotDelay());

            } catch (Exception e) {
                e.printStackTrace();
                log.error("null or empty config, set default");
                model.addAttribute("imageQuality", 15);
                model.addAttribute("screenShotDelay", 60);
            }


        } else {
            model.addAttribute("username", "Guest");
        }


        if (request.getHeader("exbord") == null) {
            log.info("not mobile app");
            model.addAttribute("isMobile", "false");
        } else {
            model.addAttribute("isMobile", "true");
            log.info("Mobile application found, version -> " + request.getHeader("exbord"));
        }
        model.addAttribute("date", today);
        model.addAttribute("from", from);
        model.addAttribute("to", to);

        return "index";
    }


    @RequestMapping("/show")
    public String showActivities(@RequestParam(required = true) String date,
                                 @RequestParam(required = true) String from,
                                 @RequestParam(required = true) String to,
                                 Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        PersianDate today = PersianDate.now();
        final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        String time = dateFormat.format(new Date());

        try {
            if (!date.isEmpty())
                today = PersianDate.parse(date);
        } catch (Exception e) {
            // todo send  and show error in user  panel
            e.printStackTrace();
        }
        UserInfo userInfo;
        Map<Integer, String> images =
                new TreeMap<Integer, String>(Collections.reverseOrder());
        ArrayList<String> dirList = new ArrayList<>();
        if (auth.isAuthenticated()) {
            model.addAttribute("username", auth.getName());
            userInfo = userRepo.findByUserName(auth.getName());
            String home = System.getProperty("user.home");
            File file = new File(home + "/" + userInfo.getUuid());
            String[] directories = file.list(new FilenameFilter() {
                @Override
                public boolean accept(File current, String name) {
                    return new File(current, name).isDirectory();
                }
            });

            if (directories != null) {
                Arrays.stream(directories).forEach(dirList::add);
            }

            String imagePath = home + "/" + userInfo.getUuid() + "/" + today + "/" + from + "-" + to;
            if (Files.exists(Paths.get(imagePath))) {
                File f = new File(imagePath);
                String[] fileList = f.list();
                assert fileList != null;
                for (String item : fileList) {
                    images.put(Integer.valueOf(utils.extractFileNumber(item)),
                            userInfo.getUuid() + "/" + today  + "/" + from + "-" + to+ "/" + item);
                }
            }
        } else {
            model.addAttribute("username", "Guest");
        }
        dirList.sort(Collections.reverseOrder());
        model.addAttribute("images", images.entrySet());
        model.addAttribute("dayList", dirList);
        model.addAttribute("today", today);
        model.addAttribute("screenShotCount", images.size());
        model.addAttribute("time", time);
        return "show";
    }


    @RequestMapping("/setting")
    public String setting(Model model) throws ExecutionException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth.isAuthenticated()) {
            model.addAttribute("username", auth.getName());
            UserInfo baseUser = userRepo.findByUserName(auth.getName());
            model.addAttribute("uuid", baseUser.getUuid());
        } else {
            model.addAttribute("username", "Guest");
        }
        @Nullable String appCacheCanary = Service.appCache.get(IS_EMPTY_GAP);
        if (appCacheCanary == null || appCacheCanary.isEmpty()) {
            log.info("NullOrEmpty catch found, fetch db");
            Service.appCache.put(ALL_TAGS, String.valueOf(experienceRepo.count()));
            Service.appCache.put(ALL_USERS, String.valueOf(userRepo.count()));
            Service.appCache.put(ALL_COMMUNES, String.valueOf(replyRepo.count()));
            Service.appCache.put(IS_EMPTY_GAP, IS_EMPTY_GAP);
        }

        return "setting";
    }

    @RequestMapping("/search")
    public String searchExperience(@RequestParam(required = false, defaultValue = "") String q,
                                   @RequestParam(required = false, defaultValue = "0") int page,
                                   @RequestParam(required = false) UUID userId,
                                   Model model) throws ExecutionException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        int totalMatchedResult;
        if (page < 0)
            page = 0;

        if (auth.isAuthenticated()) {
            model.addAttribute("username", auth.getName());
        } else {
            model.addAttribute("username", "Guest");
        }

        @Nullable String hotCacheCanary = Service.hotCache.get(IS_EMPTY_GAP);
        if (hotCacheCanary == null || hotCacheCanary.isEmpty()) {
            log.info("NullOrEmpty hot catch, fetch db");
            List<Object[]> Obj = experienceRepo.getHotTags();
            for (Object[] row : Obj) {
                Service.hotCache.put(row[0].toString(), (row[1].toString()));
                Service.hotCache.put(IS_EMPTY_GAP, IS_EMPTY_GAP);
            }
        }

        @Nullable String appCacheCanary = Service.appCache.get(IS_EMPTY_GAP);
        if (appCacheCanary == null || appCacheCanary.isEmpty()) {
            log.info("NullOrEmpty catch found, fetch db");
            Service.appCache.put(ALL_TAGS, String.valueOf(experienceRepo.count()));
            Service.appCache.put(ALL_USERS, String.valueOf(userRepo.count()));
            Service.appCache.put(ALL_COMMUNES, String.valueOf(replyRepo.count()));
            Service.appCache.put(IS_EMPTY_GAP, IS_EMPTY_GAP);
        }

        model.addAttribute(ALL_TAGS, Service.appCache.get(ALL_TAGS));
        model.addAttribute(ALL_USERS, Service.appCache.get(ALL_USERS));
        model.addAttribute(ALL_COMMUNES, Service.appCache.get(ALL_COMMUNES));

        Map<String, String> trends = new HashMap<>();
        Service.hotCache.asMap().forEach((k, v) -> {
            if (!k.equals(IS_EMPTY_GAP))
                trends.put(k, v);
        });

        List<Experience> experienceList = new ArrayList<>();
        Pageable pageable = PageRequest.of(page, PAGINATION_COUNT);
        if (userId != null) {
            totalMatchedResult = experienceRepo.countUserPost(userId);
            experienceList = experienceRepo.findExperienceByUserId(userId, pageable);
        } else {
            if (q != null) {
                totalMatchedResult = experienceRepo.countMatchedItem(q);
                experienceList = experienceRepo.findByString(q, pageable);
                model.addAttribute("experienceCount", experienceRepo.countMatchedItem(q));
            } else {
                totalMatchedResult = (int) experienceRepo.count();
                experienceList = experienceRepo.getAllItems(pageable);
                model.addAttribute("experienceCount", (long) experienceList.size());

            }
        }
        model.addAttribute("trends", trends);
        model.addAttribute("experienceList", experienceList);
        log.info("total matched count > " + totalMatchedResult);
        if (page * PAGINATION_COUNT >= totalMatchedResult)
            model.addAttribute("nextPage", page);
        else
            model.addAttribute("nextPage", page + 1);

        model.addAttribute("q", q);
        if (page > 0)
            model.addAttribute("prevPage", page - 1);
        else
            model.addAttribute("prevPage", page);


        return "result";
    }

    @RequestMapping("/profile")
    public String getProfile(Model model,
                             @RequestParam(required = false) String username) throws ExecutionException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth.isAuthenticated()) {
            model.addAttribute("username", auth.getName());
        } else {
            model.addAttribute("username", "Guest");
        }
        if (username == null) {
            return "/";
        }
        @Nullable String appCacheCanary = Service.appCache.get(IS_EMPTY_GAP);
        if (appCacheCanary == null || appCacheCanary.isEmpty()) {
            log.info("NullOrEmpty catch found, fetch db");
            Service.appCache.put(ALL_TAGS, String.valueOf(experienceRepo.count()));
            Service.appCache.put(ALL_USERS, String.valueOf(userRepo.count()));
            Service.appCache.put(ALL_COMMUNES, String.valueOf(replyRepo.count()));
            Service.appCache.put(IS_EMPTY_GAP, IS_EMPTY_GAP);
        }
        model.addAttribute(ALL_TAGS, Service.appCache.get(ALL_TAGS));
        model.addAttribute(ALL_USERS, Service.appCache.get(ALL_USERS));
        model.addAttribute(ALL_COMMUNES, Service.appCache.get(ALL_COMMUNES));

        UserInfo userInfo = new UserInfo();
        userInfo = userRepo.findByUserName(username);
        int userPostCount = experienceRepo.countUserPost(userInfo.getUuid());
        model.addAttribute("user", userInfo);
        model.addAttribute("userPostCount", userPostCount);
        model.addAttribute("flower", flowerRepo.getFlowerCount(userInfo.getUuid()));
        Pageable pageable = PageRequest.of(0, 10);
        model.addAttribute("latestPost", experienceRepo.findExperienceByUserId(userInfo.getUuid(), pageable));
        return "profile";

    }

    @RequestMapping("/login")
    public String loginPage(@RequestParam(required = false, defaultValue = "0") String error,
                            Model model) {
        if (error.equals(String.valueOf(REST.ERROR_USER_OR_PASSWORD))) {
            model.addAttribute("msg", INCORRECT_USERNAME_OR_PASSWORD);
            return "signin";
        }
        return "signin";
    }

    @RequestMapping("/logout")
    public String logoutPage() {
        SecurityContextHolder.getContext().setAuthentication(null);
        SecurityContextHolder.clearContext();
        return "redirect:/";
    }

    @RequestMapping("/signup")
    public String register(@RequestParam(required = false, defaultValue = "0") String error,
                           Model model) throws ExecutionException {

        if (error.equals(String.valueOf(REST.USER_EXIST))) {
            model.addAttribute("msg", USERNAME_ALREADY_USED);
            return "signup";
        } else if (error.equals(String.valueOf(REST.PASSWORD_NOT_SAME))) {
            model.addAttribute("msg", PASSWORD_ARE_NOT_MATCHED);
            return "signup";
        } else if (error.equals(String.valueOf(REST.SPACE_ERROR_USERNAME))) {
            model.addAttribute("msg", SPACE_ERROR_USERNAME);
            return "signup";
        }
        @Nullable String appCacheCanary = Service.appCache.get(IS_EMPTY_GAP);
        if (appCacheCanary == null || appCacheCanary.isEmpty()) {
            log.info("NullOrEmpty catch found, fetch db");
            Service.appCache.put(ALL_TAGS, String.valueOf(experienceRepo.count()));
            Service.appCache.put(ALL_USERS, String.valueOf(userRepo.count()));
            Service.appCache.put(ALL_COMMUNES, String.valueOf(replyRepo.count()));
            Service.appCache.put(IS_EMPTY_GAP, IS_EMPTY_GAP);
        }
        model.addAttribute(ALL_TAGS, Service.appCache.get(ALL_TAGS));
        model.addAttribute(ALL_USERS, Service.appCache.get(ALL_USERS));
        model.addAttribute(ALL_COMMUNES, Service.appCache.get(ALL_COMMUNES));
        return "signup";
    }

    @RequestMapping("/share")
    public String newPost(Model model) throws ExecutionException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth.isAuthenticated()) {
            model.addAttribute("username", auth.getName());
        } else {
            model.addAttribute("username", "Guest");
        }
        @Nullable String appCacheCanary = Service.appCache.get(IS_EMPTY_GAP);
        if (appCacheCanary == null || appCacheCanary.isEmpty()) {
            log.info("NullOrEmpty catch found, fetch db");
            Service.appCache.put(ALL_TAGS, String.valueOf(experienceRepo.count()));
            Service.appCache.put(ALL_USERS, String.valueOf(userRepo.count()));
            Service.appCache.put(ALL_COMMUNES, String.valueOf(replyRepo.count()));
            Service.appCache.put(IS_EMPTY_GAP, IS_EMPTY_GAP);
        }
        model.addAttribute(ALL_TAGS, Service.appCache.get(ALL_TAGS));
        model.addAttribute(ALL_USERS, Service.appCache.get(ALL_USERS));
        model.addAttribute(ALL_COMMUNES, Service.appCache.get(ALL_COMMUNES));

        return "share";
    }

    @RequestMapping("/response")
    public String responsePost(Model model, @RequestParam(required = true) UUID uuid) throws ExecutionException {
        if (uuid == null) {
            return "/";
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        @Nullable String appCacheCanary = Service.appCache.get(IS_EMPTY_GAP);
        if (appCacheCanary == null || appCacheCanary.isEmpty()) {
            log.info("NullOrEmpty catch found, fetch db");
            Service.appCache.put(ALL_TAGS, String.valueOf(experienceRepo.count()));
            Service.appCache.put(ALL_USERS, String.valueOf(userRepo.count()));
            Service.appCache.put(ALL_COMMUNES, String.valueOf(replyRepo.count()));
            Service.appCache.put(IS_EMPTY_GAP, IS_EMPTY_GAP);
        }
        model.addAttribute(ALL_TAGS, Service.appCache.get(ALL_TAGS));
        model.addAttribute(ALL_USERS, Service.appCache.get(ALL_USERS));
        model.addAttribute(ALL_COMMUNES, Service.appCache.get(ALL_COMMUNES));

        List<Reply> replyList = replyRepo.findByString(uuid);
        if (auth.isAuthenticated()) {
            if (replyList != null)
                model.addAttribute("replyList", replyList);
            model.addAttribute("uuid", uuid);
            model.addAttribute("username", auth.getName());
        } else {
            model.addAttribute("username", "Guest");
        }
        return "reply";
    }

    @RequestMapping("/message")
    public String messageHandler(Model model, @RequestParam(required = true) UUID uuid) {
        if (uuid == null) {
            return "/";
        }
        model.addAttribute("receiver", uuid);
        return "message";
    }

    @RequestMapping("/inbox")
    public String inbox(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth.isAuthenticated()) {
            model.addAttribute("username", auth.getName());
        } else {
            model.addAttribute("username", "Guest");
        }
        UserInfo userInfo = userRepo.findByUserName(auth.getName());
        List<PrivateMessage> allMessage = messageRepo.getMessage(userInfo.getUuid());
        log.info("found  " + allMessage.size() + " message for " + userInfo.getUuid());
        model.addAttribute("inbox", allMessage);
        return "inbox";
    }

    @RequestMapping("/exbord")
    public String exbordPage(@RequestParam(required = false, defaultValue = "1") int response,
                             @RequestParam(required = false) UUID resCode,
                             Model model) {

        if (response == 0) {
            model.addAttribute("msg", FILL_REQUIRED_FILED);
            return "exbord";
        }
        if (response == 200 && resCode != null) {
            model.addAttribute("msg", WE_GET_YOUR_MESSAGE + " کد پیگیری:" + resCode);
            return "exbord";

        } else {
            model.addAttribute("msg", WE_NEED_YOUR_MESSAGE);
            return "exbord";

        }
    }


}
