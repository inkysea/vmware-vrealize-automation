package com.inkysea.vmware.vra.jenkins.plugin.model;

import hudson.Extension;
import hudson.Util;
import hudson.model.AbstractDescribableImpl;
import hudson.model.AbstractProject;
import hudson.model.Descriptor;
import hudson.util.FormValidation;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.logging.Logger;


public class DestroyParam extends AbstractDescribableImpl<DestroyParam> implements Serializable {

    private String serverUrl;
    private String userName;
    private String password;
    private String tenant;
    private String deploymentName;
    private boolean waitExec;

    @DataBoundConstructor
    public DestroyParam(String serverUrl, String userName, String password, String tenant,
                        String deploymentName) {
        this.serverUrl = serverUrl;
        this.userName = userName;
        this.password = password;
        this.tenant = tenant;
        this.deploymentName = deploymentName;

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

    public String getDeploymentName() {
        return deploymentName;
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

        if (StringUtils.isBlank(this.getDeploymentName())) {
            throw new IOException("vRA Deployment name cannot be empty");
        }


        return true;
    }

    @Extension
    public static final class DescriptorImpl extends Descriptor<DestroyParam> {

        private static final Logger log;

        static {
            log = Logger.getLogger(DescriptorImpl.class.getName());
        }


        /**
         * In order to load the persisted global configuration, you have to
         * call load() in the constructor.
         */
        public DescriptorImpl() {
            load();
        }

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            // Indicates that this builder can be used with all kinds of project types
            return true;
        }

        /**
         * This human readable name is used in the configuration screen.
         */
        @Override
        public String getDisplayName() {
            return "Destroy vRealize Automation Deployment";
        }

        public FormValidation doCheckServerUrl(
                @QueryParameter final String value) {

            String url = Util.fixEmptyAndTrim(value);
            if (url == null)
                return FormValidation.error("Please enter the URL for vRealize Automation.");

            if (url.indexOf('$') >= 0)
                // set by variable, can't validate
                return FormValidation.ok();

            try {
                new URL(value).toURI();
            } catch (MalformedURLException e) {
                return FormValidation.error("This is not a valid URI");
            } catch (URISyntaxException e) {
                return FormValidation.error("This is not a valid URI");
            }

            return FormValidation.ok();
        }

        public FormValidation doCheckUserName(
                @QueryParameter final String value) {

            String url = Util.fixEmptyAndTrim(value);
            if (url == null)
                return FormValidation.error("Please enter user name.");

            if (url.indexOf('$') >= 0)
                // set by variable, can't validate
                return FormValidation.ok();

            return FormValidation.ok();
        }

        public FormValidation doCheckPassword(
                @QueryParameter final String value) {

            String url = Util.fixEmptyAndTrim(value);
            if (url == null)
                return FormValidation.error("Please enter password.");

            if (url.indexOf('$') >= 0)
                // set by variable, can't validate
                return FormValidation.error("Environment variable cannot be used in password.");

            return FormValidation.ok();
        }

        public FormValidation doCheckTenant(
                @QueryParameter final String value) {

            String url = Util.fixEmptyAndTrim(value);
            if (url == null)
                return FormValidation.error("Please enter the tenant.");

            if (url.indexOf('$') >= 0)
                // set by variable, can't validate
                return FormValidation.ok();

            return FormValidation.ok();
        }

        public FormValidation doCheckDeploymentName(
                @QueryParameter final String value) {

            String url = Util.fixEmptyAndTrim(value);
            if (url == null)
                return FormValidation.error("Please enter the Deployment Name.");

            if (url.indexOf('$') >= 0)
                // set by variable, can't validate
                return FormValidation.ok();

            return FormValidation.ok();
        }

    }
}
