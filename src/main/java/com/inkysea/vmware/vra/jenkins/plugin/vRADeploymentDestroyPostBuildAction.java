/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.inkysea.vmware.vra.jenkins.plugin;

import com.inkysea.vmware.vra.jenkins.plugin.model.Deployment;
import com.inkysea.vmware.vra.jenkins.plugin.model.DestroyParam;
import com.inkysea.vmware.vra.jenkins.plugin.model.PluginParam;
import com.inkysea.vmware.vra.jenkins.plugin.model.RequestParam;
import com.inkysea.vmware.vra.jenkins.plugin.util.EnvVariableResolver;
import hudson.EnvVars;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.*;
import hudson.tasks.*;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;


public class vRADeploymentDestroyPostBuildAction extends Notifier {

	private static final Logger LOGGER = Logger.getLogger(vRADeploymentDestroyPostBuildAction.class.getName());

	protected List<DestroyParam> destroyParams;
	protected List<Deployment> deployments = new ArrayList<Deployment>();

	@DataBoundConstructor
	public vRADeploymentDestroyPostBuildAction(List<DestroyParam> destroyParams) {
		this.destroyParams = destroyParams;
	}

	// method name must match with the property field in config.jelly
	public List<DestroyParam> getDestroyParams() {
		return destroyParams;
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
	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
			throws InterruptedException, IOException {

		EnvVars env = build.getEnvironment(listener);
		env.overrideAll(build.getBuildVariables());
		EnvVariableResolver helper = new EnvVariableResolver(build, listener);


		boolean success = true;

		int counter = 1;
		for (DestroyParam param : destroyParams) {

			// Resolve any environment variables in the parameters
			DestroyParam fparam = new DestroyParam(helper.replaceBuildParamWithValue(param.getServerUrl()),
					helper.replaceBuildParamWithValue(param.getUserName()),
					helper.replaceBuildParamWithValue(param.getPassword()),
					helper.replaceBuildParamWithValue(param.getTenant()),
					helper.replaceBuildParamWithValue(param.getDeploymentName()));

			final Deployment deployment = new Deployment(listener.getLogger(), fparam);
			if(deployment.Destroy(env.expand(param.getDeploymentName()))) {
				this.deployments.add(deployment);

				LOGGER.info("Success");
				counter++;
			} else {
				LOGGER.warning("Failed");
				success = false;
				break;
			}

		}
		return success;

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
