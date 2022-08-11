package org.lx.tools;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class LoadIp {

    public static Set<String> load(File file) throws IOException {
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

    public static Set<String> load(File file, int ipColIndex) throws IOException {
        Set<String> set = new HashSet<>();
        if (file.isFile() && file.canRead()) {
            FileLineTool.readLineWithTrim(file, new FileLineTool.LineHandle() {
                @Override
                public void handle(String line) {
                    String[] ss = line.split("\t");
                    String ip = ss[ipColIndex].trim();
                    set.add(ip);
                }
            });
        }
        return set;
    }

}
