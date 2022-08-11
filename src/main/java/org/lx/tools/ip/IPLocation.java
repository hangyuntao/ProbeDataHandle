package org.lx.tools.ip;

import lombok.Data;

@Data
public class IPLocation {

	private String ip;
	private String country;
	private String province;
	private String city;
	private String county;
	private String address;
	private String isp;
	private String isp_name;
	private String asNumber;
	private String asOrg;
	private String longitude;
	private String latitude;
	private String user;

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getProvince() {
		return province;
	}

	public void setProvince(String province) {
		this.province = province;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getCounty() {
		return county;
	}

	public void setCounty(String county) {
		this.county = county;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getIsp() {
		return isp;
	}

	public void setIsp(String isp) {
		this.isp = isp;
	}

	public String getIsp_name() {
		return isp_name;
	}

	public void setIsp_name(String isp_name) {
		this.isp_name = isp_name;
	}

	public String getAsNumber() {
		return asNumber;
	}

	public void setAsNumber(String asNumber) {
		this.asNumber = asNumber;
	}

	public String getAsOrg() {
		return asOrg;
	}

	public void setAsOrg(String asOrg) {
		this.asOrg = asOrg;
	}

	public String getLongitude() {
		return longitude;
	}

	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}

	public String getLatitude() {
		return latitude;
	}

	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	@Override
	public String toString() {
		return country + "\t" + province + "\t" + city + "\t" + county;
	}

}
