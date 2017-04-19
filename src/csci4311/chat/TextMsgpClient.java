package csci4311.chat;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Implements the msgp protocol.
 *
 * @author Ted Mader
 */
public class TextMsgpClient implements MsgpClient {

  private final String BASE_URL = "http://localhost:8080/";

  @Override
  public int join(String user, String group) {
    HashMap<String, String> body = new HashMap<>();
    body.put("user", user);
    body.put("group", group);
    return getResponseCode(createConnection("join", body));
  }

  @Override
  public int leave(String user, String group) {
    HashMap<String, String> body = new HashMap<>();
    body.put("user", user);
    body.put("group", group);
    return getResponseCode(createConnection("leave", body));
  }

  @Override
  public int send(MsgpMessage message) {
    return getResponseCode(createConnection("send", message));
  }

  @Override
  public String groups() {
    List<String> groups = getResponseAsList(createConnection("groups"));
    StringBuilder out = new StringBuilder();
    if (groups != null) {
      for (String group : groups) {
        List<String> users = getResponseAsList(createConnection("users", group));
        if (users == null) {
          continue;
        }
        out.append("#")
            .append(group)
            .append(" has ")
            .append(users.size())
            .append(" members\n");
      }
    }
    return out.toString();
  }

  @Override
  public String users(String group) {
    List<String> users = getResponseAsList(createConnection("users", group));
    StringBuilder out = new StringBuilder();
    if (users != null) {
      for (String user : users) {
        out.append("@")
            .append(user)
            .append("\n");
      }
    }
    return out.toString();
  }

  @Override
  public String history(String group) {
    return getResponseBody(createConnection("history", group));
  }

  private HttpURLConnection createConnection(String route) {
    URL url;
    HttpURLConnection connection = null;
    try {
      url = new URL(BASE_URL + route);
      connection = (HttpURLConnection) url.openConnection();
      connection.setRequestMethod("GET");
      connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
      connection.setRequestProperty("Content-Language", "en-US");
      connection.setUseCaches(false);
      connection.setDoInput(true);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return connection;
  }

  private HttpURLConnection createConnection(String route, Serializable obj) {
    HttpURLConnection connection = createConnection(route);
    if (connection == null) {
      return null;
    }
    try {
      connection.setRequestProperty("Content-Length", "" + Integer.toString(obj.toString().getBytes().length));
      connection.setDoOutput(true);
      ObjectOutputStream wr = new ObjectOutputStream(connection.getOutputStream());
      wr.writeObject(obj);
      wr.flush();
      wr.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return connection;
  }

  private int getResponseCode(HttpURLConnection connection) {
    try {
      return connection.getResponseCode();
    } catch (IOException e) {
      e.printStackTrace();
      return 0;
    } finally {
      if (connection != null) {
        connection.disconnect();
      }
    }
  }

  private String getResponseBody(HttpURLConnection connection) {
    try {
      InputStream is = connection.getInputStream();
      BufferedReader rd = new BufferedReader(new InputStreamReader(is));
      String line;
      StringBuilder response = new StringBuilder();
      while ((line = rd.readLine()) != null) {
        response.append(line).append('\n');
      }
      rd.close();
      return response.toString();

    } catch (Exception e) {
      e.printStackTrace();
      return null;
    } finally {
      if (connection != null) {
        connection.disconnect();
      }
    }
  }

  private List<String> getResponseAsList(HttpURLConnection connection) {
    try {
      InputStream is = connection.getInputStream();
      BufferedReader rd = new BufferedReader(new InputStreamReader(is));
      List<String> response = new ArrayList<>();
      String line;
      while ((line = rd.readLine()) != null) {
        if (line.startsWith("msgp")) {
          continue;
        }
        response.add(line);
      }
      return response;

    } catch (Exception e) {
      e.printStackTrace();
      return null;
    } finally {
      if (connection != null) {
        connection.disconnect();
      }
    }
  }
}