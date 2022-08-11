package org.lx.topo;

import org.lx.tools.FileLineTool;
import org.lx.tools.ip.IPIPLocation;
import org.lx.tools.ip.IPUtil;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LoadRouter {

    public static Set<String> loadRouter(File file) throws IOException {
        Set<String> set = new HashSet<>();
        if (file.isFile() && file.canRead()) {
            FileLineTool.readLineWithTrim(file, new FileLineTool.LineHandle() {

                @Override
                public void handle(String line) {
                    set.add(line);
                }
            });
        }
        return set;
    }

    public static Set<String> loadRouter(List<File> routerFiles, List<File> tracertFolders) throws IOException {
        Set<String> set = new HashSet<>();
        for (File routerFile : routerFiles) {
            FileLineTool.readLineWithTrim(routerFile, new FileLineTool.LineHandle() {

                @Override
                public void handle(String line) {
                    set.add(line);
                }
            });
        }

        TracertToPathToolProcess tracertToPathToolProcess = new TracertToPathToolProcess(new PathHandler() {

            @Override
            public void handler(String tracertIP, List<String> paths) {
                if (paths.isEmpty()) {
                    return;
                }
                int size = paths.size();
                for (int i = 0; i < size - 1; i++) {
                    String source = paths.get(i);
                    String target = paths.get(i + 1);

                    boolean isIpSource = IPUtil.judgeIP(source) && IPIPLocation.isUsedIP(source);
                    boolean isIpTarget = IPUtil.judgeIP(target) && IPIPLocation.isUsedIP(target);
                    if (isIpSource) {
                        set.add(source);
                        if (isIpTarget) {
                            if (i == (size - 1) && !target.equals(tracertIP)) {
                                set.add(target);
                            }
                        }
                    }
                }
            }
        });
        for (File file : tracertFolders) {
            tracertToPathToolProcess.extractFolder(file);
        }

        return set;
    }
}
