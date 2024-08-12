package eolopark.server.controller;

import eolopark.server.model.internal.Eolopark;
import eolopark.server.model.internal.User;
import eolopark.server.service.ParkService;
import eolopark.server.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
public class UserWebController {

    /* Attributes */
    private final UserService userService;
    private final ParkService parkService;
    private final PasswordEncoder passwordEncoder;

    /* Constructor */
    public UserWebController (UserService userService, ParkService parkService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.parkService = parkService;
        this.passwordEncoder = passwordEncoder;
    }

    /* Methods */
    @ModelAttribute
    public void addAttributes (Model model, HttpServletRequest request) {
        Principal principal = request.getUserPrincipal();
        if (principal != null) {
            model.addAttribute("logged", true);
            model.addAttribute("username", principal.getName());
            model.addAttribute("admin", request.isUserInRole("admin"));
        }
        else model.addAttribute("logged", false);
    }

    @RequestMapping ("/login")
    public String login () {return "login";}

    @RequestMapping ("/loginerror")
    public String loginerror () {
        return "loginerror";
    }

    @GetMapping ("/signup")
    public String signup () {
        return "new_user";
    }

    @PostMapping ("/signup")
    public String signup (Model model, User user) throws IllegalArgumentException {
        user.setName(userService.sanitizeString(user.getName()));
        user.setEncodedPassword(passwordEncoder.encode(userService.sanitizeString(user.getEncodedPassword())));
        userService.saveUser(user);
        return "saved_user";
    }

    @GetMapping ("/users")
    public String getAllUser (Model model, @PageableDefault (size = 5) Pageable page) {
        Page<User> users = userService.getAllUser(page);
        model.addAttribute("users", users);
        model.addAttribute("hasPrev", users.hasPrevious());
        model.addAttribute("hasNext", users.hasNext());
        model.addAttribute("nextPage", users.getNumber() + 1);
        model.addAttribute("prevPage", users.getNumber() - 1);
        model.addAttribute("lastPage", users.getTotalPages());
        return "user_index";
    }

    @GetMapping ("/users/{id}")
    public String getUser (Model model, @PathVariable long id, @PageableDefault (size = 5) Pageable page) {
        User user = userService.getUserById(id);
        Page<Eolopark> eoloparks = parkService.getParkByUserId(id, page);
        model.addAttribute("user", user);
        model.addAttribute("unlimitedParks", user.getRoles().contains("premium") || user.getRoles().contains("admin"));
        model.addAttribute("parks", eoloparks);
        model.addAttribute("hasPrev", eoloparks.hasPrevious());
        model.addAttribute("hasNext", eoloparks.hasNext());
        model.addAttribute("nextPage", eoloparks.getNumber() + 1);
        model.addAttribute("prevPage", eoloparks.getNumber() - 1);
        model.addAttribute("lastPage", eoloparks.getTotalPages());
        return "show_user";
    }

    @GetMapping ("/users/{id}/delete")
    public String deleteUser (@PathVariable long id) {
        userService.deleteUserById(id);
        return "deleted_user";
    }

    @GetMapping ("/users/{id}/upgrade")
    public String upgradeUser (@PathVariable long id) {
        userService.replaceUserById(id);
        return "upgraded_user";
    }
}
