package budget.application.service;

import budget.application.IntegrationBaseTest;
import budget.application.TestDataHelper;
import budget.application.TestDataSource;
import budget.application.service.domain.TransactionService;
import io.github.bibekaryal86.shdsvc.Email;
import io.github.bibekaryal86.shdsvc.dtos.EmailRequest;
import java.time.LocalDate;
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

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest extends IntegrationBaseTest {

  @Mock private Email email;
  private TransactionService service;
  private TestDataHelper helper;

  @BeforeEach
  void setup() {
    DataSource dataSource = TestDataSource.getDataSource();
    service = new TransactionService(dataSource, email);
    helper = new TestDataHelper(dataSource);
  }

  @AfterEach
  void cleanup() throws Exception {
    helper.deleteBulkTransactions(List.of(TEST_ID));
  }

  @Test
  void testReconcileAll_NoMismatches_NoEmailSent() throws Exception {
    helper.insertBulkTransactions(1500, 100.00, 100.00, false, 0.0, 1, 3);

    service.reconcileAll("req-no-mismatch");
    Mockito.verify(email, Mockito.never()).sendEmail(ArgumentMatchers.any());
  }

  @Test
  void testReconcileAll_OneMismatch_EmailSent() throws Exception {
    UUID txnId = helper.insertTransaction(UUID.randomUUID(), LocalDate.now(), 200.00);
    helper.insertTransactionItem(UUID.randomUUID(), txnId, TEST_ID, 50.00, "NEEDS");

    service.reconcileAll("req-one");
    ArgumentCaptor<EmailRequest> captor = ArgumentCaptor.forClass(EmailRequest.class);
    Mockito.verify(email, Mockito.times(1)).sendEmail(captor.capture());

    EmailRequest emailRequest = captor.getValue();
    Assertions.assertTrue(emailRequest.emailContent().html().contains(txnId.toString()));
  }

  @Test
  void testReconcileAll_MultipleMismatches_EmailContainsAll() throws Exception {
    UUID txnId1 = helper.insertTransaction(UUID.randomUUID(), LocalDate.now(), 100.00);
    helper.insertTransactionItem(UUID.randomUUID(), txnId1, TEST_ID, 25, "NEEDS");
    helper.insertTransactionItem(UUID.randomUUID(), txnId1, TEST_ID, 50.00, "WANTS");
    helper.insertTransactionItem(UUID.randomUUID(), txnId1, TEST_ID, 50, "INCOME");
    UUID txnId2 = helper.insertTransaction(UUID.randomUUID(), LocalDate.now(), 200.00);
    helper.insertTransactionItem(UUID.randomUUID(), txnId2, TEST_ID, 100, "TRANSFER");
    helper.insertTransactionItem(UUID.randomUUID(), txnId2, TEST_ID, 50.00, "OTHER");
    helper.insertTransactionItem(UUID.randomUUID(), txnId2, TEST_ID, 50, "OTHER");
    UUID txnId3 = helper.insertTransaction(UUID.randomUUID(), LocalDate.now(), 100.00);

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
    helper.insertBulkTransactions(1000, 100.00, 100.00, false, 0.0, 1, 1);
    helper.insertBulkTransactions(500, 200.00, 50.00, false, 1.0, 1, 1);

    service.reconcileAll("req-pagination");
    Mockito.verify(email, Mockito.times(1)).sendEmail(ArgumentMatchers.any(EmailRequest.class));
  }
}
