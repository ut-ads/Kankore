/**
 *
 */
package jp.co.profitcube.eacris.report.attribute.judgement.normalization;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;


/**
 * @author tozawa_h01
 *
 */
public class NormalizeUtil {


	/**
	 * �@�l�����̗̂��̂�16�i��������z��
	 */
	private final static String[] corporateNameAbbreviationArray = new String[] {
		"3231","3232","3233","3234","3235","3236","3237","3238","3239","323a","323b","323c","323d","323e","323f","3240",
		"3241","3242","3243",

		"3291","3292","3293","3294","3295","3296","3297","3298","3299","329A","329B","329C","329D","329E","329F","32A0",
		"32A1","32A2","32A3","32A4","32A5","32A6","32A7","32A8","32A9","32AA","32AB","32AC","32AD","32AE","32AF",
		"337D"
	};


	/**
	 *
	 */
	public NormalizeUtil() {
	}

    public void normalize(InputData inputData) {
    }
    
    
    
	/**
	 * �����𕶎����X�g�ɔz�u���܂��B���̎��������K���UTF-8�R�[�h���X�g�ɑ��݂���΁A�z�u����Ȃ��B
	 * @param charList   �z�u�����
	 * @param character
	 * @param hexCode
	 */
	public static void placeCharacterList(List<Character> charList, char character, String hex) {

		int code = (int)character;
		String hexCode = Integer.toHexString(code);
		
		if(hex.equals(hexCode.toUpperCase())) {
			charList.add(character);
		}

	}

    
    
    
    
    

	/**
	 * �����𕶎����X�g�ɍĔz�u���܂��B���̎��������K���UTF-8�R�[�h���X�g�ɑ��݂���΁A�z�u����Ȃ��B
	 * @param charList   �Ĕz�u�����
	 * @param character
	 * @param hexCode
	 */
	public static void placeCharacterList(List<Character> charList, char character) {

		int code = (int)character;
		String hexCode = Integer.toHexString(code);


		boolean flag = false;
		for(String hex : corporateNameAbbreviationArray){
			if(hex.equals(hexCode.toUpperCase())) {
				flag = true;
			}
		}
		if(!flag){
			charList.add(character);
		}
	}

    /**
     * char List�𕶎���ɕϊ����܂��B
     * @param  charList �Ώۂ̃��X�g
     * @return String   �ϊ���̕�����
     * @throws Exception
     */
    public static String stringToCharacterList(ArrayList<Character> charList) throws Exception{

		char[] newCharaArray = new char[charList.size()];

		for(int index = 0 ; index < charList.size() ; index++){
			newCharaArray[index] = charList.get(index);
		}
    	return String.valueOf(newCharaArray);
    }


    /**
     * char List�𕶎���ɕϊ����܂��B
     * @param  charList �Ώۂ̃��X�g
     * @return String   �ϊ���̕�����
     * @throws Exception
     */
    public static String stringToCharacterList(List<Character> charList) throws Exception{

		char[] newCharaArray = new char[charList.size()];

		for(int index = 0 ; index < charList.size() ; index++){
			newCharaArray[index] = charList.get(index);
		}
    	return String.valueOf(newCharaArray);
    }

    // ���K�����i
    /**
     * ���p�E�S�p�g����
     * @param targetStr
     * @return
     */
    public static String trimSpace(String targetStr) {
        char[] value = targetStr.toCharArray();
        int len = value.length;
        int st = 0;
        char[] val = value;

        while ((st < len) && (val[st] <= ' ' || val[st] == '�@')) {
            st++;
        }
        while ((st < len) && (val[len - 1] <= ' ' || val[len - 1] == '�@')) {
            len--;
        }

        return ((st>0) || (len<value.length)) ? targetStr.substring(st,len):targetStr;
    }


	/**
	 * ���l�ɕϊ��\�ȕ�����ϊ�����B
	 * ���l�ɕϊ��ł��Ȃ��������0�ɕϊ������
	 *
	 * @param  str    ���l�ϊ�������������
	 * @return long   �ϊ����ꂽ���l
	 */
	public static long getNumerical(String str) {
		long ret = -1;
		try {
			ret = Long.parseLong(str);
		} catch (NumberFormatException e) {
			ret = 0;
		}
		return ret;
	}


	/**
	 * �����̑S�p�Ɣ��p�𔻒肵�Ĕ��p�ł���ΐ���Ԃ�
	 *
	 * @param     targetString             ���ׂ���������
	 * @return    boolean                  ����
	 */
	public static boolean isSingleByte(String targetString) {
		// �擾���������T�C�Y�̌v�Z
		int strSize = targetString.length();
		int byteSize = targetString.getBytes().length;

		// �������ƃo�C�g�������������p
		if (strSize == byteSize) {
			return true;
		}
		return false;
	}

	/**
	 * ������̃o�C�g�����擾
	 * @param str        ���ׂ���������
	 * @param encode     �����^�C�v
	 * @return           Byte��
	 */
	public static int getTextByteLenght(String str, String encode) {
		int nLen;
		try {
			if (encode != null) {
				nLen = str.getBytes(encode).length;

			} else {
				nLen = str.getBytes().length;
			}

		} catch (UnsupportedEncodingException e) {
			nLen = str.getBytes().length;
		}
		return nLen;

	}

