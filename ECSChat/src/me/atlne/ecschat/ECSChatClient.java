package me.atlne.ecschat;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.swing.JOptionPane;

import me.atlne.ecschat.net.Listener;
import me.atlne.ecschat.net.Packet;
import me.atlne.ecschat.net.tcp.TCPClient;

public class ECSChatClient extends ChatWindow implements Listener {
	public ECSChatClient() {
		super("Client");
	}

	private static final long serialVersionUID = 1L;
	
	//Stores chat client instance
	private TCPClient tcpClient;
	
	//Main method, starts client and opens window
	public static void main(String[] args) {
		//Creates instance of this class, creating window as well
		ECSChatClient client = new ECSChatClient();
		
		try {
			//Starts TCP client on port 6667 (de facto standard for IRC)
			TCPClient tcpClient = new TCPClient(client, JOptionPane.showInputDialog(client, "Enter chat server IP"), 6667);
			//Sends username to server
			tcpClient.send(JOptionPane.showInputDialog(client, "Enter desired username"));
			//Sets client field in ECSChatClient instance to newly created TCP client
			client.tcpClient = tcpClient;
			
			//Adds listener to window to close client when window closed
			client.addWindowListener(new WindowAdapter() {
				//Overrides close operation to close client
				@Override
				public void windowClosing(WindowEvent event) {
					//Closes client before continuing closing
					try {
						tcpClient.close();
					} catch (IOException e) {
						e.printStackTrace();
						JOptionPane.showMessageDialog(client, "Error shutting down client!");
					}
					
					super.windowClosing(event);
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
			//Prints error message if client couldn't start
			JOptionPane.showMessageDialog(client, "Error connecting to server!");
		}
	}

	//Method for how client should deal with incoming packets
	@Override
	public void onReceive(Packet packet) throws IOException {
		//Shows message in chat area and adds new line
		chatWindow.append(packet.data.trim() + "\n");
	}

	//Sends message to all clients
	@Override
	public void sendMessage() {
		//Checks if text field has valid input
		if(!input.getText().trim().isEmpty()) {
			//Broadcasts message to all users
			try {
				tcpClient.send(input.getText().trim());
			} catch (IOException e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(this, "Error sending message!");
			}
		}
		
		//Clears input field text
		input.setText("");
	}
}