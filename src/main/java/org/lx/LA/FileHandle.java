package org.lx.LA;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.lx.pojo.ASInfo;
import org.lx.pojo.IpInfo;
import org.lx.pojo.PortInfo;
import org.lx.pojo.Vul;
import org.lx.service.IPASInfoSearch;
import org.lx.tools.NormalTool;
import org.lx.tools.ip.GetIPLocation;
import org.lx.tools.ip.IPLocation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;

/**
 * @author yuntao
 * @date 2022/8/1
 */
public class FileHandle {
    public static void main(String[] args) throws Exception {
        //getRelatedAs("Psychz Networks");
        //getRelatedAs("T-Mobile", "Sprint");
        //getDnsInfo("AT&T");
    
        File file = new File("D:\\Desktop\\json");
        File[] files = file.listFiles();
    
        Set<String> cveSet = new HashSet<>();
        for (File file1 : files) {
            BufferedReader br = new BufferedReader(new FileReader(file1));
            String line;
            
            while((line = br.readLine()) != null) {
                
                IpInfo ipInfo = JSON.parseObject(line, IpInfo.class);
                
                List<Vul> vuls = ipInfo.getVuls();
                if (vuls == null) {
                    continue;
                }
                for (Vul vul : vuls) {
                    cveSet.add(vul.getNum());
                }
            }
        }
    
        System.out.println(cveSet.size());
        FileWriter writer = new FileWriter("D:\\Desktop\\洛杉矶cve总和.txt");
        for (String s : cveSet) {
            writer.write(s + "\r\n");
        }
        writer.close();
    
        /*IPASInfoSearch ipasInfoSearch = new IPASInfoSearch(IPASInfoSearch.asDir);
        System.out.println(1);*/
    
        //getAllOpenPort();
        /*FileLineTool.readLineWithThreads(new File("D:\\Desktop\\洛杉矶电信运营商.txt"), line -> {
            try {
                getIspVulsNum(line);
                //getOpenPort(line);
            }catch (Exception e) {
                e.printStackTrace();
            }

        }, 5);*/
    
        //getAllVuls();
        
        
        //getOpenPort("LayerHost");
    }
    
    public static void getAllOpenPort() throws Exception {
        BufferedReader br = new BufferedReader(new FileReader("洛杉矶设备分类json-有IDC.txt"));
        String line;
        Map<String, Integer> map = new HashMap<>();
        while((line = br.readLine()) != null) {
            IpInfo ipInfo = JSON.parseObject(line, IpInfo.class);
            
            List<PortInfo> portInfos = ipInfo.getPortInfos();
    
            for (PortInfo portInfo : portInfos) {
                if ("open".equals(portInfo.state)) {
                    map.put(portInfo.getPort(), map.get(portInfo.getPort()) == null ? 1 : map.get(portInfo.getPort()) + 1);
                }
            }
        }
    
        map = sortMap(map);
    
        FileWriter writer = new FileWriter("D:\\Desktop\\洛杉矶运营商相关\\总开放端口.txt");
        for (String s : map.keySet()) {
            writer.write(s + "端口" + "\t" + map.get(s) + "\r\n");
        }
        writer.close();
    }
    
    public static void getIspVulsNum(String keywords) throws Exception {
        keywords = keywords.toLowerCase();
        
        BufferedReader br = new BufferedReader(new FileReader("洛杉矶设备分类json-有IDC.txt"));
        String line;
        int num = 0;
        while((line = br.readLine()) != null) {
            IpInfo ipInfo = JSON.parseObject(line, IpInfo.class);
            
            ASInfo asInfo = ipInfo.getAsInfo();
            if (!asInfo.getOrg_name().toLowerCase().contains(keywords)) {
                continue;
            }
            
            num += ipInfo.getVuls().size();
        }
    
        System.out.println(keywords + "\t" + num);
    }
    
    public static void getAllVuls() throws Exception {
        BufferedReader br = new BufferedReader(new FileReader("D:\\Desktop\\洛杉矶\\节点分析\\洛杉矶设备分类json-有IDC.txt"));
        String line;
        Map<String, Integer> map = new HashMap<>();
        int num = 0;
        Map<String, Integer> ipMap = new HashMap<>();
        while((line = br.readLine()) != null) {
            IpInfo ipInfo = JSON.parseObject(line, IpInfo.class);
            
            List<Vul> vuls = ipInfo.getVuls();
    
            if (vuls.size() != 0) {
                for (Vul vul : vuls) {
                    String name = vul.getNum();
                    map.put(name, map.get(name) == null ? 1 : map.get(name) + 1);
                }
                ipMap.put(ipInfo.getIp(), vuls.size());
            }
        }
        Map<String, String> cveMap = new HashMap<>();
        br = new BufferedReader(new FileReader("D:\\Desktop\\阿里云cve数据-1.0.txt"));
        while((line = br.readLine()) != null) {
            JSONObject json = JSON.parseObject(line);
            
            String name = json.getString("num");
            String des = json.getString("description");
            
            cveMap.put(name, des);
        }
        
        
        FileWriter writer = new FileWriter("D:\\Desktop\\洛杉矶运营商相关\\漏洞排名.txt");
        
        map = sortMap(map);
    
        for (String s : map.keySet()) {
            writer.write(s + "\t" + map.get(s) + "\t" + cveMap.get(s) + "\r\n");
            
        }
        
        writer.close();
    
        System.out.println(num);
    
        IPASInfoSearch ipasInfoSearch = new IPASInfoSearch(IPASInfoSearch.asDir);
        ipMap = sortMap(ipMap);
        int flag = 0;
        writer = new FileWriter("D:\\Desktop\\洛杉矶运营商相关\\有漏洞的ip排名.txt");
        for (String ip : ipMap.keySet()) {
            ASInfo asInfo = ipasInfoSearch.searchIPASInfo(ip);
            writer.write(ip + "\t" + asInfo.getAut() + "\t" + asInfo.getAut_name() + "\t" + asInfo.getOrg_name() + "\t" + asInfo.getIsp() + "\t" + ipMap.get(ip) + "\r\n");
            
            if (++flag == 10) {
                break;
            }
        }
        
        writer.close();
        System.out.println(ipMap.keySet().size());
    }
    
