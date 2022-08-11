package org.lx.tools;

public class ByteTool {

	public static void reversebByte(byte[] buf) {

		for (int start = 0, end = buf.length - 1; start < end; start++, end--) {
			byte temp = buf[end];
			buf[end] = buf[start];
			buf[start] = temp;
		}
	}

	public static short bytesToShort(byte[] b) {
		short s = 0;
		short s0 = (short) (b[0] & 0xff);// 最低位
		short s1 = (short) (b[1] & 0xff);
		s1 <<= 8;
		s = (short) (s0 | s1);
		return s;
	}

	public static int byteArrayToInt(byte[] b) {
		return b[3] & 0xFF | (b[2] & 0xFF) << 8 | (b[1] & 0xFF) << 16 | (b[0] & 0xFF) << 24;
	}

	public static String byteArrayToMac(byte[] buf) {
		String value = "";
		for (int i = 0; i < buf.length; i++) {
			String sTemp = Integer.toHexString(0xFF & buf[i]);
			if(sTemp.length()==1) {
				sTemp = 0+sTemp;
			}
			value = value + sTemp + ":";
		}
		value = value.substring(0, value.lastIndexOf(":"));
		return value;
	}

	public static byte intToByte(int x) {
		return (byte) x;
	}

	public static int byteToInt(byte b) {
		return b & 0xFF;
	}

	public static String byteToHexString(byte b) {
		int v = b & 0xFF;
		String hv = Integer.toHexString(v);
		if (hv.length() < 2) {
			hv = "0" + hv;
		}
		return hv;
	}

	public static String bytesToHexString(byte[] buf) {
		StringBuilder stringBuilder = new StringBuilder("");
		if (buf == null || buf.length <= 0) {
			return null;
		}
		for (int i = 0; i < buf.length; i++) {
			int v = buf[i] & 0xFF;
			String hv = Integer.toHexString(v);
			if (hv.length() < 2) {
				stringBuilder.append(0);
			}
			stringBuilder.append(hv);
			stringBuilder.append(" ");
		}
		return stringBuilder.toString();
	}

	public static String bytesToHexString(byte[] buf, int start, int length) {
		StringBuilder stringBuilder = new StringBuilder("");
		if (buf == null || buf.length <= 0) {
			return null;
		}
		for (int i = start; i < length; i++) {
			int v = buf[i] & 0xFF;
			String hv = Integer.toHexString(v);
			if (hv.length() < 2) {
				stringBuilder.append(0);
			}
			stringBuilder.append(hv);
			stringBuilder.append(" ");
		}
		return stringBuilder.toString();
	}

	public static String byteToHex(byte b) {
        int v = b & 0xFF;
        String hv = Integer.toHexString(v);
        if (hv.length() < 2) {
            return "0" + hv;
        } else {
            return hv;
        }
    }
	
	public static String bytesToHexStringNoSpace(byte[] buf) {
		StringBuilder stringBuilder = new StringBuilder("");
		if (buf == null || buf.length <= 0) {
			return null;
		}
		for (int i = 0; i < buf.length; i++) {
			int v = buf[i] & 0xFF;
			String hv = Integer.toHexString(v);
			if (hv.length() < 2) {
				stringBuilder.append(0);
			}
			stringBuilder.append(hv);
		}
		return stringBuilder.toString();
	}

	public static byte[] hexStringToBytes(String hexString) {
		if (hexString == null || hexString.equals("")) {
			return null;
		}
		hexString = hexString.toUpperCase();
		int length = hexString.length() / 2;
		char[] hexChars = hexString.toCharArray();
		byte[] d = new byte[length];
		for (int i = 0; i < length; i++) {
			int pos = i * 2;
			d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
		}
		return d;
	}

	public static byte hexStringToByte(String hexString) {
		hexString = hexString.toUpperCase();
		char[] hexChars = hexString.toCharArray();
		byte b;
		b = (byte) (charToByte(hexChars[0]) << 4 | charToByte(hexChars[1]));
		return b;
	}

