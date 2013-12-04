package com.spigit.citi.dao;

import com.spigit.citi.common.QueryType;
import com.spigit.citi.model.TransactionQueue;

import java.util.List;

public interface TransactionQueueDao {

    public List<TransactionQueue> getTransactionQueues();

    public List<TransactionQueue> getNotSuccessTransactionQueues();

    public List<TransactionQueue> getSuccessOrFailureAndNotReportedTransactionQueues();

    public void saveTransactionQueues(List<TransactionQueue> transactionQueueList, QueryType queryType);

    public void updateTransactionQueues(List<TransactionQueue> transactionQueueList);

}
