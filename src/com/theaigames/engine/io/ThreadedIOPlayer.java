package com.theaigames.engine.io;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.theaigames.blockbattle.Parameters;
import com.theaigames.blockbattle.ThreadedBlockbattle;

/**
 * Implementation of an IO Player which uses threads instead of processes.
 *
 * @author Matthias Neugebauer
 */
public class ThreadedIOPlayer implements IOPlayerable {
  private final static int MAX_ERRORS = 2;
  private final static String NULL_MOVE = "no_moves";
  public String response;
  private Thread thread = new Thread();
  private OutputStreamWriter inputStream;
  private InputStreamGobbler outputGobbler;
  // private InputStreamGobbler errorGobbler;
  private String idString;
  private StringBuilder dump = new StringBuilder();
  private int errorCounter = 0;
  private boolean finished = false;

  public ThreadedIOPlayer(Method mainMethod, String idString) throws IOException {
    this.idString = idString;

    // Connect output stream
    PipedOutputStream outOs = new PipedOutputStream();
    PipedInputStream outIs = new PipedInputStream(outOs);
    this.outputGobbler = new InputStreamGobbler(outIs, this, "output");

    // Connect error stream
    // PipedOutputStream errOs = new PipedOutputStream();
    // PipedInputStream errIs = new PipedInputStream(errOs);
    // this.errorGobbler = new InputStreamGobbler(errIs, this, "error");

    // Connect input stream
    PipedOutputStream inOs = new PipedOutputStream();
    PipedInputStream inIs = new PipedInputStream(inOs);
    this.inputStream = new OutputStreamWriter(inOs);

    // Create new Thread
    Runnable r = () -> {
      // Set streams as std
      ThreadedPrintStream.setThreadLocalSystemOut(new PrintStream(outOs));
      // ThreadedPrintStream.setThreadLocalSystemErr(new PrintStream(errOs));
      ThreadedInputStream.setThreadLocalSystemIn(inIs);

      // Run Bot
      try {
        if (ThreadedBlockbattle.parameters != null) {
          Parameters par = ThreadedBlockbattle.parameters;
          String b = String.valueOf(par.getBumpinessWeight());
          String c = String.valueOf(par.getCompletenessWeight());
          String h = String.valueOf(par.getHeightWeight());
          String o = String.valueOf(par.getHolesWeight());
          String[] args = {"-b", b, "-c", c, "-h", h, "-o", o};
          mainMethod.invoke(null, (Object) args);
        } else {
          String[] args = {};
          mainMethod.invoke(null, (Object) args);
        }
      } catch (InvocationTargetException e) {
        // Don't blow up when the thread was killed
        if (!(finished && e.getTargetException() instanceof ThreadDeath))
          e.printStackTrace();
      } catch (Exception e) {
        e.printStackTrace();
      }

      try {
        outOs.close();
        // errOs.close();
        inIs.close();
      } catch (Exception e) {
      }
    };

    this.thread = new Thread(r);
  }

  /**
   * Write a string to the bot
   *
   * @param line : input string
   * @throws IOException
   */
  public void writeToBot(String line) throws IOException {
    if (finished)
      return;
    try {
      inputStream.write(line + "\n");
      inputStream.flush();
    } catch (IOException e) {
      System.err.println("Writing to bot failed");
    }
    addToDump(line);
  }

  /**
   * Wait's until the response has a value and then returns that value
   *
   * @param timeOut : time before timeout
   * @return : bot's response, returns and empty string when there is no response
   */
  public String getResponse(long timeOut) {
    long timeStart = System.currentTimeMillis();
    String enginesays = "Output from your bot: ";
    String response;

    if (errorCounter > MAX_ERRORS) {
      addToDump(String.format("Maximum number (%d) of time-outs reached: skipping all moves.",
          MAX_ERRORS));
      return "";
    }

    while (this.response == null) {
      long timeNow = System.currentTimeMillis();
      long timeElapsed = timeNow - timeStart;

      if (timeElapsed >= timeOut) {
        addToDump(String.format(
            "Response timed out (%dms), let your bot return '%s' instead of nothing or make it faster.",
            timeOut, NULL_MOVE));
        errorCounter++;
        if (errorCounter > MAX_ERRORS)
          finish();
        addToDump(String.format("%snull", enginesays));
        return "";
      }

      try {
        Thread.sleep(2);
      } catch (InterruptedException e) {
      }
    }

    if (this.response.equalsIgnoreCase("no_moves")) {
      this.response = null;
      addToDump(String.format("%s\"%s\"", enginesays, NULL_MOVE));
      return "";
    }

    response = this.response;
    this.response = null;

    addToDump(String.format("%s\"%s\"", enginesays, response));
    return response;
  }

  /**
   * Ends the bot process and it's communication
   */
  public void finish() {
    if (finished)
      return;

    finished = true;

    // we cannot set a finish variable but we also have not synchronized blocks.
    // #stop should be fine :)
    thread.stop();

    // stop the bot's IO
    try {
      inputStream.close();
    } catch (IOException e) {
    }
    outputGobbler.finish();
    // errorGobbler.finish();
  }

  /**
   * @return : String representation of the bot ID as used in the database
   */
  public String getIdString() {
    return idString;
  }

  /**
   * Adds a string to the bot dump
   *
   * @param dumpy : string to add to the dump
   */
  public void addToDump(String dumpy) {
    dump.append(dumpy).append("\n");
  }

  /**
   * Add a warning to the bot's dump that the engine outputs
   *
   * @param warning : the warning message
   */
  public void outputEngineWarning(String warning) {
    dump.append(String.format("Engine warning: \"%s\"\n", warning));
  }

  /**
   * @return : the complete stdOut from the bot process
   */
  public String getStdout() {
    return outputGobbler.getData();
  }

  /**
   * @return : the complete stdErr from the bot process
   */
  public String getStderr() {
    return null;
    // return errorGobbler.getData();
  }

  /**
   * @return : the dump of all the IO
   */
  public String getDump() {
    return dump.toString();
  }

  @Override
  /**
   * Start the communication with the bot
   */
  public void run() {
    outputGobbler.start();
    // errorGobbler.start();
    thread.start();
  }

  public void setResponse(String response) {
    this.response = response;
  }
}
