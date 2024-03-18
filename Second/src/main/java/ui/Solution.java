package ui;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

//izvor: prezentacije s predavanja

/**
 * Razred sa svim metodama za rezoluciju opovrgavanjem
 */
public class Solution {
	
	//konstantna klauzula NIL 
	public static final Clause NIL = new Clause(Set.of(new Literal("NIL", false)), null, null);

	public static void main(String ... args) throws IOException, InterruptedException {
		
		//ucitavanje argumenata programa
		String algo = args[0].strip();
		String path = args[1].strip();
		
		Path p = Paths.get(path);
		
		//provjera je li radimo rezoluciju
		if(algo.equals("resolution")) {
			
			//premise
			Set<Clause> first = new LinkedHashSet<>();
			
			//last je skup klauzula koje predstavljaju negirani cilj
			//metoda Loader.load ucitava i u skup premisa pocetne premise
			Set<Clause> last = Loader.load(p, first);
			
			//micemo redundantne premise
			removeRedundant(first);
			removeRedundant(last);
			
			//provodimo rezoluciju te spremamo zastavicu uspjeha
			boolean result = resolution(first, last);
			
			//skup koji sadrži literale negiranog cilja
			Set<Literal> goals = new LinkedHashSet<>();
			
			for(var x: last) {
				goals.add(x.getForIndex(0));
			}
			
			Clause goal = new Clause(goals, null, null);
			
			//ovisno o zastavici formatiramo ispis
			if(result) {
				System.out.print("[CONCLUSION]: "+goal.toString2()+" is true\n");
			} else {
				
				//ako smo dobli false onda ispisujemo sve pocetne premise i negirani cilj
				for(var c: first) {
					System.out.println(c);
				}
				
				for(var c: last) {
					System.out.println(c);
				}
				System.out.println("===============");
				
				System.out.print("[CONCLUSION]: "+goal.toString2()+" is unknown\n");
			}
			
			//rijec je o cookingu
		} else {
			
			String pathInstructions = args[2].strip();
			
			//datoteka gdje se nalaze upute kako cemo dodavati i uklanjati premise te provoditi rezoluciju
			Path p2 = Paths.get(pathInstructions);
			
			//instrukcije korisnika koje moramo izvrsiti
			List<String> instructionsLines = Files.readAllLines(p2);
			
			//pocetne premise
			Set<Clause> first = new LinkedHashSet<>();
			
			List<String> clausesFirst = Files.readAllLines(p);
			
			StringBuilder sb = new StringBuilder();
			sb.append("Constructed with knowledge: \n");
			
			//ucitavanje pocetnih premisa, sada ucitavamo cijelu datoteku jer zadnja klauzula nije ciljna
			for(var x: clausesFirst) {
				if(x.charAt(0) == '#') continue;
				x = x.toLowerCase();
				Set<Literal> literals = Loader.getLiterals(x.split(" v "));
				Clause c = new Clause(literals, null, null);
				first.add(c);
				sb.append(c.toString()+"\n");
			}
			
			System.out.print(sb.toString());
			
			//micemo redundantne klauzule iz pocetnog skupa premisa
			removeRedundant(first);
			
			//imamo ucitani pocetni skup premisa
			
			//citamo instrukciju po instrukciju te izvrsavamo korisnicke naredbe 
			for(var x: instructionsLines ) {
				if(x.charAt(0) == '#') continue;
				x = x.toLowerCase();
				char symbol = x.charAt(x.length()-1);
				x = x.substring(0, x.length()-2);
				
				//ucitavamo literale iz naredbe
				Set<Literal> literals = Loader.getLiterals(x.split(" v "));
				
				//ovisno o simbolu izvodimo naredbe korisnika
				if(symbol == '?') {
					System.out.print("User's command: "+x+" ?\n");
					
					//ucitavamo ciljne klauzule
					Set<Clause> toProve = new LinkedHashSet<>();
					
					for(var y: literals) {
						y.setNegation(!y.isNegation());
						toProve.add(new Clause(Set.of(y), null, null));
					}
					
					//micemo redundante klauzule
					removeRedundant(toProve);
					
					//provodimo rezoluciju
					boolean result = resolution(first, toProve);
					
					//formatiramo ispis ovisno o rezultatu isto kao i kod obicne rezolucije
					if(result) {
						System.out.println("[CONCLUSION]: "+x+" is true");
					} else {
						for(var c: first) {
							System.out.println(c);
						}
						
						for(var c: toProve) {
							System.out.println(c);
						}
						
						System.out.println("[CONCLUSION]: "+x+" is unknown");
					}
				} else if(symbol == '-'){
					//moramo ukloniti klauzulu iz premisa
					first.remove(new Clause(literals, null, null));
				} else if(symbol == '+') {
					//moramo dodati klauzulu u premise i ukloniti redundantne klauzule jer dodavanjem smo mogli dodati 
					//klauzulu koja u kombinaciji sa nekim starim klauzulama daje neke redundantne klauzule
					first.add(new Clause(literals, null, null));
					removeRedundant(first);
				}
			}
		}
		
	}
	
