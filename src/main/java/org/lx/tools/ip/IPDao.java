package org.lx.tools.ip;


import java.io.*;
import java.math.BigInteger;
import java.net.InetAddress;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class IPDao {

    static String ipReg = "((?:(?:25[0-5]|2[0-4]\\d|((1\\d{2})|([1-9]?\\d)))\\.){3}(?:25[0-5]|2[0-4]\\d|((1\\d{2})|([1-9]?\\d))))";
    static Pattern ipp = Pattern.compile(ipReg);

    /**
     * 获取ip网段
     */
    public static String GetIPwd(String ip) {
        String[] arr = ip.split("\\.");
        return arr[0] + "." + arr[1] + "." + arr[2];
    }


    public static String getLineIP(String s) {
        String ip = "";
        String[] arr = s.split("\\s|\\(|\\)");
        for (String a : arr) {
            Matcher m = ipp.matcher(a);//正则匹配看是否为ipv4地址
            if (m.matches()) {
                ip = a;
                break;
            }
        }
        return ip;

    }

    /**
     * ip转成长整型
     */
    public static long IPToLong(String ipString) {
        long result = 0;
        StringTokenizer token = new StringTokenizer(ipString, ".");
        result += Long.parseLong(token.nextToken()) << 24;
        result += Long.parseLong(token.nextToken()) << 16;
        result += Long.parseLong(token.nextToken()) << 8;
        result += Long.parseLong(token.nextToken());
        return result;
    }

    public static long ipToLong(String strIp) {
        long[] ip = new long[4];
        // 先找到IP地址字符串中.的位置
        int position1 = strIp.indexOf(".");
        int position2 = strIp.indexOf(".", position1 + 1);
        int position3 = strIp.indexOf(".", position2 + 1);
        // 将每个.之间的字符串转换成整型
        ip[0] = Long.parseLong(strIp.substring(0, position1));
        ip[1] = Long.parseLong(strIp.substring(position1 + 1, position2));
        ip[2] = Long.parseLong(strIp.substring(position2 + 1, position3));
        ip[3] = Long.parseLong(strIp.substring(position3 + 1));
        return (ip[0] << 24) + (ip[1] << 16) + (ip[2] << 8) + ip[3];
    }

    /**
     * 长整型转成ip
     */
    public static String longToIp(long ipLong) {
        StringBuilder sb = new StringBuilder();
        sb.append(ipLong >>> 24);
        sb.append(".");
        sb.append(String.valueOf((ipLong & 0x00FFFFFF) >>> 16));
        sb.append(".");
        sb.append(String.valueOf((ipLong & 0x0000FFFF) >>> 8));
        sb.append(".");
        sb.append(String.valueOf(ipLong & 0x000000FF));
        return sb.toString();
    }

    /**
     * 获取某个网段里所有c类网段
     */
    public static List<String> GetAllIp(String ip_start, String ip_end) {
        List<String> ls = new ArrayList<String>();
        String ip1 = GetIPwd(ip_end) + ".255";
        String ip2 = GetIPwd(ip_start) + ".255";
        long l1 = IPToLong(ip2);
        long l2 = IPToLong(ip_end);
        long no = l1;
        while (l1 <= no && no < l2 && !ip1.equals(ip2)) {
            String ip = longToIp(no);
            ls.add(ip);
            no += 256;
        }
        ls.add(ip_end);
        return ls;

    }

    /**
     * 获取某个网段里所有c类网段
     */
    public static List<String> GetAllIp1(String ip_start, String ip_end) {
        List<String> ls = new ArrayList<String>();
        String ip1 = GetIPwd(ip_end) + ".255";
        String ip2 = GetIPwd(ip_start) + ".255";
        long l1 = IPToLong(ip2);
        long l2 = IPToLong(ip1);
        long no = l1;
        while (l1 <= no && no <= l2) {
            String ip = longToIp(no);
            ls.add(ip);
            no += 256;
        }
        return ls;
    }

    /**
     * 获取某个网段里所有c类网段
     */
    public static List<String> GetAllIp2(String ip_start, String ip_end) {
        List<String> ls = new ArrayList<String>();
        long l1 = IPToLong(ip_start);
        long l2 = IPToLong(ip_end);
        long no = l1;
        while (l1 <= no && no <= l2) {
            String ip = longToIp(no);
            String ip_wd = GetIPwd(ip) + ".255";
            long wd = IPToLong(ip_wd);
            if (wd <= l2) {
                ls.add(ip + "-255");
            } else {
                ls.add(ip + "-" + ip_end.substring(ip_end.lastIndexOf(".") + 1, ip_end.length()));
            }
            no = wd + 1;
        }
        return ls;

    }

    /**
     * 获取某个网段里所有c类网段
     */
    public static List<String> DivideIPC(String ip_start, String ip_end) {
        List<String> ls = new ArrayList<String>();
        long l1 = IPToLong(ip_start);
        long l2 = IPToLong(ip_end);
        if (l1 <= l2) {
            long no = l1;
            while (l1 <= no && no <= l2) {
                String ip = longToIp(no);
                String ip_wd = GetIPwd(ip) + ".255";
                long wd = IPToLong(ip_wd);
                if (wd <= l2) {
                    ls.add(ip + "\t" + ip_wd);
                } else {
                    ls.add(ip + "\t" + ip_end);
                }
                no = wd + 1;
            }
        }
        return ls;
    }

    /**
     * 判断某个String是否为ipv4
     */
    public static boolean JudgeIP(String ip) {
        Pattern p = Pattern.compile(
                "^(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])\\.(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])\\.(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])\\.(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])$");
        Matcher m = p.matcher(ip);
        boolean b = m.matches();
        return b;
    }

    public static boolean JudgeIPv6(String ipv6) {

        Pattern p = Pattern.compile("^((([0-9A-Fa-f]{1,4}:){7}[0-9A-Fa-f]{1,4})|(([0-9A-Fa-f]{1,4}:){1,7}:)|(([0-9A-Fa-f]{1,4}:){6}:[0-9A-Fa-f]{1,4})|(([0-9A-Fa-f]{1,4}:){5}(:[0-9A-Fa-f]{1,4}){1,2})|(([0-9A-Fa-f]{1,4}:){4}(:[0-9A-Fa-f]{1,4}){1,3})|(([0-9A-Fa-f]{1,4}:){3}(:[0-9A-Fa-f]{1,4}){1,4})|(([0-9A-Fa-f]{1,4}:){2}(:[0-9A-Fa-f]{1,4}){1,5})|([0-9A-Fa-f]{1,4}:(:[0-9A-Fa-f]{1,4}){1,6})|(:(:[0-9A-Fa-f]{1,4}){1,7})|(([0-9A-Fa-f]{1,4}:){6}(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3})|(([0-9A-Fa-f]{1,4}:){5}:(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3})|(([0-9A-Fa-f]{1,4}:){4}(:[0-9A-Fa-f]{1,4}){0,1}:(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3})|(([0-9A-Fa-f]{1,4}:){3}(:[0-9A-Fa-f]{1,4}){0,2}:(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3})|(([0-9A-Fa-f]{1,4}:){2}(:[0-9A-Fa-f]{1,4}){0,3}:(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3})|([0-9A-Fa-f]{1,4}:(:[0-9A-Fa-f]{1,4}){0,4}:(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3})|(:(:[0-9A-Fa-f]{1,4}){0,5}:(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}))$");
        Matcher m = p.matcher(ipv6);
        boolean b = m.matches();
        return b;
    }

    /**
     * 将ip转为二进制
     */
    public static String IPToBinary(String ip) {
        ip = ip.replaceAll("﻿", "");
        String[] arr = ip.split("\\.");
        String s1 = Integer.toBinaryString(Integer.parseInt(arr[0]));
        String s2 = Integer.toBinaryString(Integer.parseInt(arr[1]));
        String s3 = Integer.toBinaryString(Integer.parseInt(arr[2]));
        String s4 = Integer.toBinaryString(Integer.parseInt(arr[3]));
        return s1 + "." + s2 + "." + s3 + "." + s4;
    }

    /**
     * 将二进制转为ip
     */
    public static String BinaryToIP(String Binary) {
        Binary = Binary.replaceAll("﻿", "");
        String[] arr = Binary.split("\\.");
        String s1 = Integer.valueOf(arr[0], 2).toString();
        String s2 = Integer.valueOf(arr[1], 2).toString();
        String s3 = Integer.valueOf(arr[2], 2).toString();
        String s4 = Integer.valueOf(arr[3], 2).toString();
        return s1 + "." + s2 + "." + s3 + "." + s4;
    }

    public static String GetDomain(String ip) {
        String host = "*";
        try {
            // 使用IP创建对象

            InetAddress inet2 = InetAddress.getByName(ip);

            System.out.println(inet2);

            // 获得对象中存储的域名

            host = inet2.getHostName();

            System.out.println("域名：" + host);

        } catch (Exception e) {

        }
        return host;

    }

    public static long getIPNo(String dir) throws Exception {
        long count = 0;
        BufferedReader rd = new BufferedReader(new FileReader(dir));
        String s;
        while ((s = rd.readLine()) != null) {
            s = s.replaceAll("﻿", "");
            String[] arr = s.split("\t");
            count += IPDao.ipToLong(arr[1]) - IPDao.ipToLong(arr[0]) + 1;
        }
        System.out.println(count);
        rd.close();
        return count;
    }

    public static void orderIPList(List<String> ls) {
        Collections.sort(ls, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                String[] arr1 = o1.split("\t");
                String[] arr2 = o2.split("\t");
                long l1 = ipToLong(arr1[0]);
                long l2 = ipToLong(arr2[0]);
                if (l2 > l1) {
                    return -1;
                } else if (l2 < l1) {
                    return 1;
                } else {
                    return 0;
                }
            }
        });
    }


    public static void divideIPC0515(String dir, String outdir) throws Exception {
        Writer w = new FileWriter(outdir);
        InputStreamReader isr = new InputStreamReader(new FileInputStream(dir), "utf-8");
        BufferedReader rd = new BufferedReader(isr);
        String s;
        List<String> ls = new ArrayList<>();
        Long start = null;
        Long end = null;
        while ((s = rd.readLine()) != null) {
            s = s.replaceAll("﻿", "");
            String[] arr = s.split("\t");
            long ip_start = ipToLong(arr[0]);
            long ip_end = ipToLong(arr[1]);
            if (start == null || end == null) {
                start = ip_start;
                end = ip_end;
            } else {
                if (ip_start == end + 1) {
                    end = ip_end;
                } else {
                    ls.add(longToIp(start) + "\t" + longToIp(end));
                    start = ip_start;
                    end = ip_end;
                }
            }

        }
        isr.close();
        rd.close();
        if (start != null && end != null) {
            ls.add(longToIp(start) + "\t" + longToIp(end));
        }
        long all = 0;
        Set<String> set = new HashSet<>();
        for (String ss : ls) {
            ss = ss.replaceAll("﻿", "");
            String[] arr = ss.split("\t");
            List<String> ll = DivideIPC(arr[0], arr[1]);
            for (String a : ll) {
                if (!set.contains(a)) {
                    set.add(a);
                    all += ipToLong(a.split("\t")[1]) - ipToLong(a.split("\t")[0]) + 1;
                    w.write(a + "\r\n");
                }

            }
        }
        System.out.println(all);
        w.close();
    }


    public static void getNoRepeatIP(File file, Map<String, String> map) throws Exception {
        InputStreamReader isr = new InputStreamReader(new FileInputStream(file), "utf-8");
        BufferedReader rd = new BufferedReader(isr);
        String s;
        while ((s = rd.readLine()) != null) {
            s = s.replaceAll("﻿", "");
            String[] arr = s.split("\\s");
            for (String a : arr) {
                if (IPDao.JudgeIP(a)) {
                    String[] wdarr = a.split("\\.");
                    String wd = wdarr[0] + "." + wdarr[1] + "." + wdarr[2];
                    String c = wdarr[3];
                    if (!map.containsKey(wd)) {
                        map.put(wd, c);
                    } else {
                        String val = map.get(wd);
                        if (!val.equals(c) && !val.startsWith(c + ",") && !val.endsWith("," + c) && !val.contains("," + c + ",")) {
                            map.put(wd, val + "," + c);
                        }
                    }
                }
            }
        }
        isr.close();
        rd.close();

    }


    public static Map<String, String> getNoRepeatIPMap(File file) throws Exception {
        Map<String, String> map = new TreeMap<String, String>(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                long l1 = IPDao.IPToLong(o1);
                long l2 = IPDao.IPToLong(o2);
                if (l1 > l2) {
                    return 1;
                } else if (l1 == l2) {
                    return 0;
                } else {
                    return -1;
                }
            }
        });
        InputStreamReader isr = new InputStreamReader(new FileInputStream(file), "utf-8");
        BufferedReader rd = new BufferedReader(isr);
        String s;
        while ((s = rd.readLine()) != null) {
            s = s.replaceAll("﻿", "");
            String[] arr = s.split("\\s");
            for (String a : arr) {
                if (IPDao.JudgeIP(a)) {
                    String[] wdarr = a.split("\\.");
                    String wd = wdarr[0] + "." + wdarr[1] + "." + wdarr[2] + ".0";
                    String c = wdarr[3];
                    if (!map.containsKey(wd)) {
                        map.put(wd, c);
                    } else {
                        String val = map.get(wd);
                        if (!val.equals(c) && !val.startsWith(c + ",") && !val.endsWith("," + c) && !val.contains("," + c + ",")) {
                            map.put(wd, val + "," + c);
                        }
                    }
                }
            }
        }
        isr.close();
        rd.close();
        return map;

    }


    public static List<String> getAllIPv4() {
        List<String> ls = new ArrayList<>();
        for (int i = 1; i < 224; i++) {
            for (int j = 0; j <= 255; j++) {
                String sip = i + "." + j + ".0.0";
                String eip = i + "." + j + ".255.255";
                if (sip.startsWith("10.") || sip.startsWith("127.") || sip.startsWith("169.254.")
                        || sip.startsWith("192.0.0.") || sip.startsWith("192.0.2.") || sip.startsWith("192.88.99.")
                        || sip.startsWith("192.168.") || sip.startsWith("198.51.100.") || sip.startsWith("203.0.113.")) {
                    continue;
                }
                if (i == 100) {
                    if (j >= 64 && j <= 127) {
                        continue;
                    }
                }
                if (i == 172) {
                    if (j >= 16 && j <= 31) {
                        continue;
                    }
                }
                if (i == 198) {
                    if (j >= 18 && j <= 19) {
                        continue;
                    }
                }
                ls.add(sip + "\t" + eip);

            }
        }
        return ls;
    }


    public static boolean checkUsableIP(String sip) {
        if (!JudgeIP(sip)) {
            return false;
        }
        if (sip.startsWith("0.") || sip.startsWith("10.") || sip.startsWith("127.") || sip.startsWith("169.254.")
                || sip.startsWith("192.0.0.") || sip.startsWith("192.0.2.") || sip.startsWith("192.88.99.")
                || sip.startsWith("192.168.") || sip.startsWith("198.51.100.") || sip.startsWith("203.0.113.")) {
            return false;
        }
        String[] arr = sip.split("\\.");
        int i = Integer.parseInt(arr[0]);
        int j = Integer.parseInt(arr[1]);
        if (i >= 224) {
            return false;
        }
        if (i == 100) {
            if (j >= 64 && j <= 127) {
                return false;
            }
        }
        if (i == 172) {
            if (j >= 16 && j <= 31) {
                return false;
            }
        }
        if (i == 198) {
            if (j >= 18 && j <= 19) {
                return false;
            }
        }

        return true;
    }


    public static String getIPCNet(String ip) {
        String[] arr = ip.split("\\.");
        return arr[0] + "." + arr[1] + "." + arr[2] + ".0";
    }


    public static String ChangeIP2Binary(String sIP) {
        String prefixString = "00000000000000000000000000000000";
        String binaryString = Long.toBinaryString(IPDao.ipToLong(sIP));
        if (binaryString.length() < 32) {
            try {
                binaryString = prefixString.substring(0, 32 - binaryString.length()) + binaryString;
            } catch (Exception e) {
                System.out.println(binaryString.length());
            }
        }
        return binaryString;
    }

    public static String ChangeBinary2IP(String Binary) {
        Binary = Binary.replaceAll("﻿", "");
        String s1 = Integer.valueOf(Binary.substring(0, 8), 2).toString();
        String s2 = Integer.valueOf(Binary.substring(8, 16), 2).toString();
        String s3 = Integer.valueOf(Binary.substring(16, 24), 2).toString();
        String s4 = Integer.valueOf(Binary.substring(24, 32), 2).toString();
        return s1 + "." + s2 + "." + s3 + "." + s4;
    }

    public static String getSubStart(String ip, int mask) {
        if (mask <= 0 || mask > 32) {
            return "error";
        }
        String binaryString = ChangeIP2Binary(ip);
        binaryString = binaryString.substring(0, mask);
        String prefixString = "000000000000000000000000";
        if (binaryString.length() < 32) {
            try {
                binaryString = binaryString + prefixString.substring(0, 32 - binaryString.length());
            } catch (Exception e) {
                System.out.println(binaryString.length());
            }
        }
        return IPDao.longToIp(Long.valueOf(binaryString, 2));
    }

    public static String getSubEnd(String ip, int mask) {
        if (mask <= 0 || mask > 32) {
            return "error";
        }
        String binaryString = ChangeIP2Binary(ip);
        binaryString = binaryString.substring(0, mask);
        String prefixString = "11111111111111111111111111111111";
        if (binaryString.length() < 32) {
            try {
                binaryString = binaryString + prefixString.substring(0, 32 - binaryString.length());
            } catch (Exception e) {
                System.out.println(binaryString.length());
            }
        }
        return IPDao.longToIp(Long.valueOf(binaryString, 2));
    }


    public static BigInteger ipv6toInt(String ipv6) {

        int compressIndex = ipv6.indexOf("::");
        if (compressIndex != -1) {
            String part1s = ipv6.substring(0, compressIndex);
            String part2s = ipv6.substring(compressIndex + 1);
            BigInteger part1 = ipv6toInt(part1s);
            BigInteger part2 = ipv6toInt(part2s);
            int part1hasDot = 0;
            char ch[] = part1s.toCharArray();
            for (char c : ch) {
                if (c == ':') {
                    part1hasDot++;
                }
            }
            // ipv6 has most 7 dot
            return part1.shiftLeft(16 * (7 - part1hasDot)).add(part2);
        }
        String[] str = ipv6.split(":");
        BigInteger big = BigInteger.ZERO;
        for (int i = 0; i < str.length; i++) {
            //::1
            if (str[i].isEmpty()) {
                str[i] = "0";
            }
            big = big.add(BigInteger.valueOf(Long.valueOf(str[i], 16))
                    .shiftLeft(16 * (str.length - i - 1)));
            System.out.println(big + "--" + i);
        }
        return big;
    }


    public static List<String> getIPWdSubList(String sIP, String eIP) throws Exception {
        long l1 = IPDao.IPToLong(sIP);
        long l2 = IPDao.IPToLong(eIP);
        String all1 = "11111111111111111111111111111111";
        List<String> ls = new ArrayList<>();
        for (long l = l1; l <= l2; l++) {
            String ip = longToIp(l);
            String bs = ChangeIP2Binary(ip);
            if (!bs.endsWith("0")) {
                ls.add(ip + "/32");
                continue;
            }
            int last1index = bs.lastIndexOf("1");
            for (int i = last1index + 1; i <= 32; i++) {
                int j = 32 - i;
                String bss = "";
                if (i > 0) {
                    bss = bs.substring(0, i);
                }
                String bse = bss + all1.substring(0, j);
                String ipe = ChangeBinary2IP(bse);
                long ipel = IPToLong(ipe);
                if (ipel <= l2) {
                    ls.add(ip + "/" + i);
                    l = ipel;
                    break;
                }
            }
        }
        return ls;
    }

    public static boolean checkIPSub(String ipsub) {
        boolean bl = false;
        if (ipsub != null && ipsub.contains("/")) {
            String[] arr = ipsub.split("/");
            if (arr.length > 1) {
                if (JudgeIP(arr[0])) {
                    try {
                        Integer j = Integer.parseInt(arr[1]);
                        if (j > 0 && j <= 32) {
                            bl = true;
                        }
                    } catch (Exception e) {
                    }
                }
            }
        }
        return bl;
    }


    public static void changeToIPSubFile(String dir, String outdir) throws Exception {
        Writer w = new FileWriter(outdir);
        InputStreamReader isr = new InputStreamReader(new FileInputStream(dir), "utf-8");
        BufferedReader rd = new BufferedReader(isr);
        String line;
        while ((line = rd.readLine()) != null) {
            line = line.replaceAll("﻿", "");
            String[] arr = line.split("\\s|,");
            if (JudgeIP(arr[0])) {
                if (JudgeIP(arr[1])) {
                    try {
                        List<String> ls = getIPWdSubList(arr[0], arr[1]);
                        for (String s : ls) {
                            w.write(s + "\r\n");
                        }
                    } catch (Exception e) {
                    }
                } else {
                    w.write(arr[0] + "/32\r\n");
                }
            } else if (checkIPSub(arr[0])) {
                w.write(arr[0] + "\r\n");
            }
        }
        isr.close();
        rd.close();
        w.close();
    }

    //比较两个字符串是否包含
    public static boolean compareString(String s1, String s2) {
        if (s1.contains(s2) || s2.contains(s1)) {
            return true;
        } else {
            return false;
        }
    }


    public static void combineFileIP(String outdir, String[] dirArr) throws Exception {
        Writer w = new FileWriter(outdir);
        Set<String> set = new HashSet<>();
        for (String dir : dirArr) {
            InputStreamReader isr = new InputStreamReader(new FileInputStream(dir), "utf-8");
            BufferedReader rd = new BufferedReader(isr);
            String s;
            while ((s = rd.readLine()) != null) {
                s = s.replaceAll("﻿", "");
                if (!set.contains(s)) {
                    w.write(s + "\r\n");
                    set.add(s);
                }
            }
            isr.close();
            rd.close();
        }
        w.close();

    }


    //9223372036854775807
    //18446462598732840000
    public static void main(String[] args) throws Exception {

//		System.out.println(URLDecoder.decode("www.ipip.net"));
//		
//		String ipv6="::1";
//		String fullIPv6=parseAbbreviationToFullIPv6(ipv6);
//		System.out.println(JudgeIPv6("::1"));
//		System.out.println(JudgeIPv6(fullIPv6));
//		System.out.println(fullIPv6);
//		System.out.println(parseAbbreviationToFullIPv6(fullIPv6));
//		System.out.println(fullIPv6);
//		System.out.println(JSON.toJSONString(ipv6toLongArr(ipv6)));

//		JSONObject jsonObject=new JSONObject(true);
//		JSON.parse(ipv6,Feature.OrderedField);

//		System.out.println(Long.valueOf("ffff", 16) << 16);
//		System.out.println(ipv6toInt(fullIPv6)+",");


//		System.out.println(checkUsableIP("8.0.0.1"));

//		System.out.println(parseFullIPv6ToAbbreviation("fbff:ffff:ffff:ffff:ffff:ffff:ffff:ffff"));

//		divideIPC0515("H:\\数据测量\\Email探测\\20200527江苏\\南京IP.txt", "H:\\数据测量\\Email探测\\20200527江苏\\南京IPC.txt");

//		System.out.println(DivideIPC("134.159.206.0", "134.159.206.255").size());

//		System.out.println(getIPNo("H:/数据测量/湖南IP测量/湖南IP.txt"));

//		Map<String, String> map=new HashMap<String, String>();
//		for(File f:new File("E:\\数据测量\\DNS探测\\1117全球DNS测量\\开放端口").listFiles()) {
//			getNoRepeatIP(f, map);
//		}
//		Writer w=new FileWriter("E:\\数据测量\\DNS探测\\1117全球DNS测量\\开放端口.txt");
//		for(String key:map.keySet()) {
//		   for(String c:map.get(key).split(",")) {
//			   w.write(key+"."+c+"\r\n");
//		   }
//		}
//		w.close();


//		ReadUrl.getIPMarkerApi("I:/离线库/IPMarkerGEO_PRO");
//		Writer w=new FileWriter("E:\\数据测量\\DNS探测\\1117全球DNS测量\\中国开放端口.txt");
//		InputStreamReader isr= new InputStreamReader(new FileInputStream("E:\\数据测量\\DNS探测\\1117全球DNS测量\\开放端口.txt"), "utf-8");
//		BufferedReader rd=new BufferedReader(isr);
//		String s;
//		while((s=rd.readLine())!=null){
//			s=s.replaceAll("﻿", "");
//			Location lc=ReadUrl.readbyIPMarker2(s);
//			if(lc.getCountry().contains("中国")) {
//				w.write(s+"\r\n");
//			}
//		}
//		isr.close();
//		rd.close();
//		w.close();

//		Writer w=new FileWriter("E:\\数据测量\\DNS探测\\1220全球DNS测量\\ip.txt");
//		List<String> ls=getAllIPv4();
//		for(String s:ls) {
//			w.write(s+"\r\n");
//		}
//		w.close();

        divideIPC0515("I:\\工作2\\202206\\0613关岛洛杉矶测量\\关岛数据\\关岛全部IP.txt",
                "I:\\工作2\\202206\\0613关岛洛杉矶测量\\关岛数据\\关岛全部IPC.txt");

//		combineFileIP("I:\\工作2\\202206\\0614台湾大哥大\\全部活跃IP.txt",
//				new String[]{"I:\\工作2\\202206\\0614台湾大哥大\\大哥大\\Zmap扫描活跃IP.txt",
//						"I:\\工作2\\202206\\0614台湾大哥大\\台湾之星\\Zmap扫描活跃IP.txt",
//						"I:\\工作2\\202206\\0614台湾大哥大\\凯擘大宽频\\Zmap扫描活跃IP.txt",
//						"I:\\工作2\\202206\\0614台湾大哥大\\台湾固网\\Zmap扫描活跃IP.txt"});


//		System.out.println(IPDao.ipToLong("223.112.174.247"));

//		ReadUrl.getIPMarkerApi("I:\\离线库\\测试\\test_outline");
//		System.out.println(ReadUrl.readbyIPMarker2("123.206.188.167").getAllAddress());

    }
}
