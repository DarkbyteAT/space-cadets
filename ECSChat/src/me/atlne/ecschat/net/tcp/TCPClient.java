package me.atlne.ecschat.net.tcp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

import me.atlne.ecschat.net.Listener;

public class TCPClient extends TCPConnection {
	
	//Constructor for client, connects to IP and port specified
	//Creates a TCP connection using these parameters
	public TCPClient(Listener listener, final String ip, final int port) throws IOException {
		super(SocketChannel.open(new InetSocketAddress(ip, port)), listener, "SERVER");
	}
}