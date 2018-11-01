/*
 *  Copyright 2015 Adobe Systems Incorporated
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.aem.graphql.core.servlets;

import com.aem.graphql.core.models.GraphQLAdapter;
import com.aem.graphql.core.services.GraphqlService;
import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.ExecutionResult;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import java.util.stream.Collectors;



/**
 * Servlet that writes some sample content into the response. It is mounted for
 * all resources of a specific Sling resource type. The
 * {@link SlingSafeMethodsServlet} shall be used for HTTP methods that are
 * idempotent. For write operations use the {@link SlingAllMethodsServlet}.
 */
@Component(service=Servlet.class,
           immediate = true,
           property={
                   Constants.SERVICE_DESCRIPTION + "=GraphQL Endpoint",
                   "sling.servlet.methods=" + HttpConstants.METHOD_POST,
                   "sling.servlet.resourceTypes=/apps/weretail/components/form/text",
                   "sling.servlet.selectors=graphql",
                   "sling.servlet.extensions=json"
           })
public class GraphqlServlet extends SlingAllMethodsServlet {

    private static Logger log = LoggerFactory.getLogger(GraphqlServlet.class);


    @Reference
    GraphqlService graphqlService;

    @Override
    protected void doPost(final SlingHttpServletRequest req,
                          final SlingHttpServletResponse resp) {
        resp.addHeader("Content-Type","application/json");
        try {
                GraphQLAdapter graphQLAdapter = req.adaptTo(GraphQLAdapter.class);
                String query = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
                if (StringUtils.isNotEmpty(query)) {
                    graphqlService.getGraphQL(req).execute(query);
                    ExecutionResult executionResult = graphQLAdapter.execute(query);
                    String response = new ObjectMapper().writeValueAsString(executionResult);
                    resp.getWriter().print(response);
                    resp.getWriter().flush();
                }

        }catch (Exception ex){
            log.error("Exception: ",ex);
        }
    }
}
