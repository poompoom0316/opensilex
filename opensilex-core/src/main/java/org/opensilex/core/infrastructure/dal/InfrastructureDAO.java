//******************************************************************************
// OpenSILEX - Licence AGPL V3.0 - https://www.gnu.org/licenses/agpl-3.0.en.html
// Copyright © INRA 2019
// Contact: vincent.migot@inra.fr, anne.tireau@inra.fr, pascal.neveu@inra.fr
//******************************************************************************
package org.opensilex.core.infrastructure.dal;

import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.jena.arq.querybuilder.AskBuilder;
import org.apache.jena.arq.querybuilder.SelectBuilder;
import org.apache.jena.sparql.core.Var;
import org.opensilex.security.authentication.ForbiddenURIAccessException;
import org.opensilex.security.authentication.NotFoundURIException;
import org.opensilex.security.authentication.SecurityOntology;
import org.opensilex.security.user.dal.UserModel;
import org.opensilex.sparql.deserializer.SPARQLDeserializers;
import org.opensilex.sparql.model.SPARQLTreeListModel;
import org.opensilex.sparql.service.SPARQLService;
import org.opensilex.sparql.service.SPARQLQueryHelper;
import static org.opensilex.sparql.service.SPARQLQueryHelper.makeVar;

/**
 * @author vidalmor
 */
public class InfrastructureDAO {

    protected final SPARQLService sparql;

    public InfrastructureDAO(SPARQLService sparql) {
        this.sparql = sparql;
    }

    public SPARQLTreeListModel<InfrastructureModel> searchTree(String pattern, UserModel user) throws Exception {
        Set<URI> infras = getUserInfrastructures(user);
        if (infras != null && infras.isEmpty()) {
            return new SPARQLTreeListModel<>();
        }

        return sparql.searchResourceTree(
                InfrastructureModel.class,
                user.getLanguage(),
                (SelectBuilder select) -> {
                    if (pattern != null && !pattern.isEmpty()) {
                        select.addFilter(SPARQLQueryHelper.regexFilter(InfrastructureModel.NAME_FIELD, pattern));
                    }

                    if (infras != null) {
                        SPARQLQueryHelper.inURI(select, InfrastructureModel.URI_FIELD, infras);
                    }
                }
        );
    }

    public void validateInfrastructureAccess(URI infrastructureURI, UserModel user) throws Exception {
        if (!sparql.uriExists(InfrastructureModel.class, infrastructureURI)) {
            throw new NotFoundURIException(infrastructureURI);
        }

        if (user.isAdmin()) {
            return;
        }

        AskBuilder ask = sparql.getUriExistsQuery(InfrastructureModel.class, infrastructureURI);
        addAskInfrastructureAccess(ask, makeVar(InfrastructureModel.URI_FIELD), user);

        if (!sparql.executeAskQuery(ask)) {
            throw new ForbiddenURIAccessException(infrastructureURI);
        }
    }

    public void validateInfrastructureTeamAccess(URI infrastructureTeamURI, UserModel user) throws Exception {

        if (!sparql.uriExists(InfrastructureTeamModel.class, infrastructureTeamURI)) {
            throw new NotFoundURIException(infrastructureTeamURI);
        }

        if (user.isAdmin()) {
            return;
        }

        AskBuilder ask = sparql.getUriExistsQuery(InfrastructureTeamModel.class, infrastructureTeamURI);
        addAskInfrastructureAccess(ask, makeVar(InfrastructureTeamModel.INFRASTRUCTURE_FIELD), user);

        if (!sparql.executeAskQuery(ask)) {
            throw new ForbiddenURIAccessException(infrastructureTeamURI);
        }
    }

    public void validateInfrastructureFacilityAccess(URI infrastructureFacilityURI, UserModel user) throws Exception {
        if (!sparql.uriExists(InfrastructureFacilityModel.class, infrastructureFacilityURI)) {
            throw new NotFoundURIException(infrastructureFacilityURI);
        }

        if (user.isAdmin()) {
            return;
        }

        AskBuilder ask = sparql.getUriExistsQuery(InfrastructureFacilityModel.class, infrastructureFacilityURI);
        addAskInfrastructureAccess(ask, makeVar(InfrastructureFacilityModel.INFRASTRUCTURE_FIELD), user);

        if (!sparql.executeAskQuery(ask)) {
            throw new ForbiddenURIAccessException(infrastructureFacilityURI);
        }
    }

