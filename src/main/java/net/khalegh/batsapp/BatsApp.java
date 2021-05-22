package net.khalegh.batsapp;

import net.khalegh.batsapp.sys.ParentSMSNotification;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Timer;
import java.util.TimerTask;

@SpringBootApplication
public class BatsApp implements CommandLineRunner {
    public static void main(String[] args) {
        SpringApplication.run(BatsApp.class, args);

    }

    @Override
    public void run(String... args) throws Exception {
        TimerTask timerTask = new ParentSMSNotification();
        Timer timer=new Timer(true);
        // evey 5 second send sms
        timer.scheduleAtFixedRate(timerTask,0,5*1000);

    }
}
