//******************************************************************************
//                                GermplasmResourceService.java
// SILEX-PHIS
// Copyright © INRA 2019
// Creation date: 1 juil. 2019
// Contact: alice.boizet@inra.fr, anne.tireau@inra.fr, pascal.neveu@inra.fr
//******************************************************************************
package opensilex.service.resource;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import opensilex.service.configuration.DefaultBrapiPaginationValues;
import opensilex.service.configuration.GlobalWebserviceValues;
import opensilex.service.dao.GermplasmDAO;
import opensilex.service.documentation.DocumentationAnnotation;
import opensilex.service.documentation.StatusCodeMsg;
import opensilex.service.model.Germplasm;
import opensilex.service.resource.dto.germplasm.BrapiGermplasmDTO;
import opensilex.service.resource.dto.germplasm.GermplasmDTO;
import opensilex.service.resource.dto.germplasm.GermplasmPostDTO;
import opensilex.service.resource.validation.interfaces.URL;
import opensilex.service.result.ResultForm;
import opensilex.service.utils.POSTResultsReturn;
import opensilex.service.view.brapi.Status;
import opensilex.service.view.brapi.form.AbstractResultForm;
import opensilex.service.view.brapi.form.ResponseFormPOST;
import org.opensilex.security.authentication.ApiProtected;
import org.opensilex.sparql.service.SPARQLService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Germplasm resource service
 *
 * @author Alice Boizet <alice.boizet@inra.fr>
 */
//@Api("/germplasm")
//@Path("germplasm")
public class GermplasmResourceService extends ResourceService {

    final static Logger LOGGER = LoggerFactory.getLogger(GermplasmResourceService.class);

    @Inject
    private SPARQLService sparql;

