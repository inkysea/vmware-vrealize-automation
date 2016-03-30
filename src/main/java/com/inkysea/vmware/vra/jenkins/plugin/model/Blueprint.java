package com.inkysea.vmware.vra.jenkins.plugin.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.inkysea.vmware.vra.jenkins.plugin.util.zip;
import org.apache.commons.compress.archivers.ArchiveException;

import java.io.IOException;
import java.io.PrintStream;

/**
 * Created by kthieler on 3/8/16.
 */
public class Blueprint {

    private BlueprintParam params;
    private PrintStream logger;
    private Request request;


    public Blueprint(PrintStream logger, BlueprintParam params) throws IOException {

        this.params = params;
        this.logger = logger;

        this.request = new Request(logger, params.getServerUrl(),
                params.getUserName(),
                params.getPassword(),
                params.getTenant());

    }

    public boolean Create() throws IOException, InterruptedException, ArchiveException {

        JsonObject validate = null;

        if( this.params.getBluePrintName() == null ){
            // Package Blueprint as zip file and load it to vRA

            System.out.println("Loading blueprint using package  : ");


            String zipFile = this.params.getBluePrintName()+".zip";

            // validate package



            zip zipPackage = new zip( zipFile , this.params.getBlueprintPath(), this.params.getBlueprintPath());
            logger.println("Creating package from directory : "+this.params.getBlueprintPath());

            zipPackage.Create();

            // validate package
            validate = request.validatePackages(this.params.getBlueprintPath()+"/"+zipFile, "overwrite");


            String status = validate.getAsJsonObject().get("operationStatus").getAsString();

            if( status.equalsIgnoreCase("FAILED") ){
                logger.println("Failed to load blueprint error is : "+validate.getAsString());
                return false;
            }else{
                logger.println("Blueprint Validation Result: " + validate.toString());

            }

            // load package
            validate = request.Packages(this.params.getBlueprintPath()+"/"+zipFile, "overwrite" );

            status = validate.getAsJsonObject().get("operationStatus").getAsString();

            if( status.equalsIgnoreCase("FAILED") ){
                logger.println("Failed to load blueprint error is : "+validate.getAsString());
                return false;
            }else{
                logger.println("Blueprint Load Result: " + validate.toString());

            }

        }else{
            // Simple load of blueprint. Only the YAML file
            if (this.params.getOverWrite()) {
                //
            }else{
            }
        }

        // Only if publish blueprint flag is set
        // Note that all blueprints in a package will be assigned to the same service category.
        if ( this.params.getPublishBlueprint() ){


            System.out.println("Printing Array :"+validate.getAsJsonArray("operationResults"));
            System.out.println(" Array length:"+validate.getAsJsonArray("operationResults").getAsJsonArray().size());

            for ( JsonElement result : validate.getAsJsonArray("operationResults").getAsJsonArray() ){

                // get the contentName for each  "contentTypeId": "composite-blueprint"
                // so we can publish them.
                System.out.println("Printing result :"+result);

                String contentTypeId = result.getAsJsonObject().get("contentTypeId").getAsString();
                System.out.println("Printing contentTypeId :"+contentTypeId);


                if ( contentTypeId.equalsIgnoreCase("composite-blueprint")){

                    // Check if blueprint is draft or published
                    // calls {{vRAURL}}/composition-service/api/blueprints/blueprintName

                    String contentName = result.getAsJsonObject().get("contentName").getAsString();

                    System.out.println("Printing Content :"+contentName);

                    JsonObject blueprintJson = this.request.GetBlueprint(contentName);

                    System.out.println("Printing blueprintJson :"+blueprintJson);

                    String publishStatus = blueprintJson.getAsJsonArray("content").get(0)
                                                        .getAsJsonObject().get("publishStatus").getAsString();

                    System.out.println("Printing publishStatus :"+publishStatus);

                    // re-assign any previously published blueprints that are included in this package if flag is set.
                    if ( (this.params.getPublishBlueprint() && publishStatus.equalsIgnoreCase("PUBLISHED"))
                            || publishStatus.equalsIgnoreCase("DRAFT") ){

                        System.out.println("Publishing BP :");


                        // calls /composition-service/api/blueprints/{blueprintName}/status
                        this.request.PutBluprintStatus(contentName, "PUBLISHED");

                        // Get catalog item JSON and merge in service category,  increment the version value
                        // in JSON element and get the catalogID for later use
                        JsonObject catalogItemJSON = this.request.GetCatalogItemByName(contentName);

                        JsonArray catalogItemContentArray = catalogItemJSON.getAsJsonArray("content");

                        String catalogId = catalogItemContentArray.get(0).getAsJsonObject()
                                                                    .get("id").getAsString();

                        //catalog-service/api/services/?$filter=name+eq+'Full Stack'
                        JsonObject ServiceCategoryJson = request.getServiceCategory(params.getServiceCategory());

                        String ServiceID =  ServiceCategoryJson.getAsJsonArray("content").get(0).getAsJsonObject()
                                                                            .get("id").getAsString();


                        JsonObject catalogItemPublish = catalogItemCreate(catalogItemContentArray.get(0).getAsJsonObject(),
                                                                            ServiceID,
                                                                            params.getServiceCategory());

                        // publish blueprint to service category activating as an entitled item
                        // /catalog-service/api/catalogItems/a29501cd-179a-4be5-8096-f29ad4847521

                        System.out.println("Catalog Publish JSON " + catalogItemPublish);
                        this.request.PutCatalogItem(catalogId, catalogItemPublish.getAsJsonObject().toString());
                    }

                }
            }


        }

        return true;

    }

    public JsonObject catalogItemCreate(JsonObject catalogItemContent, String ServiceRefID, String ServiceRefName) {

        System.out.println("Catalog catalogItemContent JSON "+catalogItemContent);

        JsonObject ServiceRef = new JsonObject();

        ServiceRef.addProperty("id", ServiceRefID);
        ServiceRef.addProperty("label", ServiceRefName);

        catalogItemContent.add("serviceRef", ServiceRef);

        return catalogItemContent;
    }

}
