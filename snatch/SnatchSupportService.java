package com.maihe.cms.app.service.support;

import com.maihe.cms.app.service.OrderSnatchService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * 秒杀服务
 * @author: wep
 * @since: 2018/10/24
 */
@Slf4j
@Component
public class SnatchSupportService {

    private final BlockingQueue<SnatchRequest> snatchQueue = new ArrayBlockingQueue<>(3000);
    private final Object initSnatchHandlerSync = new Object();
    private volatile SnatchHandler snatchHandler;

    @Autowired
    private OrderSnatchService orderSnatchService;
    @Autowired
    private SnatchAsyncService snatchAsyncService;

    @PostConstruct
    public void init(){
        initSnatchHandler();
    }

    @PreDestroy
    public void destroy(){
        snatchHandler.shutdown();
    }

    private void initSnatchHandler(){
        final SnatchSupportService self = this;
        synchronized (this.initSnatchHandlerSync){
            if(this.snatchHandler == null || Thread.State.TERMINATED == snatchHandler.getState()){
                this.snatchHandler = new SnatchHandler(self, orderSnatchService, snatchAsyncService);
                this.snatchHandler.start();
            }
        }
    }

    public boolean offer(final SnatchRequest request){
        log.debug("Offer snatch request: {} Begin", request);
        // snatchHandler dead?
        if (!this.snatchHandler.isAlive()){
            initSnatchHandler();
        }
        if(this.snatchQueue.offer(request)){
            log.debug("Offer snatch request: {} OK", request);
            return true;
        }
        log.debug("Offer snatch request: {} Full", request);
        return false;
    }

    static class SnatchHandler extends Thread {
        static final Logger log = LoggerFactory.getLogger(SnatchSupportService.SnatchHandler.class);
        static final AtomicInteger threadCounter = new AtomicInteger(0);
        static final int batch  = 1000;

        final OrderSnatchService orderSnatchService;
        final SnatchSupportService snatchSupportService;
        final SnatchAsyncService snatchAsyncService;

        private volatile boolean stopped;

        SnatchHandler(SnatchSupportService snatchSupportService, OrderSnatchService orderSnatchService,
                    SnatchAsyncService snatchAsyncService) {
            this.snatchSupportService = snatchSupportService;
            this.orderSnatchService = orderSnatchService;
            this.snatchAsyncService = snatchAsyncService;
            setName("snatch-handler-" + threadCounter.getAndIncrement());
            setDaemon(true);
        }

        @Override
        public void run(){
            log.info("Snatch-Handler-Started");
            final BlockingQueue<SnatchRequest> q = this.snatchSupportService.snatchQueue;
            final List<SnatchRequest> snatchRequests = new ArrayList<>(batch);
            long batchStart = System.currentTimeMillis();
            for(SnatchRequest request = null; !this.stopped; ){
                try {
                    // 等待返回后不为null
                    if(request == null){
                        request = q.poll(100L, TimeUnit.MILLISECONDS);
                    }
                    final SnatchRequest newReq = request;
                    request = null;

                    // 不处理已超时或出错的
                    if (newReq != null && newReq.isCancelled()) {
                        continue;
                    }
                    if (newReq != null) {
                        snatchRequests.add(newReq);
                    }
                    // 延迟 - 增加批量大小 since 2018-11-16 pzp
                    if(newReq == null && (System.currentTimeMillis()-batchStart) < 500L){
                        continue;
                    }
                    /**
                     * 当队列里数据较少的时候，（newReq下次会为空），或者达到批量的时候
                     */
                    if (newReq == null || snatchRequests.size() >= batch) {
                        batchStart = System.currentTimeMillis();
                        final int size = snatchRequests.size();
                        if(size == 0){
                            // 等待下单请求
                            request = q.take();
                            continue;
                        }
                        orderSnatchService.snatchBatch(snatchRequests);
                        for (final SnatchRequest req : snatchRequests) {
                            req.setCause(SnatchRequest.SUSSCUSE_CAUSE);
                           snatchAsyncService.complete(req);
                        }
                        snatchRequests.clear();
                        // next-loop
                    }
                } catch (final Throwable cause){
                    batchStart = System.currentTimeMillis();
                    boolean isInterrupted = (cause instanceof InterruptedException);
                    if(isInterrupted && this.stopped){
                        // exit-handler
                        break;
                    }
                    log.error("Handle snatch requests error", cause);
                    for(final SnatchRequest req : snatchRequests){
                        req.setCause("系统繁忙，请稍后");
                        snatchAsyncService.complete(req);
                    }
                    snatchRequests.clear();
                    // next-loop
                }
            } // for
            log.info("Exit");
        }

        void shutdown(){
            this.stopped = true;
            this.interrupt();
        }

    }
}
