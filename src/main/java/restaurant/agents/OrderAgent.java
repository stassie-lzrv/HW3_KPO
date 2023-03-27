package restaurant.agents;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.gson.reflect.TypeToken;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import restaurant.behaviour.SendMessageOnce;
import restaurant.config.AgentJade;
import restaurant.json_files.GsonClass;
import restaurant.model.order.Order;
import restaurant.model.order.OrderInformation;
import restaurant.model.tech_card.DishCard;
import restaurant.setup_annotation.SetAnnotationNumber;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import restaurant.model.Error;

import static java.lang.Double.valueOf;

@AgentJade()
public class OrderAgent extends Agent implements SetAnnotationNumber {

    static AtomicInteger counter = new AtomicInteger(0);
    Order visitorsOrder;
    AID visitorAID;
    Map<AID, Integer> mapAgentTime = new HashMap<>();
    ArrayList<DishCard> neededDishes;
    Map<Integer, Double> existingResources = new HashMap<>();
    private static final Logger logger = Logger.getLogger(OrderAgent.class.getName());

    @Override
    protected void setup() {
        logger.info("Hello from " + getAID().getName());


        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            if (args[0] instanceof Order) {
                visitorsOrder = (Order) args[0];
            }
            if (args[1] instanceof AID) {
                visitorAID = (AID) args[1];
            }
        }

        DFAgentDescription dfAgentDescription = new DFAgentDescription();
        dfAgentDescription.setName(getAID());
        ServiceDescription serviceDescription = new ServiceDescription();
        serviceDescription.setType(TypesOfAgents.orderAgent);
        serviceDescription.setName(TypesOfAgents.orderAgent);
        dfAgentDescription.addServices(serviceDescription);
        try {
            DFService.register(this, dfAgentDescription);
        } catch (FIPAException fipaException) {
            logger.severe("Error registering service: " + fipaException.getMessage());
            fipaException.printStackTrace();
        }

        addBehaviour(new SendStatusToVisitor(this));
        addBehaviour(new SendMessageOnce(GsonClass.gson.toJson(visitorsOrder.vis_ord_dishes.stream().
                map(e -> e.menu_dish).toArray()),
                Ontologies.ORDER_TO_MENU,
                TypesOfAgents.menuAgent, 0));
    }


    private void createProcessAgents() {

        for (var i : neededDishes) {
            Map<Integer, Double> copyOfExistingProducts = new HashMap<>();
            for (Map.Entry<Integer, Double> entry : existingResources.entrySet()) {
                copyOfExistingProducts.put(entry.getKey(), valueOf(entry.getValue()));
            }

            boolean isEnought = true;
            for (var j : i.operations) {
                for (var k : j.oper_products) {
                    copyOfExistingProducts.put(k.prod_type, copyOfExistingProducts.get(k.prod_type) - k.prod_quantity);
                    if (copyOfExistingProducts.get(k.prod_type) < 0) {
                        isEnought = false;
                        break;
                    }
                }
                if (!isEnought) {
                    break;
                }
            }
            if (!isEnought) {
                continue;
            }
            existingResources = copyOfExistingProducts;
            ContainerController cnc = this.getContainerController();

            try {
                var t = cnc.createNewAgent("ProcessAgent" + counter.addAndGet(1), ProcessAgent.class.getName(),
                        new Object[]{i, getAID()});
                t.start();
            } catch (StaleProxyException e) {
                new Error("Cannot create process agent", e.getMessage(),
                        e.getLocalizedMessage());
            }
        }
    }
    @Override
    protected void takeDown() {
        // Deregister from the yellow pages
        try {
            DFService.deregister(this);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
        // Print out a dismissal message
        System.out.println("testAgent " + getAID().getName() + " terminating");
    }

    @Override
    public void setNumber(int number) {
        SetAnnotationNumber.super.setNumber(number);
    }
    private static class SendStatusToVisitor extends Behaviour {
        OrderAgent orderAgent;

        public SendStatusToVisitor(OrderAgent orderAgent) {
            this.orderAgent = orderAgent;
        }

        @Override
        public void action() {

            ACLMessage msg = myAgent.receive();
            if (msg != null) {
                if (Objects.equals(msg.getOntology(), Ontologies.PROCESS_TO_ORDER)) {
                    String json = msg.getContent();
                    Integer timeToCook = GsonClass.gson.fromJson(json, Integer.class);
                    orderAgent.mapAgentTime.put(msg.getSender(), timeToCook);
                    Integer currentTotalTime = 0;
                    for (var i : orderAgent.mapAgentTime.values()) {
                        currentTotalTime += i;
                    }
                    String state = OrderInformation.Status.notCooking;
                    if (currentTotalTime > 0) {
                        state = OrderInformation.Status.cooking;
                    }
                    OrderInformation orderInfo = new OrderInformation(state, currentTotalTime);
                    myAgent.addBehaviour(new SendMessageOnce(
                            GsonClass.gson.toJson(orderInfo), Ontologies.ORDER_TO_VISITOR,
                            orderAgent.visitorAID));
                } else if (Objects.equals(msg.getOntology(), Ontologies.MENU_TO_ORDER)) {
                    String json = msg.getContent();
                    orderAgent.neededDishes = GsonClass.fromJSONMapper(new TypeReference<ArrayList<DishCard>>() {
                    }, json);
                    myAgent.addBehaviour(new SendMessageOnce(GsonClass.gson.toJson(getNeededProducts()),
                            Ontologies.ORDER_TO_STOCK,
                            TypesOfAgents.stockAgent, 0));
                } else if (Objects.equals(msg.getOntology(), Ontologies.STOCK_TO_ORDER)) {
                    String json = msg.getContent();
                    Type type = new TypeToken<HashMap<Integer, Double>>() {
                    }.getType();
                    orderAgent.existingResources = GsonClass.gson.fromJson(json, type);
                    orderAgent.createProcessAgents();
                }
            } else {
                block();
            }
        }

        private Map<Integer, Double> getNeededProducts() {
            Map<Integer, Double> res = new HashMap<>();
            for (var i : orderAgent.neededDishes) {
                for (var j : i.operations) {
                    for (var k : j.oper_products) {
                        if (!res.containsKey(k.prod_type)) {
                            res.put(k.prod_type, k.prod_quantity);
                        } else {
                            res.put(k.prod_type, res.get(k.prod_type) + k.prod_quantity);
                        }
                    }
                }
            }
            return res;
        }

        @Override
        public boolean done() {
            return false;
        }
    }
}
