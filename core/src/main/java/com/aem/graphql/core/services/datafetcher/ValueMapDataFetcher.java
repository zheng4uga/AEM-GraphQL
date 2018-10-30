package com.aem.graphql.core.services.datafetcher;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ValueMapDataFetcher implements DataFetcher<ValueMap> {
    private static Logger log = LoggerFactory.getLogger(SlingModelDataFetcher.class);

    SlingHttpServletRequest request;

    public ValueMapDataFetcher(SlingHttpServletRequest request){
        this.request = request;
    }
    @Override
    public ValueMap get(DataFetchingEnvironment dataFetchingEnvironment) {
        String resourcePath = dataFetchingEnvironment.getArgument("resourcePath");
        // we are passing in the request to ensure user has access to what they want to access
        ResourceResolver rr = request.getResourceResolver();
        try {
            Resource schemaResource = rr.getResource(resourcePath);
            ValueMap properties = schemaResource.adaptTo(ValueMap.class);
            return properties;
        } catch (Exception e) {
            log.info("Exception ",e);
        }
        return null;
    }
}
