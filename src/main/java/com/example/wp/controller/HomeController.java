package com.example.wp.controller;

import com.example.wp.model.UserEntity;
import com.example.wp.service.UserServiceImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {

    private final UserServiceImpl userService;
    private final PasswordEncoder passwordEncoder;

    public HomeController(UserServiceImpl userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }


    @GetMapping("/home")
    public String Home(){
        return "Home Page";
    }

    @GetMapping("/login")
    public String showLoginForm(){
        return "login-form";
    }

    @GetMapping("/register")
    public String showRegForm(){
        return "register-form";
    }

    @PostMapping("/register")
    public String regUser(@RequestParam String username, @RequestParam String password, Model model){
        if(userService.findByUsername(username).isPresent()){
            model.addAttribute("error", "User already exists!");
            return "register-form";
        }

        String encodedPw = passwordEncoder.encode(password);

        UserEntity newUser = new UserEntity();
        newUser.setUsername(username);
        newUser.setPassword(encodedPw);
        userService.saveuser(newUser);

        return "redirect:/login";
    }


}
