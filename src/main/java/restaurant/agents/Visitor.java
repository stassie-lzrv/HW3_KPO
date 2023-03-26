package restaurant.agents;

import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import restaurant.behaviour.ReceiveMessage;
import restaurant.config.AgentJade;
import restaurant.setup_annotation.SetAnnotationNumber;

import java.util.logging.Level;
import java.util.logging.Logger;

@AgentJade(number = 5)
public class Visitor extends Agent implements SetAnnotationNumber {

    private static final Logger logger = Logger.getLogger(Visitor.class.getName());

    @Override
    protected void setup() {
        logger.log(Level.INFO, "Hello from {0}", getAID().getName());

        DFAgentDescription dfAgentDescription = new DFAgentDescription();
        dfAgentDescription.setName(getAID());
        ServiceDescription serviceDescription = new ServiceDescription();
        serviceDescription.setType(TypesOfAgents.visitor);
        serviceDescription.setName("JADE-test");
        dfAgentDescription.addServices(serviceDescription);
        try {
            DFService.register(this, dfAgentDescription);
        } catch (FIPAException fipaException) {
            logger.log(Level.SEVERE, "Failed to register with DF service.", fipaException);
        }

        addBehaviour(new ReceiveMessage());
    }

    @Override
    protected void takeDown() {
        try {
            DFService.deregister(this);
        } catch (FIPAException fipaException) {
            logger.log(Level.SEVERE, "Failed to deregister with DF service.", fipaException);
        }
        logger.log(Level.INFO, "Agent {0} terminating", getAID().getName());
    }

    @Override
    public void setNumber(int number) {
        SetAnnotationNumber.super.setNumber(number);
    }
}