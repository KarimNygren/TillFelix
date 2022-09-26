package com.passwordmanager.services;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

/**
 * The Service Interface class for Password Manager
 */
@Path("/passwordbook/v1")
public interface PasswordService {

    @POST
    @Path("/passwords")
    Response createPassword(String json);

    @GET
    @Path("/passwords/{id}")
    Response getPassword(@PathParam(value = "id") String passwordId);

    @GET
    @Path("/passwords/summary")
    Response getSummary();

    @POST
    @Path("/passwords/delete/{id}")
    Response deletePassword(@PathParam(value = "id") String passwordId);
}