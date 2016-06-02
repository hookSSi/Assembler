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

/*	Symbol Node	*/
class TAB
{
	int address;	// LOCCTR
	String value; // value
	String op;	// Operation Code

	TAB(int address, String value,String op)
	{
		this.address = address;
		this.value = value;
		this.op = op;
	}
	public int GetAddress(){return address;}
	public String GetValue(){return value;}
	public String GetOp(){return op;}
}

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
	/*	M record List	*/
	public static ArrayList<String> MList = new ArrayList<String>();
	/*	Error List	*/
	public static ArrayList<String> ErrorList = new ArrayList<String>();

	public static int LOCCTR; 	// Address Counter
	public static int BeforeLOCCTR;
	public static String ProgramName = null;
	public static int StartAddress; // Start Address
	public static int EndAddress;
	public static int BaseAddress; // Base Address
	public static String BaseOPRAND = null; // Base operand
	public static int Errorflag;

	public static void ClearPass1() // Clear to assemble again
	{
		Table = new Hashtable();
		LOCCTR = 0;
		BeforeLOCCTR = 0;
		ProgramName = new String();
		StartAddress = 0;
		EndAddress = 0;
		BaseAddress = 0;
		BaseOPRAND = new String();
		Errorflag = 0;
		MList = new ArrayList<String>();
		ErrorList = new ArrayList<String>();
	}
	public static void ClearPass2() // Clear to assemble again
	{
		LOCCTR = 0;
		BeforeLOCCTR = 0;
		StartAddress = 0;
		Trecord.Clear();
	}
}

/*	To store T record	*/
class Trecord
{
	public static ArrayList<String> TRecordTable = new ArrayList<String>();
	public static String temp = new String(); // Store untill max T record size
	public static int startAddress = 0;
	public static int endAddress = 0;
	public static int specialEndAddress = 0;
	public static boolean special = false;
	public static int count = 0;
	public static int index = 0;
	public static boolean IsTooBig = false;

