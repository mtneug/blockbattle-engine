package com.theaigames.blockbattle;

import com.theaigames.engine.ThreadedEngine;
import com.theaigames.engine.io.IOPlayerable;
import com.theaigames.engine.io.ThreadedInputStream;
import com.theaigames.engine.io.ThreadedPrintStream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Matthias Neugebauer
 */
public class ThreadedBlockbattle extends Blockbattle {
  public static Parameters parameters; //TODO refactor global parameters variable

  /**
   * Partially sets up the engine
   * 
   * @param args : command line arguments passed on running of application
   * @throws IOException
   * @throws RuntimeException
   */
  @Override
  public void setupEngine(String args[]) throws IOException, RuntimeException {
    // create engine
    this.engine = new ThreadedEngine();

    // add the test bots if in DEV_MODE
    if (DEV_MODE) {
      if (TEST_BOT.isEmpty())
        throw new RuntimeException(
            "DEV_MODE: Please provide a command to start the test bot by setting 'TEST_BOT' in your main class.");

      if (NUM_TEST_BOTS <= 0)
        throw new RuntimeException(
            "DEV_MODE: Please provide the number of bots in this game by setting 'NUM_TEST_BOTS' in your main class.");

      for (int i = 0; i < NUM_TEST_BOTS; i++)
        this.engine.addPlayer(TEST_BOT, "ID_" + i);

      return;
    }

    // add the bots from the arguments if not in DEV_MODE
    List<String> botDirs = new ArrayList<>();
    List<String> botIds = new ArrayList<>();

    if (args.length <= 0)
      throw new RuntimeException("No arguments provided.");

    for (int i = 0; i < args.length; i++) {
      botIds.add(i + "");
      botDirs.add(args[i]);
    }

    // check if the starting arguments are passed correctly
    if (botIds.isEmpty() || botDirs.isEmpty() || botIds.size() != botDirs.size())
      throw new RuntimeException("Missing some arguments.");

    // add the players
    for (int i = 0; i < botIds.size(); i++)
      this.engine.addPlayer(botDirs.get(i), botIds.get(i));
  }

  @Override
  public void finish() throws Exception {
    // stop the bots
    for (IOPlayerable ioPlayer : this.engine.getPlayers())
      ioPlayer.finish();

    if (DEV_MODE) { // print the game file when in DEV_MODE
      String playedGame = this.processor.getPlayedGame();
      System.out.println(playedGame);
    } else { // save the game to database
      try {
        this.saveGame();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    System.out.println("Done.");

  }

  public static void main(String args[]) throws Exception {
    ThreadedPrintStream.replaceSystemOutAndErr();
    ThreadedInputStream.replaceSystemIn();
    ThreadedBlockbattle game = new ThreadedBlockbattle();

    // DEV_MODE settings
    game.TEST_BOT = "bot.BotStarter";
    game.NUM_TEST_BOTS = 2;
    game.DEV_MODE = false;

    game.setupEngine(args);
    game.runEngine();
  }
}
