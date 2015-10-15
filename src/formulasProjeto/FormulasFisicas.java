package formulasProjeto;

public class FormulasFisicas {
	
	//Variacao Diaria
	public float variacaoDiaria(float k1_max, float k1_med){
		float k1;
		k1 = k1_max/k1_med;
		return k1;
	}
	
	//Variacao Horaria
	public float variacaoHoraria(float k2_max, float k2_med){
		float k2;
		k2 = k2_max/k2_med;
		return k2;
	}
	
	//Estudo Demografico
	//p = variação pop. / p1 = pop penultimo censo / p2 = população do ultimo censo
	//t = tempo / t1 = ano penultimo censo / t2 = ano ultimo censo
	public float estudoDemografico(int p2, int ka, int t, int t2){
		float p;
		p = p2 + ka*(t-t2);
		return p;
	}
	
	//p = populacao área abastecida / q = consumo per capita
	//k1 = coef. dia maior consumo / k2 = coef. hora maior consumo
	//q_esp = vazao especifica grandes consumidores - industrias
	//cons_eta = consumo na ETA
	//Vazao Captacao - Estacao Elevatoria e Adutora ate ETA
	public float vazaoCaptacao(int p, float q, int k1, float q_esp, float cons_eta){
		float q1;
		q1 = (((k1*p*q)/86400) + q_esp) + cons_eta;
		return q1;
	}
	
	
	//Vazao ETA ate Reservatorio
	public float vazaoETA(int p, float q, int k1, float q_esp){
		float q2;
		q2 = ((k1*p*q)/86400) + q_esp;
		return q2;
	}
	
	
	//Vazao Reservatorio ate Rede
	public float vazaoReservatorio(int p, float q, int k1, int k2, float q_esp){
		float q3;
		q3 = ((k1*k2*p*q)/86400) + q_esp;
		return q3;
	}
		

}
