package org.lx.topo.action;

import cn.hutool.core.net.Ipv4Util;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.lx.pojo.*;
import org.lx.service.IPASInfoSearch;
import org.lx.tools.FileLineTool;
import org.lx.tools.ip.GetIPLocation;
import org.lx.tools.ip.IPLocation;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.lx.tools.ip.IPDao.IPToLong;

/**
 * @author yuntao
 * @date 2022/7/26
 */
public class FileHandle {
    public static void nmapJsonFileHandle(String dir) throws Exception {
        System.out.println(-1);
        Map<String, NmapInfo> map = new HashMap<>();
        BufferedReader br = new BufferedReader(new FileReader("D:\\\\Desktop\\\\洛杉矶\\\\节点分析\\\\端口服务测量数据-修正后.txt"));
        
        String l;
        
        while ((l = br.readLine()) != null) {
            NmapInfo nmapInfo = JSON.parseObject(l, NmapInfo.class);
            map.put(nmapInfo.getIp(), nmapInfo);
            if (map.keySet().size() % 10000 == 0) {
                System.out.println(map.keySet().size());
            }
        }
       /* FileLineTool.readLineWithThreads(new File("D:\\Desktop\\洛杉矶\\节点分析\\端口服务测量数据-修正后.txt"), line -> {
            NmapInfo nmapInfo = JSON.parseObject(line, NmapInfo.class);
            map.put(nmapInfo.getIp(), nmapInfo);
            System.out.println(0);
        },100);*/
        
        String line;
        FileWriter writer = new FileWriter("res.txt");
        br = new BufferedReader(new FileReader(dir));
        System.out.println(1);
        IPASInfoSearch ipasInfoSearch = new IPASInfoSearch("D:\\dataCollect\\ASInfo.txt");
        
        while ((line = br.readLine()) != null) {
            System.out.println(2);
            String ip = line.split("\t")[0];
            System.out.println(ip);
            String type = line.split("\t")[1];
            System.out.println(type);
            NmapInfo nmapInfo = map.get(ip);
            List<PortInfo> portInfos = new ArrayList<>();
            String os = "none";
            List<Vul> vuls = new ArrayList<>();
            Set<String> set = new HashSet<>();
            
            if (nmapInfo == null) {
                portInfos = null;
            } else {
                portInfos = nmapInfo.getPortInfos();
                os = nmapInfo.getOs(type);
                List<PortInfo> list = nmapInfo.getPortInfos();
                
                for (PortInfo portInfo : list) {
                    List<String> vulList = portInfo.getVul();
                    if (vulList == null) {
                        continue;
                    }
                    for (String s : vulList) {
                        String[] ss = s.split("-");
                        if (Integer.parseInt(ss[1]) >= 2005) {
                            set.add(s);
                        }
                    }
                    portInfo.setVul(null);
                }
                if (set.size() > 0) {
                    for (String s : set) {
                        Vul v = new Vul();
                        v.setNum(s);
                        vuls.add(v);
                    }
                }
            }
            
            IPLocation location = GetIPLocation.get(ip);
            ASInfo asInfo = ipasInfoSearch.searchIPASInfo(ip);
            
            List<String> tags = new ArrayList<>();
            tags.add(type);
            //if (isIdc(Ipv4Util.ipv4ToLong(ip))) {
            //    tags.add("IDC");
            //}
            IpInfo ipInfo = new IpInfo();
            ipInfo.setIp(ip);
            ipInfo.setTags(tags);
            ipInfo.setAsInfo(asInfo);
            ipInfo.setPortInfos(portInfos);
            ipInfo.setLocation(location);
            ipInfo.setOs(os);
            ipInfo.setCompany("待定");
            ipInfo.setVuls(vuls);
            String jsonString = JSON.toJSONString(ipInfo);
            
            //res.add(jsonString);
            
            writer.write(jsonString + "\r\n");
            writer.flush();
        }
        
        //Vector<String> res = new Vector<>();
        //FileLineTool.readLineWithThreads(new File(dir), line -> {
        //
        //},100);
        //
        //for (String re : res) {
        //    writer.write(re + "\r\n");
        //}
        
        writer.close();
    }
    