    public static void getIspIpInfo(String keywords, IPASInfoSearch ipasInfoSearch) throws Exception {
        // D:\Desktop\洛杉矶\节点分析\path_all.txt
        // 所有路径
        keywords = keywords.toLowerCase();
        BufferedReader br = new BufferedReader(new FileReader("洛杉矶设备分类json-无IDC.txt"));
        String line;
        // as
        Set<String> asSet = new HashSet<>();
        // 总ip
        Set<String> ipSet = new HashSet<>();
        // 活跃ip
        Set<String> activeIpSet = new HashSet<>();
        // dns
        Set<String> dnsSet = new HashSet<>();
        // 跨区域
        Set<String> hexinSet = new HashSet<>();
        // 跨as
        Set<String> bianjieSet = new HashSet<>();
        // 22或3389的端口open
        Set<String> openPortSet = new HashSet<>();
        
        while ((line = br.readLine()) != null) {
            IpInfo ipInfo = JSON.parseObject(line, IpInfo.class);
            
            ASInfo asInfo = ipInfo.getAsInfo();
            if (!asInfo.getOrg_name().toLowerCase().contains(keywords)) {
                continue;
            }
            IPLocation lc = ipInfo.getLocation();
            List<PortInfo> ports = ipInfo.getPortInfos();
            for (PortInfo port : ports) {
                if ("22".equals(port.getPort()) || "3389".equals(port.getPort())) {
                    if ("open".equals(port.state)) {
                        openPortSet.add(ipInfo.getIp());
                    }
                }
            }
        }
        
        br = new BufferedReader(new FileReader("D:\\Desktop\\洛杉矶\\ip\\洛杉矶全部ip-修正后.txt"));
        
        while ((line = br.readLine()) != null) {
            ASInfo asInfo = ipasInfoSearch.searchIPASInfo(line);
            
            if (asInfo.getOrg_name().toLowerCase().contains(keywords)) {
                ipSet.add(line);
            }
        }
        
        br = new BufferedReader(new FileReader("D:\\Desktop\\洛杉矶\\节点分析\\router_all.txt"));
        Set<String> routerSet = new HashSet<>();
        while ((line = br.readLine()) != null) {
            routerSet.add(line);
        }
        
        
        br = new BufferedReader(new FileReader("D:\\Desktop\\洛杉矶\\ip\\洛杉矶活跃IP-修正后.txt"));
        while ((line = br.readLine()) != null) {
            ASInfo asInfo = ipasInfoSearch.searchIPASInfo(line);
            
            if (asInfo.getOrg_name().toLowerCase().contains(keywords)) {
                activeIpSet.add(line);
            }
        }
        
        br = new BufferedReader(new FileReader("D:\\Desktop\\洛杉矶\\节点分析\\设备-DNS.txt"));
        while ((line = br.readLine()) != null) {
            String ip = line.split("\t")[0];
            ASInfo asInfo = ipasInfoSearch.searchIPASInfo(ip);
            
            if (asInfo.getOrg_name().toLowerCase().contains(keywords)) {
                dnsSet.add(ip);
            }
        }
    
        /*br = new BufferedReader(new FileReader("D:\\Desktop\\洛杉矶\\节点分析\\洛杉矶-汇聚路由器.txt"));
        while ((line = br.readLine()) != null) {
            String ip = line.split("\t")[0];
            ASInfo asInfo = ipasInfoSearch.searchIPASInfo(ip);
        
            if (asInfo.getOrg_name().toLowerCase().contains(keywords)) {
                bianjieSet.add(ip);
            }
        }*/
        
        br = new BufferedReader(new FileReader("D:\\Desktop\\洛杉矶\\节点分析\\path_all.txt"));
        
        while ((line = br.readLine()) != null) {
            String from = line.split("\t")[0];
            String to = line.split("\t")[1];
            
            ASInfo asFrom = ipasInfoSearch.searchIPASInfo(from);
            ASInfo asTo = ipasInfoSearch.searchIPASInfo(to);
            
            if (!asFrom.getOrg_name().toLowerCase().contains(keywords) && !asTo.getOrg_name().toLowerCase().contains(keywords)) {
                continue;
            }
            
            if (asFrom.getAut().equals(asTo.getAut())) {
                continue;
            }
            
            
            if (asFrom.getOrg_name().toLowerCase().contains(keywords)) {
                asSet.add(asFrom.getAut());
                if (routerSet.contains(from)) {
                    bianjieSet.add(from);
                }
            } else if (asTo.getOrg_name().toLowerCase().contains(keywords)) {
                asSet.add(asTo.getAut());
                if (routerSet.contains(to)) {
                    bianjieSet.add(to);
                }
            }
        }
        
        br = new BufferedReader(new FileReader("D:\\Desktop\\洛杉矶\\节点分析\\洛杉矶-核心路由器.txt"));
        while ((line = br.readLine()) != null) {
            String ip = line.split("\t")[0];
            ASInfo asInfo = ipasInfoSearch.searchIPASInfo(ip);
            
            if (asInfo.getOrg_name().toLowerCase().contains(keywords)) {
                hexinSet.add(ip);
            }
        }
        
        Set<String> asSet1 = new HashSet<>();
        for (String as : asSet) {
            if (as.contains("_")) {
                String[] ss = as.split("_");
                asSet1.addAll(Arrays.asList(ss));
                
                continue;
            }
            asSet1.add(as);
            
        }
        
        System.out.println("as：" + asSet1.size() + "\t" + asSet1);
        System.out.println("拥有ip：" + ipSet.size());
        System.out.println("活跃ip：" + activeIpSet.size());
        System.out.println("dns服务器：" + dnsSet.size());
        System.out.println("核心：" + hexinSet.size());
        System.out.println("边界：" + bianjieSet.size());
        System.out.println("开放远程端口：" + openPortSet.size());
        
        FileWriter writer = new FileWriter("D:\\Desktop\\洛杉矶运营商相关\\边界路由器\\" + keywords + ".txt");
        for (String s : bianjieSet) {
            writer.write(s + "\r\n");
        }
        
        writer.close();
    }
    
    
    public static void getBianjieRouter(String keywords, IPASInfoSearch ipasInfoSearch) throws Exception {
        String line;
    
        BufferedReader br = new BufferedReader(new FileReader("D:\\Desktop\\洛杉矶\\节点分析\\router_all.txt"));
        Set<String> routerSet = new HashSet<>();
        while ((line = br.readLine()) != null) {
            routerSet.add(line);
        }
        
        br = new BufferedReader(new FileReader("D:\\Desktop\\洛杉矶\\节点分析\\path_all.txt"));
    
        Set<String> bianjieSet = new HashSet<>();
        try {
            while ((line = br.readLine()) != null) {
                String from = line.split("\t")[0] == null ? null : line.split("\t")[0];
                String to = line.split("\t")[1] == null ? null : line.split("\t")[1];
        
                if (from == null || to == null) {
                    System.out.println(line);
                    continue;
                }
        
        
                ASInfo asFrom = ipasInfoSearch.searchIPASInfo(from) == null ? null : ipasInfoSearch.searchIPASInfo(from);
                ASInfo asTo = ipasInfoSearch.searchIPASInfo(to) == null ? null : ipasInfoSearch.searchIPASInfo(to);
        
                if (asFrom == null || asTo == null) {
                    continue;
                }
        
        
                if (!asFrom.getOrg_name().toLowerCase().contains(keywords) && !asTo.getOrg_name().toLowerCase().contains(keywords)) {
                    continue;
                }
        
                if (asFrom.getAut().equals(asTo.getAut())) {
                    continue;
                }
        
        
                if (asFrom.getOrg_name().toLowerCase().contains(keywords)) {
                    if (routerSet.contains(from)) {
                        bianjieSet.add(from);
                    }
                } else {
                    if (routerSet.contains(to)) {
                        bianjieSet.add(to);
                    }
                }
            }
    
        }catch (Exception e) {
            System.out.println(11111);
            e.printStackTrace();
        }

        FileWriter writer = new FileWriter("D:\\Desktop\\洛杉矶运营商相关\\边界路由器\\" + keywords + ".txt");
        for (String s : bianjieSet) {
            writer.write(s + "\r\n");
        }
        
        writer.close();
        
    }
    
