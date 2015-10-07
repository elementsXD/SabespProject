package agentes;


import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.AMSService;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

/**
 *
 * @author Erik
 */
@SuppressWarnings("serial")
public class AgenteRepresa extends Agent {

	private String nomeRepresa = new String();
	private float capacidadeMaxima;
	private float nivelAtual;
	private float porcentagemRepresa;
	private AID represaPosterior;
	private AID myAID;
	private AID ETA;
	private float aguaEnviar;
	private float NivelRepresaPosterior;
	private float NivelRepresaAnterior;
	private AID represaAnterior;
	private float nivelManterEnviandoAgua;
	private float pressaoAgua;
	private float limiteMaximo;
	private float limiteMinimo;
	private float capMaxRepresaPosterior;
	private float capMaxRepresaAnterior;
	private boolean nivelAtualAlto;
	private boolean nivelAtualBaixo;


	public void verificaNivelRepresa(){
		
		float aux = (nivelAtual/capacidadeMaxima) * 100;
		setPorcentagemRepresa(aux);
		
		if(getPorcentagemRepresa() >= getLimiteMaximo()){
			System.out.println(this.getName() + ": Esta muito cheia e com risco de transbordar com o nivel da agua no valor de " + this.getPorcentagemRepresa() + " por cento de agua da capacidade maxima!" );
			setNivelAtualAlto(true);

		}
		else if(getPorcentagemRepresa() <= getLimiteMinimo()){
			System.out.println(this.getName() + ": Esta abaixo do limite de "+ this.getLimiteMinimo() + " comecando a apresentar riscos!");
			setNivelAtualBaixo(true);
			
		}
		else{
			System.out.println(this.getName() + " : Esta em um nivel seguro de agua no valor de " + this.getPorcentagemRepresa() + " por cento de agua da capacidade maxima.");
			setNivelAtualAlto(false);
			setNivelAtualBaixo(false);
		}
	}

	public void informacoesAgente(){
		//Metodo sera adaptado para ser usado no grapviz
		addBehaviour(new OneShotBehaviour(this) {
			
			@Override
			public void action() {
				// TODO Auto-generated method stub
				System.out.println(myAgent.getName() + ": O nome da represa e " + getAID().getLocalName());
				System.out.println(myAgent.getName() + ": A capacidade maxima e " + getCapacidadeMaxima());
				float aux = (nivelAtual/capacidadeMaxima) * 100;
				setPorcentagemRepresa(aux);
				verificaNivelRepresa();
			}
		});
		
		
	}
	
	
	//testado e esta funcionando.
	public void capturaNome(){

		//Efetua a construcao do da sequencia de represas
		addBehaviour(new OneShotBehaviour() {

			@Override
			public void action() {
				// TODO Auto-generated method stub

				myAID = myAgent.getAID();
				if(myAID.getName().equals("Jaguari@Sabesp:7778/JADE")){
					setRepresaAnterior(null);
					procuraAgentePosterior("Cachoeira");
					setCapacidadeMaxima(750000000);
					setNivelAtual(325000000);
					setLimiteMaximo(90);
					setLimiteMinimo(20);
					setNivelManterEnviandoAgua(10);
					
				}else if(myAID.getName().equals("Cachoeira@Sabesp:7778/JADE")){
					procuraAgentePosterior("Atibainha");
					procuraAgenteAnterior("Jaguari");
					setCapacidadeMaxima(73500000);
					setNivelAtual(317500000);
					setLimiteMaximo(90);
					setLimiteMinimo(20);
					setNivelManterEnviandoAgua(10);
					
				}else if(myAID.getName().equals("Atibainha@Sabesp:7778/JADE")){
					procuraAgentePosterior("PaivaCastro");
					procuraAgenteAnterior("Cachoeira");
					setCapacidadeMaxima(104000000);
					setNivelAtual(52000000);
					setLimiteMaximo(90);
					setLimiteMinimo(20);
					setNivelManterEnviandoAgua(10);
					
				}else if(myAID.getName().equals("PaivaCastro@Sabesp:7778/JADE")){
					procuraAgentePosterior(null);
					procuraAgenteAnterior("Atibainha");
					setCapacidadeMaxima(10800000);
					setNivelAtual(54000000);
					setLimiteMaximo(90);
					setLimiteMinimo(20);
					setNivelManterEnviandoAgua(10);
					
				}
			}
		});


	}

