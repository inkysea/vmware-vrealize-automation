# Jenkins vRealize Automation Plugin

The vRealize Automation Jenkins plugin enables Jenkins to provision vRealize Automation 7 Blueprints.  


## Requirements


* Jenkins 1.58+
* Java 8 to compile plugin or Java 7 if just running in Jenkins
* vRealize Automation 7 
* Maven if compiling the plugin
       
    
## How to Configure
    

1) Build step : On the Job configuration page, select Add build step and choose “Provision vRealize Automation Blueprint” option

![Build step](/doc/BuildJobSelect.png)

2) Configure :  Configure the plugin as shown

  * vRealize Automation URL -   The URL for vRealize Automation. 
  * Tenant - vRealize Automation tenant to be used
  * User Name - Username for given tenant. User must be entittled to provision the blueprint in vRealize Automation
  * Password - Password for  user
  * Blueprint - The name of the blueprint to be provisioned.
  * Execute and Wait - If checked the Jenkins job will wait for the blueprint to be provisioned
  * Add Parameter - Not yet implemented! 

![Configure](/doc/BuildJob.png)    
    

## To Be Added in the future
  * Add Parameter - Add parameters to the requested blueprint.  For example: number of CPUs
  * Destroy Environment -  Post Build job to clean up the environment provisioned by Jenkins
  * Publish Blueprint - Allows Jenkins to publish a YAML Blueprint to vRealize Automation
  

## Compile and Installation

To compile you must have maven installed.

To compile and run jenkins locally for testing

    * mvn hpi:run

To compile only the plugin 

    * mvn hpi:hpi

Once you have the .hpi file, use the plugin management console (http://example.com:8080/pluginManager/advanced) to upload the hpi file. You must restart Jenkins after the install.