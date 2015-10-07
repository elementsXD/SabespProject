package agentes;


import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;




public class StartUp {

	static ContainerController containerController;
    static AgentController agentController;
	
	public static void main(String[] args) throws InterruptedException {
		// TODO Auto-generated method stub
		
        //iniciando main container
        startMainContainer("Sabesp", "7778");
        
      //adicionando agente RMA
        addAgent(containerController, "rma", "jade.tools.rma.rma", null);
        //adicionando o agente ETA
        addAgent(containerController, "ETA", "agentes.AgenteETA", null );
        //adicionando o agente Consumidor 
        addAgent(containerController, "Consumidor", "agentes.AgenteConsumidor", null );
        
      //adicionando o agente da Represa de Aguas Claras
        addAgent(containerController, "AguasClaras", "agentes.AgenteRepresa", null);
        
      //adicionando o agente da Represa de Cachoeira
        addAgent(containerController, "Cachoeira", "agentes.AgenteRepresa", null );
        //adicionando o agente da Represa de Jaguari
        addAgent(containerController, "Jaguari", "agentes.AgenteRepresa", null);
        //adicionando o agente da Represa de Atibainha
        addAgent(containerController, "Atibainha", "agentes.AgenteRepresa", null);
        //adicioando o agente da Represa de Juqueri
        addAgent(containerController, "Juqueri", "agentes.AgenteRepresa", null);
        
        
        addAgent(containerController, "Sniffer", "jade.tools.sniffer.Sniffer",
                new Object[]{"ESC","Consumidor","ETA","AguasClaras","Cachoeira","Jaguari","Atibainha","Juqueri"});
        
        /*
        //Adicionando os agentes para serem inicializados
        //adicionando o agente Rio
        addAgent(containerController, "Rio", "AgenteRio", null );
        //adicionando o agente Previsao do tempo
        addAgent(containerController, "PrevisaoDoTempo", "AgentePrevisaoDoTempo", null );
        
        //adicionando o agente Gestor de crise
        addAgent(containerController, "GestorDeCrise", "GestorDeCrise", null );
        */
        
        
	}


	public static void startMainContainer(String host, String port) {
        
		jade.core.Runtime runtime = jade.core.Runtime.instance();
        Profile profile = new ProfileImpl();
        profile.setParameter(Profile.MAIN_HOST, host);
        profile.setParameter(Profile.MAIN_PORT, port);

        containerController = runtime.createMainContainer(profile);
    }

    public static void addAgent(ContainerController cc, String agent, String classe, Object[] args) {
        try {
            agentController = cc.createNewAgent(agent, classe, args);
            agentController.start();
        } catch (StaleProxyException s) {
            s.printStackTrace();
        }
    }
}
