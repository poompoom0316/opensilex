//******************************************************************************
// OpenSILEX - Licence AGPL V3.0 - https://www.gnu.org/licenses/agpl-3.0.en.html
// Copyright © INRA 2019
// Contact: vincent.migot@inra.fr, anne.tireau@inra.fr, pascal.neveu@inra.fr
//******************************************************************************
package test.opensilex.server.rest;

import java.util.*;
import javax.servlet.http.*;
import org.glassfish.jersey.internal.inject.*;
import org.glassfish.jersey.server.*;
import org.glassfish.jersey.test.*;
import org.mockito.*;
import org.opensilex.*;
import org.opensilex.server.rest.*;

/**
 * The application test class. Initialize the required environment for the
 * tests. Any other test class must extends OpensilexTest.
 */
public class RestApplicationTest extends JerseyTest {

    /**
     * Initialise the required environment for the tests
     *
     * @return ResourceConfig instance
     */
    @Override
    protected ResourceConfig configure() {
        OpenSilex.setup(new HashMap<String, String>() {
            {
                put(OpenSilex.PROFILE_ID_ARG_KEY, OpenSilex.TEST_PROFILE_ID);
            }
        });

        ResourceConfig resourceConfig = new RestApplication(OpenSilex.getInstance());

        // create a mock for HttpServletRequest which is not available with grizzly
        final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        resourceConfig.register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(request).to(HttpServletRequest.class);
            }
        });

        return resourceConfig;
    }
}
