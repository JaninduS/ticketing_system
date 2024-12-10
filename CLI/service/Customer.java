package service;

import cli.TicketingCLI;
import model.Ticket;

import java.util.List;
import java.util.concurrent.TimeUnit;

import service.TicketPoolService;

public class Customer implements Runnable {
    private final int customerId;
    private final int customerRetrievalRate;
    private final TicketPoolService ticketPoolService;


    public Customer(int customerId, TicketPoolService ticketPoolService, int customerRetrievalRate) {
        this.customerId = customerId;
        this.ticketPoolService = ticketPoolService;
        this.customerRetrievalRate = customerRetrievalRate;
    }
    @Override
    public void run() {
        try {
            while (ticketPoolService.isRunning() && ticketPoolService.canProcessMoreTickets()) {
                synchronized (ticketPoolService.getTicketPool()) {
                    if (Thread.interrupted()) return;
                    // Wait if the ticket pool is empty
                    while (ticketPoolService.getTicketPool().isEmpty()) {
                        System.out.println("Customer " + customerId + " is waiting, ticket pool is empty...");
                        ticketPoolService.getTicketPool().wait();  // Wait until a ticket is available
                    }
                    if (ticketPoolService.canProcessMoreTickets()) {
                        ticketPoolService.buyTicket(customerId);  // Buy a ticket from the pool
                        ticketPoolService.getTicketPool().notifyAll();
                    }
                }

                // Sleep for the retrieval rate
                Thread.sleep(customerRetrievalRate);
            }
            if (!ticketPoolService.canProcessMoreTickets()) {
                TicketingCLI.requestStopProcessing(); // Call the stop method
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Customer " + customerId + " was interrupted.");
        }
    }
}
