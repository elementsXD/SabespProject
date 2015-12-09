package agentes;


import graficos.GraficoNivelPluviometria;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.AMSService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.lang.acl.ACLMessage;
import repository.*;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Point;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.ListIterator;

import org.jfree.ui.RefineryUtilities;

@SuppressWarnings("serial")
public class AgentePrevisaoDoTempo extends Agent{

	//<--Variaveis de pluviometria-->
	private String data;
	private String manancial;
	private String pluviometria;
	final static long tempfornecimento = 10000;
	final static int qtdDiasExecutar = 1000;
	private GraficoNivelPluviometria graph_pluv = new GraficoNivelPluviometria("Pluviometria");
	//<-- Termino das variaveis de pluviometria -->
	
	//<--Variaveis das flutuacoes das represas-->
	private String jaguari_qnat;
	private String jaguari_qjus;
	private String jaguari_vop;
	private String cachoeira_qnat;
	private String cachoeira_qjus;
	private String cachoeira_vop;
	private String atibainha_qnat;
	private String atibainha_qjus;
	private String atibainha_vop;
	private String paivac_qnat;
	private String paivac_qjus;
	private String paivac_vop;
	private String qesi;
	//<--Termino das variaveis das flutuacoes das represas-->
	
	//<--Variaveis relacionadas aos dados das represas-->
	private AID atibainha;
	private AID cachoeira;
	private AID jaguari;
	private AID paivac;
	private float fluxoAguaAtibainha;
	private float fluxoAguaCachoeira;
	private float fluxoAguaJaguari;
	private float fluxoPaivaCastro;
	//<--Termino das variaveis relacionadas aos dados das represas-->
	
	//<--Variaveis relacionadas ao calendário-->
		Calendar c = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-d");
		SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd");
	//<--Termino das variaveis relacionadas ao calendário-->
		
		
	/** Metodo criado para inicializar parâmetro de data de início da simulação */
		public void iniciaData(){

			//Efetua a inicialização da variável de data da Simulação
			addBehaviour(new OneShotBehaviour() {

				@Override
				public void action() {
					c.set(2014, Calendar.JANUARY, 01);
					
				};
			});
		}

		
		/**Metodos de Calculo de Envio agua*/
		public void envioAgua() {

			addBehaviour(new TickerBehaviour(this, tempfornecimento) {

				@Override
				protected void onTick() {
					// TODO Auto-generated method stub
					if (getTickCount() > qtdDiasExecutar){

						stop();
					}else{

						AgentePrevisaoDoTempo previsao = new AgentePrevisaoDoTempo();
						AgentePrevisaoDoTempo rio = new AgentePrevisaoDoTempo();
						float area_cantareira = (float) 86100000.00; //m²;
						previsao = CarregarVariaveis_Mananciais(sdf.format(c.getTime()));
						rio = CarregarVariaveis_Rios(sdf2.format(c.getTime()));
						float chuva = Float.parseFloat(previsao.getPluviometria());
						graph_pluv.pluvUpdate(chuva);
						//Formula para calcular chuva em volume
						float pluviometria = (chuva*area_cantareira)/1000; //m³;

						//Informações Jaguari-Jacareí
						float rio_jacarei_entra = (Float.parseFloat(rio.getJaguari_qnat())*86400);
						float rio_jacarei_sai = (Float.parseFloat(rio.getJaguari_qjus())*86400);
						float chuva_jacarei = (float) (pluviometria * 0.57); //80% do total do sistema
						float jacarei_es = rio_jacarei_entra + chuva_jacarei - rio_jacarei_sai;
						setFluxoAguaJaguari(jacarei_es);
						
						//Informações Cachoeira
						float rio_cachoeira_entra = (Float.parseFloat(rio.getCachoeira_qnat())*86400);
						float rio_cachoeira_sai = (Float.parseFloat(rio.getCachoeira_qjus())*86400);
						float chuva_cachoeira = (float) (pluviometria * 0.1); //7,9% do total do sistema
						float cachoeira_es = rio_cachoeira_entra + chuva_cachoeira - rio_cachoeira_sai;
						setFluxoAguaCachoeira(cachoeira_es);
						
						//Informações Atibainha
						float rio_atibainha_entra = (Float.parseFloat(rio.getAtibainha_qnat())*86400);
						float rio_atibainha_sai = (Float.parseFloat(rio.getAtibainha_qjus())*86400);
						float chuva_atibainha = (float) (pluviometria * 0.26); //11% do total do sistema
						float atibainha_es = rio_atibainha_entra + chuva_atibainha - rio_atibainha_sai;
						setFluxoAguaAtibainha(atibainha_es);
						
						//Informações PaivaCastro
						float rio_paivacastro_entra = (Float.parseFloat(rio.getPaivac_qnat())*86400);
						float rio_paivacastro_sai = (Float.parseFloat(rio.getPaivac_qjus())*86400);
						float chuva_paivacastro = (float) (pluviometria * 0.07); //1,1% do total do sistema
						float paivacastro_es = rio_paivacastro_entra + chuva_paivacastro - rio_paivacastro_sai;
						setFluxoPaivaCastro(paivacastro_es);
						
						c.add(Calendar.DATE, 1);

					}
				}
			});	
		}	
		
