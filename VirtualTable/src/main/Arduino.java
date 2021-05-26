package main;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;

public class Arduino{
	private SerialPort portaSerial=null;
		public void setPortaSerial(SerialPort portaSerial){this.portaSerial=portaSerial;}
		public SerialPort getPortaSerial(){return portaSerial;}
	private int taxa=9600;
		public void setTaxa(int taxa){this.taxa=taxa;}
		public int getTaxa(){return taxa;}
	private ArduinoAction action=null;
		public ArduinoAction getAction(){return action;}
		public void setAction(ArduinoAction action){this.action=action;}
	public final static long TIME_SEARCHING=3000;		//3 SEGUNDOS
	public final static long CONNECTION_TIMEOUT=5000;	//5 SEGUNDOS
	public Arduino(){}
	public Arduino(String portaSerial,int taxa){
		setPortaSerial(SerialPort.getCommPort(portaSerial));
		setTaxa(taxa);
	}
	public boolean connectPortaSerial()throws IOException,InterruptedException{
		setPortaSerial(null);
		for(SerialPort portaSerial:SerialPort.getCommPorts()){
			portaSerial.setComPortParameters(taxa,8,SerialPort.ONE_STOP_BIT,SerialPort.NO_PARITY);
			portaSerial.openPort();
			if(!portaSerial.isOpen())continue;
			portaSerial.addDataListener(new SerialPortDataListener(){
				private long tempoStarted=System.currentTimeMillis();
				private long tempoParado=System.currentTimeMillis();
				public void serialEvent(SerialPortEvent p){
					if(portaSerial!=getPortaSerial()&&(getPortaSerial()!=null||(System.currentTimeMillis()-tempoStarted)>TIME_SEARCHING)){
						portaSerial.removeDataListener();
						portaSerial.closePort();
					}
					if(System.currentTimeMillis()-tempoParado>CONNECTION_TIMEOUT){
						VirtualTable.mensagem("Conexão com Arduino perdida!",VirtualTable.AVISO);
						System.exit(0);
					}
					if(p.getEventType()!=SerialPort.LISTENING_EVENT_DATA_AVAILABLE)return;
					tempoParado=System.currentTimeMillis();
					final String mensagem=getDados(portaSerial);
					if(getAction()!=null)getAction().run(mensagem);
					if(getPortaSerial()==null){
//						final Matcher matcher=Pattern.compile(
//								"VT:([0-1]+),([0-9.]+),([0-9.]+),([0-9.]+),([0-9.]+),([0-9.]+),([0-9.]+),([0-1]+),([0-1]+),([0-1]+)").matcher(mensagem);
						final Matcher matcher=Pattern.compile("VT:([0-1]+),([0-9.]+),([0-9]+),([0-9]+),([0-9]+),([0-9]+),([0-9]+),([0-1]+),([0-1]+),([0-1]+)").matcher(mensagem);
						if(matcher.find())setPortaSerial(portaSerial);
					}
				}
				public int getListeningEvents(){
					return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
				}
			});
		}
		long tempoLimite=TIME_SEARCHING;
		while(getPortaSerial()==null&&tempoLimite>0){
			tempoLimite-=100;
			Thread.sleep(100);
		}
		return (getPortaSerial()!=null);
	}
	public void close(){
		if(portaSerial==null)return;
		portaSerial.removeDataListener();
		portaSerial.closePort();
	}
	private String getDados(SerialPort portaSerial){
		final byte[]dados=new byte[portaSerial.bytesAvailable()];
		portaSerial.readBytes(dados,dados.length);
		return new String(dados);
	}
}