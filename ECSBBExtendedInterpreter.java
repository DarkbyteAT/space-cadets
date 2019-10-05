package me.atlne.barebones;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

//Class representing a BareBonesExtended (BBE) program
//Comments implemented by adding semicolon to lines starting with // if not ending with one already
//Added support for further blocks by finding block ends (multiple end delimiters)
//Added support for some syntax error logging
//Added support for setting variable values to other variables / numbers using recursive evaluation function
//Added support for basic mathematical operations
public class ECSBBExtendedInterpreter {

	//Stores regex to check if string is integer
	public static final Pattern INT_PATTERN = Pattern.compile("^-?\\d+$");
	//Stores source code for program
	private String source;
	//Stores individual lines for program
	private String[] lines;
	//Stores a log of the state of the variables throughout the program
	private String log = "";
	//Stores map of all variables (Stored as integers) with their names
	private HashMap<String, Integer> variables;
	
	//Constructor for the program, takes in the source code for the program
	public ECSBBExtendedInterpreter(String source) {
		//Uses setter to set source and initialise lines array
		setSource(source);
		//Initialises variable mapping
		variables = new HashMap<>();
	}
	
	//Main method, gets user to input name of program to run then runs from file
	public static void main(String[] args) {
		//Gets file name from user and reads file into string, creates new program from string and runs program
		new ECSBBExtendedInterpreter(readFile(JOptionPane.showInputDialog(null, "Enter file name of BareBones program you'd like to run"))).run();
	}
	
	//Helper method to read file from file name, returns contents of file as string
	public static String readFile(String fileName) {
		//Declares string to store file contents
		String contents = "";
		
		//Creates buffered reader to read file
		try(BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
			//Iterates over every line in reader
			for(Object line : reader.lines().toArray()) {
				//Adds line to contents
				contents += line.toString() + "\n";
			}
			
			//Closes reader after reading completed
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
			//If error occurs, shows error dialogue box
			JOptionPane.showMessageDialog(null, "An error occurred whilst reading file \"" + fileName + "\"!");
		}
		
		//Returns file contents
		return contents;
	}
	
	//Helper method to find if string matches any strings in set
	public static boolean matchesAny(String a, String[] set) {
		//Iterates over all strings in set
		for(String e : set) {
			//If element equal to a, returns true
			if(a.equals(e)) {
				return true;
			}
		}
		
		//Else returns false if no matches found
		return false;
	}
	
	//Helper method combining string array into string seperated by spaces from index specified onwards
	public static String combineSpaces(String[] parts, int start) {
		//String storing result
		String result = "";
		
		//Iterates over all parts from start onwards (inclusive)
		for(int i = start; i < parts.length; i++) {
			result += parts[i] + " ";
		}
		
		//Returns trimmed result to remove trailing space
		return result.trim();
	}
	
	//Writes log file
	public void writeLog() {
		try {
			//Uses file writer to write log to file
			FileWriter logWriter = new FileWriter("log.txt");
			logWriter.write(log.trim());
			logWriter.close();
			//Outputs log file creation message
			JOptionPane.showMessageDialog(null, "Log file outputted to \"log.txt\" containing details of variable states after very line was executed.");
		} catch (IOException e) {
			e.printStackTrace();
			//If error occurs, shows error dialogue box
			JOptionPane.showMessageDialog(null, "An error occurred whilst writing the log file!");
		}
	}
	
	//Helper method to display error message and create log file
	public void throwError(String message) {
		//Defines error message string
		String errorMessage = "Error: " + message;
		//Outputs error message
		JOptionPane.showMessageDialog(null, "Execution failed!\n" + errorMessage);
		//Adds error message to log
		log += errorMessage;
		//Writes log file
		writeLog();
		//Exits program
		System.exit(0);
	}
	
	//Runs program from start to end
	public void run() {
		//Runs from index 0 to length of lines - 1
		run(0, lines.length - 1);
		//Outputs that execution was successful
		JOptionPane.showMessageDialog(null, "Execution successful!");
		//Displays final log in message box
		JOptionPane.showMessageDialog(null, "OUTPUT:\n" + getVariableValues().trim());
		//Writes log file
		writeLog();
	}
	
