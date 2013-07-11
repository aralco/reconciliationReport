package com.spigit.citi.dao;

import com.spigit.citi.model.ReconciliationReportLog;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

public class ReconciliationReportLogDaoImpl implements ReconciliationReportLogDao {

    private SessionFactory sessionFactory;

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }


    @Override
    public void save(ReconciliationReportLog log) {
        Session session = sessionFactory.openSession();
        session.save(log);
        session.close();

    }
}
