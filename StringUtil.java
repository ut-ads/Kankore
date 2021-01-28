package jp.console.main;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class StringUtil {

	/**
	 * �W���\������
	 */
	private static int DEFAULT_DIGIT = 3;

	/**
	 * ���p�X�y�[�X
	 */
	private static String SINGLE_BYTE_SPACE = " ";

	/**
	 * �S�p�X�y�[�X
	 */
	private static String MULTI_BYTE_SPACE = "�@";
	
	
	

    /**
     * RPAD���s���܂��B ������[str]�̉E�Ɏw�肵��������[addStr]��[len]�� �������܂ő}�����܂��B
     * 
     * @param str
     *            �Ώە�����
     * @param len
     *            ��[����܂ł̌����iRPAD���s������̃T�C�Y���w�肵�܂��B�j
     * @param addStr
     *            �}�����镶����
     * @return �ϊ���̕�����B
     */
    public static String rpad(String str, int len, String addStr) {
        return fillString(str, "R", len, addStr);
    }

    /**
     * ������[str]�ɑ΂��āA��[���镶����[addStr]�� [position]�̈ʒu��[len]�ɖ������܂ő}�����܂��B
     * 
     * ��[str]��null��󃊃e�����̏ꍇ�ł�[addStr]�� [len]�ɖ������܂ő}���������ʂ�Ԃ��܂��B
     * 
     * @param str
     *            �Ώە�����
     * @param position
     *            �O�ɑ}�� �� L or l ��ɑ}�� �� R or r
     * @param len
     *            ��[����܂ł̌���
     * @param addStr
     *            �}�����镶����
     * @return �ϊ���̕�����B
     */
    public static String fillString(String str, String position, int len,
            String addStr) {
        if (addStr == null || addStr.length() == 0) {
            throw new IllegalArgumentException("�}�����镶����̒l���s���ł��BaddStr="
                    + addStr);
        }
        if (str == null) {
            str = "";
        }
        StringBuffer buffer = new StringBuffer(str);
        while (len > buffer.length()) {
            if (position.equalsIgnoreCase("l")) {
                int sum = buffer.length() + addStr.length();
                if (sum > len) {
                    addStr = addStr.substring(0, addStr.length() - (sum - len));
                    buffer.insert(0, addStr);
                } else {
                    buffer.insert(0, addStr);
                }
            } else {
                buffer.append(addStr);
            }
        }
        if (buffer.length() == len) {
            return buffer.toString();
        }
        return buffer.toString().substring(0, len);
    }

	
	
	
	
	

	/**
	 * targetInteger���R�������̏ꍇ�A�R���𖞂��������A'0'��擪�ɐς߂�
	 *
	 * @param targetInteger
	 *            �␳�Ώۂ̐��l
	 * @return�@�␳��� ������
	 */
	public static String digitNumber(int targetInteger) {
		return digitNumber(targetInteger, DEFAULT_DIGIT);
	}

	/**
	 * targetInteger��len�����̏ꍇ�Alen�𖞂��������A'0'��擪�ɐς߂�
	 *
	 * @param targetInteger
	 *            �␳�Ώۂ̐��l
	 * @param len
	 *            �␳�l
	 * @return�@�␳��� ������
	 */
	public static String digitNumber(int targetInteger, int len) {
		// ���͂����␳��̌������W��������菬�����ꍇ�͕�����ɕϊ����Ė߂�
		String diffLen = Integer.toString(targetInteger);
		if (diffLen.length() >= len) {
			return diffLen;
		} else {
			// ������
			int compenstionDigit = len - diffLen.length();
			StringBuilder ret = new StringBuilder();
			// �␳�̎��{
			for (int loop = 0; loop < compenstionDigit; loop++) {
				ret.append("0");
			}
			ret.append(targetInteger);
			return ret.toString();
		}
	}

	/**
	 * �w�肳�ꂽ�������ɂȂ�悤�ɃX�y�[�X���U
	 *
	 * @param targetString
	 *            ��U�Ώۂ̕�����
	 * @param maxSize
	 *            �ő包��
	 * @return�@��U��� ������
	 */
	public static String setCoverSpace(String targetString, int maxSize,
			boolean flagTop) {

		if (flagTop) {
			// �O�l���܂�
			return leftSpaceJustified(targetString, maxSize);
		} else {
			// ��l���܂�
			return rightSpaceJustified(targetString, maxSize);
		}
	}

	/**
	 * �Ώە����̍����ɃX�y�[�X���K�萔��������
	 *
	 * @param targetString
	 *            ��U�Ώۂ̕�����
	 * @param maxSize
	 *            �ő包��
	 * @return ��U��̕�����
	 */
	private static String leftSpaceJustified(String targetString, int maxSize) {
		// �擾���������T�C�Y�̌v�Z
		int strSize = targetString.length();
		// ������
		StringBuilder buf = new StringBuilder();
		int compenstionDigit = maxSize - strSize;

		//
		for (int loop = 0; loop < compenstionDigit; loop++) {
			setSpace(targetString, buf);
		}
		buf.append(targetString);
		return buf.toString();
	}

	private static void setSpace(String targetString, StringBuilder buf) {
		if (isSingleByte(targetString)) {
			buf.append(SINGLE_BYTE_SPACE);
		} else {
			buf.append(MULTI_BYTE_SPACE);
		}
	}

	private static String rightSpaceJustified(String targetString, int maxSize) {
		// �擾���������T�C�Y�̌v�Z
		int strSize = targetString.length();
		StringBuilder buf = new StringBuilder(targetString);
		if (maxSize > strSize) {
			int count = maxSize - strSize;
			for (int i = 0; i < count; i++) {
				setSpace(targetString, buf);
			}
		}
		return buf.toString();
	}

	/**
	 * �����̑S�p�Ɣ��p�𔻒肵�Ĕ��p�ł���ΐ���Ԃ�
	 *
	 * @param targetString
	 * @return�@boolean
	 */
	private static boolean isSingleByte(String targetString) {
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
	 *
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
	 * ���l�ɕϊ��\�ȕ�����ϊ�����B ���l�ɕϊ��ł��Ȃ��������0�ɕϊ������
	 *
	 * @param str
	 *            ���l�ϊ�������������
	 * @return long �ϊ����ꂽ����
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
	 *
	 * �������𔼊p�����ɕϊ����܂��B
	 *
	 * @param chinaNum
	 *            �ϊ�������������
	 * @return long �ϊ������l
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
					// hundredsPlaceArray.add(getNum(num.substring(i,i+1)));
					hundredsPlaceArray[i] = getNum(num.substring(i, i + 1));

				}
			}

			if (num.matches(".*" + list.get(1) + ".*")) {
				for (int i = 0; i < num.length(); i++) {
					// tensPlaceArray.add(getNum(num.substring(i,i+1)));
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
	 * ���p������S�p�����ɂ���B
	 * @param inNum
	 * @return
	 */
	private static String convertMultiByteNum(long inNum) {

		String strInNum = Long.toString(inNum);
		 StringBuffer sb = new StringBuffer(strInNum);
	    for (int i = 0; i < sb.length(); i++) {
	        char c = sb.charAt(i);
	        if (c >= '0' && c <= '9') {
	            sb.setCharAt(i, (char) (c - '0' + '�O'));
	          }
	        }

		return sb.toString();
	}

	/**
	 * �������𔼊p�����ɂ���ڍ�
	 * @param strNum
	 * @return
	 */
	private static long getNum(String strNum) {

		long num = 0L;
		// �������𐔒l������B
		for (int i = 0; i < strNum.length(); i++) {
			switch (strNum.charAt(i)) {
			case '��':
			case '�Z':
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
	 * �o�C�g�z���16�i���̕�����ɕϊ�����B
	 *
	 * @param bytes �o�C�g�z��
	 * @return 16�i���̕�����
	 */
	public static String asHex(byte bytes[]) {
		// �o�C�g�z��̂Q�{�̒����̕�����o�b�t�@�𐶐��B
		StringBuffer strbuf = new StringBuffer(bytes.length * 2);

		// �o�C�g�z��̗v�f�����A�������J��Ԃ��B
		for (int index = 0; index < bytes.length; index++) {
			// �o�C�g�l�����R���ɕϊ��B
			int bt = bytes[index] & 0xff;

			// �o�C�g�l��0x10�ȉ�������B
			if (bt < 0x10) {
				// 0x10�ȉ��̏ꍇ�A������o�b�t�@��0��ǉ��B
				strbuf.append("0");
			}

			// �o�C�g�l��16�i���̕�����ɕϊ����āA������o�b�t�@�ɒǉ��B
			strbuf.append(Integer.toHexString(bt));
		}

		/// 16�i���̕������Ԃ��B
		return strbuf.toString();
	}

	/**
	 * 16�i���̕�������o�C�g�z��ɕϊ�����B
	 *
	 * @param hex 16�i���̕�����
	 * @return �o�C�g�z��
	 */
	public static byte[] asByteArray(String hex) {
		// �����񒷂�1/2�̒����̃o�C�g�z��𐶐��B
		byte[] bytes = new byte[hex.length() / 2];

		// �o�C�g�z��̗v�f�����A�������J��Ԃ��B
		for (int index = 0; index < bytes.length; index++) {
			// 16�i����������o�C�g�ɕϊ����Ĕz��Ɋi�[�B
			bytes[index] = (byte) Integer.parseInt( hex.substring(index * 2, (index + 1) * 2), 16);
		}

		// �o�C�g�z���Ԃ��B
		return bytes;
	}

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
}

