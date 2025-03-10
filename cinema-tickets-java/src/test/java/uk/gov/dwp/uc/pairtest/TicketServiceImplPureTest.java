package uk.gov.dwp.uc.pairtest;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.paymentgateway.TicketPaymentServiceImpl;
import thirdparty.seatbooking.SeatReservationService;
import thirdparty.seatbooking.SeatReservationServiceImpl;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

//import org.junit.Rule;
//import org.junit.rules.ExpectedException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;


import static org.junit.Assert.assertThrows;

public class TicketServiceImplPureTest {

    private static final int PRICE_FOR_ADULTS = 25;
    private static final int PRICE_FOR_KIDS = 15;
    private static final int PRICE_FOR_INFANTS = 0;

    private TicketServiceImpl ticketService;

    private TicketTypeRequest[] createOrder(int adults, int children, int infants) {
        if (adults == 0 && children == 0 && infants == 0) {
            return new TicketTypeRequest[0];
        }
        List<TicketTypeRequest> requests = new ArrayList<>();
        if (adults > 0) {
            requests.add(new TicketTypeRequest(TicketTypeRequest.Type.ADULT, adults));
        }
        if (children > 0) {
            requests.add(new TicketTypeRequest(TicketTypeRequest.Type.CHILD, children));
        }
        if (infants > 0) {
            requests.add(new TicketTypeRequest(TicketTypeRequest.Type.INFANT, infants));
        }
        return requests.toArray(new TicketTypeRequest[0]);
    }

    private Object invokePrivateMethodLongArgument(String methodName, Object object)
            throws NoSuchMethodException,
                   IllegalAccessException,
                   InvocationTargetException {
        if (object == null) {
            throw new IllegalArgumentException("Null argument not allowed for method: " + methodName);
        }
        Method method = TicketServiceImpl.class.getDeclaredMethod(methodName, long.class);
        method.setAccessible(true);
        if (object instanceof Long) {
            // Unboxing Long to primitive long
            return method.invoke(ticketService, (Long) object);
        } else {
            throw new IllegalArgumentException("Invalid argument(s) for method: " + methodName);
        }
    }

    private Object invokePrivateMethodTicketTypeArgument(String methodName, TicketTypeRequest[] requests)
            throws NoSuchMethodException,
                   IllegalAccessException,
                   InvocationTargetException {

        if (requests == null || requests.length == 0) {
            throw new IllegalArgumentException("Invalid request data provided.");
        }
        Method method = TicketServiceImpl.class.getDeclaredMethod(methodName, TicketTypeRequest[].class);
        method.setAccessible(true);
        return method.invoke(ticketService, (Object) requests);
    }

    @Before
    public void setUp() {
        TicketPaymentService paymentService = new TicketPaymentServiceImpl();
        SeatReservationService seatService = new SeatReservationServiceImpl();
        ticketService = new TicketServiceImpl(paymentService, seatService);
    }

    // ***************** Tests with Reflection (private methods in TicketServiceImp) ***********************************

    @Test
    // *Should Return True For Valid Account
    public void testAccountIsValid()
            throws NoSuchMethodException,
                   InvocationTargetException,
                   IllegalAccessException {
        assertTrue((boolean) invokePrivateMethodLongArgument("accountIsValid", 123L));
        assertFalse((boolean) invokePrivateMethodLongArgument("accountIsValid", 0L));
        assertFalse((boolean) invokePrivateMethodLongArgument("accountIsValid", -5L));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> invokePrivateMethodLongArgument("accountIsValid", null) // Null check
        );

