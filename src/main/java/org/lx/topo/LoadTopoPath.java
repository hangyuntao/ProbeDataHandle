package org.lx.topo;

import org.lx.tools.FileLineTool;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class LoadTopoPath {

    public static List<String> loadPath(File file) throws IOException {
        List<String> list = new ArrayList<>();

        FileLineTool.readLineWithTrim(file, new FileLineTool.LineHandle() {
            @Override
            public void handle(String line) {
                list.add(line);
            }
        });

        return list;
    }

    public static List<PathString> loadPathPojo(File file) throws IOException {
        List<PathString> list = new ArrayList<>();

        FileLineTool.readLineWithTrim(file, new FileLineTool.LineHandle() {
            @Override
            public void handle(String line) {
                String[] ss = line.split("\t");
                list.add(new PathString(ss[0], ss[1]));
            }
        });

        return list;
    }

}