	//metoda koja trazi redundantne klauzule u skupu premisa i rezolventi i u skupu SOS i rezolventi
	public static Set<Clause> getRedundant(Set<Clause> first, Set<Clause> sos, List<Clause> resolvents){
		Set<Clause> redundant = new HashSet<>();
		
		for(var x: resolvents) {
			for(var y: first) {
				if(x.getLiterals().containsAll(y.getLiterals())) {
					redundant.add(x);
				}
					
			}
		}
		
		for(var x: resolvents) {
			for(var y: sos) {
				if(x.getLiterals().containsAll(y.getLiterals())) {
					redundant.add(x);
				}
					
			}
		}
		
		return redundant;
	}
	
	//metoda koja uklanja redundantne klauzule iz neke kolekcije klauzula
	public static void removeRedundant(Collection<Clause> clauses) {
		
		Set<Clause> redundant = new HashSet<>();
		for(var x: clauses) {
			for(var y: clauses) {
				if(!x.equals(y) && x.getLiterals().containsAll(y.getLiterals())) {
					redundant.add(x);
				}
			}
		}
		
		clauses.removeAll(redundant);
	}
	
	//metoda koja provodi rezoluciju
	public static boolean resolution(Set<Clause> first, Set<Clause> last) throws InterruptedException {
		
		//skup potpore
		Set<Clause> sos = new LinkedHashSet<>();
		//odmah dodajemo sve klauzule cilja u SOS
		sos.addAll(last);
		
		//skup new za spremanje medurezultata (klauzula) tijekom procesa
		Set<Clause> newSet = new LinkedHashSet<>();
		
		//parovi klauzula koje su razrijesene 
		Set<PairClause> resolvedPairs = new HashSet<>();
		
		while(true) {
			//razrjesavamo svaku klauzulu sa svakom iz skupa pocetnih premisa i SOS
			for(var x: first) {
				for(var y: sos) {
					//preskacemo klauzule koje su razrijesene
					if(resolvedPairs.contains(new PairClause(x, y))) continue;
					List<Clause> resolvents = resolve(x, y);
					resolvedPairs.add(new PairClause(x, y));
					//provjeravamo jesmo li dobili NIL
					if(resolvents.size() == 1 && resolvents.contains(NIL)) {
						sos.add(resolvents.get(0));
						printSetup(resolvents, first, sos);
						return true;
					}
					
					//micemo redundantne klauzule iz rezolventi
					Set<Clause> redundant = getRedundant(first, sos, resolvents);
					resolvents.removeAll(redundant);
					
					//dodajemo sve dobivene klauzule u newSet
					newSet.addAll(resolvents);
				}
			}
			
			//radimo isti postupak ali sada samo sa klauzulama iz SOS skupa
			for(var x: sos) {
				for(var y: sos) {
					if(x.equals(y)) continue;
					if(resolvedPairs.contains(new PairClause(x, y))) continue;
					List<Clause> resolvents = resolve(x, y);
					resolvedPairs.add(new PairClause(x, y));
					if(resolvents.size() == 1 && resolvents.contains(NIL)) {
						sos.add(resolvents.get(0));
						printSetup(resolvents, first, sos);
						return true;
					}
					
					Set<Clause> redundant = getRedundant(first, sos, resolvents);
					resolvents.removeAll(redundant);
					
					newSet.addAll(resolvents);
				}
			}
			
			//na kraju micemo sve redundantne klauzule iz newSet jer smo opet nakon dodavanja 
			//rezolventi dobivenih razrjesavanjem skupa SOS mogli dobiti redundantnost
			removeRedundant(newSet);
			
			
			//ako je new podskup od clauses vracamo false jer nismo uspjeli prosiriti skup clauses
			List<Clause> tempAll = new ArrayList<>();
			tempAll.addAll(sos);
			tempAll.addAll(first);
			
			//dodajemo nove klauzule u SOS skup
			newSet.removeAll(tempAll);
			if(newSet.size() == 0) return false;
			sos.addAll(newSet);
		}
	}
	
