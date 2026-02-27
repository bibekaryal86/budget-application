package budget.application.event;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class TransactionEventBus {

  private final List<TransactionEventSubscriber> subscribers = new CopyOnWriteArrayList<>();
  private final ExecutorService executor = Executors.newCachedThreadPool();

  public void subscribe(TransactionEventSubscriber subscriber) {
    subscribers.add(subscriber);
  }

  public void publish(TransactionEvent event) {
    for (TransactionEventSubscriber subscriber : subscribers) {
      executor.submit(() -> subscriber.onEvent(event));
    }
  }

  public void shutdown() {
    executor.shutdown();
  }
}
