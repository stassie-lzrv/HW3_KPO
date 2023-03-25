package restaurant.agents;

import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import lombok.SneakyThrows;
import restaurant.behaviour.ReceiveMessage;
import restaurant.config.AgentJade;

import static javax.sql.rowset.spi.SyncFactory.getLogger;

@AgentJade(number = 5)
public class Cooker extends Agent {

    @SneakyThrows
    @Override
    protected void setup() {
        // Описание агента
        DFAgentDescription agentDescription = new DFAgentDescription();
        agentDescription.setName(getAID());

        // Описание сервиса, предоставляемого агентом
        ServiceDescription serviceDescription = new ServiceDescription();
        serviceDescription.setType("restaurant.agents.TypesOfAgents.cooker");
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
        addBehaviour(new ReceiveMessage());
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
}