package ui;

import org.apache.commons.math3.linear.RealMatrix;

/**
 * Razred koji pamti težine i bias jednog sloja neuronske mreže
 *
 */
public class Layer {
	private RealMatrix weights, bias;

	public Layer(RealMatrix weights, RealMatrix bias) {
		super();
		this.weights = weights;
		this.bias = bias;
	}

	public RealMatrix getWeights() {
		return weights;
	}

	public void setWeights(RealMatrix weights) {
		this.weights = weights;
	}

	public RealMatrix getBias() {
		return bias;
	}

	public void setBias(RealMatrix bias) {
		this.bias = bias;
	}
	
	
}
