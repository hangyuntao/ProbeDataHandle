package org.lx.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.lx.pojo.ASInfo;
import org.lx.tools.FileDao;
import org.lx.tools.ip.IPDao;

import java.io.*;
import java.util.*;

public class IPASInfoSearch {


    public static Map<String, JSONObject> asInfoMap = new HashMap<>();

    public static String asDir = "D:\\dataCollect\\ASInfo.txt";

    public IPASInfoSearch() {

    }

    public IPASInfoSearch(String dir) throws Exception {
        InputStreamReader isr = new InputStreamReader(new FileInputStream(dir), "utf-8");
        BufferedReader rd = new BufferedReader(isr);
        String s;
        while ((s = rd.readLine()) != null) {
            s = s.replaceAll("﻿", "");
            JSONObject jo = JSON.parseObject(s);
            String key = jo.getString("_id");
            String[] ss = key.split("/");
            if (max < Integer.valueOf(ss[1])) {
                max = Integer.valueOf(ss[1]);
            }
            if (min > Integer.valueOf(ss[1])) {
                min = Integer.valueOf(ss[1]);
            }

            String binaryString = ChangeIP2Binary(ss[0]);
            String prefixString = binaryString.substring(0, Integer.valueOf(ss[1]));
            asInfoMap.put(prefixString, jo);
        }
        isr.close();
        rd.close();
    }

    int max = 0;
    int min = 32;


    public String ChangeIP2Binary(String sIP) {
        String prefixString = "000000000000000000000000";
        String binaryString = Long.toBinaryString(IPDao.ipToLong(sIP));
        if (binaryString.length() < 32) {
            try {
                binaryString = prefixString.substring(0, 32 - binaryString.length()) + binaryString;
            } catch (Exception e) {
                System.out.println(binaryString.length());
            }
        }
        return binaryString;
    }


    public List<JSONObject> GetASInfoByIP(String sIP) {
        List<JSONObject> lj = new ArrayList<>();
        String binaryString = ChangeIP2Binary(sIP);
        for (int i = max; i >= min; i--) {
            String key = binaryString.substring(0, i);
            if (asInfoMap.containsKey(key)) {
                JSONObject jo = asInfoMap.get(key);
                JSONArray jsonArray = jo.getJSONArray("asInfos");
                for (int j = 0; j < jsonArray.size(); j++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(j);
                    jsonObject.put("prefix", jo.getString("_id"));
                    lj.add(jsonObject);
                }
            }
        }
        return lj;
    }

    public ASInfo searchIPASInfo(String ip) {
        ASInfo asInfo = new ASInfo();
        List<JSONObject> lj = GetASInfoByIP(ip);
        String asn = "";
        String isp_name = "";
        String org_name = "";
        String isp = "";
        String prefix = "";
        String org_country = "";
        Set<String> asnList = new HashSet<>();
        Set<String> isp_nameList = new HashSet<>();
        Set<String> org_nameList = new HashSet<>();
        Set<String> ispList = new HashSet<>();
        Set<String> prefixList = new HashSet<>();
        Set<String> org_countryList = new HashSet<>();
        for (JSONObject jo : lj) {
            String aut = jo.getJSONObject("aut").getLong("$numberLong") + "";
            String an = jo.getString("aut_name");
            String on = jo.getString("org_name");
            String yys = jo.getString("isp");
            String pre = jo.getString("prefix");
            String orgCountry = jo.getString("org_country");
            if (!prefixList.contains(pre)) {
                prefixList.add(pre);
                if (prefix.equals("")) {
                    prefix += pre;
                } else {
                    prefix += "_" + pre;
                }
            }
            if (!aut.equals("") && !aut.equals("*")) {
                if (!asnList.contains(aut)) {
                    asnList.add(aut);
                    if (asn.equals("")) {
                        asn += aut;
                    } else {
                        asn += "_" + aut;
                    }
                }
            }
            if (!an.equals("") && !an.equals("*")) {
                if (!isp_nameList.contains(an)) {
                    isp_nameList.add(an);
                    if (isp_name.equals("")) {
                        isp_name += an;
                    } else {
                        isp_name += "_" + an;
                    }
                }
            }
            if (!on.equals("") && !on.equals("*")) {
                if (!org_nameList.contains(on)) {
                    org_nameList.add(on);
                    if (org_name.equals("")) {
                        org_name += on;
                    } else {
                        org_name += "_" + on;
                    }
                }
            }
            if (!yys.equals("") && !yys.equals("*")) {
                if (!ispList.contains(yys)) {
                    ispList.add(yys);
                    if (isp.equals("")) {
                        isp += yys;
                    } else {
                        isp += "_" + yys;
                    }
                }
            }
            if(!orgCountry.equals("")&&!orgCountry.equals("*")){
                if(!org_countryList.contains(orgCountry)){
                    org_countryList.add(orgCountry);
                    if(org_country.equals("")){
                        org_country+=orgCountry;
                    }else{
                        org_country+="_"+orgCountry;
                    }
                }
            }
        }
    
        asInfo.setOrg_country(org_country.equals("") ? "*" : org_country);
    
        asInfo.setAut(asn.equals("") ? "*" : asn);
        asInfo.setAut_name(isp_name.equals("") ? "*" : isp_name);
        asInfo.setOrg_name(org_name.equals("") ? "*" : org_name);
        asInfo.setIsp(isp.equals("") ? "*" : isp);
        asInfo.setPrefix(prefix);
        return asInfo;
    }

