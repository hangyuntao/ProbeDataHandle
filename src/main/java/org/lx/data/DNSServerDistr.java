package org.lx.data;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.lx.pojo.ASInfo;
import org.lx.service.IPASInfoSearch;
import org.lx.tools.ip.GetIPLocation;
import org.lx.tools.ip.IPDao;
import org.lx.tools.ip.IPLocation;

import java.io.*;
import java.util.*;

public class DNSServerDistr {


    public static void getDNSDistr1118(File file,Map<String, DNSDistr> map) throws Exception{
        InputStreamReader isr= new InputStreamReader(new FileInputStream(file), "utf-8");
        BufferedReader rd=new BufferedReader(isr);
        String s;
        while((s=rd.readLine())!=null){
            if(map!=null&&map.size()%1000000==0){
                System.out.println(map.size());
            }
            s=s.replaceAll("﻿", "");
            String[] arr=s.split("\t");
            List<String> anaRe=new ArrayList<>();
            if(!arr[1].equals("*")&&!arr[2].equals("*")){
                JSONArray jsonArray=JSON.parseArray(arr[4]);
                for(int i=0;i<jsonArray.size();i++){
                    JSONObject jo=jsonArray.getJSONObject(i);
                    if(jo!=null&&jo.getString("data")!=null){
                        if(jo.getString("data").contains(":")){
                            if(jo.getString("data").split(":").length!=8){
                                System.out.println(arr[4]);
                            }else{
                                break;
                            }
                        }else {
                            String ip=jo.getString("data");
                            if(IPDao.JudgeIP(ip)&&!ip.equals("0.0.0.0")) {
                                anaRe.add(ip);
                            }
                        }
                    }
                }
                JSONObject jsonObject2=JSON.parseObject(arr[3]);
                String domain=jsonObject2.getString("name");
                if(domain.endsWith(".")){
                    domain=domain.substring(0, domain.length()-1);
                }
                String head="";
                JSONObject jsonObject=null;
                head=arr[2];
                if(arr[1].contains("{")){
                    head=arr[1];
                }
                jsonObject=JSON.parseObject(head);
                if(!map.containsKey(arr[0])){
                    map.put(arr[0], new DNSDistr());
                }
                DNSDistr dnsPerform=map.get(arr[0]);
                dnsPerform.setIp(arr[0]);
                double time=Double.parseDouble(arr[1]);
                dnsPerform.setAllTime(dnsPerform.getAllTime()+time);
                dnsPerform.setAllCount(dnsPerform.getAllCount()+1);
//				dnsPerform.getDateSet().add(arr[5]);
                //System.out.println(s);
                if(jsonObject.getString("AA").equals("1")){
                    if(domain.equals(arr[0])) {
                        int qw=map.get(arr[0]).getQwNo();
                        map.get(arr[0]).setQwNo(++qw);
                    }

                }else{
                    if(jsonObject.getString("AA").equals("0")&&jsonObject.getString("RA").equals("1")){
                        if(domain.equals(arr[0])) {
                            int dg=map.get(arr[0]).getDgNo();
                            map.get(arr[0]).setDgNo(++dg);
                        }else if(jsonObject.getString("Recode").equals("0000")){
                            if(anaRe.size()>0){
                                int kf=map.get(arr[0]).getKfNo();
                                map.get(arr[0]).setKfNo(++kf);
                            }
                        }
                    }
                }

//				Map<String, String> mp=dnsPerform.getMeasureMap();
//				String key=file.getParentFile().getParentFile().getName();
//				if(mp.containsKey(key)) {
//					String val=mp.get(key);
//					if(!val.contains(arr[5]+",")) {
//						mp.put(key, val+arr[5]+",");
//					}
//				}else {
//					mp.put(key, arr[5]+",");
//				}

            }
        }
        isr.close();
        rd.close();
    }


