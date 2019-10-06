package me.atlne.ecschat;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.JOptionPane;

import me.atlne.ecschat.net.Listener;
import me.atlne.ecschat.net.Packet;
import me.atlne.ecschat.net.tcp.TCPServer;

public class ECSChatServer extends ChatWindow implements Listener {
	public ECSChatServer() {
		super("Server");
	}

	private static final long serialVersionUID = 1L;
	
	//Stores chat server instance
	private TCPServer tcpServer;
	//Stores a map of connection IDs with usernames
	private ConcurrentHashMap<String, String> usernames = new ConcurrentHashMap<>();
	
	//Main method, starts server and opens window
	public static void main(String[] args) throws IOException {
		//Creates instance of this class, creating window as well
		ECSChatServer server = new ECSChatServer();
		//Starts TCP server on port 6667 (de facto standard for IRC)
		TCPServer tcpServer = new TCPServer(server, 6667);
		//Sets server field in ECSChatServer instance to newly created TCP server
		server.tcpServer = tcpServer;
		
		//Adds listener to window to close server when window closed
		server.addWindowListener(new WindowAdapter() {
			//Overrides close operation to close server
			@Override
			public void windowClosing(WindowEvent event) {
				//Closes server before continuing closing
				try {
					tcpServer.close();
				} catch (IOException e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(server, "Error shutting down server!");
				}
				
				super.windowClosing(event);
			}
		});
	}

	//Method for how server should deal with incoming packets
	@Override
	public void onReceive(Packet packet) throws IOException {
		//Checks if packet id is in map
		if(usernames.containsKey(packet.id)) {
			//Checks if message isn't empty (or just whitespace)
			if(!packet.data.trim().isEmpty()) {
				//Stores message with name header
				String out = "[" + usernames.get(packet.id) + "]: " + packet.data.trim();
				//Broadcasts message to other users
				tcpServer.broadcast(out);
				//Prints message received to chat window
				chatWindow.append(out + "\n");
			}
		} else {
			//Checks if packet isn't empty (or just whitespace)
			if(!packet.data.trim().isEmpty()) {
				//Sets name to first packet
				usernames.put(packet.id, packet.data.trim());
				//Creates join message string
				String joinMessage = "User \"" + packet.data.trim() + "\" has joined!";
				//Sends join message to other players
				tcpServer.broadcast(joinMessage);
				//Appends join message to text area
				chatWindow.append(joinMessage + "\n");
			} else {
				//If empty, or just whitespace, responds by sending error message to user
				tcpServer.send(packet.id, "[SERVER]: Invalid name!");
			}
		}
	}

	//Sends message to all clients
	@Override
	public void sendMessage() {
		//Checks if text field has valid input
		if(!input.getText().trim().isEmpty()) {
			//Broadcasts message to all users
			try {
				tcpServer.broadcast("[SERVER]: " + input.getText().trim());
			} catch (IOException e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(this, "Error sending message!");
			}
		}
		
		//Adds message to text area
		chatWindow.append("[SERVER]: " + input.getText().trim() + "\n");
		//Clears input field text
		input.setText("");
	}
}