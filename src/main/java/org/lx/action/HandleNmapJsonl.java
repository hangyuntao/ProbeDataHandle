package org.lx.action;

import cn.hutool.core.io.FileUtil;
import com.alibaba.fastjson.JSON;
import org.lx.pojo.NmapInfo;
import org.lx.pojo.PortInfo;
import org.lx.tools.FileLineTool;
import org.lx.tools.NormalTool;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HandleNmapJsonl {


    public static void main(String[] args) throws IOException {

        Set<String> openPortSet = new HashSet<>();

        OutputStreamWriter fileWriter = new OutputStreamWriter(new FileOutputStream(new File("F:\\Analysis\\福建\\特定端口扫描\\端口开放情况.txt")), StandardCharsets.UTF_8);
        FileLineTool.readLineWithTrim(new File("F:\\Analysis\\福建\\特定端口扫描\\nmapRes.txt"), new FileLineTool.LineHandle() {
            @Override
            public void handle(String line) throws IOException {
                NmapInfo nmapInfo = JSON.parseObject(line, NmapInfo.class);
                List<PortInfo> portInfos = nmapInfo.getPortInfos();
                for (PortInfo portInfo : portInfos) {
                    if ("open".equals(portInfo.getState())) {
//                        portInfo.setType();
                        String product = portInfo.getType() == null ? "" : portInfo.getType();
                        if ("tcpwrapped".equals(product)) {
                            continue;

                        }
                        String version = (portInfo.getVersion() == null || "*".equals(portInfo.getVersion())) ? "" : portInfo.getVersion();
                        fileWriter.write(nmapInfo.getIp() + "\t" + portInfo.getPort() + "\t" + portInfo.getProtocal() + "\t" + product + "\t" + version + "\n");
                        openPortSet.add(portInfo.getPort());
                    }

                }

            }
        });
        fileWriter.close();
        FileUtil.writeString(NormalTool.arrayToString(openPortSet, ","), new File("F:\\Analysis\\福建\\特定端口扫描\\openPort.txt"), StandardCharsets.UTF_8);

    }
}
