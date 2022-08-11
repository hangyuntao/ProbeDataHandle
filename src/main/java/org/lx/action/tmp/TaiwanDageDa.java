package org.lx.action.tmp;

import cn.hutool.core.io.FileUtil;
import com.alibaba.fastjson.JSON;
import org.lx.pojo.NmapInfo;
import org.lx.pojo.PortInfo;
import org.lx.service.GetMailServerByAccount;
import org.lx.tools.FileLineTool;
import org.lx.tools.LoadIp;
import org.lx.tools.NormalTool;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TaiwanDageDa {
    static File baseFolder = new File("F:\\Analysis\\0614台湾大哥大\\IP用途分类");

    static Set<String> allIpSet = new HashSet<>();

    static Set<String> alreadyClassifySet = new HashSet<>();

    //解析网站dns，全部IP，地址，并提取在活跃IP里面的地址
    static void extractWebIp() throws IOException {
        Set<String> ipSet = new HashSet<>();
        FileLineTool.readLineWithTrim(new File(baseFolder, "元数据-台湾大哥大网站列表.txt"), new FileLineTool.LineHandle() {
            @Override
            public void handle(String line) throws IOException {
                line = line.trim();
                URL url = new URL(line);
                String domain = url.getHost();
                List<String> ips = GetMailServerByAccount.resolver(domain);
                for (String ip : ips) {
                    if (allIpSet.contains(ip) && !alreadyClassifySet.contains(ip)) {
                        ipSet.add(ip);
                    }
                }
            }
        });
        FileUtil.writeLines(ipSet, new File(baseFolder, "结果-自用WEBIP.txt"), StandardCharsets.UTF_8);
    }

    static void extractHttpPortForBanner(File nmapJsonlFile, File resultFile) throws IOException {
        FileWriter writer = new FileWriter(resultFile);
        FileLineTool.readLineWithTrim(nmapJsonlFile, new FileLineTool.LineHandle() {
            @Override
            public void handle(String line) throws IOException {
                NmapInfo nmapInfo = JSON.parseObject(line, NmapInfo.class);
                List<PortInfo> portInfos = nmapInfo.getPortInfos();
                Set<String> portSet = new HashSet<>();
                Set<String> httpsPortSet = new HashSet<>();
                for (PortInfo portInfo : portInfos) {
                    if (portInfo.getState().equals("open") && portInfo.getType().contains("http")) {
                        if (portInfo.getPort().equals("443") || portInfo.getPort().equals("8443")) {
                            httpsPortSet.add(portInfo.getPort());
                        } else {
                            portSet.add(portInfo.getPort());
                        }
                    }
                }
                if (portSet.size() > 0 || httpsPortSet.size() > 0) {
                    writer.write(nmapInfo.getIp() + "\t" + NormalTool.arrayToString(portSet, ",") + "\t" + NormalTool.arrayToString(httpsPortSet, ",") + "\n");
                }

            }
        });
        writer.close();
    }


    public static void main(String[] args) throws IOException {

        File ipasFile = new File(baseFolder, "全部活跃IP定位及AS.txt");

        allIpSet = LoadIp.load(ipasFile, 0);
//
//
        Set<String> allRouterIpSet = LoadIp.load(new File(baseFolder, "大哥大全部路由.txt"), 0);
//
//        alreadyClassifySet.addAll(allRouterIpSet);
//
//
        extractWebIp();
        File webIpFile = new File(baseFolder, "结果-自用WEBIP.txt");


        Set<String> webIpSet = LoadIp.load(webIpFile, 0);

//        alreadyClassifySet.addAll(webIpSet);


        File nmapJsonlFile = new File(baseFolder, "nmapRes.txt");
//

//        extractHttpPortForBanner(nmapJsonlFile, new File(baseFolder, "端口-http.txt"));


        File remoteFile = new File(baseFolder, "结果-远程服务开放地址.txt");
//        ClassifyByNmapJsonl.extractIpByOpenport(nmapJsonlFile, Arrays.asList("22", "3389"), remoteFile, true);
        Set<String> remoteIpSet = LoadIp.load(remoteFile, 0);


        FileWriter writer = new FileWriter(new File(baseFolder, "结果-自用-基础网络.txt"));
        FileWriter writer1 = new FileWriter(new File(baseFolder, "结果-自用.txt"));
        FileWriter writer2 = new FileWriter(new File(baseFolder, "结果-提供使用.txt"));

        List<String> printLines = new ArrayList<>();
        //路由器，以及网站所在c段为自用，其他开放22和3389所在c段为它用
        FileLineTool.readLineWithTrim(ipasFile, new FileLineTool.LineHandle() {
            @Override
            public void handle(String line) {
                String[] ss = line.split("\t");
                String ip = ss[0].trim();
                if (allRouterIpSet.contains(ip)) {
                    alreadyClassifySet.add(ip);
                    try {
                        writer.write(line + "\n");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        Set<String> httpIpC = new HashSet<>();
        for (String ip : webIpSet) {
            httpIpC.add(getIpC(ip));
        }
        FileLineTool.readLineWithTrim(ipasFile, new FileLineTool.LineHandle() {
            @Override
            public void handle(String line) {
                String[] ss = line.split("\t");
                String ip = ss[0].trim();
                if (alreadyClassifySet.contains(ip)) {
                    return;
                }

                String ipc = getIpC(ip);
                if (httpIpC.contains(ipc)) {
                    alreadyClassifySet.add(ip);
                    try {
                        writer1.write(line + "\n");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });


        Set<String> remoteIpC = new HashSet<>();
        for (String ip : remoteIpSet) {
            remoteIpC.add(getIpC(ip));
        }
        FileLineTool.readLineWithTrim(ipasFile, new FileLineTool.LineHandle() {
            @Override
            public void handle(String line) {
                String[] ss = line.split("\t");
                String ip = ss[0].trim();
                if (alreadyClassifySet.contains(ip)) {
                    return;
                }

                String ipc = getIpC(ip);
                if (remoteIpC.contains(ipc)) {
                    alreadyClassifySet.add(ip);
                    try {
                        writer2.write(line + "\n");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });


        writer.close();
        writer1.close();
        writer2.close();
    }


    static String getIpC(String ip) {
        return ip.substring(0, ip.lastIndexOf(".") + 1);
    }
}
