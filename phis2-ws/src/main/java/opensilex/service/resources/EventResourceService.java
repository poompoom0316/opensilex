//******************************************************************************
//                         EventResourceService.java
// SILEX-PHIS
// Copyright © INRA 2018
// Creation date: 13 Nov. 2018
// Contact: andreas.garcia@inra.fr, anne.tireau@inra.fr, pascal.neveu@inra.fr
//******************************************************************************
package opensilex.service.resources;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.ArrayList;
import java.util.logging.Level;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import opensilex.service.configuration.DateFormat;
import opensilex.service.configuration.DefaultBrapiPaginationValues;
import opensilex.service.configuration.GlobalWebserviceValues;
import opensilex.service.dao.EventDAO;
import opensilex.service.documentation.DocumentationAnnotation;
import opensilex.service.documentation.StatusCodeMsg;
import opensilex.service.resources.dto.event.EventPostDTO;
import opensilex.service.resources.dto.event.EventDTO;
import opensilex.service.resources.dto.rdfResourceDefinition.RdfResourceDefinitionDTO;
import opensilex.service.resources.validation.interfaces.Date;
import opensilex.service.resources.validation.interfaces.Required;
import opensilex.service.resources.validation.interfaces.URL;
import opensilex.service.utils.POSTResultsReturn;
import opensilex.service.view.brapi.Status;
import opensilex.service.view.brapi.form.AbstractResultForm;
import opensilex.service.view.brapi.form.ResponseFormPOST;
import opensilex.service.result.ResultForm;
import opensilex.service.model.Event;

/**
 * Service to handle events
 * @update [Andréas Garcia] 14 Feb., 2019: Add GET detail service
 * @update [Andréas Garcia] 5 March, 2019: Add POST service
 * @update [Andréas Garcia] 15 March, 2019: Add GET {uri}/annotations service
 * @author Andréas Garcia <andreas.garcia@inra.fr>
 */
@Api("/events")
@Path("/events")
public class EventResourceService  extends ResourceService {
    final static Logger LOGGER = LoggerFactory.getLogger(EventResourceService.class);
    