    public static Set<String> eqiupHead(File file) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;
        Set<String> set = new HashSet<>();
        while ((line = br.readLine()) != null) {
            String[] ss = line.split("\t");
            String ip = ss[0];
            
            set.add(ip.split("\\.")[0]);
        }
        
        return set;
    }
    
    public static Set<String> addIdcTag(File file) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(file));
        Set<String> headSet = eqiupHead(new File("D:\\Desktop\\关岛数据重新整理\\节点分析\\设备分类.txt"));
        String line;
        Set<String> set = new HashSet<>();
        while ((line = br.readLine()) != null) {
            String[] ss = line.split("\t");
            
            String startIp = ss[0];
            String endIp = ss[1];
            while (startIp.startsWith("0")) {
                startIp = startIp.substring(1);
            }
            if (!headSet.contains(startIp.split("\\.")[0])) {
                continue;
            }
            System.out.println(startIp.split("\\.")[0]);
            List<String> list = Ipv4Util.list(startIp, endIp);
            set.addAll(list);
        }
        return set;
    }
    
    public static Set<String> addIdcTag(File file, Set<String> headset) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;
        Set<String> set = new HashSet<>();
        while ((line = br.readLine()) != null) {
            String[] ss = line.split("\t");
            
            String startIp = ss[0];
            String endIp = ss[1];
            while (startIp.startsWith("0")) {
                startIp = startIp.substring(1);
            }
            if (!headset.contains(startIp.split("\\.")[0])) {
                continue;
            }
            
            List<String> list = Ipv4Util.list(startIp, endIp);
            set.addAll(list);
        }
        return set;
    }
    
    public static void addRouter(String dir) throws Exception {
        IPASInfoSearch ipasInfoSearch = new IPASInfoSearch("D:\\dataCollect\\ASInfo.txt");
        FileWriter writer = new FileWriter("D:\\Desktop\\关岛数据重新整理\\节点分析\\设备分类.txt", true);
        FileLineTool.readLineWithTrim(new File(dir), line -> {
            String[] ss = line.split("\t");
            String ip = ss[0].trim();
            IPLocation location = GetIPLocation.get(ip);
            ASInfo asInfo = ipasInfoSearch.searchIPASInfo(ip);
            
            if ("关岛".equals(location.getCountry())) {
                writer.write(ip + "\t" + "路由器" + "\t\t" + ((asInfo.getIsp().equals("*") ? asInfo.getOrg_name() : asInfo.getIsp())) + "\t" + location.getCountry() + "\t" + location.getProvince() + "\t" + location.getCity() + "\n");
            }
            
        });
        
        writer.close();
    }
    
    public static void checkFile() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader("D:\\Desktop\\关岛数据重新整理\\节点分析\\设备分类.txt"));
        
        String line;
        Set<String> set = new HashSet<>();
        while ((line = br.readLine()) != null) {
            set.add(line.split("\t")[0]);
        }
        
        br = new BufferedReader(new FileReader("D:\\Desktop\\关岛数据重新整理\\节点分析\\全部路由器.txt"));
        while ((line = br.readLine()) != null) {
            if (set.contains(line.split("\t")[0])) {
                System.out.println(line);
            }
        }
    }
    
    public static void checkAllRouter() throws Exception {
        BufferedReader br = new BufferedReader(new FileReader("D:\\Desktop\\关岛数据重新整理\\节点分析\\全部路由器.txt"));
        
        String line;
        Set<String> set = new HashSet<>();
        while ((line = br.readLine()) != null) {
            IPLocation location = GetIPLocation.get(line);
            if ("关岛".equals(location.getCountry())) {
                set.add(line);
            }
        }
        
        FileWriter writer = new FileWriter("关岛路由器.txt");
        for (String key : set) {
            writer.write(key + "\n");
        }
        writer.close();
    }
    
    public static void idcSet(String dir) throws Exception {
        
        BufferedReader br = new BufferedReader(new FileReader(dir));
        FileWriter writer = new FileWriter("有idc的版本.txt");
        String line;
        Set<String> idcSet = addIdcTag(new File("D:\\Desktop\\IDCList.txt"));
        System.out.println(2);
        while ((line = br.readLine()) != null) {
            JSONObject json = JSON.parseObject(line);
            if (idcSet.contains(json.getString("ip"))) {
                JSONArray arr = json.getJSONArray("tags");
                arr.add("IDC");
                json.put("tags", arr);
                System.out.println(1);
            }
            writer.write(json.toJSONString() + "\r\n");
        }
        
        writer.close();
    }
    
    public static String getIpHead(String ip) {
        return ip.split("\\.")[0];
    }
    
    // 数据量大
    public static void addRouter1(String dir) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader("D:\\Desktop\\洛杉矶\\节点分析\\洛杉矶-全部路由器.txt"));
        int i = 0;
        String line;
        Set<String> set = new HashSet<>();
        while ((line = br.readLine()) != null) {
            set.add(line);
        }
        br = new BufferedReader(new FileReader(dir));
        FileWriter writer = new FileWriter("设备分类-有路由器.txt");
        while ((line = br.readLine()) != null) {
            if (set.contains(line.split("\t")[0])) {
                writer.write(line.split("\t")[0] + "\t" + "路由器" + "\r\n");
            } else {
                writer.write(line + "\r\n");
            }
        }
        
        writer.close();
         br = new BufferedReader(new FileReader("设备分类-有路由器json.txt"));
        
        Set<String> set1 = new HashSet<>();
        while((line = br.readLine()) != null) {
            JSONObject json = JSON.parseObject(line);
            set1.add(json.getString("ip"));
        }
        
        FileWriter w = new FileWriter("设备分类-有路由器json.txt", true);
        FileLineTool.readLineWithThreads(new File("D:\\Desktop\\洛杉矶\\节点分析\\洛杉矶-全部路由器.txt"), l -> {

            if (!set1.contains(l)) {
                String ip = l;
                String type = "路由器";
    
                IpInfo ipInfo = new IpInfo();
                ipInfo.setIp(ip);
                List<String> tags = new ArrayList<>();
    
                tags.add(type);
                if (readIDCFile(Ipv4Util.ipv4ToLong(ip))) {
                    tags.add("IDC");
                }
                ipInfo.setTags(tags);
                w.write(JSON.toJSONString(ipInfo) + "\r\n");
            }
            
            }, 100);

        w.close();
    }
    
    // 数据量大
    public static void nmapJsonFileHandle1(String dir) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(dir));
        String line;
        Map<String, String> map = new HashMap<>();
        while ((line = br.readLine()) != null) {
            String ip = line.split("\t")[0];
            String type = line.split("\t")[1];
            map.put(ip, type);
        }
        
        //br = new BufferedReader(new FileReader("D:\\Desktop\\洛杉矶\\节点分析\\端口服务测量数据-修正后.txt"));
        
        AtomicInteger i = new AtomicInteger();
        FileWriter writer = new FileWriter("洛杉矶设备分类json-有IDC.txt");
        IPASInfoSearch ipasInfoSearch = new IPASInfoSearch("D:\\dataCollect\\ASInfo.txt");
        
        FileLineTool.readLineWithTrimThreads(new File("D:\\Desktop\\洛杉矶\\节点分析\\端口服务测量数据-修正后.txt"),
                l -> {
                    JSONObject json = JSON.parseObject(l);
                    String ip = json.getString("ip");
                    if (map.containsKey(ip)) {
                        String type = map.get(ip);
                        NmapInfo nmapInfo = JSON.parseObject(l, NmapInfo.class);
                        List<PortInfo> portInfos;
                        String os = "none";
                        List<Vul> vuls = new ArrayList<>();
                        Set<String> set = new HashSet<>();
        
                        if (nmapInfo == null) {
                            portInfos = null;
                        } else {
                            portInfos = nmapInfo.getPortInfos();
                            os = nmapInfo.getOs(type);
                            List<PortInfo> list = nmapInfo.getPortInfos();
            
                            for (PortInfo portInfo : list) {
                                List<String> vulList = portInfo.getVul();
                                if (vulList == null) {
                                    continue;
                                }
                                for (String s : vulList) {
                                    String[] ss = s.split("-");
                                    if (Integer.parseInt(ss[1]) >= 2005) {
                                        set.add(s);
                                    }
                                }
                                portInfo.setVul(null);
                            }
                            if (set.size() > 0) {
                                for (String s : set) {
                                    Vul v = new Vul();
                                    v.setNum(s);
                                    vuls.add(v);
                                }
                            }
                        }
        
                        IPLocation location = GetIPLocation.get(ip);
                        ASInfo asInfo = ipasInfoSearch.searchIPASInfo(ip);
        
                        List<String> tags = new ArrayList<>();
                        tags.add(type);
                        System.out.println("idcKaishi ");
                        if (readIDCFile(Ipv4Util.ipv4ToLong(ip))) {
                            tags.add("IDC");
                        }
                        System.out.println("idc结束");
                        IpInfo ipInfo = new IpInfo();
                        ipInfo.setIp(ip);
                        ipInfo.setTags(tags);
                        ipInfo.setAsInfo(asInfo);
                        ipInfo.setPortInfos(portInfos);
                        ipInfo.setLocation(location);
                        ipInfo.setOs(os);
                        ipInfo.setCompany("待定");
                        ipInfo.setVuls(vuls);
                        String jsonString = JSON.toJSONString(ipInfo);
        
                        //res.add(jsonString);
                        synchronized (new Object()) {
                            writer.write(jsonString + "\r\n");
                        }
                        System.out.println(i.incrementAndGet());
                }},
                100);
        
        writer.close();
    }
    
    public static void addIdc() throws Exception {
        
        BufferedReader br = new BufferedReader(new FileReader("不是idc.txt"));
        String line;
        Set<String> set = new HashSet<>();
        while((line = br.readLine()) != null) {
            set.add(line);
        }
        
        br = new BufferedReader(new FileReader("洛杉矶设备分类json-无IDC.txt"));
        
        FileWriter writer = new FileWriter("洛杉矶设备分类json-有IDC.txt");
        while((line = br.readLine()) != null) {
            JSONObject json = JSON.parseObject(line);
            
            String ip = json.getString("ip");
            
            if (set.contains(ip)) {
                writer.write(line + "\r\n");
            }else {
                IpInfo ipInfo = JSON.parseObject(line, IpInfo.class);
    
                List<String> tags = ipInfo.getTags();
                tags.add("IDC");
                ipInfo.setTags(tags);
                String res = JSON.toJSONString(ipInfo);
                writer.write(res + "\r\n");
            }
            
            writer.flush();
        }
        
        writer.close();
        /*File file = new File("洛杉矶设备分类json-无IDC.txt");
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;
        List<String> list = new ArrayList<>();
        while((line = br.readLine()) != null) {
            JSONObject json = JSON.parseObject(line);
            
            list.add(json.getString("ip"));
        }
    
        Set<String> idcs = new HashSet<>();
        int i  = 0;
        for (String ip : list) {
            if (readIDCFile(Ipv4Util.ipv4ToLong(ip))) {
                idcs.add(ip);
                System.out.println(++i);
            }
        }
        
        FileWriter writer = new FileWriter("有idc的ip.txt");
    
        for (String idc : idcs) {
            writer.write(idc + "\r\n");
        }
        writer.close();*/
        
        
        /*br = new BufferedReader(new FileReader(file));
        FileWriter writer = new FileWriter("洛杉矶设备分类json-有IDC.txt");
        while ((line = br.readLine()) != null) {
            JSONObject json = JSON.parseObject(line);
            
            String ip = json.getString("ip");
            
            if (idcs.contains(ip)) {
                IpInfo ipInfo = JSON.parseObject(line, IpInfo.class);
                
                List<String> tags = ipInfo.getTags();
                tags.add("IDC");
                ipInfo.setTags(tags);
                String res = JSON.toJSONString(ipInfo);
                writer.write(res + "\r\n");
            }else {
                writer.write(line + "\r\n");
            }
        }
        writer.close();*/
    }
    
    public static void mergeRouterFileAndJsonFile() throws Exception {
        BufferedReader br = new BufferedReader(new FileReader("设备分类-有路由器json.txt"));
        String line;
        Map<String, List<String>> routerMap = new HashMap<>(160000);
        while((line = br.readLine()) != null) {
            IpInfo ipInfo = JSON.parseObject(line, IpInfo.class);
            
            routerMap.put(ipInfo.getIp(), ipInfo.getTags());
        }
        
        FileWriter writer = new FileWriter("洛杉矶设备分类json-有IDC-修正后.txt");
        Set<String> ipList = new HashSet<>();
        FileLineTool.readLineWithThreads(new File("洛杉矶设备分类json-有IDC.txt"), l -> {
            IpInfo ipInfo = JSON.parseObject(l, IpInfo.class);
            
            if (routerMap.containsKey(ipInfo.getIp())) {
                ipInfo.setTags(routerMap.get(ipInfo.getIp()));
                ipList.add(ipInfo.getIp());
                writer.write(JSON.toJSONString(ipInfo) + "\r\n");
            }else {
                writer.write(l + "\r\n");
            }
        }, 100);
        writer.close();
        
        Map<String, List<String>> set1 = new HashMap<>();
        for (String s : routerMap.keySet()) {
            if (!ipList.contains(s)) {
                set1.put(s, routerMap.get(s));
            }
        }
        
        FileWriter writer1 = new FileWriter("洛杉矶设备分类json-有IDC-修正后.txt", true);
        IPASInfoSearch ipasInfoSearch = new IPASInfoSearch("D:\\dataCollect\\ASInfo.txt");
    
        FileLineTool.readLineWithTrimThreads(new File("D:\\Desktop\\洛杉矶\\节点分析\\路由器端口服务测量数据.txt"),
                l -> {
                    JSONObject json = JSON.parseObject(l);
                    String ip = json.getString("ip");
                    if (set1.containsKey(ip)) {
                        List<String> tags = set1.get(ip);
                        NmapInfo nmapInfo = JSON.parseObject(l, NmapInfo.class);
                        List<PortInfo> portInfos;
                        String os = "none";
                        List<Vul> vuls = new ArrayList<>();
                        Set<String> set = new HashSet<>();
                    
                        if (nmapInfo == null) {
                            portInfos = null;
                        } else {
                            portInfos = nmapInfo.getPortInfos();
                            os = nmapInfo.getOs(tags.get(0));
                            List<PortInfo> list = nmapInfo.getPortInfos();
                        
                            for (PortInfo portInfo : list) {
                                List<String> vulList = portInfo.getVul();
                                if (vulList == null) {
                                    continue;
                                }
                                for (String s : vulList) {
                                    String[] ss = s.split("-");
                                    if (Integer.parseInt(ss[1]) >= 2005) {
                                        set.add(s);
                                    }
                                }
                                portInfo.setVul(null);
                            }
                            if (set.size() > 0) {
                                for (String s : set) {
                                    Vul v = new Vul();
                                    v.setNum(s);
                                    vuls.add(v);
                                }
                            }
                        }
                    
                        IPLocation location = GetIPLocation.get(ip);
                        ASInfo asInfo = ipasInfoSearch.searchIPASInfo(ip);
                        
                        IpInfo ipInfo = new IpInfo();
                        ipInfo.setIp(ip);
                        ipInfo.setTags(tags);
                        ipInfo.setAsInfo(asInfo);
                        ipInfo.setPortInfos(portInfos);
                        ipInfo.setLocation(location);
                        ipInfo.setOs(os);
                        ipInfo.setCompany("待定");
                        ipInfo.setVuls(vuls);
                        String jsonString = JSON.toJSONString(ipInfo);
    
                        writer1.write(jsonString + "\r\n");
                        
                    }},
                100);
    
            writer1.close();
    }
    
    public static void main(String[] args) throws Exception {
        /*try {
            nmapJsonFileHandle1("D:\\Desktop\\洛杉矶\\节点分析\\设备分类-有路由器.txt");
        } catch (Exception e) {
            e.printStackTrace();
        }*/
        // addIdc();
       
       
       
       // 对路由器进行idc判定
       // addRouter1("D:\\Desktop\\洛杉矶\\节点分析\\设备分类-无路由器.txt");
    
        
        // 合并idc判定后的文件
        mergeRouterFileAndJsonFile();
        
/*        System.out.println(readIDCFile(Ipv4Util.ipv4ToLong("223.255.252.001")));
        */
        //checkFile();
        //checkAllRouter();
        //addRouter("D:\\Desktop\\关岛数据重新整理\\节点分析\\关岛路由器.txt");
        
        //addFile("D:\\Desktop\\关岛数据(1).txt", "1");
        
        //idcSet("D:\\Desktop\\关岛设备匹配结果-未匹配IDC.txt");
        
        
        //IPASInfoSearch ipasInfoSearch = new IPASInfoSearch("D:\\dataCollect\\ASInfo.txt");
        //Map<String, NmapInfo> map = new HashMap<>();
        //FileLineTool.readLineWithThreads(new File("D:\\Desktop\\关岛数据重新整理\\节点分析\\端口服务测量数据.txt"), line -> {
        //    NmapInfo nmapInfo = JSON.parseObject(line, NmapInfo.class);
        //    map.put(nmapInfo.getIp(), nmapInfo);
        //},1000);
        //while (true) {
        //    System.out.println(11);
        //    Scanner sc = new Scanner(System.in);
        //
        //    String ip = sc.nextLine();
        //    ASInfo asInfo = ipasInfoSearch.searchIPASInfo(ip);
        //    IPLocation location = GetIPLocation.get(ip);
        //    NmapInfo nmapInfo = map.get(ip);
        //    System.out.println(nmapInfo.getOpenPortInfos());
        //    System.out.println(asInfo.getAllInfo());
        //    System.out.println(location.getCountry());
        //}
    }
    
    public static Boolean readIDCFile(Long targetIp) throws IOException {
        BufferedReader bufferedReader1 = new BufferedReader(new FileReader("D:\\Desktop\\IDCList.txt"));
        
        String line1 = "";
        while ((line1 = bufferedReader1.readLine()) != null) {
            String[] strings = line1.split("\\s+");
            if(targetIp >= IPToLong(strings[0]) && targetIp <= IPToLong(strings[1])) {
                bufferedReader1.close();
                return true;
            }
        }
        bufferedReader1.close();
        return false;
    }
    
    
    {
        /* br = new BufferedReader(new FileReader("在路径中的洛杉矶路由器.txt"));

        Set<String> routerSet = new HashSet<>();
        while ((line = br.readLine()) != null) {
            routerSet.add(line);
        }

        System.out.println(routerSet.size());

        br = new BufferedReader(new FileReader("D:\\Desktop\\洛杉矶\\节点分析\\path_all.txt"));

        Map<String, Integer> map = new HashMap<>();
        Set<String> set = new HashSet<>();
        int i = 0;
        while ((line = br.readLine()) != null) {
            String from = line.split("\t")[0];
            String to = line.split("\t")[1];

            if (endSet.contains(from)) {
                if (routerSet.contains(to)) {
                    mapPutFun(map, to);
                }else {
                    ++i;
                }
            }else if (endSet.contains(to)) {
                if (routerSet.contains(from)) {
                    mapPutFun(map, from);
                }else {
                    ++i;
                }
            }
        }
        System.out.println(i);
        map = sortMap(map);

        for (String s : map.keySet()) {
            System.out.println(s + "\t" + map.get(s));
        }*/
    }
}
