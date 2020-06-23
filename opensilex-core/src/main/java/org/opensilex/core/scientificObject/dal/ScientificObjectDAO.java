/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.opensilex.core.scientificObject.dal;

import java.net.URI;
import org.opensilex.security.user.dal.UserModel;
import org.opensilex.sparql.model.SPARQLTreeListModel;
import org.opensilex.sparql.service.SPARQLService;

/**
 *
 * @author vmigot
 */
public class ScientificObjectDAO {

    private final SPARQLService sparql;

    public ScientificObjectDAO(SPARQLService sparql) {
        this.sparql = sparql;
    }

    public SPARQLTreeListModel<ScientificObjectModel> searchTreeByExperiment(URI experimentURI, UserModel currentUser) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
