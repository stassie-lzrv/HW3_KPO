package restaurant;

public class Main {
    public static void main(String[] args) {
        MainController mainController = new MainController();
        mainController.initAgents("restaurant.agents");
    }
}