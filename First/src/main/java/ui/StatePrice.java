package ui;

import java.util.Comparator;
import java.util.Objects;

/**
 * Razred koji je uredeni par imena stanja i cijene
 *
 */
public class StatePrice {
	
	//vlastiti komparator za usporedbu stanja po abecedi
	public static final Comparator<StatePrice> compareByAlphabet = (sp1, sp2)->sp1.getState().compareTo(sp2.getState());
	
	private String state;
	private double price;
	
	public StatePrice(String state, double price) {
		super();
		this.state = state;
		this.price = price;
	}
	public String getState() {
		return state;
	}
	public double getPrice() {
		return price;
	}

	//implementacija equals metode jer razred koristimo u kolekcijama koje zahtjevaju to, u suprotnom necemo dobiti zeljeno ponasanje
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		StatePrice other = (StatePrice) obj;
		return Objects.equals(state, other.state);
	}
	
	//implementacija hashCode metode jer razred koristimo u kolekcijama koje se temelje na hash tablicama
	@Override
	public int hashCode() {
		return Objects.hash(price, state);
	}
}
