package org.lx.tools;

import java.util.concurrent.atomic.AtomicLong;

public class ProcessPrint {

	private long total;

	private long start;
	
	private AtomicLong now = new AtomicLong();

	public ProcessPrint(long total) {
		this.total = total;
		this.start = System.currentTimeMillis();
	}

	public long getTotal() {
		return total;
	}

	public void setTotal(long total) {
		this.total = total;
	}

	public void reserTime() {
		this.start = System.currentTimeMillis();
	}
	
	public void increment() {
		now.incrementAndGet();
	}
	public void incrementAndPrint() {
		now.incrementAndGet();
		print();
	}
	
	public void print() {
		long count = now.get();
		if (count == 0) {
			return;
		}
		long now = System.currentTimeMillis() - start;
		System.out.println("已处理: " + count + "，共: " + total + "。已运行: " + formatSecond(now / 1000) + "。剩余: "
				+ formatSecond((long) (((now / (count / (float) total)) - now) / 1000)));
	}

	public void print(long count) {
		if (count == 0) {
			return;
		}
		long now = System.currentTimeMillis() - start;
		System.out.println("已处理: " + count + "，共: " + total + "。已运行: " + formatSecond(now / 1000) + "。剩余: "
				+ formatSecond((long) (((now / (count / (float) total)) - now) / 1000)));
	}

	public void printEn(long count) {
		if (count == 0) {
			return;
		}
		long now = System.currentTimeMillis() - start;
		System.out.println("handle: " + count + ", total: " + total + ". time: " + formatSecondEn(now / 1000)
				+ ". Surplus: " + formatSecondEn((long) (((now / (count / (float) total)) - now) / 1000)));
	}

	public String formatSecond(long second) {
		return second / 3600 + "小时" + (second % 3600 / 60) + "分钟" + (second % 60) + "秒";
	}

	public String formatSecondEn(long second) {
		return second / 3600 + "H" + (second % 3600 / 60) + "M" + (second % 60) + "S";
	}
}
