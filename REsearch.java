import java.util.*;
import java.io.*;
import java.nio.file.*;



class REsearch {
            //parent varible
            private static int parentState;
            //lists to store the four components of each line
            private static List<Integer> currStateNum = new ArrayList<Integer>();
            private static List<Character> ch = new ArrayList<Character>();
            private static List<Integer> next1 = new ArrayList<Integer>();
            private static List<Integer> next2 = new ArrayList<Integer>();
 

    public static void main(String[] args) {
		//try {
        //if valid arg input
        if (args.length != 1) {
            System.err.println("please use: java REsearch <filename>");
            return;
        }
        /*
        }
        catch (Exception e){
            System.err.println(e);
        }    
        */
        //declearing variables
        String nextLine = " ";
        String[] split;
        String filename = args[0];
        ArrayList<String> textFile = new ArrayList<>();
        
        
        // Read standard input to populate the FSM arrays
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
  
            nextLine = reader.readLine();
            //System.out.println(line);
            int counter = 0;

            while (nextLine != null) {

                split = nextLine.split(",");
                //System.out.println(split[2]);
                currStateNum.add(Integer.parseInt(split[0]));
                ch.add(split[1].charAt(0));
                next1.add(Integer.parseInt(split[2]));
                next2.add(Integer.parseInt(split[3]));

                nextLine = reader.readLine();
                counter++;
            }
        } 
        catch (Exception e) {
            System.err.println("Invalid piped in FSM file. Try again");
            e.printStackTrace();
        }
     

        try{
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            
            nextLine = reader.readLine();
            // While there are still lines to read from the file

            while (nextLine != null) {
                if (nextLine.length() > 0) {
                    // add to string
                    textFile.add(nextLine.trim());
                } 
                nextLine = reader.readLine();
            }

            reader.close();
        }
        catch (Exception e) {
            System.err.println(e);
        } 
        //System.out.println(textFile);

        //for each to run through each line of
        for (String line : textFile) {
            //System.out.println(line);
            int currState = 0;
            int symbol = 0;

            parentState = next1.get(currState);
            //serach fsm and return true if found
            if (searchLine(next1.get(currState), line, symbol)) {
                //prints only if found
                System.out.println(line);
            } 
        }
    }
    
    
    //search that is called regressively 
    public static boolean searchLine(int currState, String line, int symbol)
    {
        char c = line.charAt(symbol);

        if (next1.get(currState) == 0) {return true;}

        if (symbol >= line.length()) {return false;}

  
       
        //if next1 differs for current node
        if (next1.get(currState) != next1.get(currState)) {
            //set parent state
            parentState = next1.get(currState);
            //calls self else reset parentstate
            if (searchLine(next1.get(currState), line, symbol)) { return true; }
            else {

                parentState = next1.get(currState);
                //calls self
                if (searchLine(next1.get(currState), line, symbol)) { return true; }
                else { return false; }
            }
        }
        else {
            //if the charater is branching
            if (ch.get(currState) == (char)3) {

                //call self
                if (searchLine(next1.get(currState), line, symbol)) { return true; }
            }
            //if both equal "."
            else if (ch.get(currState) == c || ch.get(currState) == '.') {
                symbol++;
                //call self
                if (searchLine(next1.get(currState), line, symbol)) { return true; }
            }
            else {
                symbol++;
                //call self using parent state instead
                if (searchLine(parentState, line, symbol)) { return true; }
            }
        }
        //if no regression happens
        return false;
    }
    

}   
