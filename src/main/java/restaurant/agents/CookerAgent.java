package restaurant.agents;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import lombok.SneakyThrows;
import restaurant.ProcessLogger;
import restaurant.config.AgentJade;
import restaurant.model.cook.Cooker;
import restaurant.model.cooking.CookingLog;
import restaurant.model.cooking.CookingOperation;
import restaurant.model.tech_card.DishCard;
import restaurant.setup_annotation.SetAnnotationNumber;

import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import static javax.sql.rowset.spi.SyncFactory.getLogger;
import static restaurant.json_files.GsonClass.gson;

@AgentJade()
public class CookerAgent extends Agent implements SetAnnotationNumber {

    Cooker cooker;

    @SneakyThrows
    @Override
    protected void setup() {
        System.out.println("Hello from " + getAID().getName());
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            if (args[0] instanceof Cooker) {
                cooker = (Cooker) args[0];
            }
        }
        // Описание агента
        DFAgentDescription agentDescription = new DFAgentDescription();
        agentDescription.setName(getAID());

        // Описание сервиса, предоставляемого агентом
        ServiceDescription serviceDescription = new ServiceDescription();
        serviceDescription.setType(TypesOfAgents.cookerAgent);
        serviceDescription.setName(TypesOfAgents.cookerAgent);

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
        addBehaviour(new MakeMeWait(this));
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
        getLogger().info("Cooker agent " + getAID().getName() + " terminated.");
    }

    @Override
    public void setNumber(int number) {
        SetAnnotationNumber.super.setNumber(number);
    }

    private static class MakeMeWait extends Behaviour {
        public static AtomicInteger counter = new AtomicInteger(0);

        CookerAgent cookerAgent;

        public MakeMeWait(CookerAgent cookerAgent) {
            this.cookerAgent = cookerAgent;
        }

        @Override
        public void action() {
            ACLMessage msg = myAgent.receive();
            if (msg != null) {
                if (Objects.equals(msg.getOntology(), Ontologies.PROCESS_TO_COOKER)) {
                    CookingLog log = new CookingLog();
                    log.proc_id = counter.get();
                    counter.addAndGet(1);

                    String json = msg.getContent();
                    DishCard dishCard = gson.fromJson(json, DishCard.class);
                    log.proc_active = false;
                    log.proc_started = new Date();
                    double wait = 0.0;
                    for (var i : (dishCard.operations)) {
                        wait += i.oper_time;
                    }
                    cookerAgent.cooker.cook_active = true;
                    myAgent.doWait((int) (wait * 100000));
                    log.proc_ended = new Date();
                    log.ord_dish = dishCard.card_id;
                    log.proc_operations = new ArrayList<>();
                    for (var i : dishCard.operations) {
                        var cookingOper = new CookingOperation();
                        cookingOper.proc_oper = i.oper_type;
                        log.proc_operations.add(cookingOper);
                    }
                    ProcessLogger.logger.fine(gson.toJson(log));
                    cookerAgent.cooker.cook_active = false;
                }
            } else {
                block();
            }
        }

        @Override
        public boolean done() {
            return false;
        }
    }
}