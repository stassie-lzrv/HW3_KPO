package restaurant.agents;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import restaurant.agents.Ontologies;
import restaurant.agents.OrderAgent;
import restaurant.agents.TypesOfAgents;
import restaurant.config.AgentJade;
import restaurant.json_files.GsonClass;
import restaurant.model.order.Order;
import restaurant.setup_annotation.SetAnnotationNumber;
import restaurant.model.Error;

import java.util.Objects;

@AgentJade("MainAgent")
public class MainAgent extends Agent implements SetAnnotationNumber {
    @Override
    protected void setup() {
        System.out.println("Hello from " + getAID().getName());
        // Register the book-selling service in the yellow pages
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setName(TypesOfAgents.mainAgent);
        sd.setType(TypesOfAgents.mainAgent);
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }


        addBehaviour(new CreateOrderAgent());
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
        System.out.println("Main Agent " + getAID().getName() + " terminating");
    }

    @Override
    public void setNumber(int number) {
        SetAnnotationNumber.super.setNumber(number);
    }

    private static class CreateOrderAgent extends Behaviour {
        public static int counter = 0;

        @Override
        public void action() {
            ACLMessage msg = myAgent.receive();
            if (msg != null) {
                if (Objects.equals(msg.getOntology(), Ontologies.VISITOR_TO_MAIN)) {
                    String json = msg.getContent();
                    Order list = GsonClass.gson.fromJson(json, Order.class);
                    ContainerController cnc = myAgent.getContainerController();
                    try {
                        var t = cnc.createNewAgent(TypesOfAgents.orderAgent + counter, OrderAgent.class.getName(),
                                new Object[]{list, msg.getSender()});
                        t.start();
                    } catch (StaleProxyException e) {
                        new Error("Cannot create order agent", e.getMessage(),
                                e.getLocalizedMessage());
                    }
                    counter += 1;
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
