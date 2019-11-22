//******************************************************************************
// OpenSILEX - Licence AGPL V3.0 - https://www.gnu.org/licenses/agpl-3.0.en.html
// Copyright © INRA 2019
// Contact: vincent.migot@inra.fr, anne.tireau@inra.fr, pascal.neveu@inra.fr
//******************************************************************************
package org.opensilex.sparql.deserializer;

import org.apache.jena.datatypes.xsd.*;
import org.apache.jena.graph.*;



/**
 *
 * @author vincent
 */
public class DoubleDeserializer implements SPARQLDeserializer<Double> {

    @Override
    public Double fromString(String value) throws Exception {
        return Double.valueOf(value);
    }

    @Override
    public Node getNode(Object value) throws Exception {
        return NodeFactory.createLiteralByValue(value, XSDDatatype.XSDdouble);
    }
}
