package org.lx.topo;

import org.lx.tools.FileLineTool;
import org.lx.tools.ip.IPUtil;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;


public class AnalysisTracert {
    private File tracertResultFolder;
    private File allIPOutputFile;
    private File allRouterOutputFile;
    private File allPathOutputFile;
    private IPFilter ipFilter;

    private File tmpIPFolder;
    private File tmpRouterFolder;
    private File tmpPathFolder;
    private int threads = 6;

    //	private Set<Integer> ipSet = Collections.newSetFromMap(new ConcurrentHashMap<>());
//	private Set<Integer> routerSet = Collections.newSetFromMap(new ConcurrentHashMap<>());
//	private Set<PathLong> pathSet = Collections.newSetFromMap(new ConcurrentHashMap<>());
//	private AtomicLong ipSetSize = new AtomicLong(0);
//	private AtomicLong routerSetSize = new AtomicLong(0);
//	private AtomicLong pathSetSize = new AtomicLong(0);
    private LargeSetHandleIPLong ipSetHandle;
    private LargeSetHandleIPLong routerSetHandle;
    private LargeSetHandlePath pathSetHandle;

    public AnalysisTracert(File tracertResultFolder, File tmpFolder, File allIPOutputFile, File allRouterOutputFile,
                           File allPathOutputFile, IPFilter ipFilter, int threads) {
        this.tracertResultFolder = tracertResultFolder;
        this.allIPOutputFile = allIPOutputFile;
        this.allRouterOutputFile = allRouterOutputFile;
        this.allPathOutputFile = allPathOutputFile;
        this.ipFilter = ipFilter;
        if (!tmpFolder.exists()) {
            tmpFolder.mkdirs();
        }
        tmpIPFolder = new File(tmpFolder, "ip");
        if (!tmpIPFolder.exists()) {
            tmpIPFolder.mkdirs();
        }
        tmpRouterFolder = new File(tmpFolder, "router");
        if (!tmpRouterFolder.exists()) {
            tmpRouterFolder.mkdirs();
        }
        tmpPathFolder = new File(tmpFolder, "path");
        if (!tmpPathFolder.exists()) {
            tmpPathFolder.mkdirs();
        }
        ipSetHandle = new LargeSetHandleIPLong(tmpIPFolder);
        routerSetHandle = new LargeSetHandleIPLong(tmpRouterFolder);
        pathSetHandle = new LargeSetHandlePath(tmpPathFolder);
        this.threads = threads;
    }

    private void mergeIP(long ip) throws IOException {
//		System.out.println(ip);
        ipSetHandle.push(ip);
    }

    private void mergeRouter(long router) throws IOException {
        routerSetHandle.push(router);
    }

    private synchronized void mergePath(long source, long target) throws IOException {
        if (source > target) {
            pathSetHandle.push(new PathLong(target, source));
        } else if (source < target) {
            pathSetHandle.push(new PathLong(source, target));
        }
//		long c = pathSetSize.incrementAndGet();
//		if (c > 10000000) {
//			synchronized (pathSet) {
//				try {
//					flushPath();
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//				pathSet.clear();
//				pathSetSize.set(0);
//			}
//		}
    }

    private void flushIP() throws IOException {
        ipSetHandle.mergeToFile(allIPOutputFile);
    }

    private void flushRouter() throws IOException {
        routerSetHandle.mergeToFile(allRouterOutputFile);
    }

    private void flushPath() throws IOException {
        pathSetHandle.mergeToFile(allPathOutputFile);
    }

    private void extractTracert() throws IOException {
//		BufferedWriter writer = new BufferedWriter(new FileWriter("F:\\Analysis\\科大数据\\纽约\\活跃测量目标.txt"));
        Set<String> set = Collections.newSetFromMap(new ConcurrentHashMap<>());

        TracertToPathToolProcess tracertToPathToolProcess = new TracertToPathToolProcess(new PathHandler() {

            @Override
            public void handler(String tracertIP, List<String> paths) throws IOException {
                if (paths.isEmpty()) {
                    return;
                }
                int size = paths.size();
                for (int i = 0; i < size - 1; i++) {
                    String source = paths.get(i);
                    String target = paths.get(i + 1);

                    boolean isIpSource = IPUtil.judgeIP(source) && ipFilter.filter(source);
                    boolean isIpTarget = IPUtil.judgeIP(target) && ipFilter.filter(target);
                    if (isIpSource) {
                        long sourceLong = IPUtil.ipStr2Long(source);
//						System.out.println(source);
                        if (isIpTarget) {
                            long targetLong = IPUtil.ipStr2Long(target);
                            mergeRouter(sourceLong);
                            mergeIP(sourceLong);
                            mergeIP(targetLong);
                            mergePath(sourceLong, targetLong);
//							System.out.println(target);
                            if (i == (size - 1) && !target.equals(tracertIP)) {
                                mergeRouter(targetLong);
                            }
                            if (target.equals(tracertIP)) {
                                set.add(tracertIP);
                            }
                        }
                    }
                }
//				if(tracertIP.equals(paths.get(paths.size()-1))&&ipFilter.filter(tracertIP)) {
//					set.add(tracertIP);
//					mergeIP(IPUtil.ipStr2Long(tracertIP));
////					if(ipFilter.filter(tracertIP)) {
////					}
//				}
            }
        });
        if (threads <= 1) {
            tracertToPathToolProcess.extractFolder(tracertResultFolder);
        } else {
            tracertToPathToolProcess.extractFolderThreads(tracertResultFolder, 6);
        }
//		FileLineTool.writerToFile(set, new File("F:\\Analysis\\科大数据\\纽约\\活跃测量目标.txt"));
//		writer.close();
    }

    public void extract() throws IOException {
        extractTracert();
        flushIP();
        flushRouter();
        flushPath();
    }

    public void extractWithOldResult(List<File> oldPathFiles, List<File> oldRouterFiles) throws IOException {
        extractTracert();
        System.out.println("merge old Path");
        AtomicLong count = new AtomicLong(0);
        for (File file : oldPathFiles) {
            System.out.println(file.getAbsolutePath());
            FileLineTool.readLineWithTrim(file, new FileLineTool.LineHandle() {

                @Override
                public void handle(String line) throws IOException {
                    String[] ss = line.split("\t");
                    String source = ss[0];
                    String target = ss[1];
                    long c = count.incrementAndGet();
                    if (c % 5000000 == 0) {
                        System.out.println(c);
                    }
                    if (ipFilter.filter(source) && ipFilter.filter(target)) {
                        mergePath(IPUtil.ipStr2Long(source), IPUtil.ipStr2Long(target));
                    }
                }
            });
        }

        count.set(0);
        System.out.println("Merge old router");
        for (File file : oldRouterFiles) {
            FileLineTool.readLineWithTrim(file, new FileLineTool.LineHandle() {

                @Override
                public void handle(String line) throws IOException {
                    long c = count.incrementAndGet();
                    if (c % 5000000 == 0) {
                        System.out.println(c);
                    }
                    mergeRouter(IPUtil.ipStr2Long(line));
                }
            });
        }
        flushIP();
        flushRouter();
        flushPath();

    }

}
