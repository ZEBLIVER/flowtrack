package com.study.FlowTrack.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RootController {

    @GetMapping("/")
    public String welcome() {
        return "FlowTrack API is up and running! Auth endpoints are at /api/auth";
    }
}
