package com.maihe.cms.device.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author: wep
 * @since: 2019/07/30
 */
@Component
@Slf4j
public class SocketRunner implements CommandLineRunner {

    @Autowired
    private SocketProperties properties;

    @Override
    public void run(String... args) throws Exception {
        ServerSocket listener = null;
        Socket socket = null;
        listener = new ServerSocket(properties.getPort());
        /**
         * 　　ThreadPoolExecutor.AbortPolicy:丢弃任务并抛出RejectedExecutionException异常。（默认handle）
         *
         * 　　ThreadPoolExecutor.DiscardPolicy：也是丢弃任务，但是不抛出异常。
         *
         * 　　ThreadPoolExecutor.DiscardOldestPolicy：丢弃队列最前面的任务，然后重新尝试执行任务（重复此过程）
         *
         * 　　ThreadPoolExecutor.CallerRunsPolicy：由调用线程处理该任务
         *     LinkedBlockingQueue （未指定容量即为Integer.MAX_VALUE） Executors.newFixedThreadPool 使用
         *
         */
//        log.debug("corepool:{},poolmax:{},poolkeep:{}，queueInit：{}",
//                properties.getPoolCore(),properties.getPoolMax(),properties.getPoolKeep(),properties.getPoolQueueInit());
        ThreadPoolExecutor pool = new ThreadPoolExecutor(
                properties.getPoolCore(),
                properties.getPoolMax(),
                properties.getPoolKeep(),
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(properties.getPoolQueueInit()),
                new ThreadPoolExecutor.DiscardOldestPolicy());
        while (true){
            socket = listener.accept();
            pool.execute(new GatewayConnection(socket));
        }
    }
}
