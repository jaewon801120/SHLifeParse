package com.lgcns.testpilot.common.mapping.parser.nexacro;

import com.nexacro17.xapi.data.Debugger;
import com.nexacro17.xapi.tx.DataSerializer;
import com.nexacro17.xapi.tx.DataSerializerFactory;
import com.nexacro17.xapi.tx.ProtocolEncoder;
import com.nexacro17.xapi.tx.ProtocolFilterFactory;
import com.nexacro17.xapi.util.PlatformGlobals;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URLEncoder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PlatformResponse extends PlatformTransaction {
	private Log log = LogFactory.getLog(PlatformResponse.class);

	private static final String IO_CLOSE_KEY = "platform.tx.PlatformResponse.ioclose";

	private OutputStream out;

	private Writer writer;

	public PlatformResponse(String contentType) {
		this(contentType, (String) null);
	}

	public PlatformResponse(String contentType, String charset) {
		super(contentType, charset);
	}

	public PlatformResponse(PlatformRequest req) {
		this(req.getContentType(), req.getCharset());
		setCheckLicense(req.isCheckLicense());
	}

	public PlatformResponse(OutputStream out) {
		this(out, (String) null);
	}

	public PlatformResponse(OutputStream out, String contentType) {
		this(out, contentType, (String) null);
	}

	public PlatformResponse(OutputStream out, String contentType, String charset) {
		super(contentType, charset);
		this.out = out;
	}

	public PlatformResponse(OutputStream out, PlatformRequest req) {
		this(out, req.getContentType(), req.getCharset());
		setCheckLicense(req.isCheckLicense());
	}

	public PlatformResponse(Writer writer) {
		this(writer, (String) null);
	}

	public PlatformResponse(Writer writer, String contentType) {
		this(writer, contentType, (String) null);
	}

	public PlatformResponse(Writer writer, String contentType, String charset) {
		super(contentType, charset);
		this.writer = writer;
	}

	public PlatformResponse(Writer writer, PlatformRequest req) {
		this(writer, req.getContentType(), req.getCharset());
		setCheckLicense(req.isCheckLicense());
	}

	public void sendData() throws PlatformException, com.nexacro17.xapi.tx.PlatformException {
		checkLicense();
		boolean isVerbose = isCurrentVerbose();
		long startTime = System.currentTimeMillis();
		if (isVerbose) {
			if (this.log.isInfoEnabled())
				try {
					this.log.info("Sending data: " + URLEncoder.encode(toSimpleString(), "UTF-8") + ", contentType="
							+ ((getContentType() != null) ? URLEncoder.encode(getContentType(), "UTF-8") : null)
							+ ", charset="
							+ ((getCharset() != null) ? URLEncoder.encode(getCharset(), "UTF-8") : null));
				} catch (UnsupportedEncodingException e) {
					this.log.info("UnsupportedEncodingException");
				}
		} else if (this.log.isInfoEnabled()) {
			try {
				this.log.info("Sending data: " + URLEncoder.encode(toSimpleString(), "UTF-8") + ", contentType="
						+ ((getContentType() != null) ? URLEncoder.encode(getContentType(), "UTF-8") : null)
						+ ", charset=" + ((getCharset() != null) ? URLEncoder.encode(getCharset(), "UTF-8") : null)
						+ ", startTime=" + startTime);
			} catch (UnsupportedEncodingException e) {
				this.log.info("UnsupportedEncodingException");
			}
		}
		try {
			checkData();
			checkStream();
			if (useOutputStream()) {
				writeTo(getOutputStream());
			} else {
				writeTo(getWriter());
			}
		} catch (PlatformException ex) {
			throw ex;
		}
		long endTime = System.currentTimeMillis();
		long elapsedTime = endTime - startTime;
		if (isVerbose) {
			if (this.log.isInfoEnabled())
				this.log.info("Sent data: " + toSimpleString() + ", elapsedTime=" + elapsedTime);
		} else {
			if (this.log.isInfoEnabled())
				this.log.info(
						"Sent data: " + toSimpleString() + ", elapsedTime=" + elapsedTime + ", endTime=" + endTime);
			if (this.log.isDebugEnabled()) {
				Debugger debugger = new Debugger();
				this.log.debug(debugger.detail(getData()));
			}
		}
	}

	void writeTo(OutputStream out) throws PlatformException, com.nexacro17.xapi.tx.PlatformException {
		String contentType = getCurrentContentType();
		DataSerializer serializer = DataSerializerFactory.getSerializer(contentType);
		if (serializer == null)
			throw new PlatformException("Could not create DataSerializer: " + contentType);
		int protocolTypeCount = getProtocolTypeCount();
		OutputStream[] encoders = null;
		OutputStream encoder = out;
		if (protocolTypeCount > 0) {
			encoders = new OutputStream[protocolTypeCount];
			for (int i = 0; i < protocolTypeCount; i++) {
				String protocolType = getProtocolType(i);
				encoder = ProtocolFilterFactory.getEncoder(protocolType, encoder);
				encoders[i] = encoder;
				if (encoder == null)
					throw new PlatformException("Could not create ProtocolEncoder: " + protocolType);
			}
		}
		serializer.writeData(encoder, getData(), getDataTypeChanger(), getCurrentCharset());
		try {
			encoder.flush();
		} catch (IOException ex) {
			if (this.log.isErrorEnabled())
				this.log.error("Flushing OutputStream failed", ex);
			throw new PlatformException("Flushing OutputStream failed", ex);
		}
		if (PlatformGlobals.getBooleanProperty("platform.tx.PlatformResponse.ioclose", false))
			try {
				encoder.close();
			} catch (IOException ex) {
				if (this.log.isErrorEnabled())
					this.log.error("Closing OutputStream failed", ex);
				throw new PlatformException("Closing OutputStream failed", ex);
			}
		if (protocolTypeCount > 0)
			for (int i = 0; i < protocolTypeCount; i++) {
				((ProtocolEncoder) encoders[i]).end();
				if (this.log.isDebugEnabled())
					this.log.debug("Ended ProtocolEncoder: encoders[" + i + "]=" + encoders[i]);
			}
	}

	void writeTo(Writer writer) throws PlatformException, com.nexacro17.xapi.tx.PlatformException {
		String contentType = getCurrentContentType();
		DataSerializer serializer = DataSerializerFactory.getSerializer(contentType);
		if (serializer == null)
			throw new PlatformException("Could not create DataSerializer: " + contentType);
		int protocolTypeCount = getProtocolTypeCount();
		Writer[] encoders = null;
		Writer encoder = writer;
		if (protocolTypeCount > 0) {
			encoders = new Writer[protocolTypeCount];
			for (int i = 0; i < protocolTypeCount; i++) {
				String protocolType = getProtocolType(i);
				encoder = ProtocolFilterFactory.getEncoder(protocolType, encoder);
				encoders[i] = encoder;
				if (encoder == null)
					throw new PlatformException("Could not create ProtocolEncoder: " + protocolType);
			}
		}
		serializer.writeData(encoder, getData(), getDataTypeChanger(), getCurrentCharset());
		try {
			encoder.flush();
		} catch (IOException ex) {
			if (this.log.isErrorEnabled())
				this.log.error("Flushing Writer failed", ex);
			throw new PlatformException("Flushing Writer failed", ex);
		}
		if (PlatformGlobals.getBooleanProperty("platform.tx.PlatformResponse.ioclose", false))
			try {
				encoder.close();
			} catch (IOException ex) {
				if (this.log.isErrorEnabled())
					this.log.error("Closing Writer failed", ex);
				throw new PlatformException("Closing Writer failed", ex);
			}
		if (protocolTypeCount > 0)
			for (int i = 0; i < protocolTypeCount; i++) {
				((ProtocolEncoder) encoders[i]).end();
				if (this.log.isDebugEnabled())
					this.log.debug("Ended ProtocolEncoder: encoders[" + i + "]=" + encoders[i]);
			}
	}

	OutputStream getOutputStream() {
		return this.out;
	}

	void setOutputStream(OutputStream out) {
		this.out = out;
	}

	Writer getWriter() {
		return this.writer;
	}

	void setWriter(Writer writer) {
		this.writer = writer;
	}

	boolean useOutputStream() {
		return (this.writer == null);
	}

	private void checkData() throws PlatformException {
		if (getData() == null)
			throw new PlatformException("PlatformData is null");
	}

	private void checkStream() throws PlatformException {
		if (this.out == null && this.writer == null)
			throw new PlatformException("OutputStream and Writer are null");
	}

	public PlatformResponse() {
	}
}
