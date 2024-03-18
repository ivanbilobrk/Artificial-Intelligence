package ui;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Random;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

public class Solution {

	public static void main(String ... args) throws IOException { 
		
		//učitavanje parametara
		String trainData = args[1].strip();
		String testData = args[3].strip();
		String netType = args[5].strip();
		List<Integer> layersSize = new ArrayList<>();
		int popSize = Integer.parseInt(args[7].strip());
		int elitism = Integer.parseInt(args[9].strip());
		double p = Double.parseDouble(args[11].strip());
		double deviation = Double.parseDouble(args[13].strip());
		int iteration = Integer.parseInt(args[15].strip());
		
		
		switch(netType) {
		case "5s": layersSize.add(5); break;
		case "20s": layersSize.add(20); break;
		case "5s5s": layersSize.add(5); layersSize.add(5); break;
		};
		
		List<String> lines = Files.readAllLines(Path.of(trainData));
		
		int numOfFeatures = lines.get(0).split(",").length -1;
		
		lines.remove(0);
		
		double[][] input = new double[lines.size()][numOfFeatures];
		double[][] output = new double[lines.size()][1];
		
		for(int i = 0; i < lines.size(); i++) {
			String[] featureValues = lines.get(i).split(",");
			
			for(int j = 0; j < numOfFeatures; j++) {
				input[i][j] = Double.parseDouble(featureValues[j]); 
			}
			
			output[i][0] = Double.parseDouble(featureValues[numOfFeatures]);
		}
		
		//matrice sa svim vrijednostima značajki i očekivanim izlazima
		RealMatrix inputMatrix = MatrixUtils.createRealMatrix(input);
		RealMatrix outputMatrix = MatrixUtils.createRealMatrix(output);
		
		//treeset koji sortira neuronske mreže prema njihovoj dobroti od najbolje prema najlošijoj
		Set<NeuralNet> pops = new TreeSet<>((n1, n2) -> {
			return -Double.compare(n1.getFitness(), n2.getFitness());
		});
		
		//inicijalno punjenje populacije sa neuronskim mrežama koje imaju slučajne težine
		for(int i = 0; i < popSize; i++) {
			pops.add(new NeuralNet(inputMatrix, outputMatrix, layersSize));
		}
		
		//prolazimo kroz zadani broj iteracija
		for(int i = 0; i < iteration; i++) {
			
			//ispis pogreške na podatcima za treniranje svakih 2000 iteracija
			int iterationCount = i+1;
			if(iterationCount % 2000 == 0 && iterationCount != 0) {
				
				System.out.println("[Train error @"+iterationCount+"]: "+pops.iterator().next().getMeanSquared());
			}
			
			//nova populacija neuronskih mreža
			Set<NeuralNet> popsTemp = new TreeSet<>((n1, n2) -> {
				return -Double.compare(n1.getFitness(), n2.getFitness());
			});
			
			//u novu populaciju dodajemo prvih nekoliko najboljih roditelja koliko je zadano predanim argumentom programa
			Iterator<NeuralNet> it = pops.iterator();
			
			for(int j = 0; j < elitism; j++) {
				popsTemp.add(it.next());
			}
			
			//popunjavamo ostatak nove populacije dok veličine ne bude jednaka zadanoj veličini populacije
			while(popsTemp.size() != popSize) {
				
				//biramo dva roditelja iz populacije koristeći proporcionalnu selekciju te ih onda križamo i dobiveno dijete dodajemo u novu populaciju
				
				boolean flag = false;
				int index1 = 0;
				
				//prvi roditelj iz populacije
				index1 = select(pops);
				NeuralNet netTemp1 = pops.stream().skip(index1).findFirst().orElse(null);
				
				NeuralNet netTemp2 = null;
				
				//vrtimo se u petlji dokle god ne odaberemo roditelja koji nije isti predhodno odabranom roditelju
				while(!flag) {
					int index2 = select(pops);
					netTemp2 = pops.stream().skip(index2).findFirst().orElse(null);
					
					if(index2 != index1) {
						flag = true;
					}
				}
				
				//krizamo roditelje i dijete (novu neuronsku mrežu) dodajemo u populaciju
				NeuralNet newNet = crossingMutation(netTemp1, netTemp2, p, deviation);
				
				//napravimo unaprijedni prolaz novodobivene mreže 
				newNet.passAllInputs();
				
				popsTemp.add(newNet);
				
			}
			//zamijenimo referencu populacije novostvorenom populacijom
			pops = popsTemp;
			
		}
		
		//učitavanje testnih podataka
		List<String> lines2 = Files.readAllLines(Path.of(testData));
		
		int numOfFeatures2 = lines2.get(0).split(",").length -1;
		
		lines2.remove(0);
		
		double[][] input2 = new double[lines2.size()][numOfFeatures2];
		double[][] output2 = new double[lines2.size()][1];
		
		for(int i = 0; i < lines2.size(); i++) {
			String[] featureValues = lines2.get(i).split(",");
			
			for(int j = 0; j < numOfFeatures2; j++) {
				input2[i][j] = Double.parseDouble(featureValues[j]); 
			}
			
			output2[i][0] = Double.parseDouble(featureValues[numOfFeatures2]);
		}
		
		RealMatrix inputMatrix2 = MatrixUtils.createRealMatrix(input2);
		RealMatrix outputMatrix2 = MatrixUtils.createRealMatrix(output2);
		
		//odabiremo prvu neuronsku mrežu iz zadnje populacije, ta će mreža ujedno biti i najbolja u zadnjoj populaciji jer koristimo treeset
		NeuralNet best = pops.iterator().next();
		
		//postavljamo input i output parametre
		best.setInput(inputMatrix2);
		best.setOutput(outputMatrix2);
		
		//napravimo unaprijedni prolaz najbolje mreže iz zadnje populacije
		best.passAllInputs();
		
		//ispisujemo grešku na testnim podatcima
		System.out.println("[Test error]: "+best.getMeanSquared());
		
		
	}
	
