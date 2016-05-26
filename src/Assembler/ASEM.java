package Assembler;

import java.awt.event.*;
import java.awt.*;
import java.io.*;

import javax.swing.*;
import javax.swing.GroupLayout.Alignment;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.BadLocationException;
import javax.xml.stream.events.Characters;

import javafx.stage.FileChooser;
import java.util.*;

/*	Operation Node	*/
class OPTAB
{
	int op; // Operation Code
	int oplength; // Operation length
	int ext; // Extended Operation

	OPTAB(int op, int oplength, int ext)
	{
		this.op = op;
		this.oplength = oplength;
		this.ext = ext;
	}

	public int GetOp(){return op;}
	public int GetLength(){return oplength;}
	public int GetExt(){return oplength+ext;}
}

/*	Static things in assembler	*/
class StaticThings
{
	/*	Operation Table	*/
	public static Hashtable OpTable = new Hashtable();
	/*	Directive Table	*/
	public static Hashtable DirectTable = new Hashtable();
	/*	Symbol Table	*/
	public static Hashtable Table = new Hashtable();
	/*	Register Table	*/
	public static Hashtable RegistTable = new Hashtable();


	public static int LOCCTR; 	// Address Counter
	public static String ProgramName = null;
	public static int StartAddress; // Start Address
	public static int EndAddress;
	public static int BaseAddress; // Base Address
	public static int Errorflag;

	public static void Clear() // Clear to assemble again
	{
		Table = new Hashtable();
		LOCCTR = 0;
		ProgramName = new String();
		StartAddress = 0;
		EndAddress = 0;
		BaseAddress = 0;
		Errorflag = 0;
	}
}

/*	To store T record	*/
class Trecord
{
	public static ArrayList<String> TRecordTable = new ArrayList<String>();
	public static String temp = new String(); // Store untill max T record size
	public static int startAddress = 0;
	public static int endAddress = 0;
	public static int count = 0;

	public static void Clear()
	{
		TRecordTable = new ArrayList<String>();
		startAddress = StaticThings.StartAddress;
		endAddress = 0;
	}
	public static void Append(String T) // need to fix!!!!!!
	{
		int length = T.length();
		if(count + length < 60)
		{
			TRecordTable.add(T);
			count += length;
		}
		else
		{
			count = 0;
			TRecordTable.add("\nT"); //+GetDecToHex(StaticThings.LOCCTR)
			TRecordTable.add(T);
			count += length;
		}

	}
	static boolean IsStringInt(String s)	// Can String transform to int
	{
		try
		{
			Integer.parseInt(s);
			return true;
		}
		catch(NumberFormatException e)
		{
			return false;
		}
	}
	static String GetPartOfString(String str)	// Read line
	{
		String part = new String();
		char[] temp = new char[35];

		for(int i = 0; i < 35; i++)
		{
			temp[i] = 0;
		}

		for(int i = 0; i < 35 && i < str.length(); i++)
		{
			temp[i] = str.charAt(i);
		}
		part = new String(temp);

		return part;
	}
	static String GetDecToHex(int dec) // Dec -> Hex method
	{
		String hex;

		hex = Integer.toHexString(dec);
		if(hex.length() < 6)
		{
			int temp = 6 - hex.length();
			for(int i = 0; i < temp; i++)
			{
				hex = "0" + hex;
			}
		}

		return hex.toUpperCase();
	}
}

/*	Bit class - Byte	*/
class BitVector
{
	byte[] m_array = {0};
	int m_size;