		/**Metodos de Calculo de Envio agua*/
		public static AgentePrevisaoDoTempo CarregarVariaveis_Mananciais(String data_dia) {
			AgentePrevisaoDoTempo previsao = new AgentePrevisaoDoTempo();
			String data_hoje = "\"" + data_dia + "\"";
			//A estrutura try-catch é usada pois o objeto BufferedWriter exige que as
			//excessões sejam tratadas
			try {

				//Criação de um buffer para a ler de uma stream
				BufferedReader StrR = new BufferedReader(new FileReader("Dados_Mananciais.csv"));
				String Str;
				while((Str = StrR.readLine())!= null){
					String[] TableLine = Str.split(",");
					if(TableLine[0].equalsIgnoreCase(data_hoje)){
						if(TableLine[1].equalsIgnoreCase("\"sistemaCantareira\"")){
							previsao.setData(TableLine[0]);		
							previsao.setManancial(TableLine[1]);
							previsao.setPluviometria(TableLine[3]);
							previsao.setData(previsao.getData().replace("\"", ""));
							previsao.setManancial(previsao.getManancial().replace("\"", ""));
							previsao.setPluviometria(previsao.getPluviometria().replace("\"", ""));
							
							
							return previsao;
						}
					}
				}
				
				//Fechamos o buffer
				StrR.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException ex){
				ex.printStackTrace();
			}
			
			return previsao;
		}
		
		/**Metodos de Calculo de Envio agua*/
		public static AgentePrevisaoDoTempo CarregarVariaveis_Rios(String data_dia) {
			AgentePrevisaoDoTempo rio = new AgentePrevisaoDoTempo();
			try {

				//Criação de um buffer para a ler de uma stream
				BufferedReader StrR = new BufferedReader(new FileReader("Dados_VazaoES.csv"));
				String Str;
				while((Str = StrR.readLine())!= null){
					String[] TableLine = Str.split(",");
					if(TableLine[0].equalsIgnoreCase(data_dia)){
						rio.setData(TableLine[0]);
						rio.setJaguari_qnat(TableLine[1]);
						rio.setJaguari_qjus(TableLine[2]);
						rio.setJaguari_vop(TableLine[3]);
						rio.setCachoeira_qnat(TableLine[4]);
						rio.setCachoeira_qjus(TableLine[5]);
						rio.setCachoeira_vop(TableLine[6]);
						rio.setAtibainha_qnat(TableLine[7]);
						rio.setAtibainha_qjus(TableLine[8]);
						rio.setAtibainha_vop(TableLine[9]);
						rio.setPaivac_qnat(TableLine[10]);
						rio.setPaivac_qjus(TableLine[11]);
						rio.setPaivac_vop(TableLine[12]);
						rio.setQesi(TableLine[13]);
						rio.setData(rio.getData().replace("\"", ""));
						rio.setJaguari_qnat(rio.getJaguari_qnat().replace("\"", ""));
						rio.setJaguari_qjus(rio.getJaguari_qjus().replace("\"", ""));
						rio.setJaguari_vop(rio.getJaguari_vop().replace("\"", ""));
						rio.setCachoeira_qnat(rio.getCachoeira_qnat().replace("\"", ""));
						rio.setCachoeira_qjus(rio.getCachoeira_qjus().replace("\"", ""));
						rio.setCachoeira_vop(rio.getCachoeira_vop().replace("\"", ""));
						rio.setAtibainha_qnat(rio.getAtibainha_qnat().replace("\"", ""));
						rio.setAtibainha_qjus(rio.getAtibainha_qjus().replace("\"", ""));
						rio.setAtibainha_vop(rio.getAtibainha_vop().replace("\"", ""));
						rio.setPaivac_qnat(rio.getPaivac_qnat().replace("\"", ""));
						rio.setPaivac_qjus(rio.getPaivac_qjus().replace("\"", ""));
						rio.setPaivac_vop(rio.getPaivac_vop().replace("\"", ""));
						rio.setQesi(rio.getQesi().replace("\"", ""));
						
						
						
						return rio;
					}
				}
				
				//Fechamos o buffer
				StrR.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException ex){
				ex.printStackTrace();
			}
			return rio;
			
			
		}

