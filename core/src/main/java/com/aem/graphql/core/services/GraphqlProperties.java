package com.aem.graphql.core.services;

import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

import java.util.Arrays;
import java.util.List;

@Component(service = GraphqlProperties.class,
        immediate = true,
        property = {
                Constants.SERVICE_DESCRIPTION + "= Set strategy on what properties can be build "
        }
)
@Designate(ocd= GraphqlProperties.Config.class)
public class GraphqlProperties {
    private List<String> properties;
    private boolean blacklist;

    @ObjectClassDefinition(name="GraphQL Property Strategy",
            description = "Provides an properties interface to use to decide what properties should be build")
    public static @interface Config {
        @AttributeDefinition(name = "GraphQL Properties", description = "Location of where the GraphQL schema file")
        String[] properties() default {
                "jcr:primaryType",
                "cr:createdBy",
                "cr:lastModified",
                "jcr:lastModifiedBy",
                "sling:resourceType",
                "cq:lastRolledout",
                "cq:lastRolledoutBy",
                "jcr:mixinTypes"
        };

        @AttributeDefinition(name = "Black/White list strategy", description = "Check to use white list strategy")
        boolean strategy() default true;
    }

    @Activate
    public void activate(GraphqlProperties.Config config) {
       blacklist = config.strategy();
       properties = Arrays.asList(config.properties());
    }

    public boolean shouldAdd(String propName){
        return (blacklist && !properties.contains(propName)) || (!blacklist && properties.contains(propName));
    }

}
