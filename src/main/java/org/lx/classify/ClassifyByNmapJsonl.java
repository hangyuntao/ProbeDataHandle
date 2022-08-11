package org.lx.classify;

import cn.hutool.core.io.FileUtil;
import com.alibaba.fastjson.JSON;
import org.lx.pojo.ASInfo;
import org.lx.pojo.NmapInfo;
import org.lx.pojo.PortInfo;
import org.lx.service.IPASInfoSearch;
import org.lx.servicecheck.MySQLServiceScan;
import org.lx.servicecheck.OracleServiceScan;
import org.lx.servicecheck.SqlServerServiceScan;
import org.lx.tools.FileLineTool;
import org.lx.tools.LoadIp;
import org.lx.tools.NormalTool;
import org.lx.tools.ip.GetIPLocation;
import org.lx.tools.ip.IPLocation;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class ClassifyByNmapJsonl {

    public static String formatOpenPorts(NmapInfo nmapInfo) {
        List<String> list = new ArrayList<>();
        for (PortInfo portInfo : nmapInfo.getPortInfos()) {
            if (portInfo.getState().equals("open")) {
                if (portInfo.getType() == null || portInfo.getType().equals("*")) {
                    list.add(portInfo.getPort() + "/" + portInfo.getType());
                } else {
                    list.add(portInfo.getPort() + "/" + portInfo.getProtocal());
                }
            }
        }
        return NormalTool.arrayToString(list, ";");
    }


    public static void extractIpByOpenport(File nmapJsonlFile, List<String> ports, File resultFile, boolean withPort) throws IOException {
        FileWriter writer = new FileWriter(resultFile);
        FileLineTool.readLineWithTrim(nmapJsonlFile, new FileLineTool.LineHandle() {
            @Override
            public void handle(String line) throws IOException {
                NmapInfo nmapInfo = JSON.parseObject(line, NmapInfo.class);
                List<PortInfo> portInfos = nmapInfo.getPortInfos();
                for (PortInfo portInfo : portInfos) {
                    if (ports.contains(portInfo.getPort()) && portInfo.getState().equals("open")) {
                        if (withPort) {
                            writer.write(nmapInfo.getIp() + "\t" + portInfo.getPort() + "\n");
                        } else {
                            writer.write(nmapInfo.getIp() + "\n");
                        }

                    }
                }
            }
        });
        writer.close();

    }

    public static void extractIpByOpenportProtocol(File nmapJsonlFile, String protocol, File resultFile, boolean withPort) throws IOException {
        FileWriter writer = new FileWriter(resultFile);
        FileLineTool.readLineWithTrim(nmapJsonlFile, new FileLineTool.LineHandle() {
            @Override
            public void handle(String line) throws IOException {
                NmapInfo nmapInfo = JSON.parseObject(line, NmapInfo.class);
                List<PortInfo> portInfos = nmapInfo.getPortInfos();
                for (PortInfo portInfo : portInfos) {
                    if (portInfo.getState().equals("open") && portInfo.getType().contains(protocol)) {
                        if (withPort) {
                            writer.write(nmapInfo.getIp() + "\t" + portInfo.getPort() + "\n");
                        } else {
                            writer.write(nmapInfo.getIp() + "\n");
                        }

                    }
                }
            }
        });
        writer.close();
    }

    public static void extractByDeviceType(File nmapJsonlFile, String device_type, File resultFile) throws IOException {
        FileWriter writer = new FileWriter(resultFile);
        FileLineTool.readLineWithTrim(nmapJsonlFile, new FileLineTool.LineHandle() {
            @Override
            public void handle(String line) throws IOException {
                NmapInfo nmapInfo = JSON.parseObject(line, NmapInfo.class);
                List<PortInfo> portInfos = nmapInfo.getPortInfos();
                if (nmapInfo.getDevice_type() != null && nmapInfo.getDevice_type().equals(device_type)) {
                    String portFormat = "";
                    for (PortInfo portInfo : portInfos) {
                        if (portInfo.getState().equals("open")) {
                            portFormat = portFormat + portInfo.getPort() + ":" + portInfo.getType() + ";";
                        }
                    }
                    writer.write(nmapInfo.getIp() + "\t" + nmapInfo.getDevice_type() + "\t" + portFormat + "\t" + (nmapInfo.getOs_detail() == null ? nmapInfo.getOs_guess() : nmapInfo.getOs_detail()) + "\n");
                }
            }
        });
        writer.close();
    }

    public static void extractDatabaseWithCheck(File nmapJsonlFile, File resultFile) throws IOException {

        Set<String> checkedSet = new HashSet<>();
        if (resultFile.exists()) {
            FileLineTool.readLineWithTrim(resultFile, new FileLineTool.LineHandle() {
                @Override
                public void handle(String line) throws IOException {
                    String[] ss = line.split("\t");
                    String ip = ss[0].trim();
                    String port = ss[1].trim();
                    checkedSet.add(ip + ":" + port);
                }
            });
        }

        FileWriter writer = new FileWriter(resultFile, true);
        MySQLServiceScan mySQLServiceScan = new MySQLServiceScan();
        SqlServerServiceScan sqlServerServiceScan = new SqlServerServiceScan();
        OracleServiceScan oracleServiceScan = new OracleServiceScan();
        AtomicLong atomicLong = new AtomicLong();
        FileLineTool.readLineWithTrimThreads(nmapJsonlFile, new FileLineTool.LineHandle() {
            @Override
            public void handle(String line) throws IOException {

                NmapInfo nmapInfo = JSON.parseObject(line, NmapInfo.class);
                for (PortInfo portInfo : nmapInfo.getPortInfos()) {
                    if (!portInfo.getState().equals("open")) {
                        continue;
                    }
                    boolean checked = false;
                    boolean isDatabasePort = false;
                    String ipport = nmapInfo.getIp() + ":" + portInfo.getPort();
                    if (checkedSet.contains(ipport)) {
                        continue;
                    }
                    if (portInfo.getType().equals("mysql")) {
                        isDatabasePort = true;
                        checked = mySQLServiceScan.isActive(nmapInfo.getIp(), Integer.parseInt(portInfo.getPort()));
                        System.out.println(atomicLong.incrementAndGet());
                    } else if (portInfo.getType().equals("oracle")) {
                        isDatabasePort = true;
                        checked = oracleServiceScan.isActive(nmapInfo.getIp(), Integer.parseInt(portInfo.getPort()));
                        System.out.println(atomicLong.incrementAndGet());
                    } else if (portInfo.getType().equals("ms-sql-s")) {
                        isDatabasePort = true;
                        checked = sqlServerServiceScan.isActive(nmapInfo.getIp(), Integer.parseInt(portInfo.getPort()));
                        System.out.println(atomicLong.incrementAndGet());
                    }
                    if (isDatabasePort) {
                        writer.write(nmapInfo.getIp() + "\t" + portInfo.getPort() + "\t" + portInfo.getType() + "\t" + (checked ? "passed" : "unpassed") + "\n");
                        writer.flush();
                    }
                }
            }
        }, 500);
        writer.close();
    }


    public static final Set<String> alreadyClassifyIp = new HashSet<>();

//    public static void miguanEquip(File nmapJsonlFile, File miguanEquipFile) throws IOException {
//        FileWriter writer = new FileWriter(miguanEquipFile);
//        FileLineTool.readLineWithTrim(nmapJsonlFile, new FileLineTool.LineHandle() {
//            @Override
//            public void handle(String line) throws IOException {
//                NmapInfo nmapInfo = JSON.parseObject(line, NmapInfo.class);
//                List<PortInfo> portInfos = nmapInfo.getPortInfos();
//                int openPortCount = 0;
//                for (PortInfo portInfo : portInfos) {
//                    if (portInfo.getState().equals("open")) {
//                        openPortCount++;
//                    }
//                }
//                if(openPortCount>100){
//                    writer.write(nmapInfo.getIp()+"开放端口数量："+openPortCount+"\n");
//                }
//            }
//        });
//        writer.close();
//    }

    public static void equipDNS(File dnsScanFolder, File dnsEquipFile) throws IOException {
        List<File> files = FileUtil.loopFiles(dnsScanFolder.toPath(), pathname -> {
            String name = pathname.getName();
            System.out.println(name);
            return name.startsWith("DNSDetect") && name.endsWith("txt");
        });
        Set<String> dnsIp = new HashSet<>();
        for (File file : files) {
            FileLineTool.readLineWithTrim(file, line -> {
                String[] ss = line.split("\t");
                String ip = ss[0].trim();
                if (!alreadyClassifyIp.contains(ip)) {
                    dnsIp.add(ip + "\t" + "53/dns");
                }
            });
        }
        FileUtil.writeLines(dnsIp, dnsEquipFile, StandardCharsets.UTF_8);
    }

    public static void equipCamera(File nmapJsonlFile, File resultFile) throws IOException {
        // 摄像头	webcam  //视频服务器 rtsp协议：554   rtmp协议：1935
        FileWriter writer = new FileWriter(resultFile);
        FileLineTool.readLineWithTrim(nmapJsonlFile, new FileLineTool.LineHandle() {
            @Override
            public void handle(String line) throws IOException {
                NmapInfo nmapInfo = JSON.parseObject(line, NmapInfo.class);
                if (alreadyClassifyIp.contains(nmapInfo.getIp())) {
                    return;
                }
                if (nmapInfo.getDevice_type() != null && nmapInfo.getDevice_type().equals("webcam")) {
                    writer.write(nmapInfo.getIp() /*+ "\t" + (nmapInfo.getOs_detail() == null ? nmapInfo.getOs_guess() : nmapInfo.getOs_detail()) */ + "\n");
                    return;
                }
                for (PortInfo portInfo : nmapInfo.getPortInfos()) {
                    if (portInfo.getType().equals("rtsp") || portInfo.getType().equals("rtmp")) {
                        writer.write(nmapInfo.getIp() + "\t" + (portInfo.getPort() + "/" + portInfo.getType()) /*+ "\t" + (nmapInfo.getOs_detail() == null ? nmapInfo.getOs_guess() : nmapInfo.getOs_detail())*/ + "\n");
                        return;
                    }
                }
            }
        });
        writer.close();
    }

    public static void equipPrinter(File nmapJsonlFile, File resultFile) throws IOException {
        //  打印机	printer
        FileWriter writer = new FileWriter(resultFile);
        FileLineTool.readLineWithTrim(nmapJsonlFile, new FileLineTool.LineHandle() {
            @Override
            public void handle(String line) throws IOException {


                NmapInfo nmapInfo = JSON.parseObject(line, NmapInfo.class);
                if (alreadyClassifyIp.contains(nmapInfo.getIp())) {
                    return;
                }
                if (nmapInfo.getDevice_type() != null && nmapInfo.getDevice_type().equals("printer")) {
                    writer.write(nmapInfo.getIp() + "\t" + (nmapInfo.getOs_detail() == null ? nmapInfo.getOs_guess() : nmapInfo.getOs_detail()) + "\n");
                }

            }
        });
        writer.close();
    }

    public static void equipFirewall(File nmapJsonlFile, File resultFile) throws IOException {
        //防火墙	firewall
        FileWriter writer = new FileWriter(resultFile);
        FileLineTool.readLineWithTrim(nmapJsonlFile, new FileLineTool.LineHandle() {
            @Override
            public void handle(String line) throws IOException {


                NmapInfo nmapInfo = JSON.parseObject(line, NmapInfo.class);
                if (alreadyClassifyIp.contains(nmapInfo.getIp())) {
                    return;
                }
                if (nmapInfo.getDevice_type() != null && nmapInfo.getDevice_type().equals("firewall")) {
                    writer.write(nmapInfo.getIp() + "\t" + (nmapInfo.getOs_detail() == null ? nmapInfo.getOs_guess() : nmapInfo.getOs_detail()) + "\n");
                }

            }
        });
        writer.close();
    }

    public static void equipHttp(File httpScanFolder, File resultFile) throws IOException {
        
        List<File> files = FileUtil.loopFiles(httpScanFolder.toPath(), new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                String name = pathname.getName();
                return name.endsWith(".txt");
            }
        });
        Map<String, Set<String>> ipPortMap = new HashMap<>();
        for (File file : files) {
            String ip = file.getName().replace(".txt", "");
            if (alreadyClassifyIp.contains(ip)) {
                continue;
            }
            Set<String> set = ipPortMap.computeIfAbsent(ip, k -> new HashSet<>());
            FileLineTool.readLineWithTrim(file, new FileLineTool.LineHandle() {
                @Override
                public void handle(String line) throws IOException {
                    String[] ss = line.split("\t");
                    String port = ss[0].trim();
                    set.add(port + "/http");
                    
                }
            });
        }
        FileWriter writer = new FileWriter(resultFile);
        for (String ip : ipPortMap.keySet()) {
            System.out.println(ip);
            writer.write(ip + "\t" + NormalTool.arrayToString(ipPortMap.get(ip), ";") + "\r\n");
        }
        writer.close();
    }
    
    public static void equipHttp1(File httpScanFolder, File resultFile) throws IOException {
        
        List<File> files = FileUtil.loopFiles(httpScanFolder.toPath(), new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                String name = pathname.getName();
                return name.endsWith(".txt");
            }
        });
        Map<String, Set<String>> ipPortMap = new HashMap<>();
        for (File file : files) {
            //String ip = file.getName().replace(".txt", "");
            BufferedReader br = new BufferedReader(new FileReader(file));
            
            String line;
            
            while ((line = br.readLine()) != null) {
                String ip = line.split("\t")[0];
                if (alreadyClassifyIp.contains(ip)) {
                    continue;
                }
                Set<String> set = ipPortMap.computeIfAbsent(ip, k -> new HashSet<>());
                String port = line.split("\t")[1].trim();
                set.add(port + "/http");
            }
            
        }
        FileWriter writer = new FileWriter(resultFile);
        for (String ip : ipPortMap.keySet()) {
            System.out.println(ip);
            writer.write(ip + "\t" + NormalTool.arrayToString(ipPortMap.get(ip), ";") + "\r\n");
        }
        writer.close();
    }

    public static void equipDatabase(File databaseCheckedFile, File resultFile) throws IOException {
        //数据库服务器 mysql:3306 oracle:1521 sqlserver:1433:ms-sql-s

        Map<String, Set<String>> ipPortMap = new HashMap<>();
        FileLineTool.readLineWithTrim(databaseCheckedFile, line -> {

            String[] ss = line.split("\t");
            String ip = ss[0].trim();
            String port = ss[1].trim();
            String type = ss[2].trim();
            String pass = ss[3].trim();
            if (pass.equals("passed")) {
                if (alreadyClassifyIp.contains(ip)) {
                    return;
                }
                Set<String> set = ipPortMap.computeIfAbsent(ip, k -> new HashSet<>());
                set.add(port + "/" + type);
            }


        });
        FileWriter writer = new FileWriter(resultFile);
        for (String ip : ipPortMap.keySet()) {
            writer.write(ip + "\t" + NormalTool.arrayToString(ipPortMap.get(ip), ";") + "\n");
        }
        writer.close();
    }


    public static void main(String[] args) throws Exception {

//        Set<String> deviceTypeSet = new HashSet<>();
//        FileWriter writer = new FileWriter(new File("F:\\Analysis\\2022-江苏设备\\终端节点分析\\ip_device_type.txt"));
//        FileLineTool.readLineWithTrim(new File("F:\\Analysis\\2022-江苏设备\\终端节点分析\\nmapRes.txt"), new FileLineTool.LineHandle() {
//            @Override
//            public void handle(String line) throws IOException {
//                NmapInfo nmapInfo = JSON.parseObject(line, NmapInfo.class);
//                List<PortInfo> portInfos = nmapInfo.getPortInfos();
//                if (nmapInfo.getDevice_type() != null) {
////                    deviceTypeSet.add(nmapInfo.getDevice_type());
//                    writer.write(nmapInfo.getIp() + "\t" + nmapInfo.getDevice_type() + "\t" + (nmapInfo.getOs_detail() == null ? nmapInfo.getOs_guess() : nmapInfo.getOs_detail()) + "\n");
//                }
//            }
//        });
//        FileUtil.writeLines(deviceTypeSet, new File("F:\\Analysis\\2022-江苏设备\\终端节点分析\\deviceTypes.txt"), StandardCharsets.UTF_8);
//        writer.close();
        File baseFolder = new File("D:\\Desktop\\关岛数据重新整理\\节点分析");
        File nmapJsonlFile = new File(baseFolder, "端口服务测量数据.txt");
        extractIpByOpenport(nmapJsonlFile, Arrays.asList("25", "465", "578", "994", "110", "995", "143", "993"), new File(baseFolder, "端口-email.txt"), true);
        extractIpByOpenport(nmapJsonlFile, Arrays.asList("53"), new File(baseFolder, "端口-DNS.txt"), true);
//        extractIpByOpenportProtocol(nmapJsonlFile, "http", new File(baseFolder, "端口-http.txt"), true);
        
        //extractIpByOpenport(nmapJsonlFile, Arrays.asList("80", "443"), new File(baseFolder, "端口-http.txt"), true);
        
        //检查数据库端口服务，分类 数据库服务器
        extractDatabaseWithCheck(nmapJsonlFile, new File(baseFolder, "数据库服务检查结果.txt"));

        //蜜罐识别
//        File miguanEquipFile = new File(baseFolder,"设备-疑似蜜罐.txt");
//        miguanEquip(nmapJsonlFile, miguanEquipFile);
//        alreadyClassifyIp.addAll(LoadIp.load(miguanEquipFile, 0));

//        if (true) {
//            return;
//        }

        //Email分类
        File equipEmailFile = new File(baseFolder, "设备-EMAIL.txt");
        alreadyClassifyIp.addAll(LoadIp.load(equipEmailFile, 0));

        //DNS分类
        File dnsEquipFile = new File(baseFolder, "设备-DNS.txt");
        equipDNS(new File(baseFolder, "dns扫描结果"), dnsEquipFile);
        alreadyClassifyIp.addAll(LoadIp.load(dnsEquipFile, 0));
        
        //  打印机	printer
        File printerEquip = new File(baseFolder, "设备-打印机.txt");
        equipPrinter(nmapJsonlFile, printerEquip);
        alreadyClassifyIp.addAll(LoadIp.load(printerEquip, 0));

        //防火墙	firewall
        File firewallEquip = new File(baseFolder, "设备-防火墙.txt");
        equipFirewall(nmapJsonlFile, firewallEquip);
        alreadyClassifyIp.addAll(LoadIp.load(firewallEquip, 0));


        // 摄像头	webcam  //视频服务器 rtsp协议：554   rtmp协议：1935
//        extractByDeviceType(nmapJsonlFile, "webcam", new File(baseFolder, "设备-网络摄像头.txt"));
        File equipCameraFile = new File(baseFolder, "设备-视频监控.txt");
        equipCamera(nmapJsonlFile, equipCameraFile);
        alreadyClassifyIp.addAll(LoadIp.load(equipCameraFile, 0));


        //数据库服务器 mysql:3306 oracle:1521 sqlserver:1433:ms-sql-s
        File equipDatabaseFile = new File(baseFolder, "设备-数据库服务器.txt");
        equipDatabase(new File(baseFolder, "数据库服务检查结果.txt"), equipDatabaseFile);
        alreadyClassifyIp.addAll(LoadIp.load(equipDatabaseFile, 0));
 
        //应用服务器
        File equipHttpFile = new File(baseFolder, "设备-应用服务器.txt");
        equipHttp1(new File(baseFolder, "端口-http.txt"), equipHttpFile);
        alreadyClassifyIp.addAll(LoadIp.load(equipHttpFile, 0));

//        if (true) {
//            return;
//        }


        {
            System.out.println(1);
            IPASInfoSearch ipasInfoSearch = new IPASInfoSearch("D:\\dataCollect\\ASInfo.txt");
            FileWriter writer = new FileWriter(new File(baseFolder, "设备分类.txt"));
            //FileLineTool.readLineWithTrim(equipEmailFile, new FileLineTool.LineHandle() {
            //    @Override
            //    public void handle(String line) throws IOException {
            //        String[] ss = line.split("\t");
            //        String ip = ss[0].trim();
            //        IPLocation location = GetIPLocation.get(ip);
            //        ASInfo asInfo = ipasInfoSearch.searchIPASInfo(ip);
            //        writer.write(ip + "\t" + "EMAIL" + "\t" + ss[1] + "\t" + ((asInfo.getIsp().equals("*") ? asInfo.getOrg_name() : asInfo.getIsp())) + "\t" + location.getCountry() + "\t" + location.getProvince() + "\t" + location.getCity() + "\n");
            //    }
            //});
            //FileLineTool.readLineWithTrim(dnsEquipFile, new FileLineTool.LineHandle() {
            //    @Override
            //    public void handle(String line) throws IOException {
            //        String[] ss = line.split("\t");
            //        String ip = ss[0].trim();
            //        IPLocation location = GetIPLocation.get(ip);
            //        ASInfo asInfo = ipasInfoSearch.searchIPASInfo(ip);
            //        writer.write(ip + "\t" + "DNS" + "\t" + ss[1] + "\t" + ((asInfo.getIsp().equals("*") ? asInfo.getOrg_name() : asInfo.getIsp())) + "\t" + location.getCountry() + "\t" + location.getProvince() + "\t" + location.getCity() + "\n");
            //    }
            //});
            FileLineTool.readLineWithTrim(printerEquip, new FileLineTool.LineHandle() {
                @Override
                public void handle(String line) throws IOException {
                    String[] ss = line.split("\t");
                    String ip = ss[0].trim();
                    IPLocation location = GetIPLocation.get(ip);
                    ASInfo asInfo = ipasInfoSearch.searchIPASInfo(ip);
                    writer.write(ip + "\t" + "打印机" + "\t\t" + ((asInfo.getIsp().equals("*") ? asInfo.getOrg_name() : asInfo.getIsp())) + "\t" + location.getCountry() + "\t" + location.getProvince() + "\t" + location.getCity() + "\n");
                }
            });
            FileLineTool.readLineWithTrim(firewallEquip, new FileLineTool.LineHandle() {
                @Override
                public void handle(String line) throws IOException {
                    String ip = line.split("\t")[0].trim();
                    IPLocation location = GetIPLocation.get(ip);
                    ASInfo asInfo = ipasInfoSearch.searchIPASInfo(ip);
                    writer.write(ip + "\t" + "防火墙" + "\t\t" + ((asInfo.getIsp().equals("*") ? asInfo.getOrg_name() : asInfo.getIsp())) + "\t" + location.getCountry() + "\t" + location.getProvince() + "\t" + location.getCity() + "\n");
                }
            });
            FileLineTool.readLineWithTrim(equipCameraFile, new FileLineTool.LineHandle() {
                @Override
                public void handle(String line) throws IOException {
                    String[] ss = line.split("\t");
                    String ip = ss[0].trim();
                    IPLocation location = GetIPLocation.get(ip);
                    ASInfo asInfo = ipasInfoSearch.searchIPASInfo(ip);
                    writer.write(ip + "\t" + "视频监控" + "\t" + (ss.length > 1 ? ss[1] : "") + "\t" + ((asInfo.getIsp().equals("*") ? asInfo.getOrg_name() : asInfo.getIsp())) + "\t" + location.getCountry() + "\t" + location.getProvince() + "\t" + location.getCity() + "\n");
                }
            });
            FileLineTool.readLineWithTrim(equipDatabaseFile, new FileLineTool.LineHandle() {
                @Override
                public void handle(String line) throws IOException {
                    String[] ss = line.split("\t");
                    String ip = ss[0].trim();
                    IPLocation location = GetIPLocation.get(ip);
                    ASInfo asInfo = ipasInfoSearch.searchIPASInfo(ip);
                    writer.write(ip + "\t" + "数据库服务器" + "\t" + ss[1] + "\t" + ((asInfo.getIsp().equals("*") ? asInfo.getOrg_name() : asInfo.getIsp())) + "\t" + location.getCountry() + "\t" + location.getProvince() + "\t" + location.getCity() + "\n");
                }
            });
            FileLineTool.readLineWithTrim(equipHttpFile, new FileLineTool.LineHandle() {
                @Override
                public void handle(String line) throws IOException {
                    String[] ss = line.split("\t");
                    String ip = ss[0].trim();
                    IPLocation location = GetIPLocation.get(ip);
                    ASInfo asInfo = ipasInfoSearch.searchIPASInfo(ip);
                    writer.write(ip + "\t" + "应用服务器" + "\t" + ss[1] + "\t" + ((asInfo.getIsp().equals("*") ? asInfo.getOrg_name() : asInfo.getIsp())) + "\t" + location.getCountry() + "\t" + location.getProvince() + "\t" + location.getCity() + "\n");
                }
            });
            writer.close();
        }


    }
}
