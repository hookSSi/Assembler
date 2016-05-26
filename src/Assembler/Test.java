package Assembler;

import java.util.Hashtable;
import java.util.StringTokenizer;

public class Test 
{
	public static void main(String[] args)
	{

		BitVector test = new BitVector(1);
		test.SetOp(0xF1);
		for(int i = 0; i < 8; i++)
			System.out.print(test.GetBit(i));
		System.out.println(test.BitToString());
	
		
		System.out.println(test.BitToString());
	}
}
