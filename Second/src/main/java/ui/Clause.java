package ui;

import java.util.Objects;
import java.util.Set;

/**
 * Razred koji pohranjuje informacije o jednoj klauzuli.
 *
 */
public class Clause {
	
	//kup literala koji cine klauzulu
	private Set<Literal> literals;
	//roditeljske klauzule
	private Clause parent1, parent2;
	
	public Clause(Set<Literal> literals, Clause parent1, Clause parent2) {
		super();
		this.literals = literals;
		this.parent1 = parent1;
		this.parent2 = parent2;
	}
	
	//getteri za parametre razreda
	
	public Set<Literal> getLiterals() {
		return literals;
	}
	public Clause getParent1() {
		return parent1;
	}
	
	public Clause getParent2() {
		return parent2;
	}

	//hashCode metoda zbog pohrane u skup koji se temelji na hash tablicama
	@Override
	public int hashCode() {
		return Objects.hash(literals);
	}

	//metoda equals za usporedbu klauzula
	//dvije klauzule su iste ako su im isti literali
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Clause other = (Clause) obj;
		return Objects.equals(literals, other.literals);
	}	
	
	//metoda za ispis klauzula
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		for(int i = 0; i < literals.size(); i++) {
			
			if(i != 0) {
				sb.append(" v ");
			}
			
			Literal temp = getForIndex(i);
			
			if(temp.isNegation()) {
				sb.append("~");
			}
			
			sb.append(temp.getName());
			
		}
		return sb.toString();
	}
	
	//dohvat literala na temelju indexa, nuzno jer literale pohranjujemo u skup koji nema metode za dohvat 
	//elemenata po indexu
	//izabrao sam pohranu u skup jer cesce radim provjeru je li se neka klauzula nalazi u skupu nego sto 
	//dohvacam elemente po indexu
	public Literal getForIndex(int i) {
		int j = 0;
		for(var x: literals) {
			if(j == i) {
				return x; 
			}
			++j;
		}
		return null;
	}
	
	//druga metoda za ispis klauzula na obrnuti nacin nego inace, tj. ukoliko je neki literal negiran u ispisu ce biti 
	//ne negiran
	//metoda se koristi primarno za ispis rezultata za polje conclusion gdje treba ispisati ne negirane klauzule cilja
	public String toString2() {
		StringBuilder sb = new StringBuilder();
		
		for(int i = 0; i < literals.size(); i++) {
			
			if(i != 0) {
				sb.append(" v ");
			}
			
			Literal temp = getForIndex(i);
			
			if(!temp.isNegation()) {
				sb.append("~");
			}
			
			sb.append(temp.getName());
			
		}
		return sb.toString();
	}
	
}
