package csci4311.chat;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

/**
 * Implements methods for handling core functionality of maintaining users, groups, messages, etc.
 *
 * @author Ted Mader
 */
public class ChatServer implements MessageServer {

  private static final int PORT = 8080;

  public static void main(String[] args) throws IOException {
    InetSocketAddress addr = new InetSocketAddress(PORT);
    HttpServer server = HttpServer.create(addr, 0);
    MsgpServer msgp = new TextMsgpServer();
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
