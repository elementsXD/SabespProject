package Agentes;

import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.core.AID;
import jade.core.Agent;

public class AgenteETA extends Agent {

	
	public void RegistraServico(){
		
		String tipoServico = "Fornecimento";
		String nomeServico = "Fornecimento de �gua";
		
		//Cria��o da entra DF
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID()); //informa o AID do agente
		
		
		//Cria��o do servi�o 
		ServiceDescription sd = new ServiceDescription();
		sd.setType(tipoServico);
		sd.setName(nomeServico);
		//Adi��o do servi�o
		
		dfd.addServices(sd);
		
		//Cria��o do servi�o
		try{
			//register(agente que oferece, descri��o)
			DFService.register(this, dfd);
			System.out.println("Servi�o registrado com sucesso.");
		}catch (FIPAException e){
			e.printStackTrace();
		}
	}

	public void setup(){
		
		System.out.println("Agente ETA foi inicializado!");
		RegistraServico();
		
		
	}

	protected void takeDown(){
		try{ DFService.deregister(this);}
		catch(FIPAException e){
			e.printStackTrace();
		}
		
		System.out.println("Agente " + getAID() + "finalizado com sucesso");
	}
}
