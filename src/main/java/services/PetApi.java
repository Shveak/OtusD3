package services;

import dto.Pet;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import static io.restassured.RestAssured.given;

public class PetApi {
    private static final String BASE_URI = "https://petstore.swagger.io/v2";
    private static final String PET = "/pet";
    private final RequestSpecification spec;

    public PetApi() {
        spec = given()
                .baseUri(BASE_URI)
                .contentType(ContentType.JSON)

        ;
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

    public Response deletePetById(String id) {
        return
                given(spec)
                        .with()
                        .log().all()
                        .when()
                        .delete(PET + "/" + id);
    }
}
