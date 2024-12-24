package core;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.time.LocalDate;
import lombok.*;

@Data
@Builder
@JsonSerialize
@JsonDeserialize
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Booking {

  @JsonProperty("firstname")
  private String firstName;

  @JsonProperty("lastname")
  private String lastName;

  @JsonProperty("totalprice")
  private Integer totalPrice;

  @JsonProperty("bookingid")
  private Integer bookingId;

  @JsonProperty("depositpaid")
  private Boolean depositPaid;

  @JsonProperty("bookingdates")
  private BookingDates bookingDates;

  @JsonProperty("additionalneeds")
  private String additionalNeeds;

  @Data
  @Builder
  @JsonSerialize
  @JsonDeserialize
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class BookingDates {

    @JsonProperty("checkin")
    private LocalDate checkIn;

    @JsonProperty("checkout")
    private LocalDate checkOut;

  }
}