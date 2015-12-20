package com.theaigames.engine.io;

// Copied from https://github.com/davidhoyt/hipchat-bot/blob/af3b1961a5d173f3447426468cd931f4d4432b7a/src/main/java/com/github/davidhoyt/ThreadPrintStream.java

// Courtesy:
//  http://maiaco.com/articles/java/threadOut.php
// Some modifications made to use InheritableThreadLocal instead of just ThreadLocal.

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * A ThreadedPrintStream replaces the normal System.out and ensures
 * that output to System.out goes to a different PrintStream for
 * each thread.  It does this by using ThreadLocal to maintain a
 * PrintStream for each thread.
 */
public class ThreadedPrintStream extends PrintStream {
  // Save the existing System.out
  public final static PrintStream stdout = System.out;
  public final static PrintStream stderr = System.err;

  private final static InheritableThreadLocal<PrintStream> out = new InheritableThreadLocal<PrintStream>() {
    @Override
    protected PrintStream initialValue() {
      return stdout;
    }
  };
  private final static InheritableThreadLocal<PrintStream> err = new InheritableThreadLocal<PrintStream>() {
    @Override
    protected PrintStream initialValue() {
      return stderr;
    }
  };

  private final OutputType type;

  private ThreadedPrintStream(OutputType type) {
    super(new ByteArrayOutputStream(0));
    this.type = type;
  }

  /**
   * Changes System.out and System.err to new ThreadPrintStreams.
   */
  public static void replaceSystemOutAndErr() {
    // Create a ThreadedPrintStream and install it as System.out and System.err
    final ThreadedPrintStream threadStdOut = new ThreadedPrintStream(OutputType.out);
    final ThreadedPrintStream threadStdErr = new ThreadedPrintStream(OutputType.err);

    // Use the original System.out / System.err as the current thread's System.out / System.err
    setThreadLocalSystemOut(stdout);
    setThreadLocalSystemErr(stderr);

    // Replace System.out and System.err
    System.setOut(threadStdOut);
    System.setErr(threadStdErr);
  }

  public static PrintStream getThreadLocalSystemOut() {
    return out.get();
  }

  public static void setThreadLocalSystemOut(final PrintStream out) {
    ThreadedPrintStream.out.set(out);
  }

  public static PrintStream getThreadLocalSystemErr() {
    return err.get();
  }

  public static void setThreadLocalSystemErr(final PrintStream err) {
    ThreadedPrintStream.err.set(err);
  }

  public PrintStream getThreadStream() {
    return type == OutputType.out
        ? out.get()
        : err.get();
  }

  @Override
  public boolean checkError() {
    return getThreadStream().checkError();
  }

  @Override
  public void write(byte[] buf, int off, int len) {
    getThreadStream().write(buf, off, len);
  }

  @Override
  public void write(int b) {
    getThreadStream().write(b);
  }

  @Override
  public void flush() {
    getThreadStream().flush();
  }

  @Override
  public void close() {
    getThreadStream().close();
  }
}
