package com.github.walkvoid.zone.ai.business.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 演示管理页入口。静态资源在 {@code classpath:/static/index.html}。
 */
@Controller
public class DemoPageController {

    @GetMapping("/admin")
    public String admin() {
        return "redirect:/index.html";
    }
}
