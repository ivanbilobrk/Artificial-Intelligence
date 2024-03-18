package ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealMatrixChangingVisitor;

public class NeuralNet {
	
	private RealMatrix input, output;
	private List<Layer> weights;
	private double meanSquaredError = 0;
	
	public NeuralNet(List<Layer> weights) {
		this.weights = weights;
	}
	
	public NeuralNet(RealMatrix input, RealMatrix output, List<Integer> layersSizes) {
		this.input = input;
		this.output = output;
		weights = new ArrayList<>();
		
		int prevOutputSize = input.getColumnDimension();
		
		for(int i = 0; i < layersSizes.size(); i++) {
			int layerSize = layersSizes.get(i);
			double[][] tempWeights = generateRandomArray(layerSize, prevOutputSize, 0.01);
			double[][] bias = generateRandomArray(layerSize, 1, 0.01);
			
	        RealMatrix tempWeightsMatrix = MatrixUtils.createRealMatrix(tempWeights);
	        RealMatrix biasMatrix = MatrixUtils.createRealMatrix(bias);
	        
	        Layer l = new Layer(tempWeightsMatrix, biasMatrix);
	        weights.add(l);
	        
	        prevOutputSize = layerSize;
			
		}
		
		double[][] tempWeights = generateRandomArray(1, prevOutputSize, 0.01);
		double[][] bias = generateRandomArray(1, 1, 0.01);
		
        RealMatrix tempWeightsMatrix = MatrixUtils.createRealMatrix(tempWeights);
        RealMatrix biasMatrix = MatrixUtils.createRealMatrix(bias);
		
        Layer l = new Layer(tempWeightsMatrix, biasMatrix);
        weights.add(l);
        
        passAllInputs();
	}
	
	
	
	public RealMatrix getInput() {
		return input;
	}



	public void setInput(RealMatrix input) {
		this.input = input;
	}



	public RealMatrix getOutput() {
		return output;
	}



	public void setOutput(RealMatrix output) {
		this.output = output;
	}



	public List<Layer> getWeights() {
		return weights;
	}

	public void setWeights(List<Layer> weights) {
		this.weights = weights;
	}

	public double getMeanSquared() {
		return meanSquaredError;
	}
	
	public double getFitness() {
		return 1/meanSquaredError;
	}
	
	public void passAllInputs() {
		int inputSize = input.getRowDimension();
		double sum = 0;
		
		for(int i = 0; i < inputSize; i++) {
			double result = forwardPass(input.getRowMatrix(i));
			//System.out.println(input.getRowMatrix(i)+" "+result);
			sum += Math.pow(result - output.getEntry(i, 0), 2);
			//System.out.println(result);
		}
		meanSquaredError = sum/inputSize;
		
	}
	
	public double forwardPass(RealMatrix input) {
		RealMatrix prevOutput = input.transpose();
		for(int i = 0; i < weights.size(); i++) {
			
			RealMatrix layerResult = weights.get(i).getWeights().multiply(prevOutput).add(weights.get(i).getBias());
			
			if(i != weights.size()-1) {
				layerResult.walkInColumnOrder(new RealMatrixChangingVisitor() {
					
					@Override
					public double visit(int row, int column, double value) {
						double newValue = 1/(1+ Math.pow(Math.E, -value));
						return newValue;
					}
					
					@Override
					public void start(int rows, int columns, int startRow, int endRow, int startColumn, int endColumn) {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public double end() {
						// TODO Auto-generated method stub
						return 0;
					}
				});
			}
			prevOutput = layerResult;
		}
		return prevOutput.getEntry(0, 0);
	}
	
    public static double[][] generateRandomArray(int rows, int cols, double stdDev) {
        double[][] array = new double[rows][cols];
        Random random = new Random();
        
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                array[i][j] = random.nextGaussian() * stdDev;
            }
        }
        
        return array;
    }
}
