package ui;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.HashSet;

public class Solution {

	public static void main(String ... args) throws IOException {
		
		/*
		 * Korišteni izvori: [Skripta] Marko Čupić. Pretraživanje prostora stanja, prezentacije s predavanja
		 */
		
		//parsiranje ualznih argumenata
		String alg = "";
		String pathStates = "";
		String pathHeuristic = "";
		boolean checkOptimistic = false, checkConsistent = false;
		
		int len = args.length;
		int i;
		for(i = 0; i < len; i++) {
			switch(args[i]) {
			case "--alg": 
				alg = args[++i];
				break;
			case "--ss": 
				pathStates = args[++i];
				break;
			case "--h":
				pathHeuristic = args[++i];
				break;
			case "--check-optimistic":
				checkOptimistic = true;
				break;
			case "--check-consistent":
				checkConsistent = true;
				break;
			}
		}
		
		//mapa u kojoj je ključ ime stanja, a vrijednost kolekcija (set) koja ima susjede od tog stanja te cijenu da se 
		//dode do njih
		Map<String, Set<StatePrice>> succ = new HashMap<>();
		
		//završna stanja (goal states)
		Set<String> finalStates = new HashSet<>();
		
		String firstState = Loader.loadStates(Path.of(pathStates), succ, finalStates);
		
		//zavrsni cvor pretrage
		Node result = new Node(null, null, 0, 0);
		
		//kolekcija posjecenih stanja
		Set<String> visited = new HashSet<>();
		
		StringBuilder sb = new StringBuilder();
		long numVisit = 0L;
		
		//provjera koju pretragu moramo obaviti
		switch(alg) {
		case "bfs":
			numVisit = breadthFirstSearch(result, firstState, finalStates, succ, visited);
			sb.append("# BFS\n");
			break;
		case "ucs":
			numVisit = unifiedCostSearch(result, firstState, finalStates, succ, visited);
			sb.append("# UCS\n");
			break;
		case "astar":
			Map<String, Double> heuristic = new HashMap<>();
			Set<StatePrice> visited2 = new HashSet<>();
			Loader.loadHeuristic(Path.of(pathHeuristic), heuristic);
			numVisit = aStarSearch(result, firstState, finalStates, succ, visited2, heuristic);
			sb.append("# A-STAR "+pathHeuristic+"\n");
			break;
			
		}
		
		//ukoliko smo trebali provjeriti konzistentnost, optimisticnost ili samo obaviti pretragu, 
		//imamo drugaciji ispis
		if(checkConsistent) {
			sb.append("# HEURISTIC-CONSISTENT "+pathHeuristic+"\n");
			Map<String, Double> heuristic = new HashMap<>();
			Loader.loadHeuristic(Path.of(pathHeuristic), heuristic);
			checkConsistent(succ, heuristic, sb);
			System.out.println(sb.toString());
		} else if(checkOptimistic) {
			sb.append("# HEURISTIC-OPTIMISTIC "+pathHeuristic+"\n");
			Map<String, Double> heuristic = new HashMap<>();
			Loader.loadHeuristic(Path.of(pathHeuristic), heuristic);
			checkOptimistic(succ, heuristic, sb, finalStates);
			System.out.println(sb.toString());
		} else {
			sb.append("[FOUND_SOLUTION]: ");
			if(result != null && finalStates.contains(result.getState())) {
				sb.append("yes\n");
			} else {
				sb.append("no\n");
			}
			
			sb.append("[STATES_VISITED]: "+numVisit+"\n");
			int pathLength = result.getDepth()+1;
			sb.append("[PATH_LENGTH]: "+pathLength+"\n");
			sb.append("[TOTAL_COST]: "+result.getPrice()+"\n");
			StringBuilder sb2 = new StringBuilder();
			Node.path(sb2, result);
			sb.append("[PATH]: "+sb2.toString()+"\n");
			System.out.println(sb.toString());
		}
	}
	
