/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.opensilex.sparql.rdf4j;

import java.net.URISyntaxException;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.eclipse.rdf4j.sail.shacl.ShaclSail;
import org.opensilex.sparql.service.SPARQLService;

/**
 *
 * @author vmigot
 */
public class RDF4JInMemoryService extends SPARQLService {

    public static RepositoryConnection rdf4jRepositoryConnection;
    
    public RDF4JInMemoryService() throws URISyntaxException {
        super(getInMemoryConnection());
    }

    public static RDF4JConnection getInMemoryConnection() throws URISyntaxException {
        MemoryStore memoryStore = new MemoryStore();
        ShaclSail shacl = new ShaclSail(memoryStore);
        SailRepository repository = new SailRepository(shacl);
        repository.init();

        rdf4jRepositoryConnection = repository.getConnection();
        return new RDF4JConnection(rdf4jRepositoryConnection);
    }

}