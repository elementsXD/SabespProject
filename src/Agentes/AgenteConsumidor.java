package Agentes;



import jade.core.Agent;

/**
 *
 * @author Erik
 */
public class AgenteConsumidor extends Agent{
    
    private String areaDistribuicao = new String();
    private float valorConsumir;
    
    protected double ConsumirAgua(double valorConsumir){
        double valorConsumido = 0.8;
        
        double result = valorConsumir * valorConsumido;
       
        return result;
       
    }

    
    protected void setup(){
        
    }
    
    
    protected void takeDown ( ) {
        System.out.println("Agente de consumo de água  " + getAID( ) .getName( ) + "esta finalizado ");
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
}