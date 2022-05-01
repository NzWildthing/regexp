import java.util.*;
import java.io.*;
import java.nio.file.*;

class REsearch {
            //parent varible
            private static int parentState;
            //lists to store the four components of each line
            private static List<Integer> stateNum = new ArrayList<Integer>();
            private static List<Character> ch = new ArrayList<Character>();
            private static List<Integer> next1 = new ArrayList<Integer>();
            private static List<Integer> next2 = new ArrayList<Integer>();
 

    public static void main(String[] args) {
		try {
            //if valid arg input
			if (args.length != 1) {
				System.err.println("");
				return;
			}
        }
        catch (Exception e){
            System.err.println(e);
        }    
        
        String line;
        String[] split;
        String filename = args[0];
        ArrayList<String> fileString = new ArrayList<>();
        
        // Read standard input to populate the FSM arrays
        try {

            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

            line = reader.readLine();
            int counter = 0;

            while (line != null) {
                ///CHECKINPUT
                split = line.split(" ");
                stateNum.add(Integer.parseInt(split[0]));
                ch.add(split[1].charAt(0));
                next1.add(Integer.parseInt(split[2]));
                next2.add(Integer.parseInt(split[3]));

                line = reader.readLine();
                counter++;
            }
        } 
        catch (Exception e) {
            System.err.println("Invalid piped in FSM file");

            e.printStackTrace();
        }


        try{
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            line = reader.readLine();
            
            // While there are still lines to read from the file

            while (line != null) {
                if (line.length() > 0) {
                    fileString.add(line.trim());
                }
               
                /* String[] splitChars = nextLine.split("");

                 Check if the pattern matches the split chars
                if (executePatternSearch(splitChars)) {
                    System.err.println("Success: " + nextLine);
                    System.out.println(nextLine);
                } else {
                    System.err.println("Failed: " + nextLine);
                }
                */
                line = reader.readLine();
            }
            reader.close();
        }
        catch (Exception e) {
            System.err.println(e);
        } 
        System.err.println("---------------------------------------");
        System.err.println(stateNum);
        System.err.println(ch);
        System.err.println(next1);
        System.err.println(next2);
        //iterate through each of the lines
        for (String nextLine : fileString) {
            //create counters
            int state = 0;
            int symbol = 0;

            parentState = next1.get(state);
            //call the do the thing to recursively search the fsm
            if (searchLine(next1.get(state), nextLine, symbol)) {
            //if it returns true meaning the line has what is being searched for, print the line
            System.out.println(nextLine);
            } 
        }
    }

    //////
    public static boolean searchLine(int state, String line, int symbol)
    {
        //if the end of the fsm is reached it means it won so return true
        if (next1.get(state) == 0) {return true;}

        //if the end of the line is reached it means it lost so return false
        if (symbol >= line.length()) {return false;}

        //get the character at this place
        char c = line.charAt(symbol);

        //check if the node at this state has different nexts
        if (next1.get(state) != next1.get(state)) {

            parentState = next1.get(state);

            if (searchLine(next1.get(state), line, symbol)) { return true; }
            else {
                //System.err.println("Second branch");
                parentState = next1.get(state);
                if (searchLine(next1.get(state), line, symbol)) { return true; }
                else { return false; }
            }
        }
        else {
            //node has the same feet
            //if node matches the character
            if (ch.get(state) == c || ch.get(state) == '.') {
                //increase character counter
                symbol++;
                //call itself recursively
                if (searchLine(next1.get(state), line, symbol)) { return true; }
            }
            //check if the character is a branching one
            else if (ch.get(state) == (char)3) {
                //dont increace the character counter but call itself recursively
                if (searchLine(next1.get(state), line, symbol)) { return true; }
            }
            else {
                //increase the character counter
                symbol++;
                if (searchLine(parentState, line, symbol)) { return true; }
            }
        }
        return false;
    }

}   
