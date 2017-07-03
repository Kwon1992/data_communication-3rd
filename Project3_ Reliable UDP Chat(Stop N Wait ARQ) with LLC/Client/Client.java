package Client;

import LLC.*;
import java.io.*;
import java.net.*;
import java.util.*;
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
		SendThread send = new SendThread(sock, addr, port);
		send.start();
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
		InetAddress ip;
		byte[] myMac = null;
		byte[] destMac = null;
		int length;
		byte sendSequence = (byte)0;
		byte recvSequence = (byte)1;

		//first -> send SAMBE//
		try {
			ip = InetAddress.getLocalHost();
			NetworkInterface network = NetworkInterface.getByInetAddress(ip);
			myMac = network.getHardwareAddress();


			//formatting SABME U-Frame//
			UFrame SABME = new UFrame((byte)UFrame.SABME);
			SABME.setSourceAddress(myMac);
			SABME.setDestAddress(new byte[6]);
			length = 22;
			byte[] lengthPDU = intToByteArray(length);
			SABME.setLengthPDU(lengthPDU);
			byte[] serializedFrame = SABME.toByteArray();
			DatagramPacket packetSABME = new DatagramPacket(serializedFrame, serializedFrame.length, addr, port);

			byte[] recvBuf = new byte[65535];
			DatagramPacket recvPacket = new DatagramPacket(recvBuf, recvBuf.length);

			for(int count = 0 ; count < 3 ; count++) {
				try {
					sock.send(packetSABME);
					//After send SABME -> receive UA//
					sock.receive(recvPacket);

					int recvPacketDataLength = recvPacket.getLength();
					byte[] recvUA = Arrays.copyOfRange(recvPacket.getData(),0,recvPacket.getLength());
					LLCBitSet recvBitSet = LLCBitSet.fromBytes(recvUA);

					Frame unknownFrame = Frame.fromByteArray(recvBitSet);
					if(unknownFrame instanceof UFrame) {
						UFrame UFrame = (UFrame)unknownFrame;
						if(UFrame.getCommandType() == UFrame.UA) {
							System.out.println("Get UFRAME(UA)");
							destMac = UFrame.getSourceAddress();
							break;
						}
					} else {
						count = -1;
						continue;
					}
				} catch (SocketTimeoutException e) {
					System.out.println("Timeout(Connection:Resend SABME) [" + (count+1) +"/3]");
				}
			}
		} catch (Exception e) { e.printStackTrace(); }

		try {
			BufferedReader readBuf = new BufferedReader(new InputStreamReader(System.in));
			String msg;
			while((msg = readBuf.readLine()) != null) {
				//setting iframe for send...
				LLCBitSet msgBitSet = new LLCBitSet();
				msgBitSet.append(msg.getBytes());
				IFrame msgIFrame = new IFrame(msgBitSet);
				length = 22 + msg.length();
				byte[] lengthPDU = intToByteArray(length);
				msgIFrame.setDestAddress(new byte[6]); // originally destMac. .. for test new
				msgIFrame.setSourceAddress(myMac);
				msgIFrame.setLengthPDU(lengthPDU);
				msgIFrame.setControl(recvSequence, (byte)0, sendSequence);

				byte[] frameRes = msgIFrame.toByteArray();
				DatagramPacket sendPacket = new DatagramPacket(frameRes, frameRes.length, addr, port);
				sock.send(sendPacket);

				byte[] recvBuf = new byte[65535];
				DatagramPacket recvPacket = new DatagramPacket(recvBuf, recvBuf.length);
				System.out.println("**************************************************");
				for(int count = 0; count < 3; count++) {
					try {
						byte[] recvMsg;
						sock.receive(recvPacket);
						recvMsg = Arrays.copyOfRange(recvPacket.getData(),0,recvPacket.getLength());
						LLCBitSet recvBitSet = LLCBitSet.fromBytes(recvMsg);
						Frame unknownFrame = Frame.fromByteArray(recvBitSet);
						if(unknownFrame instanceof SFrame) {
							System.out.println("Get SFRAME!");
							SFrame sframe = (SFrame)unknownFrame;
							if(sframe.getCommandType() == LLC.SFrame.RR) {
								System.out.println("Recv S-Frame: RR");
								if(sframe.getRecvSequence() == (recvSequence)) {
									System.out.println("Send Complete...");
									sendSequence++;
									recvSequence++;
									break;
								}
							} else {
								System.out.println("Recv S-Frame: REJ"); // duplicated??
								count = -1;
								sock.send(sendPacket);
							}
						} else {
							//sleep...
						}
					} catch (SocketTimeoutException e){
						System.out.println("Timeout(No ACK:Resend I-Frame) [" + (count+1) + "/3]");
					}
				}
				//shared.setPacket(sendPacket);
			}
		} catch (IOException e) {	e.printStackTrace();	}
	}

	public final byte[] intToByteArray(int value) { // only need 2 byte.
	    return new byte[] {
	            (byte)(value >>> 8),
	            (byte)(value >>> 0)
	     		};
	}
}
