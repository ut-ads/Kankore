/**
 * 
 */
package co.jp.console.sample;

/**
 * @author h_tozawa
 *
 */
public class StringSPlits {

	/**
	 * 
	 */
	public StringSPlits() {
		// TODO �����������ꂽ�R���X�g���N�^�[�E�X�^�u
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		String str = "�����S,|/�p�C�i�b�v��,|/������,|/�Ԃǂ�";
		String[] fruit = str.split("/", 3);
		//String[] fruit = str.split("\\|");
		System.out.println("Size: " + fruit.length);
		
		int count = 0;
		for(String item : fruit){
			
			System.out.println(count + "�Ԗڂ̗v�f = :" + item);
			count++;
			
		}

		
		
		String xmlstr = "<?xml version=1.0 encoding="+"UTF-8" + "standalone="+"no" + "?>" + "<HEAD></HEAD>" + "<BODY></BODY>"
		
+"<?xml version=1.0 encoding="+"UTF-8" + "standalone="+"no" + "?>" + "<BHEAD></BHEAD>" + "<BBODY></BBODY>" + "<BFOOTER></BSFOOTER>";
				
				;
		
		
		
		String[] xmlstrArray = xmlstr.split("\\<\\?xml");
		
		
		System.out.println("Size: " + xmlstrArray.length);
		// �����̕���
		xmlstrArray[0] = "<?xml";
		
		
		
//		count = 0;
//		for(String item : xmlstrArray){
//			
//			System.out.println(count + "�Ԗڂ̗v�f = :" + item);
//			if(count > 0){
//				System.out.println(count + "�����̌��� = :" + (xmlstrArray[0] +item));
//			}
//			count++;
//			
//		}
		
		for(int i = 1 ; i < xmlstrArray.length ; i++){
			
			System.out.println(i + "�����̌��� = :" + (xmlstrArray[0] +xmlstrArray[i]));
		}
		
		

	}

}

