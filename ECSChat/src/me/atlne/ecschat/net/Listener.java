package me.atlne.ecschat.net;

import java.io.IOException;

public interface Listener {
	//Runs when a packet is received
	void onReceive(Packet packet) throws IOException;
}