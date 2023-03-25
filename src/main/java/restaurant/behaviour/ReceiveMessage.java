package restaurant.behaviour;

import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import restaurant.model.Person;
import restaurant.utility.ACLMessageUtility;

public class ReceiveMessage extends Behaviour {
    @Override
    public void action() {
        ACLMessage msg = myAgent.receive();
        if (msg != null) {
            System.out.println("Received: " + ACLMessageUtility.getContent(msg, Person.class));
        }
        else {
            block();
        }
    }

    @Override
    public boolean done() {
        return false;
    }
}
