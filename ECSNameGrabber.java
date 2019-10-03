package me.atlne.ecsnamegrab;

import java.io.IOException;

import javax.swing.JOptionPane;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**Using JSoup version 1.12.1 - jsoup-1.12.1.jar*/
public class ECSNameGrabber {																																	//Declares the main class for the ECS Name Grabber program, contains main method.
	public static void main(String[] args) {																													//Main method declaration, entry point for program
		String id = JOptionPane.showInputDialog(null, "Enter the email ID of the person you would like to know the name of."); 	 								//Gets the email ID of the person to find using a GUI box
		
		try {
			Document page = Jsoup.connect("https://www.ecs.soton.ac.uk/people/" + id.toLowerCase().trim()).get();												//Attempts to get the page for the ID entered
			String title = page.title().trim();																													//Gets the title of the HTML page, which contains the name of the person
			String name = (title.toLowerCase().trim().startsWith("people") ? "Person not found!" : "Name: " + title.substring(0, title.indexOf('|')).trim());	//Gets their name by substringing until the first "|" character is found - indicating the end of the name, if the start begins with "People", there is no name.
			JOptionPane.showMessageDialog(null, name);																											//Prints the name / error message into an error dialogue
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Page for ID \"" + id + "\" not found!");																		//Prints an error message if the page was not found
		}
	}
}
