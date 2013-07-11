package com.spigit.citi.model;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Blob;
import java.util.Date;

@Entity
@Table(name = "ReconciliationReportLog")
public class ReconciliationReportLog implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String subject;
    private String distributionList;
    @Lob
    private Blob message;
    private Date timestamp;
    @Lob
    private Blob csv;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getDistributionList() {
        return distributionList;
    }

    public void setDistributionList(String distributionList) {
        this.distributionList = distributionList;
    }

    public Blob getMessage() {
        return message;
    }

    public void setMessage(Blob message) {
        this.message = message;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public Blob getCsv() {
        return csv;
    }

    public void setCsv(Blob csv) {
        this.csv = csv;
    }
}
