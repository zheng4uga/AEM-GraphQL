package com.aem.graphql.core.models;

import com.aem.graphql.core.services.datafetcher.MapDataFetcher;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLSchema;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
import java.util.HashMap;
import java.util.Map;

import static graphql.Scalars.*;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;


@Model(adaptables = SlingHttpServletRequest.class,defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class GraphQLAdapter {

    @Self
    SlingHttpServletRequest request;

    @SlingObject
    Resource resource;

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    static final Map<Integer, GraphQLScalarType> propertiesMapper = new HashMap<Integer, GraphQLScalarType>(){{
        put(PropertyType.STRING,GraphQLString);
        put(PropertyType.BOOLEAN,GraphQLBoolean);
        put(PropertyType.LONG,GraphQLLong);
        put(PropertyType.NAME,GraphQLString);
        put(PropertyType.DOUBLE,GraphQLBigDecimal);
        put(PropertyType.DECIMAL,GraphQLBigDecimal);
        put(PropertyType.DATE,GraphQLString);
    }};
    GraphQLObjectType resourceGraphQLType;
    GraphQLObjectType queryType;
    GraphQL graphQL;


    @PostConstruct
    protected void init(){
        buildResourceGraphQLType();
        buildQueryType();
        if(null !=queryType && null != resourceGraphQLType){
            // now we build the GraphQL
            GraphQLSchema schema = GraphQLSchema.newSchema().query(queryType).build();
            graphQL = GraphQL.newGraphQL(schema).build();
        }
    }
    private void buildResourceGraphQLType(){
        Node node = resource.adaptTo(Node.class);
        //GraphQLObjectType resourceQL=null;
        if(node !=null){
            try {
                GraphQLObjectType.Builder builder = GraphQLObjectType.newObject().name("ResourceSchema")
                        .description("Dynamically build an AEM resource schema");
                PropertyIterator propertyIterator = node.getProperties();
                while (propertyIterator.hasNext()) {
                    Property property = propertyIterator.nextProperty();
                    String propName=property.getName();
                    if (property != null) {
                        int type = property.getType();
                        if (propertiesMapper.containsKey(type)) {
                            builder.field(
                                    newFieldDefinition()
                                            .name(propName.substring(propName.indexOf(':')+1))
                                            .type(propertiesMapper.get(type))

                            );
                        }
                    }
                }
                resourceGraphQLType = builder.build();
            }catch (Exception e){
                log.error("Exception :: {}",e);
            }
        }
    }

    private void buildQueryType(){
         queryType  = GraphQLObjectType.newObject().name("query").field(
                        newFieldDefinition()
                                .name("resource")
                                .type(resourceGraphQLType)
                                .dataFetcher(new MapDataFetcher(request))
                ).build();
    }
    // throwing generic exception
    public ExecutionResult execute(String query) throws Exception {

        if(StringUtils.isNotEmpty(query) && graphQL !=null){
            return graphQL.execute(query);
        }else{
            throw new Exception("Query or GraphQL object cannot be null");
        }

    }
}
