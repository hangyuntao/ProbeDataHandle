package org.lx.pojo;

import lombok.Data;

import java.util.List;

@Data
public class PortInfo {
	
	//端口号
	public String port;
	//服务类别
	public String type;
	//协议类别 TCP/UDP
	public String protocal;
	//端口状态
	public String state;
	//服务版本
	public String version;
	
	public String banner;
	//漏洞编号列表
	public List<String> vul;
	
	public PortInfo(){
		
	}
	
	public PortInfo(String port,String protocal,String state,String type,String version,String banber){
		this.port=port;
		this.protocal=protocal;
		this.state=state;
		this.type=type;
		this.version=version;
		this.banner=banber;
		
	}

	public List<String> getVul() {
		return vul;
	}

	public void setVul(List<String> vul) {
		this.vul = vul;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getPort() {
		return port;
	}
	public void setPort(String port) {
		this.port = port;
	}
	public String getBanner() {
		return banner;
	}
	public void setBanner(String banner) {
		this.banner = banner;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getProtocal() {
		return protocal;
	}
	public void setProtocal(String protocal) {
		this.protocal = protocal;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	
}
