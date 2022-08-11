package org.lx.topo;

import org.lx.tools.FileLineTool;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;


public abstract class LargeSetHandle<T> {
	private AtomicLong count = new AtomicLong(0);
	private Set<T> set = Collections.newSetFromMap(new ConcurrentHashMap<>());
	private ConcurrentHashMap<String, FileWriter> writerMap = new ConcurrentHashMap<>();

	private long maxCount = 10000000;
	private File tmpFolder;

	public LargeSetHandle(File tmpFolder) {
		this.tmpFolder = tmpFolder;
	}

	public LargeSetHandle(File tmpFolder, long maxCount) {
		this.tmpFolder = tmpFolder;
		this.maxCount = maxCount;
	}

	public void push(T t) throws IOException {
		set.add(t);
		long total = count.incrementAndGet();
		if (total > maxCount) {
			flush();
		}
	}

	private void flush() throws IOException {
		synchronized (set) {
			for (T t : set) {
				writerFile(t);
			}
			set.clear();
			count.set(0);
		}
	}

	private void writerFile(T t) throws IOException {
		String unique = getUnique(t);
		FileWriter writer = writerMap.get(unique);
		if (writer == null) {
			writer = new FileWriter(new File(tmpFolder, unique + ".txt"));
			writerMap.put(unique, writer);
		}
		writer.write(pojoToString(t) + "\n");
	}

	public void mergeToFile(File saveFile) throws IOException {
		if (set.size() > 0) {
			flush();
		}
		for (String unique : writerMap.keySet()) {
			writerMap.get(unique).close();
		}
		BufferedWriter writer = new BufferedWriter(new FileWriter(saveFile));

		for (File file : tmpFolder.listFiles()) {
			Set<String> set = new HashSet<>();
			System.out.println("start " + file.getAbsolutePath());
			FileLineTool.readLineWithTrim(file, new FileLineTool.LineHandle() {
				@Override
				public void handle(String line) {
					set.add(line);
				}
			});
			for (String s : set) {
				writer.write(s + "\n");
			}
			set.clear();
			System.out.println("finish " + file.getAbsolutePath());
		}
		writer.close();
	}

	abstract String getUnique(T t);

	abstract String pojoToString(T t);
}
