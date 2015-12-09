package agentes;



import graficos.GraficoNivelConsumo;
import graficos.GraficoNivelRepresa;
import graficos.GraficoNivelSistema;

import java.awt.Dimension;
import java.awt.Point;
import java.util.Calendar;

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
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;


@SuppressWarnings("serial")
public class AgenteETA extends Agent {


	private float consumoConsumidor;
	private AID nomeConsumidor;
	private AID paivaCastro;
	private float qtdAguaDistribuir;
	private float capacidadeMaxima;
	private float nivelAtual;
	private float pressaoAgua;
	static final long tempSolicitacao = 10000;
	private float nivelAtualJaguari;
	 private float nivelAtualCachoeira; 
	 private float nivelAtualAtibainha; 
	 private float nivelAtualPaivaCastro; 
	 private float capMaxJaguari;
	 private float capMaxCachoeira;
	 private float capMaxAtibainha;
	 private float capMaxPaivaCastro;
	private GraficoNivelRepresa graph_eta = new GraficoNivelRepresa("ETA");
	private GraficoNivelConsumo graph_consumo = new GraficoNivelConsumo("Consumidor");
	private GraficoNivelSistema graph_cantareira = new GraficoNivelSistema("SistemaCantareira");
	private float nivelAtualSistema;
	private int i = 0;
	
	//<--Variaveis relacionadas ao calendário-->
	Calendar c = Calendar.getInstance();
	
	public void distribuiAgua(AID destino, float aguaEnviar){
			
			ACLMessage msg = new ACLMessage(ACLMessage.CONFIRM);
			msg.setOntology("Entrega de agua");
			msg.setLanguage("Portugues");
			msg.addReceiver(destino);
			msg.setContent(String.valueOf(aguaEnviar));
			msg.setConversationId("Entrega de agua");
			this.send(msg);
		    float segundos = aguaEnviar / getPressaoAgua();   
		    float segundo = segundos % 60;   
		    float minutos = segundos / 60;   
		    float minuto = minutos % 60;   
		    float hora = minutos / 60;   
		    String hms = String.format ("%.0f:%.0f:%.0f", hora, minuto, segundo);   			
		    
			System.out.println(this.getName()+" : Foram necessarias "+ hms +" para transferir  "+ aguaEnviar + " metros cubicos de agua." );
			
	}
	
	public void atualizaInformacoesRepresa(float nivelRepresa){
		float nivelAtual = getNivelAtualSistema() + nivelRepresa;
		setNivelAtualSistema(nivelAtual);
		graph_cantareira.volumeUpdate(getNivelAtualSistema());
	}
	

	public void informacoesEta(){
		
		addBehaviour(new OneShotBehaviour(this) {
			
			@Override
			public void action() {
				setCapacidadeMaxima(1328000);
				setNivelAtual(1328000);
				setPressaoAgua(32);
			}
		});
		
	}
	
	public void VerificaNivel(){
		
		addBehaviour(new TickerBehaviour(this,tempSolicitacao) {
			
			@Override
			public void onTick() {
				// TODO Auto-generated method stub
					
				if(getNivelAtual() < getCapacidadeMaxima()){
					
					float aux = getCapacidadeMaxima() - getNivelAtual();
					
					ACLMessage msgEnviada = new ACLMessage(ACLMessage.REQUEST);
					msgEnviada.setOntology("Requisicao");
					msgEnviada.setLanguage("Portugues");
					msgEnviada.addReceiver(getpaivaCastro());
					msgEnviada.setContent(String.valueOf(aux));
					msgEnviada.setConversationId("Repositorio incompleto");
					myAgent.send(msgEnviada);
					
					
				}
				
			}
		});
		
	}
	