    public static void getHexinRouter(String keywords, IPASInfoSearch ipasInfoSearch) throws Exception {
        String line;
        BufferedReader br = new BufferedReader(new FileReader("D:\\Desktop\\洛杉矶\\节点分析\\洛杉矶-核心路由器.txt"));
        Set<String> set = new HashSet<>();
        while ((line = br.readLine()) != null) {
            String ip = line.split("\t")[0];
            ASInfo asInfo = ipasInfoSearch.searchIPASInfo(ip) == null ? null : ipasInfoSearch.searchIPASInfo(ip);
        
            if (asInfo == null) {
                continue;
            }
            if (asInfo.getOrg_name().toLowerCase().contains(keywords)) {
                set.add(ip);
            }
        }
        
        FileWriter writer = new FileWriter("D:\\Desktop\\洛杉矶运营商相关\\核心路由器\\" + keywords + ".txt");
        
        for (String s : set) {
            writer.write(s + "\r\n");
        }
        
        writer.close();
    }
    
    public static void getRelatedAs(String keywords, IPASInfoSearch ipasInfoSearch) throws Exception {
        keywords = keywords.toLowerCase();
        BufferedReader br = new BufferedReader(new FileReader("D:\\Desktop\\洛杉矶\\节点分析\\path_all.txt"));
        String line;
        
        Set<String> asSet = new HashSet<>();
        Map<String, Integer> map = new HashMap<>();
        Map<String, String> map2 = new HashMap<>();
        
        while ((line = br.readLine()) != null) {
            String from = line.split("\t")[0];
            String to = line.split("\t")[1];
            
            ASInfo fromAs = ipasInfoSearch.searchIPASInfo(from) == null ? null : ipasInfoSearch.searchIPASInfo(from);
            ASInfo toAs = ipasInfoSearch.searchIPASInfo(to) == null ? null : ipasInfoSearch.searchIPASInfo(to);
            if (fromAs == null || toAs == null) {
                continue;
            }
            if (!fromAs.getOrg_name().toLowerCase().contains(keywords) && !toAs.getOrg_name().toLowerCase().contains(keywords)) {
                continue;
            }
            
            if (fromAs.getAut().equals(toAs.getAut())) {
                continue;
            }
            
            if (isCompare(fromAs.getAut(), toAs.getAut())) {
                map.put(fromAs.getAut() + "\t" + toAs.getAut(), map.get(fromAs.getAut() + "\t" + toAs.getAut()) == null ? 1 : map.get(fromAs.getAut() + "\t" + toAs.getAut()) + 1);
            } else {
                map.put(toAs.getAut() + "\t" + fromAs.getAut(), map.get(toAs.getAut() + "\t" + fromAs.getAut()) == null ? 1 : map.get(toAs.getAut() + "\t" + fromAs.getAut()) + 1);
            }
            
            map2.put(fromAs.getAut(), fromAs.getAut_name());
            map2.put(toAs.getAut(), toAs.getAut_name());
            if (fromAs.getOrg_name().toLowerCase().contains(keywords)) {
                asSet.add(fromAs.getAut());
            } else {
                asSet.add(toAs.getAut());
            }
        }
        
        Set<String> asSet1 = new HashSet<>();
        for (String as : asSet) {
            if (as.contains("_")) {
                String[] ss = as.split("_");
                asSet1.addAll(Arrays.asList(ss));
                
                continue;
            }
            asSet1.add(as);
            
        }
        
        System.out.println("as数量：" + asSet1.size());
        System.out.println(asSet1);
        
        Map<String, Integer> map1 = new HashMap<>();
        
        for (String s : map.keySet()) {
            if (s.contains("_")) {
                String[] ss = s.split("\t");
                String[] from = ss[0].split("_");
                String[] to = ss[1].split("_");
                
                int flag = map.get(s);
                for (String s1 : to) {
                    for (String s2 : from) {
                        if (s1.equals(s2)) {
                            continue;
                        }
                        map2.put(s1, map2.get(ss[1]));
                        map2.put(s2, map2.get(ss[0]));
                        if (isCompare(s1, s2)) {
                            map1.put(s1 + "\t" + s2, map1.get(s1 + "\t" + s2) == null ? flag : map1.get(s1 + "\t" + s2) + flag);
                        } else {
                            map1.put(s2 + "\t" + s1, map1.get(s2 + "\t" + s1) == null ? flag : map1.get(s2 + "\t" + s1) + flag);
                        }
                    }
                }
            } else {
                map1.put(s, map.get(s));
            }
        }
        
        FileWriter writer = new FileWriter("D:\\Desktop\\洛杉矶运营商相关\\" + keywords + ".txt");
        map1 = sortMap(map1);
        System.out.println(map1);
        for (String s : map1.keySet()) {
            String[] ss = s.split("\t");
            
            String from = ss[0];
            String to = ss[1];
            /*if (s.contains("*") || map2.get(to).contains("*") || map2.get(from).contains("*")) {
                continue;
            }*/
            if (asSet1.contains(from)) {
                String l = from + "\t" + map2.get(from) + "\t" + to + "\t" + map2.get(to) + "\t" + map1.get(s) + "\n";
                if (l.contains("*")) {
                    continue;
                }
                writer.write(from + "\t" + map2.get(from) + "\t" + to + "\t" + map2.get(to) + "\t" + map1.get(s) + "\n");
                continue;
            }
            String l = to + "\t" + map2.get(to) + "\t" + from + "\t" + map2.get(from) + "\t" + map1.get(s) + "\n";
            if (l.contains("*")) {
                continue;
            }
            writer.write(to + "\t" + map2.get(to) + "\t" + from + "\t" + map2.get(from) + "\t" + map1.get(s) + "\n");
    
        }
        writer.close();
    }
    
    
    public static void getDnsInfo(String keywords, IPASInfoSearch ipasInfoSearch) throws Exception {
        keywords = keywords.toLowerCase();
        BufferedReader br = new BufferedReader(new FileReader("D:\\Desktop\\洛杉矶\\节点分析\\设备-DNS-version-simple.txt"));
        String line;
        FileWriter writer = new FileWriter("D:\\Desktop\\洛杉矶运营商相关\\dns\\" + keywords + "dns.txt");
        while ((line = br.readLine()) != null) {
            String ip = line.split("\t")[0];
            ASInfo asInfo = ipasInfoSearch.searchIPASInfo(ip);
            
            if (asInfo.getOrg_name().toLowerCase().contains(keywords)) {
                writer.write(line + "\r\n");
            }
        }
        
        writer.close();
    }
    
    
    public static void getDnsInfo(String keywords) throws Exception {
        keywords = keywords.toLowerCase();
        BufferedReader br = new BufferedReader(new FileReader("D:\\Desktop\\洛杉矶\\节点分析\\设备-DNS-version-simple.txt"));
        String line;
        IPASInfoSearch ipasInfoSearch = new IPASInfoSearch(IPASInfoSearch.asDir);
        FileWriter writer = new FileWriter("F:\\关岛\\ProbeDataHandle\\电信运营商相关信息\\" + keywords + "dns.txt");
        while ((line = br.readLine()) != null) {
            String ip = line.split("\t")[0];
            ASInfo asInfo = ipasInfoSearch.searchIPASInfo(ip);
            
            if (asInfo.getOrg_name().toLowerCase().contains(keywords)) {
                writer.write(line + "\r\n");
            }
        }
        
        writer.close();
    }
    
