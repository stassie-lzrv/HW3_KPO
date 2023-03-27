package restaurant.agents;

import com.google.gson.reflect.TypeToken;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import lombok.SneakyThrows;
import restaurant.behaviour.SendMessageOnce;
import restaurant.config.AgentJade;
import restaurant.json_files.GsonClass;
import restaurant.model.stock.ProductAtStockList;
import restaurant.setup_annotation.SetAnnotationNumber;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static java.lang.Math.min;
import static javax.sql.rowset.spi.SyncFactory.getLogger;

@AgentJade()
public class StockAgent extends Agent implements SetAnnotationNumber {

    ProductAtStockList productOnStockList;
    @SneakyThrows
    @Override
    protected void setup() {
        System.out.println("Hello from " + getAID().getName());
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            if (args[0] instanceof ProductAtStockList) {
                productOnStockList = (ProductAtStockList) args[0];
            }
        }
        // Описание агента
        DFAgentDescription agentDescription = new DFAgentDescription();
        agentDescription.setName(getAID());

        // Описание сервиса, предоставляемого агентом
        ServiceDescription serviceDescription = new ServiceDescription();
        serviceDescription.setType(TypesOfAgents.stockAgent);
        serviceDescription.setName(TypesOfAgents.stockAgent);

        // Добавление сервиса в описание агента
        agentDescription.addServices(serviceDescription);

        try {
            // Регистрация агента в сервисной желтой странице
            DFService.register(this, agentDescription);
        } catch (FIPAException e) {
            // Обработка исключения
            getLogger().severe("Failed to register agent " + getAID().getName() + " to DFService: " + e.getMessage());
            doDelete();
            return;
        }

        // Добавление поведения для обработки сообщений
        addBehaviour(new ResendProductListToOrder(this));
    }

    @SneakyThrows
    @Override
    protected void takeDown() {
        try {
            // Удаление агента
            DFService.deregister(this);
        } catch (FIPAException e) {
            // Обработка исключения
            getLogger().severe("Failed to deregister agent " + getAID().getName() + " from DFService: " + e.getMessage());
        }

        // Логирование завершения работы агента
        getLogger().info("Stock agent " + getAID().getName() + " terminated.");
    }

    @Override
    public void setNumber(int number) {
        SetAnnotationNumber.super.setNumber(number);
    }

    private static class ResendProductListToOrder extends Behaviour {
        StockAgent stockAgent;

        public ResendProductListToOrder(StockAgent stockAgent) {
            this.stockAgent = stockAgent;
        }

        @Override
        public void action() {
            ACLMessage msg = myAgent.receive();
            if (msg != null) {
                if (Objects.equals(msg.getOntology(), Ontologies.ORDER_TO_STOCK)) {
                    String json = msg.getContent();
                    Type type = new TypeToken<HashMap<Integer, Double>>() {
                    }.getType();
                    Map<Integer, Double> askedProducts = GsonClass.gson.fromJson(json, type);
                    myAgent.addBehaviour(new SendMessageOnce(
                            GsonClass.gson.toJson(getNeededProducts(askedProducts)),
                            Ontologies.STOCK_TO_ORDER, msg.getSender()));
                }
            } else {
                block();
            }
        }

        private Map<Integer, Double> getNeededProducts(Map<Integer, Double> askedProducts) {

            Map<Integer, Double> res = new HashMap<>();
            Map<Integer, Double> existingProducts = new HashMap<>();

            for (var i : stockAgent.productOnStockList.products) {
                existingProducts.put(i.prod_item_type, i.prod_item_quantity);
            }
            for (var i : askedProducts.keySet()) {
                if (existingProducts.containsKey(i)) {
                    res.put(i, min(askedProducts.get(i), existingProducts.get(i)));
                } else {
                    res.put(i, 0.0);
                    continue;
                }
                for (var j : stockAgent.productOnStockList.products) {
                    if (j.prod_item_type == i) {
                        j.prod_item_quantity -= res.get(i);
                        break;
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
