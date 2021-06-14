package net.khalegh.batsapp;

import net.khalegh.batsapp.dao.ScreenShotRepo;
import net.khalegh.batsapp.dao.UserRepo;
import net.khalegh.batsapp.entity.UserInfo;
import net.khalegh.batsapp.sys.ImageAnalyzer;
import net.khalegh.batsapp.sys.ParentSMSNotification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Timer;
import java.util.TimerTask;

@SpringBootApplication
public class BatsApp implements CommandLineRunner {

    @Autowired
    ScreenShotRepo screenShotRepo;

    @Autowired
    UserRepo userRepo;

    public static void main(String[] args) {
        SpringApplication.run(BatsApp.class, args);

    }

    @Override
    public void run(String... args) throws Exception {
        TimerTask timerTask = new ParentSMSNotification();
        Timer timer1 = new Timer(true);
        timer1.scheduleAtFixedRate(timerTask, 0, 30 * 1000);

        // inspect contents for  every user per 10 second, periodic
//
//        TimerTask timerTaskInspect = new ImageAnalyzer(screenShotRepo, userRepo);
//        Timer timer2 = new Timer(true);
//        timer2.scheduleAtFixedRate(timerTaskInspect, 0, 10 * 1000);


    }
}
