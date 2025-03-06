package uk.gov.dwp.uc.pairtest;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import static org.mockito.Mockito.*;

public class TicketServiceImplTest {

    private TicketPaymentService paymentService;
    private SeatReservationService seatReservationService;
    private TicketServiceImpl ticketService;

    private static final int PRICEFORADULTS = 25;
    private static final int PRICEFORKIDS = 15;

    @Before
    public void setUp() {
        paymentService = mock(TicketPaymentService.class);
        seatReservationService = mock(SeatReservationService.class);
        ticketService = new TicketServiceImpl(paymentService, seatReservationService);
    }

    @Test
    public void testValidTicketPurchase() {
        // Given
        TicketTypeRequest adultTicket = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 2);
        TicketTypeRequest childTicket = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 1);

        // When
        ticketService.purchaseTickets(1L, adultTicket, childTicket);

        // Then
        verify(paymentService, times(1))
                .makePayment(1L, 2 * PRICEFORADULTS + 1 * PRICEFORKIDS); // £65 total
        verify(seatReservationService, times(1))
                .reserveSeat(1L, 3); // 2 adults + 1 child = 3 seats
    }

    @Test(expected = InvalidPurchaseException.class)
    public void testInvalidAccountId() {
        // Given
        TicketTypeRequest adultTicket = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1);

        // When
        ticketService.purchaseTickets(0L, adultTicket);
    }

    @Test(expected = InvalidPurchaseException.class)
    public void testNoAdultTicket() {
        // Given
        TicketTypeRequest childTicket = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 2);

        // When
        ticketService.purchaseTickets(1L, childTicket);
    }

    @Test(expected = InvalidPurchaseException.class)
    public void testMoreThan25Tickets() {
        // Given
        TicketTypeRequest adultTicket = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 26);

        // When
        ticketService.purchaseTickets(1L, adultTicket);
    }

    @Test
    public void testCorrectPaymentCalculation() {
        // Given
        TicketTypeRequest adultTicket = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 3);
        TicketTypeRequest childTicket = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 2);
        TicketTypeRequest infantTicket = new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1);

        // When
        ticketService.purchaseTickets(1L, adultTicket, childTicket, infantTicket);

        // Then
        verify(paymentService, times(1))
                .makePayment(1L, (3 * PRICEFORADULTS) + (2 * PRICEFORKIDS)); // £105 total
    }

    @Test
    public void testCorrectSeatReservationCalculation() {
        // Given
        TicketTypeRequest adultTicket = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 2);
        TicketTypeRequest childTicket = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 3);
        TicketTypeRequest infantTicket = new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1);

        // When
        ticketService.purchaseTickets(1L, adultTicket, childTicket, infantTicket);

        // Then
        verify(seatReservationService, times(1))
                .reserveSeat(1L, 5); // 2 adults + 3 children = 5 seats
    }
}
