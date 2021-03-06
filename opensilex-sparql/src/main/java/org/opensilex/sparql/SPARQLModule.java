/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.opensilex.sparql;

import java.io.FileInputStream;
import java.io.InputStream;
import org.opensilex.sparql.service.SPARQLService;
import java.net.URI;
import java.util.List;
import java.util.Map;
import org.opensilex.OpenSilexModule;
import org.opensilex.sparql.exceptions.SPARQLValidationException;
import org.opensilex.sparql.extensions.OntologyFileDefinition;
import org.opensilex.sparql.extensions.SPARQLExtension;
import org.opensilex.sparql.rdf4j.RDF4JInMemoryServiceFactory;
import org.opensilex.sparql.service.SPARQLServiceFactory;
import org.opensilex.sparql.service.SPARQLStatement;
import org.opensilex.utils.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author vince
 */
public class SPARQLModule extends OpenSilexModule {

    private final static String DEFAULT_BASE_URI = "http://default.opensilex.org/";

    private final static Logger LOGGER = LoggerFactory.getLogger(SPARQLModule.class);

    public final static String ONTOLOGY_BASE_DOMAIN = "http://www.opensilex.org/";

    @Override
    public Class<?> getConfigClass() {
        return SPARQLConfig.class;
    }

    @Override
    public String getConfigId() {
        return "ontologies";
    }

    private URI baseURI;

    private String basePrefix;

    private SPARQLConfig sparqlConfig;

    @Override
    public void setup() throws Exception {
        sparqlConfig = this.getConfig(SPARQLConfig.class);
        if (sparqlConfig != null) {
            basePrefix = sparqlConfig.baseURIAlias() + "-";
            baseURI = new URI(sparqlConfig.baseURI());
        } else {
            basePrefix = "";
            baseURI = new URI(DEFAULT_BASE_URI);
        }

        LOGGER.debug("Set platform URI: " + baseURI.toString());
        LOGGER.debug("Set platform URI prefix: " + basePrefix);
    }

    public String getBasePrefix() {
        return basePrefix;
    }

    public URI getBaseURI() {
        return baseURI;
    }

    public URI getSuffixedURI(String suffix) {
        return baseURI.resolve(suffix);
    }

    public void installOntologies(SPARQLService sparql, boolean reset) throws Exception {
        try {
            sparql.disableSHACL();
            sparql.startTransaction();
            // Allow any module implementing SPARQLExtension to add custom ontologies
            for (SPARQLExtension module : getOpenSilex().getModulesImplementingInterface(SPARQLExtension.class)) {
                for (OntologyFileDefinition ontologyDef : module.getOntologiesFiles()) {
                    if (reset) {
                        sparql.clearGraph(ontologyDef.getUri());
                    }
                    InputStream ontologyStream = new FileInputStream(ClassUtils.getFileFromClassArtifact(module.getClass(), ontologyDef.getFilePath()));
                    sparql.loadOntology(ontologyDef.getUri(), ontologyStream, ontologyDef.getFileType());
                    ontologyStream.close();
                }
            }
            sparql.commitTransaction();
        } catch (Exception ex) {
            sparql.rollbackTransaction();
            throw ex;
        }
    }

    @Override
    public void install(boolean reset) throws Exception {
        SPARQLConfig cfg = this.getConfig(SPARQLConfig.class);
        SPARQLServiceFactory sparql = cfg.sparql();
        if (reset) {
            sparql.deleteRepository();
        }
        sparql.createRepository();
        sparql = cfg.sparql();

        SPARQLService sparqlService = sparql.provide();

        try {
            installOntologies(sparqlService, reset);

            if (cfg.enableSHACL()) {
                try {
                    sparqlService.enableSHACL();
                } catch (SPARQLValidationException ex) {
                    LOGGER.warn("Error while enable SHACL validation:");
                    Map<URI, Map<URI, List<URI>>> errors = ex.getValidationErrors();
                    errors.forEach((URI uri, Map<URI, List<URI>> error) -> {
                        LOGGER.warn("--> " + uri + ":");
                        error.forEach((URI protpertyUri, List<URI> brokenConstraints) -> {
                            LOGGER.warn("    " + protpertyUri + ":");
                            brokenConstraints.forEach(constraintURI -> {
                                LOGGER.warn("      " + constraintURI);
                            });
                        });
                    });
                }
            } else {
                sparqlService.disableSHACL();
            }
        } catch (Exception ex) {

            LOGGER.error("Error while initializing SHACL", ex);
        } finally {
            sparql.dispose(sparqlService);
        }

    }

    @Override
    public void check() throws Exception {
        LOGGER.info("Check SPARQL required ontologies");
        SPARQLServiceFactory factory = getOpenSilex().getServiceInstance(SPARQLService.DEFAULT_SPARQL_SERVICE, SPARQLServiceFactory.class);
        SPARQLService sparql = factory.provide();
        for (SPARQLExtension module : getOpenSilex().getModulesImplementingInterface(SPARQLExtension.class)) {
            for (OntologyFileDefinition ontologyDef : module.getOntologiesFiles()) {
                List<SPARQLStatement> results = sparql.getGraphStatement(ontologyDef.getUri());

                if (results.size() == 0) {
                    String errorMsg = ontologyDef.getUri().toString() + " is missing data into your triple store, did you execute `opensilex system setup` command ?";
                    LOGGER.warn("/!\\ " + errorMsg);
                    throw new Exception(errorMsg);
                }
            }
        }
        factory.dispose(sparql);
    }

    @Override
    public void startup() throws Exception {
        SPARQLServiceFactory factory = getOpenSilex().getServiceInstance(SPARQLService.DEFAULT_SPARQL_SERVICE, SPARQLServiceFactory.class);
        if (factory instanceof RDF4JInMemoryServiceFactory) {
            for (SPARQLExtension module :getOpenSilex().getModulesImplementingInterface(SPARQLExtension.class)) {
                module.inMemoryInitialization();
            }
        }
    }

}