	//Gets current values of variables
	public String getVariableValues() {
		//Declares string to store output
		String output = "";
				
		//Iterates over all variables in map's names and adds each value to string in format - "varName: value" per line
		for(String varName : variables.keySet()) {
			//Adds variable to output with value
			output += varName + ": " + variables.get(varName) + "\n";
		}
		
		return output;
	}

	//Method to run program between indices specified
	public void run(int startIndex, int endIndex) {
		//Iterates over lines
		for(int i = startIndex; i <= endIndex; i++) {
			//Gets line from array, removing whitespace
			String line = lines[i].trim();
			
			//Checks if line starts with "//" (comment)
			if(line.startsWith("//")) {
				//Ignores and continues to next line
				continue;
			}
			
			//Splits line into parts by spaces
			String[] parts = line.split(" ");
			//Gets operation code
			String opcode = parts[0].toLowerCase();
			
			//Checks what line does by testing first part case-insensitively
			if(opcode.equals("clear")) { //Checks if clearing variable (sets value to 0)
				//Puts variable name into map with value of 0 (gotten from second part)
				variables.put(parts[1], 0);
			} else if(opcode.equals("incr")) {	//Checks if incrementing variable (adds 1 to value)
				add(parts[1], 1);
			} else if(opcode.equals("decr")) {	//Checks if decrementing variables (adds -1 to value)
				add(parts[1], -1);
			} else if(opcode.equals("while")) { //Checks if start of while loop
				//Finds end statement associated with while
				int endStatementIndex = findBlockEnd("while", new String[] {"end"}, i, endIndex);
				//Gets variable being checked (condition variable)
				String condVar = parts[1];
				//Gets value it cannot be for loop to continue (end value)
				int endVal = evaluate(parts[3]);
				
				//Checks if conditional variable exists, if not, sets value to 0
				if(!variables.containsKey(condVar)) {
					variables.put(condVar, 0);
				}
				
				//Runs block between while conditional variable not equal to end value
				while(variables.get(condVar) != endVal) {
					run(i + 1, endStatementIndex - 1);
				}
			} else if(opcode.equals("set")) { //Checks if setting variable value
				//Sets variable from second part equal to following statement (all parts after second)
				variables.put(parts[1].trim(), evaluate(combineSpaces(parts, 2)));
			} else if(opcode.equals("add")) { //Checks if adding to variable
				//Checks if the variable exists
				if(variables.containsKey(parts[1].trim())) {
					//Sets variable from second part equal to itself plus following statement (all parts after second)
					variables.put(parts[1].trim(), variables.get(parts[1].trim()) + evaluate(combineSpaces(parts, 2)));
				} else {
					//Sets variable from second part equal to following statement (all parts after second)
					variables.put(parts[1].trim(), evaluate(combineSpaces(parts, 2)));
				}
			} else if(opcode.equals("sub")) { //Checks if subtracting from variable
				//Checks if the variable exists
				if(variables.containsKey(parts[1].trim())) {
					//Sets variable from second part equal to itself minus following statement (all parts after second)
					variables.put(parts[1].trim(), variables.get(parts[1].trim()) - evaluate(combineSpaces(parts, 2)));
				} else {
					//Sets variable from second part equal to -1 * following statement (all parts after second)
					variables.put(parts[1].trim(), -evaluate(combineSpaces(parts, 2)));
				}
			} else if(opcode.equals("mult")) { //Checks if multiplying variable by amount
				//Checks if the variable exists
				if(variables.containsKey(parts[1].trim())) {
					//Sets variable from second part equal to itself minus following statement (all parts after second)
					variables.put(parts[1].trim(), variables.get(parts[1].trim()) * evaluate(combineSpaces(parts, 2)));
				} else {
					//Sets variable from second part equal to 0
					variables.put(parts[1].trim(), 0);
				}
			} else if(opcode.equals("div")) { //Checks if dividing variable by amount
				//Checks if the variable exists
				if(variables.containsKey(parts[1].trim())) {
					//Sets variable from second part equal to itself minus following statement (all parts after second)
					variables.put(parts[1].trim(), variables.get(parts[1].trim()) / evaluate(combineSpaces(parts, 2)));
				} else {
					//Sets variable from second part equal to 0
					variables.put(parts[1].trim(), 0);
				}
			} else if(opcode.equals("mod")) { //Checks if modulating variable by amount (remainder division)
				//Checks if the variable exists
				if(variables.containsKey(parts[1].trim())) {
					//Sets variable from second part equal to itself minus following statement (all parts after second)
					variables.put(parts[1].trim(), variables.get(parts[1].trim()) % evaluate(combineSpaces(parts, 2)));
				} else {
					//Sets variable from second part equal to 0
					variables.put(parts[1].trim(), 0);
				}
			} else if(opcode.equals("exp")) { //Checks if exponentiating variable by amount
				//Gets evaluated amount from parts 2 and onward
				int power = evaluate(combineSpaces(parts, 2));
				
				//Checks if the variable exists
				if(variables.containsKey(parts[1].trim())) {
					//Sets variable from second part equal to itself minus following statement (all parts after second)
					variables.put(parts[1].trim(), (int) Math.pow(variables.get(parts[1].trim()), power));
				} else {
					//Sets variable from second part equal to 0 if power != 0 and 1 if power is 0 (lim{n->inf} (n^n) -> 1)
					variables.put(parts[1].trim(), power == 0 ? 1 : 0);
				}
			}
			
			//Adds current values of variables to log string
			log += "Line " + (i + 1) + ":\n" + getVariableValues() + "\n";
		}
	}
	