    /**
     * Inserts germplasm in the storage.
     *
     * @param germplasm list of germplasm to insert.
     * @example {
     *
     * }
     * @param context
     * @return the post result with the errors or the uri of the inserted
     * germplasm
     */
    @POST
    @ApiOperation(value = "Post Germplasm,",
            notes = "Register new Germplasm in the database")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "Germplasm saved", response = ResponseFormPOST.class),
        @ApiResponse(code = 400, message = DocumentationAnnotation.BAD_USER_INFORMATION),
        @ApiResponse(code = 401, message = DocumentationAnnotation.USER_NOT_AUTHORIZED),
        @ApiResponse(code = 500, message = DocumentationAnnotation.ERROR_SEND_DATA)
    })
    @ApiProtected
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response post(
            @ApiParam(value = DocumentationAnnotation.GERMPLASM_POST_DEFINITION) @Valid ArrayList<GermplasmPostDTO> germplasms,
            @Context HttpServletRequest context) throws Exception {
        AbstractResultForm postResponse = null;

        if (germplasms != null && !germplasms.isEmpty()) {
            GermplasmDAO germplasmDAO = new GermplasmDAO(sparql);

            germplasmDAO.user = userSession.getUser();

            POSTResultsReturn result = germplasmDAO.checkAndInsert(germplasmPostDTOsToGermplasm(germplasms));

            if (result.getHttpStatus().equals(Response.Status.CREATED)) {
                postResponse = new ResponseFormPOST(result.statusList);
                postResponse.getMetadata().setDatafiles(result.getCreatedResources());
            } else if (result.getHttpStatus().equals(Response.Status.BAD_REQUEST)
                    || result.getHttpStatus().equals(Response.Status.OK)
                    || result.getHttpStatus().equals(Response.Status.INTERNAL_SERVER_ERROR)) {
                postResponse = new ResponseFormPOST(result.statusList);
            }
            return Response.status(result.getHttpStatus()).entity(postResponse).build();
        } else {
            postResponse = new ResponseFormPOST(new Status(StatusCodeMsg.REQUEST_ERROR, StatusCodeMsg.ERR, "Empty accession(s) to add"));
            return Response.status(Response.Status.BAD_REQUEST).entity(postResponse).build();
        }
    }

    /**
     * Generates a germplasm list from a given list of AccessionPostDTO
     *
     * @param germplasmDTOs
     * @return the list of sensors
     */
    private List<Germplasm> germplasmPostDTOsToGermplasm(List<GermplasmPostDTO> germplasmDTOs) throws Exception {
        ArrayList<Germplasm> germplasms = new ArrayList<>();

        for (GermplasmPostDTO germplasmDTO : germplasmDTOs) {
            germplasms.add(germplasmDTO.createObjectFromDTO());
        }

        return germplasms;
    }

    @GET
    @ApiOperation(value = "Get all germplasm corresponding to the search params given",
            notes = "Retrieve all germplasm authorized for the user corresponding to the searched params given")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Retrieve all germplasm", response = BrapiGermplasmDTO.class, responseContainer = "List"),
        @ApiResponse(code = 400, message = DocumentationAnnotation.BAD_USER_INFORMATION),
        @ApiResponse(code = 401, message = DocumentationAnnotation.USER_NOT_AUTHORIZED),
        @ApiResponse(code = 500, message = DocumentationAnnotation.ERROR_FETCH_DATA)
    })
    @ApiProtected
    @Produces(MediaType.APPLICATION_JSON)
    public Response getGermplasmBySearch(
            @ApiParam(value = DocumentationAnnotation.PAGE_SIZE) @QueryParam(GlobalWebserviceValues.PAGE_SIZE) @DefaultValue(DefaultBrapiPaginationValues.PAGE_SIZE) @Min(0) int pageSize,
            @ApiParam(value = DocumentationAnnotation.PAGE) @QueryParam(GlobalWebserviceValues.PAGE) @DefaultValue(DefaultBrapiPaginationValues.PAGE) @Min(0) int page,
            @ApiParam(value = "Search by germplasmURI", example = DocumentationAnnotation.EXAMPLE_GERMPLASM_URI) @QueryParam("germplasmURI") String uri,
            @ApiParam(value = "Search by germplasm label", example = DocumentationAnnotation.EXAMPLE_GERMPLASM_URI) @QueryParam("germplasmLabel") String label,
            @ApiParam(value = "Search by germplasm Type") @QueryParam("germplasmType") @URL String germplasmType,
            @ApiParam(value = "Search by genus") @QueryParam("fromGenus") @URL String fromGenus,
            @ApiParam(value = "Search varieties, accessions or lots by Species") @QueryParam("fromSpecies") @URL String fromSpecies,
            @ApiParam(value = "Search by variety") @QueryParam("fromVariety") @URL String fromVariety,
            @ApiParam(value = "Search by accession") @QueryParam("fromAccession") @URL String fromAccession,
            //added the parameter language to choose the language label 
            @ApiParam(value = "choose the language of the species", example = "en") @QueryParam("language") String language
    ) throws Exception {
        GermplasmDAO germplasmDAO = new GermplasmDAO(sparql);
        //1. Get count
        Integer totalCount = germplasmDAO.count(uri, label, germplasmType, language, fromGenus, fromSpecies, fromVariety, fromAccession);

        //2. Get germplasms
        ArrayList<Germplasm> germplasmFounded = germplasmDAO.find(page, pageSize, uri, label, germplasmType, language, fromGenus, fromSpecies, fromVariety, fromAccession);

        //3. Return result
        ArrayList<Status> statusList = new ArrayList<>();
        ArrayList<GermplasmDTO> germplasmToReturn = new ArrayList<>();
        ResultForm<GermplasmDTO> getResponse;
        if (germplasmFounded == null) { //Request failure
            getResponse = new ResultForm<>(0, 0, germplasmToReturn, true);
            return noResultFound(getResponse, statusList);
        } else if (germplasmFounded.isEmpty()) { //No result found
            getResponse = new ResultForm<>(0, 0, germplasmToReturn, true);
            return noResultFound(getResponse, statusList);
        } else { //Results
            //Convert all objects to DTOs
            germplasmFounded.forEach((germplasm) -> {
                germplasmToReturn.add(germplasmDAO.getGermplasmDTO(germplasm, language));
            });

            getResponse = new ResultForm<>(pageSize, page, germplasmToReturn, true, totalCount);
            getResponse.setStatus(statusList);
            return Response.status(Response.Status.OK).entity(getResponse).build();
        }
    }
}
