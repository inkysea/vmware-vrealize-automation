package com.inkysea.vmware.vra.jenkins.plugin.model

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.junit.Test
import groovy.util.GroovyTestCase;

/**
 * Created by kthieler on 2/23/16.
 */
class BlueprintTest extends GroovyTestCase {

    private BlueprintParam params;
    private PrintStream logger;


    protected List<Deployment> deployments = new ArrayList<Deployment>();
    private String cpu = "{ \"data\":{\"CentOS7\":{\"data\":{\"cpu\":2}}}}";
    private List<RequestParam> requestParam = new ArrayList<RequestParam>();


    BlueprintTest() {

        Properties prop = new Properties();
        InputStream input = null;

        try {

            String filename = "config.properties";

            input =  this.getClass().getClassLoader().getResourceAsStream(filename);

            if(input==null){
                System.out.println("Sorry, unable to find " + filename);
                return;
            }

            prop.load(input);
            this.params = new BlueprintParam(prop.getProperty("vRAURL"),
                    prop.getProperty("userName"),
                    prop.getProperty("password"),
                    prop.getProperty("tenant"),
                    prop.getProperty("bluePrintName"),
                    Boolean.valueOf(prop.getProperty("packageBlueprint")),
                    prop.getProperty("blueprintPath"),
                    Boolean.valueOf(prop.getProperty("overWrite").asBoolean()));

        } catch (IOException ex) {
            ex.printStackTrace();
        } finally{
            if(input!=null){
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    @Test
    public void testBluePrintCreate() {
        Blueprint blueprint = new Blueprint(logger, params);

        blueprint.Create();


    }




    @Test
    public void testBlueprintDestroy() {

        System.out.println("test holder");

    }

}


