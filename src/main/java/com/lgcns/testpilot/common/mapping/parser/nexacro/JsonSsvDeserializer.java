package com.lgcns.testpilot.common.mapping.parser.nexacro;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class JsonSsvDeserializer {

	/** slf4j */
	private static final Logger LOGGER = LoggerFactory.getLogger(JsonSsvDeserializer.class);

	private static final String JSONPATH_HEADER = "_SSV_Header_";
	private static final String JSONPATH_VARIABLES = "variables";
	private static final String JSONPATH_DATASETS = "datasets";
	private static final char RS = 0x1E;
	private static final char US = 0x1F;
	private static final char ETX = 0x03;

	public JsonObject readData(InputStream in, String charset) throws PlatformException {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Reading data: this=" + this + ", charset=" + charset);
		}
		try {
			JsonObject data = new JsonObject();

			byte[] header = readHeader(in);
			String encoding = findEncoding(header);

			if (encoding == null) {
				encoding = charset;
			}

			readSSVHeader(header, data);
			Reader reader = new InputStreamReader(in, encoding);
			return read(reader, data);
		} catch (IOException ex) {
			String contentType = "PlatformSsv";
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error("Could not deserialize: contentType=" + contentType, ex);
			}
			throw new PlatformException("Could not deserialize: contentType=" + contentType, ex);
		}
	}

	private JsonObject read(Reader in, JsonObject data) throws PlatformException {
		SeperatorReader reader = new SeperatorReader(in, RS);
		try {
			JsonObject variables = new JsonObject();
			JsonObject datasets = new JsonObject();
			data.add(JSONPATH_VARIABLES, variables);
			data.add(JSONPATH_DATASETS, datasets);

			String line = null;
			for (;;) {
				line = reader.readLine();
				if (line == null) {
					break;
				}
				if (line.length() != 0) {
					if (isDataSetHeader(line)) {
						readDataSet(reader, line, datasets);
					} else {
						readVariable(line, variables);
					}
				}
			}
			return data;
		} catch (IOException ex) {
			String contentType = "PlatformSsv";
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error("Could not deserialize: contentType=" + contentType, ex);
			}
			throw new PlatformException("Could not deserialize: contentType=" + contentType, ex);
		}
	}

	private void readVariable(String str, JsonObject data) {
		List<String> list = split(str, US);
		Iterator<String> iter = list.iterator();

		while (iter.hasNext()) {
			String varStr = (String) iter.next();
			int index = varStr.indexOf('=');
			if (index > 0) {
				String header = varStr.substring(0, index);
				String value = varStr.substring(index + 1);

				data.addProperty(header, value);
			}
		}
	}

	private void readDataSet(SeperatorReader reader, String header, JsonObject data) throws IOException {
		int index = header.indexOf(':');
		String name = header.substring(index + 1);

		JsonArray ds = new JsonArray();

		String line = null;

		List<String> currentColumnHeader = null;
		for (;;) {
			line = reader.readLine();

			if ((line == null) || (line.length() == 0)) {
				break;
			}

			if (isConstantColumnHeader(line)) {
				currentColumnHeader = readConstantColumnHeader(line);
			} else if (isDefaultColumnHeader(line)) {
				currentColumnHeader = readDefaultColumnHeader(line);
			} else {
				readDataRow(line, ds, currentColumnHeader);
			}
		}

		data.add(name, ds);
	}

	private List<String> readConstantColumnHeader(String str) {
		List<String> columnHeader = new ArrayList<String>();

		List<String> list = split(str, US);
		Iterator<String> iter = list.iterator();
		while (iter.hasNext()) {
			String columnStr = (String) iter.next();
			columnHeader.add(columnStr);
		}
		return columnHeader;
	}

	private List<String> readDefaultColumnHeader(String str) {
		List<String> columnHeader = new ArrayList<String>();

		List<String> list = split(str, US);
		Iterator<String> iter = list.iterator();
		while (iter.hasNext()) {
			String columnStr = (String) iter.next();
			columnHeader.add(columnStr);
		}
		return columnHeader;
	}

	private void readDataRow(String str, JsonArray ds, List<String> columnHeader) {
		JsonObject row = new JsonObject();

		List<String> list = split(str, US);
		Iterator<String> iter = list.iterator();

		int columnIndex = 0;
		while (iter.hasNext()) {
			String value = iter.next();
			if (isNullString(value)) {
				// TODO : �꼸濡쒗븷源�? 怨듬갚�쑝濡� �븷源�?
				value = null;
				row.addProperty(columnHeader.get(columnIndex), value);
				// } else if(isXmlString(value)) {
				// String colName = columnHeader.get(columnIndex);
				// int typePos = colName.indexOf(':');
				// if(typePos > 0) {
				// colName = colName.substring(0, typePos);
				// }
				// value = value.replace("<![CDATA[<b>]]>", "");
				// value = value.replace("<![CDATA[</b>]]>", "");
				// JSONObject jo = XML.toJSONObject(value);
				// String ss = jo.toString();
				// JsonObject jsonObject = new JsonParser().parse(ss).getAsJsonObject();
				// row.add(colName, jsonObject.get("root"));
			} else {
				row.addProperty(columnHeader.get(columnIndex), value);
			}
			columnIndex++;
		}
		ds.add(row);
	}

	private boolean isConstantColumnHeader(String str) {
		return str.startsWith("_Const_");
	}

	private boolean isDefaultColumnHeader(String str) {
		return str.startsWith("_RowType_");
	}

	private boolean isDataSetHeader(String str) {
		return str.startsWith("Dataset:");
	}

	private byte[] readHeader(InputStream in) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		for (;;) {
			int ch = in.read();

			out.write(ch);
			if (ch == -1) {
				break;
			}
			if (ch == RS) {
				break;
			}
		}
		out.close();

		return out.toByteArray();
	}

	private String findEncoding(byte[] header) {
		return findEncoding(new String(header));
	}

	private String findEncoding(String header) {
		String prefix = "SSV:";
		int index = header.indexOf(prefix);
		if (index == 0) {
			int begin = index + prefix.length();
			int end = header.length();
			String encoding = header.substring(begin, end).trim();
			if (encoding.length() > 0) {
				return encoding;
			}
		}
		return null;
	}

	private void readSSVHeader(byte[] header, JsonObject data) {
		String headerStr = new String(header);
		String prefix = "SSV:";
		int index = headerStr.indexOf(prefix);
		if (index == 0) {
			String value = headerStr.substring(0, headerStr.length()).trim();
			data.addProperty(JSONPATH_HEADER, value);
		}
	}

	private List<String> split(String str, char ch) {
		int start = 0;
		List<String> list = new ArrayList<String>();
		for (;;) {
			int index = str.indexOf(ch, start);
			if (index == -1) {
				list.add(str.substring(start));
				break;
			}
			int end = index;
			list.add(str.substring(start, end));
			start = end + 1;
		}
		return list;
	}

	private boolean isNullString(String value) {
		if ((value.length() == 1) && (value.charAt(0) == ETX)) {
			return true;
		}
		return false;
	}

	private boolean isXmlString(String value) {
		if (value.startsWith("<?xml ") && value.endsWith("</root>")) {
			return true;
		}
		return false;
	}
}
