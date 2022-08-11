package org.lx.topo.action.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Port {
    //端口号--22
    public String port;
    //服务类别--ssh
    public String type;
    //协议类别 TCP/UDP
    public String protocal;
    //端口状态-open
    public String state = "open";
    //服务版本--openSSH 7.4
    public String version;
}
