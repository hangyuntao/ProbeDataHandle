package org.lx.topo;

public interface IPFilter {

	boolean filter(String ip);

	public static IPFilter emptyFilter() {
		return ip -> true;
	}
}
