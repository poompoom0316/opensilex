//******************************************************************************
// OpenSILEX - Licence AGPL V3.0 - https://www.gnu.org/licenses/agpl-3.0.en.html
// Copyright © INRA 2019
// Contact: vincent.migot@inra.fr, anne.tireau@inra.fr, pascal.neveu@inra.fr
//******************************************************************************
package org.opensilex.module.base;

import org.opensilex.bigdata.*;
import org.opensilex.config.*;
import org.opensilex.fs.*;
import org.opensilex.module.*;
import org.opensilex.server.security.*;
import org.opensilex.sparql.*;



/**
 *
 * @author Vincent Migot
 */
public interface BaseConfig extends ModuleConfig {

    /**
     * Flag to determine if application is in debug mode or not
     *
     * @return true Application in debug mode false Application in production
     * mode
     */
    @ConfigDescription(
            value = "Flag to determine if application is in debug mode or not",
            defaultBoolean = false
    )
    public Boolean debug();

    /**
     * Default application language
     *
     * @return default application language
     */
    @ConfigDescription(
            value = "Default application language",
            defaultString = "en"
    )
    public String defaultLanguage();

    @ConfigDescription(
        value = "Platform base URI",
        defaultString = "http://www.opensilex.org/"
    )
    public String baseURI();
    
    
    @ConfigDescription(
            value = "Big data source"
    )
    public BigDataService bigData();

    @ConfigDescription(
            value = "SPARQL data sources"
    )
    public SPARQLService sparql();

    @ConfigDescription(
            value = "Authentication service"
    )
    public AuthenticationService authentication();

    @ConfigDescription(
            value = "File storage service"
    )
    public FileStorageService fs();

}
