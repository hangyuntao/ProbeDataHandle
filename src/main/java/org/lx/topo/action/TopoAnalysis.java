package org.lx.topo.action;

import cn.hutool.core.io.FileUtil;
import org.lx.tools.FileLineTool;
import org.lx.tools.ip.GetIPLocation;
import org.lx.tools.ip.IPLocation;
import org.lx.topo.AnalysisTracert;
import org.lx.topo.CaiDaASReaderMap;
import org.lx.topo.IPFilter;
import org.lx.topo.LoadRouter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class TopoAnalysis {


    public static void extractTracert(File tracertResFolder, File tmpFolder, File allIPOutputFile, File allRouterOutputFile, File allPathOutputFile) throws IOException {
        AnalysisTracert analysisTracert = new AnalysisTracert(tracertResFolder, tmpFolder, allIPOutputFile,
                allRouterOutputFile, allPathOutputFile, new IPFilter() {
            @Override
            public boolean filter(String ip) {
                return true;
            }
        }, 6);
        analysisTracert.extract();
    }

    //通过拓扑计算边界路由器，
    public static void extractAsRouter(File pathFile, File asRouterFile) throws IOException {
        AtomicInteger count = new AtomicInteger(0);
        Set<String> asRouterSet = new HashSet<>();
//		BufferedWriter checkWriter = new BufferedWriter(new FileWriter(new File(baseFolder, "as边界路由但是非全部路由内数据.txt")));
        FileLineTool.readLineWithTrim(pathFile, new FileLineTool.LineHandle() {
            @Override
            public void handle(String line) {
                String[] ss = line.split("\t");
                String source = ss[0];
                String tagret = ss[1];
                String as1 = CaiDaASReaderMap.searchASFormate(source);
                String as2 = CaiDaASReaderMap.searchASFormate(tagret);
                if (as1 == null || as1.isEmpty() || as2 == null || as2.isEmpty()) {
                    return;
                }
                if (!as1.equals(as2)) {
                    asRouterSet.add(source);
                    asRouterSet.add(tagret);
                }
            }
        });
        FileUtil.writeLines(asRouterSet, asRouterFile, StandardCharsets.UTF_8);
    }

    //合并全部路由，提取端节点IP
    public static void mergeRouterAndExtractEquip(File baseRouterFile, File asRouterFile, File allIpFile, File allRouterFile, File endIpFile) throws IOException {
        final Set<String> set = LoadRouter.loadRouter(baseRouterFile);
        Set<String> set2 = LoadRouter.loadRouter(asRouterFile);
        set.addAll(set2);
        FileUtil.writeLines(set, allRouterFile, StandardCharsets.UTF_8);

        final FileWriter writer = new FileWriter(endIpFile);
        FileLineTool.readLineWithTrim(allIpFile, new FileLineTool.LineHandle() {
            @Override
            public void handle(String line) throws IOException {
                if (!set.contains(line)) {
                    writer.write(line + "\n");
                }
            }
        });
        writer.close();
    }


    public static void extractHexinRouter(File allRouterFile, File asRouterFile, File allPathFile, File hexinRouterFile) throws IOException {
        Set<String> allRouterSet = LoadRouter.loadRouter(allRouterFile);
        Set<String> asRouterSet = LoadRouter.loadRouter(asRouterFile);
        Set<String> hexinRouterSet = new HashSet<>();
        FileLineTool.readLineWithTrim(allPathFile, new FileLineTool.LineHandle() {
            @Override
            public void handle(String line) throws IOException {
                String[] ss = line.split("\t");
                String source = ss[0];
                String tagret = ss[1];
                if (allRouterSet.contains(source) && allRouterSet.contains(tagret)) {
                    IPLocation location1 = GetIPLocation.get(source);
                    IPLocation location2 = GetIPLocation.get(tagret);

                    String province1 = location1.getCountry() + "," + location1.getProvince();
                    if (province1.contains("*") || province1.endsWith(",")) {
                        return;
                    }
                    String province2 = location2.getCountry() + "," + location2.getProvince();
                    if (province2.contains("*") || province2.endsWith(",")) {
                        return;
                    }

                    if (!province1.equals(province2)) {
                        if (!asRouterSet.contains(source)) {
                            hexinRouterSet.add(source);
                        }
                        if (!asRouterSet.contains(tagret)) {
                            hexinRouterSet.add(tagret);
                        }
                    }
                }
            }
        });
        FileUtil.writeLines(hexinRouterSet, hexinRouterFile, StandardCharsets.UTF_8);

    }

    public static void extractHuijuRouter(File allRouterFile, File asRouterFile, File hexinRouterFile, File allPathFile, File huijuRouterFile) throws IOException {
        Set<String> allRouterSet = LoadRouter.loadRouter(allRouterFile);
        Set<String> asRouterSet = LoadRouter.loadRouter(asRouterFile);
        Set<String> hexinRouterSet = LoadRouter.loadRouter(hexinRouterFile);
        Set<String> huijuRouterSet = new HashSet<>();
        FileLineTool.readLineWithTrim(allPathFile, line -> {
            String[] ss = line.split("\t");
            String source = ss[0];
            String tagret = ss[1];
            if (allRouterSet.contains(source) && allRouterSet.contains(tagret)) {
                IPLocation location1 = GetIPLocation.get(source);
                IPLocation location2 = GetIPLocation.get(tagret);

                String province1 = location1.getCountry() + "," + location1.getProvince();
                if (province1.contains("*") || province1.endsWith(",")) {
                    return;
                }
                String province2 = location2.getCountry() + "," + location2.getProvince();
                if (province2.contains("*") || province2.endsWith(",")) {
                    return;
                }
                if (!province1.equals(province2)) {//同省
                    return;
                }
                String city1 = location1.getCountry() + "," + location1.getProvince() + "," + location1.getCity();
                if (city1.contains("*") || city1.endsWith(",")) {
                    return;
                }
                String city2 = location2.getCountry() + "," + location2.getProvince() + "," + location2.getCity();
                if (city2.contains("*") || city2.endsWith(",")) {
                    return;
                }

                if (!city1.equals(city2)) {//同省不同城市
                    if (!asRouterSet.contains(source) && !hexinRouterSet.contains(source)) {
                        huijuRouterSet.add(source);
                    }
                    if (!asRouterSet.contains(tagret) && !hexinRouterSet.contains(tagret)) {
                        huijuRouterSet.add(tagret);
                    }
                }
            }
        });
        FileUtil.writeLines(huijuRouterSet, huijuRouterFile, StandardCharsets.UTF_8);
    }

    public static void extractJieruRouter(File allRouterFile, File asRouterFile, File hexinRouterFile, File huijuRouterFile, File endIpFile, File allPathFile, File jieruRouterFile) throws IOException {
        Set<String> allRouterSet = LoadRouter.loadRouter(allRouterFile);
        Set<String> asRouterSet = LoadRouter.loadRouter(asRouterFile);
        Set<String> hexinRouterSet = LoadRouter.loadRouter(hexinRouterFile);
        Set<String> huijuRouterSet = LoadRouter.loadRouter(huijuRouterFile);
        Set<String> endIpSet = LoadRouter.loadRouter(endIpFile);
        Set<String> jieruRouterSet = new HashSet<>();
        FileLineTool.readLineWithTrim(allPathFile, new FileLineTool.LineHandle() {
            @Override
            public void handle(String line) throws IOException {
                String[] ss = line.split("\t");
                String source = ss[0];
                String tagret = ss[1];
                if (endIpSet.contains(source)) {
                    if (allRouterSet.contains(tagret)) {
                        if (asRouterSet.contains(tagret) || hexinRouterSet.contains(tagret) || huijuRouterSet.contains(tagret)) {
                            return;
                        }
                        jieruRouterSet.add(tagret);
                    }
                } else if (endIpSet.contains(tagret)) {
                    if (allRouterSet.contains(source)) {
                        if (asRouterSet.contains(source) || hexinRouterSet.contains(source) || huijuRouterSet.contains(source)) {
                            return;
                        }
                        jieruRouterSet.add(source);
                    }
                }
            }
        });
        FileUtil.writeLines(jieruRouterSet, jieruRouterFile, StandardCharsets.UTF_8);
    }

    public static void filterSingleIpFile(File ipFile, File saveFile, IPFilter ipFilter) throws IOException {
        final FileWriter writer = new FileWriter(saveFile);
        FileLineTool.readLineWithTrim(ipFile, new FileLineTool.LineHandle() {
            @Override
            public void handle(String line) throws IOException {
                if (ipFilter.filter(line)) {
                    writer.write(line + "\n");
                }
            }
        });
        writer.close();
    }


    public static void chinaJiangsu() throws IOException {
        File baseFolder = new File("F:\\Analysis\\2022-江苏设备");
        File allIpFile = new File(baseFolder, "ip_all.txt");
        File baseRouterFile = new File(baseFolder, "router_base.txt");
        File allPathFile = new File(baseFolder, "path_all.txt");

//        extractTracert(new File(baseFolder, "tracert"), new File(baseFolder, "tracertTmp"), allIpFile, baseRouterFile, allPathFile);
//        System.out.println("导出文件结束");


        //AS 路由
        File asRouterFile = new File(baseFolder, "边界路由器-empty.txt");
//        extractAsRouter(allPathFile, asRouterFile);
//
//        System.out.println("导出as路由结束");


        //合并全部路由，提取端节点 IP
        File allRouterFile = new File(baseFolder, "router_all.txt");
        File endIpFile = new File(baseFolder, "ip_end.txt");
        mergeRouterAndExtractEquip(baseRouterFile, asRouterFile, allIpFile, allRouterFile, endIpFile);


        //通过路径对路由器进行分类，边界路由器、核心路由器、汇聚路由器、接入路由器
        File hexinRouterFile = new File(baseFolder, "核心路由器.txt");
        extractHexinRouter(allRouterFile, asRouterFile, allPathFile, hexinRouterFile);
        File huijuRouterFile = new File(baseFolder, "汇聚路由器.txt");
//        extractHuijuRouter(allRouterFile, asRouterFile, hexinRouterFile, allPathFile, huijuRouterFile);
        File jieruRouterFile = new File(baseFolder, "接入路由器.txt");
//        extractJieruRouter(allRouterFile, asRouterFile, hexinRouterFile, huijuRouterFile, endIpFile, allPathFile, jieruRouterFile);

        //过滤出江苏IP
        IPFilter ipFilter = new IPFilter() {
            @Override
            public boolean filter(String ip) {
                IPLocation location = GetIPLocation.get(ip);
                if ("江苏省".equals(location.getProvince())) {
                    return true;
                }
                return false;
            }
        };
        
        //对端节点设备进行分类
        filterSingleIpFile(hexinRouterFile, new File(baseFolder, "江苏-核心路由器.txt"), ipFilter);
        filterSingleIpFile(huijuRouterFile, new File(baseFolder, "江苏-汇聚路由器.txt"), ipFilter);
        filterSingleIpFile(jieruRouterFile, new File(baseFolder, "江苏-接入路由器.txt"), ipFilter);
        filterSingleIpFile(endIpFile, new File(baseFolder, "江苏-终端IP.txt"), ipFilter);
    }

    public static void taiwanDageDa() throws IOException {

    }
    
    public static void guamData() throws IOException {
        File baseFolder = new File("D:\\Desktop\\关岛数据重新整理");
        File allIpFile = new File(baseFolder, "ip_all.txt");
        File baseRouterFile = new File(baseFolder, "router_base.txt");
        File allPathFile = new File(baseFolder, "path_all.txt");

        extractTracert(new File(baseFolder, "Tracert测量"), new File(baseFolder, "tracertTmp"), allIpFile, baseRouterFile, allPathFile);
//        System.out.println("导出文件结束");
        
        
        //AS 路由
        File asRouterFile = new File(baseFolder, "边界路由器-empty.txt");
        extractAsRouter(allPathFile, asRouterFile);
//
//        System.out.println("导出as路由结束");
        
        
        //合并全部路由，提取端节点 IP
        File allRouterFile = new File(baseFolder, "router_all.txt");
        File endIpFile = new File(baseFolder, "ip_end.txt");
        mergeRouterAndExtractEquip(baseRouterFile, asRouterFile, allIpFile, allRouterFile, endIpFile);
        
        
        //通过路径对路由器进行分类，边界路由器、核心路由器、汇聚路由器、接入路由器
        File hexinRouterFile = new File(baseFolder, "核心路由器.txt");
        extractHexinRouter(allRouterFile, asRouterFile, allPathFile, hexinRouterFile);
        File huijuRouterFile = new File(baseFolder, "汇聚路由器.txt");
        extractHuijuRouter(allRouterFile, asRouterFile, hexinRouterFile, allPathFile, huijuRouterFile);
        File jieruRouterFile = new File(baseFolder, "接入路由器.txt");
        extractJieruRouter(allRouterFile, asRouterFile, hexinRouterFile, huijuRouterFile, endIpFile, allPathFile, jieruRouterFile);
        
        //过滤出关岛IP
        IPFilter ipFilter = new IPFilter() {
            @Override
            public boolean filter(String ip) {
                IPLocation location = GetIPLocation.get(ip);
                return "关岛".equals(location.getCountry());
            }
        };
        
        //对端节点设备进行分类
        filterSingleIpFile(hexinRouterFile, new File(baseFolder, "关岛-核心路由器.txt"), ipFilter);
        filterSingleIpFile(huijuRouterFile, new File(baseFolder, "关岛-汇聚路由器.txt"), ipFilter);
        filterSingleIpFile(jieruRouterFile, new File(baseFolder, "关岛-接入路由器.txt"), ipFilter);
        filterSingleIpFile(endIpFile, new File(baseFolder, "关岛-终端IP.txt"), ipFilter);
    }
    
    public static void laData() throws IOException {
        File baseFolder = new File("D:\\Desktop\\洛杉矶\\节点分析1");
        File allIpFile = new File(baseFolder, "ip_all.txt");
        File baseRouterFile = new File(baseFolder, "router_base.txt");
        File allPathFile = new File(baseFolder, "path_all.txt");
        
        extractTracert(new File(baseFolder, "Tracert测量"), new File(baseFolder, "tracertTmp"), allIpFile, baseRouterFile, allPathFile);
//        System.out.println("导出文件结束");
        
        
        //AS 路由
        File asRouterFile = new File(baseFolder, "边界路由器-empty.txt");
        extractAsRouter(allPathFile, asRouterFile);
//
//        System.out.println("导出as路由结束");
        
        
        //合并全部路由，提取端节点 IP
        File allRouterFile = new File(baseFolder, "router_all.txt");
        File endIpFile = new File(baseFolder, "ip_end.txt");
        mergeRouterAndExtractEquip(baseRouterFile, asRouterFile, allIpFile, allRouterFile, endIpFile);
        
        
        //通过路径对路由器进行分类，边界路由器、核心路由器、汇聚路由器、接入路由器
        File hexinRouterFile = new File(baseFolder, "核心路由器.txt");
        extractHexinRouter(allRouterFile, asRouterFile, allPathFile, hexinRouterFile);
        File huijuRouterFile = new File(baseFolder, "汇聚路由器.txt");
        extractHuijuRouter(allRouterFile, asRouterFile, hexinRouterFile, allPathFile, huijuRouterFile);
        File jieruRouterFile = new File(baseFolder, "接入路由器.txt");
        extractJieruRouter(allRouterFile, asRouterFile, hexinRouterFile, huijuRouterFile, endIpFile, allPathFile, jieruRouterFile);
        
        //过滤出洛杉矶IP
        IPFilter ipFilter = ip -> {
            IPLocation location = GetIPLocation.get(ip);
            return location.getCity().contains("洛杉矶");
        };
        
        //对端节点设备进行分类
        filterSingleIpFile(hexinRouterFile, new File(baseFolder, "洛杉矶-核心路由器.txt"), ipFilter);
        filterSingleIpFile(huijuRouterFile, new File(baseFolder, "洛杉矶-汇聚路由器.txt"), ipFilter);
        filterSingleIpFile(jieruRouterFile, new File(baseFolder, "洛杉矶-接入路由器.txt"), ipFilter);
        filterSingleIpFile(endIpFile, new File(baseFolder, "洛杉矶-终端IP.txt"), ipFilter);
    }
    
    public static void main(String[] args) throws IOException {
        //guamData();
        laData();
       
       //IPLocation location = GetIPLocation.get("47.181.201.154");
       //
       // System.out.println(location);
        /*BufferedReader br = new BufferedReader(new FileReader("D:\\Desktop\\关岛数据重新整理\\关岛-终端IP.txt"));
        
        String line;
        
        while ((line = br.readLine()) != null) {
            IPLocation location = GetIPLocation.get(line);
            System.out.println(location.getCountry());
        }*/
    }

}
