# Jenkins vRealize Automation Plugin

This packer project builds and configures a vRA IAAS instance.  There are several builders included in this project.
The builders include desktop and ESX.

 

## Requirements


* Jenkins 1.58+
* Java 8 to compile plugin or Java 7 if just running in Jenkins
* vRealize Automation 7 
       
    
## How to Configure
    
Configuration
-------------

1) Build step : On Job configuration page click on Add build step select “Execute CodeStream Pipeline” option

![Build step](/doc/BuildJobSelect.png)

2) Configure :  Configure CodeStream pipeline like show in image. Below is the description of each field

  * Server URl -   vRealize CodeStream host URL
  * Tenant - User group tenant against which you want to execute the pipeline
  * User Name - Username for given tenant. User should have Release Manager or Release Engineer Role.
  * Password - Password for given user
  * Release Pipeline Name - Pipeline which you want to execute. Pipeline should be activate and its latest version will be executed.
  * Execute and Wait - If this checkbox is checked the job will wait for pipeline execution to complete or fail.
  * Add Parameter - If you want to override default value of any pipeline property then you can use this option. Click on Add Parameter and provide property name in Parameter Name. New  value in Parameter Value. For all the other properties default value will be used.

![Configure](/doc/BuildJob.png)    
    

