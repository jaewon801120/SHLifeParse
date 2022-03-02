package com.lgcns.testpilot.common.mapping.parser.nexacro;

import com.nexacro17.xapi.data.Debugger;
import com.nexacro17.xapi.data.PlatformData;
import com.nexacro17.xapi.tx.DataDeserializer;
import com.nexacro17.xapi.tx.DataSerializerFactory;
import com.nexacro17.xapi.tx.ProtocolDecoder;
import com.nexacro17.xapi.tx.ProtocolFilterFactory;
import com.nexacro17.xapi.util.PlatformGlobals;
import com.nexacro17.xapi.util.SequenceReader;
import java.io.ByteArrayInputStream;
import java.io.CharArrayReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.SequenceInputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class PlatformRequest extends PlatformTransaction {
	private Log log = LogFactory.getLog(PlatformRequest.class);

	private static final String IO_CLOSE_KEY = "platform.tx.PlatformRequest.ioclose";

	private static final String STREAM_LOG_ENABLED_KEY = "platform.tx.PlatformRequest.streamlog.enabled";

	private static final String STREAM_LOG_DIR_KEY = "platform.tx.PlatformRequest.streamlog.dir";

	private static final String STREAM_LOG_MAXSIZE_KEY = "platform.tx.PlatformRequest.streamlog.maxsize";

	private static final String STREAM_LOG_PREFIX_KEY = "platform.tx.PlatformRequest.streamlog.prefix";

	private static final String STREAM_LOG_SUFFIX_KEY = "platform.tx.PlatformRequest.streamlog.suffix";

	private static final String STREAM_LOG_EXCEEDED_SIZE_KEY = "platform.tx.PlatformRequest.streamlog.exceededsize";

	private InputStream in;

	private Reader reader;

	private StreamLogInputStream logStream;

	private StreamLogReader logReader;

	private StreamLog streamLog;

	public PlatformRequest(String contentType) {
		this(contentType, (String) null);
	}

	public PlatformRequest(String contentType, String charset) {
		super(contentType, charset);
	}

	public PlatformRequest(InputStream in) {
		this(in, (String) null);
	}

	public PlatformRequest(InputStream in, String contentType) {
		this(in, contentType, (String) null);
	}

	public PlatformRequest(InputStream in, String contentType, String charset) {
		super(contentType, charset);
		this.in = in;
	}

	public PlatformRequest(Reader reader) {
		this(reader, (String) null);
	}

	public PlatformRequest(Reader reader, String contentType) {
		this(reader, contentType, (String) null);
	}

	public PlatformRequest(Reader reader, String contentType, String charset) {
		super(contentType, charset);
		this.reader = reader;
	}

	public Object getProperty(String key) {
		return super.getProperty(key);
	}

	public void setProperty(String key, Object value) {
		super.setProperty(key, value);
	}

	public void receiveData() throws PlatformException {
		checkLicense();
		checkGlobalStreamLog();
		boolean isVerbose = isCurrentVerbose();
		long startTime = System.currentTimeMillis();
		if (isVerbose) {
			if (this.log.isInfoEnabled())
				try {
					this.log.info("Receiving data: " + URLEncoder.encode(toSimpleString(), "UTF-8") + ", contentType="
							+ ((getContentType() != null) ? URLEncoder.encode(getContentType(), "UTF-8") : null)
							+ ", charset="
							+ ((getCharset() != null) ? URLEncoder.encode(getCharset(), "UTF-8") : null));
				} catch (UnsupportedEncodingException e) {
					this.log.info("UnsupportedEncodingException");
				}
		} else if (this.log.isInfoEnabled()) {
			try {
				this.log.info("Receiving data: " + URLEncoder.encode(toSimpleString(), "UTF-8") + ", contentType="
						+ ((getContentType() != null) ? URLEncoder.encode(getContentType(), "UTF-8") : null)
						+ ", charset=" + ((getCharset() != null) ? URLEncoder.encode(getCharset(), "UTF-8") : null)
						+ ", startTime=" + startTime);
			} catch (UnsupportedEncodingException e) {
				this.log.info("UnsupportedEncodingException");
			}
		}
		try {
			checkStream();
			if (useInputStream()) {
				readFrom(getInputStream());
			} else {
				readFrom(getReader());
			}
		} catch (PlatformException ex) {
			try {
				storeStreamLog();
			} catch (IOException ie) {
				if (this.log.isErrorEnabled())
					this.log.error("Storing stream failed", ie);
			}
			throw ex;
		} catch (Throwable th) {
			if (this.log.isErrorEnabled())
				this.log.error("Receiving data failed", th);
			try {
				storeStreamLog();
			} catch (IOException ie) {
				if (this.log.isErrorEnabled())
					this.log.error("Storing stream failed", ie);
			}
			throw new PlatformException("Receiving data failed", th);
		}
		long endTime = System.currentTimeMillis();
		long elapsedTime = endTime - startTime;
		if (isVerbose) {
			if (this.log.isInfoEnabled())
				this.log.info("Received data: " + toSimpleString() + ", elapsedTime=" + elapsedTime);
		} else {
			if (this.log.isInfoEnabled())
				this.log.info(
						"Received data: " + toSimpleString() + ", elapsedTime=" + elapsedTime + ", endTime=" + endTime);
			if (this.log.isDebugEnabled()) {
				Debugger debugger = new Debugger();
				this.log.debug(debugger.detail(getData()));
			}
		}
	}

	public boolean isStreamLogEnabled() {
		return (this.streamLog == null) ? false : this.streamLog.isLogEnabled();
	}

	public void setStreamLogEnabled(boolean isStreamLogEnabled) {
		checkStreamLog();
		this.streamLog.setLogEnabled(isStreamLogEnabled);
	}

	public String getStreamLogDir() {
		return (this.streamLog == null) ? null : this.streamLog.getLogDir();
	}

	public void setStreamLogDir(String streamLogDir) {
		checkStreamLog();
		this.streamLog.setLogDir(streamLogDir);
	}

	public int getStreamLogMaxSize() {
		return (this.streamLog == null) ? 0 : this.streamLog.getLogMaxSize();
	}

	public void setStreamLogMaxSize(int streamLogMaxSize) {
		checkStreamLog();
		this.streamLog.setLogMaxSize(streamLogMaxSize);
	}

	public String getStreamLogPrefix() {
		return (this.streamLog == null) ? null : this.streamLog.getLogPrefix();
	}

	public void setStreamLogPrefix(String streamLogPrefix) {
		checkStreamLog();
		this.streamLog.setLogPrefix(streamLogPrefix);
	}

	public String getStreamLogSuffix() {
		return (this.streamLog == null) ? null : this.streamLog.getLogSuffix();
	}

	public void setStreamLogSuffix(String streamLogSuffix) {
		checkStreamLog();
		this.streamLog.setLogSuffix(streamLogSuffix);
	}

	public void storeStreamLog() throws IOException {
		if (isStreamLogEnabled())
			if (useInputStream()) {
				if (this.logStream == null) {
					(new StreamLogInputStream(null, this.streamLog)).storeStreamLog();
				} else {
					this.logStream.storeStreamLog();
				}
			} else if (this.logReader == null) {
				(new StreamLogReader(null, this.streamLog)).storeStreamLog();
			} else {
				this.logReader.storeStreamLog();
			}
	}

	InputStream getInputStream() {
		if (isStreamLogEnabled()) {
			if (this.logStream == null)
				this.logStream = new StreamLogInputStream(this.in, this.streamLog);
			return this.logStream;
		}
		return this.in;
	}

	void setInputStream(InputStream in) {
		this.in = in;
	}

	Reader getReader() {
		if (isStreamLogEnabled()) {
			if (this.logReader == null)
				this.logReader = new StreamLogReader(this.reader, this.streamLog);
			return this.logReader;
		}
		return this.reader;
	}

	void setReader(Reader reader) {
		this.reader = reader;
	}

	boolean useInputStream() {
		return (this.reader == null);
	}

	@Autowired
	private void readFrom(InputStream in) throws PlatformException, com.nexacro17.xapi.tx.PlatformException {
		InputStream checkedIn = checkInputStream(in);
		if (checkedIn == null) {
			setData(new PlatformData());
			return;
		}
		int protocolTypeCount = getProtocolTypeCount();
		InputStream[] decoders = null;
		InputStream decoder = checkedIn;
		if (protocolTypeCount == 0)
			try {
				decoder = checkProtocolFilter(decoder);
				protocolTypeCount = getProtocolTypeCount();
			} catch (IOException ex) {
				if (this.log.isErrorEnabled())
					this.log.error("Checking ProtocolFilter failed", ex);
				throw new PlatformException("Checking ProtocolFilter failed", ex);
			}
		if (protocolTypeCount > 0) {
			decoders = new InputStream[protocolTypeCount];
			for (int i = 0; i < protocolTypeCount; i++) {
				String protocolType = getProtocolType(i);
				decoder = ProtocolFilterFactory.getDecoder(protocolType, decoder);
				decoders[i] = decoder;
				if (decoder == null)
					throw new PlatformException("Could not create ProtocolDecoder: " + protocolType);
			}
		}
		InputStream typeIn = checkDefaultContentType(decoder);
		String contentType = getCurrentContentType();
		DataDeserializer deserializer = DataSerializerFactory.getDeserializer(contentType);
		if (deserializer == null)
			throw new PlatformException("Could not create DataDeserializer: " + contentType);
		String emptyToNullKey = "deserializer.data.emptytonull";
		deserializer.setProperty(emptyToNullKey, getProperty(emptyToNullKey));
		setData(deserializer.readData((typeIn == null) ? decoder : typeIn, getDataTypeChanger(), getCurrentCharset()));
		if (PlatformGlobals.getBooleanProperty("platform.tx.PlatformRequest.ioclose", false))
			try {
				decoder.close();
			} catch (IOException ex) {
				if (this.log.isErrorEnabled())
					this.log.error("Closing InputStream failed", ex);
				throw new PlatformException("Closing InputStream failed", ex);
			}
		if (protocolTypeCount > 0)
			for (int i = 0; i < protocolTypeCount; i++) {
				((ProtocolDecoder) decoders[i]).end();
				if (this.log.isDebugEnabled())
					this.log.debug("Ended ProtocolDecoder: decoders[" + i + "]=" + decoders[i]);
			}
	}

	@Autowired(required = false)
	private void readFrom(Reader reader) throws PlatformException, com.nexacro17.xapi.tx.PlatformException {
		Reader checkedReader = checkReader(reader);
		if (checkedReader == null) {
			setData(new PlatformData());
			return;
		}
		int protocolTypeCount = getProtocolTypeCount();
		Reader[] decoders = null;
		Reader decoder = checkedReader;
		if (protocolTypeCount > 0) {
			decoders = new Reader[protocolTypeCount];
			for (int i = 0; i < protocolTypeCount; i++) {
				String protocolType = getProtocolType(i);
				decoder = ProtocolFilterFactory.getDecoder(protocolType, decoder);
				decoders[i] = decoder;
				if (decoder == null)
					throw new PlatformException("Could not create ProtocolDecoder: " + protocolType);
			}
		}
		Reader typeReader = checkDefaultContentType(decoder);
		String contentType = getCurrentContentType();
		DataDeserializer deserializer = DataSerializerFactory.getDeserializer(contentType);
		if (deserializer == null)
			throw new PlatformException("Could not create DataDeserializer: " + contentType);
		String emptyToNullKey = "deserializer.data.emptytonull";
		deserializer.setProperty(emptyToNullKey, getProperty(emptyToNullKey));
		setData(deserializer.readData((typeReader == null) ? decoder : typeReader, getDataTypeChanger(),
				getCurrentCharset()));
		if (PlatformGlobals.getBooleanProperty("platform.tx.PlatformRequest.ioclose", false))
			try {
				decoder.close();
			} catch (IOException ex) {
				if (this.log.isErrorEnabled())
					this.log.error("Closing Reader failed", ex);
				throw new PlatformException("Closing Reader failed", ex);
			}
		if (protocolTypeCount > 0)
			for (int i = 0; i < protocolTypeCount; i++) {
				((ProtocolDecoder) decoders[i]).end();
				if (this.log.isDebugEnabled())
					this.log.debug("Ended ProtocolDecoder: decoders[" + i + "]=" + decoders[i]);
			}
	}

	private InputStream checkDefaultContentType(InputStream in) throws PlatformException {
		String currContentType = getContentType();
		if (currContentType == null || "".equals(currContentType))
			try {
				int count = 4;
				byte[] buffer = new byte[count];
				int offset = 0;
				while (true) {
					int n = in.read(buffer, offset, count - offset);
					if (n == -1)
						break;
					if (n == 0) {
						if (this.log.isDebugEnabled())
							this.log.debug(
									"Check Default Content-Type: n=" + n + ", offset=" + offset + ", count=" + count);
						continue;
					}
					offset += n;
					if (offset == count)
						break;
				}
				if (offset == 0) {
					if (this.log.isDebugEnabled())
						this.log.debug("Check Default Content-Type: offset=" + offset);
					return null;
				}
				if (offset == count) {
					String head = new String(buffer);
					if (head.equalsIgnoreCase("SSV:")) {
						setContentType("PlatformSsv");
					} else if (head.equalsIgnoreCase("<?xm")) {
						setContentType("PlatformXml");
					} else {
						setContentType("PlatformBinary");
					}
					return new SequenceInputStream(new ByteArrayInputStream(buffer), in);
				}
				throw new PlatformException("Check Default Content-Type failed: offset=" + offset);
			} catch (EOFException ex) {
				if (this.log.isDebugEnabled())
					this.log.debug("Check Default Content-Type: ex=" + ex);
				throw new PlatformException("Check Default Content-Type: ex=", ex);
			} catch (IOException ex) {
				if (this.log.isErrorEnabled())
					this.log.error("Check Default Content-Type failed", ex);
				throw new PlatformException("Check Default Content-Type failed", ex);
			}
		return null;
	}

	private Reader checkDefaultContentType(Reader in) throws PlatformException {
		String currContentType = getContentType();
		if (currContentType == null || "".equals(currContentType))
			try {
				int count = 4;
				char[] buffer = new char[count];
				int offset = 0;
				while (true) {
					int n = in.read(buffer, offset, count - offset);
					if (n == -1)
						break;
					if (n == 0) {
						if (this.log.isDebugEnabled())
							this.log.debug(
									"Check Default Content-Type: n=" + n + ", offset=" + offset + ", count=" + count);
						continue;
					}
					offset += n;
					if (offset == count)
						break;
				}
				if (offset == 0) {
					if (this.log.isDebugEnabled())
						this.log.debug("Check Default Content-Type: offset=" + offset);
					return null;
				}
				if (offset == count) {
					String head = new String(buffer);
					if (head.equalsIgnoreCase("SSV:")) {
						setContentType("PlatformSsv");
					} else if (head.equalsIgnoreCase("<?xm")) {
						setContentType("PlatformXml");
					} else {
						setContentType("PlatformBinary");
					}
					return (Reader) new SequenceReader(new CharArrayReader(buffer), in);
				}
				throw new PlatformException("Check Default Content-Type failed: offset=" + offset);
			} catch (EOFException ex) {
				if (this.log.isDebugEnabled())
					this.log.debug("Check Default Content-Type: ex=" + ex);
				throw new PlatformException("Check Default Content-Type: ex=", ex);
			} catch (IOException ex) {
				if (this.log.isErrorEnabled())
					this.log.error("Check Default Content-Type failed", ex);
				throw new PlatformException("Check Default Content-Type failed", ex);
			}
		return null;
	}

	private InputStream checkInputStream(InputStream in) throws PlatformException {
		try {
			int count = 4;
			byte[] buffer = new byte[count];
			int offset = 0;
			while (true) {
				int n = in.read(buffer, offset, count - offset);
				if (n == -1)
					break;
				if (n == 0) {
					if (this.log.isDebugEnabled())
						this.log.debug("Check InputStream: n=" + n + ", offset=" + offset + ", count=" + count);
					continue;
				}
				offset += n;
				if (offset == count)
					break;
			}
			if (offset == 0) {
				if (this.log.isDebugEnabled())
					this.log.debug("Check InputStream: offset=" + offset);
				return null;
			}
			if (offset == count)
				return new SequenceInputStream(new ByteArrayInputStream(buffer), in);
			throw new PlatformException("Checking InputStream failed: offset=" + offset);
		} catch (EOFException ex) {
			if (this.log.isDebugEnabled())
				this.log.debug("Check InputStream: ex=" + ex);
			return null;
		} catch (IOException ex) {
			if (this.log.isErrorEnabled())
				this.log.error("Checking InputStream failed", ex);
			throw new PlatformException("Checking InputStream failed", ex);
		}
	}

	private Reader checkReader(Reader in) throws PlatformException {
		try {
			int count = 8;
			char[] buffer = new char[count];
			int offset = 0;
			while (true) {
				int n = in.read(buffer);
				if (n == -1)
					break;
				if (n == 0) {
					if (this.log.isDebugEnabled())
						this.log.debug("Check Reader: n=" + n + ", offset=" + offset + ", count=" + count);
					continue;
				}
				offset += n;
				if (offset == count)
					break;
			}
			if (offset == 0) {
				if (this.log.isDebugEnabled())
					this.log.debug("Check Reader: offset=" + offset);
				return null;
			}
			if (offset == 8)
				return (Reader) new SequenceReader(new CharArrayReader(buffer), in);
			throw new PlatformException("Checking Reader failed: offset=" + offset);
		} catch (IOException ex) {
			if (this.log.isErrorEnabled())
				this.log.error("Checking Reader failed", ex);
			throw new PlatformException("Checking Reader failed", ex);
		}
	}

	private InputStream checkProtocolFilter(InputStream in) throws IOException {
		byte[] signature = new byte[2];
		int n = in.read(signature);
		if (n == 2) {
			if (signature[0] == -1 && signature[1] == -83) {
				if (this.log.isDebugEnabled())
					this.log.debug("Adding protocol type: PlatformZlib");
				addProtocolType("PlatformZlib");
			}
		} else {
			throw new IOException("Checking protocol filter failed: n=" + n);
		}
		return new SequenceInputStream(new ByteArrayInputStream(signature), in);
	}

	private void checkStreamLog() {
		if (this.streamLog == null)
			this.streamLog = new StreamLog();
	}

	private void checkGlobalStreamLog() {
		if (this.streamLog == null) {
			boolean isEnabled = PlatformGlobals.getBooleanProperty("platform.tx.PlatformRequest.streamlog.enabled",
					false);
			if (isEnabled)
				this.streamLog = createGlobalStreamLog();
		}
	}

	private void checkStream() throws PlatformException {
		if (this.in == null && this.reader == null)
			throw new PlatformException("InputStream and Reader are null");
	}

	private StreamLog createGlobalStreamLog() {
		String dir = PlatformGlobals.getStringProperty("platform.tx.PlatformRequest.streamlog.dir");
		int maxSize = PlatformGlobals.getIntProperty("platform.tx.PlatformRequest.streamlog.maxsize", -1);
		String prefix = PlatformGlobals.getStringProperty("platform.tx.PlatformRequest.streamlog.prefix");
		String suffix = PlatformGlobals.getStringProperty("platform.tx.PlatformRequest.streamlog.suffix");
		boolean isExceededSize = PlatformGlobals
				.getBooleanProperty("platform.tx.PlatformRequest.streamlog.exceededsize", false);
		StreamLog log = new StreamLog();
		log.setLogEnabled(true);
		log.setLogDir(dir);
		log.setLogMaxSize(maxSize);
		log.setLogPrefix(prefix);
		log.setLogSuffix(suffix);
		log.setExceededSize(isExceededSize);
		return log;
	}

	public PlatformRequest() {
	}
}