        assertEquals("Null argument not allowed for method: accountIsValid", exception.getMessage());
    }

    @Test
    // *Should Return False If No Adult Ticket In Order, Otherwise True
    public void testNoAdultTicket()
            throws NoSuchMethodException,
                   InvocationTargetException,
                   IllegalAccessException {
        // Create an order request with only child and infant tickets
        TicketTypeRequest[] requests1 = createOrder(0, 2, 1);
        // Create an order request with an adult
        TicketTypeRequest[] requests2 = createOrder(2, 1, 1);

        assertFalse((boolean) invokePrivateMethodTicketTypeArgument("ticketRequestIsValid", requests1));
        assertTrue((boolean) invokePrivateMethodTicketTypeArgument("ticketRequestIsValid", requests2));
    }

    @Test
    // *Should Return False If More Than 25 Tickets, Otherwise True
    public void testMoreThan25Tickets()
            throws InvocationTargetException,
                   IllegalAccessException,
                   NoSuchMethodException {
        TicketTypeRequest[] requests1 = createOrder(26, 0, 0);
        TicketTypeRequest[] requests2 = createOrder(20, 2, 3);
        boolean isValid1 = (boolean) invokePrivateMethodTicketTypeArgument("ticketRequestIsValid", requests1);
        boolean isValid2 = (boolean) invokePrivateMethodTicketTypeArgument("ticketRequestIsValid", requests2);

        assertFalse(isValid1);
        assertTrue(isValid2);
    }

    @Test
    // *Should Calculate Total Payment Correctly
    public void testCalculateTotalPayment()
            throws NoSuchMethodException,
                   InvocationTargetException,
                   IllegalAccessException {
        TicketTypeRequest[] requests = createOrder(1, 2, 3);
        int expectedTotal = (1 * PRICE_FOR_ADULTS) + (2 * PRICE_FOR_KIDS) + (3 * PRICE_FOR_INFANTS);
        int totalPayment = (int) invokePrivateMethodTicketTypeArgument("calculateTotalPayment", requests);

        assertEquals(expectedTotal, totalPayment); // 25 (adult) + 30 (kids)
    }

    @Test
    // *Should Calculate Total Seats Correctly
    public void testCalculateTotalSeats()
            throws NoSuchMethodException,
                   InvocationTargetException,
                   IllegalAccessException {
        TicketTypeRequest[] requests = createOrder(1, 2, 1);
        int totalSeats = (int) invokePrivateMethodTicketTypeArgument("calculateTotalSeats", requests);

        assertEquals(3, totalSeats); // 1 adult + 2 children
    }

    //************************* Direct tests (Without Reflection) ******************************************************

    @Test
    // *Ensures calculations are precise and constraints are respected
    public void testMixedExtremeOrder() {
        TicketTypeRequest[] requests = createOrder(15, 8, 2);
        try {
            ticketService.purchaseTickets(123L, requests); // Valid scenario
        } catch (InvalidPurchaseException e) {
            fail("Purchase failed despite valid ticket distribution - 25 total.");
        }
    }

    @Test
    // *Should Purchase Tickets If Valid
    public void testPurchaseTicketsValid()
            throws InvalidPurchaseException {
        TicketTypeRequest[] requests = createOrder(5, 2, 0);

        try {
            ticketService.purchaseTickets(123L, requests);
        } catch (InvalidPurchaseException e) {
            fail("Purchase failed with valid tickets");
        }
    }


    @Test(expected = InvalidPurchaseException.class)
    // *Ensures that an order with only infant tickets is correctly rejected
    public void testOnlyInfantInOrder() {
        TicketTypeRequest[] requests = createOrder(0, 0, 3);

        ticketService.purchaseTickets(123L, requests); // Should fail
    }

    @Test(expected = InvalidPurchaseException.class)
    // *Ensures that an order with only children is correctly rejected
    public void testOnlyChildrenInOrder() {
        TicketTypeRequest[] requests = createOrder(0, 5, 0);

        ticketService.purchaseTickets(123L, requests); //Should fail
    }

    @Test
// *Should Throw Exception If No Adult Ticket In Order
    public void testPurchaseTicketsNoAdult() {
        TicketTypeRequest[] requests = createOrder(0, 2, 1);

        InvalidPurchaseException exception = assertThrows(
                InvalidPurchaseException.class,
                () -> ticketService.purchaseTickets(123L, requests)
        );

        assertEquals("Invalid order. At least one Adult ticket is required.", exception.getMessage());
    }



    @Test
    // *Ensures that the smallest valid order - one adult ticket - is accepted
    public void testMinimumValidOrder() {
        TicketTypeRequest[] requests = createOrder(1, 0, 0);

        try {
            ticketService.purchaseTickets(123L, requests); // Valid scenario
        } catch (InvalidPurchaseException e) {
            fail("Purchase failed despite meeting minimum valid order requirements.");
        }
    }

    @Test
    // *Ensures that exactly 25 tickets is treated as valid
    public void testMaximumAllowedTickets() {
        TicketTypeRequest[] requests = createOrder(20, 5, 0);

        try {
            ticketService.purchaseTickets(123L, requests);  // Valid scenario
        } catch (InvalidPurchaseException e) {
            fail("Purchase failed despite valid ticket count (25 total).");
        }
    }

    @Test(expected = InvalidPurchaseException.class)
    // *Ensures that an empty order is rejected
    public void testOrderWithZeroTickets() {
        TicketTypeRequest[] requests = createOrder(0, 0, 0);

        ticketService.purchaseTickets(123L, requests); // Should fail
    }

}