	BitVector(int p_size)
	{
		m_size = 0;
		Resize(p_size);
	}
	void Resize(int p_size)
	{
		if(p_size % 8 == 0)
		{
			m_size = p_size / 8;
		}
		else
			m_size = p_size;
		byte[] newvector = new byte[m_size];
		m_array = new byte[m_size];
		if(newvector == null)
			return;
		int min;
		if(p_size < m_size)
			min = p_size;
		else
			min = m_size;
		System.arraycopy(m_array, 0, newvector, 0, min);
		m_size = p_size;
		System.arraycopy(newvector, 0, m_array, 0, newvector.length);
	}
	int GetBit (int p_index)
	{
		int cell = p_index / 8;
		int bit = 7 - (p_index % 8);
		return (m_array[cell] & (1 << bit)) >> bit;
	}
	int GetByte(int p_index)
	{
		return m_array[p_index];
	}
	void Set(int p_index, boolean p_value)
	{
		int cell = p_index / 8;
		int bit = 7 - (p_index % 8);
		if(p_value == true)
			m_array[cell] = (byte) (m_array[cell] | (1 << bit));
		else
			m_array[cell] = (byte) (m_array[cell] & (~(1 << bit)));
	}
	void SetOp(int p_value)
	{
		Integer temp = new Integer(p_value);
		if(m_size >= 3)
		{
			Set(6,false); // n
			Set(7,false); // i
			Set(8, false); // x
			Set(9, false); // b
			Set(10, false); // p
			m_array[0] =  temp.byteValue();
		}
		else
			m_array[0] =  temp.byteValue();
	}
	void SetAddress(int p_address)
	{
		if(m_size == 4)
		{
			Integer temp1 = new Integer(p_address % 256);
			Integer temp2 = new Integer((p_address / 256) % 256);
			Integer temp3 = new Integer((p_address / 256) / 256);
			m_array[1] = temp3.byteValue();
			m_array[2] = temp2.byteValue();
			m_array[3] = temp1.byteValue();
		}
		else if(m_size == 3)
		{
			Integer temp1 = new Integer(p_address % 256);
			Integer temp2 = new Integer(p_address / 256);
			m_array[1] = temp1.byteValue();
			m_array[2] = temp2.byteValue();
		}
		
	}
	void SetRegister(int p_r1, int p_r2)
	{
		Integer temp = new Integer(p_r1 * 16 + p_r2);
		m_array[1] = temp.byteValue();
	}
	String BitToString()
	{
		String str = "";
		System.out.println("\n" +m_array[0]);
		if(m_size >= 3)
		{
			str = GetDecToHex(m_array[0]);			
			for(int i = 8; i < m_size * 8; i = i + 4)
				str = str + GetDecToHex((GetBit(i) * 8 + GetBit(i+1) * 4 + GetBit(i+2) * 2 + GetBit(i+3)));
		}
		else
		{
			str = GetDecToHex(m_array[0]);
			for(int i = 8; i < m_size * 8; i = i + 4)
				str = str + GetDecToHex((GetBit(i) * 8 + GetBit(i+1) * 4 + GetBit(i+2) * 2 + GetBit(i+3)));
		}
		
		return str;
	}
	void ClearAll()
	{
		for(int i = 0; i < m_size; i++)
			m_array[i] = 0;
	}
	String GetDecToHex(int dec)
	{
		String hex;
		hex = Integer.toHexString(dec);
		if(hex.length() > 2)
		{
			hex = hex.substring(hex.length()-2, hex.length());
		}
		return hex.toUpperCase();
	}
}

/*	UI	control	*/
class MainWindow implements ActionListener
{
	JFrame m_MainFrame;
	JPanel m_MainPanel;

	JMenuBar m_MenuBar;
	JMenu m_File;
	JMenuItem m_Open,m_Save;

	JTextArea m_FileTextArea,m_TransTextArea;
	JLabel m_FileTextLabel, m_TransTextLabel;
	JScrollPane m_FileTextScroll,m_TransTextScroll;

	JButton m_TransButton;
	FileDialogWindow m_FileDialog;

	Dimension m_MonitorSize = Toolkit.getDefaultToolkit().getScreenSize();

