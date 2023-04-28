package org.otus;

import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.*;
import org.otus.dto.Category;
import org.otus.dto.Pet;
import org.otus.dto.ResponseDTO;
import org.otus.dto.TagPet;
import org.otus.services.PetApi;

import java.util.Collections;

import static org.hamcrest.Matchers.comparesEqualTo;
import static org.hamcrest.Matchers.equalTo;
import static org.otus.dto.Status.available;


@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestForPet {
    private static final PetApi PET_API = new PetApi("https://petstore.swagger.io/v2");

    //
// Создаем запрос, объект класса Pet
//
// Отправляем POST-запрос в endpoint PET на добавление нового животного (кошка-Мурка)
//
// Проверяем HTTP-статус ответа = 200
//
// Проверяем все реквизиты ответа на соответствие заданным, кроме поля "photoUrls"
//
    @Test
    @Order(1)
    public void checkAddingNewPetToStore() {
        Pet request = getPet();
        Response response = PET_API.addPet(request);
        response
                .then()
                .log().all()
                .statusCode(HttpStatus.SC_OK);
        Assertions.assertEquals(request, response.then().extract().as(Pet.class));
    }

    //
// Отправляем GET-запрос в endpoint PET/+{id животного} для поиска животного
//
// Проверяем реквизиты ответа:
//  HTTP-статус ответа = 200
//  искомое животное "Cat", зовут "Murka", статус available
//
    @Test
    @Order(2)
    public void checkFindPetById() {
        Response response = PET_API.findPetById(getPet().id());
        response
                .then()
                .log().all()
                .statusCode(HttpStatus.SC_OK)
                .body("name", equalTo("Murka"))
                .body("status", equalTo(available.name()))
                .body("category.name", comparesEqualTo("Cat"));
    }

    //
//  Меняем наш объект класса Pet на собаку Barsik
//
// Отправляем PUT-запрос в endpoint PET на изменение существующего животного
//
// Проверяем HTTP-статус ответа = 200
//
// Проверяем сменившиеся реквизиты: имя животного и его категорию
//
    @Test
    @Order(3)
    public void checkUpdatePet() {
        Pet pet = getPetForUpdate();
        Response response = PET_API.updatePet(pet);
        response
                .then()
                .log().all()
                .statusCode(HttpStatus.SC_OK);
        Pet responsePet = response.then().extract().as(Pet.class);
        Assertions.assertEquals(pet.name(), responsePet.name());
        Assertions.assertEquals(pet.category(), responsePet.category());
        String name = response.jsonPath().getString("name");
        Category category = response.jsonPath().getObject("category", Category.class);
        Assertions.assertEquals(pet.name(), name);
        Assertions.assertEquals(pet.category(), category);
    }

    //
// Отправляем DELETE-запрос в endpoint PET/+{id животного} для удаления животного
//
// Проверяем реквизиты ответа:
//  проверяем json ответа по согласно json-схемы
//  HTTP-статус ответа = 200
//  поле Code = 200
//  поле message = id нашего животного = 36952
//
    @Test
    @Order(4)
    public void checkDeletePet() {
        ValidatableResponse response = PET_API.deletePetById(getPet().id());
        response.body(JsonSchemaValidator.matchesJsonSchemaInClasspath("schema/DeletePet.json"));
        response
                .log().all()
                .statusCode(HttpStatus.SC_OK)
                .body("code", equalTo(200))
                .body("message", comparesEqualTo(getPet().id()));
    }

    //
// Отправляем GET-запрос в endpoint PET/+{id животного} для поиска животного,
// которого нет в БД, мы его удалили ранее
// Проверяем реквизиты ответа:
//  HTTP-статус ответа = 404
//  проверяем json ответа по согласно json-схемы
//   code = 1, type = "error", message = "Pet not found"
//
    @Test
    @Order(5)
    public void checkFindPetByIdNotFound() {
        Response response = PET_API.findPetById(getPet().id());
        response
                .then()
                .log().all()
                .statusCode(HttpStatus.SC_NOT_FOUND)
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("schema/DeletePet.json"));
        ResponseDTO responseDTO = response.as(ResponseDTO.class);
        Assertions.assertAll("Поиск животного, которого нет в БД",
            () -> Assertions.assertEquals(responseDTO.code(), 1),
            () -> Assertions.assertEquals(responseDTO.type(), "error"),
            () -> Assertions.assertEquals(responseDTO.message(), "Pet not found")
        );
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
