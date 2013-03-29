package org.feit.geoevents.http;

import java.io.IOException;
import java.io.InputStream;
/***
 * IO Utility class.
 * @author Tomche
 *
 */
public class IOUtil {
	/***
	 * Private constructor for disabling the instantiation of this utility class.
	 */
	private IOUtil(){
	}
	/***
	 * Create string from input stream.
	 * @param stream The input stream.
	 * @param length Length in bytes of the stream.
	 * @return The result string.
	 * @throws IOException
	 */
	public static String getStringFromStream(InputStream stream, int length) throws IOException{ 
		byte[] data = new byte[length];
		stream.read(data, 0, length);
		StringBuilder stringBuilder = new StringBuilder();
		for(int i = 0; i < data.length; i++){
			stringBuilder.append((char)data[i]);
		}
		return stringBuilder.toString();
	}
}
