package org.dspace.app.util;

import javax.persistence.*;
import java.util.Date;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 29/04/14
 * Time: 11:28
 */
@Entity
@Table(name="webapp", schema = "public")
public class WebApp {


    @Id
    @Column(name="webapp_id")
    @GeneratedValue(strategy = GenerationType.AUTO ,generator="webapp_seq")
    @SequenceGenerator(name="webapp_seq", sequenceName="webapp_seq", allocationSize = 1, initialValue = 1)
    private Integer id;

    @Column(name = "appname", unique = true, length = 32)
    private String appName;

    @Column(name = "url")
    private String url;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "started")
    private Date started;

    @Column(name = "isui")
    private Integer isui;

    public Integer getId() {
        return id;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Date getStarted() {
        return started;
    }

    public void setStarted(Date started) {
        this.started = started;
    }

    public Integer getIsui() {
        return isui;
    }

    public void setIsui(Integer isui) {
        this.isui = isui;
    }
}
