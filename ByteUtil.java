/*
 * �쐬���F2005/05/24
 * 
 * (C) COPYRIGHT Open Future System Corp. 2005
 * All Rights Reserved.
 */
package jp.co.profitcube.eacris.web.bridge.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * �o�C�g�z��𑀍삷�郆�[�e�B���e�B���\�b�h�S�B
 * <p>
 * @author daisaku
 * @version 1.0
 */
public class ByteUtil {
    
    /**
     * �V�K�̃t�@�C���Ƀo�C�g�z��̓��e���������ށB
     * �����̃t�@�C���ɏ㏑�����悤�Ƃ����ꍇ�͎��s����O����������B
     * <p>
     * @param b �o�C�g�z��
     * @param newFile �V�K�쐬�t�@�C��
     */
    public static synchronized void toFile(byte[] b, File newFile){
        if(FileUtil.isReadFile(newFile)){
            throw new ByteUtilRuntimeException("already exist file: file-path["+newFile.getPath()+"]");
        }
        OutputStream stream = null;
        try {
            stream = new FileOutputStream(newFile);
            stream.write(b);
            stream.flush();
            stream.close();
        } catch (Exception e) {
            throw new ByteUtilRuntimeException(e);
        }finally{
            if(stream != null){
                try {
                    stream.close();
                } catch (IOException e) {
                    throw new ByteUtilRuntimeException(e);
                }
            }
        }
    }

}

class ByteUtilRuntimeException extends RuntimeException{
    public ByteUtilRuntimeException(String message){
        super(message);
    }
    public ByteUtilRuntimeException(Exception e){
        super(e);
    }
}

