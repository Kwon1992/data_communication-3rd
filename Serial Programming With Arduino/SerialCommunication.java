import java.io.*;
import java.util.*;
import gnu.io.*;

public class SerialCommunication {
	static CommPortIdentifier portId;
	static Enumeration portList;
	static SerialPort serialPort;

	public static void main(String[] args) {
		portList = CommPortIdentifier.getPortIdentifiers();
		while (portList.hasMoreElements()) {
			portId = (CommPortIdentifier) portList.nextElement();
			if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
				if (portId.getName().equals("COM3")) {
						try {
							serialPort = (SerialPort) portId.open("WriteReadApp",2000);
						} catch(PortInUseException e) { }
						try {
							serialPort.setSerialPortParams(9600, SerialPort.DATABITS_8, 
																 SerialPort.STOPBITS_1, 
																 SerialPort.PARITY_NONE);
						} catch (UnsupportedCommOperationException e){ }
						SimpleRead reader = new SimpleRead();
						SimpleWrite writer = new SimpleWrite();
				}
			}
		}
	}

	public static class SimpleRead implements Runnable, SerialPortEventListener {

		InputStream inputStream;
		Thread readThread;

		public SimpleRead() {
			try {
				inputStream = serialPort.getInputStream();
			} catch (IOException e) { }
			try {
				serialPort.addEventListener(this);
			} catch (TooManyListenersException e) {}
			serialPort.notifyOnDataAvailable(true);
			readThread = new Thread(this);
			readThread.start();
		}

		public void run() {
		}

		public void serialEvent(SerialPortEvent event) {
			switch (event.getEventType()) {
			case SerialPortEvent.BI:
			case SerialPortEvent.OE:
			case SerialPortEvent.FE:
			case SerialPortEvent.PE:
			case SerialPortEvent.CD:
			case SerialPortEvent.CTS:
			case SerialPortEvent.DSR:
			case SerialPortEvent.RI:
			case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
				break;

			case SerialPortEvent.DATA_AVAILABLE:
				int numBytes = 0;
				byte[] readBuffer = new byte[100];
				try {
					while (inputStream.available() > 0) {
						numBytes = inputStream.read(readBuffer);
					}
					System.out.print(new String(readBuffer, 0, numBytes));
				} catch (IOException e) { }
				break;
			}
		}
	}
	
	public static class SimpleWrite implements Runnable {
		OutputStream outputStream;
		Thread writeThread;
		
		public SimpleWrite() {
			try {
				outputStream = serialPort.getOutputStream();
			} catch (IOException e) { }
			writeThread = new Thread(this);
			writeThread.start();
			
		}
		
		public void run() {
			try {
				int num = 0;
				while ((num = System.in.read()) > -1) {
					this.outputStream.write(num);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