    /**
     * Searches events with filters.
     * @example
     * {
     *  {
     *     "metadata": {
     *       "pagination": null,
     *       "status": [],
     *       "datafiles": []
     *     },
     *     "result": {
     *       "data": [
     *         {
     *           "uri": "http://www.phenome-fppn.fr/id/event/5a1b3c0d-58af-4cfb-811e-e141b11453b1",
     *           "type": "http://www.phenome-fppn.fr/vocabulary/2018/oeev#MoveFrom",
     *           "concernedItems": [
     *             {
     *               "labels": [
     *                 "label2",
     *                 "label3"
     *               ],
     *               "uri": "http://www.phenome-fppn.fr/m3p/arch/2017/c17000241",
     *               "typeUri": "http://www.opensilex.org/vocabulary/oeso#Thermocouple"
     *             }
     *           ],
     *           "date": "2017-09-11T12:00:00+01:00",
     *           "properties": [
     *             {
     *               "rdfType": "http://www.opensilex.org/vocabulary/oeso#Thermocouple",
     *               "relation": "http://www.phenome-fppn.fr/vocabulary/2018/oeev#from",
     *               "value": "http://www.phenome-fppn.fr/m3p/phenoarch/"
     *             }
     *           ]
     *         }
     *       ]
     *     }
     *   }
     * }
     * @param pageSize
     * @param page
     * @param uri
     * @param type
     * @param concernedItemUri
     * @param concernedItemLabel
     * @param startDate
     * @param endDate
     * @return  list of events filtered
     */
    @GET
    @ApiOperation(value = "Get all events corresponding to the search parameters given.", 
            notes = "Retrieve all events authorized for the user corresponding to the " + "search parameters given")
    @ApiResponses(value = {
        @ApiResponse(
                code = 200, 
                message = "Retrieve all events", 
                response = EventDTO.class, 
                responseContainer = "List"),
        @ApiResponse(code = 400, message = DocumentationAnnotation.BAD_USER_INFORMATION),
        @ApiResponse(code = 401, message = DocumentationAnnotation.USER_NOT_AUTHORIZED),
        @ApiResponse(code = 500, message = DocumentationAnnotation.ERROR_FETCH_DATA)
    })
    @ApiImplicitParams({@ApiImplicitParam(
            name = GlobalWebserviceValues.AUTHORIZATION, 
            required = true, dataType = GlobalWebserviceValues.DATA_TYPE_STRING, 
            paramType = GlobalWebserviceValues.HEADER, 
            value = DocumentationAnnotation.ACCES_TOKEN, 
            example = GlobalWebserviceValues.AUTHENTICATION_SCHEME + " ")})
    @Produces(MediaType.APPLICATION_JSON)
    public Response getEventsBySearch(
        @ApiParam(value = DocumentationAnnotation.PAGE_SIZE) 
            @QueryParam(GlobalWebserviceValues.PAGE_SIZE) 
            @DefaultValue(DefaultBrapiPaginationValues.PAGE_SIZE) 
            @Min(0) int pageSize, 
        @ApiParam(value = DocumentationAnnotation.PAGE) 
            @QueryParam(GlobalWebserviceValues.PAGE) 
            @DefaultValue(DefaultBrapiPaginationValues.PAGE)
            @Min(0) int page, 
        @ApiParam(value = "Search by uri", example = DocumentationAnnotation.EXAMPLE_EVENT_URI) 
            @QueryParam("uri") 
            @URL String uri, 
        @ApiParam(value = "Search by type", example = DocumentationAnnotation.EXAMPLE_EVENT_TYPE) 
            @QueryParam("type") 
            @URL String type, 
        @ApiParam(
                value = "Search by concerned item uri", 
                example = DocumentationAnnotation.EXAMPLE_EVENT_CONCERNED_ITEM_URI) 
            @QueryParam("concernedItemUri") @URL String concernedItemUri, 
        @ApiParam(
                value = "Search by concerned item label", 
                example = DocumentationAnnotation.EXAMPLE_EVENT_CONCERNED_ITEM_LABEL) 
            @QueryParam("concernedItemLabel") String concernedItemLabel, 
        @ApiParam(
                value = "Search by date - start of the range", 
                example = DocumentationAnnotation.EXAMPLE_EVENT_SEARCH_START_DATE) 
            @QueryParam("startDate") 
            @Date(DateFormat.YMDTHMSZZ) String startDate, 
        @ApiParam(
                value = "Search by date - end of the range", 
                example = DocumentationAnnotation.EXAMPLE_EVENT_SEARCH_END_DATE) 
            @QueryParam("endDate") 
            @Date(DateFormat.YMDTHMSZZ) String endDate
    ) {
        EventDAO eventDAO = new EventDAO(userSession.getUser());
        
        // 1. Search events with parameters
        ArrayList<Event> events = eventDAO.searchEvents(
                uri,
                type,
                concernedItemLabel, 
                concernedItemUri, 
                startDate, 
                endDate, 
                page, 
                pageSize);
        
        // 2. Analyse result
        ArrayList<EventDTO> eventDTOs = new ArrayList();
        ArrayList<Status> statusList = new ArrayList<>();
        ResultForm<EventDTO> responseForm;
        
        if (events == null) { // Request failure
            responseForm = new ResultForm<>(0, 0, eventDTOs, true, 0);
            return noResultFound(responseForm, statusList);
        } else if (events.isEmpty()) { // No result
            responseForm = new ResultForm<>(0, 0, eventDTOs, true, 0);
            return noResultFound(responseForm, statusList);
        } else { // Results
            
            // Generate DTOs
            events.forEach((event) -> {
                eventDTOs.add(new EventDTO(event));
            });
            
            int eventsCount =  eventDAO.count(
                uri,
                type,
                concernedItemLabel, 
                concernedItemUri, 
                startDate, 
                endDate);
            responseForm = new ResultForm<>(eventDAO.getPageSize(), eventDAO.getPage(), eventDTOs, true, eventsCount);
            if (responseForm.getResult().dataSize() == 0) {
                return noResultFound(responseForm, statusList);
            } else {
                responseForm.setStatus(statusList);
                return Response.status(Response.Status.OK).entity(responseForm).build();
            }
        }
    }
    
