package csci4311.chat;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import static java.lang.System.out;

/**
 * Implements user interaction.
 *
 * @author Ted Mader
 */
public class CLIUserAgent implements UserAgent {

  private MsgpClient client = new TextMsgpClient();
  private String user;

  @Override
  public void deliver(String message) {
    out.println("\n" + message);
    out.print("@" + user + " >> ");
    HttpURLConnection connection = client.connect(user);
    new DeliveryWorker(connection).start();
  }

  private void start() {
    HttpURLConnection connection = client.connect(user);
    new DeliveryWorker(connection).start();
    Scanner sc = new Scanner(System.in);
    while (connection != null) {
      out.print("@" + user + " >> ");
      String input = sc.nextLine();
      String[] inputArr = input.split(" ");
      switch (inputArr[0]) {
        case "join":
          out.println(inputArr.length == 2 ? client.join(user, inputArr[1]) : getUsage(inputArr[0]));
          break;
        case "leave":
          out.println(inputArr.length == 2 ? client.leave(user, inputArr[1]) : getUsage(inputArr[0]));
          break;
        case "groups":
          out.println(client.groups());
          break;
        case "users":
          out.println(inputArr.length == 2 ? client.users(inputArr[1]) : getUsage(inputArr[0]));
          break;
        case "send":
          List<String> to = new ArrayList<>();
          String message = null;
          for (int i = 1; i < inputArr.length; i++) {
            if (inputArr[i].startsWith("@") || inputArr[i].startsWith("#") && !inputArr[i].substring(1).equals(user)) {
              to.add(inputArr[i]);
            } else {
              message = String.join(" ", Arrays.copyOfRange(inputArr, i, inputArr.length));
            }
          }
          client.send(new MsgpMessage(user, to, message));
          break;
        case "history":
          out.println(inputArr.length == 2 ? client.history(inputArr[1]) : getUsage(inputArr[0]));
          break;
      }
    }
  }

  private String getUsage(String command) {
    return "usage: " + command + " <group>";
  }

  private class DeliveryWorker extends Thread {

    private HttpURLConnection connection;

    DeliveryWorker(HttpURLConnection connection) {
      this.connection = connection;
    }

    public void run() {
      try {
        BufferedReader rd = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String line = null;
        while (line == null) {
          if ((line = rd.readLine()) != null) {
            deliver(line);
          }
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  public static void main(String[] args) {
    CLIUserAgent agent = new CLIUserAgent();
    agent.user = args.length == 1 ? args[0] : "tlmader";
    agent.start();
  }
}