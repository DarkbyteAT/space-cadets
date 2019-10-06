package me.atlne.ecschat.net.tcp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ConcurrentHashMap;

import me.atlne.ecschat.net.Listener;
import me.atlne.ecschat.net.Packet;

public class TCPServer extends Thread {
	
	//Constructor for server, takes in port for server to listen on for TCP connection
	public TCPServer(Listener listener, final int port) throws IOException {
		this.listener = listener;
		//Opens channel and binds port
		this.channel = ServerSocketChannel.open();
		this.channel.bind(new InetSocketAddress(port));
		this.port = port;
		this.connections = new ConcurrentHashMap<>();
		this.running = true;
		this.start();
	}
	
	//Stores whether server is running or not
	public volatile boolean running;
	//Stores TCP port
	public final int port;
	
	//Stores listener for server
	protected volatile Listener listener;
	//Stores server socket for connecting to clients on TCP
	protected volatile ServerSocketChannel channel;
	//Stores TCP and UDP connections in conjunction with a unique key for each connection
	protected volatile ConcurrentHashMap<String, TCPConnection> connections;
	
	//Closes server and connections
	public void close() throws IOException {
		//Sets running flag to false
		running = false;
		//Closes all connections
		for(TCPConnection con : connections.values())
			con.close();
		//Clears connections map
		connections.clear();
		//Closes server channel
		channel.close();
	}
	
	//Sends a message to a client
	public void send(final String id, final String data) throws IOException {
		//Loops through each client
		for(final TCPConnection con : connections.values()) {
			//Checks if connection id matches
			if(con.id.equals(id)) {
				//Sends packet to that connection
				con.send(data);
			}
		}
	}
	
	//Broadcasts message to all clients
	public void broadcast(final String data) throws IOException {
		//Loops through each client
		for(final TCPConnection con : connections.values()) {
			//Sends packet to connection iterated over
			con.send(data);
		}
	}
	
	//Runs server loop
	@Override
	public void run() {
		//Adds any connections to TCP connections hashmap
		//Gets TCP server channel and creates connection from it
		//Creates channel to listen on new thread
		while(running && channel.isOpen()) {
			try {
				
				//Checks if channel is still open
				if(channel.isOpen()) {
					//Gets client depending on type of server channel being used
					SocketChannel client = channel.accept();
					//Generates an id for client using IP socket is connected to
					String id = Packet.hash(((InetSocketAddress) client.getLocalAddress()).getAddress().getHostAddress());
					//Only adds client if there was one accepted (i.e client isn't null)
					if(client != null) {
						//Disables Nagle's algorithm for packets
						client.setOption(StandardSocketOptions.TCP_NODELAY, true);
						//Opens channel for connections
						client.configureBlocking(false);
						connections.put(id, new TCPConnection(client, listener, id));
					}
				}
			} catch (IOException e) {}
		}
	}
	
	//Getters
	public ConcurrentHashMap<String, TCPConnection> getConnections() {
		return this.connections;
	}
}