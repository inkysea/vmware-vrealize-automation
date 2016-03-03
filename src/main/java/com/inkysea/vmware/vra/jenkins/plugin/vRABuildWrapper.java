package com.inkysea.vmware.vra.jenkins.plugin;

import com.inkysea.vmware.vra.jenkins.plugin.model.Deployment;
import com.inkysea.vmware.vra.jenkins.plugin.model.PluginParam;
import com.inkysea.vmware.vra.jenkins.plugin.model.RequestParam;
import com.inkysea.vmware.vra.jenkins.plugin.util.EnvVariableResolver;
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


public class vRABuildWrapper extends BuildWrapper {


    protected List<PluginParam> params;
    protected List<Deployment> deployments = new ArrayList<Deployment>();
    private List<RequestParam> requestParams;



    @DataBoundConstructor
    public vRABuildWrapper( List<PluginParam> params) {
        this.params = params;
    }

    // required for to write the deployment properties back to Jenkins environment variables.
    @Override
    public void makeBuildVariables(AbstractBuild build,
                                   Map<String, String> variables){

        int counter = 1;

        for (Deployment deployment : deployments) {
            //change counter to string and append be for build environment
            String strCounter = Integer.toString(counter)+"be";
            variables.putAll(deployment.getDeploymentComponents(strCounter));
            counter++;
        }

    }

    @Override
    public Environment setUp(AbstractBuild build, Launcher launcher,
                             BuildListener listener) throws IOException, InterruptedException {

        EnvVars env = build.getEnvironment(listener);
        env.overrideAll(build.getBuildVariables());
        EnvVariableResolver helper = new EnvVariableResolver(build, listener);


        boolean success = true;

        int counter = 1;
        for (PluginParam param : params) {

            // Resolve any environment variables in the parameters
            PluginParam fparam = new PluginParam(helper.replaceBuildParamWithValue(param.getServerUrl()),
                    helper.replaceBuildParamWithValue(param.getUserName()),
                    helper.replaceBuildParamWithValue(param.getPassword()),
                    helper.replaceBuildParamWithValue(param.getTenant()),
                    helper.replaceBuildParamWithValue(param.getBluePrintName()),
                    param.isWaitExec(), param.getRequestParams());

            final Deployment deployment = newDeployment(listener.getLogger(), fparam);

                if (deployment.Create()) {
                    this.deployments.add(deployment);
                    //change counter to string and append be for build environment
                    String strCounter = Integer.toString(counter)+"be";
                    env.putAll(deployment.getDeploymentComponents(strCounter));
                    counter++;
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
