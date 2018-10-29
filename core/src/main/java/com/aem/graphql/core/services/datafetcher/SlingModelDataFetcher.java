package com.aem.graphql.core.services.datafetcher;

import com.aem.graphql.core.models.ResourceSchemaModel;
import com.aem.graphql.core.utils.ServiceUserUtils;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(service = SlingModelDataFetcher.class)
public class SlingModelDataFetcher implements DataFetcher<ResourceSchemaModel> {

    private static Logger log = LoggerFactory.getLogger(SlingModelDataFetcher.class);

    @Reference
    ResourceResolverFactory resourceResolverFactory;

    @Override
    public ResourceSchemaModel get(DataFetchingEnvironment dataFetchingEnvironment)  {
        String resourcePath = dataFetchingEnvironment.getArgument("resourcePath");
        ResourceResolver rr = null;
        try {
            rr = ServiceUserUtils.getServiceResourceResolver("content-user",resourceResolverFactory);
            Resource schemaResource = rr.getResource(resourcePath);
            return schemaResource.adaptTo(ResourceSchemaModel.class);
        } catch (Exception e) {
            log.info("Exception ",e);
        }finally {
            if(rr != null && rr.isLive()){
                rr.close();
            }
        }
        return null;
    }
}
