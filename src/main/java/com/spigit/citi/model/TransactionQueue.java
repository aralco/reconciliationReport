package com.spigit.citi.model;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Blob;
import java.util.Date;

@Entity
@Table(name = "TransactionQueue")
public class TransactionQueue implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String msgFrom;
    private String msgTo;
    private String subject;
    private Date date;
    @Lob
    private Blob body;
    private String title;
    private String status;
    private Date transmitTime;
    private String errorCondition;
    private String uniqueID;
    private Boolean reported;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMsgFrom() {
        return msgFrom;
    }

    public void setMsgFrom(String msgFrom) {
        this.msgFrom = msgFrom;
    }

    public String getMsgTo() {
        return msgTo;
    }

    public void setMsgTo(String msgTo) {
        this.msgTo = msgTo;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Blob getBody() {
        return body;
    }

    public void setBody(Blob body) {
        this.body = body;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getTransmitTime() {
        return transmitTime;
    }

    public void setTransmitTime(Date transmitTime) {
        this.transmitTime = transmitTime;
    }

    public String getErrorCondition() {
        return errorCondition;
    }

    public void setErrorCondition(String errorCondition) {
        this.errorCondition = errorCondition;
    }

    public String getUniqueID() {
        return uniqueID;
    }

    public void setUniqueID(String uniqueID) {
        this.uniqueID = uniqueID;
    }

    public Boolean getReported() {
        return reported;
    }

    public void setReported(Boolean reported) {
        this.reported = reported;
    }

    public String toString()    {
        return msgFrom+","+msgTo+","+date;
    }

}
