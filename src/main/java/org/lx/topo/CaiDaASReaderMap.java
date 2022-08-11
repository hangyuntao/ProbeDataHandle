package org.lx.topo;

import org.lx.tools.NormalTool;
import org.lx.tools.ip.IPUtil;

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class CaiDaASReaderMap {

    static int max = 0;
    static File file = new File("D:\\dataCollect\\routeviews-rv2-20210220-1200.pfx2as");

    public static Map<String, String> prefixMapInfo = new ConcurrentHashMap<>();

    static void readCaiDaData() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] ss = line.split("(" + (char) 32 + "|" + (char) 9 + ")+");
                if (ss.length < 3) {
                    continue;
                }
                String sIPString = ss[0];
                String sLongString = ss[1];
                if (max < Integer.valueOf(ss[1])) {
                    max = Integer.valueOf(ss[1]);
                }
                String binaryString = changeIP2Binary(sIPString);
                String prefixString = binaryString.substring(0, Integer.valueOf(sLongString));
                if (prefixMapInfo.containsKey(prefixString)) {
                    System.out.println(prefixString + "\t" + ss[2] + "\t" + prefixMapInfo.get(prefixString));
                }
                prefixMapInfo.put(prefixString, ss[2]);
            }
            reader.close();
            System.out.println("max prefix:\t" + max);
            System.out.println("Load Map file Done!");
        } catch (IOException e) {
            e.printStackTrace();
            // return false;
        }

    }

    static {
        readCaiDaData();
    }

    public List<String> GetASPreByIP(String sIP) {
        String binaryString = changeIP2Binary(sIP);
        List<String> ls = new ArrayList<String>();
        for (int i = max; i > 0; i--) {
            String key = binaryString.substring(0, i);
            String result = (String) prefixMapInfo.get(key);
            if (result != null) {
                String[] arr = result.split("\t");
                String pre = arr[0];
                for (String as : arr[1].split("_|,")) {
                    ls.add(as + " " + pre);
                }
            }
        }
        return ls;
    }

    public static String changeIP2Binary(long ip) {
        String prefixString = "000000000000000000000000";
        String binaryString = Long.toBinaryString(ip);
        if (binaryString.length() < 32) {
            try {
                binaryString = prefixString.substring(0, 32 - binaryString.length()) + binaryString;
            } catch (Exception e) {
                System.out.println(binaryString.length());
            }
        }
        return binaryString;
    }

    public static String changeIP2Binary(String sIP) {
        String prefixString = "000000000000000000000000";
        String binaryString = Long.toBinaryString(IPUtil.ipStr2Long(sIP));
        if (binaryString.length() < 32) {
            try {
                binaryString = prefixString.substring(0, 32 - binaryString.length()) + binaryString;
            } catch (Exception e) {
                System.out.println(binaryString.length());
            }
        }
        return binaryString;
    }

    public static List<String> searchAS(long ip) {
        String binaryString = changeIP2Binary(ip);
        List<String> ls = new ArrayList<String>();
        for (int i = max; i > 0; i--) {
            String key = binaryString.substring(0, i);
            String result = prefixMapInfo.get(key);
            if (result != null) {
                String[] arr = result.split("_|,");
                for (String as : arr) {
                    if (!ls.contains(as)) {
                        try {
                            long asL = Long.parseLong(as);
                            if (asL <= 64511 || asL > 65535) {
                                ls.add(as);
                            }

                        } catch (Exception e) {
                        }

                    }
                }
//				String pre = arr[0];
//				for (String as : arr[1].split("_|,")) {
//					ls.add(as + " " + pre);
//				}
            }
        }
        return ls;

//		List<AsBase> asList = threadLocal.get();
//		if (asList == null) {
//			asList = readCaiDaData();
//			threadLocal.set(asList);
//		}
//		List<String> list = new ArrayList<>();
//		for (AsBase base : asList) {
//			if (base.getStart() <= ip && base.getEnd() >= ip) {
////				if (!list.contains(base.getAs())) {
//				list.add(base.getAs());
////				}
//			}
//		}
//		return list;
    }

    public static List<String> searchAS(String ip) {
        return searchAS(IPUtil.ipStr2Long(ip));
    }

    public static String searchASFormate(long ip) {
        List<String> list = searchAS(ip);
        if (list.isEmpty()) {
            return null;
        }
        list.sort(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.compareTo(o2);
            }
        });

        return NormalTool.arrayToString(list, ",");

    }

    public static String searchASFormate(String ip) {
        return searchASFormate(IPUtil.ipStr2Long(ip));
    }

    public static void main(String[] args) throws IOException {
        long now = System.currentTimeMillis();
        System.out.println(searchASFormate("210.2.145.142"));
//		for (int i = 0; i < 10000; i++) {
//			searchASFormate("14.102.127.169");
//		}
        System.out.println(System.currentTimeMillis() - now);
//		System.out.println(NormalTool.arrayToString(IPUtil.calucateMaskRange("92.246.148.0", 22), "\t"));
    }

}
