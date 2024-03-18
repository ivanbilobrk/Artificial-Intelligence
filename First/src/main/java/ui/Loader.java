package ui;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class Loader {
	
	/**
	 * Staticka metoda za ucitavanje početnog stanja, ciljnih stanja, prijelaza iz datoteke
	 * @param p - datoteka za citanje 
	 * @param succ - mapa u koju ce biti ucitani svi prijelazi
	 * @param finalStates - kolekcija u koju ce biti ucitana zavrsna stanja
	 * @return - prvo stanje iz kojeg pocinje pretraga
	 * @throws IOException
	 */
	public static String loadStates(Path p, Map<String, Set<StatePrice>> succ, Set<String> finalStates) throws IOException {
		
		int count = 0;
		String firstState = "";
		//citamo redak po redak te parsiramo
		for(String s: Files.readAllLines(p)) {
			if(s.charAt(0) == '#') continue;
			
			//brojac se uvodi jer znamo da prva dva redka ce biti pocetno stanje i zavrsna stanja
			if(count == 0) {
				firstState = s;
				++count;
			} else if(count == 1) {
				String[] temp = s.split(" ");
				for(var x: temp) {
					finalStates.add(x);
				}
				++count;
			} else {
				//nakon sto smo procitali prva dva redka citamo prijelaze
				String[] temp = s.split(":");
				
				String stateFrom = temp[0].strip();
				
				//susjedna stanja nekog stanja odmah sortiramo pri ucitavanju kako ne bi morali kasnije tijekom izvodenja
				//algoritma pretrage
				Set<StatePrice> nextStates = new TreeSet<>(StatePrice.compareByAlphabet);
				
				if(temp.length == 2) {
					String[] nextTemp = temp[1].strip().split(" ");

					for(String x: nextTemp) {
						StatePrice stateP = new StatePrice(x.split(",")[0], Double.parseDouble(x.split(",")[1]));
						nextStates.add(stateP);
					}
				}

				
				succ.put(stateFrom, nextStates);
			}
		}
		return firstState;
	}
	
	/**
	 * Metoda za ucitavanje podataka iz datoteke za heuristiku
	 * @param p - datoteka iz koje citamo
	 * @param heuristic - mapa kojoj je kljuc naziv stanja, a vrijednost iznos heuristike za to stanje
	 * @throws IOException
	 */
	public static void loadHeuristic(Path p, Map<String, Double> heuristic) throws IOException {
		
		for(String s: Files.readAllLines(p)) {
			if(s.charAt(0) == '#') continue;
			
			String[] temp = s.split(":");
			
			String state = temp[0].strip();
			double h =  Double.parseDouble(temp[1].strip());
			
			heuristic.put(state, h);
		}
	}
}
