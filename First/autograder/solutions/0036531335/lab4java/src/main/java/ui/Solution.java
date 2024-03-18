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
import org.apache.commons.math3.stat.descriptive.moment.Mean;

public class Solution {

	public static void main(String ... args) throws IOException { 
		
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
		
		RealMatrix inputMatrix = MatrixUtils.createRealMatrix(input);
		RealMatrix outputMatrix = MatrixUtils.createRealMatrix(output);
		
		Set<NeuralNet> pops = new TreeSet<>((n1, n2) -> {
			return -Double.compare(n1.getFitness(), n2.getFitness());
		});
		
		for(int i = 0; i < popSize; i++) {
			pops.add(new NeuralNet(inputMatrix, outputMatrix, layersSize));
		}
		
		for(int i = 0; i < iteration; i++) {
			
			int iterationCount = i+1;
			if(iterationCount % 2000 == 0 && iterationCount != 0) {
				
				System.out.println("[Train error @"+iterationCount+"]: "+pops.iterator().next().getMeanSquared());
			}
			
			Set<NeuralNet> popsTemp = new TreeSet<>((n1, n2) -> {
				return -Double.compare(n1.getFitness(), n2.getFitness());
			});
			
			Iterator<NeuralNet> it = pops.iterator();
			
			for(int j = 0; j < elitism; j++) {
				popsTemp.add(it.next());
			}
			

			while(popsTemp.size() != popSize) {
				
				boolean flag = false;
				int index1 = 0;
				
				
				index1 = select(pops);
				NeuralNet netTemp1 = pops.stream().skip(index1).findFirst().orElse(null);
				
				NeuralNet netTemp2 = null;
				
				while(!flag) {
					int index2 = select(pops);
					netTemp2 = pops.stream().skip(index2).findFirst().orElse(null);
					
					if(index2 != index1) {
						flag = true;
					}
				}
				NeuralNet newNet = crossingMutation(netTemp1, netTemp2, p, deviation);
				newNet.passAllInputs();
				
				
				popsTemp.add(newNet);
				
			}
			
			pops = popsTemp;
			
		}
		
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
		
		//System.out.println(outputMatrix2);
		
		NeuralNet best = pops.iterator().next();
		
		
		best.setInput(inputMatrix2);
		best.setOutput(outputMatrix2);
		best.passAllInputs();
		
		System.out.println("[Test error]: "+best.getMeanSquared());
		
		
	}
	
	public static NeuralNet crossingMutation(NeuralNet p1, NeuralNet p2, double p, double deviation) {
		
		List<Layer> newLayers = new ArrayList<>();
		
		for(int i = 0; i < p1.getWeights().size(); i++) {
			RealMatrix weights1 = p1.getWeights().get(i).getWeights();
			RealMatrix weights2 = p2.getWeights().get(i).getWeights();

			RealMatrix newWeights = crossMutate(weights1, weights2, p, deviation);
			
			RealMatrix bias1 = p1.getWeights().get(i).getBias();
			RealMatrix bias2 = p2.getWeights().get(i).getBias();
			
			RealMatrix newBias = crossMutate(bias1, bias2, p, deviation);
			
			Layer l = new Layer(newWeights, newBias);
			newLayers.add(l);
			
		}
		
		NeuralNet net = new NeuralNet(newLayers);
		net.setInput(p1.getInput());
		net.setOutput(p1.getOutput());

		return net;
	}
	
	public static RealMatrix crossMutate(RealMatrix weights1, RealMatrix weights2, double p, double deviation) {
		double[][] weightsArray1 = weights1.getData();
		double[][] weightsArray2 = weights2.getData();
		
		int rows = weights1.getRowDimension();
		int columns = weights1.getColumnDimension();
		
		double[][] weightsArrayNew = new double[rows][columns];
		
		for(int j = 0; j < rows; j++) {
			for(int k = 0; k < columns; k++) {
				weightsArrayNew[j][k] = (weightsArray1[j][k] + weightsArray2[j][k])/2;
		        Random random = new Random();
		        double randomNumber = random.nextDouble();
		        
		        if(randomNumber <= p) {
		        	weightsArrayNew[j][k] += random.nextGaussian() * deviation;
		        }
				
			}
		}
		
		return MatrixUtils.createRealMatrix(weightsArrayNew);
	}

	
	public static int select(Set<NeuralNet> nets) {
		
		double totalFitness = 0;
		
		for(var x: nets) {
			totalFitness += x.getFitness();
		}
		
        Random random = new Random();
        double randomNumber = random.nextDouble();
        
        double tempSum = 0;
        int index = 0;
        
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
