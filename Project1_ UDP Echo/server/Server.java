import java.io.*;
import java.net.*;

public class Server {
	public static void main(String[] args){
		if(args.length != 1){
			System.out.println("required argument:\"portNum\"");
			System.exit(1);
		}
		int port = Integer.parseInt(args[0]);


		DatagramSocket sock = null;
		try {
			sock = new DatagramSocket(port);
		} catch (SocketException e) {
			e.printStackTrace();
		}
		System.out.println("connection waiting...");

		SharedData shared = new SharedData();
		RecvThread recv = new RecvThread(sock, shared);
		SendThread send = new SendThread(sock, shared);

		recv.start();
		send.start();
	}
}

class RecvThread extends Thread {
	DatagramSocket sock;
	SharedData shared;

	public RecvThread(DatagramSocket ds, SharedData sd){
		sock = ds;
		shared = sd;
	}

	public void run() {
		while(true) {
			try {
				String msg;
				byte[] recvBuf = new byte[1024];
				DatagramPacket receivePacket = new DatagramPacket(recvBuf, recvBuf.length);
				sock.receive(receivePacket);
				msg = new String(receivePacket.getData());
				System.out.println("RECEIVED>> " + msg);
				shared.setPacket(receivePacket);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}

class SendThread extends Thread {
	DatagramSocket sock;
	SharedData shared;

	public SendThread(DatagramSocket ds, SharedData sd){
		sock = ds;
		shared = sd;
	}

	public void run() {
		while(true){
			DatagramPacket sendPacket = shared.getPacket();
			try {
				sock.send(sendPacket);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}

class SharedData {
	boolean isSendReady = false;
	DatagramPacket sharedPacket = null;

	public synchronized DatagramPacket getPacket(){
		while(!isSendReady){
			try{
				wait();
			} catch(InterruptedException e){
			}
		}
		isSendReady = false;
		notifyAll();
		return sharedPacket;
	}

	public synchronized void setPacket(DatagramPacket packet){
		while(isSendReady){
			try{
				wait();
			} catch(InterruptedException e){
			}
		}
		isSendReady = true;
		sharedPacket = packet;
		notifyAll();
	}
}
