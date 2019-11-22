//******************************************************************************
// OpenSILEX - Licence AGPL V3.0 - https://www.gnu.org/licenses/agpl-3.0.en.html
// Copyright © INRA 2019
// Contact: vincent.migot@inra.fr, anne.tireau@inra.fr, pascal.neveu@inra.fr
//******************************************************************************
package org.opensilex.sparql.exceptions;

/**
 *
 * @author vincent
 */
public class SPARQLException extends Exception {

    public SPARQLException() {
        super();
    }

    public SPARQLException(String message) {
        super(message);
    }

    public SPARQLException(String message, Throwable cause) {
        super(message, cause);
    }

    public SPARQLException(Throwable cause) {
        super(cause);
    }

}
