package com.maihe.cms.device.config;

public class RequestPacket {

    public final int protocolVersion;
    public final String mac;
    public final byte[] encryptedData;

    public RequestPacket(int protocolVersion, final String mac, byte[] encryptedData){
        this.protocolVersion = protocolVersion;
        this.mac = mac;
        this.encryptedData = encryptedData;
    }

    @Override
    public String toString(){
        return String.format("protocolVersion: %s, mac: %s, encryptedDataLength: %s",
                protocolVersion, mac, encryptedData.length);
    }

}
