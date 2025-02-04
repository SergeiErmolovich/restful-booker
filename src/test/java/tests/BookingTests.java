package tests;

import static core.Constants.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import core.Booking;

import core.Booking.BookingDates;
import core.ObjectMapperSingleton;
import core.User;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class BookingTests {

  private static final Logger logger = LoggerFactory.getLogger(BookingTests.class);
  protected int statusCode;
  protected String bookingById;
  protected String testUserJson;
  protected String testBookingJson;
  protected String updateBookingJson;
  protected String patchBookingJson;
  protected Booking testBooking;
  protected Booking updateBooking;
  protected Booking patchBooking;
  protected Booking responseBooking;
  protected User testUser;
  protected ObjectMapper objectMapper;
  protected Response response;
  protected RequestSpecification authRequestSpec;
  protected RequestSpecification noAuthRequestSpec;

  @BeforeClass
  public void setUp() {
    noAuthRequestSpec = new RequestSpecBuilder().setBaseUri(BASE_URI)
        .addHeader("Content-Type", "application/json").build();
    testUser = User.builder().username(USERNAME).password(PASSWORD).build();
    testBooking = Booking.builder().firstName("Anatoly").lastName("Dukalis").totalPrice(90)
        .depositPaid(false).bookingDates(BookingDates.builder().checkIn(
            LocalDate.of(2017, 11, 15)).checkOut(LocalDate.of(2017, 11, 18)).build())
        .additionalNeeds("Pasta").build();
    updateBooking = Booking.builder().firstName("Igor").lastName("Plahov").totalPrice(120)
        .depositPaid(true).bookingDates(BookingDates.builder().checkIn(
            LocalDate.of(2015, 3, 14)).checkOut(LocalDate.of(2015, 4, 2)).build())
        .additionalNeeds("Mashed potatos").build();
    patchBooking = Booking.builder().firstName("Vasily").lastName("Rogov").bookingDates(
        BookingDates.builder().checkIn(LocalDate.of(2016, 5, 3)).checkOut(LocalDate.of(2016, 5, 29))
            .build()).build();
    objectMapper = ObjectMapperSingleton.getInstance();
    try {
      testUserJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(testUser);
      logger.info("обьект testUser успешно сериализован, результат:\n{}", testUserJson);
      testBookingJson = objectMapper.writerWithDefaultPrettyPrinter()
          .writeValueAsString(testBooking);
      logger.info("обьект testBooking успешно сериализован, результат:\n{}", testBookingJson);
      updateBookingJson = objectMapper.writerWithDefaultPrettyPrinter()
          .writeValueAsString(updateBooking);
      logger.info("обьект updateBooking успешно сериализован, результат:\n{}", updateBookingJson);
      patchBookingJson = objectMapper.writerWithDefaultPrettyPrinter()
          .writeValueAsString(patchBooking);
      logger.info("обьект patchBooking успешно сериализован, результат:\n{}", patchBookingJson);
    } catch (JsonProcessingException e) {
      logger.error("Ошибка при сериализации объекта: {}", e.getMessage(), e);
      throw new RuntimeException("Ошибка при сериализации объекта", e);
    }
  }

  @Test(priority = 1)
  public void testCreateToken() {
    response = RestAssured.given().log().all().spec(noAuthRequestSpec).body(testUserJson)
        .when().post(AUTH_EP);
    response.then().log().all();
    statusCode = response.getStatusCode();
    Assert.assertEquals(statusCode, 200);
    testUser.setToken(response.jsonPath().getString("token"));
    authRequestSpec = new RequestSpecBuilder().setBaseUri(BASE_URI)
        .addHeader("Content-Type", "application/json")
        .addHeader("Cookie", "token=" + testUser.getToken())
        .build();
  }

  @Test(priority = 2)
  public void testCreateBooking() {
    response = RestAssured.given().log().all().spec(noAuthRequestSpec).body(testBookingJson)
        .when()
        .post(BOOKING_EP);
    response.then().log().all();
    statusCode = response.getStatusCode();
    Assert.assertEquals(statusCode, 200);
    testBooking.setBookingId(response.jsonPath().getInt("bookingid"));
    bookingById = BOOKING_EP + "/" + testBooking.getBookingId();
  }

  @Test(priority = 3)
  public void testUpdateBooking() {
    response = RestAssured.given().log().all().spec(authRequestSpec).body(updateBookingJson).when()
        .put(bookingById);
    response.then().log().all();
    statusCode = response.getStatusCode();
    Assert.assertEquals(statusCode, 200);
    try {
      responseBooking = objectMapper.readValue(response.body().asPrettyString(), Booking.class);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
    Assert.assertEquals(responseBooking, updateBooking);
    testBooking = updateBooking;
  }

  @Test(priority = 4)
  public void testUpdateBookingNoToken() {
    response = RestAssured.given().log().all().spec(noAuthRequestSpec).body(testBookingJson)
        .when().put(bookingById);
    response.then().log().all();
    statusCode = response.getStatusCode();
    Assert.assertEquals(statusCode, 403);
    Assert.assertEquals(response.asString(), FORBIDDEN_RESPONSE);
  }

  @Test(priority = 5)
  public void testPatchBooking() {
    response = RestAssured.given().log().all().spec(authRequestSpec).body(patchBookingJson).when()
        .patch(bookingById);
    response.then().log().all();
    statusCode = response.getStatusCode();
    Assert.assertEquals(statusCode, 200);
    try {
      responseBooking = objectMapper.readValue(response.body().asPrettyString(), Booking.class);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
    testBooking.setFirstName(patchBooking.getFirstName());
    testBooking.setLastName(patchBooking.getLastName());
    testBooking.setBookingDates(patchBooking.getBookingDates());
    Assert.assertEquals(responseBooking, testBooking);
  }

  @Test(priority = 6)
  public void testGetBooking() {
    response = RestAssured.given().log().all().spec(noAuthRequestSpec).when()
        .get(bookingById);
    response.then().log().all();
    statusCode = response.getStatusCode();
    try {
      responseBooking = objectMapper.readValue(response.body().asPrettyString(), Booking.class);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
    Assert.assertEquals(statusCode, 200);
    Assert.assertEquals(responseBooking, testBooking);
  }

  @Test(priority = 7)
  public void testDeleteBooking() {
    response = RestAssured.given().log().all().spec(authRequestSpec).when()
        .delete(bookingById);
    response.then().log().all();
    statusCode = response.getStatusCode();
    Assert.assertEquals(statusCode, 201);
    Assert.assertEquals(response.asString(), DELETE_RESPONSE);
    response = RestAssured.given().log().all().spec(noAuthRequestSpec).when().get(bookingById);
    response.then().log().all();
    statusCode = response.getStatusCode();
    Assert.assertEquals(statusCode, 404);
    Assert.assertEquals(response.asString(), NOT_FOUND_RESPONSE);
  }

}
