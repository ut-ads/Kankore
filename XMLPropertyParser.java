/*
 * �쐬���F2005/05/15
 * 
 * (C) COPYRIGHT Open Future System Corp. 2005
 * All Rights Reserved.
 */
package jp.co.profitcube.eacris.web.bridge.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * XML�`���̃v���p�e�B�[�t�@�C������͂���B
 * <p>
 * @author daisaku
 * @version 1.0
 */
public class XMLPropertyParser {
    
    //�R���X�g
    /** property�^�O */
    private final static String TAG_PROPERTY = "property";
    /** property�^�O��name���� */
    private final static String ATTR_NAME = "name";
    /** property�^�O��value���� */
    private final static String ATTR_VALUE = "value";
    
    //������
    /** XML�h�L�������g���i�[ */
    private Document doc;
    /** key:name, value:value���i�[ */
    private Map map = new HashMap();
    
    /**
     * �w��̃t�@�C����Ǎ��݁ADOM�I�u�W�F�N�g�ɕϊ�����
     * <p>
     * @param file XML�t�@�C��
     * @throws FileNotFoundException 
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     */
    public XMLPropertyParser(File file) throws FileNotFoundException, IOException, ParserConfigurationException, SAXException{
        //�ǂݍ���
        InputStream stream = new FileInputStream(file);
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		this.doc = db.parse(stream);
    }
    
    /**
     * DOM�I�u�W�F�N�g�𑖍����Akey��value�ɕ������}�b�v�Ɋi�[����B
     * <p>
     */
    public void parse(){
		//�v���p�e�B���擾
		NodeList nList = this.doc.getElementsByTagName(TAG_PROPERTY);
		int length = nList.getLength();
		//�v���p�e�B���}�b�v�ɃZ�b�g
		for(int i = 0; i < length; i++){
		   Node propertyNode = nList.item(i);
		   String name = getPropertyName(propertyNode);
		   String value = getPropertyValue(propertyNode);
		   map.put(name, value);
		}
    }
    
    /**
     * �m�[�h����v���p�e�B�[�����擾����B
     * <p>
     * @param node �m�[�h
     * @return ���O
     */
    private String getPropertyName(Node node){
        NamedNodeMap map = node.getAttributes();
        Node pNode = map.getNamedItem(ATTR_NAME);
        return pNode.getNodeValue();
    }
    
    /**
     * �m�[�h����v���p�e�B�[�̒l���擾����B
     * <p>
     * @param node �m�[�h
     * @return �l
     */
    private String getPropertyValue(Node node){
        NamedNodeMap map = node.getAttributes();
        Node pNode = map.getNamedItem(ATTR_VALUE);
        return pNode.getNodeValue();
    }
    
    /**
     * name�ɑΉ�����value��String�Ŏ擾����B
     * <p>
     * @param name ���O
     * @return �l
     */
    public String getPropertyAsString(String name){
        Object object = map.get(name);
        if(object == null){
            throw new PropertyValueNotFoundRuntimeException("name["+name+"]");
        }
        return (String) map.get(name);
    }
    
    /**
     * name�ɑΉ�����value��int�Ŏ擾����
     * <p>
     * @param name ���O
     * @return �l
     */
    public int getPropertyAsInt(String name){
        String p = getPropertyAsString(name);
        return Integer.parseInt(p);
    }
    
// 2010/04/22 option tsuda_t ADD START long�ǉ�
    /**
     * name�ɑΉ�����value��long�Ŏ擾����
     * <p>
     * @param name ���O
     * @return �l
     */
    public long getPropertyAsLong(String name){
        String p = getPropertyAsString(name);
        return Long.parseLong(p);
    }
//  2010/04/22 option tsuda_t ADD END long�ǉ�
    
    /**
     * name�ɑΉ�����value��int�Ŏ擾����
     * <p>
     * @param name ���O
     * @return �l
     */
    public boolean getPropertyAsBoolean(String name){
        String p = getPropertyAsString(name);
        Boolean bool = new Boolean(p);
        return bool.booleanValue();
    }
    
    /**
     * key=name�Avalue=value�`���Ń}�b�v�Ɋi�[����B
     * <p>
     * @param name ���O
     * @param value �l
     */
    public void setProperty(String name, String value){
        map.put(name, value);
    }
    
    /**
     * �o�^�ς݃v���p�e�B�[�����擾����B
     * <p>
     * @return �v���p�e�B��
     */
    public String[] getNameList(){
        int size = map.size();
        String[] names = new String[size];
        Set set = map.entrySet();
        Iterator i = set.iterator();
        int index = 0;
        while(i.hasNext()){
            names[index] = (String)i.next();
            index++;
        }
        return names;
    }
    
    /**
     * ���݂̃v���p�e�B���t�@�C���ɏ����o���B
     * <p>
     * @param file �t�@�C��
     */
    public void write(File file){
        //TODO ���o���R�[�h(�ɂȎ�)
    }
}

/**
 * �{�N���X���s����O�B
 * <p>
 * @author daisaku
 * @version 1.0
 */
class PropertyValueNotFoundRuntimeException extends RuntimeException {
    public PropertyValueNotFoundRuntimeException(String message){
        super(message);
    }
}

