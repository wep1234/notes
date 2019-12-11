package com.maihe.cms.device.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author: wep
 * @since: 2019/07/30
 */
@ConfigurationProperties(prefix = "socket")
@Data
@Component
public class SocketProperties {

    private Integer port;
    private Integer poolKeep;
    private Integer poolCore;
    private Integer poolMax;
    private Integer poolQueueInit;
}
