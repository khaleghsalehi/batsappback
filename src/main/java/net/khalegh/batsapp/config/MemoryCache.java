package net.khalegh.batsapp.config;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;

import java.util.concurrent.TimeUnit;

@Configuration
public class MemoryCache {


    @Autowired
    Environment environment;


    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(environment.getProperty("upload.path") + "/user-photos/**").addResourceLocations("/res/");
    }

    private static final int CACHE_TTL_DEFAULT = 10;

    public static LoadingCache<String, String> appCache = CacheBuilder
            .newBuilder()
            .expireAfterWrite(CACHE_TTL_DEFAULT, TimeUnit.SECONDS)
            .build(new CacheLoader<String, String>() {
                @Override
                public String load(String s) throws Exception {
                    return "";
                }
            });

    public static LoadingCache<String, String> hotCache = CacheBuilder
            .newBuilder()
            .expireAfterWrite(CACHE_TTL_DEFAULT, TimeUnit.SECONDS)
            .build(new CacheLoader<String, String>() {
                @Override
                public String load(String s) throws Exception {
                    return "";
                }
            });


    public static LoadingCache<String, String> lastPing = CacheBuilder
            .newBuilder()
            .expireAfterWrite(CACHE_TTL_DEFAULT, TimeUnit.SECONDS)
            .build(new CacheLoader<String, String>() {
                @Override
                public String load(String s) throws Exception {
                    return "";
                }
            });
    public static LoadingCache<String, String> lastActivity = CacheBuilder
            .newBuilder()
            .build(new CacheLoader<String, String>() {
                @Override
                public String load(String s) throws Exception {
                    return "";
                }
            });

    public static LoadingCache<String, String> lastUpload = CacheBuilder
            .newBuilder()
            .build(new CacheLoader<String, String>() {
                @Override
                public String load(String s) throws Exception {
                    return "";
                }
            });

    public static LoadingCache<String, String> uploadCount = CacheBuilder
            .newBuilder()
            .build(new CacheLoader<String, String>() {
                @Override
                public String load(String s) throws Exception {
                    return "";
                }
            });

    public static LoadingCache<String, String> suspectedClients = CacheBuilder
            .newBuilder()
            .build(new CacheLoader<String, String>() {
                @Override
                public String load(String s) throws Exception {
                    return "";
                }
            });


    public static LoadingCache<String, String> versionList = CacheBuilder
            .newBuilder()
            .build(new CacheLoader<String, String>() {
                @Override
                public String load(String s) throws Exception {
                    return "";
                }
            });


    public static LoadingCache<String, String> OTP = CacheBuilder
            .newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build(new CacheLoader<String, String>() {
                @Override
                public String load(String s) throws Exception {
                    return "";
                }
            });

    public static LoadingCache<String, String> AuthenticatedByOTP =
            CacheBuilder.newBuilder()
                    .expireAfterWrite(48, TimeUnit.HOURS)
                    .build(new CacheLoader<String, String>() {
                        @Override
                        public String load(String s) throws Exception {
                            return "";
                        }
                    });


    public static LoadingCache<String, String> signupDoneByOTP =
            CacheBuilder.newBuilder()
                    .build(new CacheLoader<String, String>() {
                        @Override
                        public String load(String s) throws Exception {
                            return "";
                        }
                    });

    public static LoadingCache<String, String> signUpOTP =
            CacheBuilder.newBuilder()
                    .expireAfterWrite(5, TimeUnit.MINUTES)
                    .build(new CacheLoader<String, String>() {
                        @Override
                        public String load(String s) throws Exception {
                            return "";
                        }
                    });


    public static LoadingCache<String, String> SMS_DOT_IR_TokenKey = CacheBuilder
            .newBuilder()
            .expireAfterWrite(20, TimeUnit.MINUTES)
            .build(new CacheLoader<String, String>() {
                @Override
                public String load(String s) throws Exception {
                    return "";
                }
            });


    public static LoadingCache<String, String> botProtection =
            CacheBuilder.newBuilder()
                    .build(new CacheLoader<String, String>() {
                        @Override
                        public String load(String s) throws Exception {
                            return "";
                        }
                    });

}
