package org.lx.data;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DNSDistr {
	
	private String ip;
	
	private String type;
	
	private int qwNo=0;
	
	private int dgNo=0;
	
	private int kfNo=0;
	
	private int ipv6No=0;
	
	private int activeNo=0;
	
	private int measureCount=0;
	
	private Set<String> dateSet=new HashSet<>();
	
	private double allTime=0;
	
	private double allCount=0;
	
	private double corrCount=0;
	
	private double allAnalysis=0;
	
	private String active_record;
	
	private String active_count;
	
	private Map<String, String> rep_map;
	
	public Map<String, String> getRep_map() {
		return rep_map;
	}

	public void setRep_map(Map<String, String> rep_map) {
		this.rep_map = rep_map;
	}

	public String getActive_count() {
		return active_count;
	}

	public void setActive_count(String active_count) {
		this.active_count = active_count;
	}

	public String getActive_record() {
		return active_record;
	}

	public void setActive_record(String active_record) {
		this.active_record = active_record;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public int getQwNo() {
		return qwNo;
	}

	public void setQwNo(int qwNo) {
		this.qwNo = qwNo;
	}

	public int getDgNo() {
		return dgNo;
	}

	public void setDgNo(int dgNo) {
		this.dgNo = dgNo;
	}

	public int getKfNo() {
		return kfNo;
	}

	public void setKfNo(int kfNo) {
		this.kfNo = kfNo;
	}

	public int getIpv6No() {
		return ipv6No;
	}

	public void setIpv6No(int ipv6No) {
		this.ipv6No = ipv6No;
	}

	public int getActiveNo() {
		return activeNo;
	}

	public void setActiveNo(int activeNo) {
		this.activeNo = activeNo;
	}

	public Set<String> getDateSet() {
		return dateSet;
	}

	public void setDateSet(Set<String> dateSet) {
		this.dateSet = dateSet;
	}

	public double getAllTime() {
		return allTime;
	}

	public void setAllTime(double allTime) {
		this.allTime = allTime;
	}

	public double getAllCount() {
		return allCount;
	}

	public void setAllCount(double allCount) {
		this.allCount = allCount;
	}

	public double getCorrCount() {
		return corrCount;
	}

	public void setCorrCount(double corrCount) {
		this.corrCount = corrCount;
	}

	public double getAllAnalysis() {
		return allAnalysis;
	}

	public void setAllAnalysis(double allAnalysis) {
		this.allAnalysis = allAnalysis;
	}

	public int getMeasureCount() {
		return measureCount;
	}

	public void setMeasureCount(int measureCount) {
		this.measureCount = measureCount;
	}
	
	
	
	
	

}
