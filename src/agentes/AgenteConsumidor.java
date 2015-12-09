package agentes;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import formulasProjeto.*;

/**
 *
 * @author Erik
 */
@SuppressWarnings("serial")
public class AgenteConsumidor extends Agent{
    
    private float valorConsumir;
    private float aguaRecebida;
    protected String aguaSolicitada;
    boolean msgRecebida = false;
    private AID ETA;
    long tempSolicitacao = 10000;
    final static int qtdDiasExecutar = 1000;
    final static float consumoPorCabeca = (float)0.31;
    
    public float calculoPopulacaoSimular(boolean verificarVariacao,int p2, int ka, int t, int t2, int qtdAnosFut){
    	int populacao = 0;
    	
    	if(verificarVariacao == true){
    		
    		
    		int popAtual = 8800000;
    		
    		
    		FormulasFisicas form = new FormulasFisicas();
    		float var = form.estudoDemografico(p2, ka, t, t2);
    		
    		float populacaoFutura = popAtual + (var * qtdAnosFut); 
    		
    		return populacaoFutura;	
    	}else{
    	
    		populacao = 8800000;
    		return populacao;
    	}
    }
    
    public void procuraFornecedor(){
    	
    	addBehaviour(new OneShotBehaviour(this) {
			
			@Override
			public void action() {
				// TODO Auto-generated method stub

		    	System.out.println(myAgent.getName() +": Iniciando busca por fornecedor!");
		    	DFAgentDescription template = new DFAgentDescription();
		    	
		    	//criacao do objeto contendo dados do servico desejado
		    	ServiceDescription sd = new ServiceDescription();
		    	sd.setType("Fornecimento");
		    	sd.setName("Fornecimento de agua");
		    	//adicao do servico na entrada
		    	template.addServices(sd);
		    	try{
		    		
		    		//Vou buscar pelos agentes
		    		//a busca retorna um array DFAgente Description
		    		//o paramentro this indica o agnete que esta realizando a busca
		    		DFAgentDescription[] result = DFService.search(myAgent, template);
		    		
		    		while(result[0].getName() == null){
		    			result = DFService.search(myAgent, template);
		    			
		    		}
		    		if(result[0].getName() != null){
		    			setETA(result[0].getName());	
		    			System.out.println(myAgent.getName() + ": Agente de fornecimento de agua com o nome "+ ETA.getName() + " foi encontrado com sucesso.");
		    		}
		    		
		    		System.out.println(myAgent.getName() + ": Agente de fornecimento de agua com o nome "+ ETA.getName() + " foi encontrado com sucesso.");
		    	
		    	}
		    	catch (FIPAException e){
		    		e.printStackTrace();
		    	}


			}
		});
    	    	
    }
    
    public void solicitaAgua(){

    	addBehaviour(new TickerBehaviour(this, tempSolicitacao) {
			
			@Override
			protected void onTick() {
				// TODO Auto-generated method stub
				
				if (getTickCount() > qtdDiasExecutar){
					
					stop();
				}else{
					
					setAguaSolicitada(String.valueOf(qtdAguaSolicitar()));

	    			System.out.println(myAgent.getName() + ": Iniciando processo de solicitacao de agua.");

	    			ACLMessage msgEnviada = new ACLMessage(ACLMessage.REQUEST);
	    			msgEnviada.setOntology("Solicitacao de agua");
	    			msgEnviada.setLanguage("Portugues");
	    			msgEnviada.addReceiver(getETA());
	    			msgEnviada.setContent(aguaSolicitada);
	    			msgEnviada.setConversationId("Solicitacao de agua");
	    			send(msgEnviada);

	    			System.out.println(myAgent.getName() + ": Agua no valor de "+ getAguaSolicitada() + " foi solicitada com sucesso.");
					

				}
				

			}
		});
    	
    	
    }
    
    public void trataMensagens(){
    	

    	addBehaviour(new CyclicBehaviour(this) {
			
			@Override
			public void action() {
				
				
					ACLMessage msg = myAgent.receive();
					if(msg != null){
						
						String idConversa = msg.getConversationId();
						float conteudo = Float.parseFloat(msg.getContent());
						
						if(idConversa.equalsIgnoreCase("Entrega de agua")){

							if(Float.parseFloat(getAguaSolicitada()) <= conteudo){
								System.out.println(myAgent.getName() + ": Agua recebida com sucesso!");
								
							}else
								System.out.println(myAgent.getName() +": Recebi uma quantidade menor de agua do que foi solicitado!");
					
						}
					}else
						block();
					
				
			}
		});
    	
    }
    
    public float qtdAguaSolicitar(){
    	//passando false como parametro para devolver a qtd atual da populacao
    
    	float populacao = calculoPopulacaoSimular(false,0,0,0,0,0);
    	float consumoMedio = consumoPorCabeca;
    	float qtdAguaSolicitar = populacao * consumoMedio;
    	
    	return qtdAguaSolicitar;
    	
    	
    }
    
    protected void setup(){
        
    	System.out.println(this.getName()+" foi inicializado");
   
    	
    	
    	procuraFornecedor();
    	
    	solicitaAgua();
    	
    	trataMensagens();
    	
    }
    
    
    protected void takeDown ( ) {
        System.out.println("Agente de consumo de agua  " + getAID( ) .getName( ) + "esta finalizado ");
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


	public String getAguaSolicitada() {
		return aguaSolicitada;
	}


	public void setAguaSolicitada(String aguaSolicitada) {
		this.aguaSolicitada = aguaSolicitada;
	}


	public AID getETA() {
		return ETA;
	}


	public void setETA(AID eTA) {
		ETA = eTA;
	}
}