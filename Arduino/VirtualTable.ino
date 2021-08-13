void setup(){
	Serial.begin(9600);
	pinMode(2,INPUT);
	pinMode(3,INPUT);
	pinMode(4,INPUT);
	pinMode(5,INPUT);
}
void loop(){
	String virtualTable="VT:";
	uint8_t s=digitalRead(2);			//SWITCH
	double z1=getAnalogValor(A0);		//Z1
	double x1=getAnalogValor(A1);		//X1
	double x2=getAnalogValor(A2);		//X2
	double z2=getAnalogValor(A3);		//Z2
	double x3=getAnalogValor(A4);		//X3
	double z3=getAnalogValor(A5);		//Z3
	uint8_t l=digitalRead(3);			//LEFT
	uint8_t m=digitalRead(4);			//MIDDLE
	uint8_t r=digitalRead(5);			//RIGHT
	String separador=",";
	String mensagem=(virtualTable+s+separador+z1+separador+x1+separador+x2+separador+z2+separador+x3+separador+z3+separador+l+separador+m+separador+r);		//VT:S,Z1,X1,X2,Z2,X3,Z3,L,M,R
	Serial.println(mensagem);
}
uint8_t nivel=2;
uint8_t precision=pow(4,nivel);
int getAnalogValor(int A){
	unsigned int soma=0;
	for(uint8_t i=0;i<precision;i++)soma+=analogRead(A);
	return (soma>>nivel);
}