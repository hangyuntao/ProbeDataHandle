package org.lx.tools;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

public class NormalTool {

	private static String[] chars = new String[] { "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n",
			"o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "0", "1", "2", "3", "4", "5", "6", "7", "8",
			"9", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T",
			"U", "V", "W", "X", "Y", "Z" };

	public static String getUUID8() {
		StringBuilder stringBuilder = new StringBuilder();
		String uuid = UUID.randomUUID().toString().replace("-", "");
		for (int i = 0; i < 8; i++) {
			String str = uuid.substring(i * 4, i * 4 + 4);
			int x = Integer.parseInt(str, 16);
			stringBuilder.append(chars[x % 0x3E]);
		}
		return stringBuilder.toString();
	}

	public static String getTimeStamp() {
		return System.currentTimeMillis() + "";
	}

	public static String getUUID32() {
		return UUID.randomUUID().toString().replaceAll("-", "");
	}

	public static boolean checkUUID32(String uuid) {
		if (uuid == null || uuid.length() != 32) {
			return false;
		}
		return Pattern.matches("[a-z0-9]{32}", uuid);
	}

	public static String getFormatDateString() {
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
	}

	public static String formatDateString(Date date) {
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
	}

	public static Date getDateFromStr(String str) {
		Date date = null;
		try {
			date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(str);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return date;
	}

	public static String arrayToString(Collection<String> strings, String placeholder) {
		Iterator<String> iterable = strings.iterator();
		StringBuilder builder = new StringBuilder();
		int i = 0;
		while (iterable.hasNext()) {
			String s = iterable.next();
			if (i != 0) {
				builder.append(placeholder);
			}
			builder.append(s);
			i++;
		}
		return builder.toString();
	}

	public static String arrayToString(String[] strings, String placeholder) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < strings.length; i++) {
			if (i != 0) {
				builder.append(placeholder);
			}
			builder.append(strings[i]);
		}
		return builder.toString();
	}

	public static String streamToString(InputStream stream, boolean close, String charset) throws IOException {
		if (stream == null) {
			return null;
		}
		String msg = null;
		ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
		byte[] buf = new byte[512];
		int i = -1;
		try {
			while ((i = stream.read(buf)) != -1) {
				arrayOutputStream.write(buf, 0, i);
			}
		} finally {
			if (close) {
				try {
					stream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		try {
			msg = new String(arrayOutputStream.toByteArray(), charset);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		try {
			arrayOutputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return msg;
	}

	public static byte[] streamToBytes(InputStream stream, boolean close) throws IOException {
		if (stream == null) {
			return null;
		}
		ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
		byte[] buf = new byte[512];
		int i = -1;
		try {
			while ((i = stream.read(buf)) != -1) {
				arrayOutputStream.write(buf, 0, i);
			}
		} finally {
			if (close) {
				try {
					stream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		try {
			arrayOutputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return arrayOutputStream.toByteArray();
	}

	public static boolean isEmptyString(String str) {
		if (str == null || str.isEmpty()) {
			return true;
		}
		return false;
	}

	public static boolean isEmptyString(String... strs) {
		if (strs.length == 0) {
			return true;
		}
		for (String str : strs) {
			if (str == null || str.isEmpty()) {
				return true;
			}
		}
		return false;
	}

	public static String getFileSuffix(String fileName) {
		int index = fileName.lastIndexOf('.');
		if (index == -1) {
			return "";
		}
		return fileName.substring(index + 1);
	}

	public static boolean isWindows() {
		return File.separator.equals("\\");
	}

	public static String runCommond(String cmd, String charSet) throws IOException {
		if (isEmptyString(cmd)) {
			return null;
		}
		StringBuilder stringBuilder = new StringBuilder();
		Process process = Runtime.getRuntime().exec(cmd);
		try (InputStreamReader reader = new InputStreamReader(process.getInputStream());) {
			char[] charBuf = new char[64];
			int len = 0;
			while ((len = reader.read(charBuf)) != -1) {
				stringBuilder.append(charBuf, 0, len);
			}
		}
		return stringBuilder.toString();
	}
    
    public static String listToString(List<String> ports) {
		String res = "";
	
		for (int i = 0; i < ports.size(); i++) {
			if (i == ports.size() - 1) {
				res += ports.get(i);
				continue;
			}
			
			res += ports.get(i) + ", ";
		}
	
		
		
		res.trim();
		return res;
    }
}
