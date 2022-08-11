package org.lx.pojo;

import java.util.ArrayList;
import java.util.List;

public class NmapInfo {
    private String ip;
    private String domain;
    private String os_detail;
    private String os_guess;
    private String device_type;
    private String running;
    private List<PortInfo> portInfos;
    
    public List<PortInfo> getOpenPortInfos() {
        List<PortInfo> list = new ArrayList<>();
        for (PortInfo portInfo : portInfos) {
            if ("open".equals(portInfo.state)) {
                list.add(portInfo);
            }
        }
        
        return list;
    }
    
    //传入的值为路由器、打印机及其他
    public String getOs(String type) {
        String os ;
        if ("路由器".equals(type) || "打印机".equals(type)) {
            if (os_detail != null) {
                os = os_detail.split(",")[0];
                return os;
            }
            if (os_guess != null) {
                String[] ss = os_guess.split(",");
                for (String s : ss) {
                    if (!s.trim().toLowerCase().contains("windows")) {
                        if (!s.contains("(")) {
                            return s;
                        }
                        return s.trim().substring(0, s.trim().indexOf("("));
                    }
                }
            }
            
        } else {
            if (os_detail != null) {
                os = os_detail.split(",")[0];
                return os;
            } if (os_guess != null) {
                os = os_guess.split(",")[0];
                if (!os.contains("(")) {
                    return os;
                }
                return os.trim().substring(0, os.trim().indexOf("("));
            }
        }
        return "none";
    }
    
    public String getRunning() {
        return running;
    }
    
    public void setRunning(String running) {
        this.running = running;
    }
    
    public String getIp() {
        return ip;
    }
    
    public void setIp(String ip) {
        this.ip = ip;
    }
    
    public String getDomain() {
        return domain;
    }
    
    public void setDomain(String domain) {
        this.domain = domain;
    }
    
    public String getOs_detail() {
        return os_detail;
    }
    
    public void setOs_detail(String os_detail) {
        this.os_detail = os_detail;
    }
    
    public String getOs_guess() {
        return os_guess;
    }
    
    public void setOs_guess(String os_guess) {
        this.os_guess = os_guess;
    }
    
    public String getDevice_type() {
        return device_type;
    }
    
    public void setDevice_type(String device_type) {
        this.device_type = device_type;
    }
    
    public List<PortInfo> getPortInfos() {
        return portInfos;
    }
    
    public void setPortInfos(List<PortInfo> portInfos) {
        this.portInfos = portInfos;
    }
    
    
}