		public void procuraRepresas(){
			
			addBehaviour(new OneShotBehaviour(this) {
				
				@Override
				public void action() {
					// TODO Auto-generated method stub
					graph_pluv.setLocation(new Point(0,0));
					graph_pluv.setPreferredSize(new Dimension(675, 375));
					graph_pluv.geraGrafico("teste", 1);
					AMSAgentDescription[] agentes = null;
					SearchConstraints c = new SearchConstraints();
					c.setMaxResults(new Long(-1));
					
					System.out.println(myAgent.getName() + ": Iniciando a procura pelas represas na plataforma.");
					
					try{
						
						agentes = AMSService.search(myAgent, new AMSAgentDescription(), c);
						
						
						for(int i = 0; i<agentes.length;i++){
							AID agenteID = agentes[i].getName();
							if(agenteID.equals("Atibainha@NOTE-MARCUS:7778/JADE")){
								setAtibainha(agenteID);
								System.out.println(myAgent.getName() + ": Encontrei o agente da represa " + getAtibainha());
							}else if(agenteID.equals("Cachoeira@NOTE-MARCUS:7778/JADE")){
								setCachoeira(agenteID);
								System.out.println(myAgent.getName() + ": Encontrei o agente da represa " + getCachoeira());
							}else if(agenteID.equals("Jaguari@NOTE-MARCUS:7778/JADE")){
								setJaguari(agenteID);
								System.out.println(myAgent.getName() + ": Encontrei o agente da represa " + getJaguari());
							}else if(agenteID.equals("PaivaCastro@NOTE-MARCUS:7778/JADE")){
								setPaivac(agenteID);
								System.out.println(myAgent.getName() + ": Encontrei o agente da represa " + getPaivac());
							}
							
							
						}
					    	    
					}	
				   	catch(FIPAException e){
				   		e.printStackTrace();
				   	}
					
				}
			});
			
		}
		
		public void forneceAguaPaivaCastro(){
			//Fornecimento de agua para PaivaCastro
			addBehaviour(new TickerBehaviour(this, tempfornecimento) {
				
				@Override
				protected void onTick() {
					// TODO Auto-generated method stub
				
					if (getTickCount() > qtdDiasExecutar){
						
						stop();
					}else{
					
					ACLMessage msgEnviada = new ACLMessage(ACLMessage.INFORM);
			  	    msgEnviada.setOntology("Fornecimento rio");
		    	    msgEnviada.setLanguage("Portugues");
		    	    msgEnviada.addReceiver(getPaivac());
		     	    msgEnviada.setContent(String.valueOf(getFluxoPaivaCastro()));
			 	    msgEnviada.setConversationId("Fornecimento rio");
			 	    myAgent.send(msgEnviada);
					}
					
				}
			});
		}
		
