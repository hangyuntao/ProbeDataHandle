package org.lx.topo;

import org.lx.tools.ip.IPUtil;

import java.io.File;


public class LargeSetHandlePath extends LargeSetHandle<PathLong> {

    public LargeSetHandlePath(File tmpFolder) {
        super(tmpFolder);
    }

    @Override
    String getUnique(PathLong t) {
        String source = IPUtil.ipLong2Str(t.getSource());
        return source.substring(0, source.indexOf("."));
    }

    @Override
    String pojoToString(PathLong t) {
        return t.toString();
    }

}
