package Agentes;


import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.AMSService;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.proto.states.MsgReceiver;

/**
 *
 * @author Erik
 */
public class AgenteRepresa extends Agent {

    private String nomeRepresa = new String();
    private float capacidadeMaxima;
    private float nivelAtual;
    private float porcentagemRepresa;
    private AID represaPosterior;
    private AID myAID;
    private float aguaEnviar;
    private float NivelRepresaPosterior;
    private AID represaAnterior;
    private float nivelManterEnviandoAgua;
    private float pressaoAgua;
    

	public void capturaNome(){
    	myAID = this.getAID();
    	if(myAID.equals("Jaguari")){
    		procuraAgentePosterior("Jaguari");
    		setRepresaAnterior(null);
    	}else if(myAID.equals("Cachoeira")){
    		procuraAgentePosterior("Atibainha");
    		procuraAgenteAnterior("Jaguari");
    	}else if(myAID.equals("Atibainha")){
    		procuraAgentePosterior("Juqueri");
    		procuraAgenteAnterior("Cachoeira");
    	}else if(myAID.equals("Juqueri")){
    		procuraAgentePosterior("AguasClaras");
    		procuraAgenteAnterior("Atibainha");
    	}else if(myAID.equals("AguasClaras")){
    		setRepresaPosterior(null);
    		procuraAgenteAnterior("Juqueri");    	}
    }
    
    public void procuraAgentePosterior(String agente){
    	AMSAgentDescription[] agentes = null;
		SearchConstraints c = new SearchConstraints();
		c.setMaxResults(new Long(-1));
		
		
		try{
			
			agentes = AMSService.search(this, new AMSAgentDescription(), c);
			
			
			for(int i = 0; i<agentes.length;i++){
				AID agenteID = agentes[i].getName();
				
				if(agenteID.equals(agente+"@Sabesp:1099/JADE")){
					setRepresaPosterior(agenteID);
					
				}
			}
		    	    
		}	
	   	catch(FIPAException e){
	   		e.printStackTrace();
	   	}
		
    }
    
    public void procuraEta(String valorAgua){
DFAgentDescription template = new DFAgentDescription();
    	
    	//criação do objeto contendo dados do serviço desejado
    	ServiceDescription sd = new ServiceDescription();
    	sd.setType("Fornecimento");
    	//adição do serviço na entrada
    	template.addServices(sd);
    	try{
    		
    		//Vou buscar pelos agentes
    		//a busca retorna um array DFAgente Description
    		//o paramentro this indica o agnete que esta realizando a busca
    		DFAgentDescription[] result = DFService.search(this, template);
    		

    	    ACLMessage msgEnviada = new ACLMessage(ACLMessage.REQUEST);
    	    msgEnviada.setOntology("Solicitação de água");
    	    msgEnviada.setLanguage("Português");
    	    msgEnviada.setSender(result[0].getName());
    	    msgEnviada.setContent(valorAgua);
    	    msgEnviada.setConversationId("Solicitação de água");
    	    this.send(msgEnviada);
    	    
    	    
    	}	
    	catch (FIPAException e){
    		e.printStackTrace();
    	}
    }
    
    public void procuraAgenteAnterior(String agente){
    	AMSAgentDescription[] agentes = null;
		SearchConstraints c = new SearchConstraints();
		c.setMaxResults(new Long(-1));
		
		
		try{
			
			agentes = AMSService.search(this, new AMSAgentDescription(), c);
			
			
			for(int i = 0; i<agentes.length;i++){
				AID agenteID = agentes[i].getName();
				
				if(agenteID.equals(agente+"@Sabesp:1099/JADE")){
					setRepresaAnterior(agenteID);
					
				}
			}
		    	    
		}	
	   	catch(FIPAException e){
	   		e.printStackTrace();
	   	}
		
    }
    
