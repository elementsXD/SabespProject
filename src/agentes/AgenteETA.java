package agentes;


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
import jade.core.behaviours.CyclicBehaviour;

@SuppressWarnings("serial")
public class AgenteETA extends Agent {
	
	
	static final int QTDCONSUMIDORES = 4;
	float consumoConsumidores[] = new float[QTDCONSUMIDORES];
	AID nomeConsumidores[] = new AID[QTDCONSUMIDORES];
	float consumoTotal = 0;
	AID aguasClaras;
	AID tomadorDecisoes;
	float porcentagemRegiao[] = new float[QTDCONSUMIDORES];
	private float qtdAguaDistribuir = 0;
	
	public void distribuiAgua(AID destino, float aguaEnviar){
		ACLMessage msg = new ACLMessage(ACLMessage.CONFIRM);
		msg.setOntology("Entrega de agua");
	    msg.setLanguage("Português");
	    msg.setSender(destino);
 	    msg.setContent(String.valueOf(aguaEnviar));
 	    msg.setConversationId("Entrega de agua");
 	    this.send(msg);
 	    
	}
	
	
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
		//inicialização do agente 
		System.out.println("Agente ETA foi inicializado!");
		System.out.println("Registrando serviço de fornecimento de água.");
		//registro do serviço de fornecimento de agua 
		RegistraServico();
		
		//comportamento para receber a quantidade de gasto de agua dos consumidores 
		addBehaviour(new CyclicBehaviour(this) {
			//comportamento de recepção dos valores de consumo
			@Override
			public void action() {
				// TODO Auto-generated method stub
				
				int contadorMsg = 0;
				
				
				while(contadorMsg>QTDCONSUMIDORES){
				
					ACLMessage msg = myAgent.receive();
					if(msg != null){
						
						String idConversa = msg.getConversationId();
						String content = msg.getContent();
						int tipoMensagem = msg.getPerformative();
						
						
						if(idConversa.equalsIgnoreCase("Solicitação de água") && tipoMensagem == 16){
							consumoConsumidores[contadorMsg] = Float.parseFloat(content);
							nomeConsumidores[contadorMsg] = msg.getSender();
							consumoTotal += consumoConsumidores[contadorMsg];
							contadorMsg ++;
						}
					}
				}
				
				
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
		    	    msgEnviada.setSender(getAguasClaras());
		     	    msgEnviada.setContent(String.valueOf(consumoTotal));
			 	    msgEnviada.setConversationId("Requisição de água");
			 	    myAgent.send(msgEnviada);
				    	    
				    	    
				}	
			   	catch(FIPAException e){
			   		e.printStackTrace();
			   	}
				
			}
		});
		
		
		//evento para passar a quantidade de agua para o consumidor
		addBehaviour(new CyclicBehaviour(this) {
			//
			@Override
			public void action() {
				// TODO Auto-generated method stub
				ACLMessage msg = myAgent.receive();
				if(msg != null){
					
					String idConversa = msg.getConversationId();
					String content = msg.getContent();
					
					if(idConversa.equalsIgnoreCase("Requisição de água")){
						setQtdAguaDistribuir(Float.parseFloat(content));
					}
				}
				
				for(int i = 0;i<nomeConsumidores.length;i++){
					if(consumoTotal == qtdAguaDistribuir){
						
						for(i = 0; i>QTDCONSUMIDORES;i++){
							qtdAguaDistribuir -= consumoConsumidores[i];
							distribuiAgua(nomeConsumidores[i],consumoConsumidores[i]);
						}
						
					}
					else if(consumoTotal > qtdAguaDistribuir){
						//faz a estrategia de distribuição considerando o valor de agua que recebeu
						for (i = 0;i<QTDCONSUMIDORES;i++){
							porcentagemRegiao[i] = consumoConsumidores[i] / qtdAguaDistribuir;
							float aguaEnviar = qtdAguaDistribuir * porcentagemRegiao[i];
							distribuiAgua(nomeConsumidores[i], aguaEnviar);
							
						}
						
					}else if(consumoTotal < qtdAguaDistribuir){
						//distribui a agua de forma de tal forma que os consumidores recebam o necessário
						
						float excedente = consumoTotal - qtdAguaDistribuir;
						float valorEnvio = 0;
						for(i=0; i<QTDCONSUMIDORES;i++){
							porcentagemRegiao[i] = consumoConsumidores[i] / qtdAguaDistribuir;
							valorEnvio = consumoConsumidores[i] + (porcentagemRegiao[i] * excedente);
							distribuiAgua(nomeConsumidores[i],valorEnvio);
						}
						
					}else{
						for (i = 0;i<QTDCONSUMIDORES;i++){
							distribuiAgua(nomeConsumidores[i],0);
						}
					}
				}
						
				
			}
		});
		
		
		//comportamento para informar o gasto para o tomador de decisões
		addBehaviour(new CyclicBehaviour(this) {
			
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
						
						if(agenteID.equals("TomadorDecisoes@Sabesp:1099/JADE")){
							setTomadorDecisoes(agenteID);
							
						}
					}
				    		
				    //Vou buscar pelos agentes
				    //a busca retorna um array DFAgente Description
				    //o paramentro this indica o agente que está realizando a busca
				   
				    
				
				    ACLMessage msgEnviada = new ACLMessage(ACLMessage.REQUEST);
			  	    msgEnviada.setOntology("Gasto diário");
		    	    msgEnviada.setLanguage("Português");
		    	    msgEnviada.setSender(getTomadorDecisoes());
		     	    msgEnviada.setContent(String.valueOf(consumoTotal));
			 	    msgEnviada.setConversationId("Gasto diário");
			 	    myAgent.send(msgEnviada);
				    	    
				    	    
				}	
			   	catch(FIPAException e){
			   		e.printStackTrace();
			   	}
				
			}
		});
		
		
				
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


	public AID getTomadorDecisoes() {
		return tomadorDecisoes;
	}


	public void setTomadorDecisoes(AID tomadorDecisoes) {
		this.tomadorDecisoes = tomadorDecisoes;
	}
}
