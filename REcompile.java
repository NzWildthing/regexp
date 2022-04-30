import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;

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
        setState(state, "final", -1, -1);

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
        }
        //Otherwise reached end of regex 
        //Returns start state of the machine
        return start;
    }

    public static int term(){
        //Setup some variables
        int start, f1, f2, prev;
        start = f1 = factor();
        //Last state built 
        prev = state - 1;

        //Make sure wer havn't finished parsing
        if(pointer < regex.length()){
            //0 or many, closure *
            if(regex.charAt(pointer) == '*'){
                    //Creates a temporary state
                    int temp = state - 2;
    
                    //Creates a preceding branching state
                    setState(state, "br", f1, state + 1);
    
                    //Adjusts previous state values 
                    if (type.get(temp) == "br") {
                        updateState(temp, type.get(temp), next1.get(temp), state);
                        pointer++;
                        start = state;
                        state++;
                    } //else if (isLiteral((type.get(temp)).charAt(0)) || Arrays.asList(operators).contains(type.get(temp)))
                    //Otherwise its a literal and needs to be directed to the branching state we created  
                    else{ 
                        updateState(temp, type.get(temp), state, state);
                        pointer++;
                        start = state;
                        state++;
                    }
            }
            //0 or 1, ?
            else if(regex.charAt(pointer) == '?'){
               //Stores the previous state again 
               int temp = state - 2;

               //Creates a preceding branching state
               setState(state, "br", f1, state + 1);

               //Adjusts previous state values 
               if(type.get(temp) == "br") {
                   //Adjusts both previous states, so that it reflects the question mark 
                   //rules of 0 or 1, so either skip the preceding character or consume it
                   updateState(temp, type.get(temp), next1.get(temp), state); 
                   updateState(prev, type.get(prev), state + 1, state + 1); 
                   pointer++;
                   start = state;
                   state++;
               }
               //Otherwise its a literal and needs to be attached to the branching state we created 
                else {
                   //Either one and continue parsing 
                   updateState(temp, type.get(temp), state, state);
                   //Or none and continue parsing 
                   updateState(prev, type.get(prev), state + 1, state + 1); 
                   pointer++;
                   start = state;
                   state++;
               }
            }
            //1 or many, +
            else if(regex.charAt(pointer) == '+'){
                //Creates a branching state that allows for either going back to the plus or consuming it and 
                //continuing parsing 
                start = setState(state, "br", f1, state + 1);
                pointer++;
                state++;
            }
            //Alternation either one or the other r|e
            else if(regex.charAt(pointer) == '|'){
                //Creates a new branching machine with either the previous factor or the next 
                setState(state, "br", f1, state + 1);

                //Sets the starting state of the alternation to point to our new alternating branch 
                updateState(f1 - 1, type.get(f1-1), state, state);

                pointer++;
                state++;

                //Gets the term that is in alternation F|T 
                f2 = term();

                //Adjust previous state values 
                if(type.get(prev) == "br"){
                    updateState(prev, type.get(prev), next1.get(prev), state);
                }
                else{
                    updateState(prev, type.get(prev), state, state);
                }

                //Updates the value of the start state 
                start = f2 - 1;
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
        stateL.add(s, s);
        type.add(s, c);
        next1.add(s, n1);
        next2.add(s,n2);
        return s;
    }

    //Updates the state of the machine
    private static int updateState(int s, String c, int n1, int n2) {
        stateL.set(s, s);
        type.set(s, c);
        next1.set(s, n1);
        next2.set(s,n2);
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