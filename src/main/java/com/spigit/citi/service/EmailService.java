package com.spigit.citi.service;

import com.spigit.citi.common.TransactionStatus;
import com.spigit.citi.model.ReconciliationReportLog;
import com.spigit.citi.model.TransactionQueue;
import com.sun.mail.smtp.SMTPMessage;
import org.hibernate.Hibernate;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.*;
import java.sql.Blob;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class EmailService  {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    private static final String X_ZANTAZ_RECIP = "X-ZANTAZ-RECIP";
    private static final String X_CITIINGSOURCE = "X-CITIINGSOURCE";
    private static final String X_CITIMSGSOURCE = "X-CITIMSGSOURCE";
    private static final String X_CITIMSGTYPE = "X-CITIMSGTYPE";
    private static final String X_CITICONVTYPE = "X-CITICONVTYPE";
    private static final String ENCODING_OPTIONS = "text/html; charset=UTF-8";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String MM_DD_YYYY = "MM-dd-yyyy";

    private JavaMailSender javaMailSender;
    private String envelopeFrom;
    private String envelopeTo;
    private String subject;
    private String message;
    private SessionFactory sessionFactory;

    public void setJavaMailSender(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    public void setEnvelopeFrom(String envelopeFrom) {
        this.envelopeFrom = envelopeFrom;
    }

    public void setEnvelopeTo(String envelopeTo) {
        this.envelopeTo = envelopeTo;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public TransactionQueue assemblyAndSendSMTPMessage(TransactionQueue transactionQueue)   {
        logger.info("Assembly SMTP message ");
        InputStream myInputStream = null;

        Date date = Calendar.getInstance().getTime();
        MimeMessage mimeMessage = this.javaMailSender.createMimeMessage();
        try {
            mimeMessage.setFrom(new InternetAddress(envelopeFrom));
            mimeMessage.setRecipient(Message.RecipientType.TO,
                    new InternetAddress(envelopeTo));
            mimeMessage.setSubject(transactionQueue.getSubject());
            mimeMessage.setSentDate(date);
            myInputStream = transactionQueue.getBody().getBinaryStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(myInputStream));
            String messageBody="";
            String str;
            while ((str = in.readLine()) != null) {
                messageBody += str;
            }
            mimeMessage.setText(messageBody);
            mimeMessage.setHeader(CONTENT_TYPE, ENCODING_OPTIONS);
            mimeMessage.addHeader(X_ZANTAZ_RECIP, transactionQueue.getMsgTo());
            mimeMessage.addHeader(X_CITIINGSOURCE,"FEED2");
            mimeMessage.addHeader(X_CITIMSGSOURCE,"CITIIDEAS");
            mimeMessage.addHeader(X_CITIMSGTYPE,"EMAIL");
            mimeMessage.addHeader(X_CITICONVTYPE,"POST");

            SMTPMessage smtpMessage = new SMTPMessage(mimeMessage);
            smtpMessage.setEnvelopeFrom(envelopeFrom);

            this.javaMailSender.send(smtpMessage);
            //update transactionQueue values
            transactionQueue.setTransmitTime(date);
            transactionQueue.setStatus(TransactionStatus.SUCCESS.name());
            transactionQueue.setErrorCondition("");
            logger.info("Successful sent SMTP message at {}", date);
        }
        catch (MailException e) {
            //update transactionQueue values
            transactionQueue.setTransmitTime(date);
            transactionQueue.setStatus(TransactionStatus.FAILURE.name());
            transactionQueue.setErrorCondition(e.getMessage());
            logger.warn("Error sending SMTP message {}", e.getMessage());
        }
        catch (MessagingException e) {
            //update transactionQueue values
            transactionQueue.setTransmitTime(date);
            transactionQueue.setStatus(TransactionStatus.FAILURE.name());
            transactionQueue.setErrorCondition(e.getMessage());
            logger.warn("Error creating SMTP message {}", e.getMessage());
        } catch (SQLException e)  {
            //update transactionQueue values
            transactionQueue.setTransmitTime(date);
            transactionQueue.setStatus(TransactionStatus.FAILURE.name());
            transactionQueue.setErrorCondition(e.getMessage());
            logger.warn("Error reading Blob body message {}", e.getMessage());
        } catch (IOException e)  {
            //update transactionQueue values
            transactionQueue.setTransmitTime(date);
            transactionQueue.setStatus(TransactionStatus.FAILURE.name());
            transactionQueue.setErrorCondition(e.getMessage());
            logger.warn("Error reading body message {}", e.getMessage());
        } finally {
            if(myInputStream!=null) {
                try {
                    myInputStream.close();
                } catch (IOException e) {
                    logger.error("Error closing Blob inputStream: {}",e.getMessage());
                }
            }
        }
        return transactionQueue;
    }

    public ReconciliationReportLog assemblyAndSendReconciliationReportSMTPMessage(File attachment)   {
        logger.info("Assembly Reconciliation Report SMTP message ");
        ReconciliationReportLog reportLog = null;
        InputStream myInputStream = null;

        Date date = Calendar.getInstance().getTime();
        SimpleDateFormat formatter = new SimpleDateFormat(MM_DD_YYYY);
        MimeMessage mimeMessage = this.javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, true);
            messageHelper.setFrom(new InternetAddress(envelopeFrom));
            //messageHelper.setTo(formatEmailRecipients(envelopeTo));
            messageHelper.setTo(InternetAddress.parse(envelopeTo,true));//added
            messageHelper.setValidateAddresses(true);//added
            messageHelper.setSubject(subject+" "+formatter.format(date));
            messageHelper.setSentDate(date);
            messageHelper.setText(message, true);
            messageHelper.addAttachment("ReconciliationReport_"+formatter.format(date)+".csv",attachment);

            SMTPMessage smtpMessage = new SMTPMessage(mimeMessage);
            smtpMessage.setEnvelopeFrom(envelopeFrom);

            this.javaMailSender.send(smtpMessage);
            reportLog = new ReconciliationReportLog();
            reportLog.setSubject(smtpMessage.getSubject());
            reportLog.setDistributionList(envelopeTo);
            reportLog.setMessage(createMessageBlob(message));
            reportLog.setTimestamp(date);
            reportLog.setCsv(createCsvBlob(attachment));
            logger.info("Successful sent SMTP message at {}", date);
        } catch (MailException e) {
            logger.warn("Error sending SMTP message {}", e.getMessage());
        } catch (AddressException e)    {
            logger.error("Error creating to addres email {}", e.getMessage());
        } catch (MessagingException e) {
            logger.warn("Error creating SMTP message {}", e.getMessage());
        } finally {
            if(myInputStream!=null) {
                try {
                    myInputStream.close();
                } catch (IOException e) {
                    logger.error("Error closing Blob inputStream: {}",e.getMessage());
                }
            }
        }
        return reportLog;
    }

    private InternetAddress[] formatEmailRecipients(String email) throws AddressException {
        String [] emails = email.split(",");
        InternetAddress[] emailRecipients = new InternetAddress[emails.length];
        for(int i=0;i<emails.length;i++)
            emailRecipients[i] = new InternetAddress(emails[i].trim());
        return emailRecipients;
    }

    private Blob createMessageBlob(String message)    {
        Blob messageBlob = Hibernate.getLobCreator(sessionFactory.openSession()).createBlob(message.getBytes());
        return messageBlob;
    }

    private Blob createCsvBlob(File csv)    {
        byte[] csvBytes = new byte[(int)csv.length()];
        try {
            FileInputStream stream = new FileInputStream(csv);
            stream.read(csvBytes);
            stream.close();

        } catch (FileNotFoundException e) {
            logger.warn("Could not create CSV Blob file {}",e.getMessage());
        } catch (IOException e) {
            logger.warn("Could not create CSV Blob file {}",e.getMessage());
        }
        Blob csvBlob = Hibernate.getLobCreator(sessionFactory.openSession()).createBlob(csvBytes);
        return csvBlob;
    }
}
