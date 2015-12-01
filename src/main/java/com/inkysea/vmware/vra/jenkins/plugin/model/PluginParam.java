package com.inkysea.vmware.vra.jenkins.plugin.model;

import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.io.Serializable;


public class PluginParam implements Serializable {

    private String serverUrl;
    private String userName;
    private String password;
    private String tenant;
    private String blueprintName;
    private boolean waitExec;

    public PluginParam(String serverUrl, String userName, String password, String tenant, String blueprintName, boolean waitExec) {
        this.serverUrl = serverUrl;
        this.userName = userName;
        this.password = password;
        this.tenant = tenant;
        this.blueprintName = blueprintName;
        this.waitExec = waitExec;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public String getTenant() {
        return tenant;
    }

    public String getBluePrintName() {
        return blueprintName;
    }


    public boolean isWaitExec() {
        return waitExec;
    }


    public Boolean validate() throws IOException {
        if (StringUtils.isBlank(this.getServerUrl())) {
            throw new IOException("vRA server url cannot be empty");
        }

        if (StringUtils.isBlank(this.getUserName())) {
            throw new IOException("vRA server username cannot be empty");
        }

        if (StringUtils.isBlank(this.getPassword())) {
            throw new IOException("vRA server password cannot be empty");
        }

        if (StringUtils.isBlank(this.getTenant())) {
            throw new IOException("vRA tenant cannot be empty");
        }

        if (StringUtils.isBlank(this.getBluePrintName())) {
            throw new IOException("vRA BluePrint name cannot be empty");
        }

        return true;
    }
}
