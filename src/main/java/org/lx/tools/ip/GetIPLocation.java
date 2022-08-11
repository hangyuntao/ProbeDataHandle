package org.lx.tools.ip;

import net.ipmarker.dbo.IpBM;
import net.ipmarker.memory.MemoryApi;

public class GetIPLocation {

    private static MemoryApi memoryApi;

    static {
        memoryApi = MemoryApi.create("D:\\dataCollect\\IPMarkerLoc20220703");
        if (memoryApi == null) {
            System.err.println("please put offline IPMarkerGEO_PRO");
        }
        memoryApi.printHead();
    }

    public static boolean isInLocation(String ip, String country, String province, String city, String county,
                                       String isp) {
        IPLocation location = get(ip);
        if (country != null && !country.equals(location.getCountry())) {
            return false;
        }
        if (province != null && !province.equals(location.getProvince())) {
            return false;
        }
        if (city != null && !city.equals(location.getCity())) {
            return false;
        }
        if (county != null && !county.equals(location.getCounty())) {
            return false;
        }
        if (isp != null && !isp.equals(location.getIsp())) {
            return false;
        }
        return true;
    }

    public static IPLocation get(String ip) {
        IpBM bm = memoryApi.query(ip);
        IPLocation location = new IPLocation();
        location.setIp(ip);
        location.setCountry(bm.getValue("country"));
        location.setProvince(bm.getValue("province"));
        location.setCity(bm.getValue("city"));
        location.setCounty(bm.getValue("county"));
        location.setIsp(bm.getValue("isp"));
        location.setIsp_name(bm.getValue("isp_name"));
        location.setLongitude(bm.getValue("longitude"));
        location.setLatitude(bm.getValue("latitude"));
        location.setAsNumber(bm.getValue("as_number"));
        location.setAsOrg(bm.getValue("isp_name"));
        location.setUser(bm.getValue("user"));
        return location;
    }

}
