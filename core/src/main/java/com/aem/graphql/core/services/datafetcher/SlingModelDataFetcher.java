package com.aem.graphql.core.services.datafetcher;

import com.aem.graphql.core.models.ResourceSchemaModel;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SlingModelDataFetcher implements DataFetcher<ResourceSchemaModel> {

    private static Logger log = LoggerFactory.getLogger(SlingModelDataFetcher.class);

    private SlingHttpServletRequest request;

    public SlingModelDataFetcher(SlingHttpServletRequest request){
        this.request = request;
    }
    @Override
    public ResourceSchemaModel get(DataFetchingEnvironment dataFetchingEnvironment)  {
        String resourcePath = dataFetchingEnvironment.getArgument("resourcePath");
        // we are passing in the request to ensure user has access to what they want to access
        ResourceResolver rr = request.getResourceResolver();
        try {
            Resource schemaResource = rr.getResource(resourcePath);
            return schemaResource.adaptTo(ResourceSchemaModel.class);
        } catch (Exception e) {
            log.info("Exception ",e);
        }
        return null;
    }
}