    public void enviaMensagem(int performative,String ontologia, String linguagem, AID sender,String conteudo, String conversationId){
    	
    	@SuppressWarnings("deprecation")
		ACLMessage msg = new ACLMessage();
    	msg.setPerformative(performative);
		msg.setOntology(ontologia);
	    msg.setLanguage(linguagem);
	    msg.setSender(sender);
 	    msg.setContent(conteudo);
 	    msg.setConversationId(conversationId);
 	    this.send(msg);
    }
    
    public void solicitaNivelAgua(){
    	AID represaPosterior = getRepresaPosterior();
    	if(represaPosterior != null){
    	
    	//solicitação do nivel da represa
    	ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
		msg.setOntology("Nivel água");
	    msg.setLanguage("Português");
	    msg.setSender(represaPosterior);
 	    msg.setContent(null);
 	    msg.setConversationId("Nivel água");
 	    this.send(msg);
    	} 
    	else{
    		System.out.println("Não tem represa posterior.");
    	}
    }
    
    public void solicitaAgua(float aguaNecessaria, AID represa){
    	
			ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
			msg.setOntology("Solicitação");
		    msg.setLanguage("Português");
		    msg.setSender(represa);
	 	    msg.setContent(String.valueOf(aguaNecessaria));
	 	    msg.setConversationId("Solicitação de água");
	 	    this.send(msg);
    }
    
    public void enviaAgua(float aguaEnviar, AID sender){

    	//a partir da foruma de fisica vai se manter enviando água
    	ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setOntology("Entrega de água");
	    msg.setLanguage("Português");
	    msg.setSender(sender);
 	    msg.setContent(String.valueOf(aguaEnviar));
 	    msg.setConversationId("Entrega de água");
 	    this.send(msg);
    }
    
    public void RespostaSolicitação(float aguaEnviar, AID sender){
    	ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setOntology("Resposta");
	    msg.setLanguage("Português");
	    msg.setSender(sender);
 	    msg.setContent(String.valueOf(aguaEnviar));
 	    msg.setConversationId("Solicitação de agua - resposta");
 	    this.send(msg);
    }
    

    public void verificaSolicitacaoAgua(float valorDistribuir, AID sender){

		float aguaNecessaria =  nivelAtual - valorDistribuir;
		
		//verifica se o nivel Represa ficaria positivo após entregar a agua
		if(nivelAtual > 0){
			RespostaSolicitação(aguaNecessaria, sender);
		}else{
		//Caso a quantidade de agua não seja o bastante a Represa solicita para a anterior água 
			aguaNecessaria *= -1;
			if(getRepresaAnterior() != null)
			solicitaAgua(aguaNecessaria, getRepresaAnterior());
			else{
				ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
				msg.setOntology("Resposta");
			    msg.setLanguage("Português");
			    msg.setSender(sender);
		 	    msg.setContent(String.valueOf(aguaEnviar));
		 	    msg.setConversationId("Represa sem água");
		 	    this.send(msg);	
			}
		}
    	
    }
    