    public void addAskInfrastructureAccess(AskBuilder ask, Var infraURIVar, UserModel user) {
        Var userProfileVar = makeVar("_userProfile");
        Var groupVar = makeVar(InfrastructureModel.GROUP_FIELD);
        ask.addWhere(infraURIVar, SecurityOntology.hasGroup, groupVar);
        ask.addWhere(groupVar, SecurityOntology.hasUserProfile, userProfileVar);
        ask.addWhere(userProfileVar, SecurityOntology.hasUser, SPARQLDeserializers.nodeURI(user.getUri()));
    }

    public Set<URI> getUserInfrastructures(UserModel user) throws Exception {
        if (user == null || user.isAdmin()) {
            return null;
        }

        String lang = user.getLanguage();
        Set<URI> userInfras = new HashSet<>();
        List<URI> infras = sparql.searchURIs(InfrastructureModel.class, lang, (SelectBuilder select) -> {
            Var userProfileVar = makeVar("_userProfile");
            select.addWhere(makeVar(InfrastructureModel.URI_FIELD), SecurityOntology.hasGroup, makeVar(InfrastructureModel.GROUP_FIELD));
            select.addWhere(makeVar(InfrastructureModel.GROUP_FIELD), SecurityOntology.hasUserProfile, userProfileVar);
            select.addWhere(userProfileVar, SecurityOntology.hasUser, SPARQLDeserializers.nodeURI(user.getUri()));
        });

        userInfras.addAll(infras);

        return userInfras;
    }

    public InfrastructureModel create(InfrastructureModel instance) throws Exception {
        sparql.create(instance);

        return instance;
    }

    public InfrastructureModel get(URI uri, UserModel user) throws Exception {
        validateInfrastructureAccess(uri, user);
        return sparql.getByURI(InfrastructureModel.class, uri, user.getLanguage());
    }

    public void delete(URI uri, UserModel user) throws Exception {
        validateInfrastructureAccess(uri, user);
        sparql.delete(InfrastructureModel.class, uri);
    }

    public InfrastructureModel update(InfrastructureModel instance, UserModel user) throws Exception {
        validateInfrastructureAccess(instance.getUri(), user);
        sparql.update(instance);
        return instance;
    }

    public InfrastructureFacilityModel createFacility(InfrastructureFacilityModel instance, UserModel user) throws Exception {
        InfrastructureModel infra = sparql.getByURI(InfrastructureModel.class, instance.getInfrastructure().getUri(), user.getLanguage());
        instance.setInfrastructure(infra);
        sparql.create(instance);
        return instance;
    }

    public InfrastructureFacilityModel getFacility(URI uri, UserModel user) throws Exception {
        validateInfrastructureFacilityAccess(uri, user);
        return sparql.getByURI(InfrastructureFacilityModel.class, uri, user.getLanguage());
    }

    public void deleteFacility(URI uri, UserModel user) throws Exception {
        validateInfrastructureFacilityAccess(uri, user);
        sparql.delete(InfrastructureFacilityModel.class, uri);
    }

    public InfrastructureFacilityModel updateFacility(InfrastructureFacilityModel instance, UserModel user) throws Exception {
        validateInfrastructureFacilityAccess(instance.getUri(), user);
        InfrastructureModel infra = sparql.getByURI(InfrastructureModel.class, instance.getInfrastructure().getUri(), user.getLanguage());
        instance.setInfrastructure(infra);
        sparql.update(instance);
        return instance;
    }

    public InfrastructureTeamModel getTeam(URI uri, UserModel user) throws Exception {
        validateInfrastructureTeamAccess(uri, user);
        return sparql.getByURI(InfrastructureTeamModel.class, uri, user.getLanguage());
    }

    public void deleteTeam(URI uri, UserModel user) throws Exception {
        validateInfrastructureTeamAccess(uri, user);
        sparql.delete(InfrastructureTeamModel.class, uri);
    }

    public InfrastructureTeamModel createTeam(InfrastructureTeamModel instance, UserModel user) throws Exception {
        InfrastructureModel infra = sparql.getByURI(InfrastructureModel.class, instance.getInfrastructure().getUri(), null);
        instance.setInfrastructure(infra);
        sparql.create(instance);
        return instance;
    }

    public InfrastructureTeamModel updateTeam(InfrastructureTeamModel instance, UserModel user) throws Exception {
        validateInfrastructureTeamAccess(instance.getUri(), user);
        InfrastructureModel infra = sparql.getByURI(InfrastructureModel.class, instance.getInfrastructure().getUri(), null);
        instance.setInfrastructure(infra);
        sparql.update(instance);
        return instance;
    }
}
