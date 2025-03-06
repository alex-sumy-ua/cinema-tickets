package uk.gov.dwp.uc.pairtest;

import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

public class TicketServiceImpl implements TicketService {
    /**
     * Should only have private methods other than the one below.
     */
    private static final int PRICE_FOR_ADULTS = 25;
    private static final int PRICE_FOR_KIDS = 15;

    private final TicketPaymentService paymentService;
    private final SeatReservationService seatReservationService;

    public TicketServiceImpl(TicketPaymentService paymentService, SeatReservationService seatReservationService) {
        this.paymentService = paymentService;
        this.seatReservationService = seatReservationService;
    }

    @Override
    public void purchaseTickets(Long accountId, TicketTypeRequest... ticketTypeRequests) throws InvalidPurchaseException {
        if (!accountIsValid(accountId) ||
            !ticketRequestIsValid(ticketTypeRequests)) {
            throw new InvalidPurchaseException();
        }

        // Continue with payment and seat reservation logic...
        int totalAmountToPay = calculateTotalPayment(ticketTypeRequests);
        int totalSeatsToAllocate = calculateTotalSeats(ticketTypeRequests);

        paymentService.makePayment(accountId, totalAmountToPay);
        seatReservationService.reserveSeat(accountId, totalSeatsToAllocate);

    }

    private boolean accountIsValid(Long accountId) {
        return accountId > 0;
    }

    private boolean ticketRequestIsValid(TicketTypeRequest... ticketTypeRequests) {
        int totalTickets = 0;
        int adultTickets = 0;

        for (TicketTypeRequest request : ticketTypeRequests) {
            totalTickets += request.getNoOfTickets();
            if (request.getTicketType() == TicketTypeRequest.Type.ADULT) {
                adultTickets += request.getNoOfTickets();
            }
        }
            return adultTickets > 0 && totalTickets <= 25;
    }

    private int calculateTotalPayment(TicketTypeRequest... ticketTypeRequests) {
        int totalCost = 0;
        for (TicketTypeRequest request : ticketTypeRequests) {
            if (request.getTicketType() == TicketTypeRequest.Type.ADULT) {
                totalCost += request.getNoOfTickets() * PRICE_FOR_ADULTS;
            } else if (request.getTicketType() == TicketTypeRequest.Type.CHILD) {
                totalCost += request.getNoOfTickets() * PRICE_FOR_KIDS;
            }
        }
        return totalCost;
    }

    private int calculateTotalSeats(TicketTypeRequest... ticketTypeRequests) {
        int totalSeats = 0;
        for (TicketTypeRequest request : ticketTypeRequests) {
            if (request.getTicketType() == TicketTypeRequest.Type.ADULT ||
                request.getTicketType() == TicketTypeRequest.Type.CHILD) {
                totalSeats += request.getNoOfTickets();
            }
        }
        return totalSeats;
    }

}
