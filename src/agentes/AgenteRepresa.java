package agentes;


import graficos.GraficoNivelRepresa;
import graficos.GraficoNivelSistema;

import java.awt.Dimension;
import java.awt.Point;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
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
	private float porcentagemRepresaAnterior;
	private AID represaPosterior;
	private AID myAID;
	private AID ETA;
	private float NivelRepresaPosterior;
	private AID represaAnterior;
	private float nivelManterEnviandoAgua;
	private float nvlMterEnviandoAguaRepPost;
	private float pressaoAgua;
	private float limiteMaximo;
	private float capMaxRepresaPosterior;
	private float valorRequisicaoETA;
	private float limiteDiario;
	private float solicitacaoAgua;
	static final int segundosDia = 86400;
	static final long tempSolicitacao = 10000;
	static final int qtdDiasExecutar = 1000;
	static final float qtdPerdaDeAgua = (float)0.30;
	static final float qtdContratosMedia = (float)26082.6;
	private GraficoNivelRepresa graph_jaguari = new GraficoNivelRepresa("Jaguari");
	private GraficoNivelRepresa graph_cachoeira = new GraficoNivelRepresa("Cachoeira");
	private GraficoNivelRepresa graph_atibainha = new GraficoNivelRepresa("Atibainha");
	private GraficoNivelRepresa graph_paivac = new GraficoNivelRepresa("Paiva Castro");
	
	//<--Variaveis relacionadas ao calendário-->
	Calendar c = Calendar.getInstance();
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-d");
	SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd");
	//<--Termino das variaveis relacionadas ao calendário-->





	/** Metodo criadao para efetuar a verificacao do nivel da represa   */
	public void verificaNivelRepresa(){
		
		float aux = (nivelAtual/capacidadeMaxima) * 100;
		setPorcentagemRepresa(aux);
		
		if(getPorcentagemRepresa() >= getLimiteMaximo()){
			System.out.println(this.getName() + ": Esta com o nivel "+ this.getNivelAtual()+" e a porcentagem "+getPorcentagemRepresa());
			

		}
		else if(getPorcentagemRepresa() <= getNivelManterEnviandoAgua()){
			System.out.println(this.getName() + ": Esta com o nivel "+ this.getNivelAtual() + " e a porcentagem "+getPorcentagemRepresa());
			
			
		}
		else{
			System.out.println(this.getName() + " : Esta em um nivel seguro de agua no valor de " + this.getNivelAtual() + " e a porcentagem "+getPorcentagemRepresa());
			
		}
	}
	/** Metodo que faz o calculo da porcentagem do nivel da represa */
	public void efetuaPorcentagemRepresa(){
		float aux = (getNivelAtual()/getCapacidadeMaxima()) * 100;
		setPorcentagemRepresa(aux);
		
	}
	
	/** Metodo que efetua a atualizacao dos dados da represa ao termino de cada dia */
	public void informacoesAgente(){
		//Metodo sera adaptado para ser usado no grapviz
		addBehaviour(new TickerBehaviour(this, tempSolicitacao) {

			@Override
			protected void onTick() {
				// TODO Auto-generated method stub

				if (getTickCount() > qtdDiasExecutar){

					stop();
				}else{

					System.out.println(myAgent.getName() + ": O nome da represa e " + getAID().getLocalName());
					System.out.println(myAgent.getName() + ": A capacidade maxima e " + getCapacidadeMaxima());
					efetuaPorcentagemRepresa();
					
					//AtualizaGráfico
					if(myAgent.getAID().equals("Jaguari@NOTE-MARCUS:7778/JADE")){
						graph_jaguari.volumeUpdate(getPorcentagemRepresa());
					}else if(myAgent.getAID().equals("Cachoeira@NOTE-MARCUS:7778/JADE")){
						graph_cachoeira.volumeUpdate(getPorcentagemRepresa());
					}else if(myAgent.getAID().equals("Atibainha@NOTE-MARCUS:7778/JADE")){
						graph_atibainha.volumeUpdate(getPorcentagemRepresa());
					}else if(myAgent.getAID().equals("PaivaCastro@NOTE-MARCUS:7778/JADE")){
						graph_paivac.volumeUpdate(getPorcentagemRepresa());
					}
					
					verificaNivelRepresa();
					
					informaNivelRepresa();
					
					System.out.println(myAgent.getName() + ":Execucao referente ao dia "+ c.getTime());
					c.add(Calendar.DAY_OF_MONTH, 1);
				}

			}
		});


	}
	
	/** Metodo criado para efetuar a configuracao dos dados de cada Represa quando as represas sao iniadas na plataforma */
	public void capturaNome(){

		//Efetua a construcao do da sequencia de represas
		addBehaviour(new OneShotBehaviour() {

			@Override
			public void action() {
				
				c.set(2014, Calendar.JANUARY, 01);
				
				myAID = myAgent.getAID();
				if(myAID.getName().equals("Jaguari@NOTE-MARCUS:7778/JADE")){
					
					//Gera grafico Represa Jaguari 					
					graph_jaguari.setLocation(new Point(0,0));
					graph_jaguari.setPreferredSize(new Dimension(675, 375));
					graph_jaguari.geraGrafico("teste", 1);
					
					setRepresaAnterior(null);
					procuraAgentePosterior("Cachoeira");
					procuraEta();
					setCapacidadeMaxima(750000000);
					setNivelAtual(179400000);
					setLimiteMaximo(80);
					setNivelManterEnviandoAgua(50);
					setNvlMterEnviandoAguaRepPost(70);
					setPressaoAgua(35);
					calculaLimiteDiario();
					
				}else if(myAID.getName().equals("Cachoeira@NOTE-MARCUS:7778/JADE")){
					//Gera grafico Represa Cachoeira 					
					graph_cachoeira.setLocation(new Point(675,0));
					graph_cachoeira.setPreferredSize(new Dimension(675, 375));
					graph_cachoeira.geraGrafico("teste", 1);
			        
					procuraAgentePosterior("Atibainha");
					procuraAgenteAnterior("Jaguari");
					procuraEta();
					setCapacidadeMaxima(73500000);
					setNivelAtual(33266100);
					setLimiteMaximo(90);
					setNivelManterEnviandoAgua(50);
					setNvlMterEnviandoAguaRepPost(90);
					setPressaoAgua(35);
					calculaLimiteDiario();
					
				}else if(myAID.getName().equals("Atibainha@NOTE-MARCUS:7778/JADE")){
					 //Gera grafico Represa Atibainha 					
					graph_atibainha.setLocation(new Point(0,375));
					graph_atibainha.setPreferredSize(new Dimension(675, 375));
					graph_atibainha.geraGrafico("teste", 1);
					
					procuraAgentePosterior("PaivaCastro");
					procuraAgenteAnterior("Cachoeira");
					procuraEta();
					setCapacidadeMaxima(104000000);
					setNivelAtual(42036800);
					setLimiteMaximo(90);
					setNivelManterEnviandoAgua(50);
					setNvlMterEnviandoAguaRepPost(70);
					setPressaoAgua(35);
					calculaLimiteDiario();
					
				}else if(myAID.getName().equals("PaivaCastro@NOTE-MARCUS:7778/JADE")){
					 //Gera grafico Represa PaivaCastro 					
					graph_paivac.setLocation(new Point(675,375));
					graph_paivac.setPreferredSize(new Dimension(675, 375));
					graph_paivac.geraGrafico("teste", 1);
					
					setRepresaPosterior(null);
					procuraAgenteAnterior("Atibainha");
					procuraEta();
					setCapacidadeMaxima(10800000);
					setNivelAtual(4662360);
					setLimiteMaximo(90);
					setNivelManterEnviandoAgua(50);
					setPressaoAgua(66);
					calculaLimiteDiario();
					
				}
			}
		});


	}


	/** Metodo usado para buscar o AID da Represa Posterior  */
	public void procuraAgentePosterior(String agente){
		
		
		AMSAgentDescription[] agentes = null;
		SearchConstraints c = new SearchConstraints();
		c.setMaxResults(new Long(-1));


		try{

			agentes = AMSService.search(this, new AMSAgentDescription(), c);


			for(int i = 0; i<agentes.length;i++){
				AID agenteID = agentes[i].getName();

				if(agenteID.getName().equals(agente+"@NOTE-MARCUS:7778/JADE")){
					setRepresaPosterior(agenteID);
					System.out.println(this.getName() + ": Encontrei a represa Posterior com o nome " +represaPosterior.getName());
				}
			}

		}	
		catch(FIPAException e){
			e.printStackTrace();
		}

	}
	

	/** Metodo usado para buscar o AID do agente ETA (Aguas Claras) */
	public void procuraEta(){
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

		}	
		catch (FIPAException e){
			e.printStackTrace();
		}
	}
	
	
	/** Metodo que de acordo com o nivel da represa diminui a entrega de agua */
	public float DiminuirEntregaDeAgua(float aguaEntregar){
		float aguaDiminuida = 0;
		float correcao1 = (float)0.90;
		float correcao2 = (float)0.80;
		float correcao3 = (float)0.70;
		float correcao4 = (float)0.60;
		float correcao5 = (float)0.50;
		
		efetuaPorcentagemRepresa();
		
		if(getPorcentagemRepresa() <= 50 && getPorcentagemRepresa() > 40 ){
			aguaDiminuida = aguaEntregar * correcao1;
			
			return aguaDiminuida;
		}else if(getPorcentagemRepresa() <= 40 && getPorcentagemRepresa() > 30){
			aguaDiminuida = aguaEntregar * correcao2;
			
			return aguaDiminuida;
		}else if(getPorcentagemRepresa() <= 30 && getPorcentagemRepresa() > 20){
			aguaDiminuida = aguaEntregar * correcao3;
			
			return aguaDiminuida;
		}else if(getPorcentagemRepresa() <= 20 && getPorcentagemRepresa() > 10){
			aguaDiminuida = aguaEntregar * correcao4;
			
			return aguaDiminuida;
		}else{
			aguaDiminuida = aguaEntregar * correcao5;
			
			return aguaDiminuida;
		}
		
	}
	
	/** Metodo usado para buscar o AID da Represa Anterior, caso a represa tenha uma represa anterior a ela  */
	public void procuraAgenteAnterior(String agente){
		AMSAgentDescription[] agentes = null;
		SearchConstraints c = new SearchConstraints();
		c.setMaxResults(new Long(-1));


		try{

			agentes = AMSService.search(this, new AMSAgentDescription(), c);


			for(int i = 0; i<agentes.length;i++){
				AID agenteID = agentes[i].getName();

				if(agenteID.getName().equals(agente+"@NOTE-MARCUS:7778/JADE")){
					setRepresaAnterior(agenteID);
					System.out.println(this.getName() + ": Encontrei a represa Anterior com o nome " +represaAnterior.getName());
				}
			}

		}	
		catch(FIPAException e){
			e.printStackTrace();
		}

	}

	/** Metodo que calcula o limite de agua que uma represa pode enviar por dia */
	public void calculaLimiteDiario(){
		float limiteDiario =  getPressaoAgua() * segundosDia;	
		
		setLimiteDiario(limiteDiario);
	}
	
	
	/** Metodo geral para envio de mensagem */
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
	
	/** Metodo que efetua o envio de agua entre represas */
	public void envioDeAguaEntreRepresas(float qtdAguaEnviar, AID sender){
		
		if(qtdAguaEnviar <= getLimiteDiario() && qtdAguaEnviar > 0){
			enviaMensagem(16,"Entrega de agua","Portugues",sender,String.valueOf(qtdAguaEnviar), "Entrega de agua");
			float segundos = qtdAguaEnviar / getPressaoAgua();   
		    float segundo = segundos % 60;   
		    float minutos = segundos / 60;   
		    float minuto = minutos % 60;   
		    float hora = minutos / 60;   
		    String hms = String.format ("%.0f:%.0f:%.0f", hora, minuto, segundo);   			
			
		    float nivelFuturo = getNivelAtual() - qtdAguaEnviar;
		    setNivelAtual(nivelFuturo);
			
			System.out.println(this.getName()+" : (Distribuicao entre Represas) Foram necessarias "+ hms +" para transferir  "+ qtdAguaEnviar + " metros cubicos de agua.");
			
		
		}else if(qtdAguaEnviar > 0) {
			
			enviaMensagem(16,"Entrega de agua","Portugues",sender,String.valueOf(getLimiteDiario()), "Entrega de agua");
			float segundos = getLimiteDiario() / getPressaoAgua();   
		    float segundo = segundos % 60;   
		    float minutos = segundos / 60;   
		    float minuto = minutos % 60;   
		    float hora = minutos / 60;   
		    String hms = String.format ("%.0f:%.0f:%.0f", hora, minuto, segundo);   			
			
		    float nivelFuturo = getNivelAtual() - getLimiteDiario();
		    setNivelAtual(nivelFuturo);
			
			System.out.println(this.getName()+" : (Distribuicao entre Represas) Foram necessarias "+ hms +" para transferir  "+ getLimiteDiario() + " metros cubicos de agua." );
		}
		
		
	};

	/** Metodo que solicita o nivel de agua da represa posterior para saber o quanto a represa pode enviar. */
	public void solicitaNivelAgua(){

		addBehaviour(new TickerBehaviour(this, tempSolicitacao) {
			
			@Override
			protected void onTick() {
				// TODO Auto-generated method stub

				if (getTickCount() > qtdDiasExecutar){
				
					stop();
				
				}else{
					
					AID represaPosterior = getRepresaPosterior();

					if(represaPosterior != null){

						//solicitacao do nivel da represa

						enviaMensagem(16,"Nivel agua", "Portugues", represaPosterior, null, "Nivel agua" );	
						System.out.println(myAgent.getName()+ " : Solicitacao de nivel de agua para a represa posterior "+ getRepresaPosterior() + " foi feita com sucesso.");
					}
					
				}
				
				
			}
		});
		
	}

	
	public void solicitaPorcentRepresaAnterior(){
		
		addBehaviour(new OneShotBehaviour(this) {
			
			@Override
			public void action() {
				// TODO Auto-generated method stub
				
				if(myAID.getName().equals("PaivaCastro@NOTE-MARCUS:7778/JADE")){

					if(getRepresaAnterior() != null){


						enviaMensagem(16,"Porcentagem represa", "Portugues", getRepresaAnterior(), null, "Porcentagem represa" );	
						System.out.println(myAgent.getName()+ " : Porcentagem da represa anterior foi solicitada.");
					}
				}
			}
		});
		
		

	}
	
	 /** Metodo criado para verificar a solicitacao de agua feita por outra Represa */
	public void verificaSolicitacaoAgua(float valorDistribuir, AID sender){
		setSolicitacaoAgua(valorDistribuir);
		
		float nvlAposReq =  nivelAtual - valorDistribuir;
		
		float qtdAguaManterEnviando = getCapacidadeMaxima() * (getNivelManterEnviandoAgua() / 100);
		float qtdAguaNecessaria = qtdAguaManterEnviando - nvlAposReq;

		System.out.println(this.getName() +": Recebida a solicitacao de agua no valor de "+ valorDistribuir);
		
		//verifica se o nivel Represa ficaria positivo apos entregar a agua
		if(nvlAposReq >= 0){
			
			envioDeAguaEntreRepresas(valorDistribuir, sender);
			enviaMensagem(7,"Resposta","Portugues", sender,String.valueOf(0), "Solicitacao de agua - resposta" );

		}else{
			//Caso a quantidade de agua nao seja o bastante a Represa solicita para a anterior agua 
			
			if(getRepresaAnterior() != null){
				if(qtdAguaNecessaria < 0){
					qtdAguaNecessaria = qtdAguaNecessaria * -1;
					enviaMensagem(16,"Solicitacao","Portugues", getRepresaAnterior(), String.valueOf(qtdAguaNecessaria), "Solicitacao de agua" );
				}else
					enviaMensagem(16,"Solicitacao","Portugues", getRepresaAnterior(), String.valueOf(qtdAguaNecessaria), "Solicitacao de agua" );
			}
			else{
				envioDeAguaEntreRepresas(getNivelAtual(), sender);
				enviaMensagem(7,"Solicitacao de agua - resposta","Portugues",sender,String.valueOf(0), "Solicitacao de agua - resposta");
			}
		}

	}

	/** Metodo criado para efetuar o envio de agua da Represa para a ETA (Aguas Claras)  */
	public void envioDeAguaEntreRepresaEta(float qtdAguaEnviar, AID sender){

		if(qtdAguaEnviar <= getLimiteDiario()){

			if(qtdAguaEnviar < 0 ){

				qtdAguaEnviar = qtdAguaEnviar * -1;

				enviaMensagem(16,"Entrega de agua","Portugues",sender,String.valueOf(qtdAguaEnviar), "Requisicao de agua");
				float segundos = qtdAguaEnviar / getPressaoAgua();   
				float segundo = segundos % 60;   
				float minutos = segundos / 60;   
				float minuto = minutos % 60;   
				float hora = minutos / 60;   
				String hms = String.format ("%.0f:%.0f:%.0f", hora, minuto, segundo);   					

				float nivelFuturo = getNivelAtual() - qtdAguaEnviar;
				setNivelAtual(nivelFuturo);

				System.out.println(this.getName()+" :(Distribuicao para ETA) Foram necessarias "+ hms + " para transferir  "+ qtdAguaEnviar + " metros cubicos de agua.");
			}
			else{


				enviaMensagem(16,"Entrega de agua","Portugues",sender,String.valueOf(qtdAguaEnviar), "Requisicao de agua");
				float segundos = qtdAguaEnviar / getPressaoAgua();   
				float segundo = segundos % 60;   
				float minutos = segundos / 60;   
				float minuto = minutos % 60;   
				float hora = minutos / 60;   
				String hms = String.format ("%.0f:%.0f:%.0f", hora, minuto, segundo);   					

				float nivelFuturo = getNivelAtual() - qtdAguaEnviar;
				setNivelAtual(nivelFuturo);

				System.out.println(this.getName()+" :(Distribuicao para ETA) Foram necessarias "+ hms + " para transferir  "+ qtdAguaEnviar + " metros cubicos de agua.");
			}

		}else{

			if(qtdAguaEnviar < 0){

				qtdAguaEnviar = qtdAguaEnviar * -1;


				enviaMensagem(16,"Entrega de agua","Portugues",sender,String.valueOf(getLimiteDiario()), "Requisicao de agua");

				float segundos = getLimiteDiario();   
				float segundo = segundos % 60;   
				float minutos = segundos / 60;   
				float minuto = minutos % 60;   
				float hora = minutos / 60;   
				String hms = String.format ("%.0f:%.0f:%.0f", hora, minuto, segundo);   	

				float nivelFuturo = getNivelAtual() - getLimiteDiario();
				setNivelAtual(nivelFuturo);

				System.out.println(this.getName()+" :(Distribuicao para ETA) Foram necessarias "+ hms +"para transferir  "+ getLimiteDiario() + " metros cubicos de agua." );

			}else{

				enviaMensagem(16,"Entrega de agua","Portugues",sender,String.valueOf(getLimiteDiario()), "Requisicao de agua");

				float segundos = getLimiteDiario();   
				float segundo = segundos % 60;   
				float minutos = segundos / 60;   
				float minuto = minutos % 60;   
				float hora = minutos / 60;   
				String hms = String.format ("%.0f:%.0f:%.0f", hora, minuto, segundo);   	

				float nivelFuturo = getNivelAtual() - getLimiteDiario();
				setNivelAtual(nivelFuturo);

				System.out.println(this.getName()+" :(Distribuicao para ETA) Foram necessarias "+ hms +"para transferir  "+ getLimiteDiario() + " metros cubicos de agua." );
			}
		}



	}
	
	
	/** Metodo que efetua o tratamento das mensagens recebidas dos outros agentes.   */
	public void trataMensagens(){

		addBehaviour(new CyclicBehaviour(this) {

			@Override
			public void action() {

				ACLMessage msgReceive = myAgent.receive();
				if(msgReceive != null){

					String idConversa = msgReceive.getConversationId();
					int tipoMensagem = msgReceive.getPerformative();


					if(idConversa.equalsIgnoreCase("Nivel agua") && tipoMensagem == 16){
						//Mensagem Recebida pela Represa solicitando o nivel de agua 

						String resposta = String.valueOf(getNivelAtual())+"#"+ String.valueOf(getCapacidadeMaxima());

						enviaMensagem(7, "Nivel agua","Portugues", msgReceive.getSender(), resposta, "Nivel agua - resposta");

					}else if(idConversa.equalsIgnoreCase("Nivel agua - resposta") && tipoMensagem == 7){
						//Mensagem Recebida pela Represa com a resposta do nivel de agua

						String msg_resposta = msgReceive.getContent();
						String[] nivelAtualRepresa = msg_resposta.split("#");
						String nivelAtualMsg = nivelAtualRepresa[0];
						String capMaxMsg = nivelAtualRepresa[1];

						//Recebimento do nivel atual e a capacidade maxima da represa a qual foi perguntado esses dados

						if(msgReceive.getSender().equals(represaPosterior.getName())){
							setNivelRepresaPosterior(Float.parseFloat(nivelAtualMsg));
							setCapMaxRepresaPosterior(Float.parseFloat(capMaxMsg));
						}

						float	qtdFaltaEncher = getCapMaxRepresaPosterior() - getNivelRepresaPosterior();

						float porcentRepresaPosterior = (getNivelRepresaPosterior() / getCapMaxRepresaPosterior()) * 100;


						if(porcentRepresaPosterior < getNvlMterEnviandoAguaRepPost() && getPorcentagemRepresa() > nivelManterEnviandoAgua){
							
							envioDeAguaEntreRepresas(qtdFaltaEncher, msgReceive.getSender());

						//}else if(getPorcentagemRepresa() < nivelManterEnviandoAgua && getPorcentagemRepresa() > 0){
						//	System.out.println(myAgent.getName() +": Represa esta abaixo dos nivel minimo.");
							
						//	envioDeAguaEntreRepresas(qtdFaltaEncher, msgReceive.getSender());
						}else{
							
							System.out.println(myAgent.getName() +": Represa posterior esta muito cheia para receber agua");
							envioDeAguaEntreRepresas(0, msgReceive.getSender());
						
						}
					}else if(idConversa.equalsIgnoreCase("Requisicao de agua") && tipoMensagem == 16){
						//Mensagem Recebida pela ETA efetuando uma requisicao de agua
						float valorDistribuir = Float.parseFloat(msgReceive.getContent());
						//adicionando a qtd de agua que sera perdida
						valorDistribuir += (valorDistribuir * qtdPerdaDeAgua);
						//Teste Contratos ilícitos
						//valorDistribuir += qtdContratosMedia;
						
						setValorRequisicaoETA(valorDistribuir);

						float qtdAguaAposReq =  nivelAtual - valorDistribuir;
						
						System.out.println(myAgent.getName() +": Recebida a solicitacao de agua da ETA no valor de "+ valorDistribuir );
						float nvlManterEnviando = getCapacidadeMaxima() * (getNivelManterEnviandoAgua() / 100);

						//verifica se o nivel Represa ficaria positivo apos entregar a agua, caso sim entrega o valor solicitado
						//Caso nao fique positivo, solicita agua para a represa anterior
						if(qtdAguaAposReq >= nvlManterEnviando){
							
							if(valorDistribuir <= getLimiteDiario()){
								
								
								
								envioDeAguaEntreRepresaEta(valorDistribuir, getETA());
								
							}else{
								envioDeAguaEntreRepresaEta(getLimiteDiario(), getETA());
								
							}
								
//Tratar retorno agua
						}else{

							//Efetua o Calculo da diferenca entre o nivel apos requisicao e o nivel de manter enviando agua e solicita essa diferenca para poder atender a requisicao

							//Caso a quantidade de agua nao seja o bastante a Represa solicita para a anterior agua 
							float qtdAguaManterEnviando = getNivelAtual() * (getNivelManterEnviandoAgua() / 100);
							float qtdAguaNecessaria = qtdAguaManterEnviando - qtdAguaAposReq;
							
							if(qtdAguaNecessaria < 0){
								qtdAguaNecessaria = qtdAguaNecessaria * -1;
								enviaMensagem(16,"Solicitacao","Portugues", getRepresaAnterior(), String.valueOf(qtdAguaNecessaria), "Solicitacao de agua" );
							}else
								enviaMensagem(16,"Solicitacao","Portugues", getRepresaAnterior(), String.valueOf(qtdAguaNecessaria), "Solicitacao de agua" );

						}
					}else if(idConversa.equalsIgnoreCase("Entrega de agua")){
						//Mensagem Recebida pela represa anterior referente a uma entrega de agua 
						
						float nivelFuturo = getNivelAtual() + Float.parseFloat(msgReceive.getContent());
					    setNivelAtual(nivelFuturo);
						//setNivelAtual(getNivelAtual() + Float.parseFloat(msgReceive.getContent()));
						System.out.println(myAgent.getName()+": Entrega de agua recebida no valor de "+ msgReceive.getContent());

					}
					else if(idConversa.equalsIgnoreCase("Solicitacao de agua")){
						//Mensagem Recebida pela Represa posterior que esta solicitando agua 

						verificaSolicitacaoAgua(Float.parseFloat(msgReceive.getContent()),msgReceive.getSender());

					}else if(idConversa.equalsIgnoreCase("Solicitacao de agua - resposta")){
						//Mensagem Recebida pela Represa

						if(represaPosterior != null){

							float nivelFuturoAposRecebimento = getNivelAtual() + Float.parseFloat(msgReceive.getContent());
							setNivelAtual(nivelFuturoAposRecebimento);
							float nivelFuturo = getNivelAtual() - getSolicitacaoAgua();
							
							if(nivelFuturo > 0){
								
								envioDeAguaEntreRepresas(getSolicitacaoAgua(), getRepresaPosterior());
								enviaMensagem(7,"Resposta","Portugues", getRepresaPosterior(), String.valueOf(0), "Solicitacao de agua - resposta");
								
							}else{
								
								envioDeAguaEntreRepresas(getNivelAtual(), getRepresaPosterior());
								enviaMensagem(7,"Resposta","Portugues", getRepresaPosterior(), String.valueOf(0), "Solicitacao de agua - resposta");
								
							}
							
							
							
							
						}else{
							setNivelAtual(nivelAtual += Float.parseFloat(msgReceive.getContent()));
							//efetuar envio de agua para a ETA
							
							float nvlAposReq = getNivelAtual() - getValorRequisicaoETA();
							float nvlManterEnviando = getCapacidadeMaxima() * (getNivelManterEnviandoAgua() / 100);
							
							
							if(nvlAposReq >= nvlManterEnviando){
								envioDeAguaEntreRepresaEta(DiminuirEntregaDeAgua(getValorRequisicaoETA()), getETA());
								
								
							}else if(nvlAposReq < nvlManterEnviando && nvlAposReq > 0){
								
								envioDeAguaEntreRepresaEta(DiminuirEntregaDeAgua(nvlAposReq), getETA());
								
							}else{
								envioDeAguaEntreRepresaEta(0, getETA());
							}
							
							
						}



					}
					else if (idConversa.equalsIgnoreCase("Represa sem agua")){
						//Mensagem Recebida da Represa avisando que esta sem agua para enviar.

						if(represaPosterior != null)
							enviaMensagem(7,"Resposta","Portugues", getRepresaPosterior(), String.valueOf(0), "Solicitacao de agua - resposta" );

						else{
							//mensagem de informa
							enviaMensagem(7, "Requisicao de agua", "Portugues", getETA(), String.valueOf(0), "Requisicao de agua");


						}

					}
					else if(idConversa.equalsIgnoreCase("Repositorio incompleto")){
						
						float qtdAguaNecessaria = Float.parseFloat(msgReceive.getContent());
						
						if(getNivelAtual() > 0){
							if(getNivelAtual() >= qtdAguaNecessaria){
								enviaMensagem(7, "Requisicao", "Portugues", getETA(), String.valueOf(qtdAguaNecessaria), "Repositorio incompleto");
								float aux = getNivelAtual() - qtdAguaNecessaria;
								setNivelAtual(aux);
								
							}else{
								enviaMensagem(7, "Requisicao", "Portugues", getETA(), String.valueOf(getNivelAtual()), "Repositorio incompleto");
								setNivelAtual(0);
							
							}
						
						}
						
					}else if(idConversa.equalsIgnoreCase("Fornecimento rio")){
						setNivelAtual(getNivelAtual() + Float.parseFloat(msgReceive.getContent()));
						System.out.println(myAgent.getName() +": Recebi com sucesso o fornecimento de agua do rio e da chuva no valor de"+ msgReceive.getContent());
					
					}else if(idConversa.equalsIgnoreCase("Porcentagem represa")){
						
					efetuaPorcentagemRepresa();
					
					enviaMensagem(7, "Porcentagem represa - resposta", "Portugues", msgReceive.getSender(), String.valueOf(getPorcentagemRepresa()), "Porcentagem represa - resposta");
					
					}else if(idConversa.equalsIgnoreCase("Porcentagem represa - resposta")){
						setPorcentagemRepresaAnterior(Float.parseFloat(msgReceive.getContent()));
						
					}

				}else
					block();
			}
		});
		//termino do comportamento que verifica mensagens e toma uma decisao sobre o que fazer 

	}

	public void informaNivelRepresa(){

	     String dadosCapacidade = String.valueOf(getNivelAtual())+"#"+ String.valueOf(getCapacidadeMaxima());

	     enviaMensagem(7, "Dados Represa", "Portugues", ETA, dadosCapacidade, "Dados Represa");
	 
	 }
	
	/** Metodo que efetua a execucoes dos comportamentos dos agentes.   */
	protected void setup() {		
		capturaNome();

		informacoesAgente();

		solicitaNivelAgua();
		
		trataMensagens();


	}

	
	/** Metodo executado quando o agente termina de executar os seus comportamentos.  */
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

	public void setNivelAtual(float nivelAtual) {
		this.nivelAtual = nivelAtual;
	}

	public float getPorcentagemRepresa() {
		return porcentagemRepresa;
	}

	public void setPorcentagemRepresa(float porcentagemRepresa) {
		this.porcentagemRepresa = porcentagemRepresa;
	}

	public float getPorcentagemRepresaAnterior() {
		return porcentagemRepresaAnterior;
	}
	public void setPorcentagemRepresaAnterior(float porcentagemRepresaAnterior) {
		this.porcentagemRepresaAnterior = porcentagemRepresaAnterior;
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

	public float getValorRequisicaoETA() {
		return valorRequisicaoETA;
	}

	public void setValorRequisicaoETA(float valorRequisicaoETA) {
		this.valorRequisicaoETA = valorRequisicaoETA;
	}

	public float getLimiteDiario() {
		return limiteDiario;
	}

	public void setLimiteDiario(float limiteDiario) {
		this.limiteDiario = limiteDiario;
	}
	public float getSolicitacaoAgua() {
		return solicitacaoAgua;
	}
	public void setSolicitacaoAgua(float solicitacaoAgua) {
		this.solicitacaoAgua = solicitacaoAgua;
	}
	public float getNvlMterEnviandoAguaRepPost() {
		return nvlMterEnviandoAguaRepPost;
	}
	public void setNvlMterEnviandoAguaRepPost(float nvlMterEnviandoAguaRepPost) {
		this.nvlMterEnviandoAguaRepPost = nvlMterEnviandoAguaRepPost;
	}

}
