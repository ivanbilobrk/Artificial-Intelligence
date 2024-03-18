package ui;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class ID3 {
	
	//lista hashmapa gdje svaka hashmapa drzi vrijednosti: ime značajke, vrijednost značajke
	private List<TreeMap<String, String>> dataSet;
	//lista značajki
	private List<String> features;
	//korijenski cvor stabla
	private Node root;
	
	public ID3() {
		dataSet = new ArrayList<>();
	}
	
	public void fit(String trainPath) throws IOException {
		
		List<String> lines = Files.readAllLines(Path.of(trainPath)); 
		
		//učitavanje značajki
		features = Arrays.stream(lines.get(0).split(",")).map(feature -> feature.strip()).collect(Collectors.toList());
		
		//učitavanje vrijednosti značajki
		
		lines.remove(0);
		
		for(var line: lines) {
			TreeMap<String, String> dataRow = new TreeMap<>();
			
			String[] values = line.split(",");
			
			for(int i = 0; i < values.length; i++) {
				dataRow.put(features.get(i), values[i]);
			}
			
			dataSet.add(dataRow);
		}

	}
	
	private Node id3(List<TreeMap<String, String>> dataSet, List<TreeMap<String, String>> dataSetParent, List<String> features, String label, boolean hasDepth, int maxDepth, int currentDepth) {
		
		//provjera je li uključena ograničena dubina stabla i ako je provjera jesmo li došli do granice
		//ako jesmo onda vraćamo list koji ima vrijednost najčešće vrijednosti oznake ciljne varijable
		if(hasDepth && currentDepth >= maxDepth) {
			return new Node(argmax(dataSet, label), null, null);
		}
		
		//ako je novi skup D prazan onda vraćamo list koji ima vrijednost najčešće vrijednosti oznake ciljne varijable iz skupa Dparent
		if(dataSet == null || dataSet.isEmpty()) {
			return new Node(argmax(dataSetParent, label), null, null);
		}
		
		//najčešća vrijednost ciljne varijable
		String mostOccurences = argmax(dataSet, label);
		
		//redci kojima je vrijednost ciljne  jednaka najčešćoj vrijednosti ciljne varijable
		List<TreeMap<String, String>> dataSetTemp = new ArrayList<>();
		
		for(var row: dataSet) {
			if(row.get(label).equals(mostOccurences)) dataSetTemp.add(row);
		}
		
		boolean flag = true;
		
		//provjera jesmo li filtriranjem ulaznog skupa na samo one redke koji imaju vrijednost ciljne varijable
		//istu kao i najčešću vrijednost ciljne varijble dobili ustvari isti skup, tj. više ne možemo dijeliti ulazni skup D, došli smo zapravo do lista u stablu
		//i vraćamo list koji ima vrijednost najčešće oznake primjera u čvoru tj. skupu D
		if(dataSet.size() != dataSetTemp.size()) {
			flag = false;
		} else {
			for(int i = 0; i < dataSet.size(); i++) {
				if(!dataSet.get(i).equals(dataSetTemp.get(i))) flag = false; 
			}
		}
		
		// provjera uvjeta X = prazan skup, događa se kad dođemo do lista koji ima različite oznake, tada ne možemo raditi daljnju podjelu te vraćamo list koji 
		//ima vrijednost najčešće oznake primjera u čvoru tj. skupu D
		if(features == null || features.isEmpty() || flag) {
			return new Node(mostOccurences, null, null);
		}
		
		//računanje najveće informacijske dobiti
		TreeMap<String, Float> ig = new TreeMap<>();
		
		for(int i = 0; i < features.size()-1; i++) {
			ig.put(features.get(i), calcIg(dataSet, features.get(i), label));
		}
		
		float maxIg = 0;
		String maxIgFeature = null;
		
		for(var x: ig.entrySet()) {
			if(maxIgFeature == null || x.getValue() > maxIg) {
				maxIg = x.getValue();
				maxIgFeature = x.getKey();
			}
		}
		
		//mapa koja za svako dijete trenutnog čvora sprema vrijednost značajke koju proučavamo u trenutnom čvoru
		TreeMap<String, Node> children = new TreeMap<>();
		
		//sve moguće vrijednosti koje značajka s najvećom informacijsom dobiti može poprimiti
		TreeSet<String> values = new TreeSet<>();
		
		for(var row: dataSet) {
			values.add(row.get(maxIgFeature));
		}
		
		//povećanje vrijednosti dubine rekurzije
		int newDepth = ++currentDepth;
		for(var v: values) {
			//redci podataka za koje vrijedi da značajka s najvećom informacijskom dobiti ima određenu vrijednost
			List<TreeMap<String, String>> tempDataSet = new ArrayList<>();
			
			for(var row: dataSet) {
				if(row.get(maxIgFeature).equals(v)) {
					tempDataSet.add(row);
				}
			}
			
			//nova lista značajki bez značajke koja se trenutno proučava, tj. bez one koja ima najveću informacijsku dobit
			//ova lista služi za daljnje rekurzivne pozive
			List<String> featuresTemp = new ArrayList<>();
			
			for(var x: features) {
				if(!x.equals(maxIgFeature)) featuresTemp.add(x);
			}
			
			//novi rekurzivni poziv
			Node t = id3(tempDataSet, dataSet, featuresTemp, label, hasDepth, maxDepth, newDepth);
			
			children.put(v, t);
		}
		
		return new Node(null, maxIgFeature, children);

	}
	
	//funkcija za izračun informacijske dobiti
	public float calcIg(List<TreeMap<String, String>> dataSet, String feature, String label) {
		
		//treba nam prvo entropija
		float entropy = getEntropy(dataSet, label);
		
		float estimateEntropy = 0;
		
		//sve moguće vrijednosti za ulaznu značajku
		TreeSet<String> values = new TreeSet<>();
		
		for(var row: dataSet) {
			values.add(row.get(feature));
		}
		
		//prolazimo po svim vrijednostima značajke i računamo procijenjenu vrijednost entropije nakon podjele skupa na temelju značajke
		for(var x: values) {
			List<TreeMap<String, String>> tempDataSet = new ArrayList<>();
			
			for(var row: dataSet) {
				if(row.get(feature).equals(x)) {
					tempDataSet.add(row);
				}
			}
			
			float entropy2 = getEntropy(tempDataSet, label);
			
			estimateEntropy += (((float)(tempDataSet.size()))/dataSet.size())*entropy2;
		}
		
		return entropy - estimateEntropy;
	}
	
	//funkcija za izračun entropije nekog skupa
	public float getEntropy(List<TreeMap<String, String>> dataSet, String label) {
		
		//mapa koja za svaku moguću vrijednost ciljne varijable pamti broj njezina pojavljivanja
		TreeMap<String, Integer> occurences = new TreeMap<>();
		
		
		for(var row: dataSet) {	
			occurences.merge(row.get(label), 1, (oldValue, newValue)-> oldValue+1);
		}
		
		float entropy = 0;
		
		for(var x: occurences.values()) {
			float dataSetSize = (float)dataSet.size();
			float count = (float)x;
			
			if(count == 0 || dataSetSize == 0) {
				entropy += 0;
			} else {
				entropy += (count/dataSetSize)*((Math.log(count/dataSetSize))/Math.log(2));
			}
		}
		
		//moramo negirat entropiju
		return -entropy;
	}
	
	
	public String argmax(List<TreeMap<String, String>> dataSet, String label) {
		//prvo treba naći sve vrijednosti koje oznaka label može poprimiti i mapirati broj pojavljivanja
		
		TreeMap<String, Integer> occurences = new TreeMap<>();
		
		for(var row: dataSet) {	
			occurences.merge(row.get(label), 1, (oldValue, newValue)-> oldValue+1);
		}
		
		//sada tražimo vrijednost za koju postoji najviše redaka u datasetu
		
		String maxValue = null;
		int maxOccurences = 0;
		
		for(var entry: occurences.entrySet()) {
			
			if(maxValue == null || entry.getValue() > maxOccurences) {
				maxOccurences = entry.getValue();
				maxValue = entry.getKey();
			}
		}
		
		return maxValue;

	}
	
	public Node id3Tree(boolean hasDepth, int maxDepth, int currentDepth) {
		root = id3(dataSet, dataSet, features, features.get(features.size()-1), hasDepth, maxDepth, currentDepth);
		return root;
	}
	
	//rekurzivna metoda koja vraća predikciju oznake label za ulazni skup dataSet
	public String evaluate(TreeMap<String, String> dataSet, Node root, List<TreeMap<String, String>> trainSet, String label) {
		
		
		if(root.getLabel() != null) {
			return root.getLabel();
		}
		
		String feature = root.getFeature();
		
		String value = dataSet.get(feature);
		
		root = root.getChildren().get(value);
		
		if(root == null) {
			//treba naci najcescu vrijednost oznake u podatcima za treniranje
			String maxLabel = argmax(trainSet, label);
			
			return maxLabel;
		}
		
		//inace moramo suziti podatke za treniranje
		
		List<TreeMap<String, String>> trainSetTemp = new ArrayList<>();
		
		for(var row : trainSet) {
			if(row.get(feature).equals(value))
				trainSetTemp.add(row);
		}
		
		return evaluate(dataSet, root, trainSetTemp, label);
		
	}
	
	
	//metoda koja služi za ispis predikcije, matrice zabune, 
	public void predict(String testPath) throws IOException {
		
		//učitavanje podataka za provjeru modela
		List<String> lines = Files.readAllLines(Path.of(testPath));
		
		features = Arrays.stream(lines.get(0).split(",")).map(feature -> feature.strip()).collect(Collectors.toList());
		
		String label = features.get(features.size()-1);
		
		List<TreeMap<String, String>> testSet = new ArrayList<>();
		
		lines.remove(0);
		
		for(var line: lines) {
			TreeMap<String, String> dataRow = new TreeMap<>();
			
			String[] values = line.split(",");
			
			for(int i = 0; i < values.length; i++) {
				dataRow.put(features.get(i), values[i]);
			}
			
			testSet.add(dataRow);
		}
		
		
		System.out.print("[PREDICTIONS]: ");
		int correctCount = 0;
		
		//matrica zabune ima kao ključeve sve moguće kombinacije (kartezijev produkt) vrijednosti ciljne varijable
		TreeMap<String, Integer> confusionMatrix = new TreeMap<>();
		
		TreeSet<String> labelValues = new TreeSet<>();
		
		for(var x: testSet) {
			labelValues.add(x.get(label));
		}
		
		//inicijalno punjenje matrice zabune sa nulama
		for(var x: labelValues) {
			for(var y: labelValues) {
				confusionMatrix.put(x+" "+y, 0);
			}
		}
		
		//prolazak po ulaznim testnim podatcima te brojanje točnih predikcija i punjenje matrice zabune
		for(var x: testSet) {
			//predikcija vrijednosti ciljne varijable
			String result = evaluate(x, root, dataSet, label);
			
			String keyForConfusion = x.get(label) + " " + result;
			
			//punjenje matrice zabune
			confusionMatrix.merge(keyForConfusion, 1, (oldValue, newValue)-> ++oldValue);
			
			//provjera je li model ispravno predvidio vrijednost 
			if(x.get(label).equals(result)) ++correctCount;
			
			System.out.print(result+" ");
		}
		
		//ispis točnosti
		System.out.print("\n[ACCURACY]: ");
		double accuracy = ((double)correctCount/(double)testSet.size());
		
		System.out.printf("%.5f", accuracy);
		
		System.out.println("\n[CONFUSION_MATRIX]:");
		
		String rowNamePrevious = null;
		
		//ispis matrice zabune kao 2D tablica
		for(var row : confusionMatrix.entrySet()) {
			
			
			
			if(rowNamePrevious == null  || !rowNamePrevious.equals(row.getKey().split(" ")[0])) {
				
				
				if(rowNamePrevious != null && !rowNamePrevious.equals(row.getKey().split(" ")[0])) {
					
					System.out.println();
					System.out.print(row.getValue());
				} else {
					System.out.print(row.getValue());
				}
				
				rowNamePrevious = row.getKey().split(" ")[0];
				

				
			} else if(row.getKey().split(" ")[0].equals(rowNamePrevious)) {
				System.out.print(" "+row.getValue());
			}
		}
		
	}
}
