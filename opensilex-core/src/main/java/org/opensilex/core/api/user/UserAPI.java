//******************************************************************************
// OpenSILEX - Licence AGPL V3.0 - https://www.gnu.org/licenses/agpl-3.0.en.html
// Copyright © INRA 2019
// Contact: vincent.migot@inra.fr, anne.tireau@inra.fr, pascal.neveu@inra.fr
//******************************************************************************
package org.opensilex.core.api.user;

import io.swagger.annotations.*;
import java.net.*;
import javax.inject.*;
import javax.mail.internet.*;
import javax.validation.*;
import javax.ws.rs.Path;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.Status;
import org.opensilex.core.dal.user.*;
import org.opensilex.server.response.*;
import org.opensilex.server.rest.*;
import org.opensilex.server.security.*;
import org.opensilex.server.security.model.*;
import org.opensilex.sparql.*;



@Api("Users")
@Path("/core/user")
public class UserAPI implements RestApplicationAPI {

    @Inject
    private SPARQLService sparql;

    @Inject
    private AuthenticationService authentication;

    @POST
    @Path("create")
    @ApiOperation("Create a user and return it's URI")
    @ApiResponses({
        @ApiResponse(code = 201, message = "User sucessfully created"),
        @ApiResponse(code = 409, message = "User already exists (duplicate email)")
    })
//    @ApiProtected
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response create(
            @ApiParam("User creation informations") UserCreationDTO userDTO
    ) throws Exception {

        UserDAO userDAO = new UserDAO(sparql, authentication);

        InternetAddress userEmail = new InternetAddress(userDTO.getEmail());

        if (!userDAO.userEmailexists(userEmail)) {
            User user = userDAO.create(
                    userEmail,
                    userDTO.getFirstName(),
                    userDTO.getLastName(),
                    userDTO.getPassword()
            );

            return new ObjectUriResponse(Response.Status.CREATED, user.getUri()).getResponse();
        } else {
            return new ErrorResponse(
                    Status.CONFLICT,
                    "User already exists",
                    "Duplicated email: " + userEmail.toString()
            ).getResponse();
        }
    }

    @POST
    @Path("authenticate")
    @ApiOperation("Authenticate a user and return an access token")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "User sucessfully authenticated"),
        @ApiResponse(code = 403, message = "Invalid credentials (user does not exists or invalid password)")
    })
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response authenticate(
            @ApiParam("User authentication informations") @Valid UserAuthenticationDTO authenticationDTO
    ) throws Exception {

        UserDAO userDAO = new UserDAO(sparql, authentication);

        User user;
        try {
            InternetAddress email = new InternetAddress(authenticationDTO.getIdentifier());
            user = userDAO.getByEmail(email);
        } catch (AddressException ex2) {
            try {
                URI uri = new URI(authenticationDTO.getIdentifier());
                user = userDAO.getByURI(uri);
            } catch (URISyntaxException ex1) {
                throw new Exception("Submitted user identifier is neither a valid email or URI");
            }
        }

        if (userDAO.authenticate(user, authenticationDTO.getPassword())) {
            return new SingleObjectResponse<String>(authentication.generateToken(user)).getResponse();
        } else {
            return new ErrorResponse(Status.FORBIDDEN, "Invalid credentials", "User does not exists or password is invalid").getResponse();
        }
    }
    
    
    @POST
    @Path("logout")
    @ApiOperation("Logout by discarding a user token")
    @ApiResponses({
        @ApiResponse(code = 200, message = "User sucessfully logout"),
    })
    @ApiProtected
    public Response logout(@HeaderParam(ApiProtected.HEADER_NAME) String userToken) {
        // TODO should implement a proper blacklist mechanism in AuthenticationService
        return Response.ok().build();
    }
}
