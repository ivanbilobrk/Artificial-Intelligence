package ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealMatrixChangingVisitor;

/**
 * Razred koji predstavlja neuronsku mrežu
 *
 */
public class NeuralNet {
	
	//ulazne vrijednosti značajki i očkivan izlaz
	private RealMatrix input, output;
	//sve težine mreže po slojevima
	private List<Layer> weights;
	//pogreška na trenutnim ulaznim podatcima
	private double meanSquaredError = 0;
	
	//konstruktor za stvaranje mreže s unaprijed definiranim težinama
	//koristi se za stvaranje mreža kada smo težine dobili mutacijom težina roditelja u genetskom algoritmu
	public NeuralNet(List<Layer> weights) {
		this.weights = weights;
	}
	
	//konstruktor za stvaranje mreže s nasumično definiranim težinama u skladu s Gausovom razdiobom 
	public NeuralNet(RealMatrix input, RealMatrix output, List<Integer> layersSizes) {
		this.input = input;
		this.output = output;
		weights = new ArrayList<>();
		
		//početni broj stupaca matrice težina prvog sloja
		int prevOutputSize = input.getColumnDimension();
		
		//punjenje svih slojeva nasumičnim težinama
		for(int i = 0; i < layersSizes.size(); i++) {
			
			//broj redaka matrice težina trenutnog sloja
			int layerSize = layersSizes.get(i);
			
			//stvaranje nasumičnih 2D polja, a kasnije i matrica u skladu s Gausovom razdiobom 
			double[][] tempWeights = generateRandomArray(layerSize, prevOutputSize, 0.01);
			double[][] bias = generateRandomArray(layerSize, 1, 0.01);
			
	        RealMatrix tempWeightsMatrix = MatrixUtils.createRealMatrix(tempWeights);
	        RealMatrix biasMatrix = MatrixUtils.createRealMatrix(bias);
	        
	        //stvaranje novog sloja i dodavanje u listu
	        Layer l = new Layer(tempWeightsMatrix, biasMatrix);
	        weights.add(l);
	        
	        prevOutputSize = layerSize;
			
		}
		
		//generiranje zadnjeg sloja mreže i dodavanje u listu
		//zadnji sloj generiramo izvan petlje jer dimenzije matrica težina su različito definirane u odnosu na skrivene slojeve
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
	
	//funkcija dobrote koja je definirana kao recipročna vrijednost funkcije greške kako bi te dvije vrijednosti bile obrnuto proporcionalne
	public double getFitness() {
		return 1/meanSquaredError;
	}
	
	//funkcija koja za svaki redak ulaznih podataka mreže radi unaprijedni prolaz i računa ukupnu grešku 
	public void passAllInputs() {
		int inputSize = input.getRowDimension();
		double sum = 0;
		
		for(int i = 0; i < inputSize; i++) {
			double result = forwardPass(input.getRowMatrix(i));
			sum += Math.pow(result - output.getEntry(i, 0), 2);
		}
		meanSquaredError = sum/inputSize;
		
	}
	
	//funkcija koja radi unaprijedni prolaz za neke ulazne vrijednosti značajki
	public double forwardPass(RealMatrix input) {
		
		//moramo transponirati matricu zbog matričnog množenja kod računanja izlaza neurona
		RealMatrix prevOutput = input.transpose();
		
		//idemo unaprijedno kroz slojeve mreže
		for(int i = 0; i < weights.size(); i++) {
			
			//računamo izlaz iz neurona
			RealMatrix layerResult = weights.get(i).getWeights().multiply(prevOutput).add(weights.get(i).getBias());
			
			//ako nije riječ o zadnjem sloju onda morao primijeniti i step funkciju
			if(i != weights.size()-1) {
				layerResult.walkInColumnOrder(new RealMatrixChangingVisitor() {
					
					@Override
					public double visit(int row, int column, double value) {
						//primjena sigmoidalne step funkcije
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
			//mijenjamo referencu predhodnih težina tako da trenutni izlaz bude ulaz sljedećem sloju neurona
			prevOutput = layerResult;
		}
		//vraćamo izlaznu vrijednost neurona, uvijek je samo jedna
		return prevOutput.getEntry(0, 0);
	}
	
	//metoda za stvaranje 2D polja sa nasumičnim vrijednostima iz Gausove razdiobe
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
