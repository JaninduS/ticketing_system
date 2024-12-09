package cli;

import model.SystemConfiguration;
import model.Ticket;
import service.Customer;
import service.TicketPoolService;
import service.Vendor;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;
import java.io.FileWriter;

public class TicketingCLI {

    private static int totalTickets;
    private static int ticketReleaseRate;
    private static int customerRetrievalRate;
    private static int maxTicketCapacity;

    private static TicketPoolService ticketPoolService;
    private static SystemConfiguration systemConfiguration = new SystemConfiguration();

    private static Thread[] vendorThreads;
    private static Thread[] customerThreads;

    public static void main(String[] args) {
        configureSystem();
        handleCommands();
    }

    private static void configureSystem() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== System Configuration ===");

        // Input total tickets
        totalTickets = promptPositiveInteger(scanner, "Enter total tickets available in the system: ");
        systemConfiguration.setTotalTickets(totalTickets);

        // Input max ticket capacity
        maxTicketCapacity = promptPositiveInteger(scanner, "Enter max ticket capacity: ");
        while (maxTicketCapacity > totalTickets) {
            System.out.println("Max ticket capacity cannot exceed total tickets.");
            maxTicketCapacity = promptPositiveInteger(scanner, "Enter max ticket capacity: ");
        }
        systemConfiguration.setMaxTicketCapacity(maxTicketCapacity);

        // Input ticket release rate
        ticketReleaseRate = promptPositiveInteger(scanner, "Enter ticket release rate (in seconds): ") * 1000;
        systemConfiguration.setTicketReleaseRate(ticketReleaseRate / 1000); // Save in seconds

        // Input customer retrieval rate
        customerRetrievalRate = promptPositiveInteger(scanner, "Enter customer retrieval rate (in seconds): ") * 1000;
        systemConfiguration.setCustomerRetrievalRate(customerRetrievalRate / 1000); // Save in seconds

        ticketPoolService = new TicketPoolService(maxTicketCapacity, totalTickets);

        writeConfigurationToJson();
        System.out.println("System configuration completed successfully.");
    }

    private static void writeConfigurationToJson() {
        // File path
        String filePath ="C:/Users/janin/OneDrive/Desktop/system_configuration.json";

        String json = "{\n" +
                "  \"totalTickets\": " + totalTickets + ",\n" +
                "  \"maxTicketCapacity\": " + maxTicketCapacity + ",\n" +
                "  \"ticketReleaseRate\": " + ticketReleaseRate + ",\n" +
                "  \"customerRetrievalRate\": " + customerRetrievalRate + "\n" +
                "}";

        try (FileWriter file = new FileWriter(filePath)) {
            file.write(json);
            System.out.println("Configuration saved to system_configuration.json");
        } catch (IOException e) {
            System.out.println("An error occurred while writing to the file: " + e.getMessage());
        }
    }

    private static int promptPositiveInteger(Scanner scanner, String message) {
        int value;
        while (true) {
            System.out.print(message);
            if (scanner.hasNextInt()) {
                value = scanner.nextInt();
                if (value > 0) return value;
            }
            System.out.println("Invalid input. Please enter a positive integer.");
            scanner.nextLine(); // Clear invalid input
        }
    }

    private static void handleCommands() {
        Scanner scanner = new Scanner(System.in);
        String command;

        System.out.println("\nAvailable commands:");
        System.out.println("START - Start ticket processing");
        System.out.println("STOP - Stop ticket processing");
        System.out.println("EXIT - Exit the application");

        while (true) {
            System.out.print("\nEnter command: ");
            command = scanner.nextLine().trim().toLowerCase();

            switch (command) {
                case "start":
                    startProcessing();
                    break;

                case "stop":
                    stopProcessing();
                    break;

                case "exit":
                    List<Ticket> soldTickets = ticketPoolService.getSoldTickets();
                    if (soldTickets.isEmpty()) {
                        System.out.println("No tickets were sold.");
                    } else {
                        soldTickets.forEach(System.out::println);
                    }
                    System.out.println("Exiting application...");
                    System.exit(0);
                    break;

                default:
                    System.out.println("Invalid command. Try again.");
            }
        }
    }

    private static void startProcessing() {
        if (ticketPoolService.isRunning()) {
            System.out.println("System is already running.");
            return;
        }

        System.out.println("Starting ticket processing...");
        ticketPoolService.setRunning(true);

        // Initialize vendor threads
        int numVendors = 4;
        vendorThreads = new Thread[numVendors];

        for (int i = 0; i < numVendors; i++) {
            vendorThreads[i] = new Thread(new Vendor(i + 1, ticketPoolService, ticketReleaseRate));
            vendorThreads[i].start();
        }

        // Initialize customer threads
        int numCustomers = 4;
        customerThreads = new Thread[numCustomers];

        for (int i = 0; i < numCustomers; i++) {
            customerThreads[i] = new Thread(new Customer(i + 1, ticketPoolService, customerRetrievalRate));
            customerThreads[i].start();
        }
        System.out.println("Ticket processing started.");
    }

    private static void stopProcessing() {
        if (!ticketPoolService.isRunning()) {
            System.out.println("System is not running.");
            return;
        }

        System.out.println("Stopping ticket processing...");
        ticketPoolService.setRunning(false); // Set the running flag to false

        try {
            for (Thread thread : vendorThreads) {
                thread.interrupt();
                thread.join();
            }
            for (Thread thread : customerThreads) {
                thread.interrupt();
                thread.join();
            }
            System.out.println("Ticket processing stopped");
        } catch (InterruptedException e) {
            System.out.println("Error while stopping threads: " + e.getMessage());
            Thread.currentThread().interrupt(); // Restore the interrupted status
        }
    }
}