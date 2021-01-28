/**
 * 
 */
package jp.console.main;

/**
 * @author h_tozawa
 *
 */
public class StringUtils {
	
	
	
	
	private String strData = null;
	
	
	

	/**
	 * 
	 */
	public StringUtils() {
		// TODO �����������ꂽ�R���X�g���N�^�[�E�X�^�u
	}
	
	public StringUtils(String msg) {
		this.setStrData(msg);
	}
	
	public static char[] getOldDictionary(String _strData) {


		String strData = new String(_strData);

		char[] charData = strData.toCharArray();
		
		char[] newCharaData = new char[charData.length];
		
		int j =0;
		for(int i = charData.length -1 ; i > -1 ; i-- ){
			newCharaData[j] = charData[i];
			j++;
		}
		return newCharaData;
	}

	
	public static String getOldDictionaryToString(String _strData) {

		return String.valueOf(getOldDictionary(_strData));
	}

	public String getStrData() {
		return strData;
	}

	public void setStrData(String strData) {
		this.strData = strData;
	}
	

    /**
     * LPAD���s���܂��B ������[str]�̍��Ɏw�肵��������[addStr]��[len]�� �������܂ő}�����܂��B
     * 
     * @param str
     *            �Ώە�����
     * @param len
     *            ��[����܂ł̌����iLPAD���s������̃T�C�Y���w�肵�܂��B�j
     * @param addStr
     *            �}�����镶����
     * @return �ϊ���̕�����B
     */
    public static String lpad(String str, int len, String addStr) {
        return fillString(str, "L", len, addStr);
    }

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

	
	
	
}

