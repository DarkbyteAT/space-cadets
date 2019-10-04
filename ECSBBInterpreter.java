package me.atlne.barebones;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

import javax.swing.JOptionPane;

//Class representing a BareBones (BB) program
public class ECSBBInterpreter {

	//Stores source code for program
	private String source;
	//Stores individual lines for program
	private String[] lines;
	//Stores a log of the state of the variables throughout the program
	private String log = "";
	//Stores map of all variables (Stored as integers) with their names
	private HashMap<String, Integer> variables;
	
	//Constructor for the program, takes in the source code for the program
	public ECSBBInterpreter(String source) {
		//Uses setter to set source and initialise lines array
		setSource(source);
		//Initialises variable mapping
		variables = new HashMap<>();
	}
	
	//Main method, gets user to input name of program to run then runs from file
	public static void main(String[] args) {
		//Gets file name from user and reads file into string, creates new program from string and runs program
		new ECSBBInterpreter(readFile(JOptionPane.showInputDialog(null, "Enter file name of BareBones program you'd like to run"))).run();
	}
	
	//Reads file from file name, returns contents as string
	public static String readFile(String fileName) {
		//Declares string to store file contents
		String contents = "";
		
		//Creates buffered reader to read file
		try(BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
			//Iterates over every line in reader
			for(Object line : reader.lines().toArray()) {
				//Adds line to contents
				contents += line.toString();
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
	
	//Runs program from start to end
	public void run() {
		//Runs from index 0 to length of lines - 1
		run(0, lines.length - 1);
		//Outputs that execution was successful
		JOptionPane.showMessageDialog(null, "Execution successful!");
		//Displays final log in message box
		JOptionPane.showMessageDialog(null, "OUTPUT:\n" + getVariableValues().trim());
		
		try {
			//Uses file writer to write log to file
			FileWriter logWriter = new FileWriter("log.txt");
			logWriter.write(log.trim());
			logWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
			//If error occurs, shows error dialogue box
			JOptionPane.showMessageDialog(null, "An error occurred whilst writing the log file!");
		}
		
		
		//Outputs log file creation message
		JOptionPane.showMessageDialog(null, "Log file outputted to \"log.txt\" containing details of variable states after very line was executed.");
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
			//Splits line into parts by spaces
			String[] parts = line.split(" ");
			
			//Checks what line does by testing first part case-insensitively
			if(parts[0].toLowerCase().equals("clear")) {	//Checks if clearing variable (sets value to 0)
				//Puts variable name into map with value of 0 (gotten from second part)
				variables.put(parts[1], 0);
			} else if(parts[0].toLowerCase().equals("incr")) {	//Checks if incrementing variable (adds 1 to value)
				//Gets variable name from second part
				String varName = parts[1];
				
				//Checks if variable in map
				if(variables.containsKey(varName)) {
					//Puts value of variable + 1 into map
					variables.put(varName, variables.get(varName) + 1);
				} else {
					//Puts value of 1 into map if not in map
					variables.put(varName, 1);
				}
			} else if(parts[0].toLowerCase().equals("decr")) {	//Checks if decrementing variables (subs 1 from value)
				//Gets variable name from second part
				String varName = parts[1];
				
				//Checks if variable in map
				if(variables.containsKey(varName)) {
					//Puts value of variable - 1 into map
					variables.put(varName, variables.get(varName) - 1);
				} else {
					//Puts value of -1 into map if not in map
					variables.put(varName, -1);
				}
			} else if(parts[0].toLowerCase().equals("while")) {	//Checks if start of while loop
				//Finds end statement associated with while by finding first one, adding on to the count for each while found first
				int endCount = 1, endStatementIndex = endIndex;
				
				//Loops over all lines in current block after while
				for(int j = i + 1; j <= endIndex; j++) {
					//Gets line currently checking in block in lowercase
					String blockLine = lines[j].trim().toLowerCase();
					
					//Checks if line is a while
					if(blockLine.startsWith("while")) {
						//Increments end count
						endCount++;
					} else if(blockLine.equals("end")) {
						//Decrements end count
						endCount--;
					}
					
					//Checks if end count reached 0
					if(endCount == 0) {
						//Sets end index to current iteration index
						endStatementIndex = j;
						//Breaks out of loop as end found
						break;
					}
				}
				
				//Gets variable being checked (condition variable)
				String condVar = parts[1];
				//Gets value it cannot be for loop to continue (end value)
				int endVal = Integer.parseInt(parts[3]);
				
				//Checks if conditional variable exists, if not, sets value to 0
				if(!variables.containsKey(condVar)) {
					variables.put(condVar, 0);
				}
				
				//Runs block between while conditional variable not equal to end value
				while(variables.get(condVar) != endVal) {
					//Runs block starting from index after current line and ending at last end
					run(i + 1, endStatementIndex - 1);
				}
			}
			
			//Adds current values of variables to log string
			log += "Line " + (i + 1) + ":\n" + getVariableValues() + "\n";
		}
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
		//Splits lines into array using ";" delimiter
		//Ignores last semicolon to avoid final empty line
		this.lines = this.source.substring(0, this.source.lastIndexOf(";")).split(";");
	}
}