		public void forneceAguaAtibainha(){
			
			
			//Fornecimento de agua para o Atibainha
			addBehaviour(new TickerBehaviour(this, tempfornecimento) {

				@Override
				protected void onTick() {
					// TODO Auto-generated method stub

					if (getTickCount() > qtdDiasExecutar){

						stop();
					}else{

						ACLMessage msgEnviada = new ACLMessage(ACLMessage.INFORM);
						msgEnviada.setOntology("Fornecimento rio");
						msgEnviada.setLanguage("Portugues");
						msgEnviada.addReceiver(getAtibainha());
						msgEnviada.setContent(String.valueOf(getFluxoAguaAtibainha()));
						msgEnviada.setConversationId("Fornecimento rio");
						myAgent.send(msgEnviada);

					}

				}
			});

		}

		public void forneceAguaCachoeirinha(){

			//Fornecimento de agua para o Cachoeirinha
			addBehaviour(new TickerBehaviour(this, tempfornecimento) {

				@Override
				protected void onTick() {
					// TODO Auto-generated method stub

					if (getTickCount() > qtdDiasExecutar){

						stop();
					}else{

						ACLMessage msgEnviada = new ACLMessage(ACLMessage.INFORM);
						msgEnviada.setOntology("Fornecimento rio");
						msgEnviada.setLanguage("Portugues");
						msgEnviada.addReceiver(getCachoeira());
						msgEnviada.setContent(String.valueOf(getFluxoAguaCachoeira()));
						msgEnviada.setConversationId("Fornecimento rio");
						myAgent.send(msgEnviada);

					}
				}
			});
		}

		public void forneceAguaJaguari(){

			//Fornecimento de agua para Jaguari
			addBehaviour(new TickerBehaviour(this, tempfornecimento) {

				@Override
				protected void onTick() {
					// TODO Auto-generated method stub

					if (getTickCount() > qtdDiasExecutar){

						stop();
					}else{

						ACLMessage msgEnviada = new ACLMessage(ACLMessage.INFORM);
						msgEnviada.setOntology("Fornecimento rio");
						msgEnviada.setLanguage("Portugues");
						msgEnviada.addReceiver(getJaguari());
						msgEnviada.setContent(String.valueOf(getFluxoAguaJaguari()));
						msgEnviada.setConversationId("Fornecimento rio");
						myAgent.send(msgEnviada);

					}

				}
			});

		}
		
		protected void setup(){
			
			
			//Download_DadosMananciais downl_mananc = new Download_DadosMananciais();
			//downl_mananc.Download_DadosMananciais();
			
			//Download_DadosVazaoES downl_vazao = new Download_DadosVazaoES();
			//downl_vazao.Download_DadosVazaoES();
			
			iniciaData();
			
			System.out.println(this.getName() + ": Agente que representa os rios foi inicializado!");
			
			procuraRepresas();
			
			System.out.println(this.getName() + ": Iniciando o fornecimento de agua para as Represas Jaguari, Cachoeira, Atatibainha e Paiva Castro.");
			
			envioAgua();
			
			forneceAguaJaguari();
			
			forneceAguaCachoeirinha();
			
			forneceAguaAtibainha();
			
			forneceAguaPaivaCastro();
			
			
					
		}
		
