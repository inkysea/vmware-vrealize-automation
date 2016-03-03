# Jenkins vRealize Automation Plugin

The vRealize Automation Jenkins plugin enables Jenkins to provision vRealize Automation 7 Blueprints.  


## Requirements


* Jenkins 1.58+
* Java 8 to compile plugin or Java 7 if just running in Jenkins
* vRealize Automation 7 
* Maven if compiling the plugin
       
    
## How to Configure

The vRealize Automation plugin can be configured as a build environment, build step and a post build action.

1) Build Environment : Check the "Create vRealize Automation Deployment".

![Build Environemnt](/doc/vRAPlugin_BuildEnvironment.png)

2) Build step : On the Job configuration page, select Add build step and choose “vRealize Automation Deployment” .

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
     

## To Be Added in the future
   * Publish Blueprint - Allows Jenkins to publish a YAML Blueprint to vRealize Automation
  

## Compile and Installation

To compile you must have maven installed.

To compile and run jenkins locally for testing

    * mvn hpi:run

To compile only the plugin 

    * mvn hpi:hpi

Once you have the .hpi file, use the plugin management console (http://example.com:8080/pluginManager/advanced) to upload the hpi file. You must restart Jenkins after the install.