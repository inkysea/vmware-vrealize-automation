package com.inkysea.vmware.vra.jenkins.plugin.model;

import java.io.Serializable;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import org.kohsuke.stapler.export.ExportedBean;


@ExportedBean
public class RequestParam extends AbstractDescribableImpl<RequestParam> implements Serializable, Cloneable {
    private String name;

    private String type="STRING";
    private String description="";
    private String value;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<RequestParam> {

        @Override
        public String getDisplayName() {
            return "";
        }
    }

    @Override
    public RequestParam clone() throws CloneNotSupportedException {
        return (RequestParam)super.clone();
    }
}
