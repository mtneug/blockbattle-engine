package com.theaigames.engine.io;

import java.io.*;

/**
 * @author Matthias Neugebauer
 */
public class ThreadedInputStream extends FilterInputStream {
  // Save the existing System.in
  public final static InputStream stdin = System.in;

  private final static InheritableThreadLocal<InputStream> in = new InheritableThreadLocal<InputStream>() {
    @Override
    protected InputStream initialValue() {
      return stdin;
    }
  };

  private ThreadedInputStream() {
    super(null);
  }

  public static void replaceSystemIn() {
    // Create a ThreadedInputStream and install it as System.in
    final ThreadedInputStream threadStdin = new ThreadedInputStream();

    // Use the original System.in as the current thread's System.in
    setThreadLocalSystemIn(stdin);

    // Replace System.in
    System.setIn(threadStdin);
  }

  public static InputStream getThreadLocalSystemIn() {
    return in.get();
  }

  public static void setThreadLocalSystemIn(final InputStream in) {
    ThreadedInputStream.in.set(in);
  }

  @Override
  public boolean markSupported() {
    return getThreadLocalSystemIn().markSupported();
  }

  @Override
  public synchronized void reset() throws IOException {
    getThreadLocalSystemIn().reset();
  }

  @Override
  public synchronized void mark(int readlimit) {
    getThreadLocalSystemIn().mark(readlimit);
  }

  @Override
  public void close() throws IOException {
    getThreadLocalSystemIn().close();
  }

  @Override
  public int available() throws IOException {
    return getThreadLocalSystemIn().available();
  }

  @Override
  public long skip(long n) throws IOException {
    return getThreadLocalSystemIn().skip(n);
  }

  @Override
  public int read(byte[] b, int off, int len) throws IOException {
    return getThreadLocalSystemIn().read(b, off, len);
  }

  @Override
  public int read(byte[] b) throws IOException {
    return getThreadLocalSystemIn().read(b);
  }

  @Override
  public int read() throws IOException {
    return getThreadLocalSystemIn().read();
  }
}