    /**
     * Gets an event from its URI.
     * @example
     * {
     *   "metadata": {
     *     "pagination": null,
     *     "status": [],
     *     "datafiles": []
     *   },
     *   "result": {
     *     "data": [
     *       {
     *         "uri": "http://www.opensilex.org/id/event/12590c87-1c34-426b-a231-beb7acb33415",
     *         "type": "http://www.opensilex.org/vocabulary/oeev#PestAttack",
     *         "concernedItems": [
     *           {
     *             "labels": [
     *               "Parcelle Lavalette",
     *               "Plot Lavalette"
     *             ],
     *             "uri": "http://www.opensilex.org/ues/2018/o18000124",
     *             "typeURI": "http://www.opensilex.org/vocabulary/oeso#Plot"
     *           }
     *         ],
     *         "date": "2017-09-08T12:00:00+01:00",
     *         "properties": [
     *           {
     *             "rdfType": null,
     *             "relation": "http://www.opensilex.org/vocabulary/oeev#hasPest",
     *             "value": "http://aims.fao.org/aos/agrovoc/c_34724"
     *           }
     *         ]
     *       }
     *     ]
     *   }
     * }
     * @param uri
     * @return the event found
     */
    @GET
    @Path("{uri}")
    @ApiOperation(value = "Get the event corresponding to the search uri",
                  notes = "Get the event corresponding to the search uri")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Get an event", response = EventDTO.class, responseContainer = "List"),
        @ApiResponse(code = 400, message = DocumentationAnnotation.BAD_USER_INFORMATION),
        @ApiResponse(code = 401, message = DocumentationAnnotation.USER_NOT_AUTHORIZED),
        @ApiResponse(code = 500, message = DocumentationAnnotation.ERROR_FETCH_DATA)
    })
    @ApiImplicitParams({
        @ApiImplicitParam(name = GlobalWebserviceValues.AUTHORIZATION, 
            required = true,
            dataType = GlobalWebserviceValues.DATA_TYPE_STRING, 
            paramType = GlobalWebserviceValues.HEADER,
            value = DocumentationAnnotation.ACCES_TOKEN,
            example = GlobalWebserviceValues.AUTHENTICATION_SCHEME + " ")
    })
    @Produces(MediaType.APPLICATION_JSON)
    public Response getEvent(
        @ApiParam(
                value = DocumentationAnnotation.EVENT_URI_DEFINITION, 
                required = true, 
                example = DocumentationAnnotation.EXAMPLE_EVENT_URI) 
            @PathParam("uri") @URL @Required String uri) {
        
        EventDAO eventDAO = new EventDAO(userSession.getUser());
        
        // 1. Search an event's details with its URI
        Event event = eventDAO.searchEvent(uri);
        
        // 2. Analyse result
        ArrayList<EventDTO> eventDTOs = new ArrayList();
        ArrayList<Status> statusList = new ArrayList<>();
        ResultForm<EventDTO> responseForm;
        
        if (event == null) { // Request failure
            responseForm = new ResultForm<>(0, 0, eventDTOs, true, 0);
            return noResultFound(responseForm, statusList);
        } else { // Results
            
            eventDTOs.add(new EventDTO(event));
            
            responseForm = new ResultForm<>(0, 0, eventDTOs, true, 0);
            if (responseForm.getResult().dataSize() == 0) {
                return noResultFound(responseForm, statusList);
            } else {
                responseForm.setStatus(statusList);
                return Response.status(Response.Status.OK).entity(responseForm).build();
            }
        }
    }
    
    /**
     * Gets an event's annotations.
     * @param pageSize
     * @param page
     * @example
     * [  
     *   {
     *     "uri": "http://www.opensilex.org/phenome-fppn/id/annotation/896325c3-85f7-4ad3-bf96-34ba497108c3",
     *     "creationDate": "2019-03-11T09:40:03+01:00",
     *     "creator": "http://www.phenome-fppn.fr/diaphen/id/agent/admin_phis",
     *     "motivatedBy": "http://www.w3.org/ns/oa#describing",
     *     "bodyValues": [
     *       "fth"
     *     ],
     *     "targets": [
     *       "http://www.opensilex.org/phenome-fppn/id/event/c8e0173b-ce8a-4190-ad0b-f30ac07d4edd"
     *     ]
     *   }
     * ]
     * @param uri
     * @return an event's annotations
     */
    @GET
    @Path("{uri}/annotations")
    @ApiOperation(value = "Get an event's annotations",
                  notes = "Get an event's annotations")
    @ApiResponses(value = {
        @ApiResponse(
                code = 200, message = "Get an event's annotations", 
                response = RdfResourceDefinitionDTO.class, 
                responseContainer = "List"),
        @ApiResponse(code = 400, message = DocumentationAnnotation.BAD_USER_INFORMATION),
        @ApiResponse(code = 401, message = DocumentationAnnotation.USER_NOT_AUTHORIZED),
        @ApiResponse(code = 500, message = DocumentationAnnotation.ERROR_FETCH_DATA)
    })
    @ApiImplicitParams({
        @ApiImplicitParam(name = GlobalWebserviceValues.AUTHORIZATION, required = true,
            dataType = GlobalWebserviceValues.DATA_TYPE_STRING, paramType = GlobalWebserviceValues.HEADER,
            value = DocumentationAnnotation.ACCES_TOKEN,
            example = GlobalWebserviceValues.AUTHENTICATION_SCHEME + " ")
    })
    @Produces(MediaType.APPLICATION_JSON)
    public Response getEventAnnotations(
        @ApiParam(value = DocumentationAnnotation.PAGE_SIZE) @QueryParam(GlobalWebserviceValues.PAGE_SIZE) 
        @DefaultValue(DefaultBrapiPaginationValues.PAGE_SIZE) @Min(0) int pageSize,
        @ApiParam(value = DocumentationAnnotation.PAGE) 
            @QueryParam(GlobalWebserviceValues.PAGE) 
            @DefaultValue(DefaultBrapiPaginationValues.PAGE) 
            @Min(0) int page,
        @ApiParam(
                value = DocumentationAnnotation.EVENT_URI_DEFINITION, 
                required = true, 
                example = DocumentationAnnotation.EXAMPLE_EVENT_URI) 
            @PathParam("uri") @URL @Required String uri) {
        
        AnnotationResourceService annotationResourceService = new AnnotationResourceService();
        annotationResourceService.userSession = userSession;
        return annotationResourceService.getAnnotations(null, null, uri, null, null, page, pageSize);
    }
        
    /**
     * Service to insert events.
     * @example
     * {
     *  [
     *   {
     *     "rdfType": "http://www.opensilex.org/vocabulary/oeev#MoveFrom",
     *     "description": "The pest attack lasted 20 minutes",
     *     "creator": "http://www.phenome-fppn.fr/diaphen/id/agent/marie_dupond",
     *     "concernedItemsUris": [
     *       "string"
     *     ],
     *     "date": "2017-09-08T12:00:00+01:00",
     *     "properties": [
     *       {
     *         "rdfType": "http://xmlns.com/foaf/0.1/Agent",
     *         "relation": "http://www.phenome-fppn.fr/vocabulary/2018#hasContact",
     *         "value": "http://www.phenome-fppn.fr/diaphen/id/agent/marie_dupond"
     *       }
     *     ]
     *   }
     *  ]
     * }
     * @param eventsDtos
     * @param context
     * @return  The found errors
     *          The list of the URIs of the created events
     */
    @POST
    @ApiOperation(value = "POST event(s)", 
                  notes = "Register event(s)")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Event(s) saved", response = ResponseFormPOST.class),
        @ApiResponse(code = 400, message = DocumentationAnnotation.BAD_USER_INFORMATION),
        @ApiResponse(code = 401, message = DocumentationAnnotation.USER_NOT_AUTHORIZED),
        @ApiResponse(code = 500, message = DocumentationAnnotation.ERROR_FETCH_DATA)
    })
    @ApiImplicitParams({
        @ApiImplicitParam(name = GlobalWebserviceValues.AUTHORIZATION, 
                required = true, 
                dataType = GlobalWebserviceValues.DATA_TYPE_STRING, 
                paramType = GlobalWebserviceValues.HEADER, 
                value = DocumentationAnnotation.ACCES_TOKEN, 
                example = GlobalWebserviceValues.AUTHENTICATION_SCHEME + " ")
    })
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response postEvents(
        @ApiParam(value = DocumentationAnnotation.EVENT_POST_DEFINITION) @Valid ArrayList<EventPostDTO> eventsDtos,
        @Context HttpServletRequest context) {
        AbstractResultForm postResponse = null;
        
        if (eventsDtos != null && !eventsDtos.isEmpty()) {
            EventDAO eventDao = new EventDAO(userSession.getUser());
            
            if (context.getRemoteAddr() != null) {
                eventDao.remoteUserAdress = context.getRemoteAddr();
            }
            
            ArrayList<Event> events = new ArrayList<>();
            eventsDtos.forEach((eventDto) -> {
                events.add(eventDto.createObjectFromDTO());
            });
            
            try {
                eventDao.create(events);
            } catch (Exception ex) {
                java.util.logging.Logger.getLogger(EventResourceService.class.getName()).log(Level.SEVERE, null, ex);
            }
            POSTResultsReturn result = eventDao.checkAndInsert(events);
            Response.Status httpStatus = result.getHttpStatus();
            
            if (httpStatus.equals(Response.Status.CREATED)) {
                postResponse = new ResponseFormPOST(result.statusList);
                postResponse.getMetadata().setDatafiles(result.getCreatedResources());
            } else if (httpStatus.equals(Response.Status.BAD_REQUEST)
                    || httpStatus.equals(Response.Status.OK)
                    || httpStatus.equals(Response.Status.INTERNAL_SERVER_ERROR)) {
                postResponse = new ResponseFormPOST(result.statusList);
            }
            return Response.status(httpStatus).entity(postResponse).build();
        } else {
            postResponse = new ResponseFormPOST(new Status(
                    StatusCodeMsg.REQUEST_ERROR, 
                    StatusCodeMsg.ERR, 
                    StatusCodeMsg.EVENT_TO_ADD_IS_EMPTY));
            return Response.status(Response.Status.BAD_REQUEST).entity(postResponse).build();
        }
    }
}
