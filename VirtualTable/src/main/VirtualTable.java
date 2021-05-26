package main;
import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JOptionPane;
public class VirtualTable{
	public static void main(String[]args){
		new VirtualTable();
	}
	private Robot bot;
		public Robot getBot(){return bot;}

	private double l1Size=240;		//5,5cm
	private double l2Size=605;		//13,7cm
	private double l3Size=618;		//14,0cm
	private double l4Size=742;		//17,0cm

	private Dimension telaSize=Toolkit.getDefaultToolkit().getScreenSize();
	public VirtualTable(){
		try{
			bot=new Robot();
		}catch(AWTException erro){
			mensagem("Não foi possível iniciar o controlador:/n"+erro,ERRO);
			exit();
		}
		connectArduino();
	}
	final Arduino arduino=new Arduino();
	private void connectArduino(){
		try{
			if(!arduino.connectPortaSerial()){
				mensagem("Arduino não encontrado",ERRO);
				exit();
			}
		}catch(IOException|InterruptedException erro){
			mensagem("Não foi possível receber dados:/n"+erro,ERRO);
			exit();
		}
		arduino.setAction(new ArduinoAction(){
			public void run(String mensagem){
				setMouse(mensagem);
			}
		});
	}
	final static int POT_RANGE=(int)Math.pow(2,12);		//12-BIT
	public static double getAngle(double potencia){
		final int potRange=POT_RANGE-1;
		final double angleRange=(Math.toRadians(270));	//270º: ALCANCE DO POT
		return ((potencia*angleRange)/potRange);		//[0]-[2^12] -> [RAD(0)]-[RAD(270)] 
	}
	public static double getPotValue(double radian){
		final int potRange=POT_RANGE-1;
		final double angleRange=(Math.toRadians(270));	//270º: ALCANCE DO POT
		return ((radian*potRange)/angleRange);			//[RAD(0)]-[RAD(270)]  -> [0]-[2^12] 
	}
	private int xFix=86+80;		//2,0cm + 1,9cm
	private int yFix=-114+86;	//2,0cm
	
	private int zPress=35;
	
	private int potZ1Fix=45*4;
	private int potX1Fix=0*4;
	private int potX2Fix=-16*4;
	private int potZ2Fix=-6*4;
	private int potX3Fix=4*4;
	private int potZ3Fix=1*4;
	
	private int potZ1=0;
	private int potX1=0;
	private int potX2=0;
	private int potZ2=0;
	private int potX3=0;
	private int potZ3=0;

	private boolean leftDown=false;
	private boolean middleDown=false;
	private boolean rightDown=false;
	
