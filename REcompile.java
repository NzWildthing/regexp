import java.security.InvalidParameterException;
import java.util.ArrayList;

import javax.naming.InitialContext;

/*
Brogan Jowers-Wilding   1538252
George Elstob           1534323

Rules

E -> T
E -> TE

T -> F
T -> F*
T -> F+
T -> F?
T -> F|T        standard alternation 

F -> \v         backslash
F -> .          wildcard
F -> (E)
F -> [A]        alternation
F -> v          any literal 

A -> F          as many alterations as you want 
A -> FA

*/

public class REcompile{

    private static String regex = "";
    private static int pointer; 
    private static int state; 
    private static ArrayList<Integer> stateL = new ArrayList<>() {}; 
    private static ArrayList<String> type = new ArrayList<>(); 
    private static ArrayList<Integer> next1 = new ArrayList<>();
    private static ArrayList<Integer> next2 = new ArrayList<>();

    private static char[] operators = {'*', '(', ')', '+', '?'};

    public static void main(String[] args){
        //Takes the regular expression as input 
        if(args.length > 1){
            System.out.println("Program only takes a singular regular expression as an input argument");
            return;
        }

        int startState;
        
        //Saves the regex from input 
        regex = args[0].toString();

        //Sets the pointer and start state
        pointer = 0; 
        state = 1;

        //Sets the intial state to be empty 
        setState(0, "initial", 0, 0);

        //Begins parsing the regex 
        startState = expression();

        //Sets the start state of the machine
        next1.set(0, startState);
        next2.set(0, startState);

        //Sets the final state
        setState(state, "final", state, state);

        //Prints out the fsm to the console
        print();

    }
    
    //First tests the expression part section of our defined regular grammar 
    public static int expression(){
        int start = term();
        //Makes sure we havn't finished parsing  
        if(pointer < regex.length()){
            //Checks to see if we have a valid starting regex vocab 
            if(regex.charAt(pointer) == '(' || isLiteral(regex.charAt(pointer))){
                //Don't need to save start state here as it will not be the start state of the machine
                expression();
            }
            else{
                error();
            }
        }
        //Otherwise reached end of regex 
        //Returns start state of the machine
        return start;
    }

    public static int term(){
        //Setup some variables
        int start, branch1, branch2, prev;
        start = branch1 = factor();
        //Last state built 
        prev = state - 1;

        //Make sure wer havn't finished parsing
        if(pointer < regex.length()){
            //Checks first prescendence closure 
            if(regex.charAt(pointer) == '*'){
                //Create a branching state on of them is the state returned from factor, the other is the next 
                //Final state of branching machine will already be assumed by factor to be the next machine craeted 
                start = setState(state, "br", state, state +1);
                pointer++;
                state++;
            }
            else if(regex.charAt(pointer) == '?'){
                //If previous machine was not branching set its pointers to this machine
                if(next1.get(prev) == next2.get(prev))
                {
                    next1.set(prev, state);
                    next2.set(prev, state);
                }
                else{
                    //Otherwise just change one pointer to this machine
                    next1.set(prev, state);
                }
                //Create a new branching machine pointing to the factor 
                start = setState(state, "br", branch1, state +1);
            }
        }

        return start;
    }

    public static int factor(){
        int start = -1; 

        //Make sure not at end of regex after the backslash 
        if(regex.charAt(pointer) == '\\' && (pointer+1) < regex.length()){
            //Consume the character 
            pointer ++;
            //Sets a new state 
            start = setState(state, Character.toString(regex.charAt(pointer)), state + 1, state + 1);
            //Updates state and consumes next character 
            pointer++;
            state++;
        }
        //When the character inputted is a wildcard it can match any character 
        else if(regex.charAt(pointer) == '.'){
            start = setState(state, "wc", state + 1, state + 1);
            pointer ++; 
            state ++;
        }
        else if(regex.charAt(pointer) == '('){
            //Consume the initial bracket 
            pointer ++;
            start = expression();
            //After another expression has occured we should be at the closing brackets if not throw an error
            if(regex.charAt(pointer)!= ')'){
                error();
            }
            //Consume the closing bracket 
            pointer ++;
        }
        else if(regex.charAt(pointer) == '['){
            //Consume the initial bracket 
            pointer ++;
            //Complete an alternation check 
            start = alternation();
        }
        else if(isLiteral(regex.charAt(pointer))){
            start = setState(state, Character.toString(regex.charAt(pointer)), state + 1, state + 1);
            pointer++;
            state++;
        }
        else{
            error();
        }
        //Returns start whatever value has been calculated from each case 
        return start;
    }

    public static int alternation(){
        int start; 
        String literals = "";

        //Loop through each of the literals until we reach a closing square bracket
        while(regex.charAt(pointer) != ']'){
            literals = literals + (regex.charAt(pointer) + "");
            pointer++;

            //If no ending bracket, invalid regex
            if(pointer >= regex.length()){ 
                error();
            }
        }
        //Once reached the closing bracket consume it 
        pointer++;
        start = setState(state, literals, state + 1, state + 1);
        state ++;
        return start;
    }


    //####################Extra Methods##############//

    //Checks if the given character is a valid literal or not 
    private static boolean isLiteral(char c){
		for(char ch: operators) {

			if (ch == c) {

				return false;
			}
		}
		return true;
	}

    //Sets the state of the machine
    private static int setState(int s, String c, int n1, int n2) {
        stateL.add(s);
        type.add(c);
        next1.add(n1);
        next2.add(n2);
        return s;
    }

    //Prints out an error to the user when the regex is invalid
    private static void error() {
		System.err.println("Input a valid regexp");
        throw new InvalidParameterException("Input a valid regexp");
	}

    private static void print(){
        int count = 0;
        while(count != type.size()){
            System.out.println(stateL.get(count) + " " + type.get(count) + " "  + " " + next1.get(count) + " " + next2.get(count));
            count ++;
        }
    }
}