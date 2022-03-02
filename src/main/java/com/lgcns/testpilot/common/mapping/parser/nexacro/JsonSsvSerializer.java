package com.lgcns.testpilot.common.mapping.parser.nexacro;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class JsonSsvSerializer {

	/** slf4j */
	private static final Logger LOGGER = LoggerFactory.getLogger(JsonSsvSerializer.class);

	private static final String JSONPATH_HEADER = "_SSV_Header_";
	private static final String JSONPATH_VARIABLES = "variables";
	private static final String JSONPATH_DATASETS = "datasets";
	private static final char RS = 0x1E;
	private static final char US = 0x1F;
	private static final char ETX = 0x03;

	public void writeData(OutputStream out, JsonObject data, String charset) throws PlatformException {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Writing data: this=" + this + ", charset=" + charset);
		}
		try {
			writeData(new OutputStreamWriter(out, charset), data, charset);
		} catch (UnsupportedEncodingException ex) {
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error("Unsupported charset: " + charset, ex);
			}
			throw new PlatformException("Unsupported charset: " + charset, ex);
		}
	}

	public void writeData(Writer out, JsonObject data, String charset) throws PlatformException {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Writing data: this=" + this + ", charset=" + charset);
		}

		try {
			write(out, data, charset);
		} catch (IOException ex) {
			String contentType = "PlatformSsv";
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error("Could not serialize: contentType=" + contentType, ex);
			}
			throw new PlatformException("Could not serialize: contentType=" + contentType, ex);
		}
	}

	private void write(Writer out, JsonObject data, String charset) throws IOException {

		writeHeader(out, data, charset);
		if (!data.get(JSONPATH_VARIABLES).isJsonNull()) {
			JsonObject variables = data.getAsJsonObject(JSONPATH_VARIABLES);
			writeVariableList(out, variables);
		}
		if (!data.get(JSONPATH_DATASETS).isJsonNull()) {
			JsonObject datasets = data.getAsJsonObject(JSONPATH_DATASETS);
			writeDataSetList(out, datasets);
		}

		out.flush();
	}

	private void writeHeader(Writer out, JsonObject data, String charset) throws IOException {
		LOGGER.debug("Writing data: charset={}", charset);
		if (!NullUtil.isNull(data.get(JSONPATH_HEADER))) {
			String header = data.get(JSONPATH_HEADER).getAsString();
			writeString(out, header);
		}
	}

	private void writeVariableList(Writer out, JsonObject variables) throws IOException {
		for (Entry<String, JsonElement> entry : variables.entrySet()) {
			String name = entry.getKey();

			String dataTypeStr = null;
			int typeIndex = name.indexOf(':');
			if (typeIndex > 0) {
				String typeStr = name.substring(typeIndex + 1);
				int sizeStartIndex = typeStr.indexOf('(');
				int sizeEndIndex = typeStr.indexOf(')', sizeStartIndex + 1);
				if ((sizeStartIndex > 0) && (sizeEndIndex > 0)) {
					dataTypeStr = typeStr.substring(0, sizeStartIndex);
				} else {
					dataTypeStr = typeStr;
				}
			} else {
				dataTypeStr = "string";
			}

			String value = null;
			if (entry.getValue().isJsonNull()) {
				value = null;
			} else {
				value = entry.getValue().getAsString();
			}

			if ("string".equalsIgnoreCase(dataTypeStr) && value == null) {
				writeString(out, name + "=" + ETX);
			} else {
				writeString(out, name + "=" + value);
			}
		}
	}

	private void writeDataSetList(Writer out, JsonObject datasets) throws IOException {
		int dsCount = 0;
		for (String name : datasets.keySet()) {
			if (dsCount > 0) {
				writeRS(out);
			}
			JsonArray rows = datasets.getAsJsonArray(name);
			writeDataSet(out, name, rows);
			dsCount++;
		}
	}

	private void writeDataSet(Writer out, String name, JsonArray rows) throws IOException {
		writeString(out, "Dataset:" + name);

		for (int r = 0; r < rows.size(); r++) {
			JsonObject row = rows.get(r).getAsJsonObject();
			if (r == 0) {
				int c = 0;
				for (String key : row.keySet()) {
					if (c > 0) {
						writeUS(out);
					}
					writeColumn(out, key);
					c++;
				}
				writeRS(out);
			}

			int c = 0;
			for (String key : row.keySet()) {
				String value = null;
				if (row.get(key).isJsonNull()) {
					value = null;
				} else {
					value = row.get(key).getAsString();
				}

				if (c > 0) {
					writeUS(out);
				}

				String dataTypeStr = null;
				int typeIndex = name.indexOf(':');
				if (typeIndex > 0) {
					String typeStr = name.substring(typeIndex + 1);
					int sizeStartIndex = typeStr.indexOf('(');
					int sizeEndIndex = typeStr.indexOf(')', sizeStartIndex + 1);
					if ((sizeStartIndex > 0) && (sizeEndIndex > 0)) {
						dataTypeStr = typeStr.substring(0, sizeStartIndex);
					} else {
						dataTypeStr = typeStr;
					}
				} else {
					dataTypeStr = "string";
				}

				if ("string".equalsIgnoreCase(dataTypeStr) && value == null) {
					out.write(ETX);
				} else {
					writeColumn(out, value);
				}

				c++;
			}
			writeRS(out);
		}
	}

	private void writeColumn(Writer out, String str) throws IOException {
		out.write(str);
	}

	private void writeUS(Writer out) throws IOException {
		out.write(US);
	}

	private void writeRS(Writer out) throws IOException {
		out.write(RS);
	}

	private void writeString(Writer out, String str) throws IOException {
		out.write(str);
		out.write(RS);
	}

}