package com.aem.graphql.core.models;

import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLScalarType;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
import java.util.HashMap;
import java.util.Map;

import static graphql.Scalars.GraphQLBoolean;
import static graphql.Scalars.GraphQLLong;
import static graphql.Scalars.GraphQLString;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;


@Model(adaptables = Resource.class,defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class GraphQLAdapter {

    @Self
    Resource resource;

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    Map<Integer, GraphQLScalarType> propertiesMapper = new HashMap<Integer, GraphQLScalarType>(){{
        this.put(PropertyType.STRING,GraphQLString);
        this.put(PropertyType.BOOLEAN,GraphQLBoolean);
        this.put(PropertyType.LONG,GraphQLLong);
    }};


    GraphQLObjectType graphQLObjectType;

    @PostConstruct
    protected void init(){
        Node node = resource.adaptTo(Node.class);
        GraphQLObjectType resourceQL=null;
        if(node !=null){
            try {
                GraphQLObjectType.Builder builder = GraphQLObjectType.newObject().name("ResourceQL")
                        .description("Build ResourceQL Object");
                PropertyIterator propertyIterator = node.getProperties();
                while (propertyIterator.hasNext()) {
                    Property property = propertyIterator.nextProperty();
                    if (property != null) {
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

                graphQLObjectType = builder.build();
            }catch (Exception e){
                log.error("Exception :: {}",e);
            }
        }
    }

    public GraphQLObjectType getGraphQLObjectType() {
        return graphQLObjectType;
    }
}
