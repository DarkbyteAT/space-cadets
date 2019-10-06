package me.atlne.ecschat;

import java.awt.BorderLayout;
import java.awt.Font;

import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public abstract class ChatWindow extends JFrame {
	private static final long serialVersionUID = 1L;
	
	//Stores font for chat area and input field
	public static final Font FONT = new Font("Consolas", Font.PLAIN, 20);
	//Stores chat area
	protected JTextArea chatWindow = new JTextArea();
	//Stores input bar for chat
	protected JTextField input = new JTextField();
	
	//Constructor for chat window, adds components and sets style
	public ChatWindow(String titleAddition) {
		//Sets title of window, adding addition to end of title
		super("ECS Chat - " + titleAddition);
		
		//Sends message when enter key is pressed and input field focused
		//Uses lambda operator
		input.addActionListener(e -> sendMessage());
		//Makes text are non editable
		chatWindow.setEditable(false);
		//Sets word wrap style for text area
		chatWindow.setWrapStyleWord(true);
		//Sets chat window and input field fonts
		chatWindow.setFont(FONT);
		input.setFont(FONT);
		//Sets window properties
		setSize(800, 480);
		setLayout(new BorderLayout());
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//Adds UI elements to window
		add(chatWindow, BorderLayout.CENTER);
		add(input, BorderLayout.SOUTH);
		setVisible(true);
	}
	
	//Abstract method for sending message when enter key pressed
	public abstract void sendMessage();

	public JTextArea getChatWindow() {
		return chatWindow;
	}

	public JTextField getInput() {
		return input;
	}
}