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
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class BookingTests {

  protected int statusCode;
  protected String bookingById;
  protected String createTokenJson;
  protected String createBookingJson;
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
  protected RequestSpecification deleteRequestSpec;

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
      createTokenJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(testUser);
      createBookingJson = objectMapper.writerWithDefaultPrettyPrinter()
          .writeValueAsString(testBooking);
      updateBookingJson = objectMapper.writerWithDefaultPrettyPrinter()
          .writeValueAsString(updateBooking);
      patchBookingJson = objectMapper.writerWithDefaultPrettyPrinter()
          .writeValueAsString(patchBooking);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  @Test(priority = 1)
  public void testCreateToken() {
    response = RestAssured.given().log().all().spec(noAuthRequestSpec).body(createTokenJson)
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
    response = RestAssured.given().log().all().spec(noAuthRequestSpec).body(createBookingJson)
        .when()
        .post(BOOKING_EP);
    response.then().log().all();
    statusCode = response.getStatusCode();
    Assert.assertEquals(statusCode, 200);
    testBooking.setBookingId(response.jsonPath().getInt("bookingid"));
    bookingById = BOOKING_EP + "/" + testBooking.getBookingId();
  }

  @Test(priority = 3)
  public void tesUpdateBooking() {
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
  public void testPatchBooking() {
    response = RestAssured.given().log().all().spec(authRequestSpec).body(patchBookingJson).when()
        .patch(bookingById);
    response.then().log().all();
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

  @Test(priority = 5)
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

  @Test(priority = 6)
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
