//******************************************************************************
//                          ExperimentAPI.java
// OpenSILEX - Licence AGPL V3.0 - https://www.gnu.org/licenses/agpl-3.0.en.html
// Copyright © INRAE 2020
// Contact: vincent.migot@inrae.fr, anne.tireau@inrae.fr, pascal.neveu@inrae.fr
//******************************************************************************
package org.opensilex.core.experiment.api;

import io.swagger.annotations.*;
import org.opensilex.core.experiment.dal.ExperimentDAO;
import org.opensilex.core.experiment.dal.ExperimentModel;
import org.opensilex.server.response.ErrorResponse;
import org.opensilex.server.response.ObjectUriResponse;
import org.opensilex.server.response.PaginatedListResponse;
import org.opensilex.server.response.SingleObjectResponse;
import org.opensilex.sparql.exceptions.SPARQLAlreadyExistingUriException;
import org.opensilex.sparql.exceptions.SPARQLInvalidURIException;
import org.opensilex.sparql.service.SPARQLService;
import org.opensilex.utils.OrderBy;
import org.opensilex.utils.ListWithPagination;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.opensilex.security.authentication.ApiCredential;
import org.opensilex.security.authentication.ApiCredentialGroup;
import org.opensilex.security.authentication.ApiProtected;
import org.opensilex.security.authentication.AuthenticationService;
import org.opensilex.security.authentication.injection.CurrentUser;
import org.opensilex.security.user.dal.UserModel;

/**
 * @author Vincent MIGOT
 * @author Renaud COLIN
 */
@Api(ExperimentAPI.CREDENTIAL_EXPERIMENT_GROUP_ID)
@Path("/core/experiment")
@ApiCredentialGroup(
        groupId = ExperimentAPI.CREDENTIAL_EXPERIMENT_GROUP_ID,
        groupLabelKey = ExperimentAPI.CREDENTIAL_EXPERIMENT_GROUP_LABEL_KEY
)
public class ExperimentAPI {

    public static final String CREDENTIAL_EXPERIMENT_GROUP_ID = "Experiments";
    public static final String CREDENTIAL_EXPERIMENT_GROUP_LABEL_KEY = "credential-groups.experiments";

    public static final String CREDENTIAL_EXPERIMENT_MODIFICATION_ID = "experiment-modification";
    public static final String CREDENTIAL_EXPERIMENT_MODIFICATION_LABEL_KEY = "credential.experiment.modification";

    public static final String CREDENTIAL_EXPERIMENT_READ_ID = "experiment-read";
    public static final String CREDENTIAL_EXPERIMENT_READ_LABEL_KEY = "credential.experiment.read";

    public static final String CREDENTIAL_EXPERIMENT_DELETE_ID = "experiment-delete";
    public static final String CREDENTIAL_EXPERIMENT_DELETE_LABEL_KEY = "credential.experiment.delete";

    protected static final String EXPERIMENT_EXAMPLE_URI = "http://opensilex/set/experiments/ZA17";

    @CurrentUser
    UserModel currentUser;

    @Inject
    private SPARQLService sparql;

    @Inject
    private AuthenticationService authentication;

