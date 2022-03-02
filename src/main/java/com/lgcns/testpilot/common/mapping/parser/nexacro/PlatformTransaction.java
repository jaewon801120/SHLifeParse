package com.lgcns.testpilot.common.mapping.parser.nexacro;

import com.nexacro17.xapi.data.DataSet;
import com.nexacro17.xapi.data.PlatformData;
import com.nexacro17.xapi.license.InvalidLicenseException;
import com.nexacro17.xapi.license.License;
import com.nexacro17.xapi.tx.DataTypeChanger;
import com.nexacro17.xapi.util.StringUtils;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

abstract class PlatformTransaction {
	private Log log = LogFactory.getLog(PlatformTransaction.class);

	private static final boolean CHECK_LICENSE = true;

	static final String USER_CONTENT_TYPE = "__contentType__";

	static final String USER_CHARSET = "__charset__";

	static final String USER_PROTOCOL_TYPE = "__protocolType__";

	static final String USER_VERBOSE = "__verbose__";

	static final String HTTP_DATA = "httpData";

	static final String HTTP_DATA_COUNT = "httpDataCount";

	private boolean checkLicense = true;

	private String contentType;

	private String charset;

	private List protocolTypes;

	private PlatformData data;

	private DataTypeChanger dataTypeChanger;

	private boolean verbose;

	private Map properties;

	protected PlatformTransaction(String contentType) {
		this(contentType, null);
	}

	protected PlatformTransaction(String contentType, String charset) {
		this.contentType = contentType;
		this.charset = charset;
	}

	public String getContentType() {
		return this.contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getCharset() {
		return this.charset;
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}

	public String getProtocolType(int index) {
		int userCount = getUserProtocolTypeCount();
		if (userCount == -1)
			return (this.protocolTypes == null) ? null : this.protocolTypes.get(index).toString();
		return getUserProtocolType(index);
	}

	public void addProtocolType(String protocolType) {
		if (this.protocolTypes == null)
			this.protocolTypes = new ArrayList();
		if (this.protocolTypes.contains(protocolType))
			throw new IllegalArgumentException("Duplicate protocol type: " + protocolType);
		this.protocolTypes.add(protocolType);
	}

	public void removeProtocolType(int index) {
		this.protocolTypes.remove(index);
	}

	public void removeProtocolType(String protocolType) {
		this.protocolTypes.remove(protocolType);
	}

	public boolean containsProtocolType(String protocolType) {
		return (this.protocolTypes == null) ? false : this.protocolTypes.contains(protocolType);
	}

	public void clearProtocolTypes() {
		if (getProtocolTypeCount() > 0)
			this.protocolTypes.clear();
	}

	public int getProtocolTypeCount() {
		int userCount = getUserProtocolTypeCount();
		if (userCount == -1)
			return (this.protocolTypes == null) ? 0 : this.protocolTypes.size();
		return userCount;
	}

	public PlatformData getData() {
		return this.data;
	}

	public void setData(PlatformData data) {
		this.data = data;
	}

	public DataTypeChanger getDataTypeChanger() {
		return this.dataTypeChanger;
	}

	public void setDataTypeChanger(DataTypeChanger dataTypeChanger) {
		this.dataTypeChanger = dataTypeChanger;
	}

	public boolean isVerbose() {
		return this.verbose;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	public Object getProperty(String key) {
		return (this.properties == null) ? null : this.properties.get(key);
	}

	public void setProperty(String key, Object value) {
		if (this.properties == null)
			this.properties = new HashMap();
		this.properties.put(key, value);
	}

	protected String getCurrentContentType() {
		String contentType = getUserContentType();
		if (contentType != null) {
			contentType = contentType.trim();
			if (this.log.isDebugEnabled())
				try {
					this.log.debug("User content type: " + URLEncoder.encode(contentType, "UTF-8"));
				} catch (UnsupportedEncodingException e) {
					this.log.debug("UnsupportedEncodingException");
				}
		}
		if (contentType == null || "".equals(contentType)) {
			contentType = getContentType();
			if (contentType != null)
				contentType = contentType.trim();
			if (contentType == null || "".equals(contentType))
				contentType = getDefaultContentType();
		}
		return contentType;
	}

	protected String getCurrentCharset() {
		String charset = getUserCharset();
		if (charset != null) {
			charset = charset.trim();
			if (this.log.isDebugEnabled())
				try {
					this.log.debug("User charset: " + URLEncoder.encode(charset, "UTF-8"));
				} catch (UnsupportedEncodingException e) {
					this.log.debug("UnsupportedEncodingException");
				}
		}
		if (charset == null || "".equals(charset)) {
			charset = getCharset();
			if (charset != null)
				charset = charset.trim();
			if (charset == null || "".equals(charset))
				charset = getDefaultCharset();
		}
		return charset;
	}

	protected boolean isCurrentVerbose() {
		return (isVerbose() || isUserVerbose());
	}

	protected String getDefaultContentType() {
		return "PlatformXml";
	}

	protected String getDefaultCharset() {
		return "UTF-8";
	}

	boolean isCheckLicense() {
		return this.checkLicense;
	}

	void setCheckLicense(boolean checkLicense) {
		this.checkLicense = checkLicense;
	}

	void checkLicense() throws InvalidLicenseException {
		if (isInternalPackage())
			return;
		if (isCheckLicense())
			try {
				License license = License.getInstance(0);
				if (!license.isValidTime())
					throw new InvalidLicenseException(3, "License has expired");
			} catch (InvalidLicenseException ex) {
				// if (this.log.isErrorEnabled())
				// this.log.error("Invalid license", (Throwable)ex);
				// ex.printStackTrace();
				// throw ex;
			}
	}

	String toSimpleString() {
		String name = getClass().getName();
		int index = name.lastIndexOf('.');
		if (index == -1)
			return name + "@" + Integer.toHexString(hashCode());
		return name.substring(index + 1) + "@" + Integer.toHexString(hashCode());
	}

	private String getUserContentType() {
		return getUserData("__contentType__");
	}

	private String getUserCharset() {
		return getUserData("__charset__");
	}

	private String getUserProtocolType(int index) {
		if (getUserProtocolTypeCount() > 0) {
			List protocolTypeList = StringUtils.split(getUserProtocolTypes().trim(), '|');
			if (this.log.isDebugEnabled())
				this.log.debug("User protocol type: protocolTypeList=" + protocolTypeList);
			return (String) protocolTypeList.get(index);
		}
		return null;
	}

	private int getUserProtocolTypeCount() {
		String protocolType = getUserProtocolTypes();
		if (protocolType == null)
			return -1;
		protocolType = protocolType.trim();
		if ("".equals(protocolType))
			return 0;
		return StringUtils.split(protocolType, '|').size();
	}

	private String getUserProtocolTypes() {
		return getUserData("__protocolType__");
	}

	private boolean isUserVerbose() {
		return "true".equals(getUserData("__verbose__"));
	}

	private String getUserData(String name) {
		if (this.data == null)
			return null;
		String value = this.data.getVariableList().getString(name);
		if (value == null) {
			DataSet ds = this.data.getDataSet("httpData");
			if (ds == null || ds.getRowCount() == 0)
				return null;
			return ds.getString(0, name);
		}
		return value;
	}

	private boolean isInternalPackage() {
		String className = getClass().getName();
		int len = className.length();
		if (len > 20 && className.substring(0, 20).hashCode() == 2054388934)
			return true;
		if (len > 30 && className.substring(0, 30).hashCode() == 1422498297)
			return true;
		return false;
	}

	protected PlatformTransaction() {
	}
}
