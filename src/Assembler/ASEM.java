package Assembler;

import java.awt.event.*;
import java.awt.*;
import java.io.*;

import javax.swing.*;
import javax.swing.GroupLayout.Alignment;
import javax.swing.filechooser.FileNameExtensionFilter;

import javafx.stage.FileChooser;
import java.util.StringTokenizer;

class TAB
{
	String name;
	int value;
}

class OPT
{
	final String m_Mnemonic;
	final int m_Opcode;

	OPT(String p_Mnemonic, int p_Opcode)
	{
		m_Mnemonic = p_Mnemonic; m_Opcode = p_Opcode;
	}

	public String GetMnemonic(){return m_Mnemonic;}
	public int GetOpcode(){return m_Opcode;}
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

	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource() == m_TransButton)
		{
			ReadFileLine();
			System.out.println("Assemble");
		}
		if(e.getSource() == m_Open)
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
	void ReadFileLine()	//	Read File Line by Line
	{
		m_TransTextArea.setText("");
		int count = 0;
		StringTokenizer Token = new StringTokenizer(m_FileTextArea.getText());
		while(Token.hasMoreTokens())
		{
			count++;
			m_TransTextArea.setText(m_TransTextArea.getText() + Token.nextToken() + "\n");
		}
			
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
		/*	Operation Table	*/
	static OPT[] OPTAB = new OPT[]
		{
				new OPT("ADD",0x18),new OPT("ADDF",0x58),new OPT("ADDR",0x90),
				new OPT("AND",0x40),new OPT("CLEAR",0xB4),new OPT("COMP",0x28),
				new OPT("COMPF",0x88),new OPT("COMPR",0xA0),new OPT("DIV",0x24),
				new OPT("DIVF",0x64),new OPT("DIVR",0x64),new OPT("FIX",0xC4),
				new OPT("FLOAT",0xC0),new OPT("HIO",0xF4),new OPT("J",0x3C),
				new OPT("JEQ",0x30),new OPT("JGT",0x34),new OPT("JLT",0x38),
				new OPT("JSUB",0x48),new OPT("LDA",0x00),new OPT("LDB",0x68),
				new OPT("LDCH",0x50),new OPT("LDF",0x70),new OPT("LDL",0x08),
				new OPT("LDS",0x6C),new OPT("LDT",0x74),new OPT("LDX",0x04),
				new OPT("LPS",0xD0),new OPT("MUL",0x20),new OPT("MULF",0x60),
				new OPT("MULR",0x98),new OPT("NORM",0xC8),new OPT("OR",0x44),
				new OPT("RD",0xD8),new OPT("RMO",0xAC),new OPT("RSUB",0x4C),
				new OPT("SHIFTL",0xA4),new OPT("SHIFTR",0xA8),new OPT("SIO",0xF0),
				new OPT("SSK",0xEC),new OPT("STA",0x0C),new OPT("STB",0x78),
				new OPT("STCH",0x54),new OPT("STF",0x80),new OPT("STI",0xD4),
				new OPT("STL",0x14),new OPT("STS",0x7C),new OPT("STSW",0xE8),
				new OPT("STT",0x84),new OPT("STX",0x10),new OPT("SUB",0x1C),
				new OPT("SUBF",0x5C),new OPT("SUBR",0x94),new OPT("SVC",0xB0),
				new OPT("TD",0xE0),new OPT("TIO",0xF8),new OPT("TIX",0x2C),
				new OPT("TIXR",0xB8),new OPT("WD",0xDC)
		};

	/*	Symbol Table	*/
	static TAB[] Table;

	int Scnt = 0;
	int Locctr = 0;
	int ENDval = 0;
	int Errorflag = 0;
	int length = 10; // Read character 10


	static public void main(String[] args) //throws IOException
	{
		new MainWindow();
	}
}
