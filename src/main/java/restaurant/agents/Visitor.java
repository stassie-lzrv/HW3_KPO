package restaurant.agents;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import restaurant.config.AgentJade;
import restaurant.json_files.GsonClass;
import restaurant.model.order.Order;
import restaurant.model.order.OrderInformation;
import restaurant.setup_annotation.SetAnnotationNumber;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import static restaurant.json_files.GsonClass.gson;

@AgentJade(number = 5)
public class Visitor extends Agent implements SetAnnotationNumber {
    Order visitorsOrder;

    private static final Logger logger = Logger.getLogger(Visitor.class.getName());

    @Override
    protected void setup() {
        logger.log(Level.INFO, "Hello from {0}", getAID().getName());
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            if (args[0] instanceof Order) {
                visitorsOrder = (Order) args[0];
            }
        }
        DFAgentDescription dfAgentDescription = new DFAgentDescription();
        dfAgentDescription.setName(getAID());
        ServiceDescription serviceDescription = new ServiceDescription();
        serviceDescription.setType(TypesOfAgents.visitorAgent);
        serviceDescription.setName(TypesOfAgents.visitorAgent);
        dfAgentDescription.addServices(serviceDescription);
        try {
            DFService.register(this, dfAgentDescription);
        } catch (FIPAException fipaException) {
            logger.log(Level.SEVERE, "Failed to register with DF service.", fipaException);
        }

        addBehaviour(
                new restaurant.behaviour.SendMessageOnce(
                        gson.toJson(visitorsOrder),
                        Ontologies.VISITOR_TO_MAIN,
                        TypesOfAgents.mainAgent, 0));
        addBehaviour(new RecieveTimeMessage());
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

    private static class RecieveTimeMessage extends Behaviour {
        @Override
        public void action() {
            ACLMessage msg = myAgent.receive();
            if (msg != null) {
                if (Objects.equals(msg.getOntology(), Ontologies.ORDER_TO_VISITOR)) {
                    String json = msg.getContent();
                    OrderInformation orderInfo = GsonClass.gson.fromJson(json, OrderInformation.class);
                    System.out.println(getAgent().getName() + " info " + orderInfo.toString());
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