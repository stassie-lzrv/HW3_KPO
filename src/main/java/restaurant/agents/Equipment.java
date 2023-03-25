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

@AgentJade(number = 5)
public class Equipment extends Agent implements SetAnnotationNumber {

    private final Logger logger = Logger.getLogger(Equipment.class.getName());

    @Override
    protected void setup() {
        logger.info("Hello from " + getAID().getName());

        // Register agent services
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());

        ServiceDescription sd = new ServiceDescription();
        sd.setType(TypesOfAgents.equipment);
        sd.setName("JADE-test");
        dfd.addServices(sd);

        try {
            DFService.register(this, dfd);
        } catch (FIPAException e) {
            logger.severe("Failed to register agent services: " + e.getMessage());
            doDelete();
        }

        // Add agent behaviours
        addBehaviour(new ReceiveMessage());
    }

    @Override
    protected void takeDown() {
        try {
            DFService.deregister(this);
        } catch (FIPAException e) {
            logger.warning("Failed to deregister agent services: " + e.getMessage());
        }
        logger.info("Equipment agent " + getAID().getName() + " terminated.");
    }

    @Override
    public void setNumber(int number) {
        SetAnnotationNumber.super.setNumber(number);
    }
}
