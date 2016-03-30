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
import java.util.logging.Logger;


public class BlueprintParam extends AbstractDescribableImpl<BlueprintParam> implements Serializable {

    private String serverUrl;
    private String userName;
    private String password;
    private String tenant;
    private boolean packageBlueprint;
    private String blueprintPath;
    private boolean overWrite;
    private boolean publishBlueprint;
    private String serviceCategory;
    private String blueprintName;
    private boolean reassignBlueprint;


    @DataBoundConstructor
    public BlueprintParam(String serverUrl, String userName, String password, String tenant,
                           boolean packageBlueprint, String blueprintPath, boolean overWrite,
                          boolean publishBlueprint, String serviceCategory){

        this.serverUrl = serverUrl;
        this.userName = userName;
        this.password = password;
        this.tenant = tenant;
        this.packageBlueprint = packageBlueprint;
        this.blueprintPath = blueprintPath;
        this.overWrite = overWrite;
        this.publishBlueprint = publishBlueprint;
        this.serviceCategory = serviceCategory;

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


    public boolean getPackageBlueprint() {
        return packageBlueprint;
    }


    public boolean getPublishBlueprint() {
        return publishBlueprint;
    }

    public boolean getReassignBlueprint() {
        return reassignBlueprint;
    }


    public String getBlueprintPath() {

        return blueprintPath;

    }

    public boolean getOverWrite() {
        return overWrite;
    }

    public String getServiceCategory() {
        return serviceCategory;
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
        if (StringUtils.isBlank(this.getBlueprintPath())) {
            throw new IOException("vRA BluePrint path cannot be empty");
        }


        return true;
    }

    @Extension
    public static final class DescriptorImpl extends Descriptor<BlueprintParam> {

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
            return "Load vRealize Automation Blueprint";
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

        public FormValidation doCheckBluePrintName(
                @QueryParameter final String value) {

            String url = Util.fixEmptyAndTrim(value);
            if (url == null)
                return FormValidation.error("Please enter the Blueprint Name.");

            if (url.indexOf('$') >= 0)
                // set by variable, can't validate
                return FormValidation.ok();

            return FormValidation.ok();
        }

        public FormValidation doCheckBlueprintPath(
                @QueryParameter final String value) {

            String url = Util.fixEmptyAndTrim(value);
            if (url == null)
                return FormValidation.error("Please enter the Blueprint Path.");

            if (url.indexOf('$') >= 0)
                // set by variable, can't validate
                return FormValidation.ok();

            return FormValidation.ok();
        }


    }
}