		//<--Getters e Setters das variaveis de pluviometria-->
		public String getData() {
			return data;
		}
		public void setData(String data) {
			this.data = data;
		}
		public String getManancial() {
			return manancial;
		}
		public void setManancial(String manancial) {
			this.manancial = manancial;
		}
		public String getPluviometria() {
			return pluviometria;
		}
		public void setPluviometria(String pluviometria) {
			this.pluviometria = pluviometria;
		}
		//<--Termino dos getters e setters das variaveis de pluviometria-->
		
		
		//<--Getters e Setters das variaveis das flutuacoes das represas-->
		public String getJaguari_qnat() {
			return jaguari_qnat;
		}
		public void setJaguari_qnat(String jaguari_qnat) {
			this.jaguari_qnat = jaguari_qnat;
		}
		public String getJaguari_qjus() {
			return jaguari_qjus;
		}
		public void setJaguari_qjus(String jaguari_qjus) {
			this.jaguari_qjus = jaguari_qjus;
		}
		public String getJaguari_vop() {
			return jaguari_vop;
		}
		public void setJaguari_vop(String jaguari_vop) {
			this.jaguari_vop = jaguari_vop;
		}
		public String getCachoeira_qnat() {
			return cachoeira_qnat;
		}
		public void setCachoeira_qnat(String cachoeira_qnat) {
			this.cachoeira_qnat = cachoeira_qnat;
		}
		public String getCachoeira_qjus() {
			return cachoeira_qjus;
		}
		public void setCachoeira_qjus(String cachoeira_qjus) {
			this.cachoeira_qjus = cachoeira_qjus;
		}
		public String getCachoeira_vop() {
			return cachoeira_vop;
		}
		public void setCachoeira_vop(String cachoeira_vop) {
			this.cachoeira_vop = cachoeira_vop;
		}
		public String getAtibainha_qnat() {
			return atibainha_qnat;
		}
		public void setAtibainha_qnat(String atibainha_qnat) {
			this.atibainha_qnat = atibainha_qnat;
		}
		public String getAtibainha_qjus() {
			return atibainha_qjus;
		}
		public void setAtibainha_qjus(String atibainha_qjus) {
			this.atibainha_qjus = atibainha_qjus;
		}
		public String getAtibainha_vop() {
			return atibainha_vop;
		}
		public void setAtibainha_vop(String atibainha_vop) {
			this.atibainha_vop = atibainha_vop;
		}
		public String getPaivac_qnat() {
			return paivac_qnat;
		}
		public void setPaivac_qnat(String paivac_qnat) {
			this.paivac_qnat = paivac_qnat;
		}
		public String getPaivac_qjus() {
			return paivac_qjus;
		}
		public void setPaivac_qjus(String paivac_qjus) {
			this.paivac_qjus = paivac_qjus;
		}
		public String getPaivac_vop() {
			return paivac_vop;
		}
		public void setPaivac_vop(String paivac_vop) {
			this.paivac_vop = paivac_vop;
		}
		public String getQesi() {
			return qesi;
		}
		public void setQesi(String qesi) {
			this.qesi = qesi;
		}
		//<--Termino dos getters e setters das variaveis das flutuacoes das represas-->
		
		
		//<--Getters e setters relacionados as variaveis relacionadas aos dados das represas-->
		public AID getAtibainha() {
			return atibainha;
		}
		public void setAtibainha(AID atibainha) {
			this.atibainha = atibainha;
		}
		public AID getCachoeira() {
			return cachoeira;
		}
		public void setCachoeira(AID cachoeira) {
			this.cachoeira = cachoeira;
		}
		public AID getJaguari() {
			return jaguari;
		}
		public void setJaguari(AID jaguari) {
			this.jaguari = jaguari;
		}	
		public AID getPaivac() {
			return paivac;
		}
		public void setPaivac(AID paivac) {
			this.paivac = paivac;
		}
		
		public float getFluxoAguaAtibainha() {
			return fluxoAguaAtibainha;
		}
		public void setFluxoAguaAtibainha(float fluxoAguaAtibainha) {
			this.fluxoAguaAtibainha = fluxoAguaAtibainha;
		}
		public float getFluxoAguaCachoeira() {
			return fluxoAguaCachoeira;
		}
		public void setFluxoAguaCachoeira(float fluxoAguaCachoeira) {
			this.fluxoAguaCachoeira = fluxoAguaCachoeira;
		}
		public float getFluxoAguaJaguari() {
			return fluxoAguaJaguari;
		}
		public void setFluxoAguaJaguari(float fluxoAguaJaguari) {
			this.fluxoAguaJaguari = fluxoAguaJaguari;
		}
		public float getFluxoPaivaCastro() {
			return fluxoPaivaCastro;
		}
		public void setFluxoPaivaCastro(float fluxoPaivaCastro) {
			this.fluxoPaivaCastro = fluxoPaivaCastro;
		}
		//<--Termino dos getters e setters relacionados as variaveis relacionadas aos dados das represas-->
		

}
