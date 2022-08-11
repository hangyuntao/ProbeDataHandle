package org.lx.LA;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.lx.pojo.ASInfo;
import org.lx.pojo.IpInfo;
import org.lx.pojo.Vul;
import org.lx.service.IPASInfoSearch;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;

import static org.lx.LA.FileHandle.isCompare;
import static org.lx.LA.FileHandle.sortMap;

/**
 * @author yuntao
 * @date 2022/8/4
 */
public class Topo {
    public static void main(String[] args) throws Exception {
        // D:\Desktop\洛杉矶\节点分析\path_all.txt
        /*getAsTopo("D:\\Desktop\\洛杉矶\\节点分析\\path_all.txt", "AT&T\n" +
                "Verizon\n" +
                "T-Mobile\n" +
                "CenturyLink\n" +
                "Level 3\n" +
                "Cogent\n" +
                "LayerHost\n" +
                "MULTACOM\n" +
                "Frontier Communications of America, Inc.\n" +
                "Psychz Networks\n" +
                "Sprint");*/
        
        //getIpTopo();
        //getDeviceTopo();
        
        //routerNameAnalysis1("别名解析结果3.txt");
        
        //routerNameAnalysis("别名解析结果3.txt");
        
        //getDeviceTopo("别名解析结果3.txt");
        
        getWebsiteTopo("D:\\Desktop\\网站拓扑.json");
        
        //BufferedReader br = new BufferedReader(new FileReader("D:\\Desktop\\洛杉矶\\节点分析\\洛杉矶-全部路由器.txt"));
        //String line;
        //
        //while((line = br.readLine()) != null) {
        //
        //}
    }
    
