package com.spigit.citi.dao;

import com.spigit.citi.common.QueryType;
import com.spigit.citi.model.TransactionQueryHistory;
import org.hibernate.*;
import org.hibernate.criterion.Projections;

import java.util.*;

public class TransactionQueryHistoryDaoImpl implements TransactionQueryHistoryDao {

    private SessionFactory sessionFactory;

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<TransactionQueryHistory> getTransactionQueryHistoryList() {
        Session session = sessionFactory.openSession();
        List<TransactionQueryHistory> transactionQueryHistoryList =  new ArrayList<TransactionQueryHistory>(0);
        transactionQueryHistoryList = (List<TransactionQueryHistory>)session.createQuery("from TransactionQueue").list();
        session.close();
        return transactionQueryHistoryList;

    }

    @Override
    public void saveTransactionQueryHistory(TransactionQueryHistory transactionQueryHistory) {
        Session session = sessionFactory.openSession();
        session.save(transactionQueryHistory);
        session.close();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<HashMap<String, String>> getTableProperties() {
        List<HashMap<String, String>> tablePropertiesList = new ArrayList<HashMap<String, String>>(0);
        Session session = sessionFactory.openSession();
        List<?> results = (List<?>)session.createCriteria(TransactionQueryHistory.class)
                .setProjection(Projections.projectionList()
                        .add(Projections.rowCount())
                        .add(Projections.max("timestamp"))
                        .add(Projections.groupProperty("queryType"))
                )
                .list();
        Iterator<?> iterator = results.iterator();
        while (iterator.hasNext())  {
            Object[] objects = (Object []) iterator.next();
            HashMap<String, String> propertiesMap = new HashMap<String, String>(3);
            propertiesMap.put("rows", objects[0].toString());
            propertiesMap.put("timestamp", objects[1].toString());
            propertiesMap.put("queryType", objects[2].toString());
            tablePropertiesList.add(propertiesMap);
        }
        session.close();
        return tablePropertiesList;
    }

    @Override
    @SuppressWarnings("unchecked")
    public int getNumberOfRows() {
        int numberOfRows = 0;
        Session session = sessionFactory.openSession();
        Criteria criteria = session.createCriteria(TransactionQueryHistory.class)
                .setProjection(Projections.rowCount());

        List<?> result = (List<?>)criteria.list();
        if (!result.isEmpty()) {
            numberOfRows = Integer.valueOf(result.get(0).toString());
        }
        session.close();
        return numberOfRows;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void updateLastTransactionQueryHistory() {
        List<TransactionQueryHistory> transactionQueryHistoryList = new ArrayList<TransactionQueryHistory>(0);
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();
        Query query = session.createQuery("update TransactionQueryHistory " +
                "set queryType = :queryType, timestamp = :timestamp " +
                "where queryType <> :reconciliation");
        query.setParameter("queryType", QueryType.RECONCILIATION.name());
        query.setParameter("timestamp", Calendar.getInstance().getTime());
        query.setParameter("reconciliation", QueryType.RECONCILIATION.name());
        int result = query.executeUpdate();
        transaction.commit();
        session.close();

    }
}
