package org.lx.pojo;

import lombok.Data;

/**
 * CVE漏洞
 * @author yuntao
 * @date 2022/7/26
 */
@Data
public class Vul {
    // 编号
    private String num;
    // 描述
    private String description = "*";
    // 分类
    private String classification = "*";
    // 类型
    private String type = "*";
    // 等级
    private String level = "*";
    // 分数
    private int point;
}
