package com.aem.graphql.core.services.impl;

import com.aem.graphql.core.services.GraphqlService;
import com.aem.graphql.core.services.datafetcher.SlingModelDataFetcher;
import com.aem.graphql.core.utils.ServiceUserUtils;
import graphql.GraphQL;
import graphql.schema.*;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import static graphql.Scalars.GraphQLString;

@Component(service = GraphqlService.class,
    immediate = true,
    property = {
            Constants.SERVICE_DESCRIPTION + "=GraphQL Service"
    }
)
@Designate(ocd=GraphqlServiceImpl.Config.class)
public class GraphqlServiceImpl implements GraphqlService {

    private static Logger log = LoggerFactory.getLogger(GraphqlServiceImpl.class);

    private GraphQL graphQL;

    @Reference
    ResourceResolverFactory resourceResolverFactory;

    @Reference
    SlingModelDataFetcher slingModelDataFetcher;

    @ObjectClassDefinition(name="GraphQL Service",
            description = "Provides an service to use to map graphql schema")
    public static @interface Config{
        @AttributeDefinition(name="GraphQL Schema File",description = "Location of where the GraphQL schema file")
        String[] getSchemaFile() default {"/etc/graphql/schema/resource.graphql"};
    }

    public GraphQL getGraphQL() {
        return graphQL;
    }

    @Activate
    public void activate(GraphqlServiceImpl.Config config) {
        String[] schemaFiles = config.getSchemaFile();
        ResourceResolver rr = null;
        try {
            rr = ServiceUserUtils.getServiceResourceResolver("graphql-user", resourceResolverFactory);
            if(rr !=null && schemaFiles !=null) {
                this.buildGraphQL(schemaFiles,rr);
            }
        }catch (Exception ex){
            log.error("Exception: ",ex);
        }finally{
            if(rr !=null && rr.isLive()){
                rr.close();
            }
        }
    }
    private void buildGraphQL(String[] schemaFiles, ResourceResolver rr){
        TypeDefinitionRegistry typeRegistry = new TypeDefinitionRegistry();
        SchemaParser schemaParser = new SchemaParser();
        SchemaGenerator schemaGenerator = new SchemaGenerator();
        for(String filePath:schemaFiles){
           Resource schemaFileRes = rr.getResource(filePath);
           if(null != schemaFileRes){
               InputStream resourceStream = schemaFileRes.adaptTo(InputStream.class);
               Reader resourceStreamReader = new InputStreamReader(resourceStream);
               typeRegistry.merge(schemaParser.parse(resourceStreamReader));
           }
        }
        GraphQLSchema schema = schemaGenerator.makeExecutableSchema(typeRegistry,this.buildRuntimeWiring());
        if(null != schema){
            graphQL = GraphQL.newGraphQL(schema).build();
        }
    }

    private RuntimeWiring buildRuntimeWiring(){
         return RuntimeWiring.newRuntimeWiring()
                .type("Query", typeWiring -> typeWiring
                        .dataFetcher("resource", slingModelDataFetcher))
                .build();
    }

    private RuntimeWiring buildCustomRuntimeWiring(){
        return null;
    }

}
