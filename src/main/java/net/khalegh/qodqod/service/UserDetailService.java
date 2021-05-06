package net.khalegh.qodqod.service;

import net.khalegh.qodqod.dao.UserRepo;
import net.khalegh.qodqod.entity.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailService implements UserDetailsService {
    @Autowired
    private UserRepo userRepo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserInfo userInfo = userRepo.findByUserName(username);
        if (userInfo == null) {
            throw new UsernameNotFoundException(username);
        }
        return new MyUserPrinciple(userInfo);
    }
}
