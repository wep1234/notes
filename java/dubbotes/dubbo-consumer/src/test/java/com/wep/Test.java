package com.wep;

import com.wep.service.DemoService;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Test {
    public static void main(String[] args) {
        try {
           // FileSystemXmlApplicationContext context=new FileSystemXmlApplicationContext("dubbo-consumer/src/main/resources/springmvc.xml");
            ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("springmvc.xml");
            context.start();
            DemoService demoService = (DemoService) context.getBean("demoService");
           System.out.println(demoService.sayHello("哈哈哈"));
            System.in.read();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("出错了："+e.getMessage());
        }
    }
}
