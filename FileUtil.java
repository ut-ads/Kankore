/*
 * �쐬���F2005/05/24
 * 
 * (C) COPYRIGHT Open Future System Corp. 2005
 * All Rights Reserved.
 */
package jp.co.profitcube.eacris.web.bridge.util;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * �R�����g
 * <p>
 * @author daisaku
 * @version 1.0
 */
public class FileUtil {
    
    /**
     * �ǂݍ��݉\�ȃt�@�C�����ۂ��B
     * <p>
     * @param file �Ώۃt�@�C��
     * @return true:�ǂݍ��݉\�Afalse:����ȊO
     */
    public static boolean isReadFile(File file){
        //�t�@�C�������݂��邩�ۂ�
        if(file.exists()){
            //�t�@�C�����t�@�C�����ۂ�
            if(file.isFile()){
                //�t�@�C�����Ǎ��݉\���ۂ�
                if(file.canRead()){
                   return true; 
                }
            }
        }
        return false;
    }
    
    /**
     * �t�@�C������w��̃G���R�[�h�����ŕ������Ǎ��݁A
     * ���s�P�ʂŊi�[����String�z���Ԃ��B
     * ���s�R�[�h��OS�ɏ����������̂𗘗p����B
     * <p>
     * @param file �t�@�C��
     * @param encode �G���R�[�h����
     * @return �t�@�C������Ǎ��񂾕�����̔z��
     */
    public static String[] toLines(File file, String encode){
        InputStream stream = null;
        InputStreamReader reader = null;
        BufferedReader bReader = null;
        try{
            stream = new FileInputStream(file);
            reader = new InputStreamReader(stream, encode);
            bReader = new BufferedReader(reader);
            String line = null;
            List list = new ArrayList();
            while(bReader.ready()){
                line = bReader.readLine();
                list.add(line);
            }
            String[] lines = new String[list.size()];
            Iterator i = list.iterator();
            for(int j = 0; i.hasNext(); j++){
                lines[j] = (String)i.next();
            }
            return lines;
        }catch(Exception e){
            throw new FileUtilRuntimeException(e);
        }finally{
            if(stream != null){
	            try {
	                stream.close();
	            } catch (IOException e) {
	                throw new FileUtilRuntimeException(e);
	            }
            }
            if(reader != null){
	            try {
	                reader.close();
	            } catch (IOException e) {
	                throw new FileUtilRuntimeException(e);
	            }
            }
            if(bReader != null){
	            try {
	                bReader.close();
	            } catch (IOException e) {
	                throw new FileUtilRuntimeException(e);
	            }
            }
        }
    }
    
    /**
     * �t�@�C���̓��e���o�C�g�z��Ŏ擾����B
     * <p>
     * @param file �Ǎ��ރt�@�C��
     * @return �t�@�C������Ǎ��񂾃o�C�g�z��
     */
    public static byte[] toBytes(File file){
        FileInputStream stream = null;
        try{
            stream = new FileInputStream(file);
            byte[] b = new byte[(int) file.length()];
            stream.read(b);
            /*
            fChannel = stream.getChannel();
            ByteBuffer bb = ByteBuffer.allocate((int)fChannel.size());
            fChannel.read(bb);
            System.out.println(Dumper.dump(bb.array()));
        	return bb.array();
        	*/
            return b;
        }catch(Exception e){
            throw new FileUtilRuntimeException(e);
        }finally{
            if(stream != null){
	            try {
	                stream.close();
	            } catch (IOException e) {
	                throw new FileUtilRuntimeException(e);
	            }
            }
        }
    }
    
    // 2010/04/27 option tsuda_t MOV from 2009/04/30 FH_0020 ADD M.Sasaki start.
    /**
     * �t�@�C���̃R�s�[���s���B
     * @param source �R�s�[���t�@�C��
     * @param target �R�s�[��t�@�C��
     */
    public static void copy(File source, File target) throws IOException
    {
        FileChannel sChannel = new FileInputStream(source).getChannel();
        FileChannel tChannel = new FileOutputStream(target).getChannel();

        sChannel.transferTo(0, sChannel.size(), tChannel);

        sChannel.close();
        tChannel.close();
    }

    /**
     * �I�u�W�F�N�g���񉻏��o�����s���B
     * @param obj ���񉻉\�I�u�W�F�N�g
     * @param outFile �o�͐�t�@�C����
     */
    public static void writeSerFile(Object obj, String outFile) throws IOException
    {
        FileOutputStream fos = new FileOutputStream(outFile);
        ObjectOutputStream oStream = new ObjectOutputStream(fos);
        oStream.writeObject(obj);
        oStream.close();
    }

