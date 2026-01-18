package budget.application.service;

import budget.application.IntegrationBaseTest;
import budget.application.TestDataSource;
import budget.application.service.domain.TransactionService;
import io.github.bibekaryal86.shdsvc.Email;
import io.github.bibekaryal86.shdsvc.dtos.EmailRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@org.junit.jupiter.api.Disabled
@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest extends IntegrationBaseTest {

  @Mock private Email email;
  private TransactionService service;

  @BeforeEach
  void setup() {
    DataSource dataSource = TestDataSource.getDataSource();
    service = new TransactionService(dataSource, email);
  }

  @AfterEach
  void cleanup() throws Exception {
    testDataHelper.deleteBulkTransactions(List.of(TEST_ID));
  }

  @Test
  void testReconcileAll_NoMismatches_NoEmailSent() throws Exception {
    testDataHelper.insertBulkTransactions(1500, 100.00, 100.00, false, 0.0, 1, 3);

    service.reconcileAll("req-no-mismatch");
    Mockito.verify(email, Mockito.never()).sendEmail(ArgumentMatchers.any());
  }

  @Test
  void testReconcileAll_OneMismatch_EmailSent() throws Exception {
    UUID txnId = testDataHelper.insertTransaction(UUID.randomUUID(), LocalDateTime.now(), 200.00);
    testDataHelper.insertTransactionItem(UUID.randomUUID(), txnId, TEST_ID, 50.00, "NEEDS");

    service.reconcileAll("req-one");
    ArgumentCaptor<EmailRequest> captor = ArgumentCaptor.forClass(EmailRequest.class);
    Mockito.verify(email, Mockito.times(1)).sendEmail(captor.capture());

    EmailRequest emailRequest = captor.getValue();
    Assertions.assertTrue(emailRequest.emailContent().html().contains(txnId.toString()));
  }

  @Test
  void testReconcileAll_MultipleMismatches_EmailContainsAll() throws Exception {
    UUID txnId1 = testDataHelper.insertTransaction(UUID.randomUUID(), LocalDateTime.now(), 100.00);
    testDataHelper.insertTransactionItem(UUID.randomUUID(), txnId1, TEST_ID, 25, "NEEDS");
    testDataHelper.insertTransactionItem(UUID.randomUUID(), txnId1, TEST_ID, 50.00, "WANTS");
    testDataHelper.insertTransactionItem(UUID.randomUUID(), txnId1, TEST_ID, 50, "");
    UUID txnId2 = testDataHelper.insertTransaction(UUID.randomUUID(), LocalDateTime.now(), 200.00);
    testDataHelper.insertTransactionItem(UUID.randomUUID(), txnId2, TEST_ID, 100, "");
    testDataHelper.insertTransactionItem(UUID.randomUUID(), txnId2, TEST_ID, 50.00, "");
    testDataHelper.insertTransactionItem(UUID.randomUUID(), txnId2, TEST_ID, 50, "");
    UUID txnId3 = testDataHelper.insertTransaction(UUID.randomUUID(), LocalDateTime.now(), 100.00);

    service.reconcileAll("req-multi");
    ArgumentCaptor<EmailRequest> captor = ArgumentCaptor.forClass(EmailRequest.class);
    Mockito.verify(email, Mockito.times(1)).sendEmail(captor.capture());

    EmailRequest emailRequest = captor.getValue();
    Assertions.assertTrue(emailRequest.emailContent().html().contains(txnId1.toString()));
    Assertions.assertFalse(emailRequest.emailContent().html().contains(txnId2.toString()));
    Assertions.assertTrue(emailRequest.emailContent().html().contains(txnId3.toString()));
  }

  @Test
  void testReconcileAll_PaginationHandledCorrectly() throws Exception {
    testDataHelper.insertBulkTransactions(1000, 100.00, 100.00, false, 0.0, 1, 1);
    testDataHelper.insertBulkTransactions(500, 200.00, 50.00, false, 1.0, 1, 1);

    service.reconcileAll("req-pagination");
    Mockito.verify(email, Mockito.times(1)).sendEmail(ArgumentMatchers.any(EmailRequest.class));
  }
}