	/**
	 * Metoda za provjeru optimisticnosti heuristike.
	 * Vremenska i prostorna slozenost ove metode je O(broj stanja * slozenost UCS algoritma)
	 * @param succ - mapa prijelaza 
	 * @param heuristic - vrijednosti heuristike za pojedina stanja
	 * @param sb - StringBuilder za formatiranje ispisa
	 * @param finalStates - zavrsno stanje koje ce nam trebati za racunanje stvarne cijene puta izmedu nekog stanja i zavrsnog stanja
	 */
	public static void checkOptimistic(Map<String, Set<StatePrice>> succ, Map<String, Double> heuristic, StringBuilder sb, Set<String> finalStates) {
		//zastavica o kojoj nam ovisi ispis
		boolean flag = true;
		//prolazimo sva stanja
		for(var x: succ.keySet()) {
			//za svako stanje radimo pretrazivanje jednolikom cijenom kako bi nasli stvarnu cijenu do ciljnog stanja
			//dobivenu cijenu usporedujemo sa heuristikom i ako je stvarna cijena >= heuristika onda je heuristika u redu
			//u suprotnom heuristika precjenjuje te nije optimisticna
			double h1 = heuristic.get(x);
			Set<String> visited = new HashSet<>();
			Node result = new Node(null, null, 0, 0);
			unifiedCostSearch(result, x, finalStates, succ, visited);
			double realPrice = result.getPrice();
			if(realPrice >= h1) {
				sb.append("[CONDITION]: [OK] ");
			} else {
				flag = false;
				sb.append("[CONDITION]: [ERR] ");
			}
			sb.append("h("+x+") <= h*: "+h1+" <= "+realPrice+"\n");
		}
		
		//ovisno o zastavici formatiramo ispis
		if(flag) {
			sb.append("[CONCLUSION]: Heuristic is optimistic.");
		} else {
			sb.append("[CONCLUSION]: Heuristic is not optimistic.");
		}
	}
	
	/**
	 * Metoda provjere konzistentnosti heuristike.
	 * Vremenska slozenost = O(broj stanja * broj sljedbenika)
	 * Prostorna slozenost = O(1)
	 * @param succ - mapa prijelaza 
	 * @param heuristic - vrijednosti heuristike za pojedina stanja
	 * @param sb - StringBuilder za formatiranje ispisa
	 */
	public static void checkConsistent(Map<String, Set<StatePrice>> succ, Map<String, Double> heuristic, StringBuilder sb) {
		//zastavica za formatiranje ispisa
		boolean flag = true;
		
		//prolazimo sva stanja i za ta stanja njihove sljedbenike te usporedujemo heuristike
		//ako je vrijednost heuristike stanja veca od heuristike stanja sljedbenika uvecane za cijenu prijelaza,
		//heuristika nije konzistentna
		for(var x: succ.keySet()) {
			double h1 = heuristic.get(x);
			for(var y: succ.get(x)) {
				double h2 = heuristic.get(y.getState());
				double price = y.getPrice();
				if(h1 > h2+price) {
					flag = false;
					sb.append("[CONDITION]: [ERR] ");
				} else {
					sb.append("[CONDITION]: [OK] ");
				}
				sb.append("h("+x+") <= h("+y.getState()+") + c: "+h1+" <= "+h2+" + "+price+"\n");
			}
		}
		
		//ispis ovisno o zastavici
		if(flag) {
			sb.append("[CONCLUSION]: Heuristic is consistent.");
		} else {
			sb.append("[CONCLUSION]: Heuristic is not consistent.");
		}
	}
	
	/**
	 * Metoda za pretrazivanje u sirinu
	 * @param result - cvor u koji ce biti pohranjeni podatci o zavrsnom stanju 
	 * @param firstState - pocetno stanje
	 * @param finalStates - kolekcija zavrsnih stanja
	 * @param succ - mapa prijelaza
	 * @param visited - kolekcija posjecenih stanja
	 * @return - broj prosirenih cvorova, tj. posjecenih
	 */
	public static long breadthFirstSearch(Node result, String firstState, Set<String> finalStates, Map<String, Set<StatePrice>> succ, Set<String> visited) {
		Node first = new Node(null, firstState, 0, 0);
		Long numVisit = 0L;
		
		//kolekcija open 
		Deque<Node> open = new LinkedList<>();
		
		//dodajemo pocetno stanje
		open.add(first);
		
		//algoritam u skladu s prezentacijama
		while(!open.isEmpty()) {
			Node n = open.removeFirst();
			Long tempNum = ++numVisit;
			numVisit = tempNum;
			//ako smo nasli neko zavrsno stanje pohranimo vrijednosti u cvor (dubinu, roditelja kako bi mogli napraviti ispis, cijenu i ime stanja)
			if(finalStates.contains(n.getState())) {
				result.setDepth(n.getDepth());
				result.setParent(n.getParent());
				result.setPrice(n.getPrice());
				result.setState(n.getState());
				return numVisit;
			}
			
			visited.add(n.getState());
			
			for(var sp: succ.get(n.getState())) {
				
				if(visited.contains(sp.getState())) continue;
				
				Node temp = new Node(n, sp.getState(), n.getPrice()+sp.getPrice(), n.getDepth()+1);
				
				
				open.addLast(temp);
			}
		}
		return 0;
	}
	
