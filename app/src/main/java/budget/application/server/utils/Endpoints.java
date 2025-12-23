package budget.application.server.utils;

import budget.application.utilities.Constants;

public class Endpoints {
    public static final String BASE_URL = Constants.THIS_APP_NAME + "/api/v1/";

    public static final String CATEGORY_TYPES = BASE_URL + "category-types";
    public static final String CATEGORY_TYPES_ID = BASE_URL + "category-types/";
    public static final String CATEGORIES = BASE_URL + "categories";
    public static final String CATEGORIES_ID = BASE_URL + "categories/";
    public static final String TRANSACTIONS = BASE_URL + "transactions";
    public static final String TRANSACTIONS_ID = BASE_URL + "transactions/";
    public static final String TRANSACTION_ITEMS = BASE_URL + "transaction-items";
    public static final String TRANSACTION_ITEMS_ID = BASE_URL + "transaction-items/";
}