	//Evaluates given statement
	public int evaluate(String statement) {
		//Trims statement for sanitisation
		statement = statement.trim();
		
		//Checks if statement is numerical or not
		if(INT_PATTERN.matcher(statement).find()) { //In case of integer
			//Resets the regex matcher
			INT_PATTERN.matcher(statement).reset();
			return Integer.parseInt(statement);
		} else { //In case of variable
			//Checks if variable exists in map
			if(variables.containsKey(statement)) {
				//Returns variable value
				return variables.get(statement);
			} else {
				throwError("Variable \"" + statement + "\" not defined!");
				return 0;
			}
		}
	}
	
	//Adds amount to variable
	public void add(String varName, int amount) {
		//Checks if variable in map
		if(variables.containsKey(varName)) {
			//Puts value of variable + amount into map
			variables.put(varName, variables.get(varName) + amount);
		} else {
			//Puts amount into map if not in map
			variables.put(varName, amount);
		}
	}
	
	//Returns end statement index of block to run within bounds given (start being block statement's definition line)
	public int findBlockEnd(String blockType, String[] endDelims, int startIndex, int endIndex) {
		//Finds end statement associated with block by finding first one, adding on to the count for each block found first
		int endCount = 1, endStatementIndex = endIndex;
		
		//Loops over all lines in current block after block
		for(int i = startIndex + 1; i <= endIndex; i++) {
			//Gets line currently checking in block in lowercase
			String blockLine = lines[i].trim().toLowerCase();
			
			//Checks if line is of block type
			if(blockLine.startsWith(blockType)) {
				//Increments end count
				endCount++;
			} else if(matchesAny(blockLine, endDelims)) { //Checks if any end delimeters found
				//Decrements end count
				endCount--;
			}
			
			//Checks if end count reached 0
			if(endCount == 0) {
				//Sets end index to current iteration index
				endStatementIndex = i;
				//Breaks out of loop as end found
				break;
			}
		}
		
		return endStatementIndex;
	}

	public String getSource() {
		return source;
	}

	public String[] getLines() {
		return lines;
	}

	public HashMap<String, Integer> getVariables() {
		return variables;
	}

	public void setSource(String source) {
		//Trims source to remove unnecessary whitespace
		this.source = source.trim();
		//Splits source line-by-line
		String[] lines = this.source.split("\n");
		
		//Iterates over every line to check for comments
		for(int i = 0; i < lines.length; i++) {
			//Checks if line starts with "//" and not ending in ";"
			if(lines[i].trim().startsWith("//") && !lines[i].trim().endsWith(";")) {
				//Adds a semicolon to the line so treated as individual line that is ignored in run method
				lines[i] += ";";
			}
		}
		
		//Sets source to empty string
		this.source = "";
		//Reconstructs source from lines by iterating over and adding new lines
		for(String line : lines)
			this.source += line + "\n";
		//Trims source
		this.source.trim();
		
		//Splits lines into array using ";" delimiter
		//Ignores last semicolon to avoid final empty line
		this.lines = this.source.substring(0, this.source.lastIndexOf(";")).split(";");
	}
}