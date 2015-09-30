package logica;

import java.text.DecimalFormat;

public class Main {
	
	public static final int SELECCION_RULETA = 1;
	public static final int SELECCION_VENTANA = 2;
	
	public static final int REEMPLAZO_GENERACIONAL = 1;
	public static final int REEMPLAZO_ESTACIONARIO = 2;

	public static final int PESO_MAXIMO = 70;
	public static final int LONGITUD_CROMOSOMA = 10;
	public static final int TAMAÑO_POBLACION = 20;
	
	public static final double PROBABILIDAD_CRUCE = 0.90;
	public static final double PROBABILIDAD_MUTACION = 0.01;
	
	public static final int PUNTO_DE_CORTE = 9999;	
	public static final double FITNESS_SOLUCION_CONOCIDA = 3.7096;
	
	public static int[] pesos;
	public static int[] beneficios;
	
	public static boolean[][] poblacion;
	
	public static int[] ordenados;
	
	public static boolean solucionAlcanzada = false; //Se usa si la sabemos de antemano.
	public static long milisegundos; //Para calcular el tiempo de simulacion.
	
	public static void main(String[] args) {

		pesos = new int[]{30,20,22,10,7,15,18,25,22,28};
		beneficios = new int[]{100,75,80,40,20,30,35,80,60,85};
		poblacion = new boolean[TAMAÑO_POBLACION][LONGITUD_CROMOSOMA];
		
		milisegundos = System.currentTimeMillis();
		
		//Generar población
		for(int i=0; i < TAMAÑO_POBLACION; i++){
			//generar(true) partimos de soluciones factibles (acelera la convergencia).
			//generar(false) partimos de cromosomas aleatorios
			poblacion[i] = generar(false);
		}
		
		//Hasta llegar a un número predefinido de iteraciones.
		for(int i=0; i < PUNTO_DE_CORTE; i++){
			
			//REEMPLAZO_ESTACIONARIO, SELECCION_RULETA Converge más rápido (100 iteraciones promedio).
			//REEMPLAZO_GENERACIONAL, SELECCION_VENTANA Converge razonablemente (500 iteraciones promedio).
			//REEMPLAZO_ESTACIONARIO, SELECCION_VENTANA Varía bastante (1000 iteraciones promedio).
			//REEMPLAZO_GENERACIONAL, SELECCION_RULETA Peor (10000 iteraciones promedio).
			reemplazarGeneracion(REEMPLAZO_ESTACIONARIO, SELECCION_RULETA);
			
			//Cuando no se conoce la solución, el REEMPLAZO_ESTACIONARIO funciona mejor.
			//A veces el REEMPLAZO_GENERACIONAL degenera las soluciones para corridas largas.
			
			if(solucionAlcanzada){
				System.out.println("Optimo alcanzado en la iteración "+i+".");//" a los "+(System.currentTimeMillis() - milisegundos)+" milisegundos.");
				i = PUNTO_DE_CORTE;
				break;
			}
		}
		System.out.println("Población Final:\n");
		imprimir(poblacion);
		
		milisegundos = System.currentTimeMillis() - milisegundos;
		System.out.println("\nMilisegundos Transcurridos: "+milisegundos);

	}
	