	/**
	 *
	 * ��������S�p�����ɕϊ����܂��B
	 *
	 * @param chinaNum      �ϊ�������������
	 * @return long         �ϊ������l
	 */
	public static String chinaNumToArabicNum(String chinaNum) {

		long retNum = 0;

		int maxSize = chinaNum.length();

		String calValue = new String(chinaNum);
		int count = 0;
		for (int i = 0; i < maxSize; i++) {

			if (getNum(chinaNum.substring(i, i + 1)) != -1) {

				calValue = chinaNum.substring(count, maxSize);
				break;
			} else {
				count++;
			}
		}

		String spritChinaNum[] = new String[3];

		ArrayList<String> list = new ArrayList<>();

		list.add("�S");
		list.add("�\");

		for (int i = 0; i < 2; i++) {
			if (calValue.indexOf(list.get(i)) > -1) {

				spritChinaNum[i] = calValue.substring(0,
						calValue.indexOf(list.get(i)) + 1);
				if (spritChinaNum[i].equals(list.get(i))) {
					spritChinaNum[i] = "��" + spritChinaNum[i];
				}
				calValue = calValue.substring(calValue.indexOf(list.get(i)) + 1, calValue.length());
			} else {
				spritChinaNum[i] = "��";
			}
		}

		spritChinaNum[2] = calValue;

		long hundredsPlaceArray[] = { 0, 0 };
		long tensPlaceArray[] = { 0, 0 };

		for (String num : spritChinaNum) {

			if (num.matches(".*" + list.get(0) + ".*")) {
				for (int i = 0; i < num.length(); i++) {
					// 100�̈�
					hundredsPlaceArray[i] = getNum(num.substring(i, i + 1));
				}
			}

			if (num.matches(".*" + list.get(1) + ".*")) {
				for (int i = 0; i < num.length(); i++) {
					// 10�̈�
					tensPlaceArray[i] = getNum(num.substring(i, i + 1));
				}
			}
		}

		long hundredsPlace = 0;
		long tensPlace = 0;
		long onesPlace = 0;

		hundredsPlace = Math.abs(hundredsPlaceArray[0] * hundredsPlaceArray[1]);
		tensPlace = Math.abs(tensPlaceArray[0] * tensPlaceArray[1]);

		for (int i = 0; i < calValue.length(); i++) {
			if (i == 0) {
				if (calValue.length() >= 3) {
					onesPlace += getNum(calValue.substring(i, i + 1)) * 100;
				} else if (calValue.length() == 2) {
					onesPlace += getNum(calValue.substring(i, i + 1)) * 10;
				} else {
					onesPlace += getNum(calValue);
				}
			} else if (i == 1) {
				if (calValue.length() >= 3) {
					onesPlace += getNum(calValue.substring(i, i + 1)) * 10;
				} else {
					onesPlace += getNum(calValue);
				}
			} else if (i == 2) {
				onesPlace += getNum(calValue);
			}
		}
		retNum = hundredsPlace + tensPlace + onesPlace;

		//���p������S�p�����ɂ���B
		String ret =  convertMultiByteNum(retNum);
		return ret;
	}


	/**
	 * �������𐔒l�ɕϊ�����B
	 * @param     targetNum     �ΏۂƂ��銿����
	 * @return    long          �ϊ��������l
	 */
	public static long getNum(String targetNum) {

		long num = 0L;
		// �������𐔒l������B
		for (int i = 0; i < targetNum.length(); i++) {
			char cBuf = targetNum.charAt(i);
			switch (cBuf) {
			case '��':
				num = 0;
				break;

			case '��':
			case '��':
				num = 1;
				break;

			case '��':
			case '��':
				num = 2;
				break;

			case '�Q':
			case '�O':
				num = 3;
				break;

			case '�l':
				num = 4;
				break;

			case '��':
				num = 5;
				break;

			case '�Z':
				num = 6;
				break;

			case '��':
				num = 7;
				break;

			case '��':
				num = 8;
				break;

			case '��':
				num = 9;
				break;

			case '�\':
				num = 10;
				break;

			case '�S':
				num = 100;
				break;

			default:
				num = -1;
				break;
			}

		}
		return num;
	}

	/**
	 * ���p������S�p�����ɂ���B
	 * @param     longTergetNum      �ΏۂƂ��鐔��
	 * @return    String             �ϊ������S�p����
	 */
	public static String convertMultiByteNum(long longTergetNum) {

		String strInNum = Long.toString(longTergetNum);
		 StringBuffer strBuf = new StringBuffer(strInNum);
	    for (int i = 0; i < strBuf.length(); i++) {
	        char cBuf = strBuf.charAt(i);
	        if ( cBuf >= '0' && cBuf <= '9' ) {
	            strBuf.setCharAt(i, (char) (cBuf - '0' + '�O'));
	          }
	        }
		return strBuf.toString();
	}

}

