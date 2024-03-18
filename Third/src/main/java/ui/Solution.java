package ui;

import java.io.IOException;

public class Solution {

	public static void main(String ... args) throws IOException {
		
		String data = args[0].strip();
		String test = args[1].strip();
		
		int depth = 0;
		boolean hasDepth = false;
		
		if(args.length == 3) {
			depth = Integer.parseInt(args[2]);
			hasDepth = true;
		}
		
		ID3 model = new ID3();
		
		//punimo model sa ulaznim podatcima
		model.fit(data);
		
		//gradimo stablo odluke
		Node root = model.id3Tree(hasDepth, depth, 0);
		
		//ispis grana
		System.out.println("[BRANCHES]:");
		System.out.println(root.print(1, "")); 
		
		//ispis predikcija
		model.predict(test);
		
	}

}
