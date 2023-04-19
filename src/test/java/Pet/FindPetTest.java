package Pet;

import dto.Category;
import dto.Pet;
import dto.TagPet;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.*;
import services.PetApi;

import java.util.Collections;

import static dto.Status.available;
import static org.hamcrest.Matchers.comparesEqualTo;
import static org.hamcrest.Matchers.equalTo;


@Tag("RestAssured")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FindPetTest {
    public static final PetApi PET_API = new PetApi();
    public static Pet petInStore;

    @Test
    @Order(1)
    public void checkAddingNewPetToStore() {
        Pet request = getPet();
        Response response = PET_API.addPet(request);
        Assertions.assertEquals(request, response.then().extract().as(Pet.class));
        petInStore = response.then().extract().as(Pet.class);
    }

    @Test
    @Order(2)
    public void checkFindPetById() {
        Response response = PET_API.findPetById(petInStore.id());
        Assertions.assertEquals(petInStore, response.then().extract().as(Pet.class));
    }

    @Test
    @Order(3)
    public void checkUpdatePet() {
        Pet pet = getPetForUpdate();
        Response response = PET_API.updatePet(pet);
        Assertions.assertEquals(pet.name(), response.then().extract().as(Pet.class).name());
        Assertions.assertEquals(pet.category(), response.then().extract().as(Pet.class).category());
    }

    @Test
    @Order(4)
    public void checkDeletePet() {
        Response response = PET_API.deletePetById(petInStore.id());
        response
                .then()
                .log().all()
                .statusCode(HttpStatus.SC_OK)
                .body("code", equalTo(200))
                .body("message", comparesEqualTo(petInStore.id()));
    }

    private Pet getPet() {
        return new Pet()
                .id("36952")
                .name("Murka")
                .status(available)
                .category(new Category()
                        .id("5698")
                        .name("Cat"))
                .tags(Collections.singletonList(
                        new TagPet()
                                .id("5699")
                                .name("FirstBox")));
    }

    private Pet getPetForUpdate() {
        return getPet()
                .name("Barsik")
                .category(new Category()
                        .id("5700")
                        .name("Dog"));
    }
}
