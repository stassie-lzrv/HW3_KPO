package restaurant.behaviour;

import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import restaurant.model.Person;
import restaurant.utility.JsonRecord;


public class SendMessage extends Behaviour {

    private final Person message;
    private final AID[] recipients;

    public SendMessage(AID[] recipients, Person message) {
        this.recipients = recipients;
        this.message = message;
    }

    @Override
    public void action() {
        JsonRecord cfp = new JsonRecord(ACLMessage.CFP);
        for (AID recipient : recipients) {
            cfp.addReceiver(recipient);
        }
        cfp.setContent(message);
        myAgent.send(cfp);
    }

    @Override
    public boolean done() {
        return true;
    }
}
