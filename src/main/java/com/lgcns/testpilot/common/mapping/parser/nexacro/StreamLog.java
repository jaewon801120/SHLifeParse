package com.lgcns.testpilot.common.mapping.parser.nexacro;

import com.nexacro17.xapi.util.DatetimeFormat;
import java.io.File;
import java.util.Date;

class StreamLog {
  private static final int KILO = 1024;
  
  private static final int MEGA = 1024000;
  
  public static final int IO_BUFFER_SIZE = 4096;
  
  public static final int SKIP_BUFFER_SIZE = 2048;
  
  private static final String DEFAULT_LOG_DIR = ".";
  
  private static final int DEFAULT_LOG_MAX_SIZE = 4096000;
  
  private static final String DEFAULT_LOG_PREFIX = "xapi";
  
  private static final String DEFAULT_LOG_SUFFIX = "req";
  
  private DatetimeFormat dateFormat = new DatetimeFormat();
  
  private boolean isLogEnabled;
  
  private String logDir;
  
  private int logMaxSize;
  
  private String logPrefix;
  
  private String logSuffix;
  
  private boolean isExceededSize;
  
  public boolean isLogEnabled() {
    return this.isLogEnabled;
  }
  
  public void setLogEnabled(boolean isLogEnabled) {
    this.isLogEnabled = isLogEnabled;
  }
  
  public String getLogDir() {
    if (isEmpty(this.logDir))
      return "."; 
    int len = this.logDir.length();
    char ch = this.logDir.charAt(len - 1);
    String dir = (ch == '/' || ch == '\\') ? this.logDir.substring(0, len - 1) : this.logDir;
    return (File.separatorChar == '/') ? dir.replace('\\', '/') : dir.replace('/', '\\');
  }
  
  public void setLogDir(String logDir) {
    this.logDir = logDir;
  }
  
  public String getLogPath() {
    String dir = getLogDir();
    String prefix = getLogPrefix();
    String suffix = getLogSuffix();
    String file = prefix + "_" + getCurrentDateString() + "." + suffix;
    return dir + File.separator + file;
  }
  
  public int getLogMaxSize() {
    return (this.logMaxSize <= 0) ? 4096000 : this.logMaxSize;
  }
  
  public void setLogMaxSize(int logMaxSize) {
    this.logMaxSize = logMaxSize;
  }
  
  public String getLogPrefix() {
    return isEmpty(this.logPrefix) ? "xapi" : this.logPrefix;
  }
  
  public void setLogPrefix(String logPrefix) {
    this.logPrefix = logPrefix;
  }
  
  public String getLogSuffix() {
    return isEmpty(this.logSuffix) ? "req" : this.logSuffix;
  }
  
  public void setLogSuffix(String logSuffix) {
    this.logSuffix = logSuffix;
  }
  
  public boolean isExceededSize() {
    return this.isExceededSize;
  }
  
  public void setExceededSize(boolean isExceededSize) {
    this.isExceededSize = isExceededSize;
  }
  
  private String getCurrentDateString() {
    return getDateString(System.currentTimeMillis());
  }
  
  private String getDateString(long time) {
    Date date = new Date(time);
    StringBuffer buffer = new StringBuffer();
    this.dateFormat.setFormat(2);
    buffer.append(this.dateFormat.format(date));
    buffer.append('_');
    this.dateFormat.setFormat(3);
    buffer.append(this.dateFormat.format(date));
    return buffer.toString();
  }
  
  private boolean isEmpty(String str) {
    return (str == null || str.trim().length() == 0);
  }
}
