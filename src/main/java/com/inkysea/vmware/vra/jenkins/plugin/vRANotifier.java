package com.inkysea.vmware.vra.jenkins.plugin;

import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.inkysea.vmware.vra.jenkins.plugin.model.Deployment;
import com.inkysea.vmware.vra.jenkins.plugin.model.PluginParam;
import com.inkysea.vmware.vra.jenkins.plugin.model.RequestParam;
import com.inkysea.vmware.vra.jenkins.plugin.util.EnvVariableResolver;


import hudson.EnvVars;
import hudson.Extension;
import hudson.Launcher;
import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.model.EnvironmentContributingAction;
import hudson.tasks.*;
import hudson.util.FormValidation;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import static hudson.Util.fixEmptyAndTrim;

/**
 * Created by kthieler on 2/25/16.
 */
public class vRANotifier extends Notifier {

    private String vraUrl;
    private String userName;
    private String password;
    private String tenant;
    private String blueprintName;
    private boolean waitExec;

        private static final Logger LOGGER = Logger.getLogger(vRANotifier.class.getName());

        @DataBoundConstructor
        public vRANotifier (String vraUrl, String userName, String password, String tenant, String blueprintName, boolean waitExec, List<RequestParam> requestParams) {
            this.vraUrl = fixEmptyAndTrim(vraUrl);
            this.userName = fixEmptyAndTrim(userName);
            this.password = fixEmptyAndTrim(password);
            this.tenant = fixEmptyAndTrim(tenant);
            this.blueprintName = fixEmptyAndTrim(blueprintName);
            this.waitExec = waitExec;
        }



        public BuildStepMonitor getRequiredMonitorService() {
            return BuildStepMonitor.BUILD;
        }

        @Override
        public boolean prebuild(AbstractBuild<?, ?> build, BuildListener listener) {
            LOGGER.info("prebuild");
            return super.prebuild(build, listener);
        }


        @Override
        public Action getProjectAction(AbstractProject<?, ?> project) {
            LOGGER.info("getProjectAction");
            return super.getProjectAction(project);
        }

        @Override
        public Collection<? extends Action> getProjectActions(AbstractProject<?, ?> project) {
            LOGGER.info("getProjectActions");
            return super.getProjectActions(project);
        }

        @Override
        public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
            EnvVars envVars = build.getEnvironment(listener);
            boolean result = true;

            PrintStream logger = listener.getLogger();
            EnvVariableResolver helper = new EnvVariableResolver(build, listener);
            PluginParam param = new PluginParam(helper.replaceBuildParamWithValue(vraUrl), helper.replaceBuildParamWithValue(userName),
                    helper.replaceBuildParamWithValue(password), helper.replaceBuildParamWithValue(tenant), helper.replaceBuildParamWithValue(blueprintName), waitExec);

            param.validate();

            Deployment deployment = new Deployment(logger, param);

            if(deployment.Destroy()) {
                LOGGER.info("Success");
            } else {
                LOGGER.warning("Failed");
                result = false;
            }

            return result;
        }

        @Override
        public BuildStepDescriptor getDescriptor() {
            return DESCRIPTOR;
        }

        @Extension
        public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

        public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {

            @Override
            public String getDisplayName() {
                return "Destroy vRealize Automation Deployment";
            }

            @Override
            public boolean isApplicable(Class<? extends AbstractProject> jobType) {
                return true;
            }
        }




}

