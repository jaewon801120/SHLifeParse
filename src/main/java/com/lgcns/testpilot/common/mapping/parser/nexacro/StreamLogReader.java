package com.lgcns.testpilot.common.mapping.parser.nexacro;

import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.net.URLEncoder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

class StreamLogReader extends Reader {
  private Reader in;
  
  private StreamLog streamLog;
  
  private CharArrayWriter buffer;
  
  public StreamLogReader(Reader in, StreamLog streamLog) {
    this.in = in;
    this.streamLog = streamLog;
  }
  
  public int read() throws IOException {
    int ch = this.in.read();
    if (ch != -1 && 
      checkBuffer(1))
      this.buffer.write(ch); 
    return ch;
  }
  
  public int read(char[] cbuf) throws IOException {
    return read(cbuf, 0, cbuf.length);
  }
  
  public int read(char[] cbuf, int off, int len) throws IOException {
    int n = this.in.read(cbuf, off, len);
    if (n > 0 && 
      checkBuffer(n))
      this.buffer.write(cbuf, off, n); 
    return n;
  }
  
  public long skip(long n) throws IOException {
    if (n <= 0L)
      return 0L; 
    long remaining = n;
    char[] skipBuffer = new char[2048];
    while (remaining > 0L) {
      int nr = this.in.read(skipBuffer, 0, (int)Math.min(2048L, remaining));
      if (nr < 0)
        break; 
      if (nr > 0 && 
        checkBuffer(nr))
        this.buffer.write(skipBuffer, 0, nr); 
      remaining -= nr;
    } 
    return n - remaining;
  }
  
  public boolean ready() throws IOException {
    return this.in.ready();
  }
  
  public void close() throws IOException {
    this.in.close();
  }
  
  public void storeStreamLog() throws IOException {
    Log log = LogFactory.getLog(StreamLogReader.class);
    String path = this.streamLog.getLogPath();
    File file = new File(path);
    boolean exists = file.exists();
    if (log.isDebugEnabled())
      log.debug("Storing stream: path=" + URLEncoder.encode(path, "UTF-8") + ", exists=" + exists); 
    if (exists) {
      try {
        Thread.sleep(5L);
      } catch (InterruptedException ex) {}
      path = this.streamLog.getLogPath();
      file = new File(path);
      if (log.isDebugEnabled())
        log.debug("Storing stream: path=" + URLEncoder.encode(path, "UTF-8")); 
    } 
    File parent = file.getParentFile();
    if (!parent.exists())
      parent.mkdirs(); 
    FileWriter out = new FileWriter(file);
    try {
      if (this.buffer == null) {
        if (this.in == null) {
          out.write("The buffer does not exist. in=null");
        } else if (this.in.ready()) {
          copy(this.in, out);
        } else {
          out.write("The buffer does not exist. in.ready()=false");
        } 
      } else {
        out.write(this.buffer.toCharArray());
        if (this.streamLog.isExceededSize()) {
          int maxSize = this.streamLog.getLogMaxSize();
          if (log.isDebugEnabled())
            log.debug("The buffer was exceeded. maxSize=" + maxSize); 
          out.write(System.getProperty("line.separator"));
          out.write("The buffer was exceeded. maxSize=" + maxSize);
        } else if (this.in.ready()) {
          copy(this.in, out);
        } 
      } 
    } finally {
      out.close();
    } 
  }
  
  private long copy(Reader in, Writer out) throws IOException {
    char[] buffer = new char[4096];
    long count = 0L;
    int n = 0;
    while (-1 != (n = in.read(buffer))) {
      out.write(buffer, 0, n);
      count += n;
    } 
    return count;
  }
  
  private boolean checkBuffer(int length) {
    if (this.streamLog.isExceededSize())
      return false; 
    if (this.buffer == null)
      this.buffer = new CharArrayWriter(); 
    if (this.buffer.size() + length > this.streamLog.getLogMaxSize()) {
      this.streamLog.setExceededSize(true);
      return false;
    } 
    return true;
  }
}
