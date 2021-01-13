package gui;

import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JFrame;



public class MainFrame extends JFrame{
	public static MainFrame instance = new MainFrame();
    
    private MainFrame(){
        this.setSize(1000, 710);
        this.setContentPane(MainPanel.instance);
        this.setLocationRelativeTo(null);// ��������Ļ����
		//����һ������ͼ��
		this.setIconImage(new ImageIcon(new File("C:\\Users\\szzs\\Pictures\\Camera Roll\\paidaxing.jpg").getAbsolutePath()).getImage());
		this.setTitle("Secure Transport by XiDieccc");
		//���ɸı��С
        this.setResizable(false);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        
    }
     
    public static void main(String[] args) {
        instance.setVisible(true);
    }
}
