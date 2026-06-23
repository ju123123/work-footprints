package com.workfootprint.bootstrap;

import com.workfootprint.service.UserService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class DefaultUserInitializer implements ApplicationRunner {
    private final UserService userService;

    public DefaultUserInitializer(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void run(ApplicationArguments args) {
        userService.ensureDefaultUser();
    }
}

