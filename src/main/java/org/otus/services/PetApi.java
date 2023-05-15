package org.otus.services;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import org.otus.dto.Pet;

import static io.restassured.RestAssured.given;

public class PetApi {
    private static final String URL = System.getProperty("url.swagger", "https://petstore.swagger.io/v2");
    private static final String PET = "/pet";
    private final RequestSpecification spec;

    public PetApi() {
        spec = given()
                .baseUri(URL)
                .contentType(ContentType.JSON);
    }

    public Response addPet(Pet pet) {
        return
                given(spec)
                        .with()
                        .body(pet)
                        .log().all()
                        .when()
                        .post(PET);
    }

    public Response updatePet(Pet pet) {
        return
                given(spec)
                        .with()
                        .body(pet)
                        .log().all()
                        .when()
                        .put(PET);
    }

    public Response findPetById(String id) {
        return
                given(spec)
                        .with()
                        .log().all()
                        .when()
                        .get(PET + "/" + id);
    }

    public ValidatableResponse deletePetById(String id) {
        return
                given(spec)
                        .with()
                        .log().all()
                        .when()
                        .delete(PET + "/" + id)
                        .then()
                        .log().all();
    }
}