	public void RegistraServico(){
		
		addBehaviour(new OneShotBehaviour(this) {
			
			@Override
			public void action() {
				// TODO Auto-generated method stub

				System.out.println(myAgent.getName() + ": Registrando servico de fornecimento de agua.");

				String tipoServico = "Fornecimento";
				String nomeServico = "Fornecimento de agua";

				//Criacao da entra DF
				DFAgentDescription dfd = new DFAgentDescription();
				dfd.setName(getAID()); //informa o AID do agente


				//Criacao do serviço 
				ServiceDescription sd = new ServiceDescription();
				sd.setType(tipoServico);
				sd.setName(nomeServico);
				//Adicao do serviço

				dfd.addServices(sd);

				//Criacao do serviço
				try{
					//register(agente que oferece, descrição)
					DFService.register(myAgent, dfd);
					System.out.println(myAgent.getName() + ": Servico de fornecimento de agua foi registrado com sucesso.");
				}catch (FIPAException e){
					e.printStackTrace();
				}

			}
		});
			}

	public void procuraRepresa(){
		
		addBehaviour(new OneShotBehaviour() {
			
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

						if(agenteID.equals("PaivaCastro@NOTE-MARCUS:7778/JADE")){
							setpaivaCastro(agenteID);
							System.out.println( myAgent.getName() +": "+ agenteID.getName()+" foi encontrada!");
						}
					}
				}
				catch(FIPAException e){
					e.printStackTrace();
				}
				//GeraGrafico Sistema
				graph_cantareira.setLocation(new Point(0,0));
				graph_cantareira.setPreferredSize(new Dimension(675, 375));
				graph_cantareira.geraGrafico("teste", 1);
				
				//GeraGrafico Consumidor
				graph_consumo.setLocation(new Point(0,0));
				graph_consumo.setPreferredSize(new Dimension(675, 375));
				graph_consumo.geraGrafico("teste", 1);
				
				
				//Gera grafico ETA 					
				graph_eta.setLocation(new Point(0,375));
				graph_eta.setPreferredSize(new Dimension(675, 375));
				graph_eta.geraGrafico("teste", 1);

			}
		});
			}

	public void trataMensagens(){
		//comportamento para tratamento das mensagens recebidas
				addBehaviour(new CyclicBehaviour(this) {

					@Override
					public void action() {
						// TODO Auto-generated method stub
						
						ACLMessage msg = receive();
						if(msg != null){

							String idConversa = msg.getConversationId();
							String content = msg.getContent();
							int tipoMensagem = msg.getPerformative();
							
							

							if(idConversa.equalsIgnoreCase("Solicitacao de agua") && tipoMensagem == 16){
								setConsumoConsumidor(Float.parseFloat(content));
								setNomeConsumidor(msg.getSender());
								//mostra o nome do consumidor e o valor solicitado
								System.out.println(myAgent.getName() + ": Solicitacao do "+ nomeConsumidor.getName() +" foi feita no valor de "+ content);
								
								//Vou buscar pelos agentes
								//a busca retorna um array DFAgente Description
								//o paramentro this indica o agente que esta realizando a busca
								System.out.println(myAgent.getName() + ": Iniciando requisicao de agua na Represa!");	
								ACLMessage msgEnviada = new ACLMessage(ACLMessage.REQUEST);
								msgEnviada.setOntology("Requisicao");
								msgEnviada.setLanguage("Portugues");
								msgEnviada.addReceiver(getpaivaCastro());
								msgEnviada.setContent(String.valueOf(getConsumoConsumidor()));
								msgEnviada.setConversationId("Requisicao de agua");
								myAgent.send(msgEnviada);

								graph_eta.volumeUpdate((getNivelAtual()/getCapacidadeMaxima())*100);

							}
							else if(idConversa.equalsIgnoreCase("Requisicao de agua")){
								
								float aguaRecebida = Float.parseFloat(content);
								
								if(aguaRecebida < 0){
									aguaRecebida = aguaRecebida * (-1);
									setQtdAguaDistribuir(aguaRecebida);
								}else{
									setQtdAguaDistribuir(aguaRecebida);
								}
								
							
									if(getConsumoConsumidor() == getQtdAguaDistribuir()){
									
										distribuiAgua(getNomeConsumidor(),getConsumoConsumidor());
										System.out.println(myAgent.getName() + ": Agua recebida com sucesso da represa, no valor de  "+qtdAguaDistribuir +" igual o valor solicitado."); 
										graph_consumo.consumoUpdate(getQtdAguaDistribuir());
									}else if(getConsumoConsumidor() > getQtdAguaDistribuir()){
										distribuiAgua(getNomeConsumidor(),getQtdAguaDistribuir());
										System.out.println(myAgent.getName() + ": Recebi menos agua que o necessario para distribuir ");
										graph_consumo.consumoUpdate(getQtdAguaDistribuir());
									}else if(getConsumoConsumidor() < qtdAguaDistribuir){
										//distribui a agua de forma de tal forma que os consumidores recebam o necessário

										float excedente = -1 *(getConsumoConsumidor() - getQtdAguaDistribuir());
										
										
										distribuiAgua(getNomeConsumidor(),getConsumoConsumidor());
										System.out.println(myAgent.getName() + ": Recebi mais agua que o necessario para distribuir no valor de "+excedente);
										graph_consumo.consumoUpdate(getQtdAguaDistribuir());
									}else if(getQtdAguaDistribuir() == 0){
										
										if(nivelAtual == 0){
											System.out.println(myAgent.getName() + ": Nao recebi agua e nao tenho agua!");
											distribuiAgua(getNomeConsumidor(),0);
										}else if(getConsumoConsumidor() == getNivelAtual()){
											distribuiAgua(getNomeConsumidor(),getConsumoConsumidor());
											setNivelAtual(0);
											System.out.println(myAgent.getName() + ": Nao recebi agua, porem usei "+ getConsumoConsumidor() + " do meu repositorio." );
											
										}else if(getConsumoConsumidor() < getNivelAtual()){
											distribuiAgua(getNomeConsumidor(),getConsumoConsumidor());
											System.out.println(myAgent.getName() + ": Nao recebi agua, porem usei "+ getConsumoConsumidor() + " que foi menor que o meu limite");
										}else if(getConsumoConsumidor() > getNivelAtual()){
											distribuiAgua(getNomeConsumidor(),getConsumoConsumidor());
											System.out.println(myAgent.getName() + ": Nao recebi agua, porem usei "+ getConsumoConsumidor() + " do meu repositorio, considerando que a requisicao foi maior que o meu repositorio.");
										}
										

									}
								
								
							}else if(idConversa.equalsIgnoreCase("Dados Represa")){
						        
						        String msg_resposta = msg.getContent();
						        
						        String[] nivelAtualRepresa = msg_resposta.split("#");
						        float nivelAtualMsg = Float.parseFloat(nivelAtualRepresa[0]);
						        float capMaxMsg = Float.parseFloat(nivelAtualRepresa[1]);
						        if(msg.getSender().getName().equals("Jaguari@NOTE-MARCUS:7778/JADE")){
						         setNivelAtualJaguari(nivelAtualMsg);
						         setCapMaxJaguari(capMaxMsg);
						         System.out.println("Nivel Atual Jaguari: " + getNivelAtualJaguari());
						         atualizaInformacoesRepresa(getNivelAtualJaguari());						         
						         i++;
						        }else if(msg.getSender().getName().equals("Cachoeira@NOTE-MARCUS:7778/JADE")){
						         setNivelAtualCachoeira(nivelAtualMsg);
						         setCapMaxCachoeira(capMaxMsg);
						         System.out.println("Nivel Atual Cachoeira: " + getNivelAtualCachoeira());
						         atualizaInformacoesRepresa(getNivelAtualCachoeira());
						         i++;
						        }else if(msg.getSender().getName().equals("Atibainha@NOTE-MARCUS:7778/JADE")){
						         setNivelAtualAtibainha(nivelAtualMsg);
						         setCapMaxAtibainha(capMaxMsg);
						         System.out.println("Nivel Atual Atibainha: " + getNivelAtualAtibainha());
						         atualizaInformacoesRepresa(getNivelAtualAtibainha());
						         i++;
						        }else if(msg.getSender().getName().equals("PaivaCastro@NOTE-MARCUS:7778/JADE")){
						         setNivelAtualPaivaCastro(nivelAtualMsg);
						         setCapMaxPaivaCastro(getNivelAtualPaivaCastro());
						         System.out.println("Nivel Atual Paiva Castro: " + getNivelAtualPaivaCastro());
						         atualizaInformacoesRepresa(nivelAtualMsg);
						         i++;
						        }						 
						        
						        if(i == 4){
						        	setNivelAtualSistema(0);
						        	i = 0;
						        }
						        
							}else if(idConversa.equalsIgnoreCase("Repositorio incompleto")){
								float aguaRecebida = Float.parseFloat(msg.getContent());
								if(aguaRecebida == 0){
									System.out.println(myAgent.getName() + ": Nao recebi agua da represa, continuo com o repositorio vazio.");
								}else{
									float aux = getNivelAtual() + aguaRecebida;
									setNivelAtual(aux);
									System.out.println(myAgent.getName() + ": Recebi "+ aguaRecebida + " para colocar no repositorio.");
									
								}
							
								
								
								
								
							}
						}else 
							block();

					}
				});
	}
		
	public void setup(){
		//inicializacao do agente 
		System.out.println("Agente "+ this.getName()+" foi inicializado!");
		
		informacoesEta();

		RegistraServico();
		
		procuraRepresa();
		
		trataMensagens();
		
	}

	protected void takeDown(){
		try{ DFService.deregister(this);}
		catch(FIPAException e){
			e.printStackTrace();
		}

		System.out.println("Agente " + getAID() + "finalizado com sucesso");
	}

	public AID getpaivaCastro() {
		return paivaCastro;
	}

	public void setpaivaCastro(AID paivaCastro) {
		this.paivaCastro = paivaCastro;
	}

	float getQtdAguaDistribuir() {
		return qtdAguaDistribuir;
	}

	void setQtdAguaDistribuir(float qtdAguaDistribuir) {
		this.qtdAguaDistribuir = qtdAguaDistribuir;
	}


	public float getConsumoConsumidor() {
		return consumoConsumidor;
	}


	public void setConsumoConsumidor(float consumoConsumidor) {
		this.consumoConsumidor = consumoConsumidor;
	}


	public AID getNomeConsumidor() {
		return nomeConsumidor;
	}


	public void setNomeConsumidor(AID nomeConsumidor) {
		this.nomeConsumidor = nomeConsumidor;
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


	public float getPressaoAgua() {
		return pressaoAgua;
	}


	public void setPressaoAgua(float pressaoAgua) {
		this.pressaoAgua = pressaoAgua;
	}


	public float getNivelAtualJaguari() {
		return nivelAtualJaguari;
	}


	public void setNivelAtualJaguari(float nivelAtualJaguari) {
		this.nivelAtualJaguari = nivelAtualJaguari;
	}


	public float getNivelAtualCachoeira() {
		return nivelAtualCachoeira;
	}


	public void setNivelAtualCachoeira(float nivelAtualCachoeira) {
		this.nivelAtualCachoeira = nivelAtualCachoeira;
	}


	public float getNivelAtualAtibainha() {
		return nivelAtualAtibainha;
	}


	public void setNivelAtualAtibainha(float nivelAtualAtibainha) {
		this.nivelAtualAtibainha = nivelAtualAtibainha;
	}


	public float getNivelAtualPaivaCastro() {
		return nivelAtualPaivaCastro;
	}


	public void setNivelAtualPaivaCastro(float nivelAtualPaivaCastro) {
		this.nivelAtualPaivaCastro = nivelAtualPaivaCastro;
	}


	public float getCapMaxJaguari() {
		return capMaxJaguari;
	}


	public void setCapMaxJaguari(float capMaxJaguari) {
		this.capMaxJaguari = capMaxJaguari;
	}


	public float getCapMaxCachoeira() {
		return capMaxCachoeira;
	}


	public void setCapMaxCachoeira(float capMaxCachoeira) {
		this.capMaxCachoeira = capMaxCachoeira;
	}


	public float getCapMaxAtibainha() {
		return capMaxAtibainha;
	}


	public void setCapMaxAtibainha(float capMaxAtibainha) {
		this.capMaxAtibainha = capMaxAtibainha;
	}


	public float getCapMaxPaivaCastro() {
		return capMaxPaivaCastro;
	}


	public void setCapMaxPaivaCastro(float capMaxPaivaCastro) {
		this.capMaxPaivaCastro = capMaxPaivaCastro;
	}


	public float getNivelAtualSistema() {
		return nivelAtualSistema;
	}


	public void setNivelAtualSistema(float nivelAtualSistema) {
		this.nivelAtualSistema = nivelAtualSistema;
	}



}