	private static byte charToByte(char c) {
		return (byte) "0123456789ABCDEF".indexOf(c);
	}

	public static String byteToBit(byte b) {
		return "" + (byte) ((b >> 7) & 0x1) + (byte) ((b >> 6) & 0x1) + (byte) ((b >> 5) & 0x1)
				+ (byte) ((b >> 4) & 0x1) + (byte) ((b >> 3) & 0x1) + (byte) ((b >> 2) & 0x1) + (byte) ((b >> 1) & 0x1)
				+ (byte) ((b >> 0) & 0x1);
	}

	public static String bytesToBit(byte[] bs) {
		StringBuffer stringBuffer = new StringBuffer();
		for (byte b : bs) {
			stringBuffer.append(byteToBit(b));
		}
		return stringBuffer.toString();
	}

	public static long bytes2long(byte[] res) {

		int firstByte = 0;
		int secondByte = 0;
		int thirdByte = 0;
		int fourthByte = 0;
		int index = 0;
		firstByte = (0x000000FF & ((int) res[index]));
		secondByte = (0x000000FF & ((int) res[index + 1]));
		thirdByte = (0x000000FF & ((int) res[index + 2]));
		fourthByte = (0x000000FF & ((int) res[index + 3]));
		index = index + 4;
		return ((long) (firstByte << 24 | secondByte << 16 | thirdByte << 8 | fourthByte)) & 0xFFFFFFFFL;
	}

	public static byte[] long2bytes(long l) {
		byte[] bytes = new byte[4];
		bytes[0] = (byte) ((l >> 24) & 0xFF);
		bytes[1] = (byte) ((l >> 16) & 0xFF);
		bytes[2] = (byte) ((l >> 8) & 0xFF);
		bytes[3] = (byte) ((l) & 0xFF);
		return bytes;
	}

	public static byte[] int2Bytes(int value, int len) {
		byte[] b = new byte[len];
		for (int i = 0; i < len; i++) {
			b[len - i - 1] = (byte) ((value >> 8 * i) & 0xff);
		}
		return b;
	}

	public static byte[] long2Bytes(long value, int len) {
		byte[] b = new byte[len];
		for (int i = 0; i < len; i++) {
			b[len - i - 1] = (byte) ((value >> 8 * i) & 0xff);
		}
		return b;
	}

	public static int bytes2Int(byte[] b, int start, int len) {
		int sum = 0;
		int end = start + len;
		for (int i = start; i < end; i++) {
			int n = ((int) b[i]) & 0xff;
			n <<= (--len) * 8;
			sum |= n;
		}
		return sum;
	}

	public static long bytes2long(byte[] res, int start, int len) {

		long sum = 0;
		int end = start + len;
		for (int i = start; i < end; i++) {
			long n = ((long) res[i]) & 0xffl;
			n <<= (--len) * 8;
			sum += n;
		}
		return sum;
	}

	public static byte reverseBit(byte b) {
		int v = byteToInt(b);

		// 交换每两位
		v = ((v >> 1) & 0x55) | ((v & 0x55) << 1); // abcdefgh -> badcfehg
		// 交换每四位中的前两位和后两位
		v = ((v >> 2) & 0x33) | ((v & 0x33) << 2); // badcfehg -> dcbahgfe
		// 交换前四位和后四位
		v = (v >> 4) | (v << 4); // dcbahgfe -> hgfedcba
		return intToByte(v);
	}

	public static byte[] reverseBits(byte[] bs) {

		reversebByte(bs);
		for (int i = 0; i < bs.length; i++) {
			bs[i] = reverseBit(bs[i]);
		}
		return bs;
	}

	public static String bytes2ascii(byte[] bs) {
		if (bs == null || bs.length == 0) {
			return "";
		}
		StringBuffer stringBuffer = new StringBuffer();
		for (byte b : bs) {
			stringBuffer.append((char) b);
		}
		return stringBuffer.toString();
	}
}
