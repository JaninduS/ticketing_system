package service;

import model.Ticket;

public class Vendor implements Runnable {
    private final int vendorId;
    private final TicketPoolService ticketPoolService;
    private final int ticketReleaseRate;
    private final int maxTicketCapacity;

    public Vendor(int vendorId, TicketPoolService ticketPoolService, int ticketReleaseRate) {
        this.vendorId = vendorId;
        this.ticketPoolService = ticketPoolService;
        this.ticketReleaseRate = ticketReleaseRate;
        this.maxTicketCapacity = ticketPoolService.getMaxTicketCapacity();
    }
    @Override
    public void run() {

        try {
            while (ticketPoolService.isRunning() && ticketPoolService.canProduceMoreTickets()) {
                synchronized (ticketPoolService.getTicketPool()) { // Synchronize access to the ticket pool
                    // Wait if the ticket pool is at max capacity
                    while (ticketPoolService.getTicketPool().size() >= maxTicketCapacity) {
                        System.out.println("Vendor " + vendorId + " is waiting, ticket pool is full...");
                        ticketPoolService.getTicketPool().wait(); // Wait until space is available
                    }

                    // Add a ticket if there's room
                    if (ticketPoolService.canProduceMoreTickets()) {
                        Ticket ticket = new Ticket(Integer.toString(ticketPoolService.getTicketsProduced() + 1)); // Create a new ticket
                        ticketPoolService.addTicket(ticket, vendorId); // Add the ticket to the pool
                        ticketPoolService.getTicketPool().notifyAll();
                    }
                }
                // Sleep for the ticket release rate
                Thread.sleep(ticketReleaseRate);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}