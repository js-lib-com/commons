package js.util;

import java.io.UnsupportedEncodingException;

import js.lang.BugError;

/**
 * Base64 encoding utility class. Provides handy method for encode and decode strings and bytes array to and from Base64 format.
 * <p>
 * Sample use case as below:
 * 
 * <pre>
 * String credentials = Strings.concat(user, ':', password);
 * String basicAuthorization = &quot;Basic &quot; + Base64.encode(credentials);
 * </pre>
 * 
 * @author Iulian Rotaru
 * @version final
 */
public final class Base64 {
	/** Mapping table from 6-bit nibbles to Base64 characters. */
	private static final char[] NIBLES_2_CHARS = new char[64];

	/** Mapping table from Base64 characters to 6-bit nibbles. */
	private static final byte[] CHARS_2_NIBLES = new byte[128];

	static {
		int i = 0;
		for (char c = 'A'; c <= 'Z'; c++) {
			NIBLES_2_CHARS[i++] = c;
		}
		for (char c = 'a'; c <= 'z'; c++) {
			NIBLES_2_CHARS[i++] = c;
		}
		for (char c = '0'; c <= '9'; c++) {
			NIBLES_2_CHARS[i++] = c;
		}
		NIBLES_2_CHARS[i++] = '+';
		NIBLES_2_CHARS[i++] = '/';

		for (i = 0; i < CHARS_2_NIBLES.length; i++) {
			CHARS_2_NIBLES[i] = -1;
		}
		for (i = 0; i < 64; i++) {
			CHARS_2_NIBLES[NIBLES_2_CHARS[i]] = (byte) i;
		}
	}

	/** Prevent default constructor synthesis. */
	private Base64() {
	}

	/**
	 * Encode string value into Base64 format.
	 * 
	 * @param string string to encode.
	 * @return given <code>string</code> value encoded Base64.
	 */
	public static String encode(String string) {
		try {
			return encode(string.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			throw new BugError("JVM with missing support for UTF-8.");
		}
	}

	/**
	 * Encodes a byte array into Base64 format. No blanks or line breaks are inserted.
	 * 
	 * @param bytes an array containing the data bytes to be encoded.
	 * @return a string with the Base64 encoded data.
	 */
	public static String encode(byte[] bytes) {
		int bytesLength = bytes.length;
		int base64Length = (bytesLength * 4 + 2) / 3; // base64 encoded string length, without padding

		StringBuilder base64Builder = new StringBuilder();
		int bytesIndex = 0;

		while (bytesIndex < bytesLength) {
			int i0 = bytes[bytesIndex++] & 0xff;
			int i1 = bytesIndex < bytesLength ? bytes[bytesIndex++] & 0xff : 0;
			int i2 = bytesIndex < bytesLength ? bytes[bytesIndex++] & 0xff : 0;

			int o0 = i0 >>> 2;
			int o1 = ((i0 & 3) << 4) | (i1 >>> 4);
			int o2 = ((i1 & 0xf) << 2) | (i2 >>> 6);
			int o3 = i2 & 0x3F;

			base64Builder.append(NIBLES_2_CHARS[o0]);
			base64Builder.append(NIBLES_2_CHARS[o1]);
			base64Builder.append(base64Builder.length() < base64Length ? NIBLES_2_CHARS[o2] : '=');
			base64Builder.append(base64Builder.length() < base64Length ? NIBLES_2_CHARS[o3] : '=');
		}

		return base64Builder.toString();
	}

	/**
	 * Decodes a byte array from a Base64 formated string. No blanks or line breaks are allowed within the Base64 encoded data.
	 * 
	 * @param base64 a character array containing the Base64 encoded data.
	 * @return an array containing the decoded data bytes.
	 * @throws IllegalArgumentException if the input is not valid Base64 encoded data.
	 */
	public static byte[] decode(String base64) {
		int iLen = base64.length();
		if (iLen % 4 != 0) {
			throw new IllegalArgumentException("Length of Base64 encoded input string is not a multiple of 4.");
		}
		// remove trailing padding
		while (iLen > 0 && base64.charAt(iLen - 1) == '=') {
			iLen--;
		}

		int oLen = (iLen * 3) / 4;
		byte[] out = new byte[oLen];

		int ip = 0, op = 0;
		while (ip < iLen) {
			int i0 = base64.charAt(ip++);
			int i1 = base64.charAt(ip++);
			int i2 = ip < iLen ? base64.charAt(ip++) : 'A';
			int i3 = ip < iLen ? base64.charAt(ip++) : 'A';

			if (i0 > 127 || i1 > 127 || i2 > 127 || i3 > 127) {
				throw new IllegalArgumentException("Illegal character in Base64 encoded data.");
			}

			int b0 = CHARS_2_NIBLES[i0];
			int b1 = CHARS_2_NIBLES[i1];
			int b2 = CHARS_2_NIBLES[i2];
			int b3 = CHARS_2_NIBLES[i3];
			if (b0 < 0 || b1 < 0 || b2 < 0 || b3 < 0) {
				throw new IllegalArgumentException("Illegal character in Base64 encoded data.");
			}

			int o0 = (b0 << 2) | (b1 >>> 4);
			int o1 = ((b1 & 0xf) << 4) | (b2 >>> 2);
			int o2 = ((b2 & 3) << 6) | b3;
			out[op++] = (byte) o0;
			if (op < oLen) {
				out[op++] = (byte) o1;
			}
			if (op < oLen) {
				out[op++] = (byte) o2;
			}
		}
		return out;
	}
}
