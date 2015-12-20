package com.theaigames.engine.io;

import java.io.IOException;

/**
 * Behavior specification of an IO player.
 *
 * The set of methods was extracted out of {@link IOPlayer} except for
 * {@link #setResponse(String)}. It is rather ugly but works with minimum
 * changes to other code.
 *
 * @author Matthias Neugebauer
 */
public interface IOPlayerable extends Runnable {
  void writeToBot(String line) throws IOException;
  void outputEngineWarning(String warning);
  String getResponse(long timeOut);
  void setResponse(String response);

  String getStdout();
  String getStderr();
  void addToDump(String dumpy);

  String getDump();

  String getIdString();

  void finish();
}