	public static void reemplazarGeneracion(int tipoReemplazo, int tipoSeleccion){
		
		//imprimir(poblacion);

		int[] seleccion;
		switch(tipoSeleccion){
			case SELECCION_VENTANA:
				seleccion = seleccionVentana(poblacion);
				break;
			default:
				//SELECCION_RULETA:
				seleccion = seleccionMetodoRuleta(poblacion);
		}
		
		//Toma de a pares de cromosomas
		for(int j=0; j < TAMAÑO_POBLACION; j=j+2){ 
			
			int posicion1 = seleccion[j];
			int posicion2 = seleccion[j+1];                     

			boolean[] padre1 = poblacion[posicion1];
			boolean[] padre2 = poblacion[posicion2];
			boolean[] hijo1 = padre1.clone();
			boolean[] hijo2 = padre1.clone();
			
			/*
			System.out.println("Padres Seleccionados: "+posicion1+", "+posicion2);
			imprimir(padre1);
			imprimir(padre2);
			*/
			
			if(Math.random() <= PROBABILIDAD_CRUCE){
				//System.out.println("Se cruzaron.");
				cruzar(padre1, padre2, hijo1, hijo2);
			}
			
			if(Math.random() <= PROBABILIDAD_MUTACION){
				//System.out.println("Hijo 1 mutó.");
				mutar(hijo1);
			}
			
			if(Math.random() <= PROBABILIDAD_MUTACION){
				//System.out.println("Hijo 2 mutó.");
				mutar(hijo2);
			}
			/*
			System.out.println("Hijos:");
			imprimir(hijo1);
			imprimir(hijo2);
			*/
			switch(tipoReemplazo){
				case REEMPLAZO_ESTACIONARIO:
					
					//Optimizacion (20% más rápido), si usamos ventana ya los teniamos calculados.
					if(tipoSeleccion != SELECCION_VENTANA){
						ordenados = ordenarSegunFitness(poblacion);
					}
					
					int indicePeor1 = ordenados[TAMAÑO_POBLACION-1];
					int indicePeor2 = ordenados[TAMAÑO_POBLACION-2];
					
					//Remplazar por los individuos más débiles.
					poblacion[indicePeor1] = hijo1;
					poblacion[indicePeor2] = hijo2;
					
					j = TAMAÑO_POBLACION; //Romper el bucle externo, no se reemplaza toda la población.
					break;
				default:
					//REEMPLAZO_GENERACIONAL
					
					//Funciona mucho mejor si reemplazamos una posición random en vez de siempre los padres.
					//posicion1 = (int) Math.floor(Math.random()*TAMAÑO_POBLACION);
					//posicion2 = (int) Math.floor(Math.random()*TAMAÑO_POBLACION);
					
					//Reemplazar en la posición que ocupaban en la población anterior.
					poblacion[posicion1] = hijo1;
					poblacion[posicion2] = hijo2;
			}

		}
	}
	
	public static void cruzar(boolean[] padre1, boolean[] padre2, boolean[] hijo1, boolean[] hijo2){
		
		//Cruza de dos puntos
		int PUNTO_1 = 3;
		int PUNTO_2 = 6;
		
		for(int i = PUNTO_1; i<PUNTO_2 ; i++ ){ //Intercambiar los centros.
			hijo1[i] = padre2[i];
			hijo2[i] = padre1[i];
		}
		
	}
	
	public static void mutar(boolean[] original){
		
		//Invertir una posición al azar.
		int i = (int) Math.floor(Math.random()*LONGITUD_CROMOSOMA);
		original[i] = !original[i];
		
	}
	
	public static void imprimir(boolean[][] poblacion){
		
		//Imprimir población
		for(boolean[] cromosoma:poblacion){
			imprimir(cromosoma);
		}
		
	}
	
	public static void imprimir(boolean[] cromosoma){
		
		DecimalFormat df = new DecimalFormat("#.####");
		
		for(int i=0; i < LONGITUD_CROMOSOMA; i++){
			if(cromosoma[i]){
				System.out.print("1");
			}else{
				System.out.print("0");
			}
			
		}
		
		System.out.println(" Performance = "+df.format(fitness(cromosoma)));
	}
	
	public static boolean[] generar(boolean forzarFactibilidad){
		
		boolean [] cromosoma = new boolean[LONGITUD_CROMOSOMA];
		
		do{
			for(int i=0; i < LONGITUD_CROMOSOMA; i++){
				cromosoma[i] = Math.floor(Math.random()*2) == 1;
			}
		}while(factible(cromosoma) && forzarFactibilidad); //Arancamos con todas soluciones factibles?

		return cromosoma;
		
	}
		
