package com.example.jianzhonghouduan.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/hello")
public class HelloWorld {
    @RequestMapping("/helloworld")
    public String HelloWorld(){
        return "Hello World!";
    }
}
