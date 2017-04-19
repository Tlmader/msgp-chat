package csci4311.chat;

import java.util.List;
import java.util.Set;

/**
 * Communication interface to be used by user agents to send messages and requests.
 */
public interface MsgpClient {

  /**
   * Encodes a user join request.
   *
   * @param user  user name
   * @param group group name
   * @return reply code, as per the spec
   */
  int join(String user, String group);

  /**
   * Encodes a user leave request.
   *
   * @param user  user name
   * @param group group name
   * @return reply code, as per the spec
   */
  int leave(String user, String group);

  /**
   * Encodes the sending of a message.
   *
   * @param message message content
   * @return reply code, as per the spec
   */

  int send(MsgpMessage message);

  /**
   * Requests the list of groups.
   *
   * @return existing groups; null of none
   */
  String groups();

  /**
   * Requests the users of a group.
   *
   * @param group group name
   * @return list of existing groups; null of none
   */
  Set<String> users(String group);

  /**
   * Requests the history of a group.
   *
   * @param group group name
   * @return list of all messages sent to the group; null of none
   */
  List<MsgpMessage> history(String group);
}
