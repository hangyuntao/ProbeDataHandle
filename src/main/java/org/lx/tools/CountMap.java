package org.lx.tools;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用于统计某类变量出现的次数
 * 
 * @author 96131
 *
 * @param <T>
 */
public class CountMap<T> {

	private Map<T, Integer> map = new ConcurrentHashMap<>();

	public synchronized void add(T t) {
		Integer num = map.get(t);
		if (num == null) {
			map.put(t, 1);
		} else {
			map.put(t, num + 1);
		}
	}

	public int get(T t) {
		Integer num = map.get(t);
		return num == null ? 0 : num;
	}

	public void clearAll() {
		map.clear();
	}

	public void clear(T t) {
		map.remove(t);
	}

	public void each(Handler<T> handler) {
		for (Map.Entry<T, Integer> entry : map.entrySet()) {
			handler.handle(entry.getKey(), entry.getValue());
		}
	}

	@Override
	public String toString() {

		if (map.isEmpty()) {
			return "empty CountMap";
		}
		StringBuilder builder = new StringBuilder();
		for (Map.Entry<T, Integer> entry : map.entrySet()) {
			builder.append(entry.getKey().toString()).append("\t").append(entry.getValue()).append("\n");
		}
		return builder.toString();
	}

	public String toStringWithSort(boolean reverse) {

		if (map.isEmpty()) {
			return "empty CountMap";
		}
		List<NameValue> list = getNameValuesSort(reverse);
		StringBuilder builder = new StringBuilder();
		for (NameValue nameValue : list) {
			builder.append(nameValue.getName()).append("\t").append(nameValue.getValue()).append("\n");
		}
		return builder.toString();
	}

	public String toStringWithSortByName(boolean reverse) {

		if (map.isEmpty()) {
			return "empty CountMap";
		}
		List<NameValue> list = getNameValuesWithNameSort(reverse);
		StringBuilder builder = new StringBuilder();
		for (NameValue nameValue : list) {
			builder.append(nameValue.getName()).append("\t").append(nameValue.getValue()).append("\n");
		}
		return builder.toString();
	}

	public List<NameValue> getNameValuesSort(boolean reverse) {
		if (map.isEmpty()) {
			return new ArrayList<>();
		}
		List<NameValue> list = new ArrayList<NameValue>();
		for (Map.Entry<T, Integer> entry : map.entrySet()) {
			list.add(new NameValue(entry.getKey().toString(), entry.getValue()));
		}
		list.sort(new Comparator<NameValue>() {
			@Override
			public int compare(NameValue o1, NameValue o2) {
				if (reverse) {
					return Integer.compare(o2.getValue(), o1.getValue());
				} else {
					return Integer.compare(o1.getValue(), o2.getValue());
				}

			}
		});
		return list;
	}

	public List<NameValue> getNameValuesWithNameSort(boolean reverse) {
		if (map.isEmpty()) {
			return new ArrayList<>();
		}
		List<NameValue> list = new ArrayList<NameValue>();
		for (Map.Entry<T, Integer> entry : map.entrySet()) {
			list.add(new NameValue(entry.getKey().toString(), entry.getValue()));
		}
		list.sort(new Comparator<NameValue>() {
			@Override
			public int compare(NameValue o1, NameValue o2) {
				if (reverse) {

					return o2.getName().compareTo(o1.getName());
				} else {
					return o1.getName().compareTo(o2.getName());
				}

			}
		});
		return list;
	}

	public List<NameValue> sort() {
		if (map.isEmpty()) {
			return new ArrayList<>();
		}
		List<NameValue> list = new ArrayList<NameValue>();
		for (Map.Entry<T, Integer> entry : map.entrySet()) {
			list.add(new NameValue(entry.getKey().toString(), entry.getValue()));
		}
		list.sort(new Comparator<NameValue>() {
			@Override
			public int compare(NameValue o1, NameValue o2) {
				return Integer.compare(o2.getValue(), o1.getValue());
			}
		});
		return list;
	}

	public static interface Handler<T> {
		void handle(T t, Integer num);
	}

	public Map<T, Integer> getMap() {
		return map;
	}

	public static class NameValue {
		private String name;
		private int value;

		public NameValue(String name, int value) {
			this.name = name;
			this.value = value;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public int getValue() {
			return value;
		}

		public void setValue(int value) {
			this.value = value;
		}

	}

}
