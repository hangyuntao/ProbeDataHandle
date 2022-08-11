package org.lx.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.lx.pojo.NmapInfo;
import org.lx.pojo.PortInfo;
import org.lx.tools.ip.IPDao;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NmapHandle {

    static String ipReg = "((?:(?:25[0-5]|2[0-4]\\d|((1\\d{2})|([1-9]?\\d)))\\.){3}(?:25[0-5]|2[0-4]\\d|((1\\d{2})|([1-9]?\\d))))";
    static String regex = "^\\d+/.*$";
    static Pattern p = Pattern.compile(regex);
    static Pattern ipp = Pattern.compile(ipReg);

    //获取Nmap扫描文件的所有Nmap相关信息，所有结果都会存入传进去的参数List<NmapInfo> nmapInfos
    public static void getAllNmapInfo(String dir, List<NmapInfo> nmapInfos) throws Exception {
        File file = new File(dir);
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                getAllNmapInfo(f.getPath(), nmapInfos);
            }
        } else {
            if (file.getName().contains("IPMeasureRe")) {
                InputStreamReader isr = new InputStreamReader(new FileInputStream(file), "utf-8");
                BufferedReader rd = new BufferedReader(isr);
                String line;
                boolean checkPort = false;
                PortInfo portService = null;
                List<String> ls = new ArrayList<>();
                NmapInfo nmapInfo = new NmapInfo();
                List<PortInfo> lp = new ArrayList<>();
                while ((line = rd.readLine()) != null) {
                    line = line.replaceAll("﻿", "");
                    if (line.toLowerCase().contains("nmap scan report")) {
                        if (nmapInfo.getIp() != null && !nmapInfo.getIp().equals("")) {
                            if (portService != null && portService.getPort() != null) {
                                if (ls != null && ls.size() > 0) {
                                    portService.setVul(ls);
                                }
                                lp.add(portService);
                            }
                            nmapInfo.setPortInfos(lp);
                            nmapInfos.add(nmapInfo);
                        }
                        String ip = "";
                        if (line.contains("(") && line.contains(")")) {
                            String a = line.substring(line.indexOf("(") + 1, line.indexOf(")")).trim();
                            if (ipp.matcher(a).matches()) {
                                ip = a;
                            }
                        }
                        if (ip.equals("")) {
                            String[] arr = line.split("\\s|\\(|\\)");
                            for (String a : arr) {
                                Matcher m = ipp.matcher(a);
                                if (m.matches()) {
                                    ip = a;
                                    break;
                                }
                            }
                        }
                        if (ip.equals("")) {
                            System.out.println(line);
                        }
                        nmapInfo = new NmapInfo();
                        nmapInfo.setIp(ip);
                        lp = new ArrayList<>();
                        ls = new ArrayList<>();
                        portService = new PortInfo();
                    }

                    if (line.contains("PORT") && line.contains("STATE") && line.contains("SERVICE")) {
                        checkPort = true;
                        continue;
                    }
                    if (line.contains("OS guesses:")) {
                        String os = line.substring(line.indexOf(":") + 1, line.length()).trim().replaceAll("\r\n", "");
                        nmapInfo.setOs_guess(os);
                    }
                    if (line.contains("OS details:")) {
                        String os = line.substring(line.indexOf(":") + 1, line.length()).trim().replaceAll("\r\n", "");
                        nmapInfo.setOs_detail(os);
                    }
                    if (line.contains("Device type") && line.contains(":")) {
                        String dt = line.substring(line.indexOf(":") + 1, line.length()).trim().replaceAll("\r\n", "");
                        nmapInfo.setDevice_type(dt);
                    }
                    if (line.contains("Running") && line.contains(":")) {
                        String dt = line.substring(line.indexOf(":") + 1, line.length()).trim().replaceAll("\r\n", "");
                        nmapInfo.setRunning(dt);
                    }

                    if (checkPort) {
                        Matcher m = p.matcher(line);
                        if (m.matches()) {
                            if (portService != null && portService.getPort() != null) {
                                if (ls != null && ls.size() > 0) {
                                    portService.setVul(ls);
                                }
                                lp.add(portService);
                                portService = new PortInfo();
                                ls = new ArrayList<>();
                            }
                            String port = null;
                            String state = null;
                            String service = null;
                            String[] arr = line.trim().split("\\s");
                            int c = 0;
                            for (String a : arr) {
                                if (!a.trim().equals("")) {
                                    c++;
                                    if (c == 1) {
                                        port = a.trim();
                                    } else if (c == 2) {
                                        state = a.trim();
                                    } else if (c == 3) {
                                        service = a.trim();
                                    }
                                }
                            }
                            if (port != null) {
                                portService = new PortInfo();
                                String[] parr = port.split("/");
                                portService.setPort(parr[0]);
                                portService.setProtocal(parr[1]);
                                portService.setState(state);
                                if (service != null) {
                                    service = service.replaceAll("\\?", "");
                                }
                                portService.setType(service);
                                portService.setBanner(null);
                                String version = line.replaceFirst(port, "").replaceFirst(state, "").replaceFirst(service, "").trim();
                                version = version.replaceAll("\\?", "");
                                if (version != null && !version.equals("")) {
                                    portService.setVersion(version);
                                } else {
                                    portService.setVersion("*");
                                }
                            }
                        }
                    }
                    if (line.contains("CVE-") && ls.size() <= 50) {
                        String[] arr = line.split("\\[|\\]|\\s");
                        for (String a : arr) {
                            if (a.contains("CVE-") && !a.contains(",")) {
                                a = a.replaceAll("\\.", "").trim();
                                if (!ls.contains(a)) {
                                    ls.add(a);
                                    break;
                                }
                            }
                        }
                    }
                }
                isr.close();
                rd.close();
                if (nmapInfo.getIp() != null && !nmapInfo.getIp().equals("")) {
                    if (portService != null && portService.getPort() != null) {
                        if (ls != null && ls.size() > 0) {
                            portService.setVul(ls);
                        }
                        lp.add(portService);
                    }
                    nmapInfo.setPortInfos(lp);
                    nmapInfos.add(nmapInfo);
                }
            }
        }
    }

    //获取Nmap扫描文件的所有Nmap相关信息
    public static void getAllNmapInfo(String dir, String outdir, int cveNumber) throws Exception {
        File file = new File(dir);
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                getAllNmapInfo(f.getPath(), outdir, cveNumber);
            }
        } else {
            if (file.getName().contains("IPMeasureRe") || file.getName().contains("nmapPortRe")) {
                Writer w = new FileWriter(outdir, true);
                InputStreamReader isr = new InputStreamReader(new FileInputStream(file), "utf-8");
                BufferedReader rd = new BufferedReader(isr);
                String line;
                boolean checkPort = false;
                PortInfo portService = null;
                List<String> ls = new ArrayList<>();
                NmapInfo nmapInfo = new NmapInfo();
                List<PortInfo> lp = new ArrayList<>();
                while ((line = rd.readLine()) != null) {
                    line = line.replaceAll("﻿", "");
                    if (line.toLowerCase().contains("nmap scan report")) {
                        if (nmapInfo.getIp() != null && !nmapInfo.getIp().equals("")) {
                            if (portService != null && portService.getPort() != null) {
                                if (ls != null && ls.size() > 0) {
                                    portService.setVul(ls);
                                }
                                lp.add(portService);
                            }
                            nmapInfo.setPortInfos(lp);
                            if (nmapInfo.getOs_guess() != null || nmapInfo.getOs_detail() != null || nmapInfo.getDevice_type() != null
                                    || (nmapInfo.getPortInfos() != null && nmapInfo.getPortInfos().size() > 0)) {
                                w.write(JSON.toJSONString(nmapInfo) + "\r\n");
                            }

                        }
                        String ip = "";
                        if (line.contains("(") && line.contains(")")) {
                            String a = line.substring(line.indexOf("(") + 1, line.indexOf(")")).trim();
                            if (ipp.matcher(a).matches()) {
                                ip = a;
                            }
                        }
                        if (ip.equals("")) {
                            String[] arr = line.split("\\s|\\(|\\)");
                            for (String a : arr) {
                                Matcher m = ipp.matcher(a);
                                if (m.matches()) {
                                    ip = a;
                                    break;
                                }
                            }
                        }
                        if (ip.equals("")) {
                            System.out.println(line);
                        }
                        nmapInfo = new NmapInfo();
                        nmapInfo.setIp(ip);
                        lp = new ArrayList<>();
                        ls = new ArrayList<>();
                        portService = new PortInfo();
                    }

                    if (line.contains("PORT") && line.contains("STATE") && line.contains("SERVICE")) {
                        checkPort = true;
                        continue;
                    }
                    if (line.contains("OS guesses:")) {
                        String os = line.substring(line.indexOf(":") + 1, line.length()).trim().replaceAll("\r\n", "");
                        nmapInfo.setOs_guess(os);
                    }
                    if (line.contains("OS details:")) {
                        String os = line.substring(line.indexOf(":") + 1, line.length()).trim().replaceAll("\r\n", "");
                        nmapInfo.setOs_detail(os);
                    }
                    if (line.contains("Device type") && line.contains(":")) {
                        String dt = line.substring(line.indexOf(":") + 1, line.length()).trim().replaceAll("\r\n", "");
                        nmapInfo.setDevice_type(dt);
                    }
                    if (line.contains("Running") && line.contains(":")) {
                        String dt = line.substring(line.indexOf(":") + 1, line.length()).trim().replaceAll("\r\n", "");
                        nmapInfo.setRunning(dt);
                    }

                    if (checkPort) {
                        Matcher m = p.matcher(line);
                        if (m.matches()) {
                            if (portService != null && portService.getPort() != null) {
                                if (ls != null && ls.size() > 0) {
                                    portService.setVul(ls);
                                }
                                lp.add(portService);
                                portService = new PortInfo();
                                ls = new ArrayList<>();
                            }
                            String port = null;
                            String state = null;
                            String service = null;
                            String[] arr = line.trim().split("\\s");
                            int c = 0;
                            for (String a : arr) {
                                if (!a.trim().equals("")) {
                                    c++;
                                    if (c == 1) {
                                        port = a.trim();
                                    } else if (c == 2) {
                                        state = a.trim();
                                    } else if (c == 3) {
                                        service = a.trim();
                                    }
                                }
                            }
                            if (port != null) {
                                portService = new PortInfo();
                                String[] parr = port.split("/");
                                portService.setPort(parr[0]);
                                portService.setProtocal(parr[1]);
                                portService.setState(state);
                                if (service != null) {
                                    service = service.replaceAll("\\?", "");
                                }
                                portService.setType(service);
                                portService.setBanner(null);
                                String version = line.replaceFirst(port, "").replaceFirst(state, "").replaceFirst(service, "").trim();
                                version = version.replaceAll("\\?", "");
                                if (version != null && !version.equals("")) {
                                    portService.setVersion(version);
                                } else {
                                    portService.setVersion("*");
                                }
                            }
                        }
                    }
                    if (line.contains("CVE-") && ls.size() <= cveNumber) {
                        String[] arr = line.split("\\[|\\]|\\s");
                        for (String a : arr) {
                            if (a.contains("CVE-") && !a.contains(",")) {
                                a = a.replaceAll("\\.", "").trim();
                                if (!ls.contains(a)) {
                                    ls.add(a);
                                    break;
                                }
                            }
                        }
                    }
                }
                isr.close();
                rd.close();
                if (nmapInfo.getIp() != null && !nmapInfo.getIp().equals("")) {
                    if (portService != null && portService.getPort() != null) {
                        if (ls != null && ls.size() > 0) {
                            portService.setVul(ls);
                        }
                        lp.add(portService);
                    }
                    nmapInfo.setPortInfos(lp);
                    if (nmapInfo.getOs_guess() != null || nmapInfo.getOs_detail() != null || nmapInfo.getDevice_type() != null
                            || (nmapInfo.getPortInfos() != null && nmapInfo.getPortInfos().size() > 0)) {
                        w.write(JSON.toJSONString(nmapInfo) + "\r\n");
                    }
                }
                w.close();
            }
        }
    }

    public static NmapInfo combineNmapInfo(List<NmapInfo> nmapInfos) {
        NmapInfo re = new NmapInfo();
        Map<String, PortInfo> map = new HashMap<>();
        re.setPortInfos(new ArrayList<>());
        for (NmapInfo nmapInfo : nmapInfos) {
            if (re.getIp() == null && nmapInfo.getIp() != null && !nmapInfo.getIp().equals("") && !nmapInfo.getIp().equals("*")) {
                re.setIp(nmapInfo.getIp());
            }
            if (re.getDomain() == null && nmapInfo.getDomain() != null &&
                    !nmapInfo.getDomain().equals("") && !nmapInfo.getDomain().equals("*")) {
                re.setDomain(nmapInfo.getDomain());
            }
            if (re.getOs_detail() == null && nmapInfo.getOs_detail() != null &&
                    !nmapInfo.getOs_detail().equals("") && !nmapInfo.getOs_detail().equals("*")) {
                re.setOs_detail(nmapInfo.getOs_detail());
            }
            if (re.getOs_guess() == null && nmapInfo.getOs_guess() != null &&
                    !nmapInfo.getOs_guess().equals("") && !nmapInfo.getOs_guess().equals("*")) {
                re.setOs_guess(nmapInfo.getOs_guess());
            }
            if (re.getDevice_type() == null && nmapInfo.getDevice_type() != null &&
                    !nmapInfo.getDevice_type().equals("") && !nmapInfo.getDevice_type().equals("*")) {
                re.setDevice_type(nmapInfo.getDevice_type());
            }
            if (re.getRunning() == null && nmapInfo.getRunning() != null &&
                    !nmapInfo.getRunning().equals("") && !nmapInfo.getRunning().equals("*")) {
                re.setRunning(nmapInfo.getRunning());
            }
            if (nmapInfo.getPortInfos() != null) {
                for (PortInfo portInfo : nmapInfo.getPortInfos()) {
                    if (!map.containsKey(portInfo.getPort())) {
                        map.put(portInfo.getPort(), portInfo);
                        re.getPortInfos().add(portInfo);
                    } else {
                        PortInfo pt = map.get(portInfo.getPort());
                        if ((pt.getType() == null || pt.getType().equals("") || pt.getType().equals("*")) && portInfo.getType() != null) {
                            pt.setType(portInfo.getType());
                        }
                        if ((pt.getProtocal() == null || pt.getProtocal().equals("") || pt.getProtocal().equals("*")) && portInfo.getProtocal() != null) {
                            pt.setProtocal(portInfo.getProtocal());
                        }
                        if ((pt.getState() == null || pt.getState().equals("") || pt.getState().equals("*")) && portInfo.getState() != null) {
                            pt.setState(portInfo.getState());
                        }
                        if ((pt.getVersion() == null || pt.getVersion().equals("") || pt.getVersion().equals("*")) && portInfo.getVersion() != null) {
                            pt.setVersion(portInfo.getVersion());
                        }
                        if ((pt.getBanner() == null || pt.getBanner().equals("") || pt.getBanner().equals("*")) && portInfo.getBanner() != null) {
                            pt.setBanner(portInfo.getBanner());
                        }
                        if ((pt.getVul() == null || pt.getVul().size() <= 0) && portInfo.getVul() != null) {
                            pt.setVul(portInfo.getVul());
                        }
                    }
                }
            }
        }
        return re;

    }

    //获取Nmap扫描文件的所有Nmap相关信息，结果放到Map，可能会有重复
    public static void getAllNmapInfo(String dir, Map<String, NmapInfo> map) throws Exception {
        File file = new File(dir);
        if (map == null) {
            map = new HashMap<>();
        }
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                getAllNmapInfo(f.getPath(), map);
            }
        } else {
            if (file.getName().contains("IPMeasureRe") || file.getName().contains("nmapPortRe")) {
                InputStreamReader isr = new InputStreamReader(new FileInputStream(file), "utf-8");
                BufferedReader rd = new BufferedReader(isr);
                String line;
                boolean checkPort = false;
                PortInfo portService = null;
                List<String> ls = new ArrayList<>();
                NmapInfo nmapInfo = new NmapInfo();
                List<PortInfo> lp = new ArrayList<>();
                while ((line = rd.readLine()) != null) {
                    line = line.replaceAll("﻿", "");
                    if (line.toLowerCase().contains("nmap scan report")) {
                        if (nmapInfo.getIp() != null && !nmapInfo.getIp().equals("")) {
                            if (portService != null && portService.getPort() != null) {
                                if (ls != null && ls.size() > 0) {
                                    portService.setVul(ls);
                                }
                                lp.add(portService);
                            }
                            nmapInfo.setPortInfos(lp);
                            if (nmapInfo.getOs_guess() != null || nmapInfo.getOs_detail() != null || nmapInfo.getDevice_type() != null
                                    || (nmapInfo.getPortInfos() != null && nmapInfo.getPortInfos().size() > 0)) {
                                if (map.containsKey(nmapInfo.getIp())) {
                                    List<NmapInfo> nmapInfos = new ArrayList<>();
                                    nmapInfos.add(map.get(nmapInfo.getIp()));
                                    nmapInfos.add(nmapInfo);
                                    NmapInfo re = combineNmapInfo(nmapInfos);
                                    map.put(nmapInfo.getIp(), re);
                                } else {
                                    map.put(nmapInfo.getIp(), nmapInfo);
                                }
                            }

                        }
                        String ip = "";
                        if (line.contains("(") && line.contains(")")) {
                            String a = line.substring(line.indexOf("(") + 1, line.indexOf(")")).trim();
                            if (ipp.matcher(a).matches()) {
                                ip = a;
                            }
                        }
                        if (ip.equals("")) {
                            String[] arr = line.split("\\s|\\(|\\)");
                            for (String a : arr) {
                                Matcher m = ipp.matcher(a);
                                if (m.matches()) {
                                    ip = a;
                                    break;
                                }
                            }
                        }
                        if (ip.equals("")) {
                            System.out.println(line);
                        }
                        nmapInfo = new NmapInfo();
                        nmapInfo.setIp(ip);
                        lp = new ArrayList<>();
                        ls = new ArrayList<>();
                        portService = new PortInfo();
                    }

                    if (line.contains("PORT") && line.contains("STATE") && line.contains("SERVICE")) {
                        checkPort = true;
                        continue;
                    }
                    if (line.contains("OS guesses:")) {
                        String os = line.substring(line.indexOf(":") + 1, line.length()).trim().replaceAll("\r\n", "");
                        nmapInfo.setOs_guess(os);
                    }
                    if (line.contains("OS details:")) {
                        String os = line.substring(line.indexOf(":") + 1, line.length()).trim().replaceAll("\r\n", "");
                        nmapInfo.setOs_detail(os);
                    }
                    if (line.contains("Device type") && line.contains(":")) {
                        String dt = line.substring(line.indexOf(":") + 1, line.length()).trim().replaceAll("\r\n", "");
                        nmapInfo.setDevice_type(dt);
                    }
                    if (line.contains("Running") && line.contains(":")) {
                        String dt = line.substring(line.indexOf(":") + 1, line.length()).trim().replaceAll("\r\n", "");
                        nmapInfo.setRunning(dt);
                    }

                    if (checkPort) {
                        Matcher m = p.matcher(line);
                        if (m.matches()) {
                            if (portService != null && portService.getPort() != null) {
                                if (ls != null && ls.size() > 0) {
                                    portService.setVul(ls);
                                }
                                lp.add(portService);
                                portService = new PortInfo();
                                ls = new ArrayList<>();
                            }
                            String port = null;
                            String state = null;
                            String service = null;
                            String[] arr = line.trim().split("\\s");
                            int c = 0;
                            for (String a : arr) {
                                if (!a.trim().equals("")) {
                                    c++;
                                    if (c == 1) {
                                        port = a.trim();
                                    } else if (c == 2) {
                                        state = a.trim();
                                    } else if (c == 3) {
                                        service = a.trim();
                                    }
                                }
                            }
                            if (port != null) {
                                portService = new PortInfo();
                                String[] parr = port.split("/");
                                portService.setPort(parr[0]);
                                portService.setProtocal(parr[1]);
                                portService.setState(state);
                                if (service != null) {
                                    service = service.replaceAll("\\?", "");
                                }
                                portService.setType(service);
                                portService.setBanner(null);
                                String version = line.replaceFirst(port, "").replaceFirst(state, "").replaceFirst(service, "").trim();
                                version = version.replaceAll("\\?", "");
                                if (version != null && !version.equals("")) {
                                    portService.setVersion(version);
                                } else {
                                    portService.setVersion("*");
                                }
                            }
                        }
                    }
                    if (line.contains("CVE-") && ls.size() <= 50) {
                        String[] arr = line.split("\\[|\\]|\\s");
                        for (String a : arr) {
                            if (a.contains("CVE-") && !a.contains(",")) {
                                a = a.replaceAll("\\.", "").trim();
                                if (!ls.contains(a)) {
                                    ls.add(a);
                                    break;
                                }
                            }
                        }
                    }
                }
                isr.close();
                rd.close();
                if (nmapInfo.getIp() != null && !nmapInfo.getIp().equals("")) {
                    if (portService != null && portService.getPort() != null) {
                        if (ls != null && ls.size() > 0) {
                            portService.setVul(ls);
                        }
                        lp.add(portService);
                    }
                    nmapInfo.setPortInfos(lp);
                    if (nmapInfo.getOs_guess() != null || nmapInfo.getOs_detail() != null || nmapInfo.getDevice_type() != null
                            || (nmapInfo.getPortInfos() != null && nmapInfo.getPortInfos().size() > 0)) {
                        if (map.containsKey(nmapInfo.getIp())) {
                            List<NmapInfo> nmapInfos = new ArrayList<>();
                            nmapInfos.add(map.get(nmapInfo.getIp()));
                            nmapInfos.add(nmapInfo);
                            NmapInfo re = combineNmapInfo(nmapInfos);
                            map.put(nmapInfo.getIp(), re);
                        } else {
                            map.put(nmapInfo.getIp(), nmapInfo);
                        }
                    }
                }
            }
        }
    }

    public static void getAllNmapInfoCombine(String dir, String outdir) throws Exception {
        File od = new File(outdir);
        if (!od.exists() && !od.getName().contains(".")) {
            od.mkdirs();
        }
        Map<String, NmapInfo> map = new HashMap<>();
        getAllNmapInfo(dir, map);
        List<Map.Entry<String, NmapInfo>> entryList = new ArrayList<Map.Entry<String, NmapInfo>>(map.entrySet());
        // 重写compare方法
        Collections.sort(entryList, new Comparator<Map.Entry<String, NmapInfo>>() {
            @Override
            public int compare(Map.Entry<String, NmapInfo> o1, Map.Entry<String, NmapInfo> o2) {
                Long l1 = IPDao.ipToLong(o1.getKey());
                Long l2 = IPDao.ipToLong(o2.getKey());
                return l1.compareTo(l2);
            }
        });
        Iterator<Map.Entry<String, NmapInfo>> iterator = entryList.iterator();
        Map.Entry<String, NmapInfo> tmpEntry = null;
        // 遍历传入的map
        while (iterator.hasNext()) {
            tmpEntry = iterator.next();
            NmapInfo nmapInfo = tmpEntry.getValue();
//			String port="";
//			if(nmapInfo.getPortInfos()!=null){
//				for(PortInfo portInfo:nmapInfo.getPortInfos()){
//					port+=","+portInfo.getPort();
//				}
//			}
//			port=port.replaceFirst(",","");
//			if(!port.equals("")){
//				w.write(nmapInfo.getIp()+"\t"+port+"\t"+port+"\r\n");
//			}
            Writer w = new FileWriter(new File(new File(outdir), nmapInfo.getIp() + ".txt"));
            w.write(JSON.toJSONString(nmapInfo, true));
            w.close();
        }
    }


    public static JSONObject getPortInfoJSON(PortInfo portInfo, String timestamp) throws Exception {
        JSONObject jsonObject = new JSONObject(true);
        jsonObject.put("protocal", portInfo.getProtocal());
        jsonObject.put("state", portInfo.getState());
        jsonObject.put("type", getNullString(portInfo.getType()));
        jsonObject.put("version", getNullString(portInfo.getVersion()));
        jsonObject.put("banner", getNullString(portInfo.getBanner()));
//		StringBuffer sbf=new StringBuffer();
//		if(portInfo.getVul()!=null&&portInfo.getVul().size()>0){
//			for(String v:portInfo.getVul()){
//				sbf.append(","+v);
//			}
//		}
        //sbf.toString().replaceFirst(",","")
        List<String> vuls = portInfo.getVul();
        if (vuls == null) {
            vuls = new ArrayList<>();
        }
        jsonObject.put("vul", vuls);
        jsonObject.put("timestamp", Long.parseLong(timestamp));
        return jsonObject;
    }

    public static boolean isNull(String s) {
        if (s == null) {
            return true;
        }
        s = s.trim();
        if (s.equals("") || s.equals("*")) {
            return true;
        }
        return false;
    }

    public static String getNullString(String s) {
        if (s == null) {
            return "*";
        }
        s = s.trim();
        if (s.equals("") || s.equals("*")) {
            return "*";
        }
        return s;
    }

    public static void formatTimestamp(JSONObject jo) {
        if (jo == null) {
            return;
        }
        try {
            JSONObject cu = jo.getJSONObject("current");
            Long cts = Long.parseLong(cu.getJSONObject("timestamp").getString("$numberLong"));
            cu.put("timestamp", cts);
        } catch (Exception e) {

        }
        JSONArray his = jo.getJSONArray("history");
        if (his != null && his.size() > 0) {
            for (int k = 0; k < his.size(); k++) {
                try {
                    JSONObject jj = his.getJSONObject(k);
                    Long hts = Long.parseLong(jj.getJSONObject("timestamp").getString("$numberLong"));
                    jj.put("timestamp", hts);
                } catch (Exception e) {

                }
            }
        }
    }


    public static void getAllIPOpenPort(String dir, String outdir) throws Exception {
        Writer w = new FileWriter(outdir);
        InputStreamReader isr = new InputStreamReader(new FileInputStream(dir), "utf-8");
        BufferedReader rd = new BufferedReader(isr);
        String s;
        while ((s = rd.readLine()) != null) {
            s = s.replaceAll("﻿", "");
            NmapInfo nmapInfo = JSON.parseObject(s, NmapInfo.class);
            StringBuffer sb = new StringBuffer();
            if (nmapInfo.getPortInfos() != null && nmapInfo.getPortInfos().size() > 0) {
                for (PortInfo portInfo : nmapInfo.getPortInfos()) {
                    if (portInfo.getState().toLowerCase().contains("open")) {
                        sb.append("," + portInfo.getPort());
                    }
                }
            }
            String op = sb.toString();
            if (op != null && !op.equals("")) {
                op = op.replaceFirst(",", "");
                w.write(nmapInfo.getIp() + "\t" + op + "\r\n");
            }
        }
        w.close();
    }

    //获取Nmap扫描文件的所有Nmap活跃IP
    public static void getAllNmapActiveIP(String dir, Set<String> set) throws Exception {
        File file = new File(dir);
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                getAllNmapActiveIP(f.getPath(), set);
            }
        } else {
            if (file.getName().contains("IPMeasureRe")) {
                InputStreamReader isr = new InputStreamReader(new FileInputStream(file), "utf-8");
                BufferedReader rd = new BufferedReader(isr);
                String line;
                String ip = null;
                while ((line = rd.readLine()) != null) {
                    line = line.replaceAll("﻿", "");
                    if (line.toLowerCase().contains("nmap scan report")) {
                        if (line.contains("(") && line.contains(")")) {
                            String a = line.substring(line.indexOf("(") + 1, line.indexOf(")")).trim();
                            if (ipp.matcher(a).matches()) {
                                ip = a;
                            }
                        }
                        if (ip == null || ip.equals("")) {
                            String[] arr = line.split("\\s|\\(|\\)");
                            for (String a : arr) {
                                Matcher m = ipp.matcher(a);
                                if (m.matches()) {
                                    ip = a;
                                    break;
                                }
                            }
                        }
                        if (ip == null || ip.equals("")) {
                            System.out.println(line);
                        }
                        continue;
                    }
                    if (line.toLowerCase().contains("host is up")) {
                        if (ip != null) {
                            set.add(ip);
                            ip = null;
                        }

                    }


                }
                isr.close();
                rd.close();
            }
        }
    }

    public static void getAllNmapActiveIP(String dir, String outdir) throws Exception {
        Set<String> set = new HashSet<>();
        getAllNmapActiveIP(dir, set);
        Writer w = new FileWriter(outdir);
        for (String ip : set) {
            w.write(ip + "\r\n");
        }
        w.close();
    }


    public static void quchong(String dir, String outdir) throws Exception {
        Set<String> set = new HashSet<>();
        Writer w = new FileWriter(outdir);
        InputStreamReader isr = new InputStreamReader(new FileInputStream(dir), "utf-8");
        BufferedReader rd = new BufferedReader(isr);
        String s;
        while ((s = rd.readLine()) != null) {
            s = s.replaceAll("﻿", "");
            JSONObject jsonObject = JSON.parseObject(s);
            String ip = jsonObject.getString("ip");
            if (!set.contains(ip)) {
                set.add(ip);
                w.write(s + "\r\n");
            }
        }
        isr.close();
        rd.close();
        w.close();
    }


    public static void getPortServiceNo(String dir, String outdir, String miguanDir) throws Exception {
        Writer w = new FileWriter(outdir);
        Writer w2 = new FileWriter(miguanDir);
        Map<String, Integer> map = new HashMap<>();
        InputStreamReader isr = new InputStreamReader(new FileInputStream(dir), "utf-8");
        BufferedReader rd = new BufferedReader(isr);
        String s;
        while ((s = rd.readLine()) != null) {
            s = s.replaceAll("﻿", "");
            JSONObject jsonObject = JSON.parseObject(s);
            JSONArray jsonArray = jsonObject.getJSONArray("portInfos");
            int count = 0;
            if (jsonArray != null && jsonArray.size() > 0) {
                for (int i = 0; i < jsonArray.size(); i++) {
                    JSONObject jo = jsonArray.getJSONObject(i);
                    if (jo.getString("state").contains("open")) {
                        count++;
                        String info = jo.getString("port") + "\t" + jo.getString("type");
                        if (!map.containsKey(info)) {
                            map.put(info, 1);
                        } else {
                            map.put(info, map.get(info) + 1);
                        }
                    }

                }
            }
            if (count >= 20) {
                w2.write(jsonObject.getString("ip") + "\t" + s + "\r\n");
            }
        }
        isr.close();
        rd.close();
        for (String key : map.keySet()) {
            w.write(key + "\t" + map.get(key) + "\r\n");
        }
        w.close();
        w2.close();
    }

    public static void main(String[] args) throws Exception {

//		getAllIPOpenPort("H:\\数据测量\\合肥数据提供\\越南IP端口操作系统\\IP端口扫描结果.txt",
//				"H:\\数据测量\\合肥数据提供\\越南IP端口操作系统\\开放端口IP.txt");

        //1882129599
//		System.out.println(IPDao.ipToLong("112.47.4.191"));

//		insertToDb("H:\\数据测量\\工业互联网\\绿色制造平台爬取\\IP扫描结果.txt","20220215000000");
//		getAllNmapInfo("I:\\工作2\\202206\\0613关岛洛杉矶测量\\关岛数据\\关岛重点单位\\端口服务",
//				"I:\\工作2\\202206\\0613关岛洛杉矶测量\\关岛数据\\关岛重点单位\\端口服务.txt",10000);

//		quchong("I:\\工作2\\202206\\0614台湾大哥大\\端口服务扫描\\端口服务测量数据.txt",
//				"I:\\工作2\\202206\\0614台湾大哥大\\端口服务扫描\\端口服务测量数据_去重.txt");

//		getPortServiceNo("I:\\工作2\\202206\\0613关岛洛杉矶测量\\关岛数据\\端口服务测量数据.txt",
//				"I:\\工作2\\202206\\0613关岛洛杉矶测量\\关岛数据\\端口服务测量数据统计.txt",
//				"I:\\工作2\\202206\\0613关岛洛杉矶测量\\关岛数据\\蜜罐.txt");

//		insertToDbCoverCurr("H:\\数据测量\\全球端口服务测量\\20220408全球端口服务\\端口服务测量数据8.txt","20220616000000",
//				false);
//
//		getAllNmapInfoCombine("H:\\数据测量\\全球端口服务测量\\20220408朝鲜端口服务\\0411\\漏洞及端口扫描",
//				"H:\\数据测量\\全球端口服务测量\\20220408朝鲜端口服务\\0411\\端口及漏洞结果");
//
//		insertToDbCoverCurr("H:\\数据测量\\合肥数据提供\\纽约IP端口操作系统\\IP端口扫描结果.txt","20220218000000");
//		Thread.sleep(10000);
//		insertToDbCoverCurr("H:\\数据测量\\合肥数据提供\\纽约IP端口操作系统\\IP漏洞扫描结果.txt","20220324000000");
//		Thread.sleep(10000);
//		insertToDbCoverCurr("H:\\数据测量\\合肥数据提供\\东京IP端口操作系统\\IP漏洞扫描结果.txt","20220324000000");
//		Thread.sleep(10000);
//		insertToDbCoverCurr("H:\\数据测量\\合肥数据提供\\越南IP端口操作系统\\IP端口扫描结果.txt","20220228000000");
//		Thread.sleep(10000);
//		insertToDbCoverCurr("H:\\数据测量\\合肥数据提供\\洛杉矶IP端口操作系统\\IP端口扫描结果.txt","20220328000000");

//		getAllNmapActiveIP("H:\\数据测量\\全球端口服务测量\\20220408朝鲜端口服务\\TCPPing",
//				"H:\\数据测量\\全球端口服务测量\\20220408朝鲜端口服务\\TCPActive.txt");


//		System.out.println(IPDao.ipToLong("175.6.245.20"));

//        getAllNmapInfo("F:\\Analysis\\2022-江苏设备\\终端节点分析\\nmapResult",
//                "F:\\Analysis\\2022-江苏设备\\终端节点分析\\nmapRes.txt", 10000);

        getAllNmapInfo("F:\\Analysis\\0614台湾大哥大\\端口服务扫描\\测量数据",
                "F:\\Analysis\\0614台湾大哥大\\IP用途分类\\nmapRes.txt", 10);
    }

}
