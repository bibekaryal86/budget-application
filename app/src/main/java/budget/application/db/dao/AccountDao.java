package budget.application.db.dao;

import budget.application.cache.AccountCache;
import budget.application.db.mapper.AccountRowMappers;
import budget.application.model.dto.AccountResponse;
import budget.application.model.entity.Account;
import java.math.BigDecimal;
import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class AccountDao extends BaseDao<Account> {

  private final AccountRowMappers.AccountCurrentBalanceCalcMapper accountCurrentBalanceCalcMapper;

  public AccountDao(Connection connection, AccountCache accountCache) {
    super(connection, new AccountRowMappers.AccountRowMapper(), accountCache);
    this.accountCurrentBalanceCalcMapper = new AccountRowMappers.AccountCurrentBalanceCalcMapper();
  }

  @Override
  protected String tableName() {
    return "account";
  }

  @Override
  protected List<String> insertColumns() {
    return List.of("name", "account_type", "bank_name", "status");
  }

  @Override
  protected List<Object> insertValues(Account account) {
    return List.of(
        account.name().toUpperCase(), account.accountType(), account.bankName(), account.status());
  }

  @Override
  protected List<String> updateColumns() {
    return List.of("name", "account_type", "bank_name", "status");
  }

  @Override
  protected List<Object> updateValues(Account account) {
    return List.of(
        account.name().toUpperCase(), account.accountType(), account.bankName(), account.status());
  }

  @Override
  protected UUID getId(Account account) {
    return account.id();
  }

  @Override
  protected String orderByClause() {
    return "name ASC";
  }

  public List<Account> readNoEx(List<UUID> ids) {
    try {
      return read(ids);
    } catch (Exception e) {
      return List.of();
    }
  }

  public List<String> readAllBanks() throws SQLException {
    String sql = "SELECT DISTINCT bank_name FROM account ORDER BY bank_name ASC";
    List<String> bankNames = new ArrayList<>();
    try (PreparedStatement preparedStatement = connection.prepareStatement(sql);
        ResultSet resultSet = preparedStatement.executeQuery()) {
      while (resultSet.next()) {
        bankNames.add(resultSet.getString("bank_name"));
      }
    }
    return bankNames;
  }

  public int updateAccountBalances(Map<UUID, BigDecimal> accountBalanceUpdates)
      throws SQLException {
    String sql = "UPDATE account SET account_balance = ? WHERE id = ?";

    boolean originalAutoCommit = connection.getAutoCommit();
    int totalUpdated = 0;
    int batchSize = 100;
    int count = 0;

    try {
      connection.setAutoCommit(false);
      try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
        for (Map.Entry<UUID, BigDecimal> entry : accountBalanceUpdates.entrySet()) {
          UUID id = entry.getKey();
          BigDecimal balance = entry.getValue();

          preparedStatement.setBigDecimal(1, balance);
          preparedStatement.setObject(2, id);
          preparedStatement.addBatch();

          if (++count % batchSize == 0) {
            totalUpdated += sumBatchResults(preparedStatement.executeBatch());
          }
        }

        totalUpdated += sumBatchResults(preparedStatement.executeBatch());
      }

      connection.commit();
      return totalUpdated;
    } catch (SQLException e) {
      connection.rollback();
      throw e;
    } finally {
      connection.setAutoCommit(originalAutoCommit);
    }
  }

  private int sumBatchResults(int[] results) {
    int sum = 0;
    for (int res : results) {
      if (res >= 0) sum += res;
      else if (res == Statement.SUCCESS_NO_INFO) sum += 1;
    }
    return sum;
  }

  // TODO delete this
  public Map<UUID, AccountResponse.AccountCurrentBalanceCalc> getTotalBalancesForCurrentBalance(
      List<UUID> accountIds) throws SQLException {
    String sql =
        """
          SELECT
              ti.account_id AS account_id,
              SUM(CASE WHEN ct.name = 'INCOME' THEN ti.amount ELSE 0 END) AS total_incomes,
              SUM(CASE WHEN ct.name = 'TRANSFER' AND c.name = 'TRANSFER IN' THEN ti.amount
                        WHEN ct.name = 'TRANSFER' AND c.name = 'TRANSFER OUT' THEN -ti.amount
                          ELSE 0 END ) AS total_transfers,
              SUM(CASE WHEN ct.name NOT IN ('INCOME', 'TRANSFER') THEN ti.amount ELSE 0 END) AS total_expenses
          FROM transaction_item ti
          JOIN category c ON c.id = ti.category_id
          JOIN category_type ct ON ct.id = c.category_type_id
          WHERE ti.account_id = ANY(?)
          GROUP BY ti.account_id
      """;

    List<AccountResponse.AccountCurrentBalanceCalc> results = new ArrayList<>();
    try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
      UUID[] idsArray = accountIds.toArray(new UUID[0]);
      Array sqlArray = connection.createArrayOf("uuid", idsArray);
      preparedStatement.setArray(1, sqlArray);

      try (ResultSet resultSet = preparedStatement.executeQuery()) {
        while (resultSet.next()) {
          results.add(accountCurrentBalanceCalcMapper.map(resultSet));
        }
      }
    }

    return results.stream()
        .collect(Collectors.toMap(AccountResponse.AccountCurrentBalanceCalc::id, acc -> acc));
  }
}
