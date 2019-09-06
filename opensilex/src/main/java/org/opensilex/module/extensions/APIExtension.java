/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.opensilex.module.extensions;

import java.util.ArrayList;
import java.util.List;
import org.opensilex.server.rest.RestApplication;

/**
 *
 * @author vincent
 */
public interface APIExtension {

    /**
     * This method is called during application initialization to get all
     * packages to scan for components like request filters or response mapper
     *
     * @return List of packages to scan
     */
    public default List<String> getPackagesToScan() {
        List<String> list = new ArrayList<>();
        list.addAll(apiPackages());

        return list;
    }

    /**
     * This method is called during application initialization to get all
     * packages to scan for jersey web services wich will be displayed into
     * swagger UI
     *
     * @return List of packages to scan for web services
     */
    public default List<String> apiPackages() {
        List<String> list = new ArrayList<>();
        list.add(getClass().getPackage().getName());

        return list;
    }

    /**
     * This entry point allow module to initialize anything in application after
     * all configuration is loaded at the end of application loading
     */
    public default void initAPI(RestApplication resourceConfig) {
        // Do nothing by default; 
    }
}