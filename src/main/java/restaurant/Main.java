package restaurant;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import restaurant.agents.Visitor;

import java.io.FileReader;
import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        MainController mainController = new MainController();
        mainController.initAgents();
    }
}