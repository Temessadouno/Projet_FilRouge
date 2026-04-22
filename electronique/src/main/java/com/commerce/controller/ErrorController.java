// controller/ErrorController.java
package com.commerce.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ErrorController {

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "error/access-denied"; // templates/error/access-denied.html
    }
}