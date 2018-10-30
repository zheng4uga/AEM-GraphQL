package com.aem.graphql.core.services;

import graphql.GraphQL;
import org.apache.sling.api.SlingHttpServletRequest;

public interface GraphqlService {

    GraphQL getGraphQL(SlingHttpServletRequest request);
}
