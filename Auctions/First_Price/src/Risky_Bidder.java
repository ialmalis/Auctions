import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import java.util.*;

public class Risky_Bidder extends Agent {

	public String product_name;
    public boolean proposalRe = false;
    public int budget,belief;
   
    static Random random = new Random();

	protected void setup() {

		budget = random.nextInt(500) + 800;
		System.out.println("Bidder "+getAID().getLocalName()+" has budget " + budget);

		DFAgentDescription DFAD = new DFAgentDescription();
		DFAD.setName(getAID());
		ServiceDescription SD = new ServiceDescription();
		SD.setType("First Price");
		SD.setName("First Price");
		DFAD.addServices(SD);
		try {
			DFService.register(this, DFAD);
		}
		catch (FIPAException FE) {
			FE.printStackTrace();
		}

		
		this.addBehaviour(new receiveProposal2(this));

		// Winner
		this.addBehaviour(new Winner2(this));

		// Add the behaviour for receiving INFORM
		this.addBehaviour(new Inform2(this));
	}

	
	protected void takeDown() {
	
		try {
			DFService.deregister(this);
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}

		
		System.out.println("Bidder "+getAID().getLocalName()+" is leaving...");
	}
}


class receiveProposal2 extends CyclicBehaviour {

    private Risky_Bidder testAgent;

    public receiveProposal2(Risky_Bidder agent) {
        super(agent);
        testAgent = agent;
    }

    public void action() {
        MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
        ACLMessage msg = testAgent.receive(mt);

       
        if (testAgent.budget == 0){
            System.out.println("No budget left, Agent "+testAgent.getAID().getLocalName()+" is leaving");
            testAgent.doDelete();
        }

        if (msg != null) {
           
            String myText = msg.getContent();
            String[] textMsg = myText.split(",");
            String product_name = textMsg[0];
            int product_price = Integer.parseInt(textMsg[1]);
            ACLMessage reply = msg.createReply();
            
            //the belief of bidder about product's price, always bigger than initial price,how much he wants that
            double pos=Math.abs(testAgent.random.nextDouble()-0.5);
            double testbelief=product_price*pos + product_price;
            //bid a little bit lower than belief
            testAgent.belief=(int)(testbelief*0.92);
            
            if ((testAgent.budget >= product_price) && (testAgent.belief>=product_price)){

            	if (testAgent.budget < testAgent.belief)
            	{
            		testAgent.belief=testAgent.budget;
            	}
                testAgent.product_name = product_name;
                
                // Proposal
                reply.setPerformative(ACLMessage.PROPOSE);
                reply.setContent(String.valueOf( testAgent.belief));
                System.out.println(testAgent.getLocalName() + " sent bid with price " +  testAgent.belief);
            }
            // 
            else {
                reply.setPerformative(ACLMessage.REFUSE);
                reply.setContent("out of process....");
                System.out.println(testAgent.getLocalName() + " does not follow the auction of "+product_name);
            }

            testAgent.send(reply);
        }
        else {
            block();
        }
    }
}

class Winner2 extends CyclicBehaviour {

    private Risky_Bidder testAgent;

    public Winner2(Risky_Bidder agent) {
        super(agent);
        testAgent = agent;
    }

    public void action() {
        MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
        ACLMessage msg = testAgent.receive(mt);
        if (msg != null) {
            
            String myText = msg.getContent();
            String[] textMsg = myText.split(",");
            String product_name = textMsg[0];
            int price = Integer.parseInt(textMsg[1]);
            ACLMessage reply = msg.createReply();

            reply.setPerformative(ACLMessage.INFORM);
            
            System.out.println(product_name+" is sold in  " + price);

            testAgent.send(reply);

            
            testAgent.budget -= price;    
            System.out.println("the remaining budget of "+testAgent.getAID().getLocalName()+" is.."+testAgent.budget);
        }
        else {
            block();
        }
    }
}


class Inform2 extends CyclicBehaviour {
	 private Risky_Bidder testAgent;

	    public Inform2(Risky_Bidder agent) {
	        super(agent);
	        testAgent = agent;
	    }
    public void action() {
        MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
        ACLMessage msg = testAgent.receive(mt);
        if (msg != null) {
            
            System.out.println(msg.getContent());
        }
        else {
            block();
        }
    }
}

