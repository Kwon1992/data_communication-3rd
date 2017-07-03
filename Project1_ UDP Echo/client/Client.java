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
		} catch (SocketException e) {
			e.printStackTrace();
		}

		SendThread send = new SendThread(sock,addr,port);
		RecvThread recv = new RecvThread(sock);

		send.start();
		recv.start();
	}
}


class SendThread extends Thread {
	DatagramSocket sock;
	InetAddress addr;
	int port;

	public SendThread(DatagramSocket ds,InetAddress addr,int port){
		sock = ds;
		this.addr = addr;
		this.port = port;
	}

	public void run() {
		try {
			System.out.println("Type \"quit\" to terminate program");
			BufferedReader readBuf = new BufferedReader(new InputStreamReader(System.in));
			String msg;
			while((msg = readBuf.readLine()) != null){
				if (msg.equals("quit")) break;
				DatagramPacket sendPacket = new DatagramPacket(msg.getBytes(), msg.getBytes().length,addr,port);
				sock.send(sendPacket);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	if(sock!=null) sock.close();
	}
}

class RecvThread extends Thread {
	DatagramSocket sock;

	public RecvThread(DatagramSocket ds){
		sock = ds;
	}

	public void run() {
		try {
			while(true){
				byte[] sendBuf = new byte[1024]; // arbitrary size of byte.
				DatagramPacket receivePacket = new DatagramPacket(sendBuf,sendBuf.length);
				sock.receive(receivePacket);
				System.out.println("Echo>> " + new String(receivePacket.getData()));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
