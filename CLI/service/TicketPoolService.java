package service;

import model.Ticket;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

public class TicketPoolService {
    private final List<Ticket> ticketPool;
    private final List<Ticket> soldTickets;
    private static volatile boolean running =  false;
    private int ticketsProduced = 0;
    private int ticketsProcessed = 0;
    private final int totalTickets;
    private final int maxTicketCapacity;
    private boolean processCompleted = false;

    public TicketPoolService(int maxTicketCapacity, int totalTickets) {
        // Enforce the maximum capacity for the ticket pool
        this.ticketPool = Collections.synchronizedList(new ArrayList<>());
        this.soldTickets = new ArrayList<>(totalTickets);
        this.maxTicketCapacity = maxTicketCapacity;
        this.totalTickets = totalTickets;
    }

    public synchronized boolean isProcessCompleted() {
        return processCompleted;
    }

    public synchronized void markProcessCompleted() {
        this.processCompleted = true;
    }

    // Synchronized getter and incrementer for ticketsProduced
    public synchronized  int getTicketsProduced() {
        return ticketsProduced;
    }

    public synchronized  void incrementTicketsProduced() {
        ticketsProduced++;
    }

    // Stop condition for vendors
    public synchronized boolean canProduceMoreTickets() {
        return ticketsProduced < totalTickets;
    }

    // Stop condition for customers
    public synchronized boolean canProcessMoreTickets() {
        return ticketsProcessed < totalTickets;
    }

    public synchronized List<Ticket> getTicketPool() {
        return ticketPool;
    }

    public boolean isRunning() {
        return running;
    }

    public synchronized int getTicketsProcessed() {
        return ticketsProcessed;
    }

    public synchronized void incrementTicketsProcessed() {
        ticketsProcessed++;
    }

    public int getTotalTickets() {
        return totalTickets;
    }

    public int getMaxTicketCapacity() {
        return maxTicketCapacity;
    }

    public int getSize() {
        return ticketPool.size();
    }

    public void setRunning(boolean state) {
        running = state;
    }

    public void setTicketsProcessed(int ticketsProcessed) {
        this.ticketsProcessed = ticketsProcessed;
    }

    // Getter for the list of sold tickets
    public List<Ticket> getSoldTickets() {
        return soldTickets;
    }

    public synchronized void addTicket(Ticket ticket, int vendorId) {
        try {
            ticket.setVendorId(vendorId);
            ticketPool.add(ticket);
            incrementTicketsProduced();
            System.out.println("Vendor " + vendorId + " added ticket: " + ticket + "Current pool size: " + getSize());
        } catch (Exception e) {
            System.out.println("Vendor: " + vendorId + " encountered an error while adding : " + e.getMessage());
        }
    }

    public synchronized void buyTicket(int customerId) {
        try {
            Ticket ticket = ticketPool.remove(0);
            ticket.setSold(true);
            ticket.setCustomerId(customerId);
            incrementTicketsProcessed();
            soldTickets.add(ticket);
            System.out.println("Customer " + customerId + " purchased ticket: " + ticket);

        } catch (Exception e) {
            Thread.currentThread().interrupt();
            System.out.println("Customer " + customerId + " encountered an error while adding : " + e.getMessage());
        }
    }
}
