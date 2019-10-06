package me.atlne.ecschat.net.tcp;

import java.io.IOException;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import me.atlne.ecschat.net.Listener;
import me.atlne.ecschat.net.Packet;

public class TCPConnection extends Thread {
	
	//Constructor for connection, initialises connection to server using socket channel
	//Takes in listener to output received packets to
	public TCPConnection(SocketChannel channel, Listener listener, String id) throws SocketException {
		this.channel = channel;
		this.listener = listener;
		this.id = id;
		//Starts thread for connection
		this.start();
	}
	
	//Stores size of packets
	public static final int PACKET_SIZE = 1024;
	
	//Stores ID of connection
	public volatile String id;
	//Stores ping of client (RTT ping) as well as time ping was sent
	public volatile long ping, timeSincePing;
	
	//Stores channel
	protected volatile SocketChannel channel;
	//Stores connector
	protected volatile Listener listener;
	
	//Sends data to client
	public void send(final String data) throws IOException {
		//Writes packet as a byte buffer, using data from packet and converting in to bytes
		//Adds an end-of-packet footer to data
		if(channel.isConnected()) {
			channel.write(ByteBuffer.wrap((data + "%EOP%").getBytes()));
		}
	}
	
	//Sends a ping to server
	public void ping() throws IOException {
		//Pings server
		this.send("!ping");
		//Updates time since ping
		timeSincePing = System.currentTimeMillis();
		//System.out.println("Ping!");
	}
	
	//Ticks from client
	@Override
	public void run() {
		//Tries to initialise a connection
		try {
			//Runs as long as channel is open
			while(channel.isOpen()) {
				//Creates a new byte buffer to create input
				ByteBuffer buffer = ByteBuffer.allocate(PACKET_SIZE);
				//Stores result, -1 by default to assume that channel couldn't be read
				//Checks which type of supported channel channel is (DatagramChannel or SocketChannel - UDP vs TCP)
				//Uses this to determine where to read result
				//Creates a string from buffer and passes string into connector's function
				//Only passes data if there's info to read (if result isn't -1)
				if(channel.isOpen() && channel.read(buffer) != -1) {
					//Creates packet from received data and id
					String packet = new String(buffer.array()).trim();
					
					//If packet is a ping, returns a pong back
					if(packet.contains("!ping%EOP%")) {
						send("!pong");
					} else if(packet.contains("!pong%EOP%")) { //If packet is a pong, calculates ping
						//Calculates ping from current time and time since ping was sent
						ping = System.currentTimeMillis() - timeSincePing;
						//System.out.println("Pong!\nPING: " + ping + "ms");
					} else if(!packet.trim().isEmpty()) { //Checks if packet's data isn't just a blank string or whitespace
						//Sends packet to listener
						//Finds all packets received by listener by looping through as long as packet contains an end-of-packet footer
						while(packet.contains("%EOP%")) {
							//Passes in portion before end-of-packet footer
							listener.onReceive(new Packet(id, packet.substring(0, packet.indexOf("%EOP%"))));
							//Removes that portion from packet string
							packet = packet.substring(packet.indexOf("%EOP%") + "%EOP%".length());
						}
					}
				}
			}
			
			//Closes connection
			close();
		} catch (IOException e) {
			System.err.println("Error receiving data over TCP!");
		}
	}
	
	//Closes connection
	public void close() throws IOException {
		//Closes channel
		channel.close();
	}
	
	//Getters
	public SocketChannel getChannel() {
		return this.channel;
	}
}