package Agentes;


import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.AMSService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.lang.acl.ACLMessage;

/**
 *
 * @author Erik
 */
public class AgenteRepresa extends Agent {

    private String nomeRepresa = new String();
    private float capacidadeMaxima;
    private float nivelAtual;
    private AID posterior;
    private AID myAID;
    

    public String getNomeRepresa() {
        return nomeRepresa;
    }

    public void setNomeRepresa(String nomeRepresa) {
        this.nomeRepresa = nomeRepresa;
    }


    private AID getPosterior() {
		return posterior;
	}

	private void setPosterior(AID posterior) {
		this.posterior = posterior;
	}

	public float getCapacidadeMaxima() {
        return capacidadeMaxima;
    }

    public void setCapacidadeMaxima(float capacidadeMaxima) {
        this.capacidadeMaxima = capacidadeMaxima;
    }

    public float getNivelAtual() {
        return nivelAtual;
    }

    public void setNivelAtual(float nivelAtual) {
        this.nivelAtual = nivelAtual;
    }

    public void capturaNome(){
    	myAID = this.getAID();
    	if(myAID.equals("Jaguari")){
    	
    	}else if(myAID.equals("Cachoeira")){
    	
    	}else if(myAID.equals("Atibainha")){
    	
    	}else if(myAID.equals("Juqueri")){
    	
    	}else if(myAID.equals("AguasClaras")){
    	
    	}
    }
    
    public void solicitaNivelAgua(){
    	
    	
    	/*
    	AMSAgentDescription[] agentes = null;
		SearchConstraints c = new SearchConstraints();
		c.setMaxResults(new Long(-1));
		
		
		try{
			
			agentes = AMSService.search(myAgent, new AMSAgentDescription(), c);
			
			
			for(int i = 0; i<agentes.length;i++){
				AID agenteID = agentes[i].getName();
				
				if(agenteID.equals("aguasClaras@Sabesp:1099/JADE")){
					setAguasClaras(agenteID);
					
				}
			}
		    		
		    //Vou buscar pelos agentes
		    //a busca retorna um array DFAgente Description
		    //o paramentro this indica o agente que está realizando a busca
		   
		    
		
		    ACLMessage msgEnviada = new ACLMessage(ACLMessage.REQUEST);
	  	    msgEnviada.setOntology("Requisição de água");
    	    msgEnviada.setLanguage("Português");
    	    msgEnviada.setSender();
     	    msgEnviada.setContent();
	 	    msgEnviada.setConversationId("Requisição de água");
	 	    myAgent.send(msgEnviada);
		    	    
		    	    
		}	
	   	catch(FIPAException e){
	   		e.printStackTrace();
	   	}
		
	}*/
    }
    
    protected void setup() {

        System.out.println("O nome da represa é" + getAID().getLocalName());
        System.out.println("A capacidade maxima é" + capacidadeMaxima);
        System.out.println("O nivel atual é" + nivelAtual);

        //comportamento padrao - ciclico - para receber mensagem
        addBehaviour(new CyclicBehaviour(this) {

            @Override
            public void action() {

            	
            	
            	
                ACLMessage msg = myAgent.receive();
                if (msg != null) {
                    //tratamento das mensagens
                    String conteudo = msg.getContent();
                    if (conteudo.equals("distribuirETA")) {
                         //aqui eh o tratametno de quando o agente represa recebe uma mensagem para distribuir agua para uma ETA
                        //se voce tem disponibilidade, precisa mandar agua para a ETA 
                        //enviar uma mensagem para a ETA 
                        //enviar outra mensagem para o distribuir falando que voce conseguiu fazer a transferencia

                        ACLMessage reply = msg.createReply();
                        reply.setContent("OK");
                        myAgent.send(reply);

                        ACLMessage envio = new ACLMessage(ACLMessage.INFORM);
                        envio.addReceiver(getPosterior());
                        envio.setContent("");

                    }

                }
                block();

            }

        }
        );

    }

    protected void takeDown() {
        System.out.println("Agente da represa  " + getAID().getName() + "esta finalizado.");
    }

}
