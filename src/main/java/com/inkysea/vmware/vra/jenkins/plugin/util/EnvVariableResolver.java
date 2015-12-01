package com.inkysea.vmware.vra.jenkins.plugin.util;

import java.io.IOException;

import hudson.EnvVars;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;


import static hudson.Util.fixEmptyAndTrim;


public class EnvVariableResolver {

    private EnvVars environment;


    public EnvVariableResolver(AbstractBuild<?, ?> build, BuildListener listener) throws IOException, InterruptedException {
        environment = build.getEnvironment(listener);
        environment.overrideAll(build.getBuildVariables());
    }

    public String replaceBuildParamWithValue(String paramValue) {
        return fixEmptyAndTrim(environment.expand(paramValue));
    }


}
