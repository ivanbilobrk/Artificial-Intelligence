package ui;
import java.util.Objects;

/**
 * Razred koji sluzi za pohranu informacija o cvorovima
 *
 */
public class Node implements Comparable<Node>{
	//referenca na roditeljski cvor kako bismo mogli rekonstruirati ispis
	private Node parent;
	//ime stanja
	private String state;
	//cijena puta do ovog cvora
	private double price;
	//dubina cvora
	private int depth;
	
	public Node(Node parent, String state, double price, int depth) {
		super();
		this.depth = depth;
		this.parent = parent;
		this.state = state;
		this.price = price;
	}
	
	//getteri za privatne varijable
	public int getDepth() {
		return depth;
	}
	
	public Node getParent() {
		return parent;
	}
	public String getState() {
		return state;
	}
	
	public double getPrice() {
		return price;
	}
	
	
	//setteri za privatne varijable
	public void setParent(Node parent) {
		this.parent = parent;
	}

	public void setState(String state) {
		this.state = state;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public void setDepth(int depth) {
		this.depth = depth;
	}

	//implementacija hashCode metode jer koristimo kolekcije koje se temelje na hash tablicama
	@Override
	public int hashCode() {
		return Objects.hash(state);
	}

	//equals metoda za provjeru radi li se o istom cvoru
	//treba nam zbog kolekcija koje koristimo u algoritmima
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Node other = (Node) obj;
		return Objects.equals(state, other.state);
	}
	
	/**
	 * Rekurzivna metoda za rekonstrukcija puta do cvora.
	 * @param sb - StringBuilder pomocu kojeg radimo formatiranje ispisa
	 * @param node - cvor koji je zavrsno stanje, tj. on ce se naci zadnji na ispisu
	 */
	public static void path(StringBuilder sb, Node node) {
		
		if(node.getParent() != null) {
			path(sb, node.parent);
			sb.append(" => ");
		} 
		
		sb.append(node.getState());
	}
	
	//metoda kojom implementiramo prirodni poredak cvorova u skladu s cijenom 
	//prirodni poredak nam treba zbog kolekcija koje sortiraju cvorove ovisno o njemu
	@Override
	public int compareTo(Node o) {
		if(this.price < o.price) {
			return -1;
		} else if(this.price > o.price) {
			return 1;
		} else {
			return this.state.compareTo(o.state);
		}
	}
	
	
}