	public static void Clear()
	{
		TRecordTable = new ArrayList<String>();
		specialEndAddress = 0;
		startAddress = StaticThings.StartAddress;
		endAddress = 0;
		count = 0;
		index = 0;
		TRecordTable.add("T" + GetDecToHex(startAddress));
		index = TRecordTable.size();
	}
	public static void Append(String T)
	{
		int length = T.length();

		if(count + length < 60 && !IsTooBig)
		{
			TRecordTable.add(T);
			count += length;
		}
		else if((count + length >= 60))
		{
			int start = endAddress;
			endAddress = StaticThings.BeforeLOCCTR;
			TRecordTable.add(index,GetDecToHex(endAddress - start).substring(4));
			count = 0;
			TRecordTable.add("\nT" + GetDecToHex(StaticThings.BeforeLOCCTR)); //+GetDecToHex(StaticThings.LOCCTR)
			index = TRecordTable.size();
			TRecordTable.add(T);
			count += length;
			IsTooBig = false;
			startAddress = StaticThings.BeforeLOCCTR;
		}
		else if(IsTooBig)
		{
			endAddress = StaticThings.BeforeLOCCTR;
			TRecordTable.add(index,GetDecToHex(specialEndAddress - startAddress).substring(4));
			count = 0;
			TRecordTable.add("\nT" + GetDecToHex(StaticThings.BeforeLOCCTR)); //+GetDecToHex(StaticThings.LOCCTR)
			index = TRecordTable.size();
			TRecordTable.add(T);
			count += length;
			IsTooBig = false;
			startAddress = StaticThings.BeforeLOCCTR;
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
			m_array[0] =  temp.byteValue();
			Set(6,true); // n
			Set(7,true); // i
			Set(8, false); // x
			Set(9, false); // b
			Set(10, false); // p
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
			Set(11, true); // e
		}
		else if(m_size == 3)
		{
			if(p_address >= 0)
			{
				Integer temp1 = new Integer(p_address % 256);
				Integer temp2 = new Integer(p_address / 256);
				m_array[2] = temp1.byteValue();
				m_array[1] = temp2.byteValue();
			}
			else
			{
				Integer temp1 = new Integer(-p_address % 256);
				Integer temp2 = new Integer(-p_address / 256);
				m_array[2] = temp1.byteValue();
				m_array[1] = temp2.byteValue();

				for(int i = 12; i < 24; i++)
				{
					if(GetBit(i) > 0)
						Set(i, false);
					else
						Set(i,true);
				}
				//int outofindex = GetBit(10);
				m_array[2]++;
//				if(GetBit(10) != outofindex)
//					Set(11,false);

			}
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
		for(int i = 0; i < m_size * 8; i = i + 4)
			str = str + GetDecToHex((GetBit(i) * 8 + GetBit(i+1) * 4 + GetBit(i+2) * 2 + GetBit(i+3)));
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
		m_MenuBar.setBounds(5,5,500,40);

		m_File = new JMenu("File");

		/*	Menu Componets	*/
		m_File.add(m_Open); m_File.add(m_Save);

		/*	MenuBar	*/
		m_MenuBar.add(m_File);

		/*	File Input Area	*/
		m_FileTextArea = new JTextArea();
		m_FileTextArea.setBounds(0,0,400,300);
		m_FileTextArea.setEditable(true);
		m_FileTextArea.setLineWrap(true);
		m_FileTextArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		m_FileTextScroll = new JScrollPane(m_FileTextArea,
    ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
    ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		m_FileTextScroll.setBounds(5, 50, 510, 410);

		/*	Trans Output Area	*/
		m_TransTextArea = new JTextArea();
		m_TransTextArea.setBounds(0,0,400,300);
		m_TransTextArea.setEditable(false);
		m_TransTextArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		m_TransTextScroll = new JScrollPane(m_TransTextArea,
    ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
    ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		m_TransTextScroll.setBounds(5, 600, 510, 410);

		m_TransButton = new JButton("Assemble");
		m_TransButton.setBounds(215,500,100,50);
		m_TransButton.setSize(100,50);
		m_TransButton.addActionListener(this);

		/*	Main Panel	*/
		m_MainPanel.setBounds(0,0,530,1050);
		m_MainPanel.setLayout(null);
		m_MainPanel.add(m_FileTextScroll);
		m_MainPanel.add(m_TransButton);
		m_MainPanel.add(m_TransTextScroll);
		m_MainPanel.add(m_MenuBar);

		/*	Main Frame	*/
		m_MainFrame.getContentPane().add(m_MainPanel);

		m_MainFrame.setLayout(null);
		m_MainFrame.setBounds(m_MonitorSize.width/2 - 200,0,530,1050);
		m_MainFrame.setPreferredSize(new Dimension(530,1050));
		m_MainFrame.pack();
		m_MainFrame.setVisible(true);
		m_MainFrame.setResizable(false);
		m_MainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	public void actionPerformed(ActionEvent e) //	input manager
	{
		if(e.getSource() == m_TransButton)	//	Assemble Button
		{
			StaticThings.ClearPass1();
			Pass1();
			StaticThings.ClearPass2();
			Pass2();
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
			try
			{
				m_FileDialog.Save(m_TransTextArea);
			}
			catch (IOException e1)
			{
				System.out.println("Couldn't Load File");
			}

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
							StaticThings.Table.put("START", new TAB(StaticThings.LOCCTR, OPERAND, OPCODE));
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
							StaticThings.ErrorList.add("Error : Parameter Unvalid - " + StaticThings.LOCCTR + " in Pass1\n");
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
							TAB symbol = (TAB)StaticThings.Table.get(StaticThings.BaseOPRAND);
							StaticThings.BaseAddress = symbol.GetAddress();
							StaticThings.EndAddress = StaticThings.LOCCTR;
							return;
						}
						else
						{
							/*	Symbol code handle	*/
							if(LABEL != null && StaticThings.Table.containsKey(LABEL))
							{
								StaticThings.ErrorList.add("Error : Symbol has signed already" + StaticThings.LOCCTR + " in Pass1\n");
								StaticThings.Errorflag = -1;
							}
							else if(LABEL != null && !StaticThings.Table.containsKey(LABEL))
							{
								StaticThings.Table.put(LABEL, new TAB(StaticThings.LOCCTR,OPERAND,OPCODE));	 // input LABEL to Symbol Table
								TAB symbol = (TAB)StaticThings.Table.get(LABEL);
								System.out.println(LABEL + "\t" + GetDecToHex(symbol.GetAddress()));
							}
							/*	Operation code Handle	*/
							if(OPCODE.charAt(0) == '+' && StaticThings.OpTable.containsKey(OPCODE.substring(1))) // Format 4
							{
								OPTAB temp = (OPTAB)StaticThings.OpTable.get(OPCODE.substring(1));
								StaticThings.BeforeLOCCTR = StaticThings.LOCCTR;
								StaticThings.LOCCTR += temp.oplength + 1;
							}
							else if(StaticThings.OpTable.containsKey(OPCODE))	// Format 3
							{
								OPTAB temp = (OPTAB)StaticThings.OpTable.get(OPCODE);
								StaticThings.BeforeLOCCTR = StaticThings.LOCCTR;
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
										StaticThings.ErrorList.add("Parameter Error" + StaticThings.BeforeLOCCTR + " in Pass2\n");
										StaticThings.Errorflag = -1;
									}
									if(OPCODE.equals("RESW"))
									{
										StaticThings.BeforeLOCCTR = StaticThings.LOCCTR;
										StaticThings.LOCCTR += n * 3;
									}
									else
									{
										StaticThings.BeforeLOCCTR = StaticThings.LOCCTR;
										StaticThings.LOCCTR += n;
									}
								}
								else if(OPCODE.equals("WORD") || OPCODE.equals("BYTE"))
								{
									if(OPCODE.equals("WORD"))
									{
										StaticThings.BeforeLOCCTR = StaticThings.LOCCTR;
										StaticThings.LOCCTR += 3;
									}
									else
									{
										if(OPERAND.charAt(0) == 'C')
										{
											StaticThings.BeforeLOCCTR = StaticThings.LOCCTR;
											StaticThings.LOCCTR += 1 * (OPERAND.length()-3);
										}
										else if(OPERAND.charAt(0) == 'X')
										{
											StaticThings.BeforeLOCCTR = StaticThings.LOCCTR;
											StaticThings.LOCCTR += 1 * (OPERAND.length()-3)/2;
										}
										else
										{
											StaticThings.ErrorList.add("Error : Parameter Unvalid" + StaticThings.BeforeLOCCTR + " in Pass2\n");
											StaticThings.Errorflag = -1;
										}
									}
								}
								else if(OPCODE.equals("BASE"))
								{
									StaticThings.BaseOPRAND = OPERAND;
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
							if(StaticThings.Errorflag != -1)
							{
								m_TransTextArea.setText(m_TransTextArea.getText() + "H" + StaticThings.ProgramName + GetDecToHex(StaticThings.StartAddress) + GetDecToHex(StaticThings.EndAddress - StaticThings.StartAddress) + "\n"); // Header record
								Start = true;
								Integer stringtoint = Integer.parseInt(OPERAND);
								StaticThings.LOCCTR = stringtoint;
							}
						}
						else
						{
							StaticThings.ErrorList.add("Error : Parameter Unvalid" + StaticThings.BeforeLOCCTR + " in Pass2\n");
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
							if(StaticThings.Errorflag != -1)
							{
								Trecord.TRecordTable.add(Trecord.index,GetDecToHex(StaticThings.LOCCTR  - Trecord.endAddress).substring(4));
								for(int index1 = 0; index1 < Trecord.TRecordTable.size(); index1++)
								{
									m_TransTextArea.setText(m_TransTextArea.getText() + Trecord.TRecordTable.get(index1));
								}
								for(int index2 = 0; index2 < StaticThings.MList.size(); index2++)
								{
									m_TransTextArea.setText(m_TransTextArea.getText() + "\nM" + StaticThings.MList.get(index2) + "05");
								}
								m_TransTextArea.setText(m_TransTextArea.getText() + "\nE" + GetDecToHex(StaticThings.StartAddress)); // End Record
							}								
							return;
						}
						else
						{
							/*	Operation code Handle	*/
							/*	Format 4	*/
							if(OPCODE.charAt(0) == '+' && StaticThings.OpTable.containsKey(OPCODE.substring(1)))
							{
								Trecord.special = false;
								/*	Locctr add handle	*/
								OPTAB temp = (OPTAB)StaticThings.OpTable.get(OPCODE.substring(1));
								StaticThings.BeforeLOCCTR = StaticThings.LOCCTR;
								StaticThings.LOCCTR += temp.oplength + 1;
								BitVector format = new BitVector(4);
								format.SetOp(temp.op);
								char ch = 0;
								if(OPERAND != null)
									ch = OPERAND.charAt(0);
								if(ch == '#' || ch == '@')
								{
									OPERAND = OPERAND.substring(1);
									if(ch == '#')	// Immediately addressing
									{
										format.Set(6, false); // i
										if(IsStringInt(OPERAND)) // OPERAND is integer
										{
											format.SetAddress(Integer.parseInt(OPERAND));
											Trecord.Append(format.BitToString()); // store T record into table
										}
										else // OPERAND is Symbol
										{
											TAB symbol = (TAB)StaticThings.Table.get(OPERAND);
											format.SetAddress(symbol.GetAddress());
											Trecord.Append(format.BitToString());
											StaticThings.MList.add(GetDecToHex(StaticThings.BeforeLOCCTR + 1));
										}
									}
									else // Indirect addressing
									{
										format.Set(7, false);
										if(IsStringInt(OPERAND))	// OPERAND is integer
										{
											int Target = Integer.parseInt(OPERAND);
											format.SetAddress(Target);
											Trecord.Append(format.BitToString()); // store T record into table
										}
										else	// OPERAND is Symbol
										{
											TAB symbol = (TAB)StaticThings.Table.get(OPERAND);
											if(IsStringInt(symbol.GetValue()))
											{
												OPTAB op = (OPTAB)StaticThings.OpTable.get(symbol.op);
												format.SetAddress(op.oplength * Integer.parseInt(symbol.GetValue()));
											}
											else
											{
												TAB symbol2 = (TAB)StaticThings.Table.get(symbol.GetValue());
												format.SetAddress(symbol2.GetAddress());
											}
											Trecord.Append(format.BitToString());
											StaticThings.MList.add(GetDecToHex(StaticThings.BeforeLOCCTR + 1));
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
									OperandHandle(OPERAND, format);
									Trecord.Append(format.BitToString());
									StaticThings.MList.add(GetDecToHex(StaticThings.BeforeLOCCTR + 1));
								}
							}
							/*	Format 3,Format 2, Format 1	*/
							else if(StaticThings.OpTable.containsKey(OPCODE))
							{
								Trecord.special = false;
								OPTAB temp = (OPTAB)StaticThings.OpTable.get(OPCODE);
								/*	Locctr add handle	*/
								StaticThings.BeforeLOCCTR = StaticThings.LOCCTR;
								StaticThings.LOCCTR += temp.oplength;
								if(temp.oplength == 3)	//	Format 3
								{
									BitVector format = new BitVector(temp.oplength);
									format.SetOp(temp.op);
									char ch = 0;
									if(OPERAND != null)
										ch = OPERAND.charAt(0);
									else
										OPERAND = "77777777";
									if(ch == '#' || ch == '@')
									{
										OPERAND = OPERAND.substring(1);
										if(ch == '#')	// Immediately addressing
										{
											format.Set(6, false); // i
											if(IsStringInt(OPERAND)) // OPREAND is integer
											{
												int Target = Integer.parseInt(OPERAND);
												format.SetAddress(Target);
												Trecord.Append(format.BitToString()); // store T record into table
											}
											else // OPERAND is Symbol
											{
												TAB symbol = (TAB)StaticThings.Table.get(OPERAND);
												int Target = symbol.GetAddress();
												format.SetAddress(Target);
												int PC = StaticThings.LOCCTR;
												int BASE = StaticThings.BaseAddress;

												if(-2048 <= Target - PC && Target - PC < 2048)
												{
													format.SetAddress(Target - PC);
													format.Set(10, true); // p
												}
												else if(0 <= Target - BASE && Target - BASE < 4096)
												{
													format.SetAddress(Target - BASE);
													format.Set(9, true); // b
												}
												else
												{
													StaticThings.ErrorList.add("Format 3 Immediately addressing - Out of Index" + StaticThings.BeforeLOCCTR + " in Pass2\n");
													StaticThings.Errorflag = -1; // Out of index
												}
												Trecord.Append(format.BitToString()); // store T record into table
											}
										}
										else // Indirect addressing
										{
											format.Set(7, false);
											if(IsStringInt(OPERAND))
											{
												int Target = Integer.parseInt(OPERAND);
												format.SetAddress(Target);
												Trecord.Append(format.BitToString()); // store T record into table
											}
											else
											{
												TAB symbol = (TAB)StaticThings.Table.get(OPERAND);
												int Target = symbol.GetAddress();
												int PC = StaticThings.LOCCTR;
												int BASE = StaticThings.BaseAddress;

												if(-2048 <= Target - PC && Target - PC < 2048)
												{
													format.SetAddress(Target - PC);
													format.Set(10, true); // p
												}
												else if(0 <= Target - BASE && Target - BASE < 4096)
												{
													format.SetAddress(Target - BASE);
													format.Set(9, true); // b
												}
												else
												{
													StaticThings.ErrorList.add("Format 3 Indirect addressing - Out of index" + StaticThings.BeforeLOCCTR + " in Pass2\n");
													StaticThings.Errorflag = -1; // Out of index
												}
												Trecord.Append(format.BitToString()); // store T record into table
											}
										}
									}
									else if(IsStringInt(OPERAND))		// integer
									{
										int Target = Integer.parseInt(OPERAND);
										int PC = StaticThings.LOCCTR;
										int BASE = StaticThings.BaseAddress;
										
										if(-2048 <= Target - PC && Target - PC < 2048)
										{
											format.SetAddress(Target - PC);
											format.Set(10, true); // p
										}
										else if(0 <= Target - BASE && Target - BASE < 4096)
										{
											format.SetAddress(Target - BASE);
											format.Set(9, true); // b
										}
										else if(OPERAND.equals("77777777"))
										{
											format.SetAddress(0);
										}
										else
										{
											StaticThings.ErrorList.add(OPCODE + "  " + OPERAND + " Format 3 Integer Addressing - Out of index"  + StaticThings.BeforeLOCCTR + " in Pass2\n");
											StaticThings.Errorflag = -1; // Out of index
										}
										Trecord.Append(format.BitToString()); // store T record into table
									}
									else
									{
										OperandHandle(OPERAND,format);
										Trecord.Append(format.BitToString());
									}
								}
								else if(temp.oplength == 2)
								{
									BitVector format = new BitVector(temp.oplength);
									format.SetOp(temp.op);
									OperandHandle(OPERAND,format);
									Trecord.Append(format.BitToString());
								}
								else if(temp.oplength == 1)
								{
									BitVector format = new BitVector(temp.oplength);
									format.SetOp(temp.op);
									format.SetRegister((int)StaticThings.RegistTable.get(OPERAND), 0);
									Trecord.Append(format.BitToString());
								}
							}
							/* Directive Handle	*/
							else if(StaticThings.DirectTable.containsKey(OPCODE))
							{
								if(OPCODE.equals("RESW") || OPCODE.equals("RESB"))
								{
									/*	Locctr add handle variable	*/
									int n = 0;

									if(IsStringInt(OPERAND))
									{
										n = Integer.parseInt(OPERAND);
									}
									else
									{
										StaticThings.ErrorList.add("Parameter Error"  + StaticThings.BeforeLOCCTR + " in Pass2\n");
										StaticThings.Errorflag = -1;
									}
									if(OPCODE.equals("RESW"))
									{
										StaticThings.BeforeLOCCTR = StaticThings.LOCCTR;
										StaticThings.LOCCTR += n * 3;
									}
									else
									{
										StaticThings.BeforeLOCCTR = StaticThings.LOCCTR;
										StaticThings.LOCCTR += n;
									}

									if(StaticThings.LOCCTR -StaticThings.BeforeLOCCTR >= 1000)
									{
										Trecord.IsTooBig = true;
									}
									if(!Trecord.special)
									{
										Trecord.specialEndAddress = StaticThings.BeforeLOCCTR;
										Trecord.special = true;
									}
								}
								else if(OPCODE.equals("WORD") || OPCODE.equals("BYTE"))
								{

									/*	Locctr add handle	*/
									if(OPCODE.equals("WORD"))
									{
										StaticThings.BeforeLOCCTR = StaticThings.LOCCTR;
										StaticThings.LOCCTR += 3;
									}
									else
									{
										if(OPERAND.charAt(0) == 'C')
										{
											StaticThings.BeforeLOCCTR = StaticThings.LOCCTR;
											StaticThings.LOCCTR += 1 * (OPERAND.length()-3);
											String temp = "";

											for(int index = 2; index < OPERAND.length() - 1; index++ )
											{
												temp += (Integer.toHexString(OPERAND.charAt(index))).toUpperCase();
											}
											Trecord.Append(temp);
										}
										else if(OPERAND.charAt(0) == 'X')
										{
											StaticThings.BeforeLOCCTR = StaticThings.LOCCTR;
											StaticThings.LOCCTR += 1 * (OPERAND.length()-3)/2;

											Trecord.Append(OPERAND.substring(2, OPERAND.length() - 1));
										}
										else
										{
											StaticThings.ErrorList.add("Parameter Error"  + StaticThings.BeforeLOCCTR + " in Pass2\n");
											StaticThings.Errorflag = -1;
										}
									}
								}
								else if(OPCODE.equals("BASE"))
								{
									TAB symbol = (TAB)StaticThings.Table.get(OPERAND);
									StaticThings.BaseAddress = symbol.GetAddress();
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

	char OperandHandle(String OPERAND,BitVector format) // Only handle OPERAND has operation
	{
		StringTokenizer OPERANDToken = new StringTokenizer(OPERAND);
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
			case '=':OPERANDToken = new StringTokenizer(OPERAND,"'"); operation = '=';
				index = OPERAND.length();break;
			}
		}

		/*	Do as Operation	*/
		switch(operation)
		{
		case ',' :
			if(format.m_size == 4)
			{
				format.Set(8, true); // x
				int Target = 0;
				String operand = OPERANDToken.nextToken();
				if(IsStringInt(operand))
				{
					Target = Integer.parseInt(operand);
					format.SetAddress(Target);
				}
				else
				{
					TAB symbol = (TAB)StaticThings.Table.get(operand);
					format.SetAddress(symbol.GetAddress());
					Target = symbol.GetAddress();
				}
			}
			else if(format.m_size == 3)
			{
				String operand = OPERANDToken.nextToken();
				int PC = StaticThings.LOCCTR;
				int BASE = StaticThings.BaseAddress;
				int Target = 0;
				if(IsStringInt(operand))
				{
					Target = Integer.parseInt(operand);
				}
				else
				{
					TAB symbol = (TAB)StaticThings.Table.get(operand);
					Target = symbol.GetAddress();
				}

				if(-2048 <= Target - PC && Target - PC < 2048)
				{
					format.SetAddress(Target - PC);
					format.Set(10, true); // p
				}
				else if(0 <= Target - BASE && Target - BASE < 4096)
				{
					format.SetAddress(Target - BASE);
					format.Set(9, true); // b
				}
				else
				{
					StaticThings.ErrorList.add("Format 3 X addressing - Out of index"  + StaticThings.BeforeLOCCTR + " in Pass2\n");
					StaticThings.Errorflag = -1; // Out of index
				}
				format.Set(8, true); // x
			}
			else // register
			{
				int r1 = (int)StaticThings.RegistTable.get(OPERANDToken.nextToken());
				int r2 = (int)StaticThings.RegistTable.get(OPERANDToken.nextToken());

				format.SetRegister(r1, r2);
			}
			break;
		case '+':
			if(format.m_size == 4)
			{
				String a = OPERANDToken.nextToken();
				String b = OPERANDToken.nextToken();
				int result = 0;

				if(IsStringInt(a))
				{
					result += Integer.parseInt(a);
				}
				else
				{
					TAB symbol = (TAB)StaticThings.Table.get(a);
					result += symbol.GetAddress();
				}
				if(IsStringInt(a))
				{
					result += Integer.parseInt(a);
				}
				else
				{
					TAB symbol = (TAB)StaticThings.Table.get(a);
					result += symbol.GetAddress();
				}
				format.SetAddress(result);
			}
			else if(format.m_size == 3)
			{
				String a = OPERANDToken.nextToken();
				String b = OPERANDToken.nextToken();
				int PC = StaticThings.LOCCTR;
				int BASE = StaticThings.BaseAddress;
				int result = 0;

				if(IsStringInt(a))
				{
					result += Integer.parseInt(a);
				}
				else
				{
					TAB symbol = (TAB)StaticThings.Table.get(a);
					result += symbol.GetAddress();
				}
				if(IsStringInt(a))
				{
					result += Integer.parseInt(a);
				}
				else
				{
					TAB symbol = (TAB)StaticThings.Table.get(a);
					result += symbol.GetAddress();
				}

				if(-2048 <= result - PC && result - PC < 2048)
				{
					format.SetAddress(result - PC);
					format.Set(10, true); // p
				}
				else if(0 <= result - BASE && result - BASE < 4096)
				{
					format.SetAddress(result - BASE);
					format.Set(9, true); // b
				}
				else
				{
					StaticThings.ErrorList.add("Format 3 + addressing - Out of index"  + StaticThings.BeforeLOCCTR + " in Pass2\n");
					StaticThings.Errorflag = -1; // Out of index
				}
			}
			break;
		case '-':
			if(format.m_size == 4)
			{
				String a = OPERANDToken.nextToken();
				String b = OPERANDToken.nextToken();
				int result = 0;

				if(IsStringInt(a))
				{
					result += Integer.parseInt(a);
				}
				else
				{
					TAB symbol = (TAB)StaticThings.Table.get(a);
					result += symbol.GetAddress();
				}
				if(IsStringInt(b))
				{
					result += Integer.parseInt(b);
				}
				else
				{
					TAB symbol = (TAB)StaticThings.Table.get(b);
					result += symbol.GetAddress();
				}
				format.SetAddress(result);
			}
			else if(format.m_size == 3)
			{
				String a = OPERANDToken.nextToken();
				String b = OPERANDToken.nextToken();
				int PC = StaticThings.LOCCTR;
				int BASE = StaticThings.BaseAddress;
				int result = 0;

				if(IsStringInt(a))
				{
					result += Integer.parseInt(a);
				}
				else
				{
					TAB symbol = (TAB)StaticThings.Table.get(a);
					result += symbol.GetAddress();
				}
				if(IsStringInt(b))
				{
					result -= Integer.parseInt(b);
				}
				else
				{
					TAB symbol = (TAB)StaticThings.Table.get(b);
					result -= symbol.GetAddress();
				}

				if(-2048 <= result - PC && result - PC < 2048)
				{
					format.SetAddress(result - PC);
					format.Set(10, true); // p
				}
				else if(0 <= result - BASE && result - BASE < 4096)
				{
					format.SetAddress(result - BASE);
					format.Set(9, true); // b
				}
				else
				{
					StaticThings.ErrorList.add("Format 3 - addressing - Out Of Index"  + StaticThings.BeforeLOCCTR + " in Pass2\n");
					StaticThings.Errorflag = -1; // Out of index
				}
			}
			break;
		case '=':
			break;
		case 0:
			if(format.m_size == 4)
			{
				int Target = 0;
				String operand = OPERANDToken.nextToken();
				if(IsStringInt(operand))
				{
					Target = Integer.parseInt(operand);
					format.SetAddress(Target);
				}
				else
				{
					TAB symbol = (TAB)StaticThings.Table.get(operand);
					format.SetAddress(symbol.GetAddress());
					Target = symbol.GetAddress();
				}
			}
			else if(format.m_size == 3)
			{
				int PC = StaticThings.LOCCTR;
				int BASE = StaticThings.BaseAddress;
				int Target = 0;
				String operand = OPERANDToken.nextToken();
				if(IsStringInt(operand))
				{
					Target = Integer.parseInt(operand);
				}
				else
				{
					TAB symbol = (TAB)StaticThings.Table.get(operand);
					Target = symbol.GetAddress();
				}

				if(-2048 <= (Target - PC) && (Target - PC) < 2048)
				{
					format.SetAddress(Target - PC);
					format.Set(10, true); // p
				}
				else if(0 <= Target - BASE && Target - BASE < 4096)
				{
					format.SetAddress(Target - BASE);
					format.Set(9, true); // b
				}
				else
				{
					StaticThings.ErrorList.add("format 3 normal addressing - Out of Index"  + StaticThings.BeforeLOCCTR + " in Pass2\n");
					StaticThings.Errorflag = -1; // Out of index
				}
			}
			else
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

	public void Save(JTextArea p_TransTextArea) throws IOException
	{
		save = new JFileChooser();
		filter = new FileNameExtensionFilter("Text File","txt");
		save.setCurrentDirectory(new File("user.home"));	//	Set Default Load Location
		save.setVisible(true);
		save.setAcceptAllFileFilterUsed(false);
		save.addChoosableFileFilter(filter);

		int result = save.showSaveDialog(this);

		if(result ==  JFileChooser.APPROVE_OPTION)
		{
			File selectedFile = save.getSelectedFile();
			System.out.println("Folder Address : " + selectedFile.getParent());
			System.out.println("File name : " + selectedFile.getName());
			File ErrorFile = null;
			BufferedWriter out = null;
			BufferedWriter ErrorOut = null;

			try
			{
				/*	Create Object Code	*/
				if(!selectedFile.getName().contains(".txt"))
				{
					out = new BufferedWriter(new FileWriter(selectedFile + ".txt"));
					ErrorFile = new File(selectedFile.getParent() + "/ErrorLog - " + selectedFile.getName() + ".txt");
				}					
				else
				{
					out = new BufferedWriter(new FileWriter(selectedFile));
					ErrorFile = new File(selectedFile.getParent() + "/ErrorLog - " + selectedFile.getName());
				}
					
				p_TransTextArea.write(out);
				/*	Create Error Log	*/
				ErrorOut = new BufferedWriter(new FileWriter(ErrorFile));
				for(int index = 0; index < StaticThings.ErrorList.size(); index++)
				{
					ErrorOut.write(StaticThings.ErrorList.get(index));
				}
			}
			catch(Exception e)
			{
				System.out.println("Couldn't Save File");
			}
			finally
			{
				if(out != null)
				{
					out.close();
					ErrorOut.close();
				}
			}
		}
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
		StaticThings.DirectTable.put("BASE",new OPTAB(1,0,0));  StaticThings.DirectTable.put("NOBASE",new OPTAB(2,0,0)); StaticThings.DirectTable.put("BYTE",new OPTAB(3,1,0)); StaticThings.DirectTable.put("END",new OPTAB(4,0,0));
		StaticThings.DirectTable.put("EQU",new OPTAB(5,0,0)); StaticThings.DirectTable.put("LTORG",new OPTAB(6,0,0)); StaticThings.DirectTable.put("RESB",new OPTAB(7,1,0));   StaticThings.DirectTable.put("RESW",new OPTAB(8,3,0));
		StaticThings.DirectTable.put("START",new OPTAB(9,0,0)); StaticThings.DirectTable.put("WORD",new OPTAB(10,3,0)); StaticThings.DirectTable.put("USE",new OPTAB(11,0,0));   StaticThings.DirectTable.put("CSECT",new OPTAB(12,0,0));
		StaticThings.DirectTable.put("EXTREF",new OPTAB(13,0,0)); StaticThings.DirectTable.put("EXTDEF",new OPTAB(14,0,0));

		/*	Assembler Register Table	*/
		StaticThings.RegistTable.put("A", 0); StaticThings.RegistTable.put("X", 1); StaticThings.RegistTable.put("L", 2); StaticThings.RegistTable.put("B", 3);
		StaticThings.RegistTable.put("S", 4); StaticThings.RegistTable.put("T", 5); StaticThings.RegistTable.put("F", 6);

		new MainWindow();
	}
}
