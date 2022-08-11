package org.lx.topo;


import org.lx.tools.ip.IPUtil;

public class PathLong {
	private long source;
	private long target;

	public PathLong(long source, long target) {
		this.source = source;
		this.target = target;
	}

	public long getSource() {
		return source;
	}

	public void setSource(long source) {
		this.source = source;
	}

	public long getTarget() {
		return target;
	}

	public void setTarget(long target) {
		this.target = target;
	}

	@Override
	public int hashCode() {
		return Long.hashCode(source) + Long.hashCode(target);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof PathLong) {
			PathLong pathLong = (PathLong) obj;
			if (this.source == pathLong.source && this.target == pathLong.target) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String toString() {
		return IPUtil.ipLong2Str(source) + "\t" + IPUtil.ipLong2Str(target);
	}

}