	//metoda koja priprema podatke za ispis tako što pronalazi sve roditeljske klauzule koje smo koristili za zakljucak
	public static void printSetup(List<Clause> resolvents, Set<Clause> first, Set<Clause> sos) {
		List<Clause> parents = new ArrayList<>();
		getParents(parents, resolvents.get(0));
		parents.add(resolvents.get(0));
		
		List<Clause> tempAll = new ArrayList<>();
		
		//dodajemo prvo pocetne premise, pa onda SOS skup kako bi sacuvali redoslijed
		tempAll.addAll(first);

		tempAll.addAll(sos);
		
		//ispis rezultata
		printResult(tempAll, parents, resolvents.get(0));
	}
	
	public static void printResult(List<Clause> clauses, List<Clause> parents, Clause nil) {
		
		//mapa koja sadrzi klauzule i njihove indexe prema listi clauses
		Map<Clause, Integer> mappedClauses = new HashMap<>();
		
		//mapa koja mapira index na odredenu klauzulu, indexi su poredani rastucim poretkom, ali razmak izmedu njih ne mora biti 1 te ne moraju kretati od 0
		Map<Integer, Clause> mappedIndex = new TreeMap<>();
		
		//mapa koja mapira takoder index na neku klauzulu, ali su sada indexi poredani od 0 do n, sa razmakom 1 izmedu
		Map<Integer, Clause> mappedIndex2 = new TreeMap<>();
		
		//mapiranje klauzula na odredeni index te mapiranje indexa na neku klauzulu
		for(var x: parents) {
			int index = clauses.indexOf(x);
			mappedClauses.put(x, index);
			mappedIndex.put(index, x);
		}
		
		//mapiranje indexa na klauzule, ali na nacin da indexi krecu od 0 do n, te da je razmak izmedu indexa 1
		int index = 0;
		for(var x: mappedIndex.keySet()) {
			mappedIndex2.put(index, mappedIndex.get(x));
			++index;
		}
		
		for(var x: mappedIndex2.keySet()) {
			mappedClauses.put(mappedIndex2.get(x), x);
		}
		
		//formatiranje ispisa
		StringBuilder sb = new StringBuilder();
		boolean foundParents = false;
		for(var x: mappedIndex2.keySet()) {
			Clause temp = mappedIndex2.get(x);
			
			if(temp.getParent1() != null && temp.getParent2() != null && !foundParents) {
				sb.append("===============\n");
				foundParents = true;
			}
			
			int x1 = x+1;
			sb.append(x1+". "+temp.toString());
			
			if(temp.getParent1() != null) {
				int indexP1 = mappedClauses.get(temp.getParent1());
				++indexP1;
				sb.append(" ("+indexP1+", ");
			}
			if(temp.getParent2() != null) {
				int indexP2 = mappedClauses.get(temp.getParent2());
				++indexP2;
				sb.append(indexP2+")");
			}
			
			sb.append("\n");
		}
		
		System.out.print(sb.toString());
		System.out.print("===============\n");
		
		
	}
	
