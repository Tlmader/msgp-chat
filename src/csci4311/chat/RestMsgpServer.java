package csci4311.chat;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Set;

/**
 * Implements RESTful methods for sending and receiving of encoded messages.
 *
 * @author Ted Mader
 * @since 2017-04-30
 */
public class RestMsgpServer implements MsgpServer {

  private final ChatServer server;

  RestMsgpServer(ChatServer server) {
    this.server = server;
  }

  @Override
  public void root(HttpExchange exchange) throws IOException {
    handle(exchange, 404);
  }

  @Override
  public void connect(HttpExchange exchange) throws IOException {

  }

  @Override
  public void join(HttpExchange exchange) throws IOException {

  }

  @Override
  public void leave(HttpExchange exchange) throws IOException {

  }

  @Override
  public void send(HttpExchange exchange) throws IOException {

  }

  @Override
  public void groups(HttpExchange exchange) throws IOException {
    if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
      handle(exchange, 404);
      return;
    }
    handle(exchange, setToJSON(server.groups(), "groups"));
  }

  @Override
  public void users(HttpExchange exchange) throws IOException {
    if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
      handle(exchange, 404);
      return;
    }
    handle(exchange, setToJSON(server.users(), "users"));
  }

  public void usersByGroup(HttpExchange exchange) throws IOException {
    if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
      handle(exchange, 404);
      return;
    }
    String query = exchange.getRequestURI().getPath();
    String group = query.substring(query.lastIndexOf('/') + 1);
    Set<String> userSet = server.users(group);
    if (userSet != null) {
      handle(exchange, setToJSON(server.users(group), "users"));
    } else {
      handle(exchange, 400);
    }
  }

  @Override
  public void history(HttpExchange exchange) throws IOException {

  }

  private void handle(HttpExchange exchange, int code) throws IOException {
    Headers responseHeaders = exchange.getResponseHeaders();
    responseHeaders.set("Content-Type", "application/x-www-form-urlencoded");
    exchange.sendResponseHeaders(code, 0);
    new PrintStream(exchange.getResponseBody()).close();
  }

  private void handle(HttpExchange exchange, String json) throws IOException {
    Headers responseHeaders = exchange.getResponseHeaders();
    responseHeaders.set("Content-Type", "application/json");
    exchange.sendResponseHeaders(200, 0);
    PrintStream response = new PrintStream(exchange.getResponseBody());
    response.println(json);
    response.close();
  }

  private String setToJSON(Set<String> set, String key) {
    StringBuilder json = new StringBuilder();
    json.append("[{\"")
        .append(key)
        .append("\":[");
    String prefix = "";
    if (set != null) {
      for (String e : set) {
        json.append(prefix)
            .append("\"")
            .append(e)
            .append("\"");
        prefix = ",";
      }
    }
    json.append("]}]");
    return json.toString();
  }
}
