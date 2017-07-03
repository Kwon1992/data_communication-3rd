import java.io.*;
import java.net.*;

public class Client {
	public static void main(String[] args){
		if(args.length !=2) {
			System.out.println("required arguments:\"IP\" \"portNum\"");
			System.exit(1);
		}

		String ip = args[0];
		int port = Integer.parseInt(args[1]);
		InetAddress addr = null;
		DatagramSocket sock = null;

		try {
			addr = InetAddress.getByName(ip);
		} catch (UnknownHostException e) {
			System.out.println("UNKNOWN HOST: " + ip);
			System.exit(1);
		}

		try {
			sock = new DatagramSocket();
			sock.setSoTimeout(2000); //2sec Timeout
		} catch (SocketException e) {
			e.printStackTrace();
		}

		SharedData shared = new SharedData();
		SendThread send = new SendThread(sock, addr, port, shared);
		RecvThread recv = new RecvThread(sock, shared);

		send.start();
		recv.start();
	}
}


class SendThread extends Thread {
	DatagramSocket sock;
	InetAddress addr;
	int port;
	SharedData shared;

	public SendThread(DatagramSocket ds,InetAddress addr,int port,SharedData sd){
		sock = ds;
		this.addr = addr;
		this.port = port;
		shared = sd;
	}

	public void run() {
		try {
			BufferedReader readBuf = new BufferedReader(new InputStreamReader(System.in));
			String msg;
			while((msg = readBuf.readLine()) != null){
				DatagramPacket sendPacket = new DatagramPacket(msg.getBytes(), msg.getBytes().length,addr,port);
				sock.send(sendPacket);
				shared.setPacket(sendPacket);		
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	if(sock!=null) sock.close();
	}
}

class RecvThread extends Thread {
	DatagramSocket sock;
	SharedData shared;

	public RecvThread(DatagramSocket ds, SharedData sd){
		sock = ds;
		shared = sd;
		shared.socket = ds;
	}

	public void run() {
		while(true){
			try {
				byte[] sendBuf = new byte[1024]; // arbitrary size of byte.
				DatagramPacket receivePacket = new DatagramPacket(sendBuf,sendBuf.length);
				shared.waitRecv();
				sock.receive(receivePacket);
				System.out.println("ACK received");
				shared.setRecvReady(false);
			} catch (SocketTimeoutException e) {
				shared.setTimeout();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}

class SharedData {
	boolean isRecvReady = false;
	boolean isRetransmit = false;
	DatagramPacket sharedPacket = null;
	DatagramSocket socket = null;

	public synchronized void setPacket(DatagramPacket packet){
		while(isRecvReady){
			try{
				wait();
			} catch(InterruptedException e){
			}
		}
		isRecvReady = true;
		sharedPacket = packet;
		notifyAll();
		try{
			wait();
		} catch(InterruptedException e) {
		}
	}

	public synchronized void waitRecv() {
		while(!isRecvReady){
			try {
				wait();
			} catch (InterruptedException e) {
			}
		}
	}

	public synchronized void setTimeout(){
		boolean timedOut = true;
		byte[] receiveData = new byte[1024];
		System.out.println("Timeout... Retransmit message!");
		while(timedOut){
			try{
				socket.send(sharedPacket);
				DatagramPacket received = new DatagramPacket(receiveData, receiveData.length);
				socket.receive(received);
				System.out.println("ACK received");
				timedOut = false;
				isRecvReady = false;
			} catch(SocketTimeoutException e){
				System.out.println("Timeout... Retransmit message!");
			} catch(Exception e){
				e.printStackTrace();
			}
		}
		notifyAll();
	}

	public void setRecvReady(boolean value) {
		isRecvReady = value;
	}
}
