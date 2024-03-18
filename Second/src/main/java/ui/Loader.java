package ui;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Razred koji ima staticke metode za rezoluciju
 *
 */
public class Loader {
	
	//metoda koja na transformira polje stringova u skup literala (parsira input datoteke)
	public static Set<Literal> getLiterals(String[] input) {
		Set<Literal> lista = new LinkedHashSet<>();
		for(String x: input) {
			x = x.strip();
			boolean flag = x.charAt(0) == '~'? true : false;
			
			if(flag) {
				x = x.substring(1);
			} 
			Literal l = new Literal(x, flag);
			lista.add(l);
		}
		return lista;
	}
	
	//metoda koja ucitava u skup first pocetne premise te vraća klauzule koje cine negirani cilj
	public static Set<Clause> load(Path p, Set<Clause> first) throws IOException {
		
		List<String> lines = Files.readAllLines(p);
		int size = lines.size();
		
		for(int i = 0; i < size; i++) {
			String s = lines.get(i);
			//preskacemo komentar
			if(s.charAt(0) == '#') continue;
			s = s.toLowerCase();
			String[] temp = s.split(" v ");
			Set<Literal> lista = getLiterals(temp);
			
			//zastavica za tautologiju
			boolean tautology = checkTautology(lista);
			
			//provjera jesmo li na zadnjem redu (zadnji red je ono sto trebamo izvesti)
			if(i == size-1) {
				
				//ako je cilj tautologija odmah stajemo sa izvrsavanjem
				if(tautology) {
					Clause c = new Clause(lista, null, null);
					System.out.println("[CONCLUSION]: "+c.toString()+" is true");
					System.exit(0);
				}
				
				//ako nije tautologija dodajemo klauzule koje cine negirani cilj u skup
				Set<Clause> lasts = new LinkedHashSet<>();
				for(var x: lista) {
					//stvaramo nove klauzule te ih odmah negiramo
					lasts.add(new Clause(Set.of(new Literal(x.getName(), !x.isNegation())), null, null));
				}
				return lasts;
				
			} else {
				//ukoliko nije zadnji redak datoteke dodajemo u skup first samo ako nije rijec o tautologiji
				if(!tautology) first.add(new Clause(lista, null, null));
			}
			
		}
		return null;
	}
	
	//metoda za provjeru tautologije
	public static boolean checkTautology(Set<Literal> literals) {
		for(var x: literals) {
			for(var y: literals) {
				if(x.getName().equals(y.getName()) && x.isNegation() != y.isNegation()) return true;
			}
		}
		return false;
	}
	
}
