package org.lx.classify;

import org.lx.pojo.ASInfo;
import org.lx.service.IPASInfoSearch;
import org.lx.tools.FileLineTool;
import org.lx.tools.LoadIp;
import org.lx.tools.ip.GetIPLocation;
import org.lx.tools.ip.IPLocation;

import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;

import static org.lx.classify.ClassifyByNmapJsonl.*;

/**
 * @author yuntao
 * @date 2022/8/1
 */
public class ClassifyByJson {
    public static void main(String[] args) throws Exception {
        
        File baseFolder = new File("D:\\Desktop\\洛杉矶\\节点分析");
        File nmapJsonlFile = new File(baseFolder, "端口服务测量数据-修正后.txt");

        extractIpByOpenport(nmapJsonlFile, Arrays.asList("25", "465", "578", "994", "110", "995", "143", "993"), new File(baseFolder, "端口-email.txt"), true);
        extractIpByOpenport(nmapJsonlFile, Arrays.asList("53"), new File(baseFolder, "端口-DNS.txt"), true);
        extractIpByOpenport(nmapJsonlFile, Arrays.asList("80", "443"), new File(baseFolder, "端口-http.txt"), true);
    
        //检查数据库端口服务，分类 数据库服务器
        extractDatabaseWithCheck(nmapJsonlFile, new File(baseFolder, "数据库服务检查结果.txt"));

        //Email分类
        File equipEmailFile = new File(baseFolder, "设备-EMAIL.txt");
        alreadyClassifyIp.addAll(LoadIp.load(equipEmailFile, 0));
    
        //DNS分类
        File dnsEquipFile = new File(baseFolder, "设备-DNS.txt");
        //equipDNS(new File(baseFolder, "dns扫描结果"), dnsEquipFile);
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
    
        {
            System.out.println(1);
            IPASInfoSearch ipasInfoSearch = new IPASInfoSearch("D:\\dataCollect\\ASInfo.txt");
            FileWriter writer = new FileWriter(new File(baseFolder, "设备分类-无路由器.txt"));
            
            FileLineTool.readLineWithTrim(printerEquip, line -> {
                String[] ss = line.split("\t");
                String ip = ss[0].trim();
                IPLocation location = GetIPLocation.get(ip);
                ASInfo asInfo = ipasInfoSearch.searchIPASInfo(ip);
                writer.write(ip + "\t" + "打印机" + "\t\t" + ((asInfo.getIsp().equals("*") ? asInfo.getOrg_name() : asInfo.getIsp())) + "\t" + location.getCountry() + "\t" + location.getProvince() + "\t" + location.getCity() + "\n");
            });
            FileLineTool.readLineWithTrim(firewallEquip, line -> {
                String ip = line.split("\t")[0].trim();
                IPLocation location = GetIPLocation.get(ip);
                ASInfo asInfo = ipasInfoSearch.searchIPASInfo(ip);
                writer.write(ip + "\t" + "防火墙" + "\t\t" + ((asInfo.getIsp().equals("*") ? asInfo.getOrg_name() : asInfo.getIsp())) + "\t" + location.getCountry() + "\t" + location.getProvince() + "\t" + location.getCity() + "\n");
            });
            FileLineTool.readLineWithTrim(equipCameraFile, line -> {
                String[] ss = line.split("\t");
                String ip = ss[0].trim();
                IPLocation location = GetIPLocation.get(ip);
                ASInfo asInfo = ipasInfoSearch.searchIPASInfo(ip);
                writer.write(ip + "\t" + "视频监控" + "\t" + (ss.length > 1 ? ss[1] : "") + "\t" + ((asInfo.getIsp().equals("*") ? asInfo.getOrg_name() : asInfo.getIsp())) + "\t" + location.getCountry() + "\t" + location.getProvince() + "\t" + location.getCity() + "\n");
            });
            FileLineTool.readLineWithTrim(equipDatabaseFile, line -> {
                String[] ss = line.split("\t");
                String ip = ss[0].trim();
                IPLocation location = GetIPLocation.get(ip);
                ASInfo asInfo = ipasInfoSearch.searchIPASInfo(ip);
                writer.write(ip + "\t" + "数据库服务器" + "\t" + ss[1] + "\t" + ((asInfo.getIsp().equals("*") ? asInfo.getOrg_name() : asInfo.getIsp())) + "\t" + location.getCountry() + "\t" + location.getProvince() + "\t" + location.getCity() + "\n");
            });
            FileLineTool.readLineWithTrim(equipHttpFile, line -> {
                String[] ss = line.split("\t");
                String ip = ss[0].trim();
                IPLocation location = GetIPLocation.get(ip);
                ASInfo asInfo = ipasInfoSearch.searchIPASInfo(ip);
                writer.write(ip + "\t" + "应用服务器" + "\t" + ss[1] + "\t" + ((asInfo.getIsp().equals("*") ? asInfo.getOrg_name() : asInfo.getIsp())) + "\t" + location.getCountry() + "\t" + location.getProvince() + "\t" + location.getCity() + "\n");
            });
            FileLineTool.readLineWithTrim(equipEmailFile, line -> {
                String[] ss = line.split("\t");
                String ip = ss[0].trim();
                IPLocation location = GetIPLocation.get(ip);
                ASInfo asInfo = ipasInfoSearch.searchIPASInfo(ip);
                writer.write(ip + "\t" + "email服务器" + "\t" + ss[1] + "\t" + ((asInfo.getIsp().equals("*") ? asInfo.getOrg_name() : asInfo.getIsp())) + "\t" + location.getCountry() + "\t" + location.getProvince() + "\t" + location.getCity() + "\n");
            });
            FileLineTool.readLineWithTrim(dnsEquipFile, line -> {
                String[] ss = line.split("\t");
                String ip = ss[0].trim();
                IPLocation location = GetIPLocation.get(ip);
                ASInfo asInfo = ipasInfoSearch.searchIPASInfo(ip);
                writer.write(ip + "\t" + "dns服务器" + "\t" + ss[1] + "\t" + ((asInfo.getIsp().equals("*") ? asInfo.getOrg_name() : asInfo.getIsp())) + "\t" + location.getCountry() + "\t" + location.getProvince() + "\t" + location.getCity() + "\n");
            });
            writer.close();
        }
    }
}