    public static void getIPASInfo(String dir, String asIspDir, String outdir) throws Exception {
        Writer w = new FileWriter(outdir);
        Map<String, String> ispMap = new HashMap<>();
        if (asIspDir != null && !asIspDir.equals("")) {
            ispMap = FileDao.getMap(asIspDir, "utf-8", new int[]{0}, new int[]{3});
        }
        IPASInfoSearch ipasInfoSearch = new IPASInfoSearch(IPASInfoSearch.asDir);
        InputStreamReader isr = new InputStreamReader(new FileInputStream(dir), "utf-8");
        BufferedReader rd = new BufferedReader(isr);
        String s;
        while ((s = rd.readLine()) != null) {
            s = s.replaceAll("﻿", "");
            String[] arr = s.split("\t");
            ASInfo asInfo = ipasInfoSearch.searchIPASInfo(arr[0]);
            String as = "*";
            String as_name = "*";
            String org_name = "*";
            String isp = "*";
            if (!asInfo.getAut().equals("*")) {
                String pre = asInfo.getPrefix();
                as = asInfo.getAut().split("_")[0];
                as_name = asInfo.getAut_name().split("_")[0];
                org_name = asInfo.getOrg_name().split("_")[0];
                if (ispMap.containsKey(as)) {
                    isp = ispMap.get(as);
                } else {
                    isp = asInfo.getIsp();
                }
                int n = Integer.parseInt(pre.split("_")[0].split("/")[1]);
                if (n > 24) {
                    System.out.println(s + "\t" + as + "\t" + as_name + "\t" + org_name + "\t" + pre + "\t" + isp);
                }
            }
            w.write(s + "\t" + as + "\t" + as_name + "\t" + org_name + "\t" + isp + "\r\n");
        }
        isr.close();
        rd.close();
        w.close();
    }


    public static void main(String[] args) throws Exception {

        getIPASInfo("I:\\工作2\\202206\\0613关岛洛杉矶测量\\关岛数据\\关岛全部IPC.txt",
                "I:\\工作2\\202206\\0613关岛洛杉矶测量\\关岛数据\\ASIsp.txt",
                "I:\\工作2\\202206\\0613关岛洛杉矶测量\\关岛数据\\关岛全部IPC及AS.txt");


//        DataHelper dataHelper=new DataHelper("AnchorX2020","ipv4_as");
//        IPASInfoSearch ipasInfoSearch=new IPASInfoSearch(dataHelper);
//        dataHelper.close();

//        String s="09121098";
//        System.out.println(s.substring(0,8));
//        System.out.println(new IPASInfoSearch(asDir).searchIPASInfo("63.103.50.1").getASInfo());
//        Writer w=new FileWriter("I:\\离线库\\IPMarker\\ipm_base_main_comp3.txt");
//        InputStreamReader isr= new InputStreamReader(new FileInputStream("I:\\离线库\\IPMarker\\ipm_base_main_comp.txt"), "utf-8");
//        BufferedReader rd=new BufferedReader(isr);
//        String s;
//        while((s=rd.readLine())!=null){
//            s=s.replaceAll("﻿", "");
//            String[] arr=s.split("\t");
//            String ip=arr[1];
//            ASInfo asInfo=ipasInfoSearch.searchIPASInfo(ip);
//            w.write(s+"\t"+asInfo.getAut()+"\t"+asInfo.getAut_name()+"\t"+asInfo.getOrg_name()+"\t"+asInfo.getIsp()+
//                    "\r\n");
//        }
//        isr.close();
//        rd.close();
//        w.close();


    }


}
