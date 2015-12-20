package com.theaigames.engine;

import com.theaigames.engine.io.ThreadedIOPlayer;
import com.theaigames.engine.io.IOPlayerable;

import java.io.IOException;
import java.lang.reflect.Method;

/**
 * @author Matthias Neugebauer
 */
public class ThreadedEngine extends Engine {
  public void addPlayer(String className, String idString) throws IOException {
    System.out.println("Class: " + className);

    // Pull out main method of className
    Method mainMethod;
    try {
      Class<?> clazz = Class.forName(className);
      mainMethod = clazz.getMethod("main", String[].class);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    // Attach IO
    IOPlayerable player = new ThreadedIOPlayer(mainMethod, idString);

    // Add player
    getPlayers().add(player);

    // Start running
    player.run();
  }
}
