package restaurant.agents;

import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import lombok.SneakyThrows;
import restaurant.MainController;
import restaurant.behaviour.SendMessageOnce;
import restaurant.config.AgentJade;
import restaurant.model.cook.CookList;
import restaurant.model.equipment.EquipmentList;
import restaurant.model.tech_card.DishCard;
import restaurant.setup_annotation.SetAnnotationNumber;

import static javax.sql.rowset.spi.SyncFactory.getLogger;
import static restaurant.json_files.GsonClass.gson;


@AgentJade(number = 5)
public class ProcessAgent extends Agent implements SetAnnotationNumber {

    CookList cookersList;
    EquipmentList kitchenEquipmentList;
    DishCard dishCard;
    AID orderAID;
    @SneakyThrows
    @Override
    protected void setup() {
        System.out.println("Hello from " + getAID().getName());
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            if (args[0] instanceof DishCard) {
                dishCard = (DishCard) args[0];
            }
            if (args[1] instanceof AID) {
                orderAID = (AID) args[1];
            }
        }
        // Описание агента
        DFAgentDescription agentDescription = new DFAgentDescription();
        agentDescription.setName(getAID());

        // Описание сервиса, предоставляемого агентом
        ServiceDescription serviceDescription = new ServiceDescription();
        serviceDescription.setType(TypesOfAgents.processAgent);
        serviceDescription.setName("JADE-test");

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
        findFreeCookerAndEquipment();
    }

    private void findFreeCookerAndEquipment() {
        boolean isFound = false;
        double waitTime = 0;
        for (var i : dishCard.operations) {
            waitTime += i.oper_time;
        }
        while (!(isFound)) {
            Integer cookerCounter = 0;
            for (var cooker : MainController.cookersList.cookers) {
                if (!cooker.cook_active) {
                    Integer equipCounter = 0;
                    for (var equip : MainController.kitchenEquipmentList.equipment) {
                        if (!equip.equip_active && equip.equip_type == dishCard.equip_type) {
                            addBehaviour(new SendMessageOnce(gson.toJson(dishCard), Ontologies.PROCESS_TO_COOKER,
                                    TypesOfAgents.cookerAgent, cookerCounter));
                            addBehaviour(new SendMessageOnce(gson.toJson(dishCard), Ontologies.PROCESS_TO_EQUIP,
                                    TypesOfAgents.equipmentAgent, equipCounter));
                            isFound = true;
                        }
                        equipCounter += 1;
                        addBehaviour(new SendMessageOnce(gson.toJson((int) (waitTime * 100)), Ontologies.PROCESS_TO_ORDER, orderAID));
                    }
                }
                cookerCounter += 1;
            }
        }
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
        getLogger().info("Process agent " + getAID().getName() + " terminated.");
    }

}

