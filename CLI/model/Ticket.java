package model;

public class Ticket {
    private final String ticketNum;
    private boolean isSold;
    private int vendorId;
    private int customerId;

    public Ticket(String ticketNum) {
        this.ticketNum = ticketNum;
        this.isSold = false;
    }

    public String getTicketCode() {
        return ticketNum;
    }

    public boolean isSold() {
        return isSold;
    }

    public void setSold(boolean sold) {
        isSold = sold;
    }

    public int getVendorId() {
        return vendorId;
    }

    public void setVendorId(int vendorId) {
        this.vendorId = vendorId;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    @Override
    public String toString() {
        return "Ticket{" +
                "ticketCode='" + ticketNum + '\'' +
                ", isSold=" + isSold +
                ", vendorId=" + vendorId +
                ", customerId=" + customerId +
                '}';
    }
}
