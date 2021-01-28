/* ---------------------------------------------------------------- */
/*   e-Acris3 development project.                                  */
/*                                                                  */
/*      Utilities library belongs REPORT application.               */
/*        PROGRAM : XMLUtil.java                                    */
/*         AUTHOR : S&F)M.Sasaki                                    */
/*          SINCE : 2008/09/01                                      */
/*                                                                  */
/*   (C)2008 Profit Cube Inc. All rights reserved.                  */
/* ---------------------------------------------------------------- */

package jp.co.profitcube.eacris.report.util;

import java.util.List;
import java.util.ArrayList;

import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.NamedNodeMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * XML���샆�[�e�B���e�B�E�N���X
 *
 * @author M.Sasaki
 * @version 1.0
 */

public class XMLUtil {

    /**
     * �R���X�g���N�^�i�O���C���X�^���X�����s�j
     */
    private XMLUtil() {}

    /**
     * �w��̐eNode�֎qNode��ǉ�����B
     * @param parent �eNode
     * @param child �ǉ�����qNode
     */
    public static void appendNode(Node parent, Node child)
    {
        Document owner = parent.getOwnerDocument();
        Node addNode = owner.importNode(child, true);
        parent.appendChild(addNode);
    }

    /**
     * �w�肵���G�������gNode���w�肵���eNode���璊�o����B
     * @param parent �eNode
     * @param elementName �G�������gNode��
     * @return �G�������gNode<br>
     * �w�肵���G�������gNode�����݂��Ȃ��ꍇ��null��Ԃ��B
     */
    public static Node getNode(Node parent, String elementName)
    {
        NodeList list = parent.getChildNodes();
        Node child = null;

        for (int i=0; i<list.getLength(); i++) {
            Node node = list.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                if (node.getNodeName().equals(elementName)) {
                    child = node;
                    break;
                }
            }
        }
        return child;
    }

    /**
     * �w�肵���G�������gNode�ꗗ���w�肵���eNode���璊�o����B
     * @param parent �eNode
     * @param elementName �G�������gNode��
     * @return �G�������gNode�ꗗ<br>
     * �w�肵���G�������gNode�����݂��Ȃ��ꍇ��null��Ԃ��B
     */
    public static List getElementNodeList(Node parent, String elementName)
    {
        List elementList = null;

        NodeList list = parent.getChildNodes();

        for (int i=0; i<list.getLength(); i++) {
            Node node = list.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                if (node.getNodeName().equals(elementName)) {
                    if (elementList == null) {
                        elementList = new ArrayList();
                    }
                    elementList.add(node);
                }
            }
        }
        return elementList;
    }

    /**
     * �w�肵��Node�̒l�𒊏o����B
     * @param node Node
     * @return Node�̒l
     * �w�肵��Node���e�L�X�g�q�m�[�h�������Ȃ��ꍇ�A�󕶎���Ԃ��B
     */
    public static String getNodeValue(Node node)
    {
        NodeList list = node.getChildNodes();
        String value = "";

        Node tmp = list.item(0);
        try {
            if (tmp.getNodeType() == Node.TEXT_NODE) {
                value = tmp.getNodeValue();
            }
        } catch (Exception e) {
            value = "";
        }
        return value;
    }

    /**
     * �w�肵���eNode��Node��}������B
     * @param parent �}�����Node
     * @param ins �}��Node
     * @param ref �Q��Node ����Node�̑O�ɑ}������B
     */
    public static void insertNode(Node parent, Node ins, Node ref)
    {
        Document owner = parent.getOwnerDocument();
        Node insNode = owner.importNode(ins, true);
        parent.insertBefore(insNode, ref);
    }

    /**
     * �w�肵���eNode����Node���폜����B
     * @param parent �eNode
     * @param child �폜Node
     */
    public static void removeNode(Node parent, Node child)
    {
        parent.removeChild(child);
    }

    /**
     * �w�肵���eNode�̎qNode��u������B
     * @param parent �eNode
     * @param newNode �u�����Node
     * @param oldNode �u���O��Node
     */
    public static void replaceNode(Node parent, Node newNode, Node oldNode)
    {
        Document owner = parent.getOwnerDocument();
        Node replace = owner.importNode(newNode, true);
        parent.replaceChild(replace, oldNode);
    }

    /**
     * <pre>
     * �w�肵��Node�֒l��ݒ肷��B
     * �w�肵��Node����^�O�̏ꍇ�A�l��ݒ肵�Ȃ��B�i�������Ȃ��B�j
     * @param node Node
     * @param value �ݒ�l
     * </pre>
     */
    public static void setNodeValue(Node node, String value)
    {
        NodeList list = node.getChildNodes();

        Node tmp = list.item(0);
        try {
            if (tmp.getNodeType() == Node.TEXT_NODE) {
                tmp.setNodeValue(value);
            }
        } catch (Exception e) {
            //No ope
        }
    }

    /**
     * <pre>
     * �w�肵��Node�֒l��ݒ肷��B
     * @param node Node
     * @param value �ݒ�l
     * @param flgEmpTag ��^�O�ݒ�t���O
     * <ui>
     * <li>true  : ��^�O�̏ꍇ�A�w�肵���l��ݒ肷��B</li>
     * <li>false : ��^�O�̏ꍇ�A�w�肵���l��ݒ肵�Ȃ��B�i�������Ȃ��B�j</li>
     * </ui>
     * </pre>
     */
    public static void setNodeValue(Node node, String value, boolean flgEmpTag)
    {
        if (getNodeValue(node).equals("")) {    // ��^�O�̏ꍇ
            if (flgEmpTag) {
                // ��^�O�ݒ�t���O���ݒ肳��Ă���΁A�e�L�X�g��ݒ�
                setText(node, value);
            }
        } else {                                // ��^�O�łȂ��ꍇ
            setNodeValue(node, value);
        }
    }

    /**
     * ��^�O�Ƀe�L�X�g��ݒ肷��B
     * @param node ��^�O��Node
     * @param value �ݒ蕶����
     */
    public static void setText(Node node, String value)
    {
        Document document = node.getOwnerDocument();
        node.appendChild(document.createTextNode(value));
    }

    /**
     * �G�������g Node �ɑ�����ݒ肷��B
     * @param elementNode �G�������g Node
     * @param attrName ������
     * @param attrValue �����l
     */
    public static void setAttribute(Node elementNode, String attrName, String attrValue)
    {
        try {
            ((Element)elementNode).setAttribute(attrName, attrValue);
        } catch (Exception e) {
            // �������Ȃ�
        }
    }

    /**
     * �G�������g Node �̑����l��ύX����B
     * @param elementNode �G�������g Node
     * @param attrName ������
     * @param attrValue �����l
     */
    public static void changeAttribute(Node elementNode, String attrName, String attrValue)
    {
        try {
            Element element = (Element)elementNode;
            element.removeAttribute(attrName);          // ��������U�폜
            element.setAttribute(attrName, attrValue);  // �V�K�ǉ�
        } catch (Exception e) {
            // �������Ȃ�
        }
    }

    /**
     * <pre>
     * �G�������g�����l���擾����B
     * @param elementNode �G�������gNode
     * @param attrName ������
     * @return �G�������g�����l
     * �@�w�肵�������������݂��Ȃ��ꍇ�A�󕶎���Ԃ��B
     * </pre>
     */
    public static String getNodeAttrValue(Node elementNode, String attrName)
    {
        String value = "";
        if (elementNode.hasAttributes()) {
            NamedNodeMap map = elementNode.getAttributes();
            for (int i=0; i<map.getLength(); i++) {
                Node node = map.item(i);
                if ((node.getNodeName()).equals(attrName)) {
                    value = node.getNodeValue();
                    break;
                }
            }
        }
        return value;
    }

    /**
     * XML�t�@�C����Ǎ��݁ANode�֕ϊ�����B
     * @param xmlFilePath �N���X�p�X����̑��΃p�X
     * @return XML�t�@�C����Node
     */
    public static Node xml2Node(String xmlFilePath)
    {
        DocumentBuilder builder = null;
        Document document = null;
        Node node = null;

        if (!xmlFilePath.substring(0, 1).equals("/")) {
            xmlFilePath = "/" + xmlFilePath;
        }

        try {
            builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            document = builder.parse(new XMLUtil().getClass().getResourceAsStream(xmlFilePath));
            node = (Node)document.getDocumentElement();
        } catch (Exception e) {
            node = null;
        }

        return node;
    }

     /**
      * Node�l�̎擾(XML�t�@�C�������w��)����B
      * @param xmlFilePath XML�t�@�C���̐ݒ�t�@�C������̑��΃p�X
      * @param nodeName �eNode���炽�ǂ��Ă����qNode��
      * @return nodeName�z��̍Ō�̗v�f�Ŏw�肳�ꂽ�e�L�X�gNode�̒l
      */
     public static String getConfigValue(String xmlFilePath, String[] nodeName)
     {
        Node sg = xml2Node(xmlFilePath);
        Node node = sg;

        for (int i=0; i<nodeName.length; i++) {
            node = getNode(node, nodeName[i]);
        }
        return getNodeValue(node);
     }

    /**
     * Node�l�̎擾
     * @param config XML Node�c���[
     * @param nodeName �eNode���炽�ǂ��Ă����qNode��
     * @return nodeName�z��̍Ō�̗v�f�Ŏw�肳�ꂽ�e�L�X�gNode�̒l
     */
    public static String getConfigValue(Node config, String[] nodeName)
    {
        Node node = config;

        for (int i=0; i<nodeName.length; i++) {
            node = getNode(node, nodeName[i]);
        }
        return getNodeValue(node);
     }
    
	// 2014/01/24 SBIM �����ďƉ�E�ʐ�/�␳ �Ή� ADD START suzuki_h �ʐ�����
    /**
     * �w�肵���qNode�Q�̗v�f��List�^�ŕԂ��B
     * @param node
     * @return
     */
	public static List getConfigValueList(Node node) {
		
        NodeList gchildren  = node.getChildNodes();
		
        List<String> list = new ArrayList();
        List childList = new ArrayList();
        for(int i = 0;i<gchildren.getLength();i++){
        	Node gchild = gchildren.item(i);
        	list.add((XMLUtil.getNodeValue(gchild)));
        }
        if(list!= null){
        	for(int i=0;i<list.size();i++){
        		if(list.get(i).trim().length() != 0){
        			childList.add(list.get(i));
        		}
        	}
        }

		return childList;
	}
	// 2014/01/24 SBIM �����ďƉ�E�ʐ�/�␳ �Ή� ADD END   suzuki_h �ʐ�����
	    
}

