package org.lx.tools;

import java.io.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class FileLineTool {

	public static void readLine(File file, LineHandle lineHandle) throws IOException {
		if (file.isFile() && file.canRead()) {
			try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
				String line = null;
				while ((line = reader.readLine()) != null) {
					lineHandle.handle(line);
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		} else {
			throw new IOException("file can not read");
		}

	}

	public static void readLineWithTrim(File file, LineHandle lineHandle) throws IOException {
		readLine(file, new LineHandle() {
			@Override
			public void handle(String line) throws Exception {
				line = line.trim();
				if (!line.isEmpty()) {
					lineHandle.handle(line);
				}
			}

		});
	}

	public static void readLineWithThreads(File file, LineHandle lineHandle, int threadNum) throws IOException {
		ExecutorService executorService = Executors.newFixedThreadPool(threadNum);
		if (file.isFile() && file.canRead()) {
			try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
				for (int i = 0; i < threadNum; i++) {
					executorService.submit(() -> {
						String line = null;
						try {
							while ((line = reader.readLine()) != null) {
								lineHandle.handle(line);
							}
						} catch (IOException e) {
							e.printStackTrace();
						} catch (Exception e) {
							throw new RuntimeException(e);
						}
					});
				}
				executorService.shutdown();
				while (!executorService.isTerminated()) {
					try {
						executorService.awaitTermination(5, TimeUnit.SECONDS);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		} else {
			throw new IOException("file can not read");
		}
	}

	public static void readLineWithTrimThreads(File file, LineHandle lineHandle, int threadNum) throws IOException {
		readLineWithThreads(file, new LineHandle() {
			@Override
			public void handle(String line) throws Exception {
				line = line.trim();
				if (!line.isEmpty()) {
					lineHandle.handle(line);
				}
			}

		}, threadNum);
	}

	public static void writerToFile(Collection<String> collection, File file) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(file));
		for (String s : collection) {
			writer.write(s + "\n");
//			writer.newLine();
		}
		writer.close();
	}

	public static Set<String> readFileToSet(File file) throws IOException {
		Set<String> set = new HashSet<>();
		readLineWithTrim(file, new LineHandle() {

			@Override
			public void handle(String line) throws IOException {
				set.add(line);
			}
		});
		return set;
	}

	public static interface LineHandle {
		public void handle(String line) throws Exception;
	}
}
