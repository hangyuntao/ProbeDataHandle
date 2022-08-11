package org.lx.topo.action;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sun.istack.internal.NotNull;
import org.lx.pojo.ASInfo;
import org.lx.pojo.IpInfo;
import org.lx.pojo.Vul;
import org.lx.service.IPASInfoSearch;
import org.lx.tools.ip.GetIPLocation;
import org.lx.tools.ip.IPLocation;
import org.lx.topo.action.pojo.CVE;
import org.lx.topo.action.pojo.IpPojo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;

/**
 * @author yuntao
 * @date 2022/7/27
 */
public class Test {
    
    public static IPASInfoSearch ipasInfoSearch;
    
    static {
        //try {
        //    ipasInfoSearch = new IPASInfoSearch("D:\\dataCollect\\ASInfo.txt");
        //} catch (Exception e) {
        //    throw new RuntimeException(e);
        //}
    }
    
    public static void main(String[] args) throws Exception {
        
        //addDnsAndEmail();
        
        BufferedReader br = new BufferedReader(new FileReader("D:\\Desktop\\洛杉矶\\节点分析\\洛杉矶-终端IP.txt"));
        String line;
        Set<String> endSet = new HashSet<>();
        while ((line = br.readLine()) != null) {
            endSet.add(line);
        }
    
       
        System.out.println(endSet.size());
    
        br = new BufferedReader(new FileReader("在路径中的洛杉矶路由器.txt"));

        Set<String> routerSet = new HashSet<>();
        while ((line = br.readLine()) != null) {
            routerSet.add(line);
        }

        System.out.println(routerSet.size());
        br = new BufferedReader(new FileReader("D:\\Desktop\\洛杉矶\\节点分析\\path_all.txt"));

        Map<String, Integer> map = new HashMap<>();
        
        int i = 0;
        int j = 0;
        int k = 0;
        int l = 0;
        Set<String> set = new HashSet<>();
        Set<String> set1 = new HashSet<>();
        Set<String> set2 = new HashSet<>();
        while ((line = br.readLine()) != null) {
            String from = line.split("\t")[0];
            String to = line.split("\t")[1];

            if (routerSet.contains(from) || routerSet.contains(to)) {
                k++;
                set.add(line);
                if (routerSet.contains(from) && routerSet.contains(to)) {
                    set1.add(line);
                    l++;
                }
                
                if (endSet.contains(from)) {
                    i++;
                    set2.add(line);
                    if (routerSet.contains(to)) {
                        j++;
                        mapPutFun(map, to);
                    }
                }
                if (endSet.contains(to)) {
                    i++;
                    set2.add(line);
                    if (routerSet.contains(from)) {
                        j++;
                        mapPutFun(map, from);
                    }
                }
                
            }
            
            
        }
    
        System.out.println();
        // 含有终端的边的条数
        System.out.println("有end的路径数：" + i);
        // 一端是end 一端是router的条数
        System.out.println("一端是end 一端是router的条数：" + j);
        // 含有router的边的条数
        System.out.println("含有router的边的条数:" + k);
        // 两边都是router的边的条数
        System.out.println("两边都是router的边的条数:" + l);
        map = sortMap(map);
    
        System.out.println(set.size());
        System.out.println(set1.size());
        System.out.println(set2.size());
    
    
        JSONObject result = new JSONObject();
        JSONArray nodes = new JSONArray();
        JSONArray edges = new JSONArray();
        
        Set<String> res = new HashSet<>();
        for (String s : set) {
            if (!set1.contains(s) && !set2.contains(s)) {
                System.out.println(s);
            }else {
                String from = s.split("\t")[0];
                String to = s.split("\t")[1];
                
                if (routerSet.contains(from)) {
                    res.add(from);
                }
                if (routerSet.contains(to)) {
                    res.add(to);
                }
                JSONObject edge = new JSONObject();
                edge.put("source", from);
                edge.put("target", to);
                edges.add(edge);
            }
        }
    
        FileWriter writer = new FileWriter("洛杉矶路由器调整版.txt");
        for (String re : res) {
            JSONObject node = new JSONObject();
            node.put("id", re);
            nodes.add(node);
            writer.write(re + "\r\n");
        }
        writer.close();
    
        System.out.println("res");
    
        System.out.println(res.size());
        
        
        result.put("nodes", nodes);
        result.put("edges", edges);
        
        FileWriter writer1 = new FileWriter("拓扑.txt");
        writer1.write(result.toJSONString());
        writer1.close();
        
        //br = new BufferedReader(new FileReader("D:\\Desktop\\洛杉矶\\节点分析\\path_all.txt"));
        //
        //Set<String> set1 = new HashSet<>();
        //Set<String> allIpSet = new HashSet<>();
        //List<String> list = new ArrayList<>();
        //while ((line = br.readLine()) != null) {
        //    String from = line.split("\t")[0];
        //    String to = line.split("\t")[1];
        //
        //
        //    allIpSet.add(from);
        //    allIpSet.add(to);
        //    if (routerSet.contains(from) && routerSet.contains(to)) {
        //        set1.add(from);
        //        set1.add(to);
        //    }
        //    if (routerSet.contains(from) && !routerSet.contains(to)) {
        //        list.add(line);
        //    }
        //    if (routerSet.contains(to) && !routerSet.contains(from)) {
        //        list.add(line);
        //    }
        //    //set1.add(line);
        //}
        ////FileWriter writer = new FileWriter("洛杉矶路由器去除倒数第二条路由器.txt");
        //
        //System.out.println(1111);
        //System.out.println(set1.size());
        //System.out.println(routerSet.size());
        //System.out.println(allIpSet.size());
        //
        //int j = 0;
        //for (String s : routerSet) {
        //    if (!set1.contains(s)) {
        //        //System.out.println(s);
        //    }else {
        //        //writer.write(s + "\r\n");
        //        j++;
        //    }
        //}
        ////writer.close();
        //System.out.println("234:" + list.size());
        
        //System.out.println(i);
        
        /*BufferedReader br = new BufferedReader(new FileReader("D:\\Desktop\\洛杉矶\\节点分析\\端口服务测量数据-修正后.txt"));
        String line;
        FileWriter writer = new FileWriter("端口服务测量数据-修正后.txt");
        Set<String> set = new HashSet<>();
        while((line = br.readLine()) != null) {
            JSONObject json = JSON.parseObject(line);
            
            if (set.add(json.getString("ip"))) {
                writer.write(line + "\r\n");
            }
        }
        
        writer.close();*/
    }
    public static Map<String, Integer> sortMap(@NotNull Map<String, Integer> map) {
        
        //利用Map的entrySet方法，转化为list进行排序
        List<Map.Entry<String, Integer>> entryList = new ArrayList<>(map.entrySet());
        //利用Collections的sort方法对list排序
        Collections.sort(entryList, new Comparator<Map.Entry<String, Integer>>() {
            @Override
            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                //正序排列，倒序反过来
                return o2.getValue() - o1.getValue();
            }
        });
        //遍历排序好的list，一定要放进LinkedHashMap，因为只有LinkedHashMap是根据插入顺序进行存储
        LinkedHashMap<String, Integer> linkedHashMap = new LinkedHashMap<String, Integer>();
        for (Map.Entry<String, Integer> e : entryList) {
            linkedHashMap.put(e.getKey(), e.getValue());
        }
        return linkedHashMap;
    }
    
    
    public static void mapPutFun(Map<String, Integer> map, String s) {
        map.put(s, map.get(s) == null ? 1 : map.get(s) + 1);
    }
    
    public static void addDnsAndEmail() throws Exception {
        File dnsFile = new File("D:\\Desktop\\关岛数据重新整理\\节点分析\\设备-DNS.txt");
        File emailFile = new File("D:\\Desktop\\关岛数据重新整理\\节点分析\\设备-EMAIL.txt");
        
        BufferedReader br = new BufferedReader(new FileReader(dnsFile));
        IPASInfoSearch ipasInfoSearch = new IPASInfoSearch(IPASInfoSearch.asDir);
        String line;
        FileWriter writer = new FileWriter("dns和email的json.txt");
        while ((line = br.readLine()) != null) {
            String[] ss = line.split("\t");
            
            String ip = ss[0];
            
            IpInfo ipInfo = new IpInfo();
            ipInfo.setIp(ip);
            ASInfo asInfo = ipasInfoSearch.searchIPASInfo(ip);
            
            IPLocation location = GetIPLocation.get(ip);
            
            List<String> tags = new ArrayList<>();
            tags.add("DNS服务器");
            
            ipInfo.setTags(tags);
            ipInfo.setAsInfo(asInfo);
            ipInfo.setLocation(location);
            
            writer.write(JSON.toJSONString(ipInfo) + "\n");
        }
        br = new BufferedReader(new FileReader(emailFile));
        
        while ((line = br.readLine()) != null) {
            String[] ss = line.split("\t");
            
            String ip = ss[0];
            
            IpInfo ipInfo = new IpInfo();
            ipInfo.setIp(ip);
            ASInfo asInfo = ipasInfoSearch.searchIPASInfo(ip);
            
            IPLocation location = GetIPLocation.get(ip);
            
            List<String> tags = new ArrayList<>();
            tags.add("EMAIL服务器");
            
            ipInfo.setTags(tags);
            ipInfo.setAsInfo(asInfo);
            ipInfo.setLocation(location);
            
            writer.write(JSON.toJSONString(ipInfo) + "\n");
        }
        
        writer.close();
        
    }
    
    public static String tagsToTagLine(List<String> tags) {
        StringBuilder line = new StringBuilder();
        
        if (tags.size() > 1) {
            for (String tag : tags) {
                line.append(tag).append(";");
            }
        } else {
            line.append(tags.get(0)).append(";");
        }
        
        return line.toString();
    }
    
    public static void mergeFile() throws Exception {
        BufferedReader br = new BufferedReader(new FileReader("D:\\Desktop\\闫-关岛数据-有IDC.txt"));
        
        String line;
        List<IpInfo> list = new ArrayList<>();
        
        //Map<String, Integer> map = new HashMap<>();
        while ((line = br.readLine()) != null) {
            IpPojo ipPojo = JSON.parseObject(line, IpPojo.class);
            
            IpInfo ipInfo = ipPojoToIpInfo(ipPojo);
            System.out.println(ipInfo);
            list.add(ipInfo);
            //Integer integer = map.get(ipInfo.getIp()) == null ? map.put(ipInfo.getIp(), 1) : map.put(ipInfo.getIp(), map.get(ipInfo.getIp()) + 1);
        }
        
        //for (String s : map.keySet()) {
        //    if (map.get(s) == 1) {
        //        continue;
        //    }
        //    System.out.println(s + "\t" + map.get(s) + "\n");
        //}
        //List<IpInfo> list1 = new ArrayList<>();
        ////Set<String> set = new HashSet<>();
        ////for (IpInfo ipInfo : list) {
        ////    if (map.get(ipInfo.getIp()) == 1) {
        ////        list1.add(ipInfo);
        ////    } else {
        ////        if (set.add(ipInfo.getIp())) {
        ////            ipInfo.setHostName("*");
        ////            list1.add(ipInfo);
        ////        }
        ////    }
        ////}
        //
        //System.out.println(list1.size());
        FileWriter writer = new FileWriter("D:\\Desktop\\关岛设备完全版.txt", true);
        //
        for (IpInfo ipInfo : list) {
            writer.write(JSON.toJSONString(ipInfo) + "\n");
        }
        
        writer.close();
    }
    
    public static IpInfo ipPojoToIpInfo(IpPojo ipPojo) {
        IpInfo ipInfo = new IpInfo();
        
        ipInfo.setIp(ipPojo.getIp());
        ipInfo.setCompany(ipPojo.getCompanyName());
        ipInfo.setHostName(ipPojo.getDomainName());
        List<String> tags = new ArrayList<>();
        tags.add("应用服务器");
        if (ipPojo.getTag() != null) {
            tags.add(ipPojo.getTag());
        }
        ipInfo.setTags(tags);
        ASInfo asInfo = ipasInfoSearch.searchIPASInfo(ipPojo.getIp());
        IPLocation location = GetIPLocation.get(ipPojo.getIp());
        ipInfo.setAsInfo(asInfo);
        ipInfo.setLocation(location);
        ipInfo.setPortInfos(ipPojo.getPorats());
        
        List<CVE> list = ipPojo.getCves();
        List<Vul> resList = new ArrayList<>();
        for (CVE cve : list) {
            Vul vul = new Vul();
            vul.setNum(cve.getNumbering());
            vul.setDescription(cve.getDescribe());
            vul.setLevel(cve.getLevel());
            resList.add(vul);
        }
        ipInfo.setVuls(resList);
        
        ipInfo.setOs(ipPojo.getOperatingSystem());
        
        
        return ipInfo;
    }
}
