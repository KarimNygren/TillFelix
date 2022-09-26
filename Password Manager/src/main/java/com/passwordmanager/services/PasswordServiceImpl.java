package com.passwordmanager.services;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.passwordmanager.api.Password;
import com.passwordmanager.util.InvalidPasswordException;
import com.passwordmanager.util.NonExistingPasswordException;
import com.passwordmanager.util.PasswordUtil;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.UUID;

/**
 * The Implementation of the Password Manager. Implements the Service Interface.
 */
public class PasswordServiceImpl implements PasswordService {

    // Creates a new GSON Object.
    private static final Gson gson = new Gson();

    // Creates a Ledger object
    private final Ledger ledger;

    // Constructor which takes Ledger
    public PasswordServiceImpl(Ledger ledger) {
        this.ledger = ledger;
    }

    /**
     * createPassword Method which will create a Password Object, validate the UUID and then
     * add the password to the ledger if the ID is an UUID.
     *
     * Returns Response HTTP status code 201 which means Created Success. Includes headers that are needed to set
     * Allowed Methods, Origins etc. If these headers are not present, the HTTP call will fail due to CORS.
     */
    @Override
    public Response createPassword(String json) {
        try{
            Password password = gson.fromJson(json, Password.class);
            PasswordUtil.validate(password);
            UUID uuid = ledger.addPassword(password);
            return Response.status(201).entity("Created Password with id: " + uuid.toString())
                    .header("Access-Control-Allow-Origin", "*")
                    .header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization")
                    .header("Access-Control-Allow-Credentials", "true")
                    .header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
                    .header("Access-Control-Max-Age", "1209600")
                    .build();
        } catch (JsonSyntaxException je) {
            throw new WebApplicationException(
                    Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity("Non valid json").build());
        } catch (InvalidPasswordException ie) {
            throw new WebApplicationException(
                    Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(ie.getReason()).build());
        } catch (Exception e) {
            throw new WebApplicationException(
                    Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).build());
        }
    }

    /**
     * getPassword method which will get a password based on its ID. Validates the UUID and then
     * gets the password from the ledger if the ID is a UUID.
     *
     * Returns Response with OK status and headers that are needed to set Allowed Methods, Origins etc.
     * If these headers are not present, the HTTP call will fail due to CORS.
     */
    @Override
    public Response getPassword(String passwordId) {
        try{
            PasswordUtil.validateUUID(passwordId);
            Password password = ledger.getPassword(passwordId);
            return Response.ok().entity(gson.toJson(password))
                    .header("Access-Control-Allow-Origin", "*")
                    .header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization")
                    .header("Access-Control-Allow-Credentials", "true")
                    .header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
                    .header("Access-Control-Max-Age", "1209600")
                    .build();
        } catch (NonExistingPasswordException ne) {
            throw new WebApplicationException(
                    Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(ne.getReason()).build());
        } catch (Exception e) {
            throw new WebApplicationException(
                    Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).build());
        }
    }

    /**
     * getSummary method which will get all the passwords in storage. Calls getAllPasswords() from ledger instance and
     * stores them in a Password List.
     *
     * Returns Response with OK status and headers that are needed to set Allowed Methods, Origins etc.
     * If these headers are not present, the HTTP call will fail due to CORS.
     *
     */
    @Override
    public Response getSummary() {
        try{
            List<Password> summary = ledger.getAllPasswords();
            return Response.ok().entity(gson.toJson(summary))
                    .header("Access-Control-Allow-Origin", "*")
                    .header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization")
                    .header("Access-Control-Allow-Credentials", "true")
                    .header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
                    .header("Access-Control-Max-Age", "1209600")
                    .build();
        } catch (InvalidPasswordException ie) {
            throw new WebApplicationException(
                    Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(ie.getReason()).build());
        } catch (Exception e) {
            throw new WebApplicationException(
                    Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).build());
        }
    }

    /**
     * deletePassword method which will delete a password based on its ID. Validates that the ID is a UUID and calls
     * deletePassword() on ledger instance.
     *
     * Returns Response with OK status and headers that are needed to set Allowed Methods, Origins etc.
     * If these headers are not present, the HTTP call will fail due to CORS.
     *
     */
    @Override
    public Response deletePassword(String passwordId) {
        try{
            PasswordUtil.validateUUID(passwordId);
            ledger.deletePassword(passwordId);
            return Response.ok()
                    .header("Access-Control-Allow-Origin", "*")
                    .header("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept, Authorization")
                    .header("Access-Control-Allow-Credentials", "false")
                    .header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
                    .header("Access-Control-Max-Age", "1209600")
                    .build();
        } catch (NonExistingPasswordException ne) {
            throw new WebApplicationException(
                    Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(ne.getReason()).build());
        } catch (Exception e) {
            throw new WebApplicationException(
                    Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).build());
        }
    }
}