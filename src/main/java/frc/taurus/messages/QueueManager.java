package frc.taurus.messages;

import java.util.ArrayList;
import java.util.List;

//TODO: get rid of QueueManager & QueueListener?
public class QueueManager {
  List<QueueListener> listeners = new ArrayList<>();

  public QueueManager() {
  }

  public void subscribe(QueueListener listener) {
    listeners.add(listener);
  }

  public void messageAdded() {
    for (QueueListener listener : listeners) {
      listener.newMessage();
    }
  }
}