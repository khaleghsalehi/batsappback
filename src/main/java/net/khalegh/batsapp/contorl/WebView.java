package net.khalegh.batsapp.contorl;

import com.github.mfathi91.time.PersianDate;
import com.sun.org.apache.xpath.internal.operations.Mod;
import net.khalegh.batsapp.config.ParentalConfig;
import net.khalegh.batsapp.config.MemoryCache;
import net.khalegh.batsapp.dao.*;
import net.khalegh.batsapp.entity.*;
import net.khalegh.batsapp.inspection.ContentType;
import net.khalegh.batsapp.kids.SuspectedActivity;
import net.khalegh.batsapp.service.Security;
import net.khalegh.batsapp.utils;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

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
    private static final String REGISTER_DONE = "پیام: ثبت نام با موفقیت انجام شد!";
    private static final Object PHONE_FORMAT_ERROR = "شماره وارد شده صحیح نیست";

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

    @Autowired
    ScreenShotRepo screenShotRepo;

    boolean checkIfAuthorizedByOTP(String userName) throws ExecutionException, IOException {
        if (!MemoryCache.AuthenticatedByOTP.asMap().containsKey(userName)) {
            Security.sendSMS(userName);
            log.info(userName + " need OTP verification, redirect... ");
            return false;
        }
        log.info(userName + "has been verified by OTP  at ",
                MemoryCache.AuthenticatedByOTP.get(userName));
        return true;
    }

    @RequestMapping("/welcome")
    public String welcomeAfterSignUp(){
        return "welcome";
    }
    @RequestMapping("/signup2")
    public String signup2(@RequestParam(required = true) String username,
                          Model model,
                          HttpServletResponse response) throws IOException, ExecutionException {
        model.addAttribute("username", username);
        return "signup2";
    }

    @RequestMapping("/signupOTP")
    public String signupOTP(@RequestParam(required = true) String username,
                            Model model,
                            HttpServletResponse response) throws IOException, ExecutionException {
        model.addAttribute("username", username);
        return "signupOTP";
    }

    @RequestMapping("/setCommand")
    public void setCommand(@RequestParam(required = true) String cmd,
                           HttpServletResponse response) throws IOException, ExecutionException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!auth.isAuthenticated()) {
            response.sendRedirect("/login");
            return;
        }
        String userName = auth.getName();
        if (!checkIfAuthorizedByOTP(userName)) {
            response.sendRedirect("/");
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
                        Model model) throws ExecutionException, IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        PersianDate today = PersianDate.now();

        String from = sdf.format(new Date());
        int kk = Integer.parseInt(from) + 1;
        String to = String.format("%2s", kk).replace(' ', '0');


        UserInfo userInfo = new UserInfo();
        UUID uuid;
        if (auth.isAuthenticated()) {

            String userName = auth.getName();
            if (!checkIfAuthorizedByOTP(userName)) {
                model.addAttribute("userName", userName);
                return "auth";
            }


            model.addAttribute("username", userName);
            userInfo = userRepo.findByUserName(userName);
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
        model.addAttribute("uuid", userInfo.getUuid());

        return "index";
    }


    /**
     * parents ask content control manually
     *
     * @param uuid
     * @return
     */
    @RequestMapping("/imageAnalyze")
    public String imageAnalyzer(@RequestParam(required = true) String uuid,
                                @RequestParam(required = false, defaultValue = "") String type,
                                Model model) throws ExecutionException, IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth.isAuthenticated()) {

            String userName = auth.getName();
            if (!checkIfAuthorizedByOTP(userName)) {
                model.addAttribute("userName", userName);
                return "auth";
            }


            Optional<List<ScreenShot>> pornCount = screenShotRepo.getType(UUID.fromString(uuid),
                    ContentType.PORN);
            Optional<List<ScreenShot>> hentaiCount = screenShotRepo.getType(UUID.fromString(uuid),
                    ContentType.HENTAI);
            Optional<List<ScreenShot>> sexCount = screenShotRepo.getType(UUID.fromString(uuid),
                    ContentType.SEXY);

            Optional<List<ScreenShot>> neutralCount = screenShotRepo.getType(UUID.fromString(uuid),
                    ContentType.NEUTRAL);

            Optional<List<ScreenShot>> drawingCount = screenShotRepo.getType(UUID.fromString(uuid),
                    ContentType.DRAWING);

            model.addAttribute("pornCount", 0);
            model.addAttribute("hentaiCount", 0);
            model.addAttribute("sexCount", 0);
            model.addAttribute("drawingCount", 0);
            model.addAttribute("neutralCount", 0);

            log.info("=============== REPORT ==============================");
            if (pornCount.isPresent()) {
                int size = pornCount.get().size();
                log.info("pornCount " + size);
                model.addAttribute("pornCount", size);

            }
            if (hentaiCount.isPresent()) {
                int size = hentaiCount.get().size();
                log.info("hentaiCount " + size);
                model.addAttribute("hentaiCount", size);

            }
            if (sexCount.isPresent()) {
                int size = sexCount.get().size();
                log.info("sexCount " + size);
                model.addAttribute("sexCount", size);

            }

            if (drawingCount.isPresent()) {
                int size = drawingCount.get().size();
                log.info("drawingCount " + size);
                model.addAttribute("drawingCount", size);
            }


            if (neutralCount.isPresent()) {
                int size = neutralCount.get().size();
                log.info("neutralCount " + size);
                model.addAttribute("neutralCount", size);
            }
            log.info("=============== REPORT ==============================");

            Optional<List<ScreenShot>> images = screenShotRepo.getType(UUID.fromString(uuid),
                    ContentType.valueOf(type));
            HashMap<String, String> imageList = new HashMap<>();

            String home = System.getProperty("user.home");

            if (images.isPresent()) {
                images.get().forEach(screenShot -> {
                    //WTF!!!!!!!! fix the home path!
                    String fileName = screenShot.getFileName().replace(home, "");
                    imageList.put(fileName, fileName);
                });
                model.addAttribute("images", imageList);
            }


            model.addAttribute("uuid", uuid);

            PersianDate today = PersianDate.now();

            String from = sdf.format(new Date());
            int kk = Integer.parseInt(from) + 1;
            String to = String.format("%2s", kk).replace(' ', '0');

            model.addAttribute("date", today);
            model.addAttribute("from", from);
            model.addAttribute("to", to);
            int currentMonth = today.getMonthValue();
            model.addAttribute("month", currentMonth);

            UserInfo userInfo = userRepo.findByUserName(auth.getName());
            model.addAttribute("uuid", userInfo.getUuid());
            model.addAttribute("username", userInfo.getUserName());

            if (userInfo.isPermitAIService())
                model.addAttribute("parentsPermit", true);
            else
                model.addAttribute("parentsPermit", false);
            return "analyze";
        } else {
            model.addAttribute("username", "Guest");
        }
        return "analyze";
    }

    @RequestMapping("/permit_ai")
    public ModelAndView permitAIService(@RequestParam(required = true) String uuid,
                                        @RequestParam(required = true, defaultValue = "false") boolean allow,
                                        Model model,
                                        HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth.isAuthenticated() && allow) {
            userRepo.permitAIService(UUID.fromString(uuid));
            log.info("permitAIService enabled for uuid -> " + uuid);
            return new ModelAndView("redirect:/imageAnalyze?uuid=" + uuid+"&type=UNKNOWN");
        }
        return new ModelAndView("redirect:/");

    }

    @RequestMapping("/suspect")
    public String showActivities(@RequestParam(required = true) String uuid,
                                 Model model) throws ExecutionException, IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth.isAuthenticated()) {

            String userName = auth.getName();
            if (!checkIfAuthorizedByOTP(userName)) {
                model.addAttribute("userName", userName);
                return "auth";
            }

            model.addAttribute("username", auth.getName());
            try {
                MemoryCache.suspectedClients.invalidate(uuid);
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

    public static String changEnglish(String str) {
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
                                 Model model) throws ExecutionException, IOException {

        Authentication auth = SecurityContextHolder.getContext()
                .getAuthentication();
        ArrayList<String> times = new ArrayList<>();


        String originalRequestedDate = changEnglish(date);
        PersianDate today = PersianDate.now();
        final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        String time = dateFormat.format(new Date());


        UserInfo userInfo;
        Map<Integer, String> images =
                new TreeMap<Integer, String>(Collections.reverseOrder());
        ArrayList<String> dirList = new ArrayList<>();
        if (auth.isAuthenticated()) {

            String userName = auth.getName();
            if (!checkIfAuthorizedByOTP(userName)) {
                model.addAttribute("userName", userName);
                return "auth";
            }

            model.addAttribute("username", auth.getName());
            userInfo = userRepo.findByUserName(auth.getName());

            // check if there is suspected alarm
            boolean isSuspectedUser = MemoryCache.suspectedClients
                    .asMap()
                    .containsKey(String.valueOf(userInfo.getUuid()));
            if (isSuspectedUser) {
                try {
                    int count = Integer.parseInt(MemoryCache.suspectedClients
                            .get(String.valueOf(userInfo.getUuid())));
                    if (count > SUSPENSION_MAX_POLICY) {
                        model.addAttribute("suspected", "yes");
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
    public String setting(Model model) throws ExecutionException, IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth.isAuthenticated()) {
            String userName = auth.getName();
            if (!checkIfAuthorizedByOTP(userName)) {
                model.addAttribute("userName", userName);
                return "auth";
            }


            model.addAttribute("username", auth.getName());
            UserInfo baseUser = userRepo.findByUserName(auth.getName());
            model.addAttribute("uuid", baseUser.getUuid());
        } else {
            model.addAttribute("username", "Guest");
        }
        @Nullable String appCacheCanary = MemoryCache.appCache.get(IS_EMPTY_GAP);
        if (appCacheCanary == null || appCacheCanary.isEmpty()) {
            log.info("NullOrEmpty catch found, fetch db");
            MemoryCache.appCache.put(ALL_TAGS, String.valueOf(experienceRepo.count()));
            MemoryCache.appCache.put(ALL_USERS, String.valueOf(userRepo.count()));
            MemoryCache.appCache.put(ALL_COMMUNES, String.valueOf(replyRepo.count()));
            MemoryCache.appCache.put(IS_EMPTY_GAP, IS_EMPTY_GAP);
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
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth.isAuthenticated()) {
            String userName = auth.getName();
            MemoryCache.AuthenticatedByOTP.invalidate(userName);
            log.debug("invalidate user authorized by OTP -> " + userName);
        }
        SecurityContextHolder.getContext().setAuthentication(null);
        SecurityContextHolder.clearContext();
        return "redirect:/";
    }

    @RequestMapping("/signup")
    public String register(@RequestParam(required = false, defaultValue = "0") String error,
                           HttpServletResponse response,
                           Model model) throws ExecutionException, IOException {
        //generate bot token and add to cache
        UUID botToken = UUID.randomUUID();
        model.addAttribute("botToken", botToken);
        MemoryCache.botProtection.put(String.valueOf(botToken), String.valueOf(botToken));

        if (error.equals(String.valueOf(REST.USER_EXIST))) {
            model.addAttribute("msg", USERNAME_ALREADY_USED);
            return "signup";
        } else if (error.equals(String.valueOf(REST.PASSWORD_NOT_SAME))) {
            model.addAttribute("msg", PASSWORD_ARE_NOT_MATCHED);
            return "signup";
        } else if (error.equals(String.valueOf(REST.SPACE_ERROR_USERNAME))) {
            model.addAttribute("msg", SPACE_ERROR_USERNAME);
            return "signup";
        } else if (error.equals(String.valueOf(REST.INPUT_IS_NOT_CORRECT))) {
            model.addAttribute("msg", PHONE_FORMAT_ERROR);
            error = "0";
            return "signup";
        }
        @Nullable String appCacheCanary = MemoryCache.appCache.get(IS_EMPTY_GAP);
        if (appCacheCanary == null || appCacheCanary.isEmpty()) {
            log.info("NullOrEmpty catch found, fetch db");
            MemoryCache.appCache.put(ALL_TAGS, String.valueOf(experienceRepo.count()));
            MemoryCache.appCache.put(ALL_USERS, String.valueOf(userRepo.count()));
            MemoryCache.appCache.put(ALL_COMMUNES, String.valueOf(replyRepo.count()));
            MemoryCache.appCache.put(IS_EMPTY_GAP, IS_EMPTY_GAP);
        }
        model.addAttribute(ALL_TAGS, MemoryCache.appCache.get(ALL_TAGS));
        model.addAttribute(ALL_USERS, MemoryCache.appCache.get(ALL_USERS));
        model.addAttribute(ALL_COMMUNES, MemoryCache.appCache.get(ALL_COMMUNES));
        return "signup";
    }


    @RequestMapping("/response")
    public String responsePost(Model model, @RequestParam(required = true) UUID uuid) throws ExecutionException {
        if (uuid == null) {
            return "/";
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        @Nullable String appCacheCanary = MemoryCache.appCache.get(IS_EMPTY_GAP);
        if (appCacheCanary == null || appCacheCanary.isEmpty()) {
            log.info("NullOrEmpty catch found, fetch db");
            MemoryCache.appCache.put(ALL_TAGS, String.valueOf(experienceRepo.count()));
            MemoryCache.appCache.put(ALL_USERS, String.valueOf(userRepo.count()));
            MemoryCache.appCache.put(ALL_COMMUNES, String.valueOf(replyRepo.count()));
            MemoryCache.appCache.put(IS_EMPTY_GAP, IS_EMPTY_GAP);
        }
        model.addAttribute(ALL_TAGS, MemoryCache.appCache.get(ALL_TAGS));
        model.addAttribute(ALL_USERS, MemoryCache.appCache.get(ALL_USERS));
        model.addAttribute(ALL_COMMUNES, MemoryCache.appCache.get(ALL_COMMUNES));

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