    public static Map<String, DNSDistr> getAllDNSDistr(File datadir,Map<String, DNSDistr> map) throws Exception{
        if(map==null){
            map=new HashMap<>();
        }
        if(datadir.isDirectory()){
            System.out.println(datadir.getName());
            for(File file:datadir.listFiles()){
                getAllDNSDistr(file, map);
            }
        }else{
            if(datadir.getName().contains("DNSDetect")){
                getDNSDistr1118(datadir, map);
            }
        }
        
        return map;
    }


    public static void getDNSDistr(String datadir,String outdir) throws Exception{
        Map<String, DNSDistr> map=getAllDNSDistr(new File(datadir), null);
        
        Writer w1=new FileWriter(outdir);
        for(String key:map.keySet()){
            DNSDistr dnsRe=map.get(key);
            String type="*";
            if(dnsRe.getDgNo()>dnsRe.getQwNo()){
                type="递归";
                if(dnsRe.getKfNo()>0){
                    type="开放递归";
                }
            }else if(dnsRe.getDgNo()==dnsRe.getQwNo()){
                if(dnsRe.getKfNo()>0){
                    type="开放递归";
                }else{
                    if(dnsRe.getQwNo()>0){
                        type="权威";
                    }
                }
            }else{
                if(dnsRe.getQwNo()==0){
                    type="*";
                }else{
                    type="权威";
                }
            }
            if(type.equals("*")){
                continue;
            }
            w1.write(key+"\t"+dnsRe.getQwNo()+"\t"+dnsRe.getDgNo()+"\t"+dnsRe.getKfNo()+"\t"+type+"\r\n");
        }
        w1.close();
    }



    public static void main(String[] args) throws Exception{

        
        File file = new File("D:\\Desktop\\洛杉矶\\节点分析\\dns扫描结果");
        File baseFolder = new File("D:\\Desktop\\洛杉矶\\节点分析\\dns解析结果");
        
        if (!baseFolder.exists()) {
            baseFolder.mkdirs();
        }
        
        File[] files = file.listFiles();
    
        //for (int i = 0; i < files.length; i++) {
        //    File file1 = files[i];
        //    getDNSDistr(file1.getAbsolutePath(),
        //            "D:\\Desktop\\洛杉矶\\节点分析\\dns解析结果\\" + i + ".txt");
        //}
        
        files = baseFolder.listFiles();
        Map<String, String> map = new HashMap<>();
        for (File file1 : files) {
            BufferedReader br = new BufferedReader(new FileReader(file1));
            String line;
            
            while((line = br.readLine()) != null) {
                map.put(line.split("\t")[0], line.split("\t")[4]);
            }
        }
        System.out.println(map.keySet().size());
    
        IPASInfoSearch ipasInfoSearch = new IPASInfoSearch(IPASInfoSearch.asDir);
    
        FileWriter writer = new FileWriter("设备-DNS.txt");
        int i = 0;
        BufferedReader br = new BufferedReader(new FileReader("D:\\Desktop\\洛杉矶\\节点分析\\设备-email.txt"));
        String line;
        Set<String> set = new HashSet<>();
        while((line = br.readLine()) != null) {
            set.add(line.split("\t")[0]);
        }
        for (String s : map.keySet()) {
            ASInfo asInfo = ipasInfoSearch.searchIPASInfo(s);
            IPLocation lc = GetIPLocation.get(s);
            if ("US".equals(asInfo.getOrg_country()) && lc.getCity().contains("洛杉矶")) {
                System.out.println(s);
                System.out.println(++i);
                if (set.contains(s)) {
                    continue;
                }
                writer.write(s + "\t" + "/53" + "\t" + map.get(s) + "\t" + asInfo.getAut() + "," + asInfo.getAut_name() + "," + asInfo.getIsp() + "\t" +
                        lc.getCountry() + "," + lc.getProvince() + "," + lc.getCity() + "\r\n");
            }
        }
        
        writer.close();
    }
}
