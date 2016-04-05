/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.inkysea.vmware.vra.jenkins.plugin;

import com.inkysea.vmware.vra.jenkins.plugin.model.Deployment;
import com.inkysea.vmware.vra.jenkins.plugin.model.PluginParam;
import com.inkysea.vmware.vra.jenkins.plugin.model.RequestParam;
import com.inkysea.vmware.vra.jenkins.plugin.util.EnvVariableResolver;
import hudson.EnvVars;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.*;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.BuildWrapper;
import hudson.tasks.Builder;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.io.PrintStream;
import java.util.*;
import java.util.logging.Logger;



public class vRADeploymentBuildStep  extends Builder {

	private static final Logger LOGGER = Logger.getLogger(vRADeploymentBuildStep.class.getName());

	protected List<PluginParam> params;
	protected List<Deployment> deployments = new ArrayList<Deployment>();
	private List<RequestParam> requestParams;

	@DataBoundConstructor
	public vRADeploymentBuildStep(List<PluginParam> params) {
		this.params = params;
	}

	public List<PluginParam> getParams() {
		return params;
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
		for (PluginParam param : params) {


				// Resolve any build variables included in the request paramaters.
			List<RequestParam> rparamResolved = new ArrayList<RequestParam>();;

				if ( ! (null == param.getRequestParams()) || param.getRequestParams().isEmpty()) {


					for (RequestParam rparam : param.getRequestParams()) {
						String rparamString = helper.replaceBuildParamWithValue(rparam.getRequestParam().toString());
						rparamResolved.add(new RequestParam(rparamString));
					}
				}

			// Resolve any environment variables in the parameters
			PluginParam fparam = new PluginParam(helper.replaceBuildParamWithValue(param.getServerUrl()),
					helper.replaceBuildParamWithValue(param.getUserName()),
					helper.replaceBuildParamWithValue(param.getPassword()),
					helper.replaceBuildParamWithValue(param.getTenant()),
					helper.replaceBuildParamWithValue(param.getBluePrintName()),
					param.isWaitExec(),param.getRequestTemplate(), rparamResolved);

			final Deployment deployment = newDeployment(listener.getLogger(), fparam);


			if (deployment.Create()) {
				this.deployments.add(deployment);
				//change counter to string and append bs for build step
				String strCounter = "BS_"+Integer.toString(counter);

				env.putAll(deployment.getDeploymentComponents(strCounter));

				build.addAction(new PublishEnvVarAction(deployment.getDeploymentComponents(strCounter)));

				counter++;
			} else {
				build.setResult(Result.FAILURE);
				success = false;
				break;
			}

		}
		return success;

	}


	protected Deployment newDeployment(PrintStream logger, PluginParam params) throws IOException {

		Boolean isURL = false;
		String recipe = null;

		return new Deployment(logger, params);

	}

	@Override
	public BuildStepDescriptor getDescriptor() {
		return DESCRIPTOR;
	}

	@Extension
	public static final vRADeploymentBuildStep.DescriptorImpl DESCRIPTOR = new vRADeploymentBuildStep.DescriptorImpl();

	public static class DescriptorImpl extends BuildStepDescriptor<Builder> {

		@Override
		public String getDisplayName() {
                    
			return "vRealize Automation Deployment";
		}

		@Override
		public boolean isApplicable(Class<? extends AbstractProject> jobType) {
			return true;
		}
                
	}

	public class PublishEnvVarAction extends InvisibleAction implements EnvironmentContributingAction {
	// Required class to write variables back to Jenkins when part of a build step

		private Map<String, String> variables;

		public PublishEnvVarAction(Map<String, String> deploymentComponents) {
			this.variables = deploymentComponents;

		}


		public void buildEnvVars(AbstractBuild<?, ?> build, EnvVars env) {
			env.putAll(variables);
		}

	}

}
