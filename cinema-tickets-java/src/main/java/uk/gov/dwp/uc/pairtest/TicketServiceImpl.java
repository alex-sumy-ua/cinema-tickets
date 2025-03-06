package uk.gov.dwp.uc.pairtest;

import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

public class TicketServiceImpl implements TicketService {
    /**
     * Should only have private methods other than the one below.
     */

    @Override
    public void purchaseTickets(Long accountId, TicketTypeRequest... ticketTypeRequests) throws InvalidPurchaseException {
        if (!accountIsValid(accountId) ||
            !ticketRequestIsValid(ticketTypeRequests)) {
            throw new InvalidPurchaseException();
        }

        // Continue with payment and seat reservation logic...

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

}
