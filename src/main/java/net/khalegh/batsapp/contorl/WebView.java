package net.khalegh.batsapp.contorl;

import com.github.mfathi91.time.PersianDate;
import com.github.mfathi91.time.PersianMonth;
import com.google.common.hash.Hashing;
import net.khalegh.batsapp.config.ParentalConfig;
import net.khalegh.batsapp.config.Service;
import net.khalegh.batsapp.dao.*;
import net.khalegh.batsapp.entity.*;
import net.khalegh.batsapp.kids.SuspectedActivity;
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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

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
    private static final int SUSPENSION_MAX_POLICY = 1;


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
    SuspectedActivityRepo suspectedActivityRepo;

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
                //fixme nullable value when there is not  config.get(config.size() - 1).getImageQuality()
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
        int currentMonth = today.getMonthValue();
        model.addAttribute("month", currentMonth);

        return "index";
    }


    @RequestMapping("/suspect")
    public String showActivities(@RequestParam(required = true) String uuid,
                                 Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth.isAuthenticated()) {
            model.addAttribute("username", auth.getName());
            try {
                Service.suspectedClients.invalidate(uuid);
                List<SuspectedActivity> activity = suspectedActivityRepo.getByUuid(UUID.fromString(uuid));
                model.addAttribute("activity", activity);
                PersianDate today = PersianDate.now();

                String from = sdf.format(new Date());
                int kk = Integer.parseInt(from) + 1;
                String to = String.format("%2s", kk).replace(' ', '0');
                model.addAttribute("date", today);
                model.addAttribute("from", from);
                model.addAttribute("to", to);
                int currentMonth = today.getMonthValue();
                model.addAttribute("month", currentMonth);


                return "suspect";
            } catch (Exception e) {
                return "/";
            }
        } else {
            return "/";
        }
    }

    private static String changEnglish(String str) {
        str.replaceAll("۰", "0");
        str.replaceAll("۱", "1");
        str.replaceAll("۲", "2");
        str.replaceAll("۳", "3");
        str.replaceAll("۴", "4");
        str.replaceAll("۵", "5");
        str.replaceAll("۶", "6");
        str.replaceAll("۷", "7");
        str.replaceAll("۸", "8");
        str.replaceAll("۹", "9");
        return str;

    }

    @RequestMapping("/show")
    public String showActivities(@RequestParam(required = true) String date,
                                 @RequestParam(required = true) String from,
                                 @RequestParam(required = true) String to,
                                 Model model) {

        Authentication auth = SecurityContextHolder.getContext()
                .getAuthentication();
        ArrayList<String> times = new ArrayList<>();


        String originalRequestedDate = changEnglish(date);
        PersianDate today = PersianDate.now();
        final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        String time = dateFormat.format(new Date());

//        try {
//            if (!date.isEmpty())
//                today = PersianDate.parse(date);
//        } catch (Exception e) {
//            // todo send  and show error in user  panel
//            e.printStackTrace();
//        }


        UserInfo userInfo;
        Map<Integer, String> images =
                new TreeMap<Integer, String>(Collections.reverseOrder());
        ArrayList<String> dirList = new ArrayList<>();
        if (auth.isAuthenticated()) {
            model.addAttribute("username", auth.getName());
            userInfo = userRepo.findByUserName(auth.getName());

            // check if there is suspected alarm
            boolean isSuspectedUser = Service.suspectedClients
                    .asMap()
                    .containsKey(String.valueOf(userInfo.getUuid()));
            if (isSuspectedUser) {
                try {
                    int count = Integer.parseInt(Service.suspectedClients
                            .get(String.valueOf(userInfo.getUuid())));
                    if (count > SUSPENSION_MAX_POLICY) {
                        model.addAttribute("suspected", "yup");
                        model.addAttribute("uuid", userInfo.getUuid());
                    }
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }

            String home = System.getProperty("user.home");
            String uuidHash = utils.getUuidHash(String.valueOf(userInfo.getUuid()));
            File file = new File(home + "/" + uuidHash);
            String[] directories = file.list(new FilenameFilter() {
                @Override
                public boolean accept(File current, String name) {
                    return new File(current, name).isDirectory();
                }
            });

            if (directories != null) {
                Stream<String> latestItem = Arrays.stream(directories)
                        .sorted(Collections.reverseOrder());
                latestItem.forEach(dirList::add);
            }

            int startTime = Integer.parseInt(from);
            int endTime = Integer.parseInt(to);
            if (startTime > endTime) {
                int temp;
                temp = endTime;
                endTime = startTime;
                startTime = temp;

                String sTemp;
                sTemp = from;
                from = to;
                to = sTemp;
            }
            int diff = endTime - startTime;
            log.info("get from to diff ->" + diff);
            if (diff > 1) {
                for (int i = startTime; i < endTime; i++) {
                    String l1 = String.format("%2s", i).replace(' ', '0');
                    String l2 = String.format("%2s", i + 1).replace(' ', '0');
                    log.info("calc diff times -> " + l1 + "-" + l2);
                    String e = l1 + "-" + l2;
                    times.add(e);
                }
            } else {
                times.add(from + "-" + to);
            }
            times.forEach(s -> {
                String pathCandidate = s;

                String imagePath = home + "/" + uuidHash + "/" + today + "/" + pathCandidate;

                if (Files.exists(Paths.get(imagePath))) {
                    File f = new File(imagePath);
                    String[] fileList = f.list();
                    assert fileList != null;

                    for (String item : fileList) {
                        if (originalRequestedDate.equals(date)) {
                            images.put(Integer.valueOf(utils.extractFileNumber(item)),
                                    uuidHash + "/" + today + "/" + s + "/" + item);
                        }
                    }
                }
            });

        } else {
            model.addAttribute("username", "Guest");
        }
        System.out.println("ad");
        model.addAttribute("dayList", dirList);
        if (dirList.size() > 0) {
            model.addAttribute("images", images.entrySet());
            model.addAttribute("screenShotCount", images.size());
        } else {
            model.addAttribute("screenShotCount", "empty");
        }

        model.addAttribute("today", today);
        model.addAttribute("time", time);


        model.addAttribute("date", today);
        model.addAttribute("from", from);
        model.addAttribute("to", to);


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

        PersianDate today = PersianDate.now();

        String from = sdf.format(new Date());
        int kk = Integer.parseInt(from) + 1;
        String to = String.format("%2s", kk).replace(' ', '0');
        model.addAttribute("date", today);
        model.addAttribute("from", from);
        model.addAttribute("to", to);
        int currentMonth = today.getMonthValue();
        model.addAttribute("month", currentMonth);

        return "setting";
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
