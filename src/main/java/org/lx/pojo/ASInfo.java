package org.lx.pojo;

import lombok.Data;

@Data
public class ASInfo {

	private String aut;
	
	private String aut_name;
	
	private String org_name;
	
	private String isp;
	
	private String prefix;
	
	private String org_country;
	
	public ASInfo(){
		this.aut="*";
		this.aut_name="*";
		this.org_name="*";
		this.isp="*";
		this.prefix="*";
	}
	public ASInfo(String aut,String aut_name,String org_name){
		this.aut=aut;
		this.aut_name=aut_name;
		this.org_name=org_name;
		this.isp="*";
		this.prefix="*";
	}

	public String getAut() {
		return aut;
	}

	public void setAut(String aut) {
		this.aut = aut;
	}

	public String getAut_name() {
		return aut_name;
	}

	public void setAut_name(String aut_name) {
		this.aut_name = aut_name;
	}

	public String getOrg_name() {
		return org_name;
	}

	public void setOrg_name(String org_name) {
		this.org_name = org_name;
	}

	public String getIsp() {
		return isp;
	}

	public void setIsp(String isp) {
		this.isp = isp;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}
	
	
	public String getAllInfo(){
		return aut+"\t"+prefix+"\t"+isp+"\t"+aut_name+"\t"+org_name;
	}

	public String getASInfo(){
		return aut+"\t"+aut_name+"\t"+org_name;
	}
	
	
}
