package Assembler;

import java.util.Hashtable;
import java.util.StringTokenizer;

public class Test 
{
	public static void main(String[] args)
	{

		BitVector test = new BitVector(3);
		test.SetOp(0x3F);
		test.SetAddress(-6);
		test.Set(10, true); // e
		
		
		for(int i = 0; i < test.m_size * 8; i++)
			System.out.print(test.GetBit(i));
		System.out.println("\nDEBUG :" + test.BitToString());
		System.out.println(-0xFEC);
	}
}
