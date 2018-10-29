package com.aem.graphql.core.utils;

import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;

import java.util.HashMap;
import java.util.Map;

public class ServiceUserUtils {

    public static ResourceResolver getServiceResourceResolver(String serviceUser,
                                                              ResourceResolverFactory resourceResolverFactory) throws LoginException {
        Map<String,Object> userParams = new HashMap<>();
        userParams.put(ResourceResolverFactory.SUBSERVICE,serviceUser);
        return resourceResolverFactory.getServiceResourceResolver(userParams);
    }
}