	/**
	 * Metoda za UCS algoritam
	 * @param result - cvor u koji ce biti pohranjeni podatci o zavrsnom stanju 
	 * @param firstState - pocetno stanje
	 * @param finalStates - kolekcija zavrsnih stanja
	 * @param succ - mapa prijelaza
	 * @param visited - kolekcija posjecenih stanja
	 * @return - broj prosirenih cvorova, tj. posjecenih
	 */
	public static long unifiedCostSearch(Node result, String firstState, Set<String> finalStates, Map<String, Set<StatePrice>> succ, Set<String> visited) {
		//kolekcija open je prioritetni red koji sortira cvorove po cijeni od najmanje do najvece
		Queue<Node> open = new PriorityQueue<>();
		Long numVisit = 0L;
		
		Node first = new Node(null, firstState, 0, 0);
		
		open.add(first);
		
		//algoritam u skladu s prezentacijom
		while(!open.isEmpty()) {
			Node n = open.remove();
			Long tempNum = ++numVisit;
			numVisit = tempNum;
			if(finalStates.contains(n.getState())) {
				result.setDepth(n.getDepth());
				result.setParent(n.getParent());
				result.setPrice(n.getPrice());
				result.setState(n.getState());
				return numVisit;
			}
			
			visited.add(n.getState());
			
			Set<StatePrice> nextStates = succ.get(n.getState());
		
			for(var sp: nextStates) {
				if(visited.contains(sp.getState())) continue;
				
				double newPrice = n.getPrice()+sp.getPrice();
				Node temp = new Node(n, sp.getState(), newPrice, n.getDepth()+1);
				
				open.add(temp);
			}
			
		}
		
		return 0;
		
	}
	
	/**
	 * 
	 * @param result - cvor u koji ce biti pohranjeni podatci o zavrsnom stanju 
	 * @param firstState - pocetno stanje
	 * @param finalStates - kolekcija zavrsnih stanja
	 * @param succ - mapa prijelaza
	 * @param visited - kolekcija posjecenih stanja
	 * @param heuristic - mapa sa vrijednostima heuristike za svaka stanja
	 * @return - broj prosirenih cvorova, tj. posjecenih
	 */
	public static long aStarSearch(Node result, String firstState, Set<String> finalStates, Map<String, Set<StatePrice>> succ, Set<StatePrice> visited, Map<String, Double> heuristic) {
		//komparator za prioritetni red koji sortira cvorove po cijeni i heuristici
		//u razredu Node imamo napravljen prirodni poredak samo po cijeni, pa ovdje moramo napraviti komparator koji 
		//ce usporedivati cvorove i po heuristici
		Comparator<Node> compareByHeuristic = (n1, n2)->{
			double estimate1 = n1.getPrice()+heuristic.get(n1.getState());
			double estimate2 = n2.getPrice()+heuristic.get(n2.getState());
			
			int res = Double.compare(estimate1, estimate2);
			if(res < 0)
				return -1;
			if(res > 0)
				return 1;
			return n1.getState().compareTo(n2.getState());
		};
		
		Queue<Node> open = new PriorityQueue<>(compareByHeuristic);
		Long numVisit = 0L;
		Node first = new Node(null, firstState, 0, 0);
		
		open.add(first);
		
		//algoritam u skladu s prezentacijom
		while(!open.isEmpty()) {
			Node n = open.remove();
			Long tempNum = ++numVisit;
			numVisit = tempNum;
			if(finalStates.contains(n.getState())) {
				result.setDepth(n.getDepth());
				result.setParent(n.getParent());
				result.setPrice(n.getPrice());
				result.setState(n.getState());
				return numVisit;
			}
			visited.add(new StatePrice(n.getState(), n.getPrice()));
			
			Set<StatePrice> nextStates = succ.get(n.getState());
			
			for(var sp: nextStates) {
				double newPrice = n.getPrice()+sp.getPrice();
				Node temp = new Node(n, sp.getState(), newPrice, n.getDepth()+1);
				boolean temp1 = visited.contains(sp);
				boolean temp2 = open.contains(temp);
				boolean flag = true;
				
				//uvjetno brisemo cvor iz visited kolekcije ako smo naisli na novi put do istog stanja s manjom cijenom
				if(temp1) {
					flag = visited.removeIf((node)->{
						if(node.getState().equals(temp.getState()) && node.getPrice() > newPrice) {
							return true;
						} else {
							return false;
						}
					});
					
				}
				//uvjetno brisemo cvor iz open kolekcije ako smo naisli na novi put do istog stanja s manjom cijenom
				else if(temp2) {
					flag = open.removeIf((node)->{
						if(node.getState().equals(temp.getState()) && node.getPrice() > newPrice) {
							return true;
						} else {
							return false;
						}
					});
				}
				
				if(flag) {
					
					open.add(temp);
				}
			}
		}
		return 0;
		
	}

}
