package Agentes;


import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

/**
 *
 * @author Erik
 */
public class AgenteRepresa extends Agent {

    private String nomeRepresa = new String();
    private String represaConectada = new String();
    private float capacidadeMaxima;
    private float nivelAtual;
    private AID posterior;

    public String getNomeRepresa() {
        return nomeRepresa;
    }

    public void setNomeRepresa(String nomeRepresa) {
        this.nomeRepresa = nomeRepresa;
    }

    public String getRepresaConectada() {
        return represaConectada;
    }

    public void setRepresaConectada(String represaConectada) {
        this.represaConectada = represaConectada;
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

    protected void setup() {

        System.out.println("O nome da represa é" + getAID().getLocalName());
        System.out.println("A capacidade maxima é" + capacidadeMaxima);
        System.out.println("O nivel atual é" + nivelAtual);

        //comportamento padrao - ciclico - para receber mensagem
        addBehaviour(new CyclicBehaviour(this) {

            @Override
            public void action() {

                ACLMessage msg = myAgent.receive();
                if (msg != null) {
                    //tratamento das mensagens
                    String conteudo = msg.getContent();
                    if (conteudo.equals("distribuirETA")) {
                         //aqui eh o tratametno de quando o agente represa recebe uma mensagem para distribuir agua para uma ETA
                        //se voce tem disponibilidade, precisa mandar agua para a ETA 
                        //enviar uma mensagem para a ETA 
                        //enviar outra mensagem para o distribuir falando que voce conseguiu fazer a transferencia

                        ACLMessage reply = msg.createReply();
                        reply.setContent("OK");
                        myAgent.send(reply);

                        ACLMessage envio = new ACLMessage(ACLMessage.INFORM);
                        envio.addReceiver(posterior);
                        envio.setContent("ADD 3000L");

                    }

                }
                block();

            }

        }
        );

    }

    protected void takeDown() {
        System.out.println("Agente da represa  " + getAID().getName() + "esta finalizado.");
    }

}
