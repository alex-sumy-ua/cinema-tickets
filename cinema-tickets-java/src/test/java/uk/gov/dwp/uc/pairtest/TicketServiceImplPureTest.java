package uk.gov.dwp.uc.pairtest;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.paymentgateway.TicketPaymentServiceImpl;
import thirdparty.seatbooking.SeatReservationService;
import thirdparty.seatbooking.SeatReservationServiceImpl;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class TicketServiceImplPureTest {

    private TicketServiceImpl ticketService;

    @Before
    public void setUp() {
        TicketPaymentService paymentService = new TicketPaymentServiceImpl();
        SeatReservationService seatService = new SeatReservationServiceImpl();
        ticketService = new TicketServiceImpl(paymentService, seatService);
    }

    // ***************** Tests with Reflection (private methods in TicketServiceImp) ***********************************

    @Test
    @DisplayName("Should Return True For Valid Account")
    public void testAccountIsValid() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = TicketServiceImpl.class.getDeclaredMethod("accountIsValid", Long.class);
        method.setAccessible(true);
        assertTrue((boolean) method.invoke(ticketService, 123L));
        assertFalse((boolean) method.invoke(ticketService, 0L));
        assertFalse((boolean) method.invoke(ticketService, -5L));
    }

    @Test
    @DisplayName("Should Return False If No Adult Ticket In Order, Otherwise True")
    public void testNoAdultTicket() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = TicketServiceImpl.class.getDeclaredMethod("ticketRequestIsValid", TicketTypeRequest[].class);
        method.setAccessible(true);

        // Create an order request with only child and infant tickets
        TicketTypeRequest[] requests1 = {
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 2),
                new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1)
        };

        // Create an order request with an adult
        TicketTypeRequest[] requests2 = {
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 2),
                new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1),
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1)
        };

        boolean isValid1 = (boolean) method.invoke(ticketService, (Object) requests1);
        boolean isValid2 = (boolean) method.invoke(ticketService, (Object) requests2);

        assertFalse(isValid1);
        assertTrue(isValid2);
    }

    @Test
    @DisplayName("Should Return False If More Than 25 Tickets, Otherwise True")
    public void testMoreThan25Tickets() throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        Method method = TicketServiceImpl.class.getDeclaredMethod("ticketRequestIsValid", TicketTypeRequest[].class);
        method.setAccessible(true);

        TicketTypeRequest[] requests1 = {
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 26)
        };
        TicketTypeRequest[] requests2 = {
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 20),
                new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 2),
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 3)
        };

        boolean isValid1 = (boolean) method.invoke(ticketService, (Object) requests1);
        boolean isValid2 = (boolean) method.invoke(ticketService, (Object) requests2);

        assertFalse(isValid1);
        assertTrue(isValid2);
    }

    @Test
    @DisplayName("Should Calculate Total Payment Correctly")
    public void testCalculateTotalPayment() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = TicketServiceImpl.class.getDeclaredMethod("calculateTotalPayment", TicketTypeRequest[].class);
        method.setAccessible(true);

        TicketTypeRequest[] requests = {
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1),
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 2),
                new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 3)
        };

        int totalPayment = (int) method.invoke(ticketService, (Object) requests);
        assertEquals(55, totalPayment); // 25 (adult) + 30 (kids)
    }

    @Test
    @DisplayName("Should Calculate Total Seats Correctly")
    public void testCalculateTotalSeats() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = TicketServiceImpl.class.getDeclaredMethod("calculateTotalSeats", TicketTypeRequest[].class);
        method.setAccessible(true);

        TicketTypeRequest[] requests = {
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1),
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 2),
                new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1)
        };

        int totalSeats = (int) method.invoke(ticketService, (Object) requests);
        assertEquals(3, totalSeats); // 1 adult + 2 children
    }

    //************************* Direct tests (Without Reflection) ******************************************************

    @Test
    @DisplayName("Should Purchase Tickets If Valid")
    public void testPurchaseTicketsValid() throws InvalidPurchaseException {
        TicketTypeRequest[] requests = {
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 5),
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 2)
        };

        try {
            ticketService.purchaseTickets(123L, requests);
        } catch (InvalidPurchaseException e) {
            fail("Purchase failed with valid tickets");
        }
    }

    @Test
    @DisplayName("Should Throw Exception If No Adult Ticket In Order")
    public void testPurchaseTicketsNoAdult() {
        TicketTypeRequest[] requests = {
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 2),
                new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1)
        };

        try {
            ticketService.purchaseTickets(123L, requests);
            fail("Expected InvalidPurchaseException");
        } catch (InvalidPurchaseException e) {
            // Expected exception
        }
    }

}
