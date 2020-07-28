import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;

public class OddWords{
	 public static void main(String [ ] args) {
		 
		File input = new File(args[0]);
		BSTlex root = new BSTlex();
		try{
		//get words without punctuation
        Scanner sc = new Scanner(input).useDelimiter("[^a-zA-Z0-9]");

        while (sc.hasNext()) {
			//get next word and set to lowercase
            String word = sc.next().toLowerCase();		
			if (!word.isEmpty()){
			//insert word, if returns false this indicates duplicate and so find and remove this value from bst
			if (root.insert(word) == false) {
				BSTlex del = root.find(word);
				System.out.println(del.key + " DELETED");
				del.remove();
        		}
			}
		}
		
        sc.close();
		}
		catch (FileNotFoundException ex)  
    	{
 			System.out.println(ex);
    	}
   		System.out.println("LEXICON:");
		 //print all values from BST in order
		root.transverse();
		 
	    }
	
	
}