package restaurant;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import restaurant.agents.*;
import restaurant.model.cook.CookList;
import restaurant.model.cook.Cooker;
import restaurant.model.equipment.Equipment;
import restaurant.model.equipment.EquipmentList;
import restaurant.model.menu.Menu;
import restaurant.model.order.Order;
import restaurant.model.order.OrderList;
import restaurant.model.Error;
import restaurant.model.stock.ProductAtStockList;
import restaurant.model.tech_card.DishCardList;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

import static restaurant.json_files.GsonClass.gson;

public class MainController {

    public static CookList cookersList;
    public static EquipmentList kitchenEquipmentList;

    private final ContainerController containerController;
    private Exception ex;

    public MainController() {
        final Runtime rt = Runtime.instance();
        final Profile p = new ProfileImpl();

        p.setParameter(Profile.MAIN_HOST, "localhost");
        p.setParameter(Profile.MAIN_PORT, "8080");
        p.setParameter(Profile.GUI, "true");
        containerController = rt.createMainContainer(p);
    }

    void initAgents() {
        try {
            createAgent(MainAgent.class, "MainAgent").start();
            createVisitorAgent();
            createCookerAgent();
            createMenuAgent();
            createStockAgent();
            createEquipmentAgent();
        } catch (Exception e) {
            new Error("Cant create agents", e.getMessage(), e.getLocalizedMessage());
        }
    }


    private String readFileFromResources(String filename) throws URISyntaxException, IOException {
        URL resource = getClass().getClassLoader().getResource(filename);
        byte[] bytes = Files.readAllBytes(Paths.get(resource.toURI()));
        return new String(bytes);
    }


    private void createVisitorAgent() throws StaleProxyException, Error {
        String json = "";
        try {
            json = readFileFromResources("visitors_orders.json");
        } catch (Exception ex) {
            this.ex = ex;
            throw new Error("File-error", ex.getMessage(), ex.getLocalizedMessage());
        }
        OrderList visitorsOrdersList = gson.fromJson(json, OrderList.class);
        if (visitorsOrdersList == null) {
            throw new Error("JSON-error", "visitorsOrdersList", "");
        }
        int counter = 0;
        for (var i : visitorsOrdersList.visitors_orders) {
            containerController.createNewAgent(
                    "VisitorAgent" + counter,
                    Visitor.class.getName(), new Order[]{i}).start();
            counter += 1;
        }
    }

    private void createStockAgent() throws StaleProxyException, Error {
        String json = "";
        try {
            json = readFileFromResources("products.json");
        } catch (Exception ex) {
            this.ex = ex;
            throw new Error("File-error", ex.getMessage(), ex.getLocalizedMessage());
        }
        ProductAtStockList productOnStockList = gson.fromJson(json, ProductAtStockList.class);
        if (productOnStockList == null) {
            throw new Error("JSON-error", "stockList", "");
        }
        containerController.createNewAgent(
                "StockAgent",
                StockAgent.class.getName(), new ProductAtStockList[]{productOnStockList}).start();
    }

    private void createMenuAgent() throws StaleProxyException, Error {
        String json = "";
        try {
            json = readFileFromResources("menu_dishes.json");
        } catch (Exception ex) {
            this.ex = ex;
            throw new Error("File-error", ex.getMessage(), ex.getLocalizedMessage());
        }
        Menu menu = gson.fromJson(json, Menu.class);
        if (menu == null) {
            throw new Error("JSON-error", "menuList", "");
        }
        try {
            json = readFileFromResources("dish_cards.json");
        } catch (Exception ex) {
            this.ex = ex;
            throw new Error("File-error", ex.getMessage(), ex.getLocalizedMessage());
        }
        DishCardList dishCards = gson.fromJson(json, DishCardList.class);
        if (dishCards == null) {
            throw new Error("JSON-error", "dishCards", "");
        }
        containerController.createNewAgent(
                "MenuAgent",
                MenuAgent.class.getName(), new Object[]{menu, dishCards}).start();
    }

    private void createCookerAgent() throws StaleProxyException, Error {
        String json = "";
        try {
            json = readFileFromResources("cookers.json");
        } catch (Exception ex) {
            this.ex = ex;
            throw new Error("File-error", ex.getMessage(), ex.getLocalizedMessage());
        }
        cookersList = gson.fromJson(json, CookList.class);
        if (cookersList == null) {
            throw new Error("JSON-error", "cookersList", "");
        }

        int counter = 0;
        for (var i : cookersList.cookers) {
            var t = containerController.createNewAgent(
                    "CookerAgent" + counter,
                    CookerAgent.class.getName(), new Cooker[]{i});
            t.start();
            counter += 1;
        }
    }

    private void createEquipmentAgent() throws StaleProxyException, Error {
        String json = "";
        try {
            json = readFileFromResources("equipment.json");
        } catch (Exception ex) {
            this.ex = ex;
            throw new Error("File-error", ex.getMessage(), ex.getLocalizedMessage());
        }
        kitchenEquipmentList = gson.fromJson(json, EquipmentList.class);
        if (kitchenEquipmentList == null) {
            throw new Error("JSON-error", "EquipmentAgent", "");
        }
        int counter = 0;
        for (var i : kitchenEquipmentList.equipment) {
            var t = containerController.createNewAgent(
                    "EquipmentAgent" + counter,
                    EquipmentAgent.class.getName(), new Equipment[]{i});
            t.start();
            counter += 1;
        }
    }

    private AgentController createAgent(Class<?> clazz, String agentName) throws StaleProxyException {
        return containerController.createNewAgent(
                agentName,
                clazz.getName(),
                null);
    }

}
