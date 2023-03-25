package restaurant.model.order;

public class OrderInformation {
    public static class Status{
        public static final String ready= "READY";
        public static final String cooking= "COOKING";
        public static final String notCooking= "NOT_COOKING";
    }
    String status = Status.notCooking;
    int minutesLeft = 0;

    public OrderInformation(String status, int minutesLeft) {
        this.status = status;
        this.minutesLeft = minutesLeft;
    }

    @Override
    public String toString() {
        return "OrderInfo{" +
                "status='" + status + '\'' +
                ", minutesLeft=" + minutesLeft +
                '}';
    }
}