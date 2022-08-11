package org.lx.topo.action.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CVE {
    String numbering;
    String describe;
    String level;
}
