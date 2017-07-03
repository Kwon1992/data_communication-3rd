package Server;

import LLC.*;
import java.io.*;
import java.net.*;
import java.util.*;

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
		RecvThread recv = new RecvThread(sock);
		recv.start();
	}
}


class RecvThread extends Thread {
	DatagramSocket sock;

	public RecvThread(DatagramSocket ds){
		sock = ds;
	}
	public void run() {
		byte[] msg;
		InetAddress ip, clientIp;
		int clientPort;
		byte[] Mymac = null;
		byte[] Destmac = null;
		byte recvSequence = (byte)0;
		int length;

		try {
			ip = InetAddress.getLocalHost();
			NetworkInterface network = NetworkInterface.getByInetAddress(ip);
			Mymac = network.getHardwareAddress();
		} catch (Exception e) { e.printStackTrace(); }

		while(true) {
				try {
					//First :: receive Frame from Client to connection.//
					byte[] recvBuf = new byte[65535];
					DatagramPacket recvPacket = new DatagramPacket(recvBuf, recvBuf.length);
					sock.receive(recvPacket);
					msg = Arrays.copyOfRange(recvPacket.getData(),0,recvPacket.getLength());
					clientIp = recvPacket.getAddress();
					clientPort = recvPacket.getPort();

					LLCBitSet recvBitSet = LLCBitSet.fromBytes(msg);
					Frame unknownFrame = Frame.fromByteArray(recvBitSet);
					System.out.println("**************************************************");
					//If received Frame is UFrame, then check whether it is SABME or not.//
					if(unknownFrame instanceof UFrame) {
						UFrame UFrame = (UFrame)unknownFrame;
						System.out.print("Get U-FRAME");

						if(UFrame.getCommandType() == (byte)UFrame.SABME) {
							System.out.println("(SABME)");
							Destmac = UFrame.getSourceAddress();


							//formatting UA U-Frame//
							UFrame UA = new UFrame((byte)UFrame.UA);
							UA.setSourceAddress(Mymac); // set MAC Address of server.
							UA.setDestAddress(Destmac); // set MAC Address of client.
							length = 22; // set lengtPDU (FIXED)
							byte[] lengthPDU = intToByteArray(length);
							UA.setLengthPDU(lengthPDU);

							byte[] serializedFrame = UA.toByteArray(); // make Frame -> byte Array for make DatagramPacket
							DatagramPacket packetUA = new DatagramPacket(serializedFrame, serializedFrame.length, clientIp, clientPort); // Send the packet to client. (UA)
							sock.send(packetUA);
							System.out.println("Send U-FRAME(UA)");
						}

					} else if (unknownFrame instanceof IFrame) { // Not Make IFrame Yet...
						System.out.println("Get I-FRAME");
						IFrame iFrame = (IFrame)unknownFrame;
						if(iFrame.getSendSequence() == recvSequence) {
							System.out.println("Expected Packet Recv.. SequenceNum(" + recvSequence + ")");

							//print get msg..
							LLCBitSet recvMsgBitSet = iFrame.getInformation();
							byte[] msgByteArr = recvMsgBitSet.toByteArray();
							String s = new String(msgByteArr);
							System.out.println("RECV>> " + s);

							SFrame RR = new SFrame(LLC.SFrame.RR);
							RR.setDestAddress(Destmac);
							RR.setSourceAddress(Mymac);
							length = 22;
							byte[] lengthPDU = intToByteArray(length);
							recvSequence++;
							RR.setControl(recvSequence,(byte)0,LLC.SFrame.RR);
							byte[] serializedFrame = RR.toByteArray();
							DatagramPacket packetRR = new DatagramPacket(serializedFrame, serializedFrame.length, clientIp, clientPort);
							sock.send(packetRR);
							System.out.println("Send S-FRAME(RR)");
						} else {
							System.out.println("Unexpected Packet Recv...[Excpected:" + recvSequence + "] [Received:"+iFrame.getSendSequence()+"]");
							SFrame REJ = new SFrame(LLC.SFrame.REJ);
							REJ.setDestAddress(Destmac);
							REJ.setSourceAddress(Mymac);
							length = 22;
							byte[] lengthPDU = intToByteArray(length);
							REJ.setLengthPDU(lengthPDU);
							REJ.setControl(recvSequence,(byte)0,LLC.SFrame.REJ);
							byte[] serializedFrame = REJ.toByteArray();
							DatagramPacket packetREJ = new DatagramPacket(serializedFrame, serializedFrame.length, clientIp, clientPort);
							sock.send(packetREJ);
							System.out.println("Send S-FRAME(REJ)");

						}
					} else {
						//Sleep...
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}
	public final byte[] intToByteArray(int value) {
		return new byte[] { (byte)(value >>> 8), (byte)(value >>> 0 )};
	}
}
