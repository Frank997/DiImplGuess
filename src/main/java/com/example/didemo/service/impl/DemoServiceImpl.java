package com.example.didemo.service.impl;

import com.example.didemo.anno.Autowired;
import com.example.didemo.anno.Service;
import com.example.didemo.bean.DemoBean;
import com.example.didemo.service.DemoService;

@Service
public class DemoServiceImpl implements DemoService {
    @Autowired
    private DemoBean demoBean;

    public String doSth() {
        return "I am DemoServiceImpl.doStn() implementation. :::"+ demoBean.getName();
    }
}
