package budget.application.event;

public interface TransactionEventSubscriber {
  void onEvent(TransactionEvent event);
}
