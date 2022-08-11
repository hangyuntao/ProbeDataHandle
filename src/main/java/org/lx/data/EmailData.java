package org.lx.data;

import com.alibaba.fastjson.JSONObject;
import org.lx.tools.ip.GetIPLocation;
import org.lx.tools.ip.IPLocation;

import java.io.*;
import java.math.BigDecimal;
import java.util.*;

public class EmailData {


    public static void getPerformMap(File dir, Map<String, EmailPerform> map) throws Exception{
        if(dir.isDirectory()) {
            for(File f:dir.listFiles()) {
                getPerformMap(f, map);
            }
        }else {
            String fn=dir.getName().toLowerCase();
            if(fn.endsWith(".txt")&&fn.contains("email")) {
                InputStreamReader isr= new InputStreamReader(new FileInputStream(dir), "utf-8");
                BufferedReader rd=new BufferedReader(isr);
                String s;
                while((s=rd.readLine())!=null){
                    s=s.replaceAll("﻿", "");
                    String[] arr=s.split("\t");
                    if(!map.containsKey(arr[0])) {
                        map.put(arr[0], new EmailPerform());
                    }
                    EmailPerform emailPerform=map.get(arr[0]);
                    if(emailPerform.getType()==null) {
                        emailPerform.setType(arr[1]);
                    }else {
                        if(!emailPerform.getType().equals(arr[1])&&!emailPerform.getType().contains(","+arr[1])){
                            emailPerform.setType(emailPerform.getType()+","+arr[1]);
                        }
                    }

                    emailPerform.setAllCount(emailPerform.getAllCount()+1);
                    emailPerform.setOnline(emailPerform.getOnline()+1);

                    if(!arr[2].equals("*")) {
                        try {
                            Double rep=Double.parseDouble(arr[2]);
                            if(rep!=null) {
                                emailPerform.setDelay(emailPerform.getDelay()+rep);
                            }
                        } catch (Exception e) {
                        }
                    }

                    if(!arr[3].equals("*")) {
                        try {
                            Double vali=Double.parseDouble(arr[3]);
                            if(vali!=null&&vali>0) {
                                emailPerform.setValiCount(emailPerform.getValiCount()+1);
                                emailPerform.setValidate(emailPerform.getValidate()+vali);
                            }
                        } catch (Exception e) {
                        }
                    }

                }
                isr.close();
                rd.close();
            }
        }
    }

    public static String getEmailType(Integer port) {
        if(port==null) {
            return "*";
        }
        switch (port) {
            case 25:
                return "smtp";
            case 465:
                return "smtps";
            case 587:
                return "smtps";
            case 994:
                return "smtps";
            case 110:
                return "pop3";
            case 995:
                return "pop3s";
            case 143:
                return "imap";
            case 993:
                return "ipmaps";
            default:
                return "*";
        }
    }
    public static Double getScore(double o,double d){
        if(d<=0||Double.isNaN(d)){
            return 50*o;
        }else{
            if(d<=100){
                return 50+50*o;
            }else{
                return 50*o+50*100/d;
            }
        }
    }

    public static void handleData(String dir,String outdir,Integer measureCount) throws Exception{
        if (measureCount==null||measureCount<=0){
            measureCount=1;
        }
        File ff=new File(dir);
        Map<String, EmailPerform> map=new HashMap<>();
        getPerformMap(ff, map);
        Writer w=new FileWriter(outdir);
        for(String key:map.keySet()) {
            
            IPLocation lc = GetIPLocation.get(key);
            
            if (!lc.getCity().contains("洛杉矶")) {
                continue;
            }
            
            EmailPerform emailPerform = map.get(key);
            List<String> ls = new ArrayList<>();
            for (String t : emailPerform.getType().split(",")) {
                try {
                    String tp = getEmailType(Integer.parseInt(t));
                    if (!tp.equals("*")&&!ls.contains(tp)) {
                        ls.add(tp);
                    }
                } catch (Exception e) {
                }
            }
            Collections.sort(ls);
            String type = "";
            for (String t : ls) {
                if (type.equals("")) {
                    type += t;
                } else {
                    type += "," + t;
                }
            }
            JSONObject jsonObject = new JSONObject(true);
            jsonObject.put("ip", key);
            jsonObject.put("type", type);

            Double allCount = (double) (ls.size() * measureCount);
            if (emailPerform.getOnline() > 0 && allCount > 0) {
                Double o = emailPerform.getOnline() / allCount;
                if (o > 1) {
                    o = (double) 1;
                }
                BigDecimal online = BigDecimal.valueOf(o);
                jsonObject.put("online", online);
                Double d = emailPerform.getValidate() / emailPerform.getValiCount();

                if (emailPerform.getValidate() > 0 && emailPerform.getValiCount() > 0) {
                    BigDecimal delay = BigDecimal.valueOf(d);
                    jsonObject.put("delay", delay);
                } else {
                    jsonObject.put("delay", BigDecimal.valueOf(-1));
                }
                Double score = getScore(o, d);
                jsonObject.put("score", BigDecimal.valueOf(score));
                w.write(key + "\t"+jsonObject.getString("type")+ "\r\n");
            }
        }
        w.close();
    }



    public static void main(String[] args) throws Exception{
        handleData("D:\\Desktop\\洛杉矶\\节点分析\\email扫描结果",
                "D:\\Desktop\\洛杉矶\\节点分析\\设备-email.txt",2);
    }
}