	//rekurzivna metoda koja dohvaca sve roditeljske klauzule za neku klauzulu
	public static void getParents(List<Clause> parents, Clause nil){
		if(nil.getParent1() != null) {
			parents.add(nil.getParent1());
			getParents(parents, nil.getParent1());
		} 
		
		if(nil.getParent2() != null) {
			parents.add(nil.getParent2());
			getParents(parents, nil.getParent2());
		}
	}

	//metoda koja razrjesava dvije klauzule
	public static List<Clause> resolve(Clause x, Clause y) throws InterruptedException {
		//lista klauzula koje dobijemo razrjesavanjem
		List<Clause> clauses = new ArrayList<>();
		
		//provjera jesmo li dobili NIL
		if(x.getLiterals().size() == 1 && y.getLiterals().size() == 1 && x.getForIndex(0).getName().equals(y.getForIndex(0).getName()) && (x.getForIndex(0).isNegation() != y.getForIndex(0).isNegation())) {
			return List.of(new Clause(Set.of(new Literal("NIL", false)), x, y));
		}
		
		//idemo po svim literalima prve klauzule te za svaki literal u drugoj klauzuli provjeravamo mozemo li razrjesiti klauzule
		for(var l1: x.getLiterals()) {
			for(var l2: y.getLiterals()) {
				
				if(l1.getName().equals(l2.getName()) && l1.isNegation() != l2.isNegation()) {
					
					Set<Literal> literals = createClauseFromLiterals(x.getLiterals(), y.getLiterals(), l1.getName());
					
					
					if(literals != null && literals.size() != 0)
						clauses.add(new Clause(literals, x, y));
				}
			}
		}
		
		return clauses;
	}
	
	//metoda koja uklanja tautologiju iz skupa klauzula
	public void removeTautology(Set<Clause> clauses) {
		
		//klauzule koje su tautologija te ih treba maknuti
		Set<Clause> toRemove = new HashSet<>();
		
		//za svaku klauzulu vrtimo se po svim literalima te provjeravamo ima li literala s istim nazivom, ali drukcijom negacijom te ako ima onda klauzulu dodamo u 
		//skup klauzula za brisanje
		for(var c: clauses) {
			for(var x: c.getLiterals()) {
				for(var y: c.getLiterals()) {
					if(x.getName().equals(y.getName()) && x.isNegation() != y.isNegation()) {
						toRemove.add(c);
					}
				}
			}
		}
		//brisemo tautologije iz skupa
		clauses.removeAll(toRemove);
	}
	
	//metoda koja vraca spojeni skup literalana temelju dva skupa literala od nekih klauzula
	public static Set<Literal> createClauseFromLiterals(Set<Literal> l1, Set<Literal> l2, String name) {
		
		//literali koji cine novu klauzulu
		Set<Literal> newLiterals = new LinkedHashSet<>();
		
		//dodajemo sve literale iz prvog skupa te pazimo da ne dodamo literal po kojem smo razrijesili klauzule (String name)
		for(var x: l1) {
			if(!x.getName().equals(name)) newLiterals.add(x);
		}
		
		//isti postupak kao i za prvi skup literala
		for(var x: l2) {
			if(!x.getName().equals(name)) newLiterals.add(x);
		}
		
		//provjera je li novi literali daju klauzulu koja je tautologija, ako je vracamo null jer ne zelimo takve klauzule imati u skupu 
		//klauzula s kojima rjesavamo problem rezolucije
		if(Loader.checkTautology(newLiterals)) return null;


		if(newLiterals.size() == 0) return null;
		
		return newLiterals;
	}

}
