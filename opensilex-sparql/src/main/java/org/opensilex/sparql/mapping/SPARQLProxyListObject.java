//******************************************************************************
// OpenSILEX - Licence AGPL V3.0 - https://www.gnu.org/licenses/agpl-3.0.en.html
// Copyright © INRA 2019
// Contact: vincent.migot@inra.fr, anne.tireau@inra.fr, pascal.neveu@inra.fr
//******************************************************************************
package org.opensilex.sparql.mapping;

import java.net.URI;
import java.util.List;
import org.apache.jena.arq.querybuilder.SelectBuilder;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Property;
import org.opensilex.sparql.service.SPARQLService;
import org.opensilex.sparql.deserializer.SPARQLDeserializers;
import org.opensilex.sparql.model.SPARQLResourceModel;
import static org.opensilex.sparql.service.SPARQLQueryHelper.makeVar;

/**
 *
 * @author vincent
 */
public class SPARQLProxyListObject<T extends SPARQLResourceModel> extends SPARQLProxyList<T> {

    public SPARQLProxyListObject(SPARQLClassObjectMapperIndex repository, Node graph, URI uri, Property property, Class<T> genericType, boolean isReverseRelation, String lang, SPARQLService service) {
        super(repository, graph, uri, property, genericType, isReverseRelation, lang, service);
    }

    @Override
    protected List<T> loadData() throws Exception {
        SPARQLClassObjectMapper<T> mapper = mapperIndex.getForClass(genericType);

        Node nodeURI = SPARQLDeserializers.nodeURI(uri);
        List<T> list = service.search(genericType, lang, (SelectBuilder select) -> {
            if (isReverseRelation) {
                select.addWhere(makeVar(mapper.getURIFieldName()), property, nodeURI);
            } else {
                select.addWhere(nodeURI, property, makeVar(mapper.getURIFieldName()));
            }
        });

        return list;
    }

}
