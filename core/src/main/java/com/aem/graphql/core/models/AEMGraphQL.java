package com.aem.graphql.core.models;

import com.aem.graphql.core.services.GraphqlProperties;
import com.aem.graphql.core.services.datafetcher.MapDataFetcher;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLSchema;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;

import javax.jcr.*;

import java.util.HashMap;
import java.util.Map;

import static graphql.Scalars.*;
import static graphql.Scalars.GraphQLBigDecimal;
import static graphql.Scalars.GraphQLString;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;

public abstract class AEMGraphQL {

   protected static final Map<Integer, GraphQLScalarType> propertiesMapper = new HashMap<Integer, GraphQLScalarType>(){{
        put(PropertyType.STRING,GraphQLString);
        put(PropertyType.BOOLEAN,GraphQLBoolean);
        put(PropertyType.LONG,GraphQLLong);
        put(PropertyType.NAME,GraphQLString);
        put(PropertyType.DOUBLE,GraphQLBigDecimal);
        put(PropertyType.DECIMAL,GraphQLBigDecimal);
        put(PropertyType.DATE,GraphQLString);
    }};

   protected GraphQLObjectType resourceGraphQLType;
   protected GraphQLObjectType queryType;
   protected GraphQL graphQL;

   @OSGiService
   private GraphqlProperties graphqlProperties;

   abstract void init() throws Exception;

   protected void initQuery(SlingHttpServletRequest request, Resource resource) throws Exception {
       GraphQLObjectType.Builder rootBuilder = buildResourceGraphQLType(resource.adaptTo(Node.class));
       resourceGraphQLType = rootBuilder.build();// build the main root builder
       buildQueryType(request);
       if(null !=queryType && null != resourceGraphQLType){
           // now we build the GraphQL
           GraphQLSchema schema = GraphQLSchema.newSchema().query(queryType).build();
           graphQL = GraphQL.newGraphQL(schema).build();
       }
   }


    //TODO: this type of recursion in jcr:root is very dangerous as it could potential lead to DDOS, so we need to add a max depth
    protected GraphQLObjectType.Builder buildResourceGraphQLType(Node node) throws Exception {
        // handle first conversion
        GraphQLObjectType.Builder currentBuilder = convertNodeToGraphQLObjectType(node);
        if(!node.hasNodes()){
            // meaning there isn't anymore node left
            return currentBuilder;
        }
        // if it gets here it mean it has more node so we need to recursively build the object
        NodeIterator nodeIterator = node.getNodes();
        while(nodeIterator.hasNext()){
            Node currentNode = nodeIterator.nextNode();
            // add each field to previously built graphqlObject
            if(currentNode !=null) {
                currentBuilder.field(newFieldDefinition().name(currentNode.getName().replace(":","_")).type(buildResourceGraphQLType(currentNode)));
            }
        }
        return currentBuilder;
    }

    protected GraphQLObjectType.Builder convertNodeToGraphQLObjectType(Node node) throws Exception {
        if(node !=null){
            GraphQLObjectType.Builder builder = GraphQLObjectType.newObject().name(node.getName().replace(":","_")).description(node.getPath());
            PropertyIterator propertyIterator = node.getProperties();
            while (propertyIterator.hasNext()) {
                Property property = propertyIterator.nextProperty();
                String propName= property.getName();
                //TODO: need to add a ignorable property so certain property can be blacklist
                if (property != null) {
                    int type = property.getType();
                    if (propertiesMapper.containsKey(type) && this.graphqlProperties.shouldAdd(propName)) {
                           builder.field(
                                   newFieldDefinition()
                                           .name(propName.replace(":", "_"))
                                           .type(propertiesMapper.get(type)));

                    }
                }
            }
            return builder;
        }else{
            // we need to throw some sort of exception here
            throw new Exception("Node use to convert to GraphQL object cannot be null");
        }
    }

    protected void buildQueryType(SlingHttpServletRequest request){
        queryType  = GraphQLObjectType.newObject().name("query").field(
                newFieldDefinition()
                        .name("resource")
                        .type(resourceGraphQLType)
                        .dataFetcher(new MapDataFetcher(request,graphqlProperties))
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
