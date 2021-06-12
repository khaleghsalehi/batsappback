package net.khalegh.batsapp.config;

import net.khalegh.batsapp.service.UserDetailService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.annotation.Resource;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {


    @Resource
    private UserDetailService userDetailsService;

    @Bean
    public DaoAuthenticationProvider authProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) {
        auth.authenticationProvider(authProvider());
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web
                .ignoring()
                .antMatchers("/v1/**","/nui/**", "/fa/**", "/assets/css/**",
                        "/assets/fonts/**",
                        "/assets/img/**",
                        "/assets/js/**",
                        "/assets/scss/**",
                        "/assets/swf/**",
                        "/assets/font-awesome.4.5.0/**",
                        "/assets/font-awesome/**",
                        "/user-photos/**",
                        "/Music/**");
    }

    @Override
    protected void configure(HttpSecurity httpSecurity)
            throws Exception {
        httpSecurity
                .cors()
                .and()
                .authorizeRequests()
                .antMatchers("/v1/getAuthKey/**", "/v1/setCommand/**", "/v1/getCommand/**", "/v1/startbot", "/v1/contact", "/exbord", "/v1/reg", "/v1/get", "/signup/**", "/search/**")
                .permitAll()
                .anyRequest()
                .authenticated()
                .and()
                .formLogin()
                .loginPage("/login")
                .failureUrl("/login?error=-1")
                .successForwardUrl("/")
                .permitAll()
                .and()
                .logout()
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login")
                .permitAll();

        httpSecurity.csrf()
                .ignoringAntMatchers("/search/**");
        httpSecurity.headers()
                .frameOptions()
                .sameOrigin();

    }

}
