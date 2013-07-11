package com.spigit.citi.dao;

import com.spigit.citi.common.QueryType;
import com.spigit.citi.common.TransactionStatus;
import com.spigit.citi.model.TransactionQueue;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class TransactionQueueDaoImpl implements TransactionQueueDao {

    public static final String YYYY_MM_DD = "yyyyMMdd";
    private SessionFactory sessionFactory;

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<TransactionQueue> getTransactionQueues() {
        Session session = sessionFactory.openSession();
        List<TransactionQueue> transactionQueueList =  new ArrayList<TransactionQueue>(0);
        transactionQueueList = (List<TransactionQueue>)session.createQuery("from TransactionQueue").list();
        session.close();
        return transactionQueueList;

    }

    @Override
    @SuppressWarnings("unchecked")
    public List<TransactionQueue> getNotSuccessTransactionQueues() {
        Session session = sessionFactory.openSession();
        List<TransactionQueue> transactionQueueList = new ArrayList<TransactionQueue>(0);
        Query query = session.createQuery("from TransactionQueue where status <> :status");
        query.setParameter("status",TransactionStatus.SUCCESS.name());
        transactionQueueList = (List<TransactionQueue>)query.list();
        session.close();
        return transactionQueueList;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<TransactionQueue> getSuccessOrFailureTransactionQueues() {
        List<TransactionQueue> transactionQueueList = new ArrayList<TransactionQueue>(0);
        Session session = sessionFactory.openSession();
        Query query = session.createQuery("from TransactionQueue where status = :status1 or status = :status2");
        query.setParameter("status1",TransactionStatus.SUCCESS.name());
        query.setParameter("status2",TransactionStatus.FAILURE.name());
        transactionQueueList = (List<TransactionQueue>)query.list();
        session.close();
        return transactionQueueList;
    }

    @Override
    public void saveTransactionQueues(List<TransactionQueue> transactionQueueList, QueryType queryType) {
        Date date = Calendar.getInstance().getTime();
        SimpleDateFormat formatter = new SimpleDateFormat(YYYY_MM_DD);
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();
        int sequenceNumber =1;
        for(TransactionQueue transactionQueue : transactionQueueList)    {
            transactionQueue.setStatus(TransactionStatus.READY.name());
            transactionQueue.setUniqueID(formatter.format(date) + "_" + queryType.name() + "_" + sequenceNumber++);
            session.save(transactionQueue);

            if(sequenceNumber%20 == 0)    {
                session.flush();
                session.clear();
            }
        }
        transaction.commit();
        session.close();

    }

    @Override
    public void updateTransactionQueues(List<TransactionQueue> transactionQueueList) {
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();
        int count =1;
        for(TransactionQueue transactionQueue : transactionQueueList)    {
            session.update(transactionQueue);
            if(count%20 == 0)    {
                session.flush();
                session.clear();
            }
        }
        transaction.commit();
        session.close();
    }
}
