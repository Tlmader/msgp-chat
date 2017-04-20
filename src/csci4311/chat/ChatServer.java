package csci4311.chat;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.Executors;

/**
 * Implements methods for handling core functionality of maintaining users, groupUsers, messages, etc.
 *
 * @author Ted Mader
 */
public class ChatServer implements MessageServer {

  static final int PORT = 1337;

  private Map<String, HashSet<String>> groupUsers = new HashMap<>();
  private Map<String, List<MsgpMessage>> groupHistory = new HashMap<>();
  private Map<String, PrintStream> userStreams = new HashMap<>();

  int connect(String user, PrintStream ps) {
    userStreams.put(user, ps);
    return 200;
  }

  @Override
  public int join(String user, String group) {
    if (!userStreams.containsKey(user)) {
      return 400;
    }
    if (!groupUsers.containsKey(group)) {
      groupUsers.put(group, new HashSet<>());
      groupHistory.put(group, new ArrayList<>());
    }
    if (groupUsers.get(group).add(user)) {
      return 200;
    }
    return 201;
  }

  @Override
  public int leave(String user, String group) {
    if (!groupUsers.containsKey(group)) {
      return 400;
    }
    if (groupUsers.get(group).remove(user)) {
      return 200;
    }
    return 201;
  }

  @Override
  public int send(MsgpMessage message) {
    if (!userStreams.containsKey(message.getFrom())) {
      return 400;
    }
    for (String to : message.getTo()) {
      if ((to.startsWith("@") && !userStreams.containsKey(to.substring(1))) ||
          (to.startsWith("#") && !groupUsers.containsKey(to.substring(1)))) {
        return 400;
      }
    }
    for (String to : message.getTo()) {
      if (to.startsWith("@") && !to.substring(1).equals(message.getFrom())) {
        PrintStream ps = userStreams.get(to.substring(1));
        ps.println(message.getMessage());
        ps.close();
      } else if (to.startsWith("#")) {
        groupHistory.get(to.substring(1)).add(message);
      }
    }
    return 200;
  }

  @Override
  public Set<String> groups() {
    return groupUsers.keySet();
  }

  @Override
  public Set<String> users(String group) {
    return groupUsers.get(group);
  }

  @Override
  public List<MsgpMessage> history(String group) {
    return groupHistory.get(group);
  }

  public static void main(String[] args) throws IOException {
    InetSocketAddress addr = new InetSocketAddress(PORT);
    HttpServer server = HttpServer.create(addr, 0);
    MsgpServer msgp = new TextMsgpServer();
    server.createContext("/", msgp::connect);
    server.createContext("/join", msgp::join);
    server.createContext("/leave", msgp::leave);
    server.createContext("/send", msgp::send);
    server.createContext("/groups", msgp::groups);
    server.createContext("/users", msgp::users);
    server.createContext("/history", msgp::history);
    server.setExecutor(Executors.newCachedThreadPool());
    server.start();
    System.out.println("Server is listening on port " + PORT + "...");
  }
}
