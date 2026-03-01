package budget.application.db.dao;

import budget.application.db.mapper.AccountBalancesRowMappers;
import budget.application.db.mapper.AccountRowMappers;
import budget.application.db.util.DaoUtils;
import budget.application.model.dto.AccountResponse;
import budget.application.model.dto.InsightsResponse;
import budget.application.model.entity.AccountBalances;
import io.github.bibekaryal86.shdsvc.helpers.CommonUtilities;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AccountBalancesDao extends BaseDao<AccountBalances> {

  public AccountBalancesDao(Connection connection) {
    super(connection, new AccountBalancesRowMappers.AccountBalancesRowMapper(), null);
  }

  @Override
  protected String tableName() {
    return "account_balances";
  }

  @Override
  protected List<String> insertColumns() {
    return List.of("account_id", "year_month", "account_balance");
  }

  @Override
  protected List<Object> insertValues(AccountBalances accountBalances) {
    return List.of(
        accountBalances.accountId(), accountBalances.yearMonth(), accountBalances.accountBalance());
  }

  @Override
  protected List<String> updateColumns() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  protected List<Object> updateValues(AccountBalances accountBalances) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  protected UUID getId(AccountBalances accountBalances) {
    return accountBalances.id();
  }

  @Override
  protected String orderByClause() {
    return "year_month DESC";
  }

  public void createAccountBalances(List<AccountBalances> accountBalances) throws SQLException {
    for (AccountBalances accountBalance : accountBalances) {
      create(accountBalance);
    }
  }

  public List<InsightsResponse.AccountSummary> readAccountBalances(
      LocalDate beginDate, LocalDate endDate, List<UUID> accountIds) throws SQLException {
    log.debug(
        "Read account balances: BeginDate=[{}], EndDate=[{}], AccountIds=[{}]",
        beginDate,
        endDate,
        accountIds);
    List<Object> params = new ArrayList<>();
    params.add(beginDate);
    params.add(endDate);

    boolean hasAccountIds = !CommonUtilities.isEmpty(accountIds);
    params.add(hasAccountIds);
    params.add(hasAccountIds ? accountIds.toArray(new UUID[0]) : null);

    String sql =
        """
            SELECT
              ab.year_month,
              ab.account_id,
              ab.account_balance,
              a.name as account_name,
              a.account_type,
              a.bank_name as account_bank_name,
              a.status as account_status
            FROM
                account_balances ab
                INNER JOIN account a on ab.account_id = a.id
            WHERE
                ab.year_month BETWEEN ? AND ? AND (? = FALSE OR ab.account_id = ANY(?))
       """;

    Map<String, InsightsResponse.AccountSummary> accountBalanceSummaryMap = new LinkedHashMap<>();
    try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
      DaoUtils.bindParams(preparedStatement, params, Boolean.TRUE);
      try (ResultSet resultSet = preparedStatement.executeQuery()) {
        while (resultSet.next()) {
          LocalDate yearMonth = resultSet.getObject("year_month", LocalDate.class);
          String yearMonthStr = DaoUtils.getYearMonth(yearMonth);

          AccountResponse.Account accountWithBalance =
              new AccountRowMappers.AccountRowMapperResponse().map(resultSet);
          accountBalanceSummaryMap
              .computeIfAbsent(
                  yearMonthStr,
                  k -> new InsightsResponse.AccountSummary(yearMonthStr, new ArrayList<>()))
              .accounts()
              .add(accountWithBalance);
        }
      }
    }
    return new ArrayList<>(accountBalanceSummaryMap.values());
  }

  public int updateAccountBalances(
      LocalDate yearMonth, String notes, Map<UUID, BigDecimal> accountBalanceUpdates)
      throws SQLException {
    String sql =
            "WITH data(account_id, balance) AS ("
              + "SELECT * FROM UNNEST(?::uuid[], ?::numeric[])) "
              + "UPDATE account_balances a "
              + "SET account_balance = d.balance, "
              + "notes = COALESCE(a.notes, '') || ? "
              + "FROM data d "
              + "WHERE a.account_id = d.account_id "
              + "AND a.year_month = ? ";

    boolean originalAutoCommit = connection.getAutoCommit();
    int updatedCount = 0;

    try {
      connection.setAutoCommit(false);

      UUID[] accountIds = accountBalanceUpdates.keySet().toArray(UUID[]::new);
      BigDecimal[] balances = accountBalanceUpdates.values().toArray(BigDecimal[]::new);

      try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
        preparedStatement.setArray(1, connection.createArrayOf("uuid", accountIds));
        preparedStatement.setArray(2, connection.createArrayOf("numeric", balances));
        preparedStatement.setObject(3, notes);
        preparedStatement.setObject(4, yearMonth);
        updatedCount = preparedStatement.executeUpdate();
      }

      connection.commit();
      return updatedCount;
    } catch (SQLException e) {
      connection.rollback();
      throw e;
    } finally {
      connection.setAutoCommit(originalAutoCommit);
    }
  }
}