    protected void setup() {

        System.out.println("O nome da represa é" + getAID().getLocalName());
        System.out.println("A capacidade maxima é" + capacidadeMaxima);
        float aux = (nivelAtual/capacidadeMaxima) * 100;
        setPorcentagemRepresa(aux);
        System.out.println("O nivel atual é" + getPorcentagemRepresa());
        capturaNome();
        solicitaNivelAgua();
        
        //Comportamento de tratamento de mensagens, assim a represa toma uma decisão para cada mensagem
        addBehaviour(new CyclicBehaviour(this) {
			
			@Override
			public void action() {
				// TODO Auto-generated method stub
				
				ACLMessage msgReceive = myAgent.receive();
				if(msgReceive != null){
					
					String idConversa = msgReceive.getConversationId();
					int tipoMensagem = msgReceive.getPerformative();
					
					
					if(idConversa.equalsIgnoreCase("Nivel água") && tipoMensagem == 16){
						
						enviaMensagem(16, "Nivel água - resposta","Português", msgReceive.getSender(), String.valueOf(getPorcentagemRepresa()), "Nivel água - resposta");
						
					}else if(idConversa.equalsIgnoreCase("Nivel água - resposta") && tipoMensagem == 7){
						
						float nivelRepresa = Float.parseFloat(msgReceive.getContent());
						if(nivelRepresa < 100 && porcentagemRepresa > nivelManterEnviandoAgua){
							//como será um fluxo continuo de agua apenas mexendo na pressao, só iremos chamar
							//o metodo de envio que será modificado mais pra frente so passando a pressao e o 
							
							enviaAgua(aguaEnviar,msgReceive.getSender());
						}
						else{
							
						}
					}else if(idConversa.equalsIgnoreCase("Requisição de água") && tipoMensagem == 16){
						float valorDistribuir = Float.parseFloat(msgReceive.getContent());
						float aguaNecessaria =  nivelAtual - valorDistribuir;
						
						//verifica se o nivel Represa ficaria positivo após entregar a agua
						if(nivelAtual > 0){
							enviaAgua(aguaNecessaria, msgReceive.getSender());
						}else{
						//Caso a quantidade de agua não seja o bastante a Represa solicita para a anterior água 
							aguaNecessaria *= -1;
							
							solicitaAgua(aguaNecessaria, getRepresaAnterior());
							
						}
					}else if(idConversa.equalsIgnoreCase("Solicitação de água")){
						verificaSolicitacaoAgua(Float.parseFloat(msgReceive.getContent()),msgReceive.getSender());
						
					}else if(idConversa.equalsIgnoreCase("Solicitação de água - resposta")){
						if(represaPosterior != null)
						RespostaSolicitação(Float.parseFloat(msgReceive.getContent()), getRepresaPosterior());
						else{
							nivelAtual += Float.parseFloat(msgReceive.getContent());
							procuraEta(String.valueOf(nivelAtual));
						}
							
							
						
					}
					else if (idConversa.equalsIgnoreCase("Represa sem água")){
						if(represaPosterior != null)
						RespostaSolicitação(0,getRepresaPosterior());
						else{
							//mensagem de inform
							enviaMensagem(16, "Resposta", "Português", msgReceive.getSender(), String.valueOf(aguaEnviar), "Represa sem água");
							
							
						}
							
					}
					
				}
			}
		});
        //termino do comportamento que verifica mensagens e toma uma decisão sobre o que fazer 
    }

    protected void takeDown() {
        System.out.println("Agente da represa  " + getAID().getName() + "esta finalizado.");
    }
    
    public String getNomeRepresa() {
        return nomeRepresa;
    }

    public void setNomeRepresa(String nomeRepresa) {
        this.nomeRepresa = nomeRepresa;
    }


    private AID getRepresaPosterior() {
		return represaPosterior;
	}

	private void setRepresaPosterior(AID represaPosterior) {
		this.represaPosterior = represaPosterior;
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

    public float getAguaEnviar() {
		return aguaEnviar;
	}

	public void setAguaEnviar(float aguaEnviar) {
		this.aguaEnviar = aguaEnviar;
	}

	public void setNivelAtual(float nivelAtual) {
        this.nivelAtual = nivelAtual;
    }

	public float getPorcentagemRepresa() {
		return porcentagemRepresa;
	}

	public void setPorcentagemRepresa(float porcentagemRepresa) {
		this.porcentagemRepresa = porcentagemRepresa;
	}

	public float getNivelRepresaPosterior() {
		return NivelRepresaPosterior;
	}

	public void setNivelRepresaPosterior(float nivelRepresaPosterior) {
		NivelRepresaPosterior = nivelRepresaPosterior;
	}

	public float getPressaoAgua() {
		return pressaoAgua;
	}

	public void setPressaoAgua(float pressaoAgua) {
		this.pressaoAgua = pressaoAgua;
	}

	public AID getRepresaAnterior() {
		return represaAnterior;
	}

	public void setRepresaAnterior(AID represaAnterior) {
		this.represaAnterior = represaAnterior;
	}

    public float getNivelManterEnviandoAgua() {
		return nivelManterEnviandoAgua;
	}

	public void setNivelManterEnviandoAgua(float nivelManterEnviandoAgua) {
		this.nivelManterEnviandoAgua = nivelManterEnviandoAgua;
	}

}
