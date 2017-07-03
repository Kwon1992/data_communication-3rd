const int ledPin=13;
int blinkRate=0;
int len=0,i;
bool isFirst = true;

void setup() {
  Serial.begin(9600);
  pinMode(ledPin,OUTPUT);
}

void loop() {
  if(len = Serial.available()){
    delay(100); // time to delivery. 
    if(len == 1 && isFirst == true) {isFirst = false;} // value of len is 1 in first. so, pass the first time of "len == 1"
    else{
      if(blinkRate!=0) blinkRate=0;
      for(int i=0;i<len;i++){
        char ch = Serial.read();
        if(isDigit(ch)){
          ch = ch - '0';
          blinkRate=blinkRate*10+ch;
        }
      }
    isFirst = true;
    }
  }
  if(blinkRate>0){
    blink();
  }
}

void blink(){
  digitalWrite(ledPin,HIGH);
  delay(blinkRate);
  digitalWrite(ledPin,LOW);
  delay(blinkRate);
}

