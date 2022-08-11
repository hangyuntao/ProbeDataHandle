package org.lx.tools.ip;

import com.alibaba.fastjson.JSON;
import net.ipip.ipdb.City;
import net.ipip.ipdb.CityInfo;
import net.ipip.ipdb.InvalidDatabaseException;

import java.io.IOException;

public class IPIPLocation {

	private static City db = null;
	static {
		try {
			db = new City("E:\\Analysis\\IPDatabase\\ipipfree.ipdb");
		} catch (InvalidDatabaseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static IPLocation getLocation(String ip) {
		try {

			CityInfo info = db.findInfo(ip, "CN");

			IPLocation location = new IPLocation();
			location.setIp(ip);
			location.setCountry(info.getCountryName());
			location.setProvince(info.getRegionName());
			location.setCity(info.getCityName());
			return location;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static boolean isUsedIP(String ip) {
		IPLocation location = getLocation(ip);
		if ("本机地址".equals(location.getCountry()) || "局域网".equals(location.getCountry())
				|| "保留地址".equals(location.getCountry())||"共享地址".equals(location.getCountry())||"本地链路".equals(location.getCountry())) {
			return false;
		}
		return true;
	}

	public static void main(String[] args) {
		System.out.println(JSON.toJSONString(getLocation("114.114.114.114")));
	}
}
