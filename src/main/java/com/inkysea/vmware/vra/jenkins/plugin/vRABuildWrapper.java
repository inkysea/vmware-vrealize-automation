package com.inkysea.vmware.vra.jenkins.plugin;

import com.inkysea.vmware.vra.jenkins.plugin.model.Deployment;
import com.inkysea.vmware.vra.jenkins.plugin.model.PluginParam;
import hudson.EnvVars;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.tasks.BuildWrapper;
import hudson.tasks.BuildWrapper.Environment;
import hudson.tasks.BuildWrapperDescriptor;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Created by kthieler on 2/25/16.
 */
public class vRABuildWrapper extends BuildWrapper {


    protected List<PluginParam> params;
    protected List<Deployment> deployments = new ArrayList<Deployment>();


    @DataBoundConstructor
    public vRABuildWrapper( List<PluginParam> params ) {
        this.params = params;
    }

    // required for to write the deployment properties back to Jenkins environment variables.
    @Override
    public void makeBuildVariables(AbstractBuild build,
                                   Map<String, String> variables) {

        for (Deployment deployment : deployments) {
            variables.putAll(deployment.getOutputs());
        }

    }

    @Override
    public Environment setUp(AbstractBuild build, Launcher launcher,
                             BuildListener listener) throws IOException, InterruptedException {

        EnvVars env = build.getEnvironment(listener);
        env.overrideAll(build.getBuildVariables());

        boolean success = true;

        for (PluginParam param : params) {

            final Deployment deployment = newDeployment(listener.getLogger(), param);


                if (deployment.Create()) {
                    this.deployments.add(deployment);

                    System.out.println("Machine List : "+deployment.getOutputs());

                    env.putAll(deployment.getOutputs());
                } else {
                    build.setResult(Result.FAILURE);
                    success = false;
                    break;
                }

            if (!success) {
                doTearDown();
                return null;
            }


        }
        return new Environment() {
            @Override
            public boolean tearDown(AbstractBuild build, BuildListener listener)
                    throws IOException, InterruptedException {

                return doTearDown();

            }

        };

    }

    protected boolean doTearDown() throws IOException, InterruptedException{
        boolean result = true;

        List<Deployment> reversedList = new ArrayList<Deployment>(deployments);
        Collections.reverse(reversedList);

        for (Deployment deployment : reversedList) {
            // automatically delete the stack?
            result = result && deployment.Destroy();
        }

        return result;
    }

    protected Deployment newDeployment(PrintStream logger, PluginParam params) throws IOException {

        Boolean isURL = false;
        String recipe = null;

        return new Deployment(logger, params);

    }

    @Extension
    public static class DescriptorImpl extends BuildWrapperDescriptor {

        @Override
        public String getDisplayName() {
            return "Create vRealize Automation Deployment";
        }

        @Override
        public boolean isApplicable(AbstractProject<?, ?> item) {
            return true;
        }

    }

    public List<PluginParam> getParams() {
        return params;
    }

    // Required for Jenkins to avoid NPEs.
    private Object readResolve() {
        deployments = new ArrayList<Deployment>();
        return this;
    }

}
