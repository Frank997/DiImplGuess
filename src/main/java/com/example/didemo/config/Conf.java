package com.example.didemo.config;

import com.example.didemo.anno.Bean;
import com.example.didemo.anno.Configure;
import com.example.didemo.bean.DemoBean;

@Configure
public class Conf {
    @Bean
    public DemoBean getDemoBean() {
        DemoBean demoBean = new DemoBean();
        demoBean.setId(123);
        demoBean.setName("A DEMO BEAN");
        return demoBean;
    }
}
