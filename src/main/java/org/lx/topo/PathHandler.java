package org.lx.topo;

import java.io.IOException;
import java.util.List;

public interface PathHandler {
	void handler(String tracertIP, List<String> paths) throws IOException;
}