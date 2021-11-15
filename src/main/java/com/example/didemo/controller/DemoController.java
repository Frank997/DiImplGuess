package com.example.didemo.controller;

import com.example.didemo.anno.Autowired;
import com.example.didemo.anno.Controller;
import com.example.didemo.anno.Path;
import com.example.didemo.service.DemoService;

@Controller("/demo")
public class DemoController {
    @Autowired
    private DemoService demoService;

    @Path("/method1")
    public String method1() {
        System.out.println("method1 got called");
        return demoService.doSth();
    }
}
