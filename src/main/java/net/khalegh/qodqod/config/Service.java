package net.khalegh.qodqod.config;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;

import java.util.concurrent.TimeUnit;

@Configuration

public class Service {
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
}
