package org.lx.topo;

import org.lx.tools.ip.IPUtil;

import java.io.File;


public class LargeSetHandleIPLong extends LargeSetHandle<Long> {

	public LargeSetHandleIPLong(File tmpFolder) {
		super(tmpFolder);
	}

	@Override
	String getUnique(Long t) {
		String ip = IPUtil.ipLong2Str(t);
		return ip.substring(0, ip.indexOf("."));
	}

	@Override
	String pojoToString(Long t) {
		return IPUtil.ipLong2Str(t);
	}

}
