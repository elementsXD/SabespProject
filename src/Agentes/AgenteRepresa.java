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
    private AID posterior;
    private AID myAID;
    private float aguaEnviar;
    private float NivelRepresaPosterior;
    private AID represaAnterior;
    private float nivelManterEnviandoAgua;
    private float pressaoAgua;
    
    public float getNivelManterEnviandoAgua() {
		return nivelManterEnviandoAgua;
	}

	public void setNivelManterEnviandoAgua(float nivelManterEnviandoAgua) {
		this.nivelManterEnviandoAgua = nivelManterEnviandoAgua;
	}

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
    		setPosterior(null);
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
					setPosterior(agenteID);
					
				}
			}
		    	    
		}	
	   	catch(FIPAException e){
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
    
    
    public void solicitaNivelAgua(){
    	AID posterior = getPosterior();
    	if(posterior != null){
    	
    	//solicitação do nivel da represa
    	ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
		msg.setOntology("Nivel água");
	    msg.setLanguage("Português");
	    msg.setSender(posterior);
 	    msg.setContent(null);
 	    msg.setConversationId("Nivel água");
 	    this.send(msg);
    	} 
    	else{
    		System.out.println("Não tem represa posterior.");
    	}
    }
    

    public void enviaAgua(float aguaEnviar, AID sender){
    	//a partir da foruma de fisica vai se manter enviando água
    	ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setOntology("Nivel água - resposta");
	    msg.setLanguage("Português");
	    msg.setSender(sender);
 	    msg.setContent(String.valueOf(aguaEnviar));
 	    msg.setConversationId("Nivel água");
 	    this.send(msg);
    }
    
    protected void setup() {

        System.out.println("O nome da represa é" + getAID().getLocalName());
        System.out.println("A capacidade maxima é" + capacidadeMaxima);
        float aux = (nivelAtual/capacidadeMaxima) * 100;
        setPorcentagemRepresa(aux);
        System.out.println("O nivel atual é" + getPorcentagemRepresa());
        capturaNome();
        solicitaNivelAgua();
        
        
        ACLMessage msgReceive = this.receive();
		if(msgReceive != null){
			
			String idConversa = msgReceive.getConversationId();
			int tipoMensagem = msgReceive.getPerformative();
			
			
			if(idConversa.equalsIgnoreCase("Nivel água") && tipoMensagem == 16){
				ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
				msg.setOntology("Nivel água - resposta");
			    msg.setLanguage("Português");
			    msg.setSender(msgReceive.getSender());
		 	    msg.setContent(String.valueOf(getPorcentagemRepresa()));
		 	    msg.setConversationId("Nivel água");
		 	    this.send(msg);
			}
			
			if(idConversa.equalsIgnoreCase("Nivel água - resposta") && tipoMensagem == 7){
				float nivelRepresa = Float.parseFloat(msgReceive.getContent());
				if(nivelRepresa < 100 && porcentagemRepresa > 40){
					//como será um fluxo continuo de agua apenas mexendo na pressao, só iremos chamar
					//o metodo de envio que será modificado mais pra frente so passando a pressao e o 
					
					enviaAgua(aguaEnviar,msgReceive.getSender());
				}
			}
			
			if(idConversa.equalsIgnoreCase("Requisição de água") && tipoMensagem == 16){
				float valorDistribuir = Float.parseFloat(msgReceive.getContent());
				nivelAtual -= valorDistribuir;
				if(nivelAtual > 0){
					ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
					msg.setOntology("Nivel água - resposta");
				    msg.setLanguage("Português");
				    msg.setSender(msgReceive.getSender());
			 	    msg.setContent(String.valueOf(valorDistribuir));
			 	    msg.setConversationId("Nivel água");
			 	    this.send(msg);
				}else{
					
				}
			}
			
		}
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

    public float getAguaEnviar() {
		return aguaEnviar;
	}

	public void setAguaEnviar(float aguaEnviar) {
		aguaEnviar = aguaEnviar;
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

}
