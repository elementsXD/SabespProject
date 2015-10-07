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
import jade.core.behaviours.OneShotBehaviour;
import formulasProjeto.*;

@SuppressWarnings("serial")
public class AgenteETA extends Agent {


	private float consumoConsumidor;
	private AID nomeConsumidor;
	private AID paivaCastro;
	private AID gestorCrise;
	private float qtdAguaDistribuir;
	private float capacidadeMaxima;
	private float nivelAtual;
	private int p;
	private int q;
	private int k1;
	private int q_esp;
	
	
	
	public void distribuiAgua(AID destino, float aguaEnviar){
		
		float aguaEntregue = 0;
		FormulasFisicas form = new FormulasFisicas();
		
		
		
		float aguaSendoEntregue = 0;
		
		aguaSendoEntregue = form.vazaoETA(p, q, k1, q_esp);
		
		
		
		while(aguaEntregue < aguaEnviar){

			ACLMessage msg = new ACLMessage(ACLMessage.CONFIRM);
			msg.setOntology("Entrega de agua");
			msg.setLanguage("Portugues");
			msg.addReceiver(destino);
			msg.setContent(String.valueOf(aguaSendoEntregue));
			msg.setConversationId("Entrega de agua");
			this.send(msg);
			
			aguaEntregue += aguaSendoEntregue;
			
		}
				
	}
	
	public void configuradorVazao(int p, int q, int k1, int q_esp){
		setP(p);
		setQ(q);
		setK1(k1);
		setQ_esp(q_esp);
	}

	public void informacoesEta(){
		
		addBehaviour(new OneShotBehaviour(this) {
			
			@Override
			public void action() {
				// TODO Auto-generated method stub

				setCapacidadeMaxima(1328000);
				setNivelAtual(500000);
				
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

						if(agenteID.equals("PaivaCastro@Sabesp:7778/JADE")){
							setpaivaCastro(agenteID);
							System.out.println( myAgent.getName() +": "+ agenteID.getName()+" foi encontrada!");
						}
					}
				}
				catch(FIPAException e){
					e.printStackTrace();
				}


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



							}
							else if(idConversa.equalsIgnoreCase("Requisicao de agua")){
								setQtdAguaDistribuir(Float.parseFloat(content));

								if(getConsumoConsumidor() == qtdAguaDistribuir){
								
									distribuiAgua(getNomeConsumidor(),getConsumoConsumidor());
									System.out.println(myAgent.getName() + ": Agua recebida com sucesso da represa "+ msg.getSender() + " igual o valor solicitado."); 

								}else if(getConsumoConsumidor() > qtdAguaDistribuir){
									distribuiAgua(getNomeConsumidor(),getConsumoConsumidor());
									System.out.println(myAgent.getName() + ": Recebi menos agua que o necessario para distribuir");
								}else if(getConsumoConsumidor() < qtdAguaDistribuir){
									//distribui a agua de forma de tal forma que os consumidores recebam o necessário

									float excedente = getConsumoConsumidor() - qtdAguaDistribuir;
									distribuiAgua(getNomeConsumidor(),getConsumoConsumidor());
									System.out.println(myAgent.getName() + ": Recebi mais agua que o necessario para distribuir no valor de "+excedente);
								}else{

									System.out.println(myAgent.getName() + ": Nao recebi agua!");
									distribuiAgua(getNomeConsumidor(),0);

								}
							}else
								block();

						}

					}
				});
	}
	
	public void informaGestorCrise(){
		//comportamento para informar o gasto para o gestor de crise caso encontre ele
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
								if(agenteID.equals("")){
									System.out.println("Gestor de crise nao foi encontrada!");
								}else{
									if(agenteID.equals("GestorDeCrise@Sabesp:1099/JADE"))
										setgestorCrise(agenteID);
								}
							}

							ACLMessage msgEnviada = new ACLMessage(ACLMessage.REQUEST);
							msgEnviada.setOntology("Gasto diario");
							msgEnviada.setLanguage("Portugues");
							msgEnviada.setSender(getgestorCrise());
							msgEnviada.setContent(String.valueOf(getConsumoConsumidor()));
							msgEnviada.setConversationId("Gasto diario");
							myAgent.send(msgEnviada);


						}	
						catch(FIPAException e){
							e.printStackTrace();
						}

					}
				});

		
	}

	
	public void setup(){
		//inicializacao do agente 
		System.out.println("Agente "+ this.getName()+" foi inicializado!");


		RegistraServico();
		
		procuraRepresa();
		
		trataMensagens();
		
		informaGestorCrise();

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


	public AID getgestorCrise() {
		return gestorCrise;
	}


	public void setgestorCrise(AID gestorCrise) {
		this.gestorCrise = gestorCrise;
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

	public int getP() {
		return p;
	}

	public void setP(int p) {
		this.p = p;
	}

	public int getQ() {
		return q;
	}

	public void setQ(int q) {
		this.q = q;
	}

	public int getK1() {
		return k1;
	}

	public void setK1(int k1) {
		this.k1 = k1;
	}

	public int getQ_esp() {
		return q_esp;
	}

	public void setQ_esp(int q_esp) {
		this.q_esp = q_esp;
	}



}
