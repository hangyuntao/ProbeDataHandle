package org.lx.tools.ip;

import org.lx.tools.ByteTool;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class IPUtil {

    public static byte[] ipStr2Bytes(String ip) {
        byte[] ipb = new byte[4];
        String[] ips = ip.split("\\.");
        if (ips.length == 4) {
            for (int j = 0; j < 4; j++) {
                ipb[j] = (byte) Short.parseShort(ips[j]);
            }
        }
        return ipb;
    }

    public static long ipStr2Long(String ip) {
        byte[] ipb = ipStr2Bytes(ip);
        long l = (0xff & ipb[0]);
        l = (l << 8) + (0xff & ipb[1]);
        l = (l << 8) + (0xff & ipb[2]);
        l = (l << 8) + (0xff & ipb[3]);
        return l;
    }

    public static String ipBytes2Str(byte[] ipb) {
        StringBuffer sb = new StringBuffer();
        for (int j = 0; j < 4; j++) {
            sb.append(0xff & ipb[j]);
            if (j < 3) {
                sb.append(".");
            }
        }
        return sb.toString();
    }

    public static String ipLong2Str(long ipl) {
        byte[] ipb = new byte[4];
        ipb[3] = (byte) ipl;
        ipl = ipl >> 8;
        ipb[2] = (byte) ipl;
        ipl = ipl >> 8;
        ipb[1] = (byte) ipl;
        ipl = ipl >> 8;
        ipb[0] = (byte) ipl;

        StringBuffer sb = new StringBuffer();
        for (int j = 0; j < 4; j++) {
            sb.append(0xff & ipb[j]);
            if (j < 3) {
                sb.append(".");
            }
        }
        return sb.toString();
    }

    public static long ipBytes2Long(byte[] ipb) {
        long l = (0xff & ipb[0]);
        l = (l << 8) + (0xff & ipb[1]);
        l = (l << 8) + (0xff & ipb[2]);
        l = (l << 8) + (0xff & ipb[3]);
        return l;
    }

    public static byte[] ipLong2Bytes(long ipl) {
        byte[] ipb = new byte[4];
        ipb[3] = (byte) ipl;
        ipl = ipl >> 8;
        ipb[2] = (byte) ipl;
        ipl = ipl >> 8;
        ipb[1] = (byte) ipl;
        ipl = ipl >> 8;
        ipb[0] = (byte) ipl;
        return ipb;
    }

    /**
     * 判断某个String是否为ipv4
     */
    public static boolean judgeIP(String ip) {
        if (ip == null || ip.isEmpty()) {
            return false;
        }
        Pattern p = Pattern.compile(
                "^(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])\\.(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])\\.(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])\\.(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])$");
        Matcher m = p.matcher(ip);
        boolean b = m.matches();
        return b;
    }

    public static List<String> calucate(String ip, int mask) {
        String ipBit = ByteTool.bytesToBit(IPUtil.ipStr2Bytes(ip));
        StringBuilder sb1 = new StringBuilder();
        StringBuilder sb2 = new StringBuilder();

        for (int i = 0; i < 32; i++) {
            if (i < mask) {
                sb1.append(ipBit.charAt(i));
                sb2.append(ipBit.charAt(i));
            } else {
                sb1.append("0");
                sb2.append("1");
            }
        }
        long long1 = Long.parseLong(sb1.toString(), 2);
        long long2 = Long.parseLong(sb2.toString(), 2);

        List<String> list = new ArrayList<String>();
        for (long i = long1 + 1; i < long2; i++) {
            list.add(IPUtil.ipLong2Str(i));
        }
        return list;
    }

    public static long[] calucateMaskRangeLong(String ip, int mask) {
        String ipBit = ByteTool.bytesToBit(IPUtil.ipStr2Bytes(ip));
        StringBuilder sb1 = new StringBuilder();
        StringBuilder sb2 = new StringBuilder();

        for (int i = 0; i < 32; i++) {
            if (i < mask) {
                sb1.append(ipBit.charAt(i));
                sb2.append(ipBit.charAt(i));
            } else {
                sb1.append("0");
                sb2.append("1");
            }
        }
        long long1 = Long.parseLong(sb1.toString(), 2);
        long long2 = Long.parseLong(sb2.toString(), 2);

        return new long[]{long1, long2};
    }

    public static String[] calucateMaskRange(String ip, int mask) {
        long[] ll = calucateMaskRangeLong(ip, mask);
        return new String[]{IPUtil.ipLong2Str(ll[0]), IPUtil.ipLong2Str(ll[1])};
    }

    public static String getIPA(String ip) {
        return ip.substring(0, ip.indexOf("."));
    }
}
