package Agentes;


import jade.domain.AMSService;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.core.AID;
import jade.core.Agent;

public class AgenteETA extends Agent {
	
	
	static final int QTDCONSUMIDORES = 4;
	float consumoConsumidores[] = new float[QTDCONSUMIDORES];
	AID nomeConsumidores[] = new AID[QTDCONSUMIDORES];
	float consumoTotal = 0;
	AID aguasClaras;
	private float qtdAguaDistribuir = 0;
	
	
	public void RegistraServico(){
		
		String tipoServico = "Fornecimento";
		String nomeServico = "Fornecimento de água";
		
		//Criação da entra DF
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID()); //informa o AID do agente
		
		
		//Criação do serviço 
		ServiceDescription sd = new ServiceDescription();
		sd.setType(tipoServico);
		sd.setName(nomeServico);
		//Adição do serviço
		
		dfd.addServices(sd);
		
		//Criação do serviço
		try{
			//register(agente que oferece, descrição)
			DFService.register(this, dfd);
			System.out.println("Serviço registrado com sucesso.");
		}catch (FIPAException e){
			e.printStackTrace();
		}
	}

	public void setup(){
		
		System.out.println("Agente ETA foi inicializado!");
		System.out.println("Registrando serviço de fornecimento de água.");
		RegistraServico();
		
		int contadorMsg = 0;
		
		
		
		while(contadorMsg>QTDCONSUMIDORES){
		
			ACLMessage msg = this.receive();
			if(msg != null){
				
				String idConversa = msg.getConversationId();
				String content = msg.getContent();
				int tipoMensagem = msg.getPerformative();
				
				
				if(idConversa.equalsIgnoreCase("Solicitação de água") && tipoMensagem == 16){
					consumoConsumidores[contadorMsg] = Float.parseFloat(content);
					nomeConsumidores[contadorMsg] = msg.getSender();
					consumoTotal += consumoConsumidores[contadorMsg];
				}
			}
		}
		
		
		AMSAgentDescription[] agentes = null;
		
		SearchConstraints c = new SearchConstraints();
		c.setMaxResults(new Long(-1));
		
		
		
		try{
			
			agentes = AMSService.search(this, new AMSAgentDescription(), c);
			
			
			for(int i = 0; i<agentes.length;i++){
				AID agenteID = agentes[i].getName();
				
				if(agenteID.equals("aguasClaras@Sabesp:1099/JADE")){
					setAguasClaras(agenteID);
					
				}
			}
		    		
		    //Vou buscar pelos agentes
		    //a busca retorna um array DFAgente Description
		    //o paramentro this indica o agnete que esta realizando a busca
		   
		    
		
		    ACLMessage msgEnviada = new ACLMessage(ACLMessage.REQUEST);
	  	    msgEnviada.setOntology("Requisição de água");
    	    msgEnviada.setLanguage("Português");
    	    msgEnviada.setSender(getAguasClaras());
     	    msgEnviada.setContent(String.valueOf(consumoTotal));
	 	    msgEnviada.setConversationId("Requisição de água");
	 	    this.send(msgEnviada);
		    	    
		    	    
		}	
	   	catch(FIPAException e){
	   		e.printStackTrace();
	   	}
		
		
		
		ACLMessage msg = this.receive();
		if(msg != null){
			
			String idConversa = msg.getConversationId();
			String content = msg.getContent();
			
			if(idConversa.equalsIgnoreCase("Requisição de água")){
				setQtdAguaDistribuir(Float.parseFloat(content));
			}
		}
		
		for(int i = 0;i<nomeConsumidores.length;i++){
			if(consumoTotal == qtdAguaDistribuir){
				//efetua distribuição de acordo com o que cada regiao solicitou
			}
			else if(consumoTotal < qtdAguaDistribuir){
				//faz a estrategia de distribuição considerando o valor de agua que recebeu
			}else{
				//distribui a agua de forma de tal forma que os consumidores recebam o necessário
			}
		}
				
	}

	protected void takeDown(){
		try{ DFService.deregister(this);}
		catch(FIPAException e){
			e.printStackTrace();
		}
		
		System.out.println("Agente " + getAID() + "finalizado com sucesso");
	}

	public AID getAguasClaras() {
		return aguasClaras;
	}

	public void setAguasClaras(AID aguasClaras) {
		this.aguasClaras = aguasClaras;
	}

	float getQtdAguaDistribuir() {
		return qtdAguaDistribuir;
	}

	void setQtdAguaDistribuir(float qtdAguaDistribuir) {
		this.qtdAguaDistribuir = qtdAguaDistribuir;
	}
}
