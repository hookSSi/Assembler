package Assembler;

import java.util.Hashtable;
import java.util.StringTokenizer;

public class Test 
{
	public static void main(String[] args)
	{
		BitVector test = new BitVector(4);
		test.SetOp(0x74);
		test.SetAddress(4096);
		test.Set(7, true);
		test.Set(11, true);
		for(int i = 0; i < 32; i++)
			System.out.print(test.GetBit(i));
		System.out.println(test.BitToString());
	}
}
