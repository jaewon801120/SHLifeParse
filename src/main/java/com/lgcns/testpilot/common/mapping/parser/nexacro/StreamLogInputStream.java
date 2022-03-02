package com.lgcns.testpilot.common.mapping.parser.nexacro;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

class StreamLogInputStream extends InputStream {
  private InputStream in;
  
  private StreamLog streamLog;
  
  private ByteArrayOutputStream buffer;
  
  public StreamLogInputStream(InputStream in, StreamLog streamLog) {
    this.in = in;
    this.streamLog = streamLog;
  }
  
  public int read() throws IOException {
    int b = this.in.read();
    if (b != -1 && 
      checkBuffer(1))
      this.buffer.write(b); 
    return b;
  }
  
  public int read(byte[] b) throws IOException {
    return read(b, 0, b.length);
  }
  
  public int read(byte[] b, int off, int len) throws IOException {
    int n = this.in.read(b, off, len);
    if (n > 0 && 
      checkBuffer(n))
      this.buffer.write(b, off, n); 
    return n;
  }
  
  public long skip(long n) throws IOException {
    if (n <= 0L)
      return 0L; 
    long remaining = n;
    byte[] skipBuffer = new byte[2048];
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
  
  public int available() throws IOException {
    return this.in.available();
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
    FileOutputStream out = new FileOutputStream(file);
    try {
      if (this.buffer == null) {
        if (this.in == null) {
          out.write("The buffer does not exist. in=null".getBytes());
        } else if (this.in.available() > 0) {
          copy(this.in, out);
        } else {
          out.write("The buffer does not exist. in.available()=0".getBytes());
        } 
      } else {
        if (log.isDebugEnabled())
          log.debug("buffer=" + this.buffer.size()); 
        out.write(this.buffer.toByteArray());
        if (this.streamLog.isExceededSize()) {
          int maxSize = this.streamLog.getLogMaxSize();
          if (log.isDebugEnabled())
            log.debug("The buffer was exceeded. maxSize=" + maxSize); 
          out.write(System.getProperty("line.separator").getBytes());
          out.write(("The buffer was exceeded. maxSize=" + maxSize).getBytes());
        } else if (this.in.available() > 0) {
          copy(this.in, out);
        } 
      } 
    } finally {
      out.close();
    } 
  }
  
  private long copy(InputStream in, OutputStream out) throws IOException {
    byte[] buffer = new byte[4096];
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
      this.buffer = new ByteArrayOutputStream(); 
    if (this.buffer.size() + length > this.streamLog.getLogMaxSize()) {
      this.streamLog.setExceededSize(true);
      return false;
    } 
    return true;
  }
}
