package com.lgcns.testpilot.common.mapping.parser.nexacro;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.SequenceInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.nexacro17.xapi.data.PlatformData;
import com.nexacro17.xapi.tx.PlatformType;

/**
 * Nexacro �뙆�꽌�뿉�꽌 JSON�뜲�씠�꽣�뒗 SSV DATA�뿉�꽌 異붿텧�븯�뒗寃껋쓣 湲곕낯�쑝濡쒗븳�떎. XML -> LOAD
 * -> SSVDATA -> JSON SSV -> LOAD -> SSVDATA -> JSON BINARY -> LOAD -> SSVDATA
 * -> JSON
 * 
 * @author MINJI KIM
 *
 */
public class NexacroParser {

	private static final Logger LOGGER = LoggerFactory.getLogger(NexacroParser.class);

	public static JsonObject getJsonObject(InputStream content, String charset) throws PlatformException {
		JsonObject parsedContent = null;
		PlatformRequest pr = null;
		PlatformData pd = null;

		int count = 4;
		byte[] buffer = new byte[count];
		int offset = 0;
		while (true) {
			int n = 0;
			try {
				n = content.read(buffer, offset, count - offset);
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (n == -1)
				break;
			if (n == 0) {
				LOGGER.debug("Check Default Content-Type: n=" + n + ", offset=" + offset + ", count=" + count);
				continue;
			}
			offset += n;
			if (offset == count)
				break;
		}

		String head = new String(buffer);
		String nexacroContentType = "";
		if (head.equalsIgnoreCase("SSV:")) {
			nexacroContentType = PlatformType.CONTENT_TYPE_SSV;
		} else if (head.equalsIgnoreCase("<?xm")) {
			nexacroContentType = PlatformType.CONTENT_TYPE_XML;
		} else {
			nexacroContentType = PlatformType.CONTENT_TYPE_BINARY;
		}

		pr = readRequest(new SequenceInputStream(new ByteArrayInputStream(buffer), content), nexacroContentType,
				charset);
		pd = pr.getData();
        JsonSsvDeserializer jsd = new JsonSsvDeserializer();
        parsedContent = jsd.readData(new ByteArrayInputStream(pd.saveSsv().getBytes()), charset);

//	    switch (contentType) {
//            case PlatformType.HTTP_CONTENT_TYPE_BINARY:
//                try {
//                    parsedContent = getJsonObjectFromBinary(content, charset);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    LOGGER.error("Error Occurred while Parsing Nexcro Binary to Json", e);
//                }
//                break;
//            case PlatformType.HTTP_CONTENT_TYPE_XML:
//                
//                if (head.equalsIgnoreCase("SSV:")) {
//                    pr = readRequest(new SequenceInputStream(new ByteArrayInputStream(buffer), content), PlatformType.CONTENT_TYPE_SSV, charset);
//                    pd = pr.getData();
//                    JsonSsvDeserializer jsd = new JsonSsvDeserializer();
//                    parsedContent = jsd.readData(new ByteArrayInputStream(pd.saveSsv().getBytes()), charset);
//                } else if (head.equalsIgnoreCase("<?xm")) {
//                    pr = readRequest(new SequenceInputStream(new ByteArrayInputStream(buffer), content), PlatformType.CONTENT_TYPE_XML, charset);
//                    pd = pr.getData();
//                    JsonSsvDeserializer jsd = new JsonSsvDeserializer();
//                    parsedContent = jsd.readData(new ByteArrayInputStream(pd.saveSsv().getBytes()), charset);
//                } else {
//                    
//                } 
//                
//                break;
//            case PlatformType.HTTP_CONTENT_TYPE_HTML:
//                pr = readRequest(content, PlatformType.CONTENT_TYPE_HTML, charset);
//                pd = pr.getData();
//                
//                break;
//            case HttpMime.APPLICATION_FORM_URLENCODED:
//                break;
//                
//            default:
//	    } 
		if (parsedContent == null) {
			parsedContent = new JsonObject();
		}
		return parsedContent;
	}

//	public static void getXmlFromJsonObject(OutputStream out, JsonObject jsonData, String charset)  throws Exception {
//	    OutputStream outPd = new ByteArrayOutputStream();
//        JsonSsvSerializer jss = new JsonSsvSerializer();
//        jss.writeData(outPd, jsonData, charset);
//        PlatformData pd = new PlatformData();
//        pd.loadSsv(outPd.toString());
//        
//        PlatformResponse res = new PlatformResponse();
//        res.setContentType(PlatformType.CONTENT_TYPE_XML);
//        res.setCharset(charset);
//        res.setOutputStream(out);
//        try {
//            res.setData(pd);
//            res.sendData();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//	}

	public static void getSsvFromJsonObject(OutputStream out, JsonObject jsonData, String charset) throws Exception {
		OutputStream outPd = new ByteArrayOutputStream();
		JsonSsvSerializer jss = new JsonSsvSerializer();
		jss.writeData(outPd, jsonData, charset);
		PlatformData pd = new PlatformData();
		pd.loadSsv(outPd.toString());

		PlatformResponse res = new PlatformResponse();
		res.setContentType(PlatformType.CONTENT_TYPE_SSV);
		res.setCharset(charset);
		res.setOutputStream(out);
		try {
			res.setData(pd);
			res.sendData();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

//	public static JsonObject getJsonObjectFromBinary(InputStream content, String charset) throws Exception {
//		PlatformRequest  pr = readRequest(content, PlatformType.CONTENT_TYPE_BINARY, charset);
//		PlatformData pd = pr.getData();
//		LOGGER.debug("platform data :: {} ", pd.saveSsv());
//		
//		JsonSsvDeserializer jsd = new JsonSsvDeserializer();
//		JsonObject jo = jsd.readData(new ByteArrayInputStream(pd.saveSsv().getBytes()), charset);
//		return jo;
//	}
//	
//	public static JsonObject getJsonObjectFromXML(InputStream content, String charset) throws Exception {
//		PlatformRequest  pr = readRequest(content, PlatformType.CONTENT_TYPE_XML, charset);
//		PlatformData pd = pr.getData();
//		LOGGER.debug("platform data :: {} ", pd.saveSsv());
//		
//		JsonSsvDeserializer jsd = new JsonSsvDeserializer();
//		JsonObject jo = jsd.readData(new ByteArrayInputStream(pd.saveSsv().getBytes()), charset);
//		return jo;
//	}
//	
//	
//	public static void getBinaryFromJsonObject(OutputStream out, JsonObject jsonData, String charset)  throws Exception {
//		OutputStream outPd = new ByteArrayOutputStream();
//		JsonSsvSerializer jss = new JsonSsvSerializer();
//		jss.writeData(outPd, jsonData, charset);
//		PlatformData pd = new PlatformData();
//		pd.loadSsv(outPd.toString());
//		LOGGER.debug("platform data :: {} ", outPd.toString());
//		
//		PlatformResponse res = new PlatformResponse();
//		res.setContentType(PlatformType.CONTENT_TYPE_BINARY);
//		res.setCharset(charset);
//		res.setOutputStream(out);
//       	res.addProtocolType(PlatformType.PROTOCOL_TYPE_ZLIB);	// PlatformType.PROTOCOL_TYPE_ZLIB = "PlatformZlib";
//		
//		try {
//        	res.setData(pd);
//			res.sendData();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
//	
//	public static void getXMLFromJsonObject(OutputStream out, JsonObject jsonData, String charset)  throws Exception {
//		OutputStream outPd = new ByteArrayOutputStream();
//		JsonSsvSerializer jss = new JsonSsvSerializer();
//		jss.writeData(outPd, jsonData, charset);
//		PlatformData pd = new PlatformData();
//		pd.loadSsv(outPd.toString());
//		
//		LOGGER.debug("platform data :: {} ", outPd.toString());
//		
//		PlatformResponse res = new PlatformResponse();
//		res.setContentType(PlatformType.CONTENT_TYPE_XML);
//		res.setCharset(charset);
//		res.setOutputStream(out);
//		
//		try {
//        	res.setData(pd);
//			res.sendData();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}

	private static PlatformRequest readRequest(InputStream in, String contentType, String charset) {
		PlatformRequest req = new PlatformRequest(in, contentType, charset);
		try {
			req.receiveData();
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		} finally {
			try {
				in.close();
			} catch (IOException e) {
				LOGGER.error(e.getMessage(), e);
			}
		}
		return req;
	}

}