	MainWindow()
	{
		m_FileDialog = new FileDialogWindow();

		m_MainFrame = new JFrame("Assembler");
		m_MainPanel = new JPanel();

		m_Open = new JMenuItem("Open");
		m_Save = new JMenuItem("Save");

		m_Open.addActionListener(this);
		m_Save.addActionListener(this);

		m_MenuBar = new JMenuBar();
		m_MenuBar.setBounds(5,5,400,40);

		m_File = new JMenu("File");

		/*	Menu Componets	*/
		m_File.add(m_Open); m_File.add(m_Save);

		/*	MenuBar	*/
		m_MenuBar.add(m_File);

		/*	File Input Area	*/
		m_FileTextArea = new JTextArea();
		m_FileTextArea.setBounds(0,0,300,300);
		m_FileTextArea.setEditable(false);
		m_FileTextScroll = new JScrollPane(m_FileTextArea,
    ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
    ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		m_FileTextScroll.setBounds(5, 50, 410, 400);

		/*	Trans Output Area	*/
		m_TransTextArea = new JTextArea();
		m_TransTextArea.setBounds(0,0,300,300);
		m_TransTextArea.setEditable(false);
		m_TransTextScroll = new JScrollPane(m_TransTextArea,
    ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
    ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		m_TransTextScroll.setBounds(5, 600, 410, 400);

		m_TransButton = new JButton("Assemble");
		m_TransButton.setBounds(150,500,100,50);
		m_TransButton.setSize(100,50);
		m_TransButton.addActionListener(this);

		/*	Main Panel	*/
		m_MainPanel.setBounds(0,0,430,1050);
		m_MainPanel.setLayout(null);
		m_MainPanel.add(m_FileTextScroll);
		m_MainPanel.add(m_TransButton);
		m_MainPanel.add(m_TransTextScroll);
		m_MainPanel.add(m_MenuBar);

		/*	Main Frame	*/
		m_MainFrame.getContentPane().add(m_MainPanel);

		m_MainFrame.setLayout(null);
		m_MainFrame.setBounds(m_MonitorSize.width/2 - 200,0,430,1050);
		m_MainFrame.setPreferredSize(new Dimension(430,1050));
		m_MainFrame.pack();
		m_MainFrame.setVisible(true);
		m_MainFrame.setResizable(false);
		m_MainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	public void actionPerformed(ActionEvent e) //	input manager
	{
		if(e.getSource() == m_TransButton)	//	Assemble Button
		{
			StaticThings.Clear();
			Pass1();
			Pass2();
			System.out.println("Assemble");
		}
		if(e.getSource() == m_Open)	//	Open Source File
		{
			try
			{
				m_FileDialog.Load(m_FileTextArea);
			}
			catch (IOException e1)
			{
				System.out.println("Cannot Load File");
			}
		}
		if(e.getSource() == m_Save)
		{
			m_FileDialog.Save();
		}
	}
	void Pass1()
	{
		/*	Clear Before reading	*/
		StaticThings.LOCCTR = 0;
		StaticThings.StartAddress = 0;
		StaticThings.Errorflag = 0;

		/*	Variable to read line	`*/
		int lineCount = m_FileTextArea.getLineCount();
		String line = new String();
		StringTokenizer Token;

		/*	Varialbe for Pass1	*/
		m_TransTextArea.setText("");
		String LABEL = new String();
		String OPCODE= new String();
		String OPERAND = new String();

		try
		{
			for(int i = 0; i < lineCount; i++)
			{
				/*	Initialize to read line	*/
				int start = m_FileTextArea.getLineStartOffset(i);
				int end = m_FileTextArea.getLineEndOffset(i);
				line = m_FileTextArea.getText(start,end-start);

				Token = new StringTokenizer(GetPartOfString(line)," ");
				LABEL = null; OPCODE = null; OPERAND = null;



				if(StaticThings.Table.get("START") == null)
				{
					if(Token.countTokens() == 3)
					{
						LABEL = Token.nextToken();
					}
					if(Token.hasMoreTokens())
						OPCODE = Token.nextToken();
					if(Token.hasMoreTokens())
						OPERAND = Token.nextToken();

					if(OPCODE.equals("START"))	// Find START
					{
						if(IsStringInt(OPERAND))
						{
							Integer stringtoint = Integer.parseInt(OPERAND,16);
							StaticThings.LOCCTR = stringtoint;
							StaticThings.StartAddress = stringtoint;
							StaticThings.Table.put("START", LABEL);
							if(LABEL.length() < 6)
							{
								int temp = 6 - LABEL.length();
								for(int j = 0; j < temp; j++)
									LABEL += " ";
							}
							StaticThings.ProgramName = LABEL;
						}
						else
						{
							System.out.println("Error : Parameter Unvalid");
							StaticThings.Errorflag = -1;	// Parameter Error
						}
					}
				}
				else if(StaticThings.Table.get("START") != null)
				{
					if(Token.countTokens() == 3)
						LABEL = Token.nextToken();
					if(Token.hasMoreTokens())
						OPCODE = Token.nextToken();
					if(Token.hasMoreTokens())
						OPERAND = Token.nextToken();
					if(LABEL != null)
						LABEL = LABEL.trim();
					if(OPCODE != null)
						OPCODE = OPCODE.trim();
					if(OPERAND != null)
						OPERAND = OPERAND.trim();

					if(OPCODE != null || !OPCODE.equals("."))
					{
						if(OPCODE.equals("END"))
						{
							StaticThings.EndAddress = StaticThings.LOCCTR;
							return;
						}
						else
						{
							/*	Symbol code handle	*/
							if(LABEL != null && StaticThings.Table.containsKey(LABEL))
							{
								System.out.println("Error : Symbol has signed already");
								StaticThings.Errorflag = -1;
							}
							else if(LABEL != null && !StaticThings.Table.containsKey(LABEL))
							{
								StaticThings.Table.put(LABEL, StaticThings.LOCCTR);	 // input LABEL to Symbol Table
								System.out.println(LABEL + "\t" + GetDecToHex((int)StaticThings.Table.get(LABEL)));
							}
							/*	Operation code Handle	*/
							if(OPCODE.charAt(0) == '+' && StaticThings.OpTable.containsKey(OPCODE.substring(1))) // Format 4
							{
								OPTAB temp = (OPTAB)StaticThings.OpTable.get(OPCODE.substring(1));
								StaticThings.LOCCTR += temp.oplength + 1;
							}
							else if(StaticThings.OpTable.containsKey(OPCODE))	// Format 3
							{
								OPTAB temp = (OPTAB)StaticThings.OpTable.get(OPCODE);
								StaticThings.LOCCTR += temp.oplength;
							}
							/* Directive Handle	*/
							else if(StaticThings.DirectTable.containsKey(OPCODE))
							{
								if(OPCODE.equals("RESW") || OPCODE.equals("RESB"))
								{
									int n = 0;

									if(IsStringInt(OPERAND))
									{
										n = Integer.parseInt(OPERAND);
									}
									else
									{
										StaticThings.Errorflag = -1;
									}
									if(OPCODE.equals("RESW"))
									{
										StaticThings.LOCCTR += n * 3;
									}
									else
									{
										StaticThings.LOCCTR += n;
									}
								}
								else if(OPCODE.equals("WORD") || OPCODE.equals("BYTE"))
								{
									if(OPCODE.equals("WORD"))
									{
										StaticThings.LOCCTR += 3;
									}
									else
									{
										if(OPERAND.charAt(0) == 'C')
										{
											StaticThings.LOCCTR += 1 * (OPERAND.length()-3);
										}
										else if(OPERAND.charAt(0) == 'X')
										{
											StaticThings.LOCCTR += 1 * (OPERAND.length()-3)/2;
										}
										else
										{
											System.out.println("Error : Parameter Unvalid");
											StaticThings.Errorflag = -1;
										}
									}
								}
							}
						}
					}
				}
			}

		}
		catch(BadLocationException e)
		{
			System.out.println(e.toString());
		}
	}

	void Pass2()
	{
		/*	Clear Before reading	*/
		StaticThings.LOCCTR = 0;
		StaticThings.BaseAddress = 0;
		StaticThings.Errorflag = 0;

		/*	Variable to read line	`*/
		int lineCount = m_FileTextArea.getLineCount();
		String line = new String();
		StringTokenizer Token;

		/*	Varialbe for Pass2	*/
		String LABEL = new String();
		String OPCODE= new String();
		String OPERAND = new String();
		boolean Start = false;

		try
		{
			for(int i = 0; i < lineCount; i++)
			{
				/*	Initialize to read line	*/
				int start = m_FileTextArea.getLineStartOffset(i);
				int end = m_FileTextArea.getLineEndOffset(i);
				line = m_FileTextArea.getText(start,end-start);

				Token = new StringTokenizer(GetPartOfString(line)," ");
				LABEL = null; OPCODE = null; OPERAND = null;

				if(!Start)
				{
					/*	Get LABEL, OPCODE, OPERAND from line	*/
					if(Token.countTokens() == 3)
						LABEL = Token.nextToken();
					if(Token.hasMoreTokens())
						OPCODE = Token.nextToken();
					if(Token.hasMoreTokens())
						OPERAND = Token.nextToken();

					if(OPCODE.equals("START"))	// Find START
					{
						if(IsStringInt(OPERAND))
						{
							m_TransTextArea.setText(m_TransTextArea.getText() + "H" + StaticThings.ProgramName + GetDecToHex(StaticThings.StartAddress) + GetDecToHex(StaticThings.EndAddress - StaticThings.StartAddress) + "\n"); // Header record
							Start = true;
							Integer stringtoint = Integer.parseInt(OPERAND);
							StaticThings.LOCCTR = stringtoint;
						}
						else
						{
							System.out.println("Error : Parameter Unvalid");
							StaticThings.Errorflag = -1;	// Parameter Error
						}
					}
				}
				else if(Start)
				{
					/*	Get LABEL, OPCODE, OPERAND from line	*/
					if(Token.countTokens() == 3)
					{
						LABEL = Token.nextToken();
					}
					if(Token.hasMoreTokens())
						OPCODE = Token.nextToken();
					if(Token.hasMoreTokens())
						OPERAND = Token.nextToken();

					if(LABEL != null)
						LABEL = LABEL.trim();
					if(OPCODE != null)
						OPCODE = OPCODE.trim();
					if(OPERAND != null)
						OPERAND = OPERAND.trim();

					if(OPCODE != null || !OPCODE.equals("."))
					{
						if(OPCODE.equals("END"))
						{
							for(int index = 0; index < Trecord.TRecordTable.size(); index++)
							{
								m_TransTextArea.setText(m_TransTextArea.getText() + Trecord.TRecordTable.get(index));
							}
							m_TransTextArea.setText(m_TransTextArea.getText() + "E" + GetDecToHex(StaticThings.StartAddress)); // End Record
							return;
						}
						else
						{
							/*	Operation code Handle	*/
							if(OPCODE.charAt(0) == '+' && StaticThings.OpTable.containsKey(OPCODE.substring(1))) // Format 4
							{
								/*	Locctr add handle	*/
								OPTAB temp = (OPTAB)StaticThings.OpTable.get(OPCODE.substring(1));
								StaticThings.LOCCTR += temp.oplength + 1;
								
								BitVector format = new BitVector(4);
								format.Set(11, true); // e
								format.SetOp(temp.op);
								char ch = OPERAND.charAt(0);
								if(ch == '#' || ch == '@')
								{
									if(ch == '#')	// Immediately addressing
									{
										format.Set(7, true); // i
										if(IsStringInt(OPERAND)) // OPREAND is integer
										{
											format.SetAddress(Integer.parseInt(OPERAND));
											Trecord.Append(format.BitToString()); // store T record into table
										}
										else // OPERAND is Symbol
										{
											OperandHandle(OPERAND,format);
											Trecord.Append(format.BitToString());
										}
									}
									else // Indirect addressing
									{
										format.Set(6, true);
										if(IsStringInt(OPERAND))
										{
											format.SetAddress(Integer.parseInt(OPERAND));
											Trecord.Append(format.BitToString());
										}
										else
										{
											OperandHandle(OPERAND,format);
											Trecord.Append(format.BitToString());
										}	
									}
								}
								else if(IsStringInt(OPERAND))
								{
									format.SetAddress(Integer.parseInt(OPERAND));
									Trecord.Append(format.BitToString());
								}
								else
								{
									OperandHandle(OPERAND,format);
									Trecord.Append(format.BitToString());
								}
								
							}
							else if(StaticThings.OpTable.containsKey(OPCODE)) // Format 3
							{
								OPTAB temp = (OPTAB)StaticThings.OpTable.get(OPCODE);
								
								/*	Locctr add handle	*/
								StaticThings.LOCCTR += temp.oplength;
								
								BitVector format = new BitVector(3);
								format.SetOp(temp.op);
								char ch = OPERAND.charAt(0);
								if(ch == '#' || ch == '@')
								{
									if(ch == '#')	// Immediately addressing
									{
										format.Set(7, true); // i
										if(IsStringInt(OPERAND)) // OPREAND is integer
										{
											format.SetAddress(Integer.parseInt(OPERAND));
											Trecord.Append(format.BitToString()); // store T record into table
										}
										else // OPERAND is Symbol
										{
											format.SetAddress((int)StaticThings.Table.get(OPERAND.substring(1, OPERAND.length())));
											Trecord.Append(format.BitToString());
										}
									}
									else // Indirect addressing
									{
										format.Set(6, true);
										if(IsStringInt(OPERAND))
										{
											format.SetAddress(Integer.parseInt(OPERAND));
											Trecord.Append(format.BitToString());
										}
										else
										{
											OperandHandle(OPERAND,format);
											Trecord.Append(format.BitToString());
										}	
									}
								}
								else if(IsStringInt(OPERAND))
								{
									format.SetAddress(Integer.parseInt(OPERAND));
									Trecord.Append(format.BitToString());
								}
								else
								{
									OperandHandle(OPERAND,format);
									Trecord.Append(format.BitToString());
								}
							}
							/* Directive Handle	*/
							else if(StaticThings.DirectTable.containsKey(OPCODE))
							{
								if(OPCODE.equals("RESW") || OPCODE.equals("RESB"))
								{
									/*	Locctr add handle	*/
									int n = 0;

									if(IsStringInt(OPERAND))
									{
										n = Integer.parseInt(OPERAND);
									}
									else
									{
										StaticThings.Errorflag = -1;
									}
									if(OPCODE.equals("RESW"))
									{
										StaticThings.LOCCTR += n * 3;
									}
									else
									{
										StaticThings.LOCCTR += n;
									}
								}
								else if(OPCODE.equals("WORD") || OPCODE.equals("BYTE"))
								{

									/*	Locctr add handle	*/
									if(OPCODE.equals("WORD"))
									{
										StaticThings.LOCCTR += 3;
									}
									else
									{
										if(OPERAND.charAt(0) == 'C')
										{
											StaticThings.LOCCTR += 1 * (OPERAND.length()-3);
										}
										else if(OPERAND.charAt(0) == 'X')
										{
											StaticThings.LOCCTR += 1 * (OPERAND.length()-3)/2;
										}
										else
										{
											StaticThings.Errorflag = -1;
										}
									}
								}
								else if(OPCODE.equals("BASE"))
								{

								}
							}
						}
					}
				}
			}

		}
		catch(BadLocationException e)
		{
			System.out.println(e.toString());
		}
	}

	boolean IsStringInt(String s)	// Can String transform to int
	{
		try
		{
			Integer.parseInt(s);
			return true;
		}
		catch(NumberFormatException e)
		{
			return false;
		}
	}
	String GetPartOfString(String str)	// Read line
	{
		String part = new String();
		char[] temp = new char[35];

		for(int i = 0; i < 35; i++)
		{
			temp[i] = 0;
		}

		for(int i = 0; i < 35 && i < str.length(); i++)
		{
			temp[i] = str.charAt(i);
		}
		part = new String(temp);

		return part;
	}
	String GetDecToHex(int dec) // Dec -> Hex method
	{
		String hex;

		hex = Integer.toHexString(dec);
		if(hex.length() < 6)
		{
			int temp = 6 - hex.length();
			for(int i = 0; i < temp; i++)
			{
				hex = "0" + hex;
			}
		}

		return hex.toUpperCase();
	}
	char OperandHandle(String OPERAND,BitVector format) // Only handle OPERAND is Symbol
	{
		StringTokenizer OPERANDToken = null;
		char operation = 0;
		
		/*	Searching Operation	*/
		for(int index = 0; index < OPERAND.length(); index++)
		{
			switch(OPERAND.charAt(index))
			{
			case ',':OPERANDToken = new StringTokenizer(OPERAND,","); operation = ',';
				index = OPERAND.length();break;
			case '+':OPERANDToken = new StringTokenizer(OPERAND,"+"); operation = '+';
				index = OPERAND.length();break;
			case '-':OPERANDToken = new StringTokenizer(OPERAND,"-"); operation = '-';
				index = OPERAND.length();break;
			case '=':OPERANDToken = new StringTokenizer(OPERAND,"="); operation = '=';
				index = OPERAND.length();break;
			}
		}
		/*	Do as Operation	*/
		switch(operation)
		{
		case ',' :
			if(format.m_size == 3)
			{
				format.Set(8, true); // x
				int PC = StaticThings.LOCCTR;
				int BASE = StaticThings.BaseAddress;
				int Target = (int)StaticThings.Table.get(OPERANDToken.nextToken());
				
				if(-2048 >= Target - PC && Target - PC <=2047)
				{
					format.Set(10, true); // p
				}	
				else if(0 >= Target - BASE && Target - BASE <= 4095)
				{
					format.Set(9, true); // b
				}
			}
			else // register
			{
				int r1 = (int)StaticThings.RegistTable.get(OPERANDToken.nextToken());
				int r2 = (int)StaticThings.RegistTable.get(OPERANDToken.nextToken());

				format.SetRegister(r1, r2);
			}
			break;
		case '+':
			break;
		case '-':
			break;
		case '=':
			break;
		case 0:		// Default
			if(format.m_size == 4)
			{
				int Target = (int)StaticThings.Table.get(OPERAND);
				format.SetAddress(Target);
			}
			else if(format.m_size == 3)
			{
				int PC = StaticThings.LOCCTR;
				int BASE = StaticThings.BaseAddress;
				int Target = (int)StaticThings.Table.get(OPERAND);
				
				if(-2048 >= Target - PC && Target - PC <=2047)
				{
					format.Set(10, true); // p
				}	
				else if(0 >= Target - BASE && Target - BASE <= 4095)
				{
					format.Set(9, true); // b
				}			
			}
			else // register
			{
				int r1 = (int)StaticThings.RegistTable.get(OPERANDToken.nextToken());

				format.SetRegister(r1, 0);
			}
			break;
		}
		
		return operation;
	}
}

/*	File load and save system	*/
class FileDialogWindow extends JFrame
{
	JFileChooser  load;
	JFileChooser  save;
	FileNameExtensionFilter filter;

	public void Load(JTextArea p_FileTextArea) throws IOException
	{
		filter = new FileNameExtensionFilter("Text File","txt");
		load = new JFileChooser();
		load.setCurrentDirectory(new File("user.home"));	//	Set Default Load Location
		load.setVisible(true);
		load.setAcceptAllFileFilterUsed(false);
		load.addChoosableFileFilter(filter);


		int result = load.showOpenDialog(this);

		if(result ==  JFileChooser.APPROVE_OPTION)
		{
			File  selectedFile = load.getSelectedFile();
			System.out.println("Folder Address : " + selectedFile.getAbsolutePath());
			System.out.println("File name : " + selectedFile.getName());

			BufferedReader in = null;

			try
			{
				in = new BufferedReader(new FileReader(selectedFile));
				p_FileTextArea.setText("");

				String s = null;
				while( (s = in.readLine()) != null)
				{
					p_FileTextArea.setText(p_FileTextArea.getText() + s + '\n');
				}
			}
			catch(Exception e)
			{
				System.out.println("Cannot Load File");
			}
			finally
			{
				if(in != null)
				{
					in.close();
				}
			}
		}
	}

	public void Save()
	{
		save = new JFileChooser();
		save.setCurrentDirectory(new File("C:/Users/mit-com-2/Desktop/Assembler"));	//	Set Default Load Location
		save.setVisible(true);
		save.setAcceptAllFileFilterUsed(false);
		save.addChoosableFileFilter(filter);

	}
}

/*	Main	*/
public class ASEM
{
	static public void main(String[] args) //throws IOException
	{
		/*	Assembler operations table	*/
		StaticThings.OpTable.put("ADD",new OPTAB(0x18,3,0));StaticThings.OpTable.put("ADDF",new OPTAB(0x58,3,1));StaticThings.OpTable.put("ADDR",new OPTAB(0x90,2,1));
		StaticThings.OpTable.put("AND",new OPTAB(0x40,3,0));StaticThings.OpTable.put("CLEAR",new OPTAB(0xB4,2,1));StaticThings.OpTable.put("COMP",new OPTAB(0x28,3,0));
		StaticThings.OpTable.put("COMPF",new OPTAB(0x88,3,1));StaticThings.OpTable.put("COMPR",new OPTAB(0xA0,2,1));StaticThings.OpTable.put("DIV",new OPTAB(0x24,3,0));
		StaticThings.OpTable.put("DIVF",new OPTAB(0x64,3,1));StaticThings.OpTable.put("DIVR",new OPTAB(0x64,2,1));StaticThings.OpTable.put("FIX",new OPTAB(0xC4,1,1));
		StaticThings.OpTable.put("FLOAT",new OPTAB(0xC0,1,1));StaticThings.OpTable.put("HIO",new OPTAB(0xF4,1,1));StaticThings.OpTable.put("J",new OPTAB(0x3C,3,0));
		StaticThings.OpTable.put("JEQ",new OPTAB(0x30,3,0));StaticThings.OpTable.put("JGT",new OPTAB(0x34,3,0));StaticThings.OpTable.put("JLT",new OPTAB(0x38,3,0));
		StaticThings.OpTable.put("JSUB",new OPTAB(0x48,3,0));StaticThings.OpTable.put("LDA",new OPTAB(0x00,3,0));StaticThings.OpTable.put("LDB",new OPTAB(0x68,3,1));
		StaticThings.OpTable.put("LDCH",new OPTAB(0x50,3,0));StaticThings.OpTable.put("LDF",new OPTAB(0x70,3,1));StaticThings.OpTable.put("LDL",new OPTAB(0x08,3,0));
		StaticThings.OpTable.put("LDS",new OPTAB(0x6C,3,1));StaticThings.OpTable.put("LDT",new OPTAB(0x74,3,1));StaticThings.OpTable.put("LDX",new OPTAB(0x04,3,0));
		StaticThings.OpTable.put("LPS",new OPTAB(0xD0,3,1));StaticThings.OpTable.put("MUL",new OPTAB(0x20,3,0));StaticThings.OpTable.put("MULF",new OPTAB(0x60,3,1));
		StaticThings.OpTable.put("MULR",new OPTAB(0x98,2,1));StaticThings.OpTable.put("NORM",new OPTAB(0xC8,1,1));StaticThings.OpTable.put("OR",new OPTAB(0x44,3,0));
		StaticThings.OpTable.put("RD",new OPTAB(0xD8,3,0));StaticThings.OpTable.put("RMO",new OPTAB(0xAC,2,1));StaticThings.OpTable.put("RSUB",new OPTAB(0x4C,3,0));
		StaticThings.OpTable.put("SHIFTL",new OPTAB(0xA4,2,1));StaticThings.OpTable.put("SHIFTR",new OPTAB(0xA8,2,1));StaticThings.OpTable.put("SIO",new OPTAB(0xF0,1,1));
		StaticThings.OpTable.put("SSK",new OPTAB(0xEC,3,1));StaticThings.OpTable.put("STA",new OPTAB(0x0C,3,0));StaticThings.OpTable.put("STB",new OPTAB(0x78,3,1));
		StaticThings.OpTable.put("STCH",new OPTAB(0x54,3,0));StaticThings.OpTable.put("STF",new OPTAB(0x80,3,1));StaticThings.OpTable.put("STI",new OPTAB(0xD4,3,1));
		StaticThings.OpTable.put("STL",new OPTAB(0x14,3,0));StaticThings.OpTable.put("STS",new OPTAB(0x7C,3,1));StaticThings.OpTable.put("STSW",new OPTAB(0xE8,3,0));
		StaticThings.OpTable.put("STT",new OPTAB(0x84,3,1));StaticThings.OpTable.put("STX",new OPTAB(0x10,3,0));StaticThings.OpTable.put("SUB",new OPTAB(0x1C,3,0));
		StaticThings.OpTable.put("SUBF",new OPTAB(0x5C,3,1));StaticThings.OpTable.put("SUBR",new OPTAB(0x94,2,1));StaticThings.OpTable.put("SVC",new OPTAB(0xB0,2,1));
		StaticThings.OpTable.put("TD",new OPTAB(0xE0,3,0));StaticThings.OpTable.put("TIO",new OPTAB(0xF8,1,1));StaticThings.OpTable.put("TIX",new OPTAB(0x2C,3,0));
		StaticThings.OpTable.put("TIXR",new OPTAB(0xB8,2,1));StaticThings.OpTable.put("WD",new OPTAB(0xDC,3,0));

		/*	Assembler directives table	*/
		StaticThings.DirectTable.put("BASE",new OPTAB(1,0,0));  StaticThings.DirectTable.put("NOBASE",new OPTAB(2,0,0)); StaticThings.DirectTable.put("BYTE",new OPTAB(3,0,0)); StaticThings.DirectTable.put("END",new OPTAB(4,0,0));
		StaticThings.DirectTable.put("EQU",new OPTAB(5,0,0)); StaticThings.DirectTable.put("LTORG",new OPTAB(6,0,0)); StaticThings.DirectTable.put("RESB",new OPTAB(7,0,0));   StaticThings.DirectTable.put("RESW",new OPTAB(8,0,0));
		StaticThings.DirectTable.put("START",new OPTAB(9,0,0)); StaticThings.DirectTable.put("WORD",new OPTAB(10,0,0)); StaticThings.DirectTable.put("USE",new OPTAB(11,0,0));   StaticThings.DirectTable.put("CSECT",new OPTAB(12,0,0));
		StaticThings.DirectTable.put("EXTREF",new OPTAB(13,0,0)); StaticThings.DirectTable.put("EXTDEF",new OPTAB(14,0,0));

		/*	Assembler Register Table	*/
		StaticThings.RegistTable.put("A", 0); StaticThings.RegistTable.put("X", 1); StaticThings.RegistTable.put("L", 2); StaticThings.RegistTable.put("B", 3);
		StaticThings.RegistTable.put("S", 4); StaticThings.RegistTable.put("T", 5); StaticThings.RegistTable.put("F", 6);
		
		new MainWindow();
	}
}
