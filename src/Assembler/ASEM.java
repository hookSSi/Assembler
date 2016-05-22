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

class StaticThigs
{
	/*	Operation Table	*/
	public static Hashtable OpTable = new Hashtable();
	/*	Directive Table	*/
	public static Hashtable DirectTable = new Hashtable();
	/*	Symbol Table	*/
	public static Hashtable Table = new Hashtable();


	public static int LOCCTR; 	// Address Counter
	public static String ProgramName;
	public static int StartAddress; // Start Address
	public static int BaseAddress; // Base Address
	public static int Errorflag;

	public void Clear()
	{
		Table = null;
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
		m_FileTextArea.setColumns(30);
		m_FileTextArea.setLineWrap(true);
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
			
			Pass1();
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
		StaticThigs.LOCCTR = 0;
		StaticThigs.StartAddress = 0;
		StaticThigs.BaseAddress = 0;
		StaticThigs.Errorflag = 0;

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
								
				
				
				if(StaticThigs.Table.get("START") == null)
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
							Integer stringtoint = Integer.parseInt(OPERAND);
							StaticThigs.LOCCTR = stringtoint;
							StaticThigs.StartAddress = stringtoint;
							StaticThigs.ProgramName = LABEL;
							StaticThigs.Table.put("START", LABEL);
						}
						else
						{
							StaticThigs.Errorflag = -1;	// Parameter Error
						}
					}
				}
				else if(StaticThigs.Table.get("START") != null)
				{
					if(Token.countTokens() == 3)
					{
						LABEL = Token.nextToken();
					}
					if(Token.hasMoreTokens())
						OPCODE = Token.nextToken();
					if(Token.hasMoreTokens())
						OPERAND = Token.nextToken();
					
					if(OPCODE != null || !OPCODE.equals("."))
					{
						if(OPCODE.equals("END"))
						{
							return;
						}
						else
						{							
							if(LABEL != null && StaticThigs.Table.containsKey(LABEL))
							{
								StaticThigs.Errorflag = -1;
							}
							else if(LABEL != null && !StaticThigs.Table.containsKey(LABEL))
							{
								StaticThigs.Table.put(LABEL, StaticThigs.LOCCTR);	 // input LABEL to Symbol Table
								System.out.print(LABEL + "\t");
								System.out.format("%X%n", StaticThigs.Table.get(LABEL));
							}
							/*	Operation code Handle	*/
							if(OPCODE.charAt(0) == '+' && StaticThigs.OpTable.containsKey(OPCODE.substring(1))) // Format 4
							{
								OPTAB temp = (OPTAB)StaticThigs.OpTable.get(OPCODE.substring(1));
								StaticThigs.LOCCTR += temp.oplength + 1;
							}
							else if(StaticThigs.OpTable.containsKey(OPCODE))	// Format 3
							{
								OPTAB temp = (OPTAB)StaticThigs.OpTable.get(OPCODE);
								StaticThigs.LOCCTR += temp.oplength;
							}
							/* Directive Handle	*/
							else if(StaticThigs.DirectTable.containsKey(OPCODE))
							{
								if(OPCODE.equals("RESW") || OPCODE.equals("RESB"))
								{
									int n = 0;
									OPERAND = OPERAND.trim();
									
									if(IsStringInt(OPERAND))
									{
										n = Integer.parseInt(OPERAND);			
									}
									else
									{
										StaticThigs.Errorflag = -1;
									}
									if(OPCODE.equals("RESW"))
									{
										StaticThigs.LOCCTR += n * 3;
									}
									else
									{
										StaticThigs.LOCCTR += n;
									}
								}
								else if(OPCODE.equals("WORD") || OPCODE.equals("BYTE"))
								{
									if(OPCODE.equals("WORD"))
									{
										StaticThigs.LOCCTR += 3;
									}
									else
									{
										if(OPERAND.charAt(0) == 'C')
										{		
											StaticThigs.LOCCTR += 1 * (OPERAND.trim().length()-3);
										}
										else if(OPERAND.charAt(0) == 'X')
										{
											StaticThigs.LOCCTR += 1 * (OPERAND.trim().length()-3)/2;
										}
										else
										{
											StaticThigs.Errorflag = -1;
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

	boolean IsStringInt(String s)
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
	
	String GetPartOfString(String str)
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

/*
class TextFrame extends JFrame implements ActionListener
{
	JButton b;

	TextFrame()
	{
		b = new JButton();
		b.setSize(250,250);
		b.addActionListener(this);

		this.setVisible(true);
		this.setLayeredPane(null);
		this.setSize(300, 300);
	}

	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource() == b)
		{
			return new Color();
		}
	}
}*/

/*	Main	*/
public class ASEM
{
	static public void main(String[] args) //throws IOException
	{
		/*	Assembler operations	*/
		StaticThigs.OpTable.put("ADD",new OPTAB(0x18,3,0));StaticThigs.OpTable.put("ADDF",new OPTAB(0x58,3,1));StaticThigs.OpTable.put("ADDR",new OPTAB(0x90,2,1));
		StaticThigs.OpTable.put("AND",new OPTAB(0x40,3,0));StaticThigs.OpTable.put("CLEAR",new OPTAB(0xB4,2,1));StaticThigs.OpTable.put("COMP",new OPTAB(0x28,3,0));
		StaticThigs.OpTable.put("COMPF",new OPTAB(0x88,3,1));StaticThigs.OpTable.put("COMPR",new OPTAB(0xA0,2,1));StaticThigs.OpTable.put("DIV",new OPTAB(0x24,3,0));
		StaticThigs.OpTable.put("DIVF",new OPTAB(0x64,3,1));StaticThigs.OpTable.put("DIVR",new OPTAB(0x64,2,1));StaticThigs.OpTable.put("FIX",new OPTAB(0xC4,1,1));
		StaticThigs.OpTable.put("FLOAT",new OPTAB(0xC0,1,1));StaticThigs.OpTable.put("HIO",new OPTAB(0xF4,1,1));StaticThigs.OpTable.put("J",new OPTAB(0x3C,3,0));
		StaticThigs.OpTable.put("JEQ",new OPTAB(0x30,3,0));StaticThigs.OpTable.put("JGT",new OPTAB(0x34,3,0));StaticThigs.OpTable.put("JLT",new OPTAB(0x38,3,0));
		StaticThigs.OpTable.put("JSUB",new OPTAB(0x48,3,0));StaticThigs.OpTable.put("LDA",new OPTAB(0x00,3,0));StaticThigs.OpTable.put("LDB",new OPTAB(0x68,3,1));
		StaticThigs.OpTable.put("LDCH",new OPTAB(0x50,3,0));StaticThigs.OpTable.put("LDF",new OPTAB(0x70,3,1));StaticThigs.OpTable.put("LDL",new OPTAB(0x08,3,0));
		StaticThigs.OpTable.put("LDS",new OPTAB(0x6C,3,1));StaticThigs.OpTable.put("LDT",new OPTAB(0x74,3,1));StaticThigs.OpTable.put("LDX",new OPTAB(0x04,3,0));
		StaticThigs.OpTable.put("LPS",new OPTAB(0xD0,3,1));StaticThigs.OpTable.put("MUL",new OPTAB(0x20,3,0));StaticThigs.OpTable.put("MULF",new OPTAB(0x60,3,1));
		StaticThigs.OpTable.put("MULR",new OPTAB(0x98,2,1));StaticThigs.OpTable.put("NORM",new OPTAB(0xC8,1,1));StaticThigs.OpTable.put("OR",new OPTAB(0x44,3,0));
		StaticThigs.OpTable.put("RD",new OPTAB(0xD8,3,0));StaticThigs.OpTable.put("RMO",new OPTAB(0xAC,2,1));StaticThigs.OpTable.put("RSUB",new OPTAB(0x4C,3,0));
		StaticThigs.OpTable.put("SHIFTL",new OPTAB(0xA4,2,1));StaticThigs.OpTable.put("SHIFTR",new OPTAB(0xA8,2,1));StaticThigs.OpTable.put("SIO",new OPTAB(0xF0,1,1));
		StaticThigs.OpTable.put("SSK",new OPTAB(0xEC,3,1));StaticThigs.OpTable.put("STA",new OPTAB(0x0C,3,0));StaticThigs.OpTable.put("STB",new OPTAB(0x78,3,1));
		StaticThigs.OpTable.put("STCH",new OPTAB(0x54,3,0));StaticThigs.OpTable.put("STF",new OPTAB(0x80,3,1));StaticThigs.OpTable.put("STI",new OPTAB(0xD4,3,1));
		StaticThigs.OpTable.put("STL",new OPTAB(0x14,3,0));StaticThigs.OpTable.put("STS",new OPTAB(0x7C,3,1));StaticThigs.OpTable.put("STSW",new OPTAB(0xE8,3,0));
		StaticThigs.OpTable.put("STT",new OPTAB(0x84,3,1));StaticThigs.OpTable.put("STX",new OPTAB(0x10,3,0));StaticThigs.OpTable.put("SUB",new OPTAB(0x1C,3,0));
		StaticThigs.OpTable.put("SUBF",new OPTAB(0x5C,3,1));StaticThigs.OpTable.put("SUBR",new OPTAB(0x94,2,1));StaticThigs.OpTable.put("SVC",new OPTAB(0xB0,2,1));
		StaticThigs.OpTable.put("TD",new OPTAB(0xE0,3,0));StaticThigs.OpTable.put("TIO",new OPTAB(0xF8,1,1));StaticThigs.OpTable.put("TIX",new OPTAB(0x2C,3,0));
		StaticThigs.OpTable.put("TIXR",new OPTAB(0xB8,2,1));StaticThigs.OpTable.put("WD",new OPTAB(0xDC,3,0));

		/*	Assembler directives	*/
		StaticThigs.DirectTable.put("BASE",new OPTAB(1,0,0));  StaticThigs.DirectTable.put("NOBASE",new OPTAB(2,0,0)); StaticThigs.DirectTable.put("BYTE",new OPTAB(3,0,0)); StaticThigs.DirectTable.put("END",new OPTAB(4,0,0));
		StaticThigs.DirectTable.put("EQU",new OPTAB(5,0,0)); StaticThigs.DirectTable.put("LTORG",new OPTAB(6,0,0)); StaticThigs.DirectTable.put("RESB",new OPTAB(7,0,0));   StaticThigs.DirectTable.put("RESW",new OPTAB(8,0,0));
		StaticThigs.DirectTable.put("START",new OPTAB(9,0,0)); StaticThigs.DirectTable.put("WORD",new OPTAB(10,0,0)); StaticThigs.DirectTable.put("USE",new OPTAB(11,0,0));   StaticThigs.DirectTable.put("CSECT",new OPTAB(12,0,0));
		StaticThigs.DirectTable.put("EXTREF",new OPTAB(13,0,0)); StaticThigs.DirectTable.put("EXTDEF",new OPTAB(14,0,0));

		new MainWindow();
	}
}