    public static void fileHandle() throws Exception {
        BufferedReader br = new BufferedReader(new FileReader("D:\\Desktop\\洛杉矶\\ip\\洛杉矶全部ip-修正后.txt"));
        String line;
        IPASInfoSearch ipasInfoSearch = new IPASInfoSearch(IPASInfoSearch.asDir);
        Map<String, Integer> map = new HashMap<>();
        while ((line = br.readLine()) != null) {
            ASInfo asInfo = ipasInfoSearch.searchIPASInfo(line);
            
            map.put(asInfo.getOrg_name(), map.get(asInfo.getOrg_name()) == null ? 1 : map.get(asInfo.getOrg_name()) + 1);
        }
        
        map = sortMap(map);
        FileWriter writer = new FileWriter("洛杉矶全部ip的as结果.txt");
        for (String s : map.keySet()) {
            writer.write(s + "\t" + map.get(s) + "\r\n");
        }
        
        writer.close();
    }
    
    /*public static void getRelatedAs(String keywords) throws Exception {
        keywords = keywords.toLowerCase();
        BufferedReader br = new BufferedReader(new FileReader("D:\\Desktop\\洛杉矶\\节点分析\\path_all.txt"));
        String line;
        IPASInfoSearch ipasInfoSearch = new IPASInfoSearch(IPASInfoSearch.asDir);
        
        Set<String> asSet = new HashSet<>();
        Map<String, Integer> map = new HashMap<>();
        Map<String, String> map2 = new HashMap<>();
        
        while ((line = br.readLine()) != null) {
            String from = line.split("\t")[0];
            String to = line.split("\t")[1];
            
            ASInfo fromAs = ipasInfoSearch.searchIPASInfo(from);
            ASInfo toAs = ipasInfoSearch.searchIPASInfo(to);
            
            if (!fromAs.getOrg_name().toLowerCase().contains(keywords) && !toAs.getOrg_name().toLowerCase().contains(keywords)) {
                continue;
            }
            
            if (fromAs.getAut().equals(toAs.getAut())) {
                continue;
            }
            
            if (isCompare(fromAs.getAut(), toAs.getAut())) {
                map.put(fromAs.getAut() + "\t" + toAs.getAut(), map.get(fromAs.getAut() + "\t" + toAs.getAut()) == null ? 1 : map.get(fromAs.getAut() + "\t" + toAs.getAut()) + 1);
            } else {
                map.put(toAs.getAut() + "\t" + fromAs.getAut(), map.get(toAs.getAut() + "\t" + fromAs.getAut()) == null ? 1 : map.get(toAs.getAut() + "\t" + fromAs.getAut()) + 1);
            }
            
            map2.put(fromAs.getAut(), fromAs.getAut_name());
            map2.put(toAs.getAut(), toAs.getAut_name());
            if (fromAs.getOrg_name().toLowerCase().contains(keywords)) {
                asSet.add(fromAs.getAut());
            } else {
                asSet.add(toAs.getAut());
            }
        }
        
        Set<String> asSet1 = new HashSet<>();
        for (String as : asSet) {
            if (as.contains("_")) {
                String[] ss = as.split("_");
                asSet1.addAll(Arrays.asList(ss));
                
                continue;
            }
            asSet1.add(as);
            
        }
        
        System.out.println("as数量：" + asSet1.size());
        System.out.println(asSet1);
        
        Map<String, Integer> map1 = new HashMap<>();
        
        for (String s : map.keySet()) {
            if (s.contains("_")) {
                String[] ss = s.split("\t");
                String[] from = ss[0].split("_");
                String[] to = ss[1].split("_");
                
                int flag = map.get(s);
                for (String s1 : to) {
                    for (String s2 : from) {
                        if (s1.equals(s2)) {
                            continue;
                        }
                        map2.put(s1, map2.get(ss[1]));
                        map2.put(s2, map2.get(ss[0]));
                        if (isCompare(s1, s2)) {
                            map1.put(s1 + "\t" + s2, map1.get(s1 + "\t" + s2) == null ? flag : map1.get(s1 + "\t" + s2) + flag);
                        } else {
                            map1.put(s2 + "\t" + s1, map1.get(s2 + "\t" + s1) == null ? flag : map1.get(s2 + "\t" + s1) + flag);
                        }
                    }
                }
            } else {
                map1.put(s, map.get(s));
            }
        }
        
        FileWriter writer = new FileWriter(keywords + ".txt");
        map1 = sortMap(map1);
        System.out.println(map1);
        for (String s : map1.keySet()) {
            String[] ss = s.split("\t");
            
            String from = ss[0];
            String to = ss[1];
            if (asSet1.contains(from)) {
                
                writer.write(from + "\t" + map2.get(from) + "\t" + to + "\t" + map2.get(to) + "\t" + map1.get(s) + "\n");
            }
        }
        writer.close();
    }*/
    
