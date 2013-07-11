package com.spigit.citi.dao;

import com.spigit.citi.model.TransactionQueryHistory;

import java.util.HashMap;
import java.util.List;

public interface TransactionQueryHistoryDao {

    public List<TransactionQueryHistory> getTransactionQueryHistoryList();

    public void saveTransactionQueryHistory(TransactionQueryHistory transactionQueryHistory);

    public List<HashMap<String,String>> getTableProperties();

    public int getNumberOfRows();

    public void updateLastTransactionQueryHistory();

}
