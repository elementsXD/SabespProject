package agentes;




import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
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
    
    private String areaDistribuicao = new String();
    private float valorConsumir;
    private float aguaRecebida;
    protected String aguaSolicitada;
    boolean msgRecebida = false;
    private AID ETA;
    
    public String ConsumirAgua(double valorConsumir){
        
        if(valorConsumir == 0){
        	return "Nao recebemos agua";
        }else{
        	setAguaRecebida(0);
        	return "Valor Consumido";
        }
    }

    public float calculoPopulacaoSimular(boolean verificarVariacao,int p2, int ka, int t, int t2, int qtdAnosFut){
    	int populacao = 0;
    	
    	if(verificarVariacao == true){
    		
    		
    		int popAtual = 8100000;
    		
    		
    		FormulasFisicas form = new FormulasFisicas();
    		float var = form.estudoDemografico(p2, ka, t, t2);
    		
    		float populacaoFutura = popAtual + (var * qtdAnosFut); 
    		
    		return populacaoFutura;	
    	}else{
    	
    		populacao = 8100000;
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
		    		
		    		setETA(result[0].getName());
		    		System.out.println(myAgent.getName() + ": Agente de fornecimento de agua com o nome "+ ETA.getName() + " foi encontrado com sucesso.");
		    	
		    	}
		    	catch (FIPAException e){
		    		e.printStackTrace();
		    	}


			}
		});
    	    	
    }
    
    public void solicitaAgua(){

    	addBehaviour(new CyclicBehaviour(this) {

    		@Override
    		public void action() {
    			// TODO Auto-generated method stub

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

    			block(30000);

    		}
    	});
    }
    
    public void trataMensagens(){
    	

    	addBehaviour(new CyclicBehaviour(this) {
			
			@Override
			public void action() {
				// TODO Auto-generated method stub
				
				while(msgRecebida == false){
					//parte do codigo para receber a quantidade de agua do agente ETA
					
					ACLMessage msg = myAgent.receive();
					if(msg != null){
						
						String idConversa = msg.getConversationId();
						String content = msg.getContent();
						
						if(idConversa.equalsIgnoreCase("Entrega de agua")){
							String retorno = ConsumirAgua(Float.parseFloat(content));
							System.out.println(myAgent.getName() +": "+retorno);
							
							msgRecebida = true;
						}
					}
				}
				block();
				
				msgRecebida= false;
				
			}
		});
    	
    }
    
    public float qtdAguaSolicitar(){
    	//passando false como parametro para devolver a qtd atual da populacao
    	float populacao = calculoPopulacaoSimular(false,0,0,0,0,0);
    	float consumoMedio = 165;
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


	public String getAguaSolicitada() {
		return aguaSolicitada;
	}


	public void setAguaSolicitada(String aguaSolicitada) {
		this.aguaSolicitada = aguaSolicitada;
	}


	public boolean isMsgRecebida() {
		return msgRecebida;
	}


	public void setMsgRecebida(boolean msgRecebida) {
		this.msgRecebida = msgRecebida;
	}


	public AID getETA() {
		return ETA;
	}


	public void setETA(AID eTA) {
		ETA = eTA;
	}
}