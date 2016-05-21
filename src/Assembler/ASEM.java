package Assembler;

import java.awt.event.*;
import java.awt.*;
import java.io.*;

import javax.swing.*;
import javax.swing.GroupLayout.Alignment;
import javax.swing.filechooser.FileNameExtensionFilter;

import javafx.stage.FileChooser;
import java.util.*;

class TAB
{
	String name;
	int value;
}


class StaticThigs
{
	/*	Operation Table	*/
	public static Hashtable OpTable = new Hashtable();

	/*	Symbol Table	*/
	public static Hashtable Table = new Hashtable();


	public static int LOCCTR; 	// Address Counter
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
		m_MainFrame.setResizable(true);
		m_MainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	public void actionPerformed(ActionEvent e) //	input manager
	{
		if(e.getSource() == m_TransButton)	//	Assemble Button
		{
			//ReadFileLine();
			System.out.println("Assemble");
			System.out.println(StaticThigs.OpTable.get(0));
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
	void ReadFileLine()	//	Read File Line
	{
		/*	Variable to read line	`*/
		int lineCount = m_FileTextArea.getLineCount();
		String line = new String();

		/*	Varialbe for Pass1	*/
		m_TransTextArea.setText("");
		String Label = new String();
		String OPCODE= new String();
		String PARAMETER = new String();

		try
		{
			for(int i = 0; i < lineCount; i++)
			{
				int start = m_FileTextArea.getLineStartOffset(i);

				line = m_FileTextArea.getText(start, start + 35);

				StringTokenizer Token = new StringTokenizer(line);
				
				while(Token.hasMoreTokens())
				{
					if(Token.countTokens() == 3)
					{
						Label = Token.nextToken();
					}
					OPCODE = Token.nextToken();
					PARAMETER = Token.nextToken();
					
					if(OPCODE == "START")
					{
						StaticThigs.LOCCTR = PARAMETER;
					}

				}
			}

		}
		catch(Exception e)
		{

		}
	}

	void Pass1()
	{
		StaticThigs.LOCCTR = 0;
		StaticThigs.StartAddress = 0;
		StaticThigs.BaseAddress = 0;
		StaticThigs.Errorflag = 0;
	}
}

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
		/*	Operation Table	*/
		StaticThigs.OpTable.put("ADD",0x18);StaticThigs.OpTable.put("ADDF",0x58);StaticThigs.OpTable.put("ADDR",0x90);
		StaticThigs.OpTable.put("AND",0x40);StaticThigs.OpTable.put("CLEAR",0xB4);StaticThigs.OpTable.put("COMP",0x28);
		StaticThigs.OpTable.put("COMPF",0x88);StaticThigs.OpTable.put("COMPR",0xA0);StaticThigs.OpTable.put("DIV",0x24);
		StaticThigs.OpTable.put("DIVF",0x64);StaticThigs.OpTable.put("DIVR",0x64);StaticThigs.OpTable.put("FIX",0xC4);
		StaticThigs.OpTable.put("FLOAT",0xC0);StaticThigs.OpTable.put("HIO",0xF4);StaticThigs.OpTable.put("J",0x3C);
		StaticThigs.OpTable.put("JEQ",0x30);StaticThigs.OpTable.put("JGT",0x34);StaticThigs.OpTable.put("JLT",0x38);
		StaticThigs.OpTable.put("JSUB",0x48);StaticThigs.OpTable.put("LDA",0x00);StaticThigs.OpTable.put("LDB",0x68);
		StaticThigs.OpTable.put("LDCH",0x50);StaticThigs.OpTable.put("LDF",0x70);StaticThigs.OpTable.put("LDL",0x08);
		StaticThigs.OpTable.put("LDS",0x6C);StaticThigs.OpTable.put("LDT",0x74);StaticThigs.OpTable.put("LDX",0x04);
		StaticThigs.OpTable.put("LPS",0xD0);StaticThigs.OpTable.put("MUL",0x20);StaticThigs.OpTable.put("MULF",0x60);
		StaticThigs.OpTable.put("MULR",0x98);StaticThigs.OpTable.put("NORM",0xC8);StaticThigs.OpTable.put("OR",0x44);
		StaticThigs.OpTable.put("RD",0xD8);StaticThigs.OpTable.put("RMO",0xAC);StaticThigs.OpTable.put("RSUB",0x4C);
		StaticThigs.OpTable.put("SHIFTL",0xA4);StaticThigs.OpTable.put("SHIFTR",0xA8);StaticThigs.OpTable.put("SIO",0xF0);
		StaticThigs.OpTable.put("SSK",0xEC);StaticThigs.OpTable.put("STA",0x0C);StaticThigs.OpTable.put("STB",0x78);
		StaticThigs.OpTable.put("STCH",0x54);StaticThigs.OpTable.put("STF",0x80);StaticThigs.OpTable.put("STI",0xD4);
		StaticThigs.OpTable.put("STL",0x14);StaticThigs.OpTable.put("STS",0x7C);StaticThigs.OpTable.put("STSW",0xE8);
		StaticThigs.OpTable.put("STT",0x84);StaticThigs.OpTable.put("STX",0x10);StaticThigs.OpTable.put("SUB",0x1C);
		StaticThigs.OpTable.put("SUBF",0x5C);StaticThigs.OpTable.put("SUBR",0x94);StaticThigs.OpTable.put("SVC",0xB0);
		StaticThigs.OpTable.put("TD",0xE0);StaticThigs.OpTable.put("TIO",0xF8);StaticThigs.OpTable.put("TIX",0x2C);
		StaticThigs.OpTable.put("TIXR",0xB8);StaticThigs.OpTable.put("WD",0xDC);

		new MainWindow();
	}
}
