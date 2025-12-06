package com.example.filtertoday.common;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class IndexController {

    @GetMapping("/")
    public String mainPage() {
        return "index"; // templates/index.html을 찾아가라!
    }
}
