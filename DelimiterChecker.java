/**
 *
 */
package jp.util;

import java.util.ArrayList;
import java.util.List;

/**
 * @author PC User
 *
 */
public class DelimiterChecker {

	/**
	 *
	 */
	public DelimiterChecker() {
		// TODO �����������ꂽ�R���X�g���N�^�[�E�X�^�u
	}

	/**
	 * @throws Exception
	 */
	public List<String> delimiterCheck(String strData) throws Exception {
		String delimiter = "<\\?xml";


		List<String> retList = new ArrayList<>();

		String[] strArray = strData.split(delimiter);

		for(int index = 0 ; index < strArray.length ; index++){

			String str = strArray[index];
			if(index == 0) {

				str = str.replaceAll("\n","");
				str = str.replaceAll("\t","");
				str = str.replaceAll("\\s","");
				str = str.replaceAll("�@","");


				if(str.length() > 0){

					System.out.println("delimiter ERR:  " + str);
					return retList;
				}
			} else {
				retList.add("<?xml"+str);
			}
		}
		return retList;
	}


}

