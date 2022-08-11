package org.lx.topo;

import org.lx.tools.ProcessPrint;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


public class TracertToPathToolProcess {

	private ProcessPrint processPrint = null;

	private AtomicInteger tracertCount = new AtomicInteger(0);

	private PathHandler pathHandler;
//	private int tracertCount = 0;

	public TracertToPathToolProcess(PathHandler pathHandler) {
		this.pathHandler = pathHandler;
	}

//	private void doExtract(File file) throws IOException {
//		if (file.getName().startsWith("tracertRe")) {
//			doExtractTracert(file);
//		}
//	}

	private void doExtract(File file) throws IOException {
		System.out.println(file.getAbsolutePath());
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line = null;
		String tracertIP = null;
		List<String> paths = null;
		while ((line = reader.readLine()) != null) {
			line = line.trim();
			if (line.contains("﻿")) {
				line = line.replaceAll("﻿", "");
			}
			if (line.isEmpty()) {
				continue;
			}
			if (line.startsWith("TracertIP")) {
//				berforeIP = null;
				int c = tracertCount.incrementAndGet();
				if (c % 10000 == 0) {
					processPrint.print(c);
				}
				if (paths != null) {
					pathHandler.handler(tracertIP, paths);
				}
				tracertIP = line.replace("TracertIP:", "");
				paths = new ArrayList<>();
				continue;
			}
			if (line.contains("*")) {
				paths.add("*");
				continue;
			}
			String ip = line.split("\t")[0].trim();
			paths.add(ip);
		}
		pathHandler.handler(tracertIP, paths);
		reader.close();
	}

	private void countTracertAll(File file) throws IOException {
		System.out.println("countTracertAll");
//		int sum = 0;
		final AtomicInteger sum = new AtomicInteger(0);
		tracertCount.set(0);
		List<File> fileList = new ArrayList<>();
		getAllFile(file, fileList);
		ExecutorService executorService = Executors.newFixedThreadPool(6);
		for (File f : fileList) {
			executorService.submit(new Runnable() {
				@Override
				public void run() {
					System.out.println(f.getAbsolutePath());
					try (BufferedReader reader = new BufferedReader(new FileReader(f))) {
						String line = null;
						while ((line = reader.readLine()) != null) {
							if (line.contains("TracertIP")) {
								sum.incrementAndGet();
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}

				}
			});
		}
		executorService.shutdown();
		while (!(executorService.isTerminated())) {
			try {
				executorService.awaitTermination(5, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println("countTracertAll finish");
		processPrint = new ProcessPrint(sum.get());
		processPrint.print(0);
	}

	public void extractFolder(File file) throws IOException {
		countTracertAll(file);
		List<File> fileList = new ArrayList<>();
		getAllFile(file, fileList);
		for (File f : fileList) {
			try {
				doExtract(f);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	private void getAllFile(File file, List<File> fileList) {
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			for (File f : files) {
				getAllFile(f, fileList);
			}
		} else {
			if (file.getName().startsWith("tracertRe")) {
				fileList.add(file);
			}
		}
	}

	private ExecutorService executorService;

	public void extractFolderThreads(File file, int threadNum) throws IOException {
		countTracertAll(file);
		executorService = Executors.newFixedThreadPool(threadNum);
		List<File> fileList = new ArrayList<>();
		getAllFile(file, fileList);
		for (File f : fileList) {
			executorService.submit(new Runnable() {
				@Override
				public void run() {
					try {
						doExtract(f);
					} catch (IOException e) {
						e.printStackTrace();
					}
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

}
