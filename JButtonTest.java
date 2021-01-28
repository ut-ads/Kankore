/**
 * 
 */
package co.jp.any.swing.component;

import javax.swing.JButton;
import javax.swing.JFrame;

import co.jp.any.swing.excecute.SwingExec;

/**
 * @author tozawa_h01
 *
 */
public class JButtonTest {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static final int DEFAULT_BUTTON_XPOSI_SIZE = 150;
	
	private static final int DEFAULT_BUTTON_YPOSI_SIZE = 30;
	
	private static final int DEFAULT_SPACE_SIZE = 10;
	
	private static final int ACTIVE_BAR_SIZE = 33;
	

	private static Object connectButton[] = {new JButton("����"),new JButton("��"),new JButton("����")};
	
	private static JFrame frame = new JFrame("JButtonTest");

	private static final SwingExec swingExecute = new SwingExec();
	
	public JButtonTest() {
		components();
	}

	private void components() {
		// �\���J�n�ʒu
		int x_Position = 10;
		int y_Position = 10;
		
		int index = 0;
		// �{�^���\���ʒu
		for(Object bttons : connectButton){
			if (bttons instanceof JButton) {
				JButton btton = (JButton) bttons;
				btton.setBounds(x_Position,y_Position,DEFAULT_BUTTON_XPOSI_SIZE, DEFAULT_BUTTON_YPOSI_SIZE);
				x_Position = x_Position + DEFAULT_BUTTON_XPOSI_SIZE + DEFAULT_SPACE_SIZE; 

				// ���X�i�[�̃Z�b�g
				setActionListeners(index, btton);
				index++;
			}
		}

		// �t���[���Ƀ{�^����ǉ�
		for(Object bttons : connectButton){
			if (bttons instanceof JButton) {
				JButton btton = (JButton) bttons;
				frame.getContentPane().add(btton);
			}
		}
        
        frame.getContentPane().setLayout(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.addWindowListener(swingExecute.new MyWindowAdapter());
		frame.setTitle("JButtonTest");
		
		
		int frameXSize = x_Position + DEFAULT_SPACE_SIZE;
		int frameYSize = y_Position + DEFAULT_BUTTON_YPOSI_SIZE + DEFAULT_SPACE_SIZE + ACTIVE_BAR_SIZE;
		
		frame.setSize( frameXSize, frameYSize);
		frame.setVisible(true);
		
	}

	/**
	 * @param index
	 * @param btton
	 */
	private void setActionListeners(int index, JButton btton) {
		// ���X�i�[
		switch(index){
			case 0:
				btton.addActionListener(swingExecute.new bottanOkActionHandler());
				break;
				
			case 1:
				btton.addActionListener(swingExecute.new bottanNgActionHandler());
				break;
				
			case 2:
				btton.addActionListener(swingExecute.new bottanEndActionHandler());
				break;
				
			default:
				System.err.println(index);
				break;
		}
	}

	public static int getFrameWidth(){
		return frame.getWidth();
	}
	
	public static int getFrameHeight(){
		return frame.getHeight();
	}
}

