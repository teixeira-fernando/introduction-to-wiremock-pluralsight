package com.booking.service;

import com.booking.domain.BookingPayment;
import com.booking.domain.CreditCard;
import com.booking.gateway.PayBuddyGateway;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static com.booking.service.BookingResponse.BookingResponseStatus.SUCCESS;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

public class BookingServiceTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(wireMockConfig()
            .httpsPort(8443)
            .keystorePath("src/main/resources/wiremock-keystore.jks")
            .keystorePassword("password")
    );

    private BookingService bookingService;

    @Before
    public void setUp() {
        bookingService = new BookingService(
                new PayBuddyGateway("localhost", 8443));
    }

    @Test
    public void shouldSucceedToPayForBooking() {
        // Given
        stubFor(post(urlPathEqualTo("/payments"))
                .withRequestBody(equalToJson("{" +
                        "  \"creditCardNumber\": \"1234-1234-1234-1234\"," +
                        "  \"creditCardExpiry\": \"2018-02-01\"," +
                        "  \"amount\": 20.55" +
                        "}"))
                .willReturn(okJson("{" +
                        "  \"paymentId\": \"2222\"," +
                        "  \"paymentResponseStatus\": \"SUCCESS\"" +
                        "}")));

        // When
        final BookingResponse bookingResponse = bookingService.payForBooking(
                new BookingPayment(
                        "1111",
                        new BigDecimal("20.55"),
                        new CreditCard("1234-1234-1234-1234",
                                LocalDate.of(2018, 2, 1))));

        // Then
        assertThat(bookingResponse)
                .isEqualTo(new BookingResponse("1111", "2222", SUCCESS));
    }
}
