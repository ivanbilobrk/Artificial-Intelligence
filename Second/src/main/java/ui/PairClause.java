package ui;

import java.util.Objects;

/**
 * Razred koji pohranjuje par klauzula koje su razrijesene
 *
 */
public class PairClause {
	Clause c1, c2;

	public PairClause(Clause c1, Clause c2) {
		super();
		this.c1 = c1;
		this.c2 = c2;
	}

	//hashcode metoda jer cemo pohranjivati instance razreda u skup
	@Override
	public int hashCode() {
		return Objects.hash(c1, c2);
	}

	//equals metoda jer cemo usporedivati instance ovog razreda
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PairClause other = (PairClause) obj;
		return Objects.equals(c1, other.c1) && Objects.equals(c2, other.c2);
	}
	
}
