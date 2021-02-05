package frc.taurus.messages;

import java.util.Optional;

public class MessageQueue<T> extends QueueManager {

  private T[] buffer; // storage array (circular)
  private int capacity; // capacity (size of buffer)
  private int back = 0; // index of last (youngest) element in array

  private static final int defaultQueueSize = 512;

  /**
   * Constructor that creates a queue with the default size .
   */
  public MessageQueue() {
    this(defaultQueueSize);
  }

  /**
   * Constructor that creates a queue with the specified size
   * 
   * @param size the size of the queue (cannot be changed)
   */
  @SuppressWarnings("unchecked")
  public MessageQueue(final int size) {
    if (size <= 0) {
      throw new IllegalArgumentException("The size must be greater than 0");
    }
    buffer = (T[]) new Object[size];
    capacity = buffer.length;
  }

  /**
   * Returns the maximum size of the queue
   * 
   * @return this queue's capacity
   */
  public int capacity() {
    return capacity;
  }

  /**
   * Returns index of front of queue. Index monotonically increases, even when it
   * exceeds the capacity of the underlying storage buffer.
   * 
   * @return index of front of queue.
   */
  public synchronized int front() {
    return Math.max(back, capacity) - capacity;
  }

  /**
   * Returns index of front of queue. Index monotonically increases, even when it
   * exceeds the capacity of the underlying storage buffer.
   * 
   * @return index of front of queue.
   */
  public synchronized int back() {
    return back;
  }

  public synchronized void clear() {
    back = 0;
  }

  public synchronized void write(final T element) {
    buffer[back % capacity] = null; // dereference for garbage collection. Not sure this is necessary
    buffer[back % capacity] = element;
    back++;

    messageAdded();  // call update() function of all listeners
  }

  public synchronized Optional<T> read(QueueReader reader) {
    // make sure idx is in the bounds of valid data
    if (reader.nextReadIndex >= back) {
      // idx has moved too far forward. Message has not yet been written.
      reader.nextReadIndex = back;
      return Optional.empty();
    }

    if (reader.nextReadIndex < front()) {
      // idx is too far back. This data has already been overwritten.
      reader.nextReadIndex = front();
    }

    T element = buffer[reader.nextReadIndex % capacity];
    reader.nextReadIndex++;

    return Optional.of(element);
  }

  public synchronized Optional<T> readLast() {
    if (back == 0) {
      // nothing written yet
      return Optional.empty();
    }
    T element = buffer[(back - 1) % capacity];
    return Optional.of(element);
  }

  public QueueReader makeReader() {
    return new QueueReader(this);
  }




  public class QueueReader {
    MessageQueue<T> mParent;
    int nextReadIndex;

    protected QueueReader(MessageQueue<T> parent) {
      mParent = parent;
    }

    /**
     * Get current size of queue
     * 
     * @return numer of elements not yet read out of queue
     */
    public int size() {
      return (mParent.back() - nextReadIndex);
    }

    /**
     * Check if queue is empty
     * 
     * @return true if all elements have been read out of queue
     */
    public boolean isEmpty() {
      return (mParent.back() == nextReadIndex);
    }

    /**
     * Read next element out of queue
     * 
     * @return next element
     */
    public Optional<T> read() {
      return mParent.read(this);
      // readIndex will be adjusted in this function
    }

    /**
     * Read last element placed in queue
     * 
     * @return last element
     */
    public Optional<T> readLast() {
      nextReadIndex = mParent.back;
      return mParent.readLast();
    }
  }
}