#include <stdio.h>
#include <unistd.h>
#include <sys/ioctl.h>
#include <asm/io.h>
#include <math.h> 
#include <assert.h>
#include "mdriver.h"

// how to compile:
// gcc mdriver.c -o libmdriver.so -shared -I /usr/lib/jvm/java-6-sun-1.6.0.00/include -I /usr/lib/jvm/java-6-sun-1.6.0.00/include/linux
//

void Move(int x,int y,int z);
void Movedmm(int x,int y, int z);

// stuff needed for Java to be able to interface
JNIEXPORT void JNICALL Java_mdriver_Move(JNIEnv * e, jclass o, jint x, jint y, jint z)
{
  Move(x,y,z);
}
JNIEXPORT void JNICALL Java_mdriver_Movedmm(JNIEnv * e, jclass o, jint x, jint y, jint z)
{
  Movedmm(x,y,z);
}  
//------------------------------------------------------------
int main( int argc, char *argv[] )
{
  int i;
  printf("Motor Driver version 1\n");
  printf("by Bohdan P. Maksymiuk\n");
  
  if( ioperm(888,1,1) )
  {
    printf("Couldn't get port 888\n");
    return 1;
  }
  //val( = inb(888);
  //printf("old = %s\n",binprint(val,buf));
  //Move(5,5,0);
  
  //Move(1,2,0);
  //Move(2,1,0);
  //Move(10,20,0);
  //for( i=0; i<10; i++ )  
  //{
 
     //Move(5,7,0);
     //Move(-10,20,0);
     //Move(5,-7,0);
     //Move(10,-20,0);
  //}		
  return 0;
}


//------------------------------------------------------------
//Move x,y,z in terms of cm
void Move(int x,int y,int z)
{
	int i,largest;
	char d = 0;
	char m = 0;
	float xstep,ystep,zstep;
	float x1,y1,z1;
	int lx,ly,lz=0;
	//printf("c: x=%d, y=%d, z=%d \n",(int)x,(int)y,(int)z);
	x=x*160;
	y=y*160;
	z=z*160;
	
	if (x < 0) {d = d | 2; x = abs(x);}
	if (y < 0) {d = d | 8; y = abs(y);}
	if (z < 0) {d = d | 32; z = abs(z); }
	
	largest=(float)x;
	if ((x>y)&&(x>z)) {largest = (float)x;}
	if ((y>x)&&(y>z)) {largest = (float)y;}
	if ((z>y)&&(z>x)) {largest = (float)z;}
	 
	xstep = (float)x/largest;
	ystep = (float)y/largest;
	zstep = (float)z/largest;
	x1=x;
	y1=y;
	z1=z;
	//printf("largest=%g,stepx=%g, stepy=%g, stepz=%g \n",largest,xstep,ystep,zstep);
	lx=(int)x1;
	ly=(int)y1;
	lz=(int)z1;
	//printf("x=%d, y=%d, z=%d \n",(int)x1,(int)y1,(int)z1);
	while (x1+y1+z1 >0)
	{
		m = 0;
		if (x1>0) {  x1=x1-xstep; }
		if (y1>0) {  y1=y1-ystep; }
		if (z1>0) {  z1=z1-zstep; }
		if (lx != (int)x1) {m = m | 1; }
		if (ly != (int)y1) {m = m | 4; }
		if (lz != (int)z1) {m = m | 16; }
		if (ioperm(888, 3, 1)){printf("ioperm error");}

		
		outb(d,888);  delay();
		outb(m|d,888); delay();
		outb(0,888); delay();

		lx=(int)x1;
		ly=(int)y1;
		lz=(int)z1;
		//printf("x=%d, y=%d, z=%d \n",(int)x1,(int)y1,(int)z1);
	}
}
//------------------------------------------------------------
// move to x,y,z represented in mm
void Movedmm(int x,int y, int z)
{
	int i,largest;
	char d = 0;
	char m = 0;
	float xstep,ystep,zstep;
	float x1,y1,z1;
	int lx,ly,lz=0;
	//printf("c: x=%d, y=%d, z=%d \n",(int)x,(int)y,(int)z);
	x=x*16;
	y=y*16;
	z=z*16;
	
	if (x < 0) {d = d | 2; x = abs(x);}
	if (y < 0) {d = d | 8; y = abs(y);}
	if (z < 0) {d = d | 32; z = abs(z); }
	
	largest=(float)x;
	if ((x>y)&&(x>z)) {largest = (float)x;}
	if ((y>x)&&(y>z)) {largest = (float)y;}
	if ((z>y)&&(z>x)) {largest = (float)z;}
	 
	xstep = (float)x/largest;
	ystep = (float)y/largest;
	zstep = (float)z/largest;
	x1=x;
	y1=y;
	z1=z;
	//printf("largest=%g,stepx=%g, stepy=%g, stepz=%g \n",largest,xstep,ystep,zstep);
	lx=(int)x1;
	ly=(int)y1;
	lz=(int)z1;
	//printf("x=%d, y=%d, z=%d \n",(int)x1,(int)y1,(int)z1);
	while (x1+y1+z1 >0)
	{
		m = 0;
		if (x1>0) {  x1=x1-xstep; }
		if (y1>0) {  y1=y1-ystep; }
		if (z1>0) {  z1=z1-zstep; }
		if (lx != (int)x1) {m = m | 1; }
		if (ly != (int)y1) {m = m | 4; }
		if (lz != (int)z1) {m = m | 16; }
		if (ioperm(888, 3, 1)){printf("ioperm error");}

		
		outb(d,888);  delay();
		outb(m|d,888); delay();
		outb(0,888); delay();

		lx=(int)x1;
		ly=(int)y1;
		lz=(int)z1;
		//printf("x=%d, y=%d, z=%d \n",(int)x1,(int)y1,(int)z1);
	}
}

//------------------------------------------------------------
// when preented with a character x, creates a null terminated
// string of 1's and 0's that represent the binary version of x
char *binprint( unsigned char x, char *buf )
{
  	int i;
	for( i=0; i<8; i++ )
		buf[7-i]=(x&(1<<i))?'1':'0';
	buf[8]=0;
	return buf;
}

//------------------------------------------------------------
// waste time
int delay()
{
	int i;
	for(i=0; i<100000; i++ )  {  }
	return 0;
}
