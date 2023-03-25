package restaurant.agents;


import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import restaurant.behaviour.ReceiveMessage;
import restaurant.config.AgentJade;
import restaurant.setup_annotation.SetAnnotationNumber;

import java.util.logging.Logger;

public class Manager extends Agent implements SetAnnotationNumber {

    // Создание объекта логгера
    private final static Logger logger = Logger.getLogger(Manager.class.getName());

    @Override
    protected void setup() {
        logger.info("Hello from " + getAID().getName());

        // Описание агента
        DFAgentDescription dfAgentDescription = new DFAgentDescription();
        dfAgentDescription.setName(getAID());

        // Описание сервиса
        ServiceDescription serviceDescription = new ServiceDescription();
        serviceDescription.setType(TypesOfAgents.manager);
        serviceDescription.setName("JADE-test");

        dfAgentDescription.addServices(serviceDescription);

        try {
            // Регистрация сервиса
            DFService.register(this, dfAgentDescription);
        } catch (FIPAException e) {
            logger.warning("Exception caught: " + e.getMessage());
        }

        // Добавление поведения
        addBehaviour(new ReceiveMessage());
    }

    // Метод завершения работы агента
    @Override
    protected void takeDown() {
        try {
            // Снятие регистрации сервиса
            DFService.deregister(this);
        } catch (FIPAException e) {
            logger.warning("Exception caught: " + e.getMessage());
        }
        logger.info("Manager agent " + getAID().getName() + " terminating");
    }

    // Метод установки номера агента
    @Override
    public void setNumber(int number) {
        SetAnnotationNumber.super.setNumber(number);
    }
}
