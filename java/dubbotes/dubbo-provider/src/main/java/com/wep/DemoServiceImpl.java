package com.wep;

import org.springframework.stereotype.Service;
import com.wep.service.DemoService;

@Service("demoService")
public class DemoServiceImpl implements DemoService {
    @Override
    public String sayHello(String name) {
        return name;
    }
}
