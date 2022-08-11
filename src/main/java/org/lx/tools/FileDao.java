package org.lx.tools;

import org.lx.tools.ip.IPDao;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class FileDao {
	public static List<String> GetFileList(String dir) throws Exception{
		List<String> ls=new ArrayList<String>();
		BufferedReader rd=new BufferedReader(new FileReader(dir));
		String s;
		while((s=rd.readLine())!=null){
			s=s.replaceAll("﻿", "");
			ls.add(s);
		}
		rd.close();
		return ls;
	}
	public static List<String> GetFileList1(String dir,String code) throws Exception{
		List<String> ls=new ArrayList<String>();
		InputStreamReader isr= new InputStreamReader(new FileInputStream(dir), code);
		BufferedReader rd=new BufferedReader(isr);
		String s;
		while((s=rd.readLine())!=null){
			s=s.replaceAll("﻿", "");
			ls.add(s);
		}
		isr.close();
		rd.close();
		return ls;
	}

	public static void reorderFile(String dir,String code, String outdir, Comparator comparator) throws Exception{
		List<String> ls=new ArrayList<String>();
		InputStreamReader isr= new InputStreamReader(new FileInputStream(dir), code);
		BufferedReader rd=new BufferedReader(isr);
		String s;
		while((s=rd.readLine())!=null){
			s=s.replaceAll("﻿", "");
			ls.add(s);
		}
		isr.close();
		rd.close();
		if(comparator!=null){
			Collections.sort(ls,comparator);
		}else{
			Collections.sort(ls);
		}
		Writer w=new FileWriter(outdir);
		for(String a:ls){
			w.write(a+"\r\n");
		}
		w.close();
	}
	public static Set<String> GetFileSet(String dir,String code) throws Exception{
		Set<String> ls=new HashSet<String>(); 
		InputStreamReader isr= new InputStreamReader(new FileInputStream(dir), code);
		BufferedReader rd=new BufferedReader(isr);
		String s;
		while((s=rd.readLine())!=null){
			s=s.replaceAll("﻿", "");
			if(s.trim().equals("")){
				continue;
			}
			ls.add(s);
		}
		rd.close();
		return ls;
	}
   public static void CombineFile(List<File> f, String outdir,String code) throws Exception{
	   Writer w=new FileWriter(outdir);
	   for(int i=0;i<f.size();i++){
		   InputStreamReader isr= new InputStreamReader(new FileInputStream(f.get(i)), code);
			BufferedReader rd=new BufferedReader(isr);
		   String s;
		   while((s=rd.readLine())!=null){
			   s=s.replaceAll("﻿", "");
			   if(!s.trim().equals("")){
				   w.write(s+"\r\n");
			   }
		   }
		   rd.close();
	   }
	   w.close();
   }
   
   
   public static void CombineFile(File dir, File outdir,String code) throws Exception{
	   Writer w=new FileWriter(outdir);
	   for(File f:dir.listFiles()){
		   if(!f.getName().endsWith(".txt")){
			   continue;
		   }
		   InputStreamReader isr= new InputStreamReader(new FileInputStream(f), code);
			BufferedReader rd=new BufferedReader(isr);
		   String s;
		   while((s=rd.readLine())!=null){
			   s=s.replaceAll("﻿", "");
			   if(!s.trim().equals("")){
				   w.write(s+"\r\n");
			   }
		   }
		   rd.close();
	   }
	   w.close();
   }
	public static Map<String,List<String>> getMapList(String dir,String code,int[] key,int[] value) throws Exception{
		Map<String, List<String>> map=new HashMap<>();
		InputStreamReader isr= new InputStreamReader(new FileInputStream(dir), code);
		BufferedReader rd=new BufferedReader(isr);
		String s;
		while((s=rd.readLine())!=null){
			s=s.replaceAll("﻿", "");
			String[] arr=s.split("\t");
			String mk="";
			for(int i:key){
				mk+="\t"+arr[i];
			}
			mk=mk.replaceFirst("\t", "").trim();
			String mv="";
			for(int i:value){
				mv+="\t"+arr[i];
			}
			mv=mv.replaceFirst("\t", "");
			if(!map.containsKey(mk)){
				map.put(mk,new ArrayList<>());
			}
			if(!map.get(mk).contains(mv)){
				map.get(mk).add(mv);
			}
		}
		rd.close();
		return map;
	}
   
   
   public static Set<String> DuplicateRemove(String dir,String code) throws Exception{
	   Set<String> ls=new HashSet<String>();
		InputStreamReader isr= new InputStreamReader(new FileInputStream(dir), code);
		BufferedReader rd=new BufferedReader(isr);
		String string;
		while((string=rd.readLine())!=null){
			String s=string.split("\t")[1];
			ls.add(s);
		}
		rd.close();
		return ls;
   }
   
   public static int getLineNo(String dir,String code) throws Exception{
	   int i=0;
	   InputStreamReader isr= new InputStreamReader(new FileInputStream(dir), code);
		BufferedReader rd=new BufferedReader(isr);
		while((rd.readLine())!=null){
			i++;
		}
		rd.close();
		return i;
   }
   public static Long getLineNoLong(String dir,String code) throws Exception{
	   long i=0;
	   InputStreamReader isr= new InputStreamReader(new FileInputStream(dir), code);
		BufferedReader rd=new BufferedReader(isr);
		while((rd.readLine())!=null){
			i++;
		}
		rd.close();
		return i;
   }
   public static void DivideFile(String dir,String code,int a) throws Exception{
	   int count=getLineNo(dir, code);
	   System.out.println(count);
	   int b=count/a;
	   int i=0;
	   int j=1;
	   Writer w=null;
	   InputStreamReader isr= new InputStreamReader(new FileInputStream(dir), code);
		BufferedReader rd=new BufferedReader(isr);
		String s;
		while((s=rd.readLine())!=null){
			i++;
			if(w==null){
				w=new FileWriter(dir.substring(0,dir.lastIndexOf("."))+"_"+j+".txt");
			}
			if(i<j*b){
				w.write(s+"\r\n");
			}else if(i==j*b&&j<a){
				w.close();
				j++;
				w=new FileWriter(dir.substring(0,dir.lastIndexOf("."))+"_"+j+".txt");
			}
		}
		rd.close();
        if(w!=null){
        	w.close();
        }
   }






   public static void DivideFile(String dir,int[] arr) throws Exception{
	   List<String> ls=GetFileList(dir);
	   int st=0;
	   for(int i=0;i<arr.length;i++){
		   Writer w=new FileWriter(dir.substring(0,dir.lastIndexOf("."))+"_"+i+".txt");
		   int start=st;
		   int end=st+arr[i];
		   if(st>=ls.size()){
			   break;
		   }
		   if(end>=ls.size()){
			   end=ls.size();
		   }
		   for(int j=start;j<end;j++){
			   w.write(ls.get(j)+"\r\n");
		   }
		   w.close();
		   st=end;
	   }
	   if(st<ls.size()){
		   Writer w=new FileWriter(dir.substring(0,dir.lastIndexOf("."))+"_"+"rest"+".txt");
		   for(int j=st;j<ls.size();j++){
			   w.write(ls.get(j)+"\r\n");
		   }
		   w.close();
	   }
	  
   }
   public static long getFileLength(String dir) throws Exception{
	   InputStreamReader isr= new InputStreamReader(new FileInputStream(dir), "utf-8");
		BufferedReader rd=new BufferedReader(isr);
		long count=0;
		while((rd.readLine())!=null){
			count++;
		}
		rd.close();
		System.out.println(count);
		return count;
   }
   
   public static void DivideFile1(String dir,int a) throws Exception{
	   long count=getFileLength(dir);
		long b=count/a;
		for(int i=1;i<=a;i++){
			Writer w=new FileWriter(dir.substring(0,dir.lastIndexOf("."))+i+".txt");
			InputStreamReader isr= new InputStreamReader(new FileInputStream(dir), "utf-8");
			BufferedReader rd=new BufferedReader(isr);
			String line;
			long start=(i-1)*b;
			long c=0;
			while((line=rd.readLine())!=null){
				line=line.replaceAll("﻿", "");
				c++;
				if(i!=a){
					if(c>i*b){
						break;
					}
					if(c>start&&c<=i*b){
						w.write(line+"\r\n");
					}
				}else{
					if(c>start){
						w.write(line+"\r\n");
					}
				}
			}
			rd.close();
			w.close();
		}
		
   }
   public static Map<String, String> getMap(String dir,String code,int[] key,int[] value) {
	   Map<String, String> map=new HashMap<>();
		try {
			InputStreamReader isr= new InputStreamReader(new FileInputStream(dir), code);
			BufferedReader rd=new BufferedReader(isr);
			String s;
			while((s=rd.readLine())!=null){
				s=s.replaceAll("﻿", "");
				if(s.equals("")){
					continue;
				}
				String[] arr=s.split("\t");
				try {
					String mk="";
					for(int i:key){
						mk+="\t"+arr[i];
					}
					mk=mk.replaceFirst("\t", "");
					String mv="";
					for(int i:value){
						mv+="\t"+arr[i];
					}
					mv=mv.replaceFirst("\t", "");
					map.put(mk.trim(), mv);
				}catch (Exception e){
					e.printStackTrace();
					System.err.println(s);
					System.exit(0);
				}

			}
			rd.close();
		}catch (Exception e){
			e.printStackTrace();
		}
		return map;
   }


	public static Map<String, String> getMapLowercase(String dir,String code,int[] key,int[] value) throws Exception{
		Map<String, String> map=new HashMap<>();
		InputStreamReader isr= new InputStreamReader(new FileInputStream(dir), code);
		BufferedReader rd=new BufferedReader(isr);
		String s;
		while((s=rd.readLine())!=null){
			s=s.replaceAll("﻿", "");
			String[] arr=s.split("\t");
			String mk="";
			for(int i:key){
				mk+="\t"+arr[i];
			}
			mk=mk.replaceFirst("\t", "");
			String mv="";
			for(int i:value){
				mv+="\t"+arr[i];
			}
			mv=mv.replaceFirst("\t", "");
			map.put(mk.trim().toLowerCase(), mv);
		}
		rd.close();
		return map;
	}


   public static Set<String> getSet(String dir,String code,int[] key) throws Exception{
	   Set<String> set=new HashSet<>();
	   InputStreamReader isr= new InputStreamReader(new FileInputStream(dir), code);
		BufferedReader rd=new BufferedReader(isr);
		String s;
		while((s=rd.readLine())!=null){
			s=s.replaceAll("﻿", "");
			String[] arr=s.split("\t");
			String mk="";
			for(int i:key){
				if(arr.length<i+1){
					System.out.println(s);
				}
				mk+="\t"+arr[i];
			}
			mk=mk.replaceFirst("\t", "");
			set.add(mk);
		}
		rd.close();
		return set;
   }
   public static String removeCN(String s){
		String reg = "[\u4e00-\u9fa5]";
   	Pattern pat = Pattern.compile(reg);  
   	Matcher mat=pat.matcher(s); 
   	String repickStr = mat.replaceAll("");
   	return repickStr;
	}   
   
   public static void checkDBWd(String dir) throws Exception{
	   BufferedReader rd=new BufferedReader(new FileReader(dir));
		String s;
		long c=0;
		while((s=rd.readLine())!=null){
			s=s.replaceAll("﻿", "");
			String[] arr=s.split("\t");
			long start= IPDao.ipToLong(arr[1]);
			long end=IPDao.ipToLong(arr[3]);
			if(Long.parseLong(arr[0])!=start||Long.parseLong(arr[2])!=end){
				System.out.println(s+"--1");
			}
			if(end<start){
				System.out.println(s+"--2");
			}
			if(start!=c+1){
				System.out.println(s+"--3"); 
			}
			c=end;
		}
		rd.close();
   }
   
   public static List<String> getWindowsPath(File file,String code) throws Exception{
   	List<String> ls=new ArrayList<>();
   	if(!file.exists()){
   		return ls;
   	}
   	InputStreamReader isr= new InputStreamReader(new FileInputStream(file), code);
		BufferedReader rd=new BufferedReader(isr);
		String s;
		while((s=rd.readLine())!=null){
			s=s.replaceAll("﻿", "");
			if(s.trim().equals("")){
				continue;
			}
			String[] arr=s.split("[ \t]");
			List<String> ll=new ArrayList<>();
			for(int i=0;i<arr.length;i++){
				if(!arr[i].trim().equals("")){
					ll.add(arr[i].trim());
				}
			}
			if(!isNumeric(ll.get(0))){
				continue;
			}
			if(!s.contains("ms")&&!s.contains("毫秒")&&!s.contains("[")){
				ls.add("请求超时\t*\t*");
				continue;
			}
			int ii=9999;
	        for(int i=0;i<ll.size();i++){
	            if(ll.get(i).equals("ms")||ll.get(i).contains("毫秒")){
	                if(ll.get(i-1).contains("<1")){
	                    if(ii>1){
	                    	ii=1;
	                    }
	                }else{
	                	int j=Integer.parseInt(ll.get(i-1));
	                    if(j<ii){
	                    	ii=j;
	                    }
	                }
	                
	            }
	        }
	        if(ii==9999){
	        	System.out.println(s+"--"+"error");
	        	System.out.println(file.getPath());
	        }
	        if(s.contains("[")){
	        	for(int i=0;i<ll.size();i++){
		            if(ll.get(i).contains("[")){
		                String ip=ll.get(i);
		                ip=ip.substring(1, ip.length()-1);
		                String t="*";
		                if(ii!=9999){
		                	t=ii+"";
		                }
		                ls.add(ip+"\t"+t+"\t"+ll.get(i-1));
		            }
		        }
	        }else{
	        	for(int i=0;i<ll.size();i++){
		            if(IPDao.JudgeIP(ll.get(i))){
		            	String t="*";
		            	if(ii!=9999){
		                	t=ii+"";
		                }
		                ls.add(ll.get(i)+"\t"+t+"\t*");
		            }
		        }
	        }
	        
		}
		isr.close();
		rd.close();
		return ls;
   }
   public static List<String> getLinuxRoutePath(File file) throws Exception{
   	List<String> ls=new ArrayList<>();
   	List<String> ll=new ArrayList<>();
   	InputStreamReader isr= new InputStreamReader(new FileInputStream(file), "utf-8");
		BufferedReader rd=new BufferedReader(isr);
		String s;
		while((s=rd.readLine())!=null){
			s=s.replaceAll("﻿", "");
			if(!s.contains("msec")&&!s.contains("*")&&!s.contains("ms")){
	       		 continue;
	       	 }
	       	 ll.add(s);
		}
		isr.close();
		rd.close();
		List<String> ll2=new ArrayList<>();
        String re=ll.get(0);
        for(int i=1;i<ll.size();i++){
        	String line=ll.get(i);
        	 String[] arr2=line.trim().split("[() \t]");
        	if(isNumeric(arr2[0])){
        		ll2.add(re);
        		re=line;
        	}else{
        		re+=" "+line;
        	}
        	 if(i==ll.size()-1){
        		 ll2.add(re);
        	 }
        }
        for(int i=0;i<ll2.size();i++){
        	if(ll2.get(i).contains("ms")){
        		ls.add(getResult2(ll2.get(i)));
        	}else{
        		ls.add("请求超时\t*\t*");
        	}
        }
		return ls;
   }
   public static String getResult2(String line){
       String[] arr=line.split("[ \t]");
       List<String> ls=new ArrayList<String>();
       for(String st:arr){
           if(st.trim().equals("")){
               continue;
           }
           ls.add(st);
       }
       List<Double> li=new ArrayList<Double>();
       String ip="*";
       String domain="*";
       for(int i=0;i<ls.size();i++){
    	   if(ls.get(i).contains("(")){
    		   ip=ls.get(i);
    		   ip=ip.substring(ip.indexOf("(")+1,ip.indexOf(")")).trim();
    		   if(!ls.get(i-1).trim().equals(ip)){
    			   domain=ls.get(i-1).trim();
    		   }
    	   }
    	   if(IPDao.JudgeIP(ls.get(i))){
    		   ip=ls.get(i);
    	   }
           if(ls.get(i).trim().equals("ms")){
               li.add(Double.parseDouble(ls.get(i-1)));
           }
       }
       Collections.sort(li);
       if(li.size()>0){
    	   return ip+"\t"+li.get(0)+"\t"+domain;
       }else{
    	   return ip+"\t*\t"+domain;
       }
       
   }
   public static boolean isNumeric(String str){ 
	   Pattern pattern = Pattern.compile("[0-9]*"); 
	   Matcher isNum = pattern.matcher(str);
	   if( !isNum.matches() ){
	       return false; 
	   } 
	   return true; 
	}
   public static boolean deleteDirectory(String sPath) {
       //如果sPath不以文件分隔符结尾，自动添加文件分隔符
       if (!sPath.endsWith(File.separator)) {
           sPath = sPath + File.separator;
       }
       File dirFile = new File(sPath);
       //如果dir对应的文件不存在，或者不是一个目录，则退出
       if (!dirFile.exists() || !dirFile.isDirectory()) {
           return false;
       }
       boolean flag = true;
       //删除文件夹下的所有文件(包括子目录)
       File[] files = dirFile.listFiles();
       for (int i = 0; i < files.length; i++) {
           //删除子文件
           if (files[i].isFile()) {
               flag = files[i].delete();
               if (!flag) break;
           } //删除子目录
           else {
               flag = deleteDirectory(files[i].getAbsolutePath());
               if (!flag) break;
           }
       }
       if (!flag) return false;
       //删除当前目录
       if (dirFile.delete()) {
           return true;
       } else {
           return false;
       }
   }
   
   public static List<File> getMeasureFile(String dir,List<File> lf){
   	File f1=new File(dir);
   	if(f1.isDirectory()){
   		for(File f2:f1.listFiles()){
   			if(f2.isDirectory()){
   				lf=getMeasureFile(f2.getPath(), lf);
   			}else{
   				if(f2.getName().endsWith(".txt")){
       				String fn=f2.getName();
       				fn=fn.substring(0,fn.lastIndexOf("."));
       				if(!isContainsEn(fn)){
       					lf.add(f2);
       				}
       			}
   			}
   		}
   	}
   	return lf;
   }
   
   public static boolean isContainsEn(String s){
   	Pattern p = Pattern.compile("[a-zA-z]");
       if(p.matcher(s).find())
       {
           return true;
       }else{
           return false;
       }
   }
   
   
   public static Date getFileCreateDate(File file){
	   Path path= Paths.get(file.getPath());
       BasicFileAttributeView basicview= Files.getFileAttributeView(path, BasicFileAttributeView.class, LinkOption.NOFOLLOW_LINKS );
       BasicFileAttributes attr;
       Date createDate=null;
       try {
           attr = basicview.readAttributes();
           createDate = new Date(attr.creationTime().toMillis());
       } catch (Exception e) {
           e.printStackTrace();
       }
       return createDate;
   }
	public static void main(String[] args) throws Exception{

//		FileDao.DivideFile("H:\\数据测量\\全球端口服务测量\\20220408全球端口服务\\活跃测量\\world_active.txt", "utf-8",20);

		FileDao.DivideFile("H:\\数据测量\\工控设备探测\\其他单位提供\\活跃IP\\活跃IP.txt", "utf-8",5);
		
//		CombineFile(new File("H:/数据测量/Nmap探测banner/全球探测/全球IP2/需重测数据"), new File("H:/数据测量/Nmap探测banner/全球探测/全球IP2/需重测数据0919.txt"), "utf-8");
		
//		DivideFile1("H:/数据测量/全球IP测量/全球IPC2.txt", 10);
		

	}

}
