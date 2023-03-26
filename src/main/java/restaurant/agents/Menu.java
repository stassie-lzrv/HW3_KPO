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
public class Menu extends Agent implements SetAnnotationNumber {

    private static final Logger logger = Logger.getLogger(Menu.class.getName());

    @Override
    protected void setup() {
        logger.info("Hello from " + getAID().getName());

        DFAgentDescription dfAgentDescription = new DFAgentDescription();
        dfAgentDescription.setName(getAID());
        ServiceDescription serviceDescription = new ServiceDescription();
        serviceDescription.setType(TypesOfAgents.menu);
        serviceDescription.setName("JADE-test");
        dfAgentDescription.addServices(serviceDescription);
        try {
            DFService.register(this, dfAgentDescription);
        } catch (FIPAException fipaException) {
            logger.severe("Error registering agent: " + fipaException.getMessage());
        }

        addBehaviour(new ReceiveMessage());
    }

    @Override
    protected void takeDown() {
        try {
            DFService.deregister(this);
        } catch (FIPAException fipaException) {
            logger.severe("Error deregistering agent: " + fipaException.getMessage());
        }
        logger.info("Menu agent " + getAID().getName() + " terminating");
    }

    @Override
    public void setNumber(int number) {
        SetAnnotationNumber.super.setNumber(number);
    }
}
