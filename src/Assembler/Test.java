package Assembler;

import java.util.Hashtable;
import java.util.StringTokenizer;

class power
{
	public int a = 1;
	int b = 2;
	int c = 3;
	power(int a){this.a =a;}
	
	public int GetA(){return a;}
}



public class Test 
{
	public static void main(String[] args)
	{
		/*
		Hashtable OpTable = new Hashtable();
		power p = new power(2);
		power p2 = new power(3);
		OpTable.put("Test", p);
		
		p2 = (power)OpTable.get("Test");
		
		if(OpTable.containsKey("Tes2t"))
			System.out.println(p2.a);*/
		
		System.out.println(Integer.parseInt("1000",16));
	}
}
