package com.github.walkvoid.zone.gateway.config;

import com.github.walkvoid.zone.common.model.BooleanEnum;
import com.github.walkvoid.zone.user.business.db.dao.UserInfoDAO;
import com.github.walkvoid.zone.user.model.entity.UserInfo;
import com.github.walkvoid.zone.user.model.enums.UserInfoStatusEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 数据初始化 — 确保 admin 用户密码为 BCrypt
 *
 * @author walkvoid
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserInfoDAO userInfoDAO;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserInfoDAO userInfoDAO, PasswordEncoder passwordEncoder) {
        this.userInfoDAO = userInfoDAO;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        UserInfo admin = userInfoDAO.selectByUsername("admin");
        if (admin == null) {
            // 新建 admin 用户
            admin = new UserInfo();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setNickname("管理员");
            admin.setEmail("admin@zone.com");
            admin.setIsAdmin(BooleanEnum.YES);
            admin.setStatus(UserInfoStatusEnum.ACTIVE);
            userInfoDAO.insert(admin);
            log.info("Admin user created with BCrypt password");
        } else if (admin.getPassword() == null
                || !admin.getPassword().startsWith("$2a$")) {
            // 密码还是明文或占位符，更新为 BCrypt
            admin.setPassword(passwordEncoder.encode("admin123"));
            userInfoDAO.updateById(admin);
            log.info("Admin password updated to BCrypt");
        } else {
            log.info("Admin user already initialized");
        }
    }
}