	public static boolean factible(boolean[] cromosoma){
		int peso = 0;
		for (int i=0; i< cromosoma.length; i++){
			if(cromosoma[i]){
				peso += pesos[i];
				if(peso > PESO_MAXIMO){
					return true;
				}
			}
		}
		return false;
	}
	
	public static double fitness(boolean[] cromosoma){
		
		double peso = 0;
		double beneficio = 0;
		
		for (int i=0; i< cromosoma.length; i++){
			if(cromosoma[i]){
				peso += pesos[i];
				beneficio += beneficios[i];
			}
		}
		
		/* Esta penalización parece que degenera las soluciones en algunos casos.
		if(peso > PESO_MAXIMO){
			//Penalizar el beneficio si se pasa del peso máximo.
			 beneficio = 0; 
		}*/
		
		if(peso > PESO_MAXIMO){
			//Penalizar el peso si se pasa del peso máximo.
			 peso += 10; 
		}
		
		double fitness = beneficio/(1+peso);
		
		if(fitness >= FITNESS_SOLUCION_CONOCIDA){
			solucionAlcanzada = true;
		}
		
		return fitness;
	}
	
	public static int[] ordenarSegunFitness(boolean[][] poblacion){
		
		double[] performances = new double[TAMAÑO_POBLACION];
		int[] ordenados = new int[TAMAÑO_POBLACION];
		
		int aux;
		
		for(int i=0; i < TAMAÑO_POBLACION; i++){ // calcula los fitness individuales
			performances[i] = fitness(poblacion[i]);
		}
		
		for(int i=0; i < TAMAÑO_POBLACION; i++){ // ordena los indices de los cromosomas según el fitness de cada uno
			aux = 0;
			for(int j=0; j < TAMAÑO_POBLACION; j++){
				if(performances[j] > performances[aux])
					aux = j;
			}
			ordenados[i] = aux;
			performances[aux] = 0;
		}
		
		/*
		System.out.println("Ordenados: ");
		for(int e:ordenados){
			System.out.print(e+" ");
		}
		System.out.print("\n");
		*/
		
		return ordenados;
	}

	public static int[] seleccionMetodoRuleta(boolean[][] poblacion){ // devuelve un arreglo con los indices de los cromosomas en el orden en que se seleccionaron
		
		int[] seleccion = new int[TAMAÑO_POBLACION];
		
		double[] performances = new double[TAMAÑO_POBLACION];		
		double performanceTotal = 0;
		
		double[] pi = new double[TAMAÑO_POBLACION];
		double[] qi = new double[TAMAÑO_POBLACION];
		
		double aux;
		double r;
		
		for(int i=0; i < TAMAÑO_POBLACION; i++){ // calcula los fitness individuales y el total
			aux = fitness(poblacion[i]);
			performances[i] = aux;
			performanceTotal += aux;
		}
		
		aux = 0;
		
		for(int i=0; i < TAMAÑO_POBLACION; i++){ // calcula los p y q
			pi[i] = performances[i]/performanceTotal;
			aux += pi[i];
			qi[i] = aux;
		}
		
		for(int i=0; i < TAMAÑO_POBLACION; i++){ // selecciona los cromosomas
			r = Math.random();
			
			for(int j=0; j < TAMAÑO_POBLACION; j++){
				if(r <= qi[j]){
					seleccion[i] = j;
					j=TAMAÑO_POBLACION;
				}
			}			
		}		
		
		return seleccion;
	}
	
	public static int[] seleccionVentana(boolean[][] poblacion){ // devuelve un arreglo con los indices de los cromosomas en el orden en que se seleccionaron
		
		int[] seleccion = new int[TAMAÑO_POBLACION];
		ordenados = ordenarSegunFitness(poblacion);
		
		double r;
		for(int i=0; i < TAMAÑO_POBLACION; i++){ // selecciona los cromosomas
			r = Math.floor(Math.random()*(i + 1));			
			seleccion[i] = ordenados[(int) r];
		}		
		
		return seleccion;
	}
	
}
