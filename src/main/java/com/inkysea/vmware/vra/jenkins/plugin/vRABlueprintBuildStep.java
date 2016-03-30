/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.inkysea.vmware.vra.jenkins.plugin;

import com.inkysea.vmware.vra.jenkins.plugin.model.Blueprint;
import com.inkysea.vmware.vra.jenkins.plugin.model.BlueprintParam;
import com.inkysea.vmware.vra.jenkins.plugin.util.EnvVariableResolver;
import hudson.EnvVars;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.*;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Builder;
import org.apache.commons.compress.archivers.ArchiveException;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;


public class vRABlueprintBuildStep extends Builder {

	private static final Logger LOGGER = Logger.getLogger(vRABlueprintBuildStep.class.getName());

	protected List<BlueprintParam> params;
	protected List<Blueprint> blueprintList = new ArrayList<Blueprint>();

	@DataBoundConstructor
	public vRABlueprintBuildStep(List<BlueprintParam> params) {
		this.params = params;
	}

	public List<BlueprintParam> getParams() {
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
		System.out.println("Loading Blueprint !");


		int counter = 1;
		for (BlueprintParam param : params) {

			System.out.println("Creating package from directory : " + param.getBlueprintPath());


			// Resolve any environment variables in the parameters
			BlueprintParam fparam = new BlueprintParam(helper.replaceBuildParamWithValue(param.getServerUrl()),
					helper.replaceBuildParamWithValue(param.getUserName()),
					helper.replaceBuildParamWithValue(param.getPassword()),
					helper.replaceBuildParamWithValue(param.getTenant()),
					param.getPackageBlueprint(),
					env.get("WORKSPACE")+"/"+param.getBlueprintPath(),
					param.getOverWrite(),
					param.getPublishBlueprint(),
					param.getServiceCategory());

			final Blueprint blueprint = newBlueprint(listener.getLogger(), fparam);


			try {
				if (blueprint.Create()) {
					this.blueprintList.add(blueprint);

                } else {
                    build.setResult(Result.FAILURE);
                    success = false;
                    break;
                }
			} catch (ArchiveException e) {
				e.printStackTrace();
			}

		}
		return success;

	}


	protected Blueprint newBlueprint(PrintStream logger, BlueprintParam params) throws IOException {

		Boolean isURL = false;
		String recipe = null;

		return new Blueprint(logger, params);

	}

	@Override
	public BuildStepDescriptor getDescriptor() {
		return DESCRIPTOR;
	}

	@Extension
	public static final vRABlueprintBuildStep.DescriptorImpl DESCRIPTOR = new vRABlueprintBuildStep.DescriptorImpl();

	public static class DescriptorImpl extends BuildStepDescriptor<Builder> {

		@Override
		public String getDisplayName() {
                    
			return "vRealize Automation Blueprint";
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
