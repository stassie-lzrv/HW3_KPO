package restaurant.agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import restaurant.behaviour.SendMessage;
import restaurant.config.AgentJade;
import restaurant.model.Person;

@AgentJade("Hello")
public class HelloWorld extends Agent {

    private AID[] testAgents;

    @Override
    protected void setup() {
        System.out.println("Hello world! I'm an agent!");
        System.out.println("My local name is " + getAID().getLocalName());
        System.out.println("My GUID is " + getAID().getName());
        System.out.println("My addresses are " + String.join(",", getAID().getAddressesArray()));


        findTestAgents();
    }

    private void findTestAgents() {
        addBehaviour(new TickerBehaviour(this, 10000) {
            protected void onTick() {
                DFAgentDescription dfAgentDescription = new DFAgentDescription();
                ServiceDescription serviceDescription = new ServiceDescription();
                serviceDescription.setType("test-squad");
                dfAgentDescription.addServices(serviceDescription);
                try {
                    DFAgentDescription[] result = DFService.search(myAgent, dfAgentDescription);
                    testAgents = new AID[result.length];
                    for (int i = 0; i < result.length; ++i) {
                        testAgents[i] = result[i].getName();
                    }
                } catch (FIPAException fipaException) {
                    fipaException.printStackTrace();
                }
                myAgent.addBehaviour(new SendMessage(testAgents,
                        new Person(
                                "Anton",
                                "Kalinin"
                        )));
            }
        });
    }
}
