# Jenkins vRealize Automation Plugin

The vRealize Automation plugin for Jenkins allows Jenkins to provision blueprints using vRealize Automation 7.  


## Requirements


* Jenkins 1.58+
* Java 8 to compile plugin or Java 7 if just running in Jenkins
* vRealize Automation 7 
       
    
## How to Configure
    

1) Build step : On Job the configuration page, select Add build step and choose “Provision vRealize Automation Blueprint” option

![Build step](/doc/BuildJobSelect.png)

2) Configure :  Configure as shown

  * Server URl -   vRealize Automation host URL
  * Tenant - vRealize Automation tenant to be used
  * User Name - Username for given tenant. User must be entittled to provision the blueprint in vRealize Automation
  * Password - Password for  user
  * Blueprint - The name of the blueprint to be provisioned.
  * Execute and Wait - If checked the Jenkins job will wait for the blueprint to be provisioned
  * Add Parameter - Not yet implemented! 

![Configure](/doc/BuildJob.png)    
    

## To Be Added in the future

    * Add Parameter - Add parameters to the requested blueprint.  For example: number of CPUs.
    * Destroy Environment -  Post Build job to clean up the environment provisioned by Jenkins
    * Publish Blueprint - Allows Jenkins to publish a YAML Blueprint to vRealize Automation
    