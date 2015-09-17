package logica;

public class Main {

	public static int PESO_MAXIMO = 70;
	public static int LONGITUD_CROMOSOMA = 10;
	
	public static int PUNTO_DE_CORTE = 999;
	
	public static int[] pesos;
	public static int[] beneficios;
	
	public static boolean[] cromosoma;
	
	public static boolean[][] poblacion;
	
	public static void main(String[] args) {

		pesos = new int[]{30,20,22,10,7,15,18,25,22,28};
		beneficios = new int[]{100,75,80,40,20,30,35,80,60,85};
		
		cromosoma = generar();
		
		for(int i=0; i < PUNTO_DE_CORTE; i++){
			boolean[] nuevoCandidato = generar();
			
			if(performance(nuevoCandidato) > performance(cromosoma)){
				cromosoma = nuevoCandidato;
			}
			/*
			if(performance(cromosoma) >= 240){
				imprimir(cromosoma);
			}*/
		}
		
		imprimir(cromosoma);

	}
	
	public static boolean[] cruzar(boolean[] padre1, boolean[] padre2){
		return null;
	}
	
	public static boolean[] mutar(boolean[] original){
		boolean [] mutado = original.clone();
		
		//Invertir una posición al azar.
		int i = (int) Math.floor(Math.random()*LONGITUD_CROMOSOMA);
		mutado[i] = !mutado[i];
		
		return mutado;
	}
	
	public static void imprimir(boolean[] cromosoma){
		for(int i=0; i < LONGITUD_CROMOSOMA; i++){
			if(cromosoma[i]){
				System.out.print("1");
			}else{
				System.out.print("0");
			}
			
		}
		
		System.out.println(" Performance = "+performance(cromosoma));
	}
	
	public static boolean[] generar(){
		boolean [] cromosoma = new boolean[LONGITUD_CROMOSOMA];
		
		for(int i=0; i < LONGITUD_CROMOSOMA; i++){
			cromosoma[i] = Math.floor(Math.random()*2) == 1;
		}

		return cromosoma;
		
	}
	
	public static float performance(boolean[] cromosoma){
		
		int peso = 0;
		int beneficio = 0;
		
		for (int i=0; i< cromosoma.length; i++){
			if(cromosoma[i]){
				peso += pesos[i];
				beneficio += beneficios[i];
			}
		}
		
		if(peso > PESO_MAXIMO){
			/* Penalizar el beneficio 
			 * si se pasa del peso máximo.
			 */
			beneficio = 0; 
		}
		
		return beneficio/peso;
	}

}
