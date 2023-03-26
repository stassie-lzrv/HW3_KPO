package restaurant.agents;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import restaurant.behaviour.SendMessageOnce;
import restaurant.config.AgentJade;
import restaurant.json_files.GsonClass;
import restaurant.model.menu.Menu;
import restaurant.model.tech_card.DishCard;
import restaurant.model.tech_card.DishCardList;
import restaurant.setup_annotation.SetAnnotationNumber;

import java.util.ArrayList;
import java.util.Objects;
import java.util.logging.Logger;

@AgentJade()
public class MenuAgent extends Agent implements SetAnnotationNumber {

    private static final Logger logger = Logger.getLogger(MenuAgent.class.getName());

    Menu menu;
    DishCardList arrayOfDishCards;
    @Override
    protected void setup() {
        logger.info("Hello from " + getAID().getName());

        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            if (args[0] instanceof Menu) {
                menu = (Menu) args[0];
            }
            if (args[1] instanceof DishCardList) {
                arrayOfDishCards = (DishCardList) args[1];
            }
        }
        DFAgentDescription dfAgentDescription = new DFAgentDescription();
        dfAgentDescription.setName(getAID());
        ServiceDescription serviceDescription = new ServiceDescription();
        serviceDescription.setType(TypesOfAgents.menuAgent);
        serviceDescription.setName(TypesOfAgents.menuAgent);
        dfAgentDescription.addServices(serviceDescription);
        try {
            DFService.register(this, dfAgentDescription);
        } catch (FIPAException fipaException) {
            logger.severe("Error registering agent: " + fipaException.getMessage());
        }

        addBehaviour(new resendDishesToOrderAgent(this));
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
    private static class resendDishesToOrderAgent extends Behaviour {
        MenuAgent menuAgent;

        public resendDishesToOrderAgent(MenuAgent menuAgent) {
            this.menuAgent = menuAgent;
        }

        @Override
        public void action() {
            ACLMessage msg = myAgent.receive();
            if (msg != null) {
                if (Objects.equals(msg.getOntology(), Ontologies.ORDER_TO_MENU)) {
                    String json = msg.getContent();
                    int[] menuList = GsonClass.gson.fromJson(json, int[].class);
                    myAgent.addBehaviour(new SendMessageOnce(
                            GsonClass.gson.toJson(getNeededDishes(menuList)),
                            Ontologies.MENU_TO_ORDER, msg.getSender()));
                }
            } else {
                block();
            }
        }

        private ArrayList<DishCard> getNeededDishes(int[] menuList) {
            ArrayList<DishCard> res = new ArrayList<>();
            for (var i : menuList) {
                Integer indexInDishesCard = -1;
                for (var j : menuAgent.menu.menu_dishes) {
                    if (j.menu_dish_id == i) {
                        indexInDishesCard = j.menu_dish_card;
                    }
                }
                for (var j : menuAgent.arrayOfDishCards.dish_cards) {
                    if (j.card_id == indexInDishesCard) {
                        res.add(j);
                    }
                }
            }
            return res;
        }

        @Override
        public boolean done() {
            return false;
        }
    }
}
