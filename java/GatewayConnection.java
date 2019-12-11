package com.maihe.cms.device.config;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.util.IOUtils;
import com.maihe.cms.core.utils.AESDataUtil;
import com.maihe.cms.core.utils.SpringContextHolder;
import com.maihe.cms.exception.RuleViolatedException;
import com.maihe.cms.service.hzz.FactoryDeviceService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * 设备网关连接。
 *
 * @author wep
 * @since 2019/07/30
 *
 */
@Slf4j
public class GatewayConnection implements Runnable {

    private static final int BUF_SZIE = 8192;

    private Socket socket;
    private boolean isLogined;

    public GatewayConnection(Socket socket){
        this.socket = socket;
    }

    private FactoryDeviceService service = SpringContextHolder.getBean(FactoryDeviceService.class);

    private RequestPacket readPacket(InputStream inputStream) throws IOException {
        // version
        int protocolVersion = inputStream.read();
        if(protocolVersion == -1){
            throw new EOFException();
        }
        if(protocolVersion != 1){
            throw new IOException("Protocol version error: " + protocolVersion);
        }

        // mac
        StringBuilder sbuf = new StringBuilder();
        for(int i = 0; i < 6; ++i){
            final int c = inputStream.read();
            if(c == -1){
                throw new EOFException();
            }
            sbuf.append(String.format("%02X", c));
        }
        final String mac = sbuf.toString();

        // {encrypted-data}
        final ByteArrayOutputStream encryptedData = new ByteArrayOutputStream();
        int c = inputStream.read();
        if(c != '{'){
            throw new IOException("The start of encrypted data: " + (char)c);
        }
        int i;
        for(i = 0; i < BUF_SZIE + 1/* } */; ++i){
            c = inputStream.read();
            if(c == -1){
                throw new EOFException();
            }
            if(c == '}'){
                break;
            }
            encryptedData.write(c);
        }
        if(i > BUF_SZIE){
            throw new IOException("The encrypted data too long");
        }

        return new RequestPacket(protocolVersion, mac, encryptedData.toByteArray());
    }

    private Map<String,Object> handle(RequestPacket requestPacket){
        Map<String,Object> map = new HashMap<>(4);
        String key = "";
        if(requestPacket.protocolVersion == 1){
            // 1. 生成秘钥
            key = requestPacket.mac.substring(0,8);
            key = key + key;
            // 2. 解密数据
            String data = AESDataUtil.Decrypt(requestPacket.encryptedData,key);
            // 3. 处理数据
            if (StringUtils.isBlank(data)) {
                throw new RuleViolatedException("the data is empty");
            }else{
                data = "{" + data + "}";
                JSONObject jsonObject = JSONObject.parseObject(data);
                String aggrement = jsonObject.getString("A");
                log.debug("decryptData:{}",jsonObject.toString());
                byte[] macBytes = new byte[6];
                if(StringUtils.isNotBlank(aggrement)){
                    //设备心跳
                    if("1".equals(aggrement)){
                        map.put("needSend",true);
                        String mac = requestPacket.mac;
                        for(int i = 0; i < 6; ++i){
                            String once = mac.substring(i*2,i*2+2);
                            macBytes[i] =  (byte)Integer.parseInt(once,16);
                        }
                        String encryptedData = "\"A\":\"2\"";
                        encryptedData = AESDataUtil.Encrypt(encryptedData,key);
                        map.put("protocolVersion",1);
                        map.put("mac",macBytes);
                        map.put("encryptedData",encryptedData);
                    }else if("3".equals(aggrement)){
                        //设备数据包
                        /**采集盒编号*/
                        String hid = jsonObject.getString("HID");
                        /**设备状态 01正常 02 故障 推送给相应厂家 13 暂停 暂时不处理*/
                        String state = jsonObject.getString("Data");
                        if("01".equals(state) || "02".equals(state)|| "04".equals(state)){
                            service.recodeLog(hid,state);
                        }
                        map.put("needSend",false);
                    }else{
                        map.put("needSend",false);
                    }
                    return map;
                }else{
                    throw new RuleViolatedException("the err agreement");
                }
            }
        }else{
            return null;
        }
    }

    @Override
    public void run() {
        OutputStream writer = null;
        try {
            // 设置读超时90秒
            socket.setSoTimeout(90 * 1000);
            log.debug("dev connected: {}", socket.getRemoteSocketAddress());

            InputStream inputStream = new BufferedInputStream(socket.getInputStream(), BUF_SZIE);
            writer = new BufferedOutputStream(socket.getOutputStream());

            for(;;){
                // 1. 读包
                final RequestPacket packet = readPacket(inputStream);
                log.debug("request packet: {}", packet);
                // 2. 处理
                Map<String,Object> result = handle(packet);
                //3.返回数据
                log.debug("needSend:{},encryptedData:{}",result.get("needSend"),result.get("encryptedData"));
                if(result !=null && (Boolean) result.get("needSend")){
                    writer.write((int)result.get("protocolVersion"));
                    byte[] mac = (byte[]) result.get("mac");
                    writer.write(mac);
                    writer.write('{');
                    writer.write(((String)result.get("encryptedData")).getBytes("UTF-8"));
                    writer.write('}');
                    writer.flush();
                }
            }

        } catch(RuleViolatedException e){
            log.error("errmsg:{},stockAddress:{}",e.getMessage(),socket.getRemoteSocketAddress());
        } catch (IOException e) {
            log.error("IOException",e);
        } finally {
            IOUtils.close(writer);
            IOUtils.close(socket);
        }
    }

}
