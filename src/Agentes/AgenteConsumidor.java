package Agentes;



import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

/**
 *
 * @author Erik
 */
public class AgenteConsumidor extends Agent{
    
    private String areaDistribuicao = new String();
    private float valorConsumir;
    private float aguaRecebida;
    protected String aguaSolicitada;
    boolean msgRecebida = false;
    
    protected String ConsumirAgua(double valorConsumir){
        
        if(valorConsumir == 0){
        	return "N�o recebemos �gua";
        }else{
        	setAguaRecebida(0);
        	return "Valor Consumido";
        }
    }

    
    protected void setup(){
        
    	System.out.println("Agente Consumidor foi inicializado");	
    
    
    	System.out.println("Iniciando processo de solicita��o de �gua.");
    
    	
    	//Fa
    	
    	DFAgentDescription template = new DFAgentDescription();
    	
    	//cria��o do objeto contendo dados do servi�o desejado
    	ServiceDescription sd = new ServiceDescription();
    	sd.setType("Fornecimento");
    	//adi��o do servi�o na entrada
    	template.addServices(sd);
    	try{
    		
    		//Vou buscar pelos agentes
    		//a busca retorna um array DFAgente Description
    		//o paramentro this indica o agnete que esta realizando a busca
    		DFAgentDescription[] result = DFService.search(this, template);
    		

    	    ACLMessage msgEnviada = new ACLMessage(ACLMessage.REQUEST);
    	    msgEnviada.setOntology("Solicita��o de �gua");
    	    msgEnviada.setLanguage("Portugu�s");
    	    msgEnviada.setSender(result[0].getName());
    	    msgEnviada.setContent(aguaSolicitada);
    	    msgEnviada.setConversationId("Solicita��o de �gua");
    	    this.send(msgEnviada);
    	    
    	    
    	}	
    	catch (FIPAException e){
    		e.printStackTrace();
    	}
    	
    	
    	addBehaviour(new CyclicBehaviour(this) {
			
			@Override
			public void action() {
				// TODO Auto-generated method stub
				
				ACLMessage msg = myAgent.receive();
				if(msg != null){
					
					String idConversa = msg.getConversationId();
					String content = msg.getContent();
					
					if(idConversa.equalsIgnoreCase("Solicita��o de �gua")){
						ConsumirAgua(Float.parseFloat(content));
					}
				}
				
				
			}
		});
    	
    	
    }
    
    
    
    protected void takeDown ( ) {
        System.out.println("Agente de consumo de �gua  " + getAID( ) .getName( ) + "esta finalizado ");
    }

    public String getAreaDistribuicao() {
        return areaDistribuicao;
    }

    public void setAreaDistribuicao(String areaDistribuicao) {
        this.areaDistribuicao = areaDistribuicao;
    }

    public float getValorConsumir() {
        return valorConsumir;
    }

    public void setValorConsumir(float valorConsumir) {
        this.valorConsumir = valorConsumir;
    }


	protected float getAguaRecebida() {
		return aguaRecebida;
	}


	protected void setAguaRecebida(float aguaRecebida) {
		this.aguaRecebida = aguaRecebida;
	}
}