    /**
     * �I�u�W�F�N�g����XML���o�����s���B
     * @param obj ���񉻉\�I�u�W�F�N�g
     * @param outFile �o�͐�t�@�C����
     */
    public static void writeXmlSerFile(Object obj, String outFile) throws IOException
    {
        FileOutputStream fos = new FileOutputStream(outFile);
        XMLEncoder oStream = new XMLEncoder(new BufferedOutputStream(fos));
        oStream.writeObject(obj);
        oStream.close();
    }

    /**
     * ���񉻃t�@�C���̓Ǐo�����s���B
     * @param inFile �Ǐo�����t�@�C��
     * @return �I�u�W�F�N�g
     */
    public static Object readSerFile(String inFile) throws IOException, ClassNotFoundException
    {
        Object retVal = null;

        FileInputStream fis = new FileInputStream(inFile);
        ObjectInputStream iStream = new ObjectInputStream(fis);
        retVal = iStream.readObject();
        iStream.close();

        return retVal;
    }

    /**
     * ����XML�t�@�C���̓Ǐo�����s���B
     * @param inFile �Ǐo�����t�@�C��
     * @return �I�u�W�F�N�g
     */
    public static Object readXmlSerFile(String inFile) throws IOException, ClassNotFoundException
    {
        Object retVal = null;

        FileInputStream fis = new FileInputStream(inFile);
        XMLDecoder iStream = new XMLDecoder(new BufferedInputStream(fis));
        retVal = iStream.readObject();
        iStream.close();

        return retVal;
    }

    /**
     * �t�@�C���̏������s���B
     * @param filePath �o�͐�t�@�C��
     * @param outString �o�͕�����
     * @param encoding �G���R�[�f�B���O
     */
    public static void writeFile(String filePath, String outString, String encoding) throws Exception
    {
        BufferedWriter bWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath), encoding));
        bWriter.write(outString);
        bWriter.close();
    }

    /**
     * �o�C�g��̏������s���B
     * @param outByte �o�̓o�C�g��
     * @param outFile �o�͐�t�@�C����
     */
    public static void writeByteFile(byte[] outByte, String outFile) throws IOException
    {
        FileOutputStream fos = new FileOutputStream(outFile);
        fos.write(outByte);
        fos.close();
    }
    // 2010/04/27 option tsuda_t MOV from 2009/04/30 FH_0020 ADD M.Sasaki end.
    
    // 2014/01/22 SBIM �����ďƉ�E�ʐ�/�␳ �Ή� ADD START hishinuma_d �񓚕␳
    /**
     * �w��f�B���N�g���ȉ��̃t�@�C���E�f�B���N�g�����폜����B
     * @param dirPath �폜�Ώۃf�B���N�g��
     */
    public static void deleteFile(String dirPath) {
    	File dir = new File(dirPath);
    	File[] files = dir.listFiles(getFileRegexFilter("*"));
    	
    	for (File file : files) {
    		deleteDirectory(file);
    	}
    }
    
    /**
     * �ċA�I�Ƀt�@�C���E�f�B���N�g�����폜����B
     * @param file
     */
    public static void deleteDirectory(File file) {
    	if (file.exists() == false){
    		return;
    	}
    	
    	if (file.isFile()) {
    		file.delete();
    		
    	} else if (file.isDirectory()) {
    		File[] files = file.listFiles();
    		for (int i = 0; i < files.length; i++) {
    			deleteDirectory(files[i]);
    		}
    		file.delete();
    	}
    }
    
    /**
     * ���K�\�����g�����߂̃t�B���^�[�N���X
     * @param regex
     * @return
     */
    public static FilenameFilter getFileRegexFilter(String regex) {  
        final String regex_ = regex;  
        return new FilenameFilter() {
            public boolean accept(File file, String name) {
                boolean ret = name.matches(regex_);
                return ret;  
            }
        };  
    }
    // 2014/01/22 SBIM �����ďƉ�E�ʐ�/�␳ �Ή� ADD END hishinuma_d �񓚕␳
}

class FileUtilRuntimeException extends RuntimeException{
    public FileUtilRuntimeException(String message){
        super(message);
    }
    public FileUtilRuntimeException(Exception e){
        super(e);
    }
}
