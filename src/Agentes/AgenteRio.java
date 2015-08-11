package Agentes;


import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.domain.AMSService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.lang.acl.ACLMessage;

public class AgenteRio extends Agent{

	AID atatibainha;
	AID cachoeira;
	AID jaguari;
	float fluxoAguaAtatibainha;
	float fluxoAguaCachoeira;
	float fluxoAguaJaguari;
	
	protected void setup(){
		
		
		System.out.println("Agente que representa os rios foi inicializado!");
		System.out.println("Iniciando o fornecimento de agua para as Represas Jaguari, Cachoeira e Atatibainha.");
		
		ParallelBehaviour pb = new ParallelBehaviour(this, ParallelBehaviour.WHEN_ALL) {
		
			public int onEnd(){
				System.out.println("Rio acabou de forncer a quantidade diário de água.");
				return 0;
			}
		};
		
		addBehaviour(pb);
		pb.addSubBehaviour(new SimpleBehaviour(this) {
			int qtd=0;
			
			
			@Override
			public boolean done() {
				// TODO Auto-generated method stub
				if(qtd==1){
					return true;
				}
				else
				return false;
			}
			
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
						
						if(agenteID.equals("RepresaAtatibainha@Sabesp:1099/JADE")){
							setAtatibainha(agenteID);
						}
					}
				    		
				    //Vou buscar pelos agentes
				    //a busca retorna um array DFAgente Description
				    //o paramentro this indica o agente que está realizando a busca
				   
				    
				
				    ACLMessage msgEnviada = new ACLMessage(ACLMessage.INFORM);
			  	    msgEnviada.setOntology("Fornecimento rio");
		    	    msgEnviada.setLanguage("Português");
		    	    msgEnviada.setSender(getAtatibainha());
		     	    msgEnviada.setContent(String.valueOf(getFluxoAguaAtatibainha()));
			 	    msgEnviada.setConversationId("Fornecimento rio");
			 	    myAgent.send(msgEnviada);
				    	    
				    	    
				}	
			   	catch(FIPAException e){
			   		e.printStackTrace();
			   	}
				
				qtd +=1;
			}
		});
		
		pb.addSubBehaviour(new SimpleBehaviour(this) {
			
			int qtd = 0;
			
			@Override
			public boolean done() {
				// TODO Auto-generated method stub
				if(qtd==1){
					return true;
				}
				else
				return false;
			}
			
			@Override
			public void action() {
				// TODO Auto-generated method stub
				
				int qtd = 0;
				
				AMSAgentDescription[] agentes = null;
				SearchConstraints c = new SearchConstraints();
				c.setMaxResults(new Long(-1));
				
				try{
					
					agentes = AMSService.search(myAgent, new AMSAgentDescription(), c);
					
					
					for(int i = 0; i<agentes.length;i++){
						AID agenteID = agentes[i].getName();
						
						if(agenteID.equals("RepresaCachoeira@Sabesp:1099/JADE")){
							setCachoeira(agenteID);
						}
					}
				    		
				    //Vou buscar pelos agentes
				    //a busca retorna um array DFAgente Description
				    //o paramentro this indica o agente que está realizando a busca
				   
				    
				
				    ACLMessage msgEnviada = new ACLMessage(ACLMessage.INFORM);
			  	    msgEnviada.setOntology("Fornecimento rio");
		    	    msgEnviada.setLanguage("Português");
		    	    msgEnviada.setSender(getCachoeira());
		     	    msgEnviada.setContent(String.valueOf(getFluxoAguaCachoeira()));
			 	    msgEnviada.setConversationId("Fornecimento rio");
			 	    myAgent.send(msgEnviada);
				    	    
				    	    
				}	
			   	catch(FIPAException e){
			   		e.printStackTrace();
			   	}
				
				qtd +=1;
			}
		});
		
		pb.addSubBehaviour(new SimpleBehaviour(this) {
			
			int qtd = 0;
			
			@Override
			public boolean done() {
				// TODO Auto-generated method stub
				if(qtd==1){
					return true;
				}
				else
				return false;
			}
			
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
						
						if(agenteID.equals("RepresaJaguari@Sabesp:1099/JADE")){
							setJaguari(agenteID);
						}
					}
				    		
				    //Vou buscar pelos agentes
				    //a busca retorna um array DFAgente Description
				    //o paramentro this indica o agente que está realizando a busca
				   
				    
				
				    ACLMessage msgEnviada = new ACLMessage(ACLMessage.INFORM);
			  	    msgEnviada.setOntology("Fornecimento rio");
		    	    msgEnviada.setLanguage("Português");
		    	    msgEnviada.setSender(getJaguari());
		     	    msgEnviada.setContent(String.valueOf(getFluxoAguaJaguari()));
			 	    msgEnviada.setConversationId("Fornecimento rio");
			 	    myAgent.send(msgEnviada);
				    	    
				    	    
				}	
			   	catch(FIPAException e){
			   		e.printStackTrace();
			   	}
				
				qtd+=1;
			}
		});
		
		
		
		/*
		addBehaviour(new CyclicBehaviour(this) {
			
			@Override
			public void action() {
				// TODO Auto-generated method stub
				
				
			}
		});
		
		addBehaviour(new CyclicBehaviour(this) {
			
			@Override
			public void action() {
				// TODO Auto-generated method stub
				
				
			}
		});
		
		addBehaviour(new CyclicBehaviour(this) {
			
			@Override
			public void action() {
				// TODO Auto-generated method stub
				
				
				
			}
		});
	
	*/
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
