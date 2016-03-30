# Jenkins vRealize Automation Plugin

The vRealize Automation Jenkins plugin enables Jenkins to provision vRealize Automation 7 Blueprints.  


## Requirements


* Jenkins 1.58+
* Java 8 to compile plugin or Java 7 if just running in Jenkins
* vRealize Automation 7 
* Maven if compiling the plugin
       
    
## How to Configure for Deployments

For Deployments, the vRealize Automation plugin can be configured as a build environment, build step and a post build action.

1) Build Environment : Check the "Create vRealize Automation Deployment".  Note that jenkins will automatically 
destroy any environments provisioned as part of the build once the build completes. Environment details will be 
written to Jenkins as environment variables. 

![Build Environemnt](/doc/vRAPlugin_BuildEnvironment.png)

2) Build step : On the Job configuration page, select Add build step and choose “vRealize Automation Deployment”. 
Environment details will be  written to Jenkins as environment variables. 

![Build step](/doc/vRA_Build.png)

3) Post-Build Action : Select "Add Post Build" and choose "vRealize Automation Deployment".

![Post Build Action](/doc/vRA_PostBuildmenu.png)


Configure :  Configure the plugin as shown

  * vRealize Automation URL -   The URL for vRealize Automation. 
  * Tenant - vRealize Automation tenant to be used
  * User Name - Username for given tenant. User must be entittled to provision the blueprint in vRealize Automation
  * Password - Password for  user
  * Blueprint - The name of the blueprint to be provisioned.
  * Execute and Wait - If checked the Jenkins job will wait for the blueprint to be provisioned
  * Add Deployment Configuration - Deployment configuration parameters can be specified to adjust settings such as CPU.
   Parameters are specified as JSON.

![Configure](/doc/vRA_BuildStep.png)    

       
    The JSON for parameters can be determined by looking at the JSON blueprint template from vRA.  The template for a 
    blueprint can be viewed by running a build job and looking at the console output or using the vRealize cloudclient. 
    See below for the JSON blueprint logged to the build console.
     
![Configure](/doc/console.png)    
     
A deployment can be destroyed using the Post-build Action, "Destroy vRealize Automation Deployment". Note that 
environment variables can be used to specify the deployment name to be deleted.
 
![Configure](/doc/vRA_PostDestroy.png)    

Environment details are written back to Jenkins as environment variables.  The following convention is used to name the
variables.

For deployments from Build Environment:

VRADEP_BE_NUMBER_NAME  :  Provides the deployment name  where *NUMBER* is an incrementing number starting from 1. The number coresponds
to the order of deployments specified under the build environment section.

example: VRADEP_BE_1_NAME=CentOS_7-18373323

VRADEP_BE_NUMBER_TENANT  :  Provides the tenant name of the deployment.  where <NUMBER> is an incrementing number starting from 1. The number coresponds
to the order of deployments specified under the build environment section.

example: VRADEP_BE_1_TENANT=vsphere.local

For deployments from a Build Step:

The naming for a deployment from a build step is the same as build environment.  However, the BE is replaced with BS. 
Note that the incrementing number has local scope. Meaning a deployment in build environment will not increment the number
in build step.

example: VRADEP_BS_1_NAME=CentOS_7-18373323

example: VRADEP_BS_1_TENANT=vsphere.local

The naming for a deployment from a Post-Build action is the same as build environment.  However, the BE is replaced with PB. 
Note that the incrementing number has local scope. 

example: VRADEP_PB_1_NAME=CentOS_7-18373323

example: VRADEP_PB_1_TENANT=vsphere.local

Details for each machine and NSX load balancer included in a deployment are also written as environment variables.  The 
variables can be resolved using the deployment name and tenant from above.

For example:

VRADEP_PB_1_COMPONENT#_NAME
VRADEP_BS_1_COMPONENT1_NAME=CENTOS7

VRADEP_PB_1_COMPONENT_MACHINE#_NAME 

VRADEP_BS_1_COMPONENT1_MACHINE1_NAME=CLOUDADMINS0167

VRADEP_PB_1_COMPONENT_MACHINE#_NETWORK#_NAME 

VRADEP_BS_1_COMPONENT1_MACHINE1_NETWORK1_NAME=DPORTGROUP

VRADEP_PB_1_COMPONENT_MACHINE#_NETWORK#_IP# 

VRADEP_BS_1_COMPONENT1_MACHINE1_NETWORK1_IP1=10.25.27.104


An example of an environment variable for an NSX load balancer:

VRADEP_BS_#_LB#_NAME

VRADEP_BS_1_LB1_NAME=WP-LOADBALANCER-055D64D8-7162-4A96-B8B5-7936ED41B149

VRADEP_BS_#_LB#_NAME

VRADEP_BS_1_LB1_SERVICES=Virtual IP: 192.168.110.211, Services: HTTP


## How to Configure for Blueprints as Code

The plugin enables infrastructure as code for vRealize Automation blueprints that have been exported as YAML.  
The current version of the plugin as of 1.3 only support packaged blueprints which include the necessary composite-blueprint
and software components.  The following screenshot is an example file structure for a blueprint package.  

![Configure](/doc/blueprintFileStructure.png)    

To configure the Jenkins plugin for blueprints, apply the credentials to authenticate to vRA. Apply the following settings.
  
  1.  Blueprint Path is the relative path to the directory containing your blueprint.
  
  2.  The Publish Blueprint determines if the blueprint will be published in vRA or left as a draft.  To deploy a blueprint
  the blueprint must be published.
  
  3.  Assign to Service Category publishes the blueprint to a service category in vRA.  Note that the Service Category must already
  exist in vRA.  If this is an update to a blueprint that already belongs to a service category, changing this field will assign the 
  blueprint to the service category specified in this field.
  
![Configure](/doc/BPConfig.png)    


## Compile and Installation

To compile you must have maven installed.

To compile and run jenkins locally for testing

    * mvn hpi:run

To compile only the plugin 

    * mvn hpi:hpi

Once you have the .hpi file, use the plugin management console (http://example.com:8080/pluginManager/advanced) to upload the hpi file. You must restart Jenkins after the install.