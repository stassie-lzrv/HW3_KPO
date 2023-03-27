package restaurant.agents;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import restaurant.config.AgentJade;
import restaurant.model.equipment.Equipment;
import restaurant.model.tech_card.DishCard;
import restaurant.setup_annotation.SetAnnotationNumber;

import java.util.Objects;
import java.util.logging.Logger;

import static restaurant.json_files.GsonClass.gson;

@AgentJade()
public class EquipmentAgent extends Agent implements SetAnnotationNumber {

    Equipment kitchenEquipment;
    private final Logger logger = Logger.getLogger(EquipmentAgent.class.getName());

    @Override
    protected void setup() {
        logger.info("Hello from " + getAID().getName());


        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            if (args[0] instanceof Equipment) {
                kitchenEquipment = (Equipment) args[0];
            }
        }
        // Register agent services
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());

        ServiceDescription sd = new ServiceDescription();
        sd.setType(TypesOfAgents.equipmentAgent);
        sd.setName(TypesOfAgents.equipmentAgent);
        dfd.addServices(sd);

        try {
            DFService.register(this, dfd);
        } catch (FIPAException e) {
            logger.severe("Failed to register agent services: " + e.getMessage());
            doDelete();
        }

        // Add agent behaviours
        addBehaviour(new MakeMeWait(this));
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
    private static class MakeMeWait extends Behaviour {
        EquipmentAgent equipmentAgent;

        public MakeMeWait(EquipmentAgent cookerAgent) {
            this.equipmentAgent = cookerAgent;
        }

        @Override
        public void action() {
            ACLMessage msg = myAgent.receive();
            if (msg != null) {
                if (Objects.equals(msg.getOntology(), Ontologies.PROCESS_TO_EQUIP)) {
                    String json = msg.getContent();
                    DishCard dishCard = gson.fromJson(json, DishCard.class);
                    double wait = 0.0;
                    for (var i : (dishCard.operations)) {
                        wait += i.oper_time;
                    }
                    equipmentAgent.kitchenEquipment.equip_active = true;
                    myAgent.doWait((int) (wait * 100000));
                    equipmentAgent.kitchenEquipment.equip_active = false;
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
