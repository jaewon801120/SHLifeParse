/**
 *
 */
package com.lgcns.testpilot.common.mapping.parser.nexacro;

import java.io.ByteArrayInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

/**
 * @author 70936
 *
 */
public class NexacroParserTest {
	/** slf4j */
	private static final Logger LOGGER = LoggerFactory.getLogger(NexacroParserTest.class);

	private NexacroParserTest() {
        // should not be Instances
    }
	
	public static String getDatafromSSV(String data) {
		String ret = "";
		try {
			byte[] byteData = data.getBytes();
			ByteArrayInputStream bin = new ByteArrayInputStream(byteData);
			try {
				JsonObject jsonContents = NexacroParser.getJsonObject(bin, "UTF-8");
				if (jsonContents != null) {
					ret = jsonContents.toString();
					LOGGER.debug("JSON Data : \n{}", ret);
					System.out.println(ret);
					// ByteArrayOutputStream outStream = new ByteArrayOutputStream();
					// NexacroParser.getXmlFromJsonObject(outStream, asisResParamsAsJSON, "UTF-8");
					// LOGGER.debug("RESPONSE XML : [{}]", outStream.toString("UTF-8"));
				}
			} catch (UnsupportedOperationException e) {
				LOGGER.error(e.getMessage(), e);
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
		
		try {
			FileUtils.writeFile("D:\\Download\\", "test_hangul.json", ret);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return ret;
	}

	public static void main(String[] args) {
		
		if (args.length < 1) {
			//return;

			//String p = "D:\\병행검증솔루션\\Project\\신한오렌지\\PerfecTwin\\case\\nexacro\\오렌지(BIN)\\res";
			String p = "D:\\병행검증솔루션\\Project\\신한오렌지\\PerfecTwin\\case\\nexacro\\sni\\test_hangul.dat";
			byte[] byteData = FileUtils.readFileToByte(p);
			if (byteData == null) {
				return;
			}
			String data = new String(byteData);
			String ret = getDatafromSSV(data);
			return;
		}
		
		getDatafromSSV(args[0]);
	}
}
