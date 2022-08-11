package org.lx.LA;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.lx.tools.ip.GetIPLocation;
import org.lx.tools.ip.IPLocation;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashSet;
import java.util.Set;

/**
 * @author yuntao
 * @date 2022/8/9
 */
public class RouterHandle {
    public static void main(String[] args) throws Exception {
        //getAllRouter("D:\\Desktop\\洛杉矶\\节点分析\\router_all.txt", "洛杉矶");
        matchPortInfoFile();
    }
    
    public static void getAllRouter(String routerAll, String keyword) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(routerAll));
        String line;
        FileWriter writer = new FileWriter("D:\\Desktop\\洛杉矶\\节点分析\\洛杉矶-全部路由器.txt");
    
        while((line = br.readLine()) != null) {
            IPLocation loc = GetIPLocation.get(line);
            if (loc.getCity().contains(keyword)) {
                writer.write(line + "\r\n");
            }
        }
        
        writer.close();
    }
    
    public static void matchPortInfoFile() throws Exception {
        BufferedReader br = new BufferedReader(new FileReader("D:\\Desktop\\洛杉矶\\节点分析\\洛杉矶-全部路由器.txt"));
        String line;
        // D:\Desktop\洛杉矶\节点分析\端口服务测量数据-修正后.txt
        Set<String> set = new HashSet<>();
        while((line = br.readLine()) != null) {
            set.add(line);
        }
        
        br = new BufferedReader(new FileReader("D:\\Desktop\\洛杉矶\\节点分析\\端口服务测量数据-修正后.txt"));
        FileWriter writer = new FileWriter("D:\\Desktop\\洛杉矶\\节点分析\\路由器端口服务测量数据.txt");
        while((line = br.readLine()) != null) {
            JSONObject json = JSON.parseObject(line);
            if (set.contains(json.getString("ip"))) {
                writer.write(line + "\r\n");
            }
        }
        writer.close();
    }
    
    
    
}
