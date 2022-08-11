package org.lx.pojo;

import com.alibaba.fastjson.annotation.JSONType;
import lombok.Data;
import org.lx.tools.ip.IPLocation;

import java.util.List;

/**
 * @author yuntao
 * @date 2022/7/26
 */
@Data
@JSONType(orders={"ip","hostName", "company", "tags", "os", "portInfos"})
public class IpInfo {
    // Ip地址
    private String ip;
    // as信息
    private ASInfo asInfo;
    // 端口信息
    private List<PortInfo> portInfos;
    // 定位
    private IPLocation location;
    // 使用的公司
    private String company;
    // 设备分类
    private List<String> tags;
    //设备使用的操作系统
    private String os;
    //cve漏洞
    private List<Vul> vuls;
    // 域名
    private String hostName = "*";
}
