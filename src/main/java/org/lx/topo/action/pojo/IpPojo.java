package org.lx.topo.action.pojo;

import com.alibaba.fastjson.annotation.JSONType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.lx.pojo.PortInfo;

import java.util.ArrayList;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JSONType(orders={"companyName","ip","domainName","cves"})
public class IpPojo {
    String ip;
    String companyName;
    String domainName;
    String operatingSystem;
    ArrayList<PortInfo> porats;
    ArrayList<CVE> cves;
    String tag;
}
