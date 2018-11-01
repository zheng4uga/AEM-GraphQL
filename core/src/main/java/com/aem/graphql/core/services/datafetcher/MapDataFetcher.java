package com.aem.graphql.core.services.datafetcher;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class MapDataFetcher implements DataFetcher<Map> {
    private static Logger log = LoggerFactory.getLogger(MapDataFetcher.class);

    SlingHttpServletRequest request;

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    public MapDataFetcher(SlingHttpServletRequest request){
        this.request = request;
    }
    @Override
    public Map get(DataFetchingEnvironment dataFetchingEnvironment) {
        //String resourcePath = dataFetchingEnvironment.getArgument("resourcePath");
        // we are passing in the request to ensure user has access to what they want to access
        try {
            Map<String,Object> resultMap = new HashMap<>();
            Resource schemaResource = request.getResource();
            Node schemaNode = schemaResource.adaptTo(Node.class);
            PropertyIterator propertyIterator = schemaNode.getProperties();
            while(propertyIterator.hasNext()){
                Property property = propertyIterator.nextProperty();
                String name = property.getName();
                String processedName = name.substring(name.indexOf(':')+1);
                switch(property.getType()){
                    case PropertyType.DATE :
                        Calendar currentCalendar =  property.getDate();
                        resultMap.put(processedName,sdf.format(currentCalendar.getTime()));
                        break;
                    case PropertyType.BOOLEAN:
                        resultMap.put(processedName,property.getBoolean());
                        break;
                    case PropertyType.DOUBLE:
                        resultMap.put(processedName,property.getDouble());
                        break;
                    case PropertyType.DECIMAL:
                        resultMap.put(processedName,property.getDecimal());
                        break;
                    case PropertyType.NAME:
                        resultMap.put(processedName,property.getString());
                        break;
                    case PropertyType.LONG:
                        resultMap.put(processedName,property.getLong());
                        break;
                    default:
                        resultMap.put(processedName,property.getString());
                        break;
                }
            }
            return resultMap;
        } catch (Exception e) {
            log.info("Exception ",e);
        }
        return null;
    }
}