    public static void getAsTopo(String dir, String keywords) throws Exception {
        
        keywords = keywords.toLowerCase();
        List<String> keys = Arrays.asList(keywords.split("\n"));
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
            
            for (String key : keys) {
                if (!fromAs.getOrg_name().toLowerCase().contains(key) && !toAs.getOrg_name().toLowerCase().contains(key)) {
                    continue;
                }
                
                if ("*".equals(fromAs.getAut_name()) || "*".equals(toAs.getAut_name())) {
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
                if (fromAs.getOrg_name().toLowerCase().contains(key)) {
                    asSet.add(fromAs.getAut());
                } else {
                    asSet.add(toAs.getAut());
                }
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
        
        FileWriter writer = new FileWriter("asTopo.txt");
        map1 = sortMap(map1);
        JSONObject res = new JSONObject();
        JSONArray nodes = new JSONArray();
        JSONArray edges = new JSONArray();
        System.out.println(map1);
        Set<String> nodeSet = new HashSet<>();
        for (String s : map1.keySet()) {
            String[] ss = s.split("\t");
            
            String from = ss[0];
            String to = ss[1];
            if (asSet1.contains(from)) {
                writer.write(from + "\t" + map2.get(from) + "\t" + to + "\t" + map2.get(to) + "\t" + map1.get(s) + "\n");
            }
            if (nodeSet.add(ss[0])) {
                JSONObject node = new JSONObject();
                node.put("name", ss[0]);
                if (asSet1.contains(ss[0])) {
                    JSONObject itemStyle = new JSONObject();
                    itemStyle.put("color", "red");
                    node.put("itemStyle", itemStyle);
                    JSONArray size = new JSONArray();
                    size.add(20);
                    size.add(12);
                    node.put("symbolSize", size);
                    JSONObject label = new JSONObject();
                    label.put("show", true);
                    node.put("label", label);
                    node.put("symbol", "image://https://s2.loli.net/2022/08/05/dL95lOaXNPAT1xz.png");
                }
                
                nodes.add(node);
            }
            if (nodeSet.add(ss[1])) {
                JSONObject node = new JSONObject();
                node.put("name", ss[1]);
                if (asSet1.contains(ss[1])) {
                    JSONObject itemStyle = new JSONObject();
                    itemStyle.put("color", "red");
                    node.put("itemStyle", itemStyle);
                    JSONArray size = new JSONArray();
                    size.add(20);
                    size.add(12);
                    node.put("symbolSize", size);
                    JSONObject label = new JSONObject();
                    label.put("show", true);
                    node.put("label", label);
                    node.put("symbol", "image://https://s2.loli.net/2022/08/05/dL95lOaXNPAT1xz.png");
                }
                
                nodes.add(node);
            }
            JSONObject edge = new JSONObject();
            edge.put("source", ss[0]);
            edge.put("target", ss[1]);
            edges.add(edge);
        }
        writer.close();
        res.put("nodes", nodes);
        res.put("edges", edges);
        
        FileWriter writer1 = new FileWriter("json.txt");
        writer1.write(res.toJSONString());
        writer1.close();
    }
    
    public static void getIpTopo() throws Exception {
        
        BufferedReader br = new BufferedReader(new FileReader("D:\\Desktop\\洛杉矶\\节点分析\\洛杉矶-全部路由器.txt"));
        String line;
        
        Set<String> nodeSet = new HashSet<>();
        JSONObject res = new JSONObject();
        JSONArray nodes = new JSONArray();
        JSONArray edges = new JSONArray();
        
        Set<String> routerSet = new HashSet<>();
        while ((line = br.readLine()) != null) {
            routerSet.add(line);
        }
        
        System.out.println(routerSet.size());
        
        Map<String, Integer> map = new HashMap<>();
        
        br = new BufferedReader(new FileReader("D:\\Desktop\\洛杉矶\\节点分析\\path_all.txt"));
        int i = 0;
        while ((line = br.readLine()) != null) {
            String from = line.split("\t")[0];
            String to = line.split("\t")[1];
            //IPLocation fromLoc = GetIPLocation.get(from);
            //IPLocation toLoc = GetIPLocation.get(to);
            //
            //if (!fromLoc.getCity().contains("洛杉矶") || !toLoc.getCity().contains("洛杉矶")) {
            //    continue;
            //}
            
            if (routerSet.contains(from) && routerSet.contains(to)) {
                i++;
                nodeSet.add(from);
                nodeSet.add(to);
                JSONObject edge = new JSONObject();
                edge.put("source", from);
                edge.put("target", to);
                edges.add(edge);
                map.put(from, map.get(from) == null ? 1 : map.get(from) + 1);
                map.put(to, map.get(to) == null ? 1 : map.get(to) + 1);
            }
            
        }
        
        for (String s : nodeSet) {
            JSONObject node = new JSONObject();
            node.put("id", s);
            nodes.add(node);
        }
        System.out.println("edges的size：" + edges.size());
        System.out.println(i);
        res.put("nodes", nodes);
        res.put("edges", edges);
        
        System.out.println(nodes.size());
        
        FileWriter writer1 = new FileWriter("ip级拓扑json.txt");
        writer1.write(res.toJSONString());
        writer1.close();
        
        map = sortMap(map);
        
        for (String s : map.keySet()) {
            System.out.println(s + "\t" + map.get(s));
        }
    }
    
    public static void getDeviceTopo() throws Exception {
        BufferedReader br = new BufferedReader(new FileReader("洛杉矶设备分类json-有IDC-修正后.txt"));
        String line;
        Set<String> deviceSet = new HashSet<>();
        while ((line = br.readLine()) != null) {
            JSONObject json = JSON.parseObject(line);
            
            deviceSet.add(json.getString("ip"));
        }
        Set<String> nodeSet = new HashSet<>();
        JSONObject res = new JSONObject();
        JSONArray nodes = new JSONArray();
        JSONArray edges = new JSONArray();
        br = new BufferedReader(new FileReader("D:\\Desktop\\洛杉矶\\节点分析\\path_all.txt"));
        
        Map<String, Integer> map = new HashMap<>();
        while ((line = br.readLine()) != null) {
            String from = line.split("\t")[0];
            String to = line.split("\t")[1];
            //IPLocation fromLoc = GetIPLocation.get(from);
            //IPLocation toLoc = GetIPLocation.get(to);
            //
            //if (!fromLoc.getCity().contains("洛杉矶") || !toLoc.getCity().contains("洛杉矶")) {
            //    continue;
            //}
            
            if (deviceSet.contains(from) && deviceSet.contains(to)) {
                
                nodeSet.add(from);
                nodeSet.add(to);
                JSONObject edge = new JSONObject();
                edge.put("source", from);
                edge.put("target", to);
                edges.add(edge);
                map.put(from, map.get(from) == null ? 1 : map.get(from) + 1);
                map.put(to, map.get(to) == null ? 1 : map.get(to) + 1);
            }
            
        }
        
        for (String s : nodeSet) {
            JSONObject node = new JSONObject();
            node.put("id", s);
            nodes.add(node);
        }
        
        res.put("nodes", nodes);
        res.put("edges", edges);
        
        FileWriter writer1 = new FileWriter("设备级拓扑json.txt");
        writer1.write(res.toJSONString());
        writer1.close();
        
        map = sortMap(map);
        
        for (String s : map.keySet()) {
            System.out.println(s + "\t" + map.get(s));
        }
    }
    
    // 列去重
    public static void routerNameAnalysis(String dir) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(dir));
        String line;
        List<Set<String>> setList = new ArrayList<>(270);
        for (int i = 0; i < 270; i++) {
            Set<String> set = new HashSet<>();
            setList.add(set);
        }
        
        List<String> list = new ArrayList<>();
        int maxLen = 0;
        int row = 0;
        while ((line = br.readLine()) != null) {
            String[] ss = line.split("\t");
            int length = ss.length;
            maxLen = Math.max(maxLen, length);
            
            StringBuilder sb = null;
            
            for (int i = 0; i < ss.length; i++) {
                String s = ss[i];
                Set<String> set = setList.get(i);
                if (!set.add(s)) {
                    System.out.println(i);
                    System.out.println(row);
                    System.out.println(s);
                } else {
                    if (sb == null) {
                        sb = new StringBuilder();
                        sb.append(s);
                    } else {
                        sb.append("\t").append(s);
                    }
                }
                setList.set(i, set);
            }
            list.add(sb.toString());
            row++;
        }
        
        System.out.println(list.size());
        System.out.println(maxLen);
        
        FileWriter writer = new FileWriter("别名解析结果3.txt");
        for (String s : list) {
            writer.write(s + "\r\n");
        }
        
        writer.close();
        
        
    }
    
    
    // 行去重
    public static void routerNameAnalysis1(String dir) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader("别名解析结果3.txt"));
        String line;
        List<String> list = new ArrayList<>();
        while ((line = br.readLine()) != null) {
            String[] ss = line.split("\t");
            
            Set<String> set = new HashSet<>(Arrays.asList(ss));
            
            StringBuilder sb = null;
            
            for (String s : set) {
                if (sb == null) {
                    sb = new StringBuilder();
                    sb.append(s);
                } else {
                    sb.append("\t").append(s);
                }
            }
            
            list.add(sb.toString());
        }
        
        
        FileWriter writer = new FileWriter("别名解析结果4.txt");
        for (String s : list) {
            writer.write(s + "\r\n");
        }
        writer.close();
        
        System.out.println(list.size());
    }
    
    public static void test() throws Exception {
        BufferedReader br = new BufferedReader(new FileReader("别名解析结果3.txt"));
        String line;
        Set<String> set = new HashSet<>();
        while ((line = br.readLine()) != null) {
            String[] ss = line.split("\t");
            
            for (String s : ss) {
                if (!set.add(s)) {
                    System.out.println(s);
                }
            }
        }
    }
    
    
    public static void getWebsiteTopo(String dir) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(dir));
        String line;
        StringBuilder sb = new StringBuilder();
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        File f = new File("D:\\Desktop\\网站扫描结果json");
        
        File[] files = f.listFiles();
        
        Set<String> keySet;
        keySet = new HashSet<>();
        keySet.add("griffithobservatory.org");
        keySet.add("www.mrsewerrooter.la");
        keySet.add("cao.lacity.org");
        keySet.add("bca.lacity.org");
        keySet.add("myla311.lacity.org");
        keySet.add("ewddlacity.com");
        keySet.add("business.lacity.org");
        keySet.add("www.lafd.org");
        keySet.add("niseiweek.org");
        keySet.add("wordpress.org");
        keySet.add("joesairportparking.com");
        keySet.add("www.opglaviic.com");
        int num = 0;
        int high = 0;
        int cri = 0;
        int med = 0;
        
        Set<String> cveSet = new HashSet<>();
        for (File file : files) {
            br = new BufferedReader(new FileReader(file));
            
            while ((line = br.readLine()) != null) {
                IpInfo ipInfo = JSON.parseObject(line, IpInfo.class);
                if (!keySet.contains(ipInfo.getHostName())) {
                    continue;
                }
                num += ipInfo.getVuls() == null ? 0 : ipInfo.getVuls().size();
                if (ipInfo.getVuls() != null) {
                    List<Vul> vuls = ipInfo.getVuls();
                    for (Vul vul : vuls) {
                        cveSet.add(vul.getNum());
                    }
                }
            }
        }
        
        br = new BufferedReader(new FileReader("D:\\Desktop\\cve字典表\\洛杉矶cve字典表1.txt"));
    
        while((line = br.readLine()) != null) {
            
            Vul vul = JSON.parseObject(line, Vul.class);
            if (!cveSet.contains(vul.getNum())) {
                continue;
            }
            if ("CRITICAL".equals(vul.getLevel())) {
                cri++;
            }else if ("MEDIUM".equals(vul.getLevel())){
                med++;
            }else {
                high++;
            }
        }
        System.out.println(num);
        System.out.println(cri);
        System.out.println(med);
        System.out.println(high);
        
        JSONArray json = JSON.parseArray(sb.toString());
        
        JSONObject res = new JSONObject();
        JSONArray nodes = new JSONArray();
        JSONArray edges = new JSONArray();
        Set<String> set = new HashSet<>();
        Set<String> noSet = new HashSet<>();
        noSet.add("cleanharbors.com");
        noSet.add("bluetriton.com");
        noSet.add("mooresolar.com");
        noSet.add("aroundclock.com");
        noSet.add("www.authorize.net");
        noSet.add("asolarp.com");
        noSet.add("www.arrowheadwater.com");
        noSet.add("www.shopify.com");
        noSet.add("tvc2050.com");
        noSet.add("www.hgtv.com");
        noSet.add("www.cadizwaterproject.com");
        noSet.add("www.cadizinc.com");
        noSet.add("www.centurylink.com");
        
        noSet.add("www.expertise.com");
        noSet.add("www.alexelectrician.com");
        noSet.add("www.theelectricconnection.com");
        noSet.add("www.fiveemeraldlimo.com");
        noSet.add("www.ciwebgroup.com");
        noSet.add("www.911restorationwestla.com");
        noSet.add("drductless.com");
        noSet.add("www.mysynchrony.com");
        
        
        for (
                Object o : json) {
            JSONObject j = (JSONObject) o;
            
            JSONObject edge = new JSONObject();
            if (noSet.contains(j.getString("src")) || noSet.contains(j.getString("dst"))) {
                continue;
            }
            edge.put("source", j.getString("src"));
            edge.put("target", j.getString("dst"));
            set.add(j.getString("src"));
            set.add(j.getString("dst"));
            edges.add(edge);
        }
        
        
        for (
                String s : set) {
            
            JSONObject node = new JSONObject();
            node.put("name", s);
            if (keySet.contains(s)) {
                JSONObject itemStyle = new JSONObject();
                itemStyle.put("color", "red");
                JSONObject label = new JSONObject();
                label.put("show", true);
                label.put("fontSize", 20);
                node.put("label", label);
                node.put("itemStyle", itemStyle);
            }
            nodes.add(node);
        }
        res.put("nodes", nodes);
        res.put("edges", edges);
        
        System.out.println(res);
        
        FileWriter writer = new FileWriter("网站连接关系.txt");
        
        writer.write(res.toJSONString());
        writer.close();
        
        
    }
    
    public static void getDeviceTopo(String dir) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(dir));
        String line;
        int row = 0;
        Map<Integer, Set<String>> map = new HashMap<>();
        while ((line = br.readLine()) != null) {
            Set<String> set = new HashSet<>(Arrays.asList(line.split("\t")));
            
            map.put(row, set);
            
            row++;
        }
        
        br = new BufferedReader(new FileReader("拓扑.txt"));
        
        JSONObject res = new JSONObject();
        JSONArray ns = new JSONArray();
        JSONArray es = new JSONArray();
        Set<String> nodeSet = new HashSet<>();
        row = 0;
        Set<String> pathSet = new HashSet<>();
        JSONArray tes = new JSONArray();
        while ((line = br.readLine()) != null) {
            
            JSONObject json = JSON.parseObject(line);
            tes = json.getJSONArray("nodes");
            JSONArray edges = json.getJSONArray("edges");
            
            System.out.println(edges.size());
            System.out.println(json.getJSONArray("nodes").size());
            for (Object o : edges) {
                
                JSONObject edge = (JSONObject) o;
                String source = edge.getString("source");
                String target = edge.getString("target");
                
                for (int num : map.keySet()) {
                    
                    Set<String> set = map.get(num);
                    String sourceName;
                    String targetName;
                    if (set.contains(source)) {
                        sourceName = "router" + num;
                    } else {
                        sourceName = source;
                    }
                    if (set.contains(target)) {
                        targetName = "router" + num;
                    } else {
                        targetName = target;
                    }
                    if (!sourceName.equals(targetName)) {
                        JSONObject e = new JSONObject();
                        e.put("source", sourceName);
                        e.put("target", targetName);
                        if (pathSet.add(e.toJSONString())) {
                            es.add(e);
                            nodeSet.add(sourceName);
                            nodeSet.add(targetName);
                        }
                    }
                }
            }
            
        }
        
        Set<String> sssset = new HashSet<>();
        for (Object te : tes) {
            JSONObject node = (JSONObject) te;
            String id = node.getString("id");
            sssset.add(id);
        }
        
        for (String s : nodeSet) {
            if (!sssset.contains(s)) {
                System.out.println(s);
            }
            JSONObject node = new JSONObject();
            node.put("id", s);
            ns.add(node);
        }
        
        
        res.put("nodes", ns);
        res.put("edges", es);
        
        System.out.println();
        System.out.println(ns.size());
        System.out.println(es.size());
        
        
        System.out.println(nodeSet.size());
        
        FileWriter writer = new FileWriter("设备级拓扑json.txt");
        writer.write(res.toJSONString());
        writer.close();
    }
    
}
