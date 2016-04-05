package com.inkysea.vmware.vra.jenkins.plugin.model;

import java.io.IOException;
import java.io.Serializable;

import hudson.Extension;
import hudson.Util;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.util.FormValidation;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.export.ExportedBean;


@ExportedBean
public class RequestParam extends AbstractDescribableImpl<RequestParam> implements Serializable, Cloneable {

    private String json;

    private String type="STRING";
    private String description="";
    private String value;

    @DataBoundConstructor
    public RequestParam(String json) {
        this.json = json;
    }

    public String getRequestParam() {
        return json;
    }

    public Boolean validate() throws IOException {
        if (StringUtils.isBlank(this.getRequestParam())) {
            throw new IOException("Deployment Parameter can not be empty");
        }

        return true;
    }

    public String getJson() {
        return json;
    }

    public void setJson(String name) {
        this.json = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<RequestParam> {

        @Override
        public String getDisplayName() {
            return "Deployment Parameters";
        }

        public FormValidation doCheckRequestParams(
                @QueryParameter final String value) {

            String url = Util.fixEmptyAndTrim(value);
            if (url == null)
                return FormValidation.error("Please enter a JSON request parameter.");

            if (url.indexOf('$') >= 0)
                // set by variable, can't validate
                return FormValidation.ok();

            return FormValidation.ok();
        }
    }

    @Override
    public RequestParam clone() throws CloneNotSupportedException {
        return (RequestParam)super.clone();
    }


}
