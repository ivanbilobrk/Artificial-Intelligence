package ui;

import java.util.Objects;

/**
 * Razred koji predstavlja jedan literal
 *
 */
public class Literal {
	//ime literala
	private String name;
	//zastavica je li literal negiran
	private boolean negation;
	
	public Literal(String name, boolean negation) {
		super();
		this.name = name;
		this.negation = negation;
	}

	//hashcode metoda jer dodajemo literal u skupove
	@Override
	public int hashCode() {
		return Objects.hash(name, negation);
	}

	//equals metoda za usporedbu dva literala jesu li isti
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Literal other = (Literal) obj;
		return Objects.equals(name, other.name) && negation == other.negation;
	}

	//getter i setteri za neke parametre razreda
	
	public String getName() {
		return name;
	}
	
	public void setNegation(boolean negation) {
		this.negation = negation;
	}

	public boolean isNegation() {
		return negation;
	}
	
}