	//metoda koja ide po svim slojevima dviju mreža i poziva metode za križanje i mutaciju dviju neuronskih mreža
	public static NeuralNet crossingMutation(NeuralNet p1, NeuralNet p2, double p, double deviation) {
		
		//lista novih težina za svaki sloj mreže
		List<Layer> newLayers = new ArrayList<>();
		
		//vrtimo se po svim svim slojevima i za svaki sloj radimo križanje i mutaciju težina
		for(int i = 0; i < p1.getWeights().size(); i++) {
			
			//prvo radimo križanje običnih težina bez bias-a
			RealMatrix weights1 = p1.getWeights().get(i).getWeights();
			RealMatrix weights2 = p2.getWeights().get(i).getWeights();

			//mutiramo novodobivene težine trenutnog sloja
			RealMatrix newWeights = crossMutate(weights1, weights2, p, deviation);
			
			//križanje i mutacija bias-a
			RealMatrix bias1 = p1.getWeights().get(i).getBias();
			RealMatrix bias2 = p2.getWeights().get(i).getBias();
			
			RealMatrix newBias = crossMutate(bias1, bias2, p, deviation);
			
			//stvaranje novog sloja težina i dodavanje u listu
			Layer l = new Layer(newWeights, newBias);
			newLayers.add(l);
			
		}
		
		//stvaramo novu neuronsku mrežu s predhodno dobivenim slojevima
		NeuralNet net = new NeuralNet(newLayers);
		net.setInput(p1.getInput());
		net.setOutput(p1.getOutput());

		return net;
	}
	
	//metoda za križanje težina ili bias-a nekog sloja neuronske mreže
	public static RealMatrix crossMutate(RealMatrix weights1, RealMatrix weights2, double p, double deviation) {
		double[][] weightsArray1 = weights1.getData();
		double[][] weightsArray2 = weights2.getData();
		
		int rows = weights1.getRowDimension();
		int columns = weights1.getColumnDimension();
		
		double[][] weightsArrayNew = new double[rows][columns];
		
		//vrtimo se po svim redcima i stupcima dviju matrica i radimo aritemtičku sredinu pojedinih težina te svaku težinu mutiramo samo 
		//ako dobiveni slučajni broj je manji od zadane vjerojatnosti
		for(int j = 0; j < rows; j++) {
			for(int k = 0; k < columns; k++) {
				weightsArrayNew[j][k] = (weightsArray1[j][k] + weightsArray2[j][k])/2;
		        Random random = new Random();
		        double randomNumber = random.nextDouble();
		        
		        if(randomNumber <= p) {
		        	//mutiranje težine
		        	weightsArrayNew[j][k] += random.nextGaussian() * deviation;
		        }
				
			}
		}
		
		return MatrixUtils.createRealMatrix(weightsArrayNew);
	}

	//metoda koja radi proporcionalnu selekciju i vraća dobiveni index izabranog elementa
	public static int select(Set<NeuralNet> nets) {
		
		double totalFitness = 0;
		
		//posumiramo sve dobrote u populaciji
		for(var x: nets) {
			totalFitness += x.getFitness();
		}
		
		//generiramo slučajni broj između 0 i 1
        Random random = new Random();
        double randomNumber = random.nextDouble();
        
        double tempSum = 0;
        int index = 0;
        
        //prolazimo po svim mrežama i zbrajamo dobrote i trenutnu sumu dobrota dijelimo sa ukupnim zbrojem svih dobrota
        //kada dođemo do uvjeta da je taj broj veći od nasumične vrijednosti možemo vratiti index elementa
        for(var x: nets) {
        	tempSum += x.getFitness();
        	
        	if(tempSum/totalFitness >= randomNumber) {
        		return index;
        	}
        	
        	++index;
        	
        }
        return 0;
		
	}
	

}