    public static void getOpenPort(String keywords) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader("D:\\Desktop\\洛杉矶\\节点分析\\洛杉矶设备分类json-有IDC.txt"));
        String line;
        List<IpInfo> ipInfos =  new ArrayList<>();
        keywords = keywords.toLowerCase();
        while((line = br.readLine()) != null) {
            IpInfo ipInfo = JSON.parseObject(line, IpInfo.class);
    
            ASInfo asInfo = ipInfo.getAsInfo();
            if (!asInfo.getOrg_name().toLowerCase().contains(keywords)) {
                continue;
            }
            IPLocation lc = ipInfo.getLocation();
            List<PortInfo> ports = ipInfo.getPortInfos();
            for (PortInfo port : ports) {
                if ("22".equals(port.getPort()) || "3389".equals(port.getPort())) {
                    if ("open".equals(port.state)) {
                        ipInfos.add(ipInfo);
                    }
                }
            }
        }
    
        FileWriter writer = new FileWriter("D:\\Desktop\\洛杉矶运营商相关\\开放远程端口\\" + keywords + "openPort.txt");
        for (IpInfo ipInfo : ipInfos) {
            String ip = ipInfo.getIp();
            String os = ipInfo.getOs();
            
            List<PortInfo> portInfos = ipInfo.getPortInfos();
            List<String> ports = new ArrayList<>();
    
           
            for (PortInfo portInfo : portInfos) {
                if ("open".equals(portInfo.state)) {
                    ports.add(portInfo.getPort());
                }
            }
            String p = NormalTool.listToString(ports);
            
            String num;
            List<Vul> vuls = ipInfo.getVuls();
            num = vuls.size() + "";
            
            writer.write(ip + "\t" + os + "\t" + p + "\t" + num + "\r\n");
        }
        
        writer.close();
    }
    
    public static boolean isCompare(String s1, String s2) {
        if (s1.compareTo(s2) > 0) {
            return true;
        }
        if (s1.compareTo(s2) < 0) {
            return false;
        }
        
        return false;
    }
    
    public static void getIspIpInfo(String keywords) throws Exception {
        // D:\Desktop\洛杉矶\节点分析\path_all.txt
        // 所有路径
        keywords = keywords.toLowerCase();
        BufferedReader br = new BufferedReader(new FileReader("洛杉矶设备分类json-无IDC.txt"));
        String line;
        // as
        Set<String> asSet = new HashSet<>();
        // 总ip
        Set<String> ipSet = new HashSet<>();
        // 活跃ip
        Set<String> activeIpSet = new HashSet<>();
        // dns
        Set<String> dnsSet = new HashSet<>();
        // 跨区域
        Set<String> hexinSet = new HashSet<>();
        // 跨as
        Set<String> bianjieSet = new HashSet<>();
        // 22或3389的端口open
        Set<String> openPortSet = new HashSet<>();
        
        while ((line = br.readLine()) != null) {
            IpInfo ipInfo = JSON.parseObject(line, IpInfo.class);
            
            ASInfo asInfo = ipInfo.getAsInfo();
            if (!asInfo.getOrg_name().toLowerCase().contains(keywords)) {
                continue;
            }
            IPLocation lc = ipInfo.getLocation();
            List<PortInfo> ports = ipInfo.getPortInfos();
            for (PortInfo port : ports) {
                if ("22".equals(port.getPort()) || "3389".equals(port.getPort())) {
                    if ("open".equals(port.state)) {
                        openPortSet.add(ipInfo.getIp());
                    }
                }
            }
        }
        
        br = new BufferedReader(new FileReader("D:\\Desktop\\洛杉矶\\ip\\洛杉矶全部ip-修正后.txt"));
        IPASInfoSearch ipasInfoSearch = new IPASInfoSearch(IPASInfoSearch.asDir);
        while ((line = br.readLine()) != null) {
            ASInfo asInfo = ipasInfoSearch.searchIPASInfo(line);
            
            if (asInfo.getOrg_name().toLowerCase().contains(keywords)) {
                ipSet.add(line);
            }
        }
        
        br = new BufferedReader(new FileReader("D:\\Desktop\\洛杉矶\\节点分析\\router_all.txt"));
        Set<String> routerSet = new HashSet<>();
        while ((line = br.readLine()) != null) {
            routerSet.add(line);
        }
        
        
        br = new BufferedReader(new FileReader("D:\\Desktop\\洛杉矶\\ip\\洛杉矶活跃IP-修正后.txt"));
        while ((line = br.readLine()) != null) {
            ASInfo asInfo = ipasInfoSearch.searchIPASInfo(line);
            
            if (asInfo.getOrg_name().toLowerCase().contains(keywords)) {
                activeIpSet.add(line);
            }
        }
        
        br = new BufferedReader(new FileReader("D:\\Desktop\\洛杉矶\\节点分析\\设备-DNS.txt"));
        while ((line = br.readLine()) != null) {
            String ip = line.split("\t")[0];
            ASInfo asInfo = ipasInfoSearch.searchIPASInfo(ip);
            
            if (asInfo.getOrg_name().toLowerCase().contains(keywords)) {
                dnsSet.add(ip);
            }
        }
    
        /*br = new BufferedReader(new FileReader("D:\\Desktop\\洛杉矶\\节点分析\\洛杉矶-汇聚路由器.txt"));
        while ((line = br.readLine()) != null) {
            String ip = line.split("\t")[0];
            ASInfo asInfo = ipasInfoSearch.searchIPASInfo(ip);
        
            if (asInfo.getOrg_name().toLowerCase().contains(keywords)) {
                bianjieSet.add(ip);
            }
        }*/
        
        br = new BufferedReader(new FileReader("D:\\Desktop\\洛杉矶\\节点分析\\path_all.txt"));
        
        while ((line = br.readLine()) != null) {
            String from = line.split("\t")[0];
            String to = line.split("\t")[1];
            
            ASInfo asFrom = ipasInfoSearch.searchIPASInfo(from);
            ASInfo asTo = ipasInfoSearch.searchIPASInfo(to);
            
            if (!asFrom.getOrg_name().toLowerCase().contains(keywords) && !asTo.getOrg_name().toLowerCase().contains(keywords)) {
                continue;
            }
            
            if (asFrom.getAut().equals(asTo.getAut())) {
                continue;
            }
            
            
            if (asFrom.getOrg_name().toLowerCase().contains(keywords)) {
                asSet.add(asFrom.getAut());
                if (routerSet.contains(from)) {
                    bianjieSet.add(from);
                }
            } else if (asTo.getOrg_name().toLowerCase().contains(keywords)) {
                asSet.add(asTo.getAut());
                if (routerSet.contains(to)) {
                    bianjieSet.add(to);
                }
            }
        }
        
        br = new BufferedReader(new FileReader("D:\\Desktop\\洛杉矶\\节点分析\\洛杉矶-核心路由器.txt"));
        while ((line = br.readLine()) != null) {
            String ip = line.split("\t")[0];
            ASInfo asInfo = ipasInfoSearch.searchIPASInfo(ip);
            
            if (asInfo.getOrg_name().toLowerCase().contains(keywords)) {
                hexinSet.add(ip);
            }
        }
        
        Set<String> asSet1 = new HashSet<>();
        for (String as : asSet) {
            if (as.contains("_")) {
                String[] ss = as.split("_");
                asSet1.addAll(Arrays.asList(ss));
                
                continue;
            }
            asSet1.add(as);
            
        }
        
        System.out.println("as：" + asSet1.size() + "\t" + asSet1);
        System.out.println("拥有ip：" + ipSet.size());
        System.out.println("活跃ip：" + activeIpSet.size());
        System.out.println("dns服务器：" + dnsSet.size());
        System.out.println("核心：" + hexinSet.size());
        System.out.println("边界：" + bianjieSet.size());
        System.out.println("开放远程端口：" + openPortSet.size());
    
        FileWriter writer = new FileWriter("D:\\Desktop\\洛杉矶运营商相关\\边界路由器\\" + keywords + ".txt");
        for (String s : bianjieSet) {
            writer.write(s);
        }
        
        writer.close();
    }
    
    // 得到以公司为keyword的ip的相关信息
    public static void getIspIpInfo(String keywords1, String keywords2) throws Exception {
        // D:\Desktop\洛杉矶\节点分析\path_all.txt
        // 所有路径
        keywords1 = keywords1.toLowerCase();
        keywords2 = keywords2.toLowerCase();
        BufferedReader br = new BufferedReader(new FileReader("洛杉矶设备分类json-无IDC.txt"));
        String line;
        // as
        Set<String> asSet = new HashSet<>();
        // 总ip
        Set<String> ipSet = new HashSet<>();
        // 活跃ip
        Set<String> activeIpSet = new HashSet<>();
        // dns
        Set<String> dnsSet = new HashSet<>();
        // 跨区域
        Set<String> hexinSet = new HashSet<>();
        // 跨as
        Set<String> bianjieSet = new HashSet<>();
        // 22或3389的端口open
        Set<String> openPortSet = new HashSet<>();
        
        while ((line = br.readLine()) != null) {
            IpInfo ipInfo = JSON.parseObject(line, IpInfo.class);
            
            ASInfo asInfo = ipInfo.getAsInfo();
            if (!asInfo.getOrg_name().toLowerCase().contains(keywords1) && !asInfo.getOrg_name().toLowerCase().contains(keywords2)) {
                continue;
            }
            IPLocation lc = ipInfo.getLocation();
            List<PortInfo> ports = ipInfo.getPortInfos();
            for (PortInfo port : ports) {
                if ("22".equals(port.getPort()) || "3389".equals(port.getPort())) {
                    if ("open".equals(port.state)) {
                        openPortSet.add(ipInfo.getIp());
                    }
                }
            }
        }
        
        br = new BufferedReader(new FileReader("D:\\Desktop\\洛杉矶\\ip\\洛杉矶全部ip-修正后.txt"));
        IPASInfoSearch ipasInfoSearch = new IPASInfoSearch(IPASInfoSearch.asDir);
        while ((line = br.readLine()) != null) {
            ASInfo asInfo = ipasInfoSearch.searchIPASInfo(line);
            
            if (asInfo.getOrg_name().toLowerCase().contains(keywords1) || asInfo.getOrg_name().toLowerCase().contains(keywords2)) {
                ipSet.add(line);
            }
        }
        
        br = new BufferedReader(new FileReader("D:\\Desktop\\洛杉矶\\ip\\洛杉矶活跃IP-修正后.txt"));
        while ((line = br.readLine()) != null) {
            ASInfo asInfo = ipasInfoSearch.searchIPASInfo(line);
            
            if (asInfo.getOrg_name().toLowerCase().contains(keywords1) || asInfo.getOrg_name().toLowerCase().contains(keywords2)) {
                activeIpSet.add(line);
            }
        }
        
        br = new BufferedReader(new FileReader("D:\\Desktop\\洛杉矶\\节点分析\\设备-DNS.txt"));
        while ((line = br.readLine()) != null) {
            String ip = line.split("\t")[0];
            ASInfo asInfo = ipasInfoSearch.searchIPASInfo(ip);
            
            if (asInfo.getOrg_name().toLowerCase().contains(keywords1) || asInfo.getOrg_name().toLowerCase().contains(keywords2)) {
                dnsSet.add(ip);
            }
        }
    
        /*br = new BufferedReader(new FileReader("D:\\Desktop\\洛杉矶\\节点分析\\洛杉矶-汇聚路由器.txt"));
        while ((line = br.readLine()) != null) {
            String ip = line.split("\t")[0];
            ASInfo asInfo = ipasInfoSearch.searchIPASInfo(ip);
        
            if (asInfo.getOrg_name().toLowerCase().contains(keywords)) {
                bianjieSet.add(ip);
            }
        }*/
        
        br = new BufferedReader(new FileReader("D:\\Desktop\\洛杉矶\\节点分析\\path_all.txt"));
        
        while ((line = br.readLine()) != null) {
            String from = line.split("\t")[0];
            String to = line.split("\t")[1];
            
            ASInfo asFrom = ipasInfoSearch.searchIPASInfo(from);
            ASInfo asTo = ipasInfoSearch.searchIPASInfo(to);
            
            if ((!asFrom.getOrg_name().toLowerCase().contains(keywords1) && !asTo.getOrg_name().toLowerCase().contains(keywords1))
                    && !asFrom.getOrg_name().toLowerCase().contains(keywords2) && !asTo.getOrg_name().toLowerCase().contains(keywords2)) {
                continue;
            }
            
            if (asFrom.getAut().equals(asTo.getAut())) {
                continue;
            }
            
            
            if (asFrom.getOrg_name().toLowerCase().contains(keywords1)) {
                asSet.add(asFrom.getAut());
                bianjieSet.add(from);
            } else {
                asSet.add(asTo.getAut());
                bianjieSet.add(to);
            }
            
            if (asFrom.getOrg_name().toLowerCase().contains(keywords2)) {
                asSet.add(asFrom.getAut());
                bianjieSet.add(from);
            } else {
                asSet.add(asTo.getAut());
                bianjieSet.add(to);
            }
        }
        
        br = new BufferedReader(new FileReader("D:\\Desktop\\洛杉矶\\节点分析\\洛杉矶-核心路由器.txt"));
        while ((line = br.readLine()) != null) {
            String ip = line.split("\t")[0];
            ASInfo asInfo = ipasInfoSearch.searchIPASInfo(ip);
            
            if (asInfo.getOrg_name().toLowerCase().contains(keywords1) || asInfo.getOrg_name().toLowerCase().contains(keywords2)) {
                hexinSet.add(ip);
            }
        }
        
        Set<String> asSet1 = new HashSet<>();
        for (String as : asSet) {
            if (as.contains("_")) {
                String[] ss = as.split("_");
                asSet1.addAll(Arrays.asList(ss));
                
                continue;
            }
            asSet1.add(as);
            
        }
        
        System.out.println("as：" + asSet1.size() + "\t" + asSet1);
        System.out.println("拥有ip：" + ipSet.size());
        System.out.println("活跃ip：" + activeIpSet.size());
        System.out.println("dns服务器：" + dnsSet.size());
        System.out.println("核心：" + hexinSet.size());
        System.out.println("边界：" + bianjieSet.size());
        System.out.println("开放远程端口：" + openPortSet.size());
    }
    
    public static Map<String, Integer> sortMap(Map<String, Integer> map) {
        //利用Map的entrySet方法，转化为list进行排序
        List<Map.Entry<String, Integer>> entryList = new ArrayList<>(map.entrySet());
        //利用Collections的sort方法对list排序
        Collections.sort(entryList, (o1, o2) -> {
            //正序排列，倒序反过来
            return o2.getValue() - o1.getValue();
        });
        //遍历排序好的list，一定要放进LinkedHashMap，因为只有LinkedHashMap是根据插入顺序进行存储
        LinkedHashMap<String, Integer> linkedHashMap = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> e : entryList) {
            linkedHashMap.put(e.getKey(), e.getValue());
        }
        return linkedHashMap;
    }
    
    // 过滤掉非洛杉矶的ip的端口信息
    public static void portServiceFilter() throws Exception {
        BufferedReader br = new BufferedReader(new FileReader("D:\\Desktop\\洛杉矶\\端口服务测量数据.txt"));
        String line;
        FileWriter writer = new FileWriter("D:\\Desktop\\洛杉矶\\端口服务测量数据-修正后.txt");
        IPASInfoSearch ipasInfoSearch = new IPASInfoSearch("D:\\dataCollect\\ASInfo.txt");
        
        
        while ((line = br.readLine()) != null) {
            JSONObject json = JSON.parseObject(line);
            
            String ip = json.getString("ip");
            ASInfo asInfo = ipasInfoSearch.searchIPASInfo(ip);
            IPLocation lc = GetIPLocation.get(ip);
            
            if ("US".equals(asInfo.getOrg_country()) && lc.getCountry().contains("美国")) {
                writer.write(line + "\r\n");
                //writer.flush();
            }
        }
        writer.close();
    }
}
