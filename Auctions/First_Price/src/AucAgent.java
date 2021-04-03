import java.util.Hashtable;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class AucAgent extends Agent {

	private Hashtable products_list;
	private GuiAuc myGui;
   	public AID[] myBidders;
    public AID wonBidder=null;
    public int won_price=0;
    public MessageTemplate mt;
    public boolean findB = false;
    public boolean ProposalsSent = false;
    public boolean myCondition = false;
    public FindBidder p = null;
    public Proposals q = null;
    public Receive_Proposals r = null;
   

    @Override
    protected void setup() {

		System.out.println(getAID().getName()+" Auctioneer starts...");

		products_list = new Hashtable();
        myGui = new GuiAuc(this);
		myGui.showGui();
        this.addBehaviour(new requestToBidders(this));
    }

	
	protected void takeDown() {
		System.out.println("Auction has been terminated...");
		myGui.dispose();}
	
	
	public int getPrice(final String productName) {
        Integer product_price = (Integer) products_list.get(productName);
        return product_price;}
	public String getProduct() {
        return (String)products_list.keySet().toArray()[0];
    }
	public void addToList(final String productName, final int product_price) {

		addBehaviour(
                     new OneShotBehaviour() {
                         public void action() {
                             products_list.put(productName, new Integer(product_price));
                                              }});}
	
	public void removeList(final String productName) {

		addBehaviour(new OneShotBehaviour() {
                         public void action() {
                         Integer dummy=(Integer) products_list.remove(productName);}});}
                         

    public boolean isproducts_listEmpty() {
        return products_list.isEmpty();}}
   
class requestToBidders extends TickerBehaviour {

    private AucAgent testAgent;

    private String productName;

    public requestToBidders(AucAgent agent) {
        super(agent, 6000);
        testAgent = agent;
    }

    @Override
    protected void onTick(){

        testAgent.findB = false;
        testAgent.ProposalsSent = false;
        testAgent.myCondition = false;
        
        
        if (!testAgent.isproducts_listEmpty()) {

            if (testAgent.p != null) testAgent.removeBehaviour(testAgent.p);
            if (testAgent.q != null) testAgent.removeBehaviour(testAgent.q);
            if (testAgent.r != null) testAgent.removeBehaviour(testAgent.r);
            

            productName = testAgent.getProduct();
            System.out.println("Item " + productName+" is now ready for sale...");

            
            testAgent.p = new FindBidder(testAgent);
            testAgent.addBehaviour(testAgent.p);
                    
            // Send Proposals.....
            testAgent.q = new Proposals(testAgent, productName, testAgent.getPrice(productName));
            testAgent.addBehaviour(testAgent.q);

            // Receive proposals
            testAgent.r = new Receive_Proposals(testAgent,productName);
            testAgent.addBehaviour(testAgent.r);
            
            
        }        
        else {
			System.out.println("The Auction List is empty....");
        }
    }
}

class FindBidder extends Behaviour {

    private AucAgent testAgent;

    public FindBidder(AucAgent agent) {
        super(agent);
        testAgent = agent;
    }
    
    public void action() {

       

            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType("First Price");
            template.addServices(sd);
            try {
                DFAgentDescription[] BiddersList = DFService.search(testAgent, template); 
                testAgent.myBidders = new AID[BiddersList.length];
                    for (int i = 0; i < BiddersList.length; i++) {
                        testAgent.myBidders[i] = BiddersList[i].getName();}
                    if (BiddersList.length>0) {
                    testAgent.findB=true;}
                    else {testAgent.doDelete();}
            }
                       
            catch (FIPAException fe) {
                fe.printStackTrace();}}
      
    public boolean done() {
        return testAgent.findB;}}
   
class Proposals extends Behaviour {

    private AucAgent testAgent;
    private String product_name;
    private int product_price;

    public Proposals(AucAgent agent, String product_name, int product_price) {
        super(agent);
        testAgent = agent;
        this.product_name = product_name;
        this.product_price = product_price;
    }
    
    public void action() {

     
        	if (testAgent.findB) {

            ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
            for (int i = 0; i < testAgent.myBidders.length; i++) {
                cfp.addReceiver(testAgent.myBidders[i]);
            } 
            cfp.setContent(this.product_name + "," + this.product_price);
            cfp.setConversationId("First Price");
            cfp.setReplyWith("cfp"+System.currentTimeMillis()); // Unique value
            testAgent.send(cfp);

            // Prepare message template
            testAgent.mt = MessageTemplate.and(MessageTemplate.MatchConversationId("First Price"),
                                             MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
        
            testAgent.ProposalsSent = true;

        }
    }

    public boolean done() {
        return testAgent.ProposalsSent;
    }
}

class Receive_Proposals extends Behaviour {

    private AucAgent testAgent;

    private int Counter = 0; 
    private int refusals=0;
    private String product_name;

    public Receive_Proposals(AucAgent agent, String name) {
        super(agent);
        testAgent = agent;
        product_name=name;
    }
    
    public void action() {

        if (testAgent.ProposalsSent) {

        	 testAgent.myCondition = false; 
            ACLMessage msg = testAgent.receive(testAgent.mt);
            if (msg != null) {
                
                if (msg.getPerformative() == ACLMessage.PROPOSE) {

                    
                    int price = Integer.parseInt(msg.getContent());
                    if ( price > testAgent.won_price) {
                       
                        testAgent.won_price = price;
                        testAgent.wonBidder = msg.getSender();}	}
                    

                  
                    
                

                if (msg.getPerformative() == ACLMessage.REFUSE){
                    System.out.println(msg.getSender().getLocalName() + " is not joining this auction");
                    refusals++;
                }

                Counter++;
                
                
            }
            else {
                block();}
            
            
            if (Counter == testAgent.myBidders.length) {
               
                if (refusals<testAgent.myBidders.length) {
            	ACLMessage order = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
                order.addReceiver(testAgent.wonBidder);
                order.setContent(product_name + "," + testAgent.won_price);
                order.setConversationId("First Price");
                order.setReplyWith("offer "+System.currentTimeMillis());
                System.out.println(product_name+" sold to .... "+ testAgent.wonBidder.getLocalName());
                testAgent.send(order);}
                
                else
                {
                	System.out.println(product_name+" is not sold to anyone...");
                }
                testAgent.removeList(product_name);
                testAgent.findB = false;
                testAgent.ProposalsSent = false;
                             
                testAgent.won_price = 0;
                testAgent.wonBidder = null;
                testAgent.myCondition = true;
            
            
            }}
        
    
    
    }
   
    public boolean done() {
        return testAgent.myCondition;}}
   
