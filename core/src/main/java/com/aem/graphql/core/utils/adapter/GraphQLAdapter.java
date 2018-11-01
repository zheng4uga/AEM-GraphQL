package com.aem.graphql.core.utils.adapter;

import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLScalarType;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.TypeRuntimeWiring;
import org.apache.sling.api.adapter.AdapterFactory;
import org.apache.sling.api.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.*;

import java.util.HashMap;
import java.util.Map;

import static graphql.Scalars.GraphQLBoolean;
import static graphql.Scalars.GraphQLLong;
import static graphql.Scalars.GraphQLString;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;

public class GraphQLAdapter implements AdapterFactory {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    Map<Integer, GraphQLScalarType> propertiesMapper = new HashMap<Integer, GraphQLScalarType>(){{
        this.put(PropertyType.STRING,GraphQLString);
        this.put(PropertyType.BOOLEAN,GraphQLBoolean);
        this.put(PropertyType.LONG,GraphQLLong);
    }};

    @Override
    public <AdapterType> AdapterType getAdapter(Object o, Class<AdapterType> aClass) {
        if(o!=null && o instanceof Resource){
            Resource currentResource = (Resource) o;
            try {
                return (AdapterType) convertResourceToGraphQLType(currentResource)
            } catch (Exception e) {
                log.error("Error:: {}",e)
            }

        }
        return null;
    }

    private GraphQLObjectType convertResourceToGraphQLType(Resource resource) throws Exception {
        Node node = resource.adaptTo(Node.class);
        GraphQLObjectType resourceQL=null;
        if(node !=null){
            GraphQLObjectType.Builder builder = GraphQLObjectType.newObject().name("ResourceQL").description("Build ResourceQL Object");
            PropertyIterator propertyIterator = node.getProperties();
            while(propertyIterator.hasNext()){
                Property property = propertyIterator.nextProperty();
                if(property !=null) {
                    int type = property.getType();
                    if (propertiesMapper.containsKey(type)) {
                        builder.field(
                                newFieldDefinition()
                                        .name(property.getName())
                                        .type(propertiesMapper.get(type))

                        );
                    }
                }
            }
            resourceQL = builder.build();
        }
        return resourceQL;
    }
}