    /**
     * Create an Experiment
     *
     * @param xpDto the Experiment to create
     * @return a {@link Response} with a {@link ObjectUriResponse} containing the created Experiment {@link URI}
     */
    @POST
    @Path("create")
    @ApiOperation("Create an experiment")
    @ApiProtected
    @ApiCredential(
            credentialId = CREDENTIAL_EXPERIMENT_MODIFICATION_ID,
            credentialLabelKey = CREDENTIAL_EXPERIMENT_MODIFICATION_LABEL_KEY
    )
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "Create an experiment", response = ObjectUriResponse.class),
        @ApiResponse(code = 409, message = "An experiment with the same URI already exists", response = ErrorResponse.class)
    })
    public Response createExperiment(
            @ApiParam("Experiment description") @Valid ExperimentCreationDTO xpDto
    ) throws Exception {
        try {
            ExperimentDAO dao = new ExperimentDAO(sparql);
            ExperimentModel createdXp = dao.create(xpDto.newModel());
            return new ObjectUriResponse(Response.Status.CREATED, createdXp.getUri()).getResponse();

        } catch (SPARQLAlreadyExistingUriException e) {
            // Return error response 409 - CONFLICT if experiment URI already exists
            return new ErrorResponse(
                    Response.Status.CONFLICT,
                    "Experiment already exists",
                    "Duplicated URI: " + e.getUri()
            ).getResponse();
        }
    }

    /**
     * @param xpDto the Experiment to update
     * @return a {@link Response} with a {@link ObjectUriResponse} containing the updated Experiment {@link URI}
     */
    @PUT
    @Path("update")
    @ApiOperation("Update an experiment")
    @ApiProtected
    @ApiCredential(
            credentialId = CREDENTIAL_EXPERIMENT_MODIFICATION_ID,
            credentialLabelKey = CREDENTIAL_EXPERIMENT_MODIFICATION_LABEL_KEY
    )
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)

    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Experiment updated", response = ObjectUriResponse.class),
        @ApiResponse(code = 404, message = "Experiment URI not found", response = ErrorResponse.class)
    })
    public Response updateExperiment(
            @ApiParam("Experiment description") @Valid ExperimentCreationDTO xpDto
    ) throws Exception {
        try {
            ExperimentDAO dao = new ExperimentDAO(sparql);

            ExperimentModel model = xpDto.newModel();
            dao.update(model);
            return new ObjectUriResponse(Response.Status.OK, model.getUri()).getResponse();

        } catch (SPARQLInvalidURIException e) {
            return new ErrorResponse(
                    Response.Status.NOT_FOUND, "Experiment not found",
                    "Unknown Experiment URI: " + e.getUri()
            ).getResponse();
        }
    }

    /**
     * @param xpUri the Experiment URI
     * @return a {@link Response} with a {@link SingleObjectResponse} containing the {@link ExperimentGetDTO}
     */
    @GET
    @Path("get/{uri}")
    @ApiOperation("Get an experiment by URI")
    @ApiProtected
    @ApiCredential(
            credentialId = CREDENTIAL_EXPERIMENT_READ_ID,
            credentialLabelKey = CREDENTIAL_EXPERIMENT_READ_LABEL_KEY
    )
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)

    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Experiment retrieved", response = ExperimentGetDTO.class),
        @ApiResponse(code = 404, message = "Experiment URI not found", response = ErrorResponse.class)
    })
    public Response getExperiment(
            @ApiParam(value = "Experiment URI", example = "http://opensilex.dev/set/experiments/ZA17", required = true) @PathParam("uri") @NotNull URI xpUri
    ) throws Exception {
        ExperimentDAO dao = new ExperimentDAO(sparql);
        ExperimentModel model = dao.get(xpUri);

        if (model != null) {
            return new SingleObjectResponse<>(ExperimentGetDTO.fromModel(model)).getResponse();
        } else {
            return new ErrorResponse(
                    Response.Status.NOT_FOUND, "Experiment not found",
                    "Unknown Experiment URI: " + xpUri.toString()
            ).getResponse();
        }
    }

    /**
     * Search experiments
     *
     * @return filtered, ordered and paginated list
     * @see ExperimentDAO
     */
    @GET
    @Path("search")
    @ApiOperation("Search Experiments")
    @ApiProtected
    @ApiCredential(
            credentialId = CREDENTIAL_EXPERIMENT_READ_ID,
            credentialLabelKey = CREDENTIAL_EXPERIMENT_READ_LABEL_KEY
    )
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Return Experiment list", response = ExperimentGetDTO.class, responseContainer = "List")
    })
    public Response searchExperiments(
            @ApiParam(value = "Search by start date", example = "2017-06-15") @QueryParam("startDate") LocalDate startDate,
            @ApiParam(value = "Search by end date", example = "2017-06-15") @QueryParam("endDate") LocalDate endDate,
            @ApiParam(value = "Regex pattern for filtering by label", example = "ZA17") @QueryParam("label") String label,
            @ApiParam(value = "Search by involved species", example = "http://www.phenome-fppn.fr/id/species/zeamays") @QueryParam("species") List<URI> species,
            @ApiParam(value = "Search by related project uri", example = "http://www.phenome-fppn.fr/projects/ZA17\nhttp://www.phenome-fppn.fr/id/projects/ZA18") @QueryParam("projects") List<URI> projects,
            @ApiParam(value = "Search private(false) or public projects(true)", example = "true") @QueryParam("isPublic") Boolean isPublic,
            @ApiParam(value = "Search ended(false) or active projects(true)", example = "true") @QueryParam("isEnded") Boolean isEnded,
            @ApiParam(value = "List of fields to sort as an array of fieldName=asc|desc", example = "label=asc") @QueryParam("orderBy") List<OrderBy> orderByList,
            @ApiParam(value = "Page number", example = "0") @QueryParam("page") @DefaultValue("0") @Min(0) int page,
            @ApiParam(value = "Page size", example = "20") @QueryParam("pageSize") @DefaultValue("20") @Min(0) int pageSize
    ) throws Exception {
        ExperimentDAO xpDao = new ExperimentDAO(sparql);

        ListWithPagination<ExperimentModel> resultList = xpDao.search(
                label,
                species,
                startDate,
                endDate,
                isEnded,
                projects,
                isPublic,
                currentUser,
                orderByList,
                page,
                pageSize
        );

        // Convert paginated list to DTO
        ListWithPagination<ExperimentGetDTO> resultDTOList = resultList.convert(ExperimentGetDTO.class, ExperimentGetDTO::fromModel);
        return new PaginatedListResponse<>(resultDTOList).getResponse();
    }

    /**
     * Remove an experiment
     *
     * @param xpUri the experiment URI
     * @return a {@link Response} with a {@link ObjectUriResponse} containing the deleted Experiment {@link URI}
     */
    @DELETE
    @Path("delete/{uri}")
    @ApiOperation("Delete an experiment")
    @ApiProtected
    @ApiCredential(
            credentialId = CREDENTIAL_EXPERIMENT_DELETE_ID,
            credentialLabelKey = CREDENTIAL_EXPERIMENT_DELETE_LABEL_KEY
    )
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)

    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Experiment deleted", response = ObjectUriResponse.class),
        @ApiResponse(code = 404, message = "Experiment URI not found", response = ErrorResponse.class)
    })
    public Response deleteExperiment(
            @ApiParam(value = "Experiment URI", example = EXPERIMENT_EXAMPLE_URI, required = true) @PathParam("uri") @NotNull URI xpUri
    ) throws Exception {
        try {
            ExperimentDAO dao = new ExperimentDAO(sparql);
            dao.delete(xpUri);
            return new ObjectUriResponse(xpUri).getResponse();

        } catch (SPARQLInvalidURIException e) {
            return new ErrorResponse(
                    Response.Status.NOT_FOUND, "Experiment not found",
                    "Unknown Experiment URI: " + e.getUri()
            ).getResponse();
        }
    }

    /**
     * Updates the sensors linked to an experiment.
     *
     * @param xpUri
     * @param factors
     * @return the query result
     * @example [ "http://www.phenome-fppn.fr/opensilex/2018/s18001" ]
     * @example { "metadata": { "pagination": null, "status": [ { "message": "Resources updated", "exception": { "type": "Info", "href": null,
     * "details": "The experiment http://www.opensilex.fr/platform/OSL2018-1 has now 1 linked sensors" } } ], "datafiles": [
     * "http://www.opensilex.fr/platform/OSL2015-1" ] } }
     */
    @PUT
    @Path("{uri}/factors")
    @ApiOperation("Update the factors which participates in an experiment")
    @ApiProtected
    @ApiCredential(
            credentialId = CREDENTIAL_EXPERIMENT_MODIFICATION_ID,
            credentialLabelKey = CREDENTIAL_EXPERIMENT_MODIFICATION_LABEL_KEY
    )
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "The list of factors which participates in the experiment updated", response = ObjectUriResponse.class),
        @ApiResponse(code = 404, message = "Experiment URI not found", response = ErrorResponse.class)
    })
    public Response putFactors(
            @ApiParam(value = "Experiment URI", example = EXPERIMENT_EXAMPLE_URI, required = true) @PathParam("uri") @NotNull URI xpUri,
            @ApiParam(value = "List of factors uris") ArrayList<URI> factors) throws Exception {
        try {
            ExperimentDAO xpDao = new ExperimentDAO(sparql);
            xpDao.updateWithFactors(xpUri, factors);
            return new ObjectUriResponse(Response.Status.OK, xpUri).getResponse();
        } catch (SPARQLInvalidURIException e) {
            return new ErrorResponse(
                    Response.Status.NOT_FOUND, "Experiment not found",
                    "Unknown Experiment URI: " + e.getUri()
            ).getResponse();
        }
    }
}
