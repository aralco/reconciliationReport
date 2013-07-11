package com.spigit.citi.service;

import com.spigit.citi.dao.ReconciliationReportLogDao;
import com.spigit.citi.dao.TransactionQueryHistoryDao;
import com.spigit.citi.dao.TransactionQueueDao;
import com.spigit.citi.model.ReconciliationReportLog;
import com.spigit.citi.model.TransactionQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class ReconciliationReportService extends Service {
    private static final Logger logger = LoggerFactory.getLogger(ReconciliationReportService.class);

    @Autowired
    private TransactionQueueDao transactionQueueDAO;
    @Autowired
    private TransactionQueryHistoryDao transactionQueryHistoryDao;
    @Autowired
    private ReconciliationReportLogDao reconciliationReportLogDao;
    private EmailService emailService;

    public void setEmailService(EmailService emailService) {
        this.emailService = emailService;
    }

    @Override
    public void execute() {
        logger.info("*************************ReconciliationReport*************************");
        List<TransactionQueue> transactionQueues = getTransactionQueues();
        if(transactionQueues!=null && !transactionQueues.isEmpty()) {
            File csvReconciliationReport = assemblyReconciliationReport(transactionQueues);
            ReconciliationReportLog reportLog = assemblyAndSendSMTPMessage(csvReconciliationReport);
            if(reportLog!=null)  {
                updateReconciliationReportStatus();
                saveReconciliationReportLog(reportLog);
            }
        } else   {
            logger.warn("There are no transactions in queue.");
        }

    }

    private List<TransactionQueue> getTransactionQueues()   {
        logger.info("Loading SUCCESS/FAILURE transactions.");
        return transactionQueueDAO.getSuccessOrFailureTransactionQueues();
    }

    private File assemblyReconciliationReport(List<TransactionQueue> transactionQueues) {
        logger.info("Assembly of CSV Reconciliation Report file.");
        File csvReconciliationReport = null;
        FileWriter csvWriter = null;
        try {
            csvReconciliationReport = File.createTempFile("ReconciliationReport",".csv");
            csvWriter = new FileWriter(csvReconciliationReport);
            logger.info("Temporal CSV File created with name :"+csvReconciliationReport.getName()+", on the following path:"+csvReconciliationReport.getAbsolutePath());
            //Id,UniqueID,MessageFrom,MessageTo,Subject,Date,Status,TransmitTime,ErrorCondition
            csvWriter.append("Id");
            csvWriter.append(",");
            csvWriter.append("UniqueID");
            csvWriter.append(",");
            csvWriter.append("MessageFrom");
            csvWriter.append(",");
            csvWriter.append("MessageTo");
            csvWriter.append(",");
            csvWriter.append("Subject");
            csvWriter.append(",");
            csvWriter.append("Date");
            csvWriter.append(",");
            csvWriter.append("Status");
            csvWriter.append(",");
            csvWriter.append("TransmitTime");
            csvWriter.append(",");
            csvWriter.append("ErrorCondition");
            csvWriter.append("\n");

            for(TransactionQueue transactionQueue : transactionQueues)  {
                csvWriter.append(transactionQueue.getId().toString());
                csvWriter.append(",");
                csvWriter.append(transactionQueue.getUniqueID());
                csvWriter.append(",");
                csvWriter.append(transactionQueue.getMsgFrom());
                csvWriter.append(",");
                csvWriter.append(transactionQueue.getMsgTo());
                csvWriter.append(",");
                csvWriter.append(transactionQueue.getSubject());
                csvWriter.append(",");
                csvWriter.append(transactionQueue.getDate().toString());
                csvWriter.append(",");
                csvWriter.append(transactionQueue.getStatus());
                csvWriter.append(",");
                csvWriter.append(transactionQueue.getTransmitTime().toString());
                csvWriter.append(",");
                csvWriter.append(transactionQueue.getErrorCondition());
                csvWriter.append("\n");
                csvWriter.flush();
            }


        } catch (IOException e) {
            logger.warn("Could not create csv file due to {}",e.getMessage());
        } finally {
            if(csvWriter!=null) {
                try {
                    csvWriter.close();
                } catch (IOException e) {
                    logger.warn("Could not close csv file, due to {}",e.getMessage());
                }
            }
        }

        return csvReconciliationReport;
    }

    private ReconciliationReportLog assemblyAndSendSMTPMessage(File attachment) {
        logger.info("Send SMTP message with CSV report as attachment.");
        return emailService.assemblyAndSendReconciliationReportSMTPMessage(attachment);
    }

    private void updateReconciliationReportStatus()  {
        logger.info("Updating TransactionQueryHistory table.");
        transactionQueryHistoryDao.updateLastTransactionQueryHistory();
    }

    private void saveReconciliationReportLog(ReconciliationReportLog reportLog) {
        logger.info("Saving on ReconciliationReportLog table.");
        reconciliationReportLogDao.save(reportLog);
    }
}
