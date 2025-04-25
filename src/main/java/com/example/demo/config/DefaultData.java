package com.example.demo.config;


import com.example.demo.constant.RoleName;
import com.example.demo.entity.Hour;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.repository.HourRepo;
import com.example.demo.repository.RoleRepo;
import com.example.demo.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class DefaultData implements ApplicationRunner {
    private final RoleRepo roleRepo;
    private final UserRepo userRepo;
    private final PasswordEncoder encoder;
    private final HourRepo hourRepo;
    @Override
    public void run(ApplicationArguments args) throws Exception {
        Role role = roleRepo.findByName(RoleName.ROLE_ADMIN);
        if(role==null) {
            role = new Role(RoleName.ROLE_ADMIN,"Manage all system");
            roleRepo.save(role);
        }
        Role role1 = roleRepo.findByName(RoleName.ROLE_DOCTOR);
        if(role1==null) {
            role1 = new Role(RoleName.ROLE_DOCTOR,"Doctor");
            roleRepo.save(role1);
        }
        Role role2 = roleRepo.findByName(RoleName.ROLE_PATIENT);
        if(role2==null) {
            role2 = new Role(RoleName.ROLE_PATIENT,"Patient");
            roleRepo.save(role2);
        }
        User user = userRepo.findByUsername("admin");
        if(user == null) {
            user = new User();
            user.setUsername("admin");
            user.setPassword(encoder.encode("123456"));
            user.setRole(role);
            user.setEnabled(true);
            userRepo.save(user);
        }
        createHours();
    }
    private void createHours() {
        Hour hour1 = new Hour("7h - 8h");
        Hour hour2 = new Hour("8h - 9h");
        Hour hour3 = new Hour("9h - 10h");
        Hour hour4 = new Hour("10h - 11h");
        Hour hour5 = new Hour("13h - 14h");
        Hour hour6 = new Hour("14h - 15h");
        Hour hour7 = new Hour("15h - 16h");
        Hour hour8 = new Hour("16h - 17h");
        if(hourRepo.findById(1L).isEmpty()) {
            hourRepo.saveAll(List.of(hour1,hour2,hour3,hour4,hour5,hour6,hour7,hour8));
        }
    }
}