	private void setMouse(String mensagem){
//		final Matcher matcher=Pattern.compile("VT:([0-1]+),([0-9.]+),([0-9.]+),([0-9.]+),([0-9.]+),([0-9.]+),([0-9.]+),([0-1]+),([0-1]+),([0-1]+)").matcher(mensagem);
		final Matcher matcher=Pattern.compile("VT:([0-1]+),([0-9]+),([0-9]+),([0-9]+),([0-9]+),([0-9]+),([0-9]+),([0-1]+),([0-1]+),([0-1]+)").matcher(mensagem);
		if(!matcher.find())return;	//MENSAGEM CORROMPIDA
		final boolean interruptor=(Integer.parseInt(matcher.group(1))!=0);
		if(!interruptor)return;		//AFETAR MOUSE NÃO PERMITIDO
		final int newPotZ1=Integer.parseInt(matcher.group(2))+potZ1Fix;
		final int newPotX1=Integer.parseInt(matcher.group(3))+potX1Fix;
		final int newPotX2=Integer.parseInt(matcher.group(4))+potX2Fix;
		final int newPotZ2=Integer.parseInt(matcher.group(5))+potZ2Fix;
		final int newPotX3=Integer.parseInt(matcher.group(6))+potX3Fix;
		final int newPotZ3=Integer.parseInt(matcher.group(7))+potZ3Fix;
		if(newPotZ1==potZ1&&newPotX1==potX1&&newPotX2==potX2&&newPotZ2==potZ2&&newPotX3==potX3&&newPotZ3==potZ3)return;	//PONTO NÃO MUDOU
		potZ1=newPotZ1;
		potX1=newPotX1;
		potX2=newPotX2;
		potZ2=newPotZ2;
		potX3=newPotX3;
		potZ3=newPotZ3;
		final double angleZ1=getAngle(newPotZ1);
		final double angleX1=getAngle(newPotX1);
		final double angleX2=getAngle(newPotX2);
		final double angleZ2=getAngle(newPotZ2);
		final double angleX3=getAngle(newPotX3);
		final Ponto3D ponto=getMousePosition(angleZ1,angleX1,angleX2,angleZ2,angleX3);
		
		getBot().mouseMove((int)Math.round(ponto.getX()),(int)Math.round(ponto.getY()));
		
		final boolean newLeftDown=(Integer.parseInt(matcher.group(8))!=0||ponto.getZ()<zPress);
		final boolean newMiddleDown=(Integer.parseInt(matcher.group(9))!=0);
		final boolean newRightDown=(Integer.parseInt(matcher.group(10))!=0);
		
		if(leftDown!=newLeftDown){
			leftDown=newLeftDown;
			if(newLeftDown){
				getBot().mousePress(MouseEvent.BUTTON1_DOWN_MASK);
			}else getBot().mouseRelease(MouseEvent.BUTTON1_DOWN_MASK);
		}
		if(middleDown!=newMiddleDown){
			middleDown=newMiddleDown;
			if(middleDown){
				getBot().mousePress(MouseEvent.BUTTON2_DOWN_MASK);
			}else getBot().mouseRelease(MouseEvent.BUTTON2_DOWN_MASK);
		}
		if(rightDown!=newRightDown){
			rightDown=newRightDown;
			if(rightDown){
				getBot().mousePress(MouseEvent.BUTTON3_DOWN_MASK);
			}else getBot().mouseRelease(MouseEvent.BUTTON3_DOWN_MASK);
		}
	}
	private Ponto3D getMousePosition(double angleZ1,double angleX1,double angleX2,double angleZ2,double angleX3){
		return Ponto3D.getPonto3D(Ponto3D.transform(
				new Ponto3D(telaSize.width+xFix,yFix,0).getPointMatrix(),	//(tela.width,0,0)
				Ponto3D.getRotationZ(angleZ1-Math.toRadians(90)),		//RZ(z1)
				Ponto3D.getTranslationZMatrix(l1Size),					//TZ(l1)
				Ponto3D.getRotationX(angleX1-Math.toRadians(135)),		//RX(x1)
				Ponto3D.getTranslationZMatrix(l2Size),					//TZ(l2)
				Ponto3D.getRotationX(Math.toRadians(45)-angleX2),		//RX(x2)
				Ponto3D.getRotationZ(angleZ2-Math.toRadians(135)),		//RZ(z2)
				Ponto3D.getTranslationZMatrix(l3Size),					//TZ(l3)
				Ponto3D.getRotationX(angleX3-Math.toRadians(225)),		//RX(x3)
				Ponto3D.getTranslationZMatrix(l4Size)					//TZ(l4)
		));															//(x,y,z) DO CURSOR
	}
	public final static int ERRO=0,AVISO=1;
	public static void mensagem(String mensagem,int tipo){
		switch(tipo){
			case AVISO:	JOptionPane.showMessageDialog(null,mensagem,"Aviso!",JOptionPane.WARNING_MESSAGE);break;
			case ERRO:	JOptionPane.showMessageDialog(null,mensagem,"Erro...!",JOptionPane.ERROR_MESSAGE);break;
		}
	}
	private void exit(){
		arduino.close();
		System.exit(0);
	}
}