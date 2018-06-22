package com.wep;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Test {
    public static void main(String[] args) {
        try {
            //FileSystemXmlApplicationContext context=new FileSystemXmlApplicationContext("dubbo-provider/src/main/resources/springmvc.xml");
            ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("springmvc.xml");
            context.start();
            System.out.println("Dubbo provider start...");
            System.in.read();   // 按任意键退出
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("3333");
        }
    }
}
