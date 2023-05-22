package org.otus;

import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.otus.dto.Category;
import org.otus.dto.Pet;
import org.otus.dto.ResponseDTO;
import org.otus.dto.TagPet;
import org.otus.services.PetApi;

import java.util.Collections;

import static org.hamcrest.Matchers.comparesEqualTo;
import static org.hamcrest.Matchers.equalTo;
import static org.otus.dto.Status.available;


public class TestForPet {
    private static final PetApi PET_API = new PetApi();
    private String id;
    private boolean isNeedDeleted;

    @BeforeEach
    public void runBeforeTestMethod() {
        isNeedDeleted = false;
    }

    @AfterEach
    public void runAfterTestMethod() {
        if (isNeedDeleted) {
            PET_API.deletePetById(id);
        }
    }

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
    public void checkAddingNewPetToStore() {
        id = "36952";
        Pet request = getPet(id);
        Response response = PET_API.addPet(request);
        isNeedDeleted = true;
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
    public void checkFindPetById() {
        id = "36953";
        PET_API.addPet(getPet(id));
        isNeedDeleted = true;
        Response response = PET_API.findPetById(id);
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
    public void checkUpdatePet() {
        id = "36954";
        Pet request = getPet(id);
        PET_API.addPet(request);
        isNeedDeleted = true;
        Pet pet = request
                .name("Barsik")
                .category(new Category()
                        .id("5700")
                        .name("Dog"));
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
    public void checkDeletePet() {
        id = "36955";
        PET_API.addPet(getPet(id));
        ValidatableResponse response = PET_API.deletePetById(id);
        response
                .log().all()
                .statusCode(HttpStatus.SC_OK)
                .body("code", equalTo(200))
                .body("message", comparesEqualTo(id));
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
    public void checkFindPetByIdNotFound() {
        id = "36956";
        Response response = PET_API.findPetById(id);
        response
                .then()
                .log().all()
                .statusCode(HttpStatus.SC_NOT_FOUND);
        ResponseDTO responseDTO = response.as(ResponseDTO.class);
        Assertions.assertAll("Поиск животного, которого нет в БД",
            () -> Assertions.assertEquals(responseDTO.code(), 1),
            () -> Assertions.assertEquals(responseDTO.type(), "error"),
            () -> Assertions.assertEquals(responseDTO.message(), "Pet not found")
        );
    }

    private Pet getPet(String id) {
        return new Pet()
                .id(id)
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
}
