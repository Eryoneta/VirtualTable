package main;
import java.util.Arrays;
public class Ponto3D{
	private double x=0;
		public double getX(){return x;}
		public void setX(double x){this.x=x;}
	private double y=0;
		public double getY(){return y;}
		public void setY(double y){this.y=y;}
	private double z=0;
		public double getZ(){return z;}
		public void setZ(double z){this.z=z;}
	public void setLocation(double x,double y,double z){setX(x);setY(y);setZ(z);}
	public void setLocation(Ponto3D ponto){setX(ponto.x);setY(ponto.y);setZ(ponto.z);}
	public Ponto3D(){}
	public Ponto3D(double x,double y,double z){setLocation(x,y,z);}
	public Ponto3D(Ponto3D ponto){setLocation(ponto.getX(),ponto.getY(),ponto.getZ());}
	public double[][]getPointMatrix(){
		return new double[][]{
				new double[]{1,0,0,getX()},	//	1	0	0	x
				new double[]{0,1,0,getY()},	//	0	1	0	y
				new double[]{0,0,1,getZ()},	//	0	0	1	z
				new double[]{0,0,0,1}		//	0	0	0	1
		};
	}
	public static Ponto3D getPonto3D(double[][]matrix){
		matrix=transform(matrix,new double[][]{
				new double[]{1},
				new double[]{1},
				new double[]{1},
				new double[]{1}
		});
		return new Ponto3D(matrix[0][0],matrix[1][0],matrix[2][0]);
	}
	public static double[][]getTranslationXMatrix(double distanceX){	//TODO: REMOVER?
		return new double[][]{
				new double[]{1,0,0,distanceX},	//	1		0		0 		x
				new double[]{0,1,0,0},			//	0		1		0		0
				new double[]{0,0,1,0},			//	0		0		1		0
				new double[]{0,0,0,1}			//	0		0		0		1
		};
	}
	public static double[][]getTranslationZMatrix(double distanceZ){
		return new double[][]{
				new double[]{1,0,0,0},			//	1		0		0 		0
				new double[]{0,1,0,0},			//	0		1		0		0
				new double[]{0,0,1,distanceZ},	//	0		0		1		z
				new double[]{0,0,0,1}			//	0		0		0		1
		};
	}
	public static double[][]getRotationZ(double angleZ){
		return new double[][]{
				new double[]{Math.cos(angleZ),-Math.sin(angleZ),0,0},	//	COS		-SIN	0 		0
				new double[]{Math.sin(angleZ),Math.cos(angleZ),0,0},	//	SIN		COS		0		0
				new double[]{0,0,1,0},									//	0		0		1		0
				new double[]{0,0,0,1}									//	0		0		0		1
		};
	}
	/*
	public static double[][]getRotationY(double angleY){
		return new double[][]{
				new double[]{Math.cos(angleY),0,Math.sin(angleY),0},	//	COS		0		SIN		0
				new double[]{0,1,0,0},									//	0		1		0		0
				new double[]{-Math.sin(angleY),0,Math.cos(angleY),0},	//	-SIN	0		COS		0
				new double[]{0,0,0,1}									//	0		0		0		1
		};
	}
	 */
	public static double[][]getRotationX(double angleX){
		return new double[][]{
				new double[]{1,0,0,0},									//	1		0		0		0
				new double[]{0,Math.cos(angleX),-Math.sin(angleX),0},	//	0		COS		-SIN	0
				new double[]{0,Math.sin(angleX),Math.cos(angleX),0},	//	0		SIN		COS		0
				new double[]{0,0,0,1}									//	0		0		0		1
		};
	}
	public static double[][]transform(double[][]...matrixes){
		if(matrixes.length==1)return matrixes[0];
		final double[][]m1=matrixes[0];
		final double[][]m2=matrixes[1];
		final int colsM1=m1[0].length;
		final int rowsM1=m1.length;
		final int colsM2=m2[0].length;
		final int rowsM2=m2.length;
		if(colsM1!=rowsM2&&rowsM1!=colsM2)return null;
		final double[][]matrix=new double[rowsM1][colsM2];
		for(int r=0;r<rowsM1;r++)for(int c=0;c<colsM2;c++){
			matrix[r][c]=0;
			for(int i=0;i<rowsM2;i++)matrix[r][c]=(double)(matrix[r][c]+(double)(m1[r][i]*m2[i][c]));
		}
		if(matrixes.length==2)return matrix;
		return transform(matrix,transform(Arrays.copyOfRange(matrixes,2,matrixes.length)));		//TODO: POTENCIALMENTE PESADO!
	}
}