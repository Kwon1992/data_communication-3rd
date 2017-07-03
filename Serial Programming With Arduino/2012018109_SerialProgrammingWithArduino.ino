const int ledPin=13;
int blinkRate=0;
int len=0,i;
bool isFirst = true;
bool IsWrite = false;

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
      IsWrite = false;
      isFirst = true;    
      }  
   }  
   if(blinkRate>0){    
    blink();  
   }
}

void blink(){  
  if(!IsWrite){
    Serial.print("Your input is >> ");
    Serial.println(blinkRate);
    IsWrite = true;
  }
  digitalWrite(ledPin,HIGH);  
  delay(blinkRate);  
  digitalWrite(ledPin,LOW); 
  delay(blinkRate);
}

