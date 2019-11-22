//******************************************************************************
// OpenSILEX - Licence AGPL V3.0 - https://www.gnu.org/licenses/agpl-3.0.en.html
// Copyright © INRA 2019
// Contact: vincent.migot@inra.fr, anne.tireau@inra.fr, pascal.neveu@inra.fr
//******************************************************************************
package org.opensilex.server.security;

import io.swagger.annotations.*;
import java.lang.annotation.*;


@ApiImplicitParams({
    @ApiImplicitParam(
            name = ApiProtected.HEADER_NAME, 
            required = true,
            dataType = "string", 
            paramType = "header",
            value = "Authentication token")
})
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiProtected {
    
    public final static String HEADER_NAME = "Authorization";

}