package pl.rengreen.taskmanager.dataloader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import pl.rengreen.taskmanager.model.Role;
import pl.rengreen.taskmanager.model.User;
import pl.rengreen.taskmanager.service.RoleService;
import pl.rengreen.taskmanager.service.UserService;

@Component
public class InitialDataLoader implements ApplicationListener<ContextRefreshedEvent> {

    private final UserService userService;
    private final RoleService roleService;
    private final Logger logger = LoggerFactory.getLogger(InitialDataLoader.class);

    @Value("${default.admin.mail}")
    private String defaultAdminMail;
    @Value("${default.admin.name}")
    private String defaultAdminName;
    @Value("${default.admin.password}")
    private String defaultAdminPassword;
    @Value("${default.admin.image}")
    private String defaultAdminImage;

    @Autowired
    public InitialDataLoader(UserService userService, RoleService roleService) {
        this.userService = userService;
        this.roleService = roleService;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        // Check if roles already exist to avoid duplicates
        if (roleService.findAll().isEmpty()) {
            roleService.createRole(new Role("ADMIN"));
            roleService.createRole(new Role("USER"));
            roleService.findAll().stream()
                    .map(role -> "saved role: " + role.getRole())
                    .forEach(logger::info);
        }

        // Check if the default admin user already exists to avoid duplicates
        if (!userService.isUserEmailPresent(defaultAdminMail)) {
            User admin = new User(
                    defaultAdminMail,
                    defaultAdminName,
                    defaultAdminPassword,
                    defaultAdminImage);
            userService.createUser(admin);
            userService.changeRoleToAdmin(admin);
            logger.info("saved admin user: " + admin.getName());
        } else {
            logger.info("admin user already exists: " + defaultAdminMail);
        }
    }
}