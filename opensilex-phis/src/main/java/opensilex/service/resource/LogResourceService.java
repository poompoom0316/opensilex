//******************************************************************************
//                       LogResourceService.java
// SILEX-PHIS
// Copyright © INRAE
// Creation date: February 2020
// Contact: arnaud.charleroy@inra.fr, anne.tireau@inra.fr, pascal.neveu@inra.fr
//******************************************************************************
package opensilex.service.resource;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.validation.constraints.Min;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import opensilex.service.configuration.DateFormat;
import opensilex.service.configuration.DefaultBrapiPaginationValues;
import opensilex.service.configuration.GlobalWebserviceValues;
import opensilex.service.dao.DataQueryLogDAO;
import opensilex.service.documentation.DocumentationAnnotation;
import opensilex.service.model.DataQueryLog;
import opensilex.service.resource.dto.data.DataLogAccessUserDTO;
import opensilex.service.resource.dto.data.DataQueryLogSearchDTO;
import opensilex.service.resource.validation.interfaces.Date;
import opensilex.service.resource.validation.interfaces.URL;
import opensilex.service.result.ResultForm;
import opensilex.service.view.brapi.Status;
import org.opensilex.security.authentication.ApiProtected;
import org.opensilex.security.user.dal.UserModel;
import org.opensilex.sparql.service.SPARQLService;

/**
 * LogResourceService ressource service
 *
 * @author Arnaud Charleroy
 */
@Api("/log")
@Path("/log")
public class LogResourceService extends ResourceService {

    @Inject
    private SPARQLService sparql;

    /**
     * Returns the logs create by the search of data from environment services
     * or data services
     *
     * @param pageSize
     * @param page
     * @param userUri
     * @param startDate
     * @param endDate
     * @param dateSortAsc
     * @return The file content or null with a 404 status if it doesn't exists
     */
    @GET
    @Path("querylog")
    @ApiOperation(value = "Get data query logs")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Retrieve data query logs", response = DataQueryLog.class, responseContainer = "List"),
        @ApiResponse(code = 400, message = DocumentationAnnotation.BAD_USER_INFORMATION),
        @ApiResponse(code = 401, message = DocumentationAnnotation.USER_NOT_AUTHORIZED),
        @ApiResponse(code = 500, message = DocumentationAnnotation.ERROR_FETCH_DATA)
    })
    @ApiProtected
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDataQueryLogSearch(
            @ApiParam(value = DocumentationAnnotation.PAGE_SIZE) @QueryParam(GlobalWebserviceValues.PAGE_SIZE) @DefaultValue(DefaultBrapiPaginationValues.PAGE_SIZE) @Min(0) int pageSize,
            @ApiParam(value = DocumentationAnnotation.PAGE) @QueryParam(GlobalWebserviceValues.PAGE) @DefaultValue(DefaultBrapiPaginationValues.PAGE) @Min(0) int page,
            @ApiParam(value = "Search by user uri", example = DocumentationAnnotation.EXAMPLE_VARIABLE_URI) @QueryParam("userUri") @URL String userUri,
            @ApiParam(value = "Search by minimal date", example = DocumentationAnnotation.EXAMPLE_XSDDATETIME) @QueryParam("startDate") @Date({DateFormat.YMDTHMSZ, DateFormat.YMD}) String startDate,
            @ApiParam(value = "Search by maximal date", example = DocumentationAnnotation.EXAMPLE_XSDDATETIME) @QueryParam("endDate") @Date({DateFormat.YMDTHMSZ, DateFormat.YMD}) String endDate,
            @ApiParam(value = "Date search result order ('true' for ascending and 'false' for descending)", example = "true") @QueryParam("dateSortAsc") boolean dateSortAsc
    ) throws Exception {
        ArrayList<DataQueryLogSearchDTO> list = new ArrayList<>();
        ArrayList<Status> statusList = new ArrayList<>();
        ResultForm<DataQueryLogSearchDTO> getResponse;

        DataQueryLogDAO dataDAO = new DataQueryLogDAO();

        //1. Get count
        Integer totalCount = dataDAO.count(userUri, startDate, endDate, null);

        List<DataQueryLog> dataQueryLogList = new ArrayList<>();
        //2. Get data
        if (totalCount > 0) {
            dataQueryLogList = dataDAO.find(page, pageSize, userUri, startDate, endDate, null);
        }
        List<UserModel> listOfUsers = new ArrayList<>();
        try {
            //3. Get User informations
            listOfUsers = sparql.search(UserModel.class, null);
        } catch (Exception ex) {
            Logger.getLogger(LogResourceService.class.getName()).log(Level.SEVERE, null, ex);
        }

        //4. Return result
        if (dataQueryLogList == null) {
            // Request failure
            getResponse = new ResultForm<>(0, 0, list, true, 0);
            return noResultFound(getResponse, statusList);
        } else if (dataQueryLogList.isEmpty()) {
            // No results
            getResponse = new ResultForm<>(0, 0, list, true, 0);
        } else {
            // Convert all data object to DTO's
            for (DataQueryLog queryLog : dataQueryLogList) {
                DataLogAccessUserDTO foundUser = this.lookupUser(listOfUsers, queryLog.getUserUri());
                list.add(
                        new DataQueryLogSearchDTO(
                                foundUser,
                                queryLog.getQuery(),
                                queryLog.getDate(),
                                queryLog.getRemoteAdress()
                        )
                );
            }
            // Return list of DTO
            getResponse = new ResultForm<>(pageSize, page, list, true, totalCount);

        }
        getResponse.setStatus(statusList);
        return Response.status(Response.Status.OK).entity(getResponse).build();
    }

    private DataLogAccessUserDTO lookupUser(List<UserModel> personList, String userUri) {
        UserModel foundUser = personList.stream().
                filter(p -> p.getUri() != null && p.getUri().toString().equals(userUri)).
                findAny().orElse(null);
        if (foundUser != null) {
            DataLogAccessUserDTO returnedUser = new DataLogAccessUserDTO();
            returnedUser.setUri(foundUser.getUri().toString());
            returnedUser.setFirstName(foundUser.getLastName());
            returnedUser.setFamilyName(foundUser.getFirstName());
            return returnedUser;
        }
        return null;
    }
}
