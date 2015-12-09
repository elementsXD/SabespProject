package agentes;


import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.domain.AMSService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.lang.acl.ACLMessage;

@SuppressWarnings("serial")
public class AgenteRio extends Agent{

	AID atatibainha;
	AID cachoeira;
	AID jaguari;
	float fluxoAguaAtatibainha;
	float fluxoAguaCachoeira;
	float fluxoAguaJaguari;
	
	public void procuraRepresas(){
		
		addBehaviour(new OneShotBehaviour(this) {
			
			@Override
			public void action() {
				// TODO Auto-generated method stub
				
				AMSAgentDescription[] agentes = null;
				SearchConstraints c = new SearchConstraints();
				c.setMaxResults(new Long(-1));
				
				try{
					
					agentes = AMSService.search(myAgent, new AMSAgentDescription(), c);
					
					
					for(int i = 0; i<agentes.length;i++){
						AID agenteID = agentes[i].getName();
						
						if(agenteID.equals("Atatibainha@Sabesp:1099/JADE")){
							setAtatibainha(agenteID);
						}else if(agenteID.equals("Cachoeira@Sabesp:1099/JADE")){
							setCachoeira(agenteID);
						}else if(agenteID.equals("Jaguari@Sabesp:1099/JADE")){
							setJaguari(agenteID);
						}
						
						
					}
				    	    
				}	
			   	catch(FIPAException e){
			   		e.printStackTrace();
			   	}
				
			}
		});
		
	}
	
	protected void setup(){
		
		
		System.out.println("Agente que representa os rios foi inicializado!");
		
		procuraRepresas();
		
		System.out.println("Iniciando o fornecimento de agua para as Represas Jaguari, Cachoeira e Atatibainha.");
		
		
		
		//Fornecimento de agua para o Atibainha
		addBehaviour(new CyclicBehaviour(this) {
			
			@Override
			public void action() {
				// TODO Auto-generated method stub
				
				    ACLMessage msgEnviada = new ACLMessage(ACLMessage.INFORM);
			  	    msgEnviada.setOntology("Fornecimento rio");
		    	    msgEnviada.setLanguage("Portugues");
		    	    msgEnviada.setSender(getAtatibainha());
		     	    msgEnviada.setContent(String.valueOf(getFluxoAguaAtatibainha()));
			 	    msgEnviada.setConversationId("Fornecimento rio");
			 	    myAgent.send(msgEnviada);
				
			}
		});
		
		//Fornecimento de agua para o Cachoeirinha
		addBehaviour(new CyclicBehaviour(this) {
			
			@Override
			public void action() {
				// TODO Auto-generated method stub
				
				    ACLMessage msgEnviada = new ACLMessage(ACLMessage.INFORM);
			  	    msgEnviada.setOntology("Fornecimento rio");
		    	    msgEnviada.setLanguage("Portugues");
		    	    msgEnviada.setSender(getCachoeira());
		     	    msgEnviada.setContent(String.valueOf(getFluxoAguaCachoeira()));
			 	    msgEnviada.setConversationId("Fornecimento rio");
			 	    myAgent.send(msgEnviada);
				    	    
				
			}
		});
		
		//Fornecimento de agua para Jaguari
		addBehaviour(new CyclicBehaviour(this) {
			
			@Override
			public void action() {
				// TODO Auto-generated method stub
				

				    ACLMessage msgEnviada = new ACLMessage(ACLMessage.INFORM);
			  	    msgEnviada.setOntology("Fornecimento rio");
		    	    msgEnviada.setLanguage("Português");
		    	    msgEnviada.setSender(getJaguari());
		     	    msgEnviada.setContent(String.valueOf(getFluxoAguaJaguari()));
			 	    msgEnviada.setConversationId("Fornecimento rio");
			 	    myAgent.send(msgEnviada);
				
			}
		});
	
	
	}
		
	public AID getAtatibainha() {
		return atatibainha;
	}

	public void setAtatibainha(AID atatibainha) {
		this.atatibainha = atatibainha;
	}

	public AID getCachoeira() {
		return cachoeira;
	}

	public void setCachoeira(AID cachoeira) {
		this.cachoeira = cachoeira;
	}

	public AID getJaguari() {
		return jaguari;
	}

	public void setJaguari(AID jaguari) {
		this.jaguari = jaguari;
	}

	public float getFluxoAguaAtatibainha() {
		return fluxoAguaAtatibainha;
	}

	public void setFluxoAguaAtatibainha(float fluxoAgua) {
		this.fluxoAguaAtatibainha = fluxoAgua;
	}

	public float getFluxoAguaCachoeira() {
		return fluxoAguaCachoeira;
	}

	public void setFluxoAguaCachoeira(float fluxoAguaCachoeira) {
		this.fluxoAguaCachoeira = fluxoAguaCachoeira;
	}

	public float getFluxoAguaJaguari() {
		return fluxoAguaJaguari;
	}

	public void setFluxoAguaJaguari(float fluxoAguaJaguari) {
		this.fluxoAguaJaguari = fluxoAguaJaguari;
	}
}