	public void procuraAgentePosterior(String agente){
		AMSAgentDescription[] agentes = null;
		SearchConstraints c = new SearchConstraints();
		c.setMaxResults(new Long(-1));


		try{

			agentes = AMSService.search(this, new AMSAgentDescription(), c);


			for(int i = 0; i<agentes.length;i++){
				AID agenteID = agentes[i].getName();

				if(agenteID.getName().equals(agente+"@Sabesp:7778/JADE")){
					setRepresaPosterior(agenteID);
					System.out.println(this.getName() + ": Encontrei a represa Posterior com o nome " +represaPosterior.getName());
				}
			}

		}	
		catch(FIPAException e){
			e.printStackTrace();
		}

	}

	public void procuraEta(String valorAgua){
		DFAgentDescription template = new DFAgentDescription();

		//cricao do objeto contendo dados do servico desejado
		ServiceDescription sd = new ServiceDescription();
		sd.setType("Fornecimento");
		//adicao do servico na entrada
		template.addServices(sd);
		try{

			//Vou buscar pelos agentes
			//a busca retorna um array DFAgente Description
			//o paramentro this indica o agnete que esta realizando a busca
			DFAgentDescription[] result = DFService.search(this, template);
			
			setETA(result[0].getName());
			

			ACLMessage msgEnviada = new ACLMessage(ACLMessage.REQUEST);
			msgEnviada.setOntology("Requisicao de agua");
			msgEnviada.setLanguage("Portugues");
			msgEnviada.setSender(getETA());
			msgEnviada.setContent(valorAgua);
			msgEnviada.setConversationId("Requisicao de agua");
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

				if(agenteID.getName().equals(agente+"@Sabesp:7778/JADE")){
					setRepresaAnterior(agenteID);
					System.out.println(this.getName() + ": Encontrei a represa Anterior com o nome " +represaAnterior.getName());
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
		msg.addReceiver(sender);
		msg.setContent(conteudo);
		msg.setConversationId(conversationId);
		this.send(msg);
	}
	//metodo de distribuicao de agua 
	public void envioDeAgua(){
		
	};

	public void solicitaNivelAgua(){
		
		addBehaviour(new CyclicBehaviour(this) {
			
			@Override
			public void action() {
				// TODO Auto-generated method stub

				AID represaPosterior = getRepresaPosterior();
				AID represaAnterior = getRepresaAnterior();
				
				
				if(represaPosterior != null){

					//solicitacao do nivel da represa

					enviaMensagem(16,"Nivel agua", "Portugues", represaPosterior, null, "Nivel agua" );	

				}
				/*
				else{
					System.out.println(myAgent.getName() + ": Nao tem represa posterior.");
				}
				*/
				
				if(represaAnterior != null){
					
					enviaMensagem(16,"Nivel agua", "Portugues", represaPosterior, null, "Nivel agua" );	
				}
				/*else{
					System.out.println(myAgent.getName() + ": Nao tem represa anterior.");
				}
				
				*/
				
				block(20000);
				

			}
		});
			}

	//verifica solicitacao de agua feita por outra Represa
	public void verificaSolicitacaoAgua(float valorDistribuir, AID sender){

		float aguaNecessaria =  nivelAtual - valorDistribuir;

		//verifica se o nivel Represa ficaria positivo apos entregar a agua
		if(nivelAtual > getNivelManterEnviandoAgua()){
			System.out.println(this.getName() +": Recebida a solicitacao de agua no valor de "+ valorDistribuir);
			enviaMensagem(7,"Resposta","Portugues", sender,String.valueOf(aguaNecessaria), "Solicitacao de agua - resposta" );

		}else{
			//Caso a quantidade de agua nao seja o bastante a Represa solicita para a anterior agua 
			aguaNecessaria *= -1;
			if(getRepresaAnterior() != null)
				enviaMensagem(16,"Solicitacao","Portugues", getRepresaAnterior(), String.valueOf(aguaNecessaria), "Solicitacao de agua" );

			else{
				enviaMensagem(7,"Resposta","Portugues",sender,String.valueOf(aguaEnviar), "Represa sem agua");

			}
		}

	}

	public void trataMensagens(){
		//Comportamento de tratamento de mensagens, assim a represa toma uma decisao para cada mensagem
		addBehaviour(new CyclicBehaviour(this) {

			@Override
			public void action() {
				// TODO Auto-generated method stub

				ACLMessage msgReceive = myAgent.receive();
				if(msgReceive != null){

					String idConversa = msgReceive.getConversationId();
					int tipoMensagem = msgReceive.getPerformative();


					if(idConversa.equalsIgnoreCase("Nivel agua") && tipoMensagem == 16){
						String resposta = String.valueOf(getNivelAtual())+"#"+ String.valueOf(getCapacidadeMaxima());
						
						enviaMensagem(7, "Nivel agua","Portugues", msgReceive.getSender(), resposta, "Nivel agua - resposta");

					}else if(idConversa.equalsIgnoreCase("Nivel agua - resposta") && tipoMensagem == 7){
						String msg_resposta = msgReceive.getContent();
						String[] nivelAtualRepresa = msg_resposta.split("#");
						String nivelAtualMsg = nivelAtualRepresa[0];
						String capMaxMsg = nivelAtualRepresa[1];
						

						
						//Recebimento do nivel atual e a capacidade maxima da represa a qual foi perguntado esses dados
						
						if(msgReceive.getSender().equals(represaPosterior.getName())){
							setNivelRepresaPosterior(Float.parseFloat(nivelAtualMsg));
							setCapMaxRepresaPosterior(Float.parseFloat(capMaxMsg));
						}else if(msgReceive.getSender().equals(represaAnterior.getName())){
							setNivelRepresaAnterior(Float.parseFloat(nivelAtualMsg));
							setCapMaxRepresaPosterior(Float.parseFloat(capMaxMsg));
						}
						
						float	qtdFaltaEncher = getCapMaxRepresaPosterior() - getNivelRepresaPosterior();
						
						
						
						if(getNivelRepresaPosterior() < 90 && porcentagemRepresa > nivelManterEnviandoAgua){
							//como sera um fluxo continuo de agua apenas mexendo na pressao, so iremos chamar o metodo de acordo com o necessario
							setAguaEnviar(300);
							float aux = getNivelAtual() - getAguaEnviar();
							setNivelAtual(aux);
							enviaMensagem(16,"Entrega de agua","Portugues",msgReceive.getSender(),String.valueOf(aguaEnviar), "Entrega de agua");
							//a partir da foruma de fisica vai se manter enviando agua
							
						}
						else{
							enviaMensagem(7,"Entrega de agua", "Portugues", msgReceive.getSender(),String.valueOf(0),  "Entrega de agua" );
						}
					}else if(idConversa.equalsIgnoreCase("Requisicao de agua") && tipoMensagem == 16){
						float valorDistribuir = Float.parseFloat(msgReceive.getContent());
						float qtdAguaAposReq =  nivelAtual - valorDistribuir;
						System.out.println(myAgent.getName() +"Recebida a solicitacao de agua da ETA no valor de "+ valorDistribuir );
						//verifica se o nivel Represa ficaria positivo apos entregar a agua
						if(nivelAtual > 0){
							nivelAtual -= valorDistribuir;
							enviaMensagem(7,"Requisicao de agua", "Portugues", msgReceive.getSender(),String.valueOf(valorDistribuir), "Requisicao de agua");
						}else{
							//Caso a quantidade de agua nao seja o bastante a Represa solicita para a anterior agua 
							qtdAguaAposReq *= -1;

							enviaMensagem(16,"Solicitacao","Portugues", getRepresaAnterior(), String.valueOf(qtdAguaAposReq), "Solicitacao de agua" );

						}
					}else if(idConversa.equalsIgnoreCase("Entrega de agua")){
						float aux = getNivelAtual() + Float.parseFloat(msgReceive.getContent());
						setNivelAtual(aux);
						System.out.println(myAgent.getName()+": Entrega de agua recebida no valor de "+ msgReceive.getContent());
					}
					else if(idConversa.equalsIgnoreCase("Solicitacao de agua")){
						verificaSolicitacaoAgua(Float.parseFloat(msgReceive.getContent()),msgReceive.getSender());

					}else if(idConversa.equalsIgnoreCase("Solicitacao de agua - resposta")){
						if(represaPosterior != null)
							enviaMensagem(7,"Resposta","Portugues", getRepresaPosterior(), msgReceive.getContent(), "Solicitacao de agua - resposta" );

						else{
							nivelAtual += Float.parseFloat(msgReceive.getContent());
							procuraEta(String.valueOf(nivelAtual));
						}



					}
					else if (idConversa.equalsIgnoreCase("Represa sem agua")){
						if(represaPosterior != null)
							enviaMensagem(7,"Resposta","Portugues", getRepresaPosterior(), String.valueOf(0), "Solicitacao de agua - resposta" );

						else{
							//mensagem de inform
							enviaMensagem(7, "Requisicao de agua", "Portugues", getETA(), String.valueOf(aguaEnviar), "Requisicao de agua");


						}

					}

				}
			}
		});
		//termino do comportamento que verifica mensagens e toma uma decisao sobre o que fazer 

	}
	
	protected void setup() {
		
		setNivelAtualAlto(false);
		setNivelAtualBaixo(false);
		
		capturaNome();

		informacoesAgente();

		solicitaNivelAgua();
		
		trataMensagens();


	}

	protected void takeDown() {
		System.out.println(this.getName() + ": Agente da represa  " + getAID().getName() + "esta finalizado.");
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

	public float getLimiteMaximo() {
		return limiteMaximo;
	}

	public void setLimiteMaximo(float limiteMaximo) {
		this.limiteMaximo = limiteMaximo;
	}

	public float getLimiteMinimo() {
		return limiteMinimo;
	}

	public void setLimiteMinimo(float limiteMinimo) {
		this.limiteMinimo = limiteMinimo;
	}

	public float getNivelRepresaAnterior() {
		return NivelRepresaAnterior;
	}

	public void setNivelRepresaAnterior(float nivelRepresaAnterior) {
		NivelRepresaAnterior = nivelRepresaAnterior;
	}

	public AID getETA() {
		return ETA;
	}

	public void setETA(AID eTA) {
		ETA = eTA;
	}

	public float getCapMaxRepresaPosterior() {
		return capMaxRepresaPosterior;
	}

	public void setCapMaxRepresaPosterior(float capMaxRepresaPosterior) {
		this.capMaxRepresaPosterior = capMaxRepresaPosterior;
	}

	public float getCapMaxRepresaAnterior() {
		return capMaxRepresaAnterior;
	}

	public void setCapMaxRepresaAnterior(float capMaxRepresaAnterior) {
		this.capMaxRepresaAnterior = capMaxRepresaAnterior;
	}

	public boolean isNivelAtualAlto() {
		return nivelAtualAlto;
	}

	public void setNivelAtualAlto(boolean nivelAtualAlto) {
		this.nivelAtualAlto = nivelAtualAlto;
	}

	public boolean isNivelAtualBaixo() {
		return nivelAtualBaixo;
	}

	public void setNivelAtualBaixo(boolean nivelAtualBaixo) {
		this.nivelAtualBaixo = nivelAtualBaixo;
	}

}
