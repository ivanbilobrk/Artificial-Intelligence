package ui;

import java.util.TreeMap;

/*
 * Razred koji pohranjuje sve potrebne informacije o jednom čvoru ID3 stabla odluke
 */
public class Node {
	
	//label poprima ne null vrijednost samo ako je ovaj čvor list te tada se u label pohrani vrijednost ciljne varijable
	//feature je naziv oznake čvora
	private String label, feature;
	
	//djeca čvora gdje je ključ vrijednost oznake, a vrijednost uz taj ključ je novi čvor za kojeg vrijedi ta vrijednost 
	private TreeMap<String, Node> children;
	
	public Node(String label, String feature, TreeMap<String, Node> children) {
		this.children = children;
		this.label = label;
		this.feature = feature;
		this.children = children;
	}
	
	public TreeMap<String, Node> getChildren() {
		return children;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getFeature() {
		return feature;
	}

	public void setFeature(String feature) {
		this.feature = feature;
	}
	
	
	//rekurzivna metoda za ispis čvora i cijelog podstabla ispod njega
	public String print(int level, String current) {
	    StringBuilder sb = new StringBuilder();
	    if (this.label != null) {
	        sb.append(current).append(label).append("\n");
	    } else {
	        for (var x : children.entrySet()) {
	            Node child = x.getValue();
	            String childFeatures = feature + "=" + x.getKey() + " ";
	            sb.append(child.print(level + 1, current + level + ":" + childFeatures));
	        }
	    }
	    return sb.toString();
	}

	
}
