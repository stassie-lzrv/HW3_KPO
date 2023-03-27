package restaurant.behaviour;

import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import restaurant.utility.JsonRecord;
import restaurant.model.Error;

public class SendMessageOnce extends Behaviour {
    String message;
    String ontology;
    String agentType;
    int index;
    AID name = null;
    public SendMessageOnce(String message, String ontology, String agentType, int index) {
        this.message = message;
        this.ontology = ontology;
        this.agentType = agentType;
        this.index = index;
    }

    public SendMessageOnce(String message, String ontology, AID name) {
        this.message = message;
        this.ontology = ontology;
        this.name = name;
    }

    boolean isSend = false;
    @Override
    public void action() {
        JsonRecord cfp = new JsonRecord(ACLMessage.CFP);
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType(agentType);
        template.addServices(sd);
        try {
            if(name == null){
                DFAgentDescription[] result = DFService.search(myAgent,template);
                name = result[index].getName();
            }
            cfp.addReceiver(name);
            cfp.setOntology(ontology);
            cfp.setContent(message);
            myAgent.send(cfp);
            isSend = true;
        } catch (IndexOutOfBoundsException ex) {
            //ex.printStackTrace();
        } catch (Exception ex){
            try {
                throw new Error("Cant find agent",ex.getMessage(),ex.getLocalizedMessage());
            } catch (Error ignored) {

            }
        }

    }
    @Override
    public boolean done() {
        return isSend;
    }
}

