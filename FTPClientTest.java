/*
 * @(#)FTPClient.java
 *
 * Copyright (c) 2014, NTT DATA Getronics Corporation.
 */
package jp.co.nttdg.bfep.stp.bean.hub;

import java.util.ArrayList;
import java.util.List;

import jp.co.nttdg.bfep.stp.framework.BfepStpException;
import jp.co.nttdg.bfep.stp.framework.BfepStpLoggerFactory;
import jp.co.nttdg.bfep.stp.framework.BfepStpTestLogger;

import org.apache.camel.Exchange;
import org.apache.camel.test.junit4.ExchangeTestSupport;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPFile;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author h_tozawa
 * @version 1.0
 * 
 */
public class FTPClientTest extends ExchangeTestSupport {

    /** テストケースの初期化処理 */
    @BeforeClass
    public static void propertySet() {
        System.setProperty(BfepStpLoggerFactory.TEST, "12");
    }

    /** テストケース実行前にMessageListを初期化 */
    @Before
    public void cleartMessageList() {
        this.logger.clearMessageList();
    }

    /** テスト用ロガーインスタンス。 */
    BfepStpTestLogger logger = BfepStpTestLogger.getInstance();

    /** テストクラスの初期化 */
    FTPClient target = null;

    /** FTPクライアント */
    // private final org.apache.commons.net.ftp.FTPClient ftpClient;

    /** IPアドレス */
    private String ipAddress = new String();

    /** ログインユーザ */
    private String userName = new String();

    /** ログインパスワード */
    private String password = new String();

    /** パッシブモード */
    private boolean isPSV = false;

    /** Exchange */
    private Exchange exchange = createExchange();

    /**
     * 
     */
    public FTPClientTest() {
    }

    /**
     * コンストラクタテスト ※パシブモードON
     * 
     */
    @Test
    public void constructorTestCase001() {

        // constructorのテスト
        this.setConectionValue(true);

        // 実行
        target = new FTPClient(this.ipAddress, this.userName, this.password,
                this.isPSV);

        // 検証
        // constructorの引数が渡っているか確認
        assertEquals(this.ipAddress, target.getIpAddress());
        assertEquals(this.userName, target.getUser());
        assertEquals(this.password, target.getPassword());
        assertTrue(target.isPassive());

        // org.apache.commons.net.ftp.FTPClient インスタンスが生成されているか確認
        assertTrue(target.getFtpClient() instanceof org.apache.commons.net.ftp.FTPClient);

    }

    /**
     * コンストラクタテスト ※パシブモードOFF
     * 
     */
    @Test
    public void constructorTestCase002() {

        // constructorのテスト
        this.setConectionValue(false);
        // 実行
        target = new FTPClient(this.ipAddress, this.userName, this.password,
                this.isPSV);

        // 検証
        // constructorの引数が渡っているか確認
        assertEquals(this.ipAddress, target.getIpAddress());
        assertEquals(this.userName, target.getUser());
        assertEquals(this.password, target.getPassword());
        assertFalse(target.isPassive());

        // org.apache.commons.net.ftp.FTPClient インスタンスが生成されているか確認
        assertTrue(target.getFtpClient() instanceof org.apache.commons.net.ftp.FTPClient);

    }

    /**
     * 正常系
     */
    @Test
    public void connectTestCase001() {

        this.setConectionValue(true);

        // 実行
        target = new FTPClient(this.ipAddress, this.userName, this.password,
                this.isPSV);

        try {
            target.connect();

            // サーバーと接続が確立した
            if (target.getFtpClient().isConnected()) {
                assertTrue(target.getFtpClient().isConnected());

            } else {

                // サーバーと接続が確立しなかった
                assertFalse(target.getFtpClient().isConnected());

                // 設定値の確認
                assertEquals(this.ipAddress, target.getIpAddress());
                assertEquals(this.userName, target.getUser());
                assertEquals(this.password, target.getPassword());
                assertTrue(target.isPassive());
            }

        } catch (BfepStpException e) {

            // 正常系試験なので、こちらには来ない。
            fail("BfepStpException：正常系試験なので、こちらには来ない。" + e.getMessage());

        }

    }

    /**
     * 異常系　（接続の応答 コードが200番台以外） ※やり方が不明なので、とりあえず、IPアドレス不正
     */
    @Test
    public void connectTestCase002() {

        this.setConectionValue(true);

        // 実行
        target = new FTPClient("990.990.990.990", this.userName, this.password,
                this.isPSV);

        try {
            target.connect();

            fail("テストが異常です。");

        } catch (BfepStpException e) {

            List<BfepStpTestLogger.Message> list = this.logger.getMessageList();
            BfepStpTestLogger.Message message = list.get(0);

            // 検証
            assertEquals(5000, message.no);
            assertEquals(1, message.args.length);
            assertEquals("990.990.990.990", message.args[0]);

        }

    }

    /**
     * 異常系　（FTPサーバーが見つからない） ※　とりあえず、FTPサーバーを止めるか、正しくないIPアドレスで接続してみる。
     */
    @Test
    public void connectTestCase003() {

        this.setConectionValue(true);

        // 実行
        target = new FTPClient(this.ipAddress, this.userName, this.password,
                this.isPSV);

        try {
            target.connect();
            fail("テストが異常です。");

        } catch (BfepStpException e) {

            List<BfepStpTestLogger.Message> list = this.logger.getMessageList();
            BfepStpTestLogger.Message message = list.get(0);

            // 確認用
            System.out.println(e.getMessage());

            // 検証
            assertEquals(5001, message.no);
            assertEquals(2, message.args.length);
            assertEquals(this.ipAddress, message.args[0]);
            assertEquals(e.getMessage(), message.args[1]);

        }

    }

    /**
     * 異常系　（ログイン失敗） ※やり方が不明なので、とりあえず、IPアドレスを不正な形
     */
    @Test
    public void connectTestCase004() {

        this.setConectionValue(true);

        // 実行
        target = new FTPClient(this.ipAddress, "this.userName",
                "this.password", this.isPSV);

        try {
            target.connect();

            fail("テストが異常です。");

        } catch (BfepStpException e) {

            List<BfepStpTestLogger.Message> list = this.logger.getMessageList();
            BfepStpTestLogger.Message message = list.get(0);

            // 検証
            assertEquals(5002, message.no);
            assertEquals(1, message.args.length);
            assertEquals("this.userName", message.args[0]);

        }

    }

    /**
     * 異常系　（ログイン失敗） ※やり方が不明なので、とりあえず、IPアドレスを不正な形
     */
    @Test
    public void connectTestCase005() {

        this.setConectionValue(true);

        // 実行
        target = new FTPClient(this.ipAddress, this.userName, this.password,
                this.isPSV);

        try {
            target.connect();

            fail("テストが異常です。");

        } catch (BfepStpException e) {

            List<BfepStpTestLogger.Message> list = this.logger.getMessageList();
            BfepStpTestLogger.Message message = list.get(0);

            // 検証
            assertEquals(5003, message.no);
            assertEquals(2, message.args.length);
            assertEquals(this.userName, message.args[0]);
            assertEquals(e.getMessage(), message.args[1]);

        }
    }

    /**
     * 異常系　（ファイル転送モード切替失敗） ※やり方が不明？、
     */
    @Test
    public void connectTestCase006() {

        this.setConectionValue(true);

        // 実行
        target = new FTPClient(this.ipAddress, this.userName, this.password,
                this.isPSV);

        try {
            target.connect();

            fail("テストが異常です。");

        } catch (BfepStpException e) {

            List<BfepStpTestLogger.Message> list = this.logger.getMessageList();
            BfepStpTestLogger.Message message = list.get(0);

            // 検証
            assertEquals(5010, message.no);
            assertEquals(1, message.args.length);
            assertEquals(FTP.BINARY_FILE_TYPE, message.args[0]);

        }

    }

    /**
     * 異常系　（ファイル転送モード切替で例外が発生） ※やり方が不明？、
     */
    @Test
    public void connectTestCase007() {

        this.setConectionValue(true);

        // 実行
        target = new FTPClient(this.ipAddress, this.userName, this.password,
                this.isPSV);

        try {
            target.connect();

            fail("テストが異常です。");

        } catch (BfepStpException e) {

            List<BfepStpTestLogger.Message> list = this.logger.getMessageList();
            BfepStpTestLogger.Message message = list.get(0);

            // 検証
            assertEquals(5010, message.no);
            assertEquals(2, message.args.length);
            assertEquals(FTP.BINARY_FILE_TYPE, message.args[0]);
            assertEquals(e.getMessage(), message.args[1]);

        }

    }

    /**
     * 正常系
     */
    @Test
    public void disconnectTestCase001() {

        this.setConectionValue(true);

        // 実行
        target = new FTPClient(this.ipAddress, this.userName, this.password,
                this.isPSV);

        try {

            target.connect();
            target.disconnect();

            if (!target.getFtpClient().isAvailable()) {

                // 設定値の確認
                assertEquals(this.ipAddress, target.getIpAddress());
                assertEquals(this.userName, target.getUser());
                assertEquals(this.password, target.getPassword());
                assertTrue(target.isPassive());

            } else {

                fail("切断できていない");

            }

        } catch (BfepStpException e) {

            // 正常系試験なので、こちらには来ない。
            fail("テストが異常です。" + e.getMessage());

        }
    }

    /**
     * 正常系 (切断状態からの試験)
     */
    @Ignore
    public void disconnectTestCase002() {

        this.setConectionValue(true);

        // 実行
        target = new FTPClient(this.ipAddress, this.userName, this.password,
                this.isPSV);

        try {

            target.disconnect();

            // 設定値の確認
            assertEquals(this.ipAddress, target.getIpAddress());
            assertEquals(this.userName, target.getUser());
            assertEquals(this.password, target.getPassword());
            assertTrue(target.isPassive());

        } catch (BfepStpException e) {

            // 正常系試験なので、こちらには来ない。
            fail("テストが異常です。" + e.getMessage());

        }
    }

    /**
     * 異常系　　ログアウト処理中に例外発生
     */
    @Test
    public void disconnectTestCase003() {

        this.setConectionValue(true);

        // 実行
        target = new FTPClient(this.ipAddress, this.userName, this.password,
                this.isPSV);

        try {

            target.connect();
            target.disconnect();

            if (!target.getFtpClient().isAvailable()) {

                fail("テストが異常です。");

            }

        } catch (BfepStpException e) {

            // 設定値の確認
            assertEquals(this.ipAddress, target.getIpAddress());
            assertEquals(this.userName, target.getUser());
            assertEquals(this.password, target.getPassword());
            assertTrue(target.isPassive());

            List<BfepStpTestLogger.Message> list = this.logger.getMessageList();
            BfepStpTestLogger.Message message = list.get(0);

            // 検証
            assertEquals(5010, message.no);
            assertEquals(1, message.args.length);
            assertEquals(e.getMessage(), message.args[0]);

        }
    }

    /**
     * 異常系　　切断処理中に例外発生
     */
    @Test
    public void disconnectTestCase004() {

        this.setConectionValue(true);

        // 実行
        target = new FTPClient(this.ipAddress, this.userName, this.password,
                this.isPSV);

        try {

            target.connect();
            target.disconnect();

            if (!target.getFtpClient().isAvailable()) {

                fail("テストが異常です。");

            }

        } catch (BfepStpException e) {

            // 設定値の確認
            assertEquals(this.ipAddress, target.getIpAddress());
            assertEquals(this.userName, target.getUser());
            assertEquals(this.password, target.getPassword());
            assertTrue(target.isPassive());

            List<BfepStpTestLogger.Message> list = this.logger.getMessageList();
            BfepStpTestLogger.Message message = list.get(0);

            // 検証
            assertEquals(5010, message.no);
            assertEquals(1, message.args.length);
            assertEquals(e.getMessage(), message.args[0]);

        }
    }

    /**
     * 正常系（ファイルあり）
     * 
     */
    @Test
    public void listFilesTestCase001() {

        this.setConectionValue(true);

        List<FTPFile> ftpFileList = new ArrayList<FTPFile>();

        // 実行
        target = new FTPClient(this.ipAddress, this.userName, this.password,
                this.isPSV);

        try {
            target.connect();

            // ファイルの絶対パス
            String dirPaths = "/home/test/listFiles";
            // テスト用正規表現
            String regex = "\\b([A-Za-z_]\\w*)\\.(java|class)\\b";

            ftpFileList = target.listFiles(dirPaths, regex);

            // 取得した内容をconsole出力
            for (int i = 0; i < ftpFileList.size(); i++) {

                FTPFile ftpFile = ftpFileList.get(i);
                System.out.println(ftpFile.getName());
                assertEquals(FTPFile.FILE_TYPE, ftpFile.getType());

            }

        } catch (BfepStpException e) {
            fail("テストが異常です。" + e.getMessage());
        }

    }

    /**
     * 切断状態からの正常系（ファイルあり）
     * 
     */
    @Test
    public void listFilesTestCase002() {

        this.setConectionValue(true);

        List<FTPFile> ftpFileList = new ArrayList<FTPFile>();

        // 実行
        target = new FTPClient(this.ipAddress, this.userName, this.password,
                this.isPSV);

        try {

            // ファイルの絶対パス
            String dirPaths = "/home/test/listFiles";
            // テスト用正規表現
            String regex = "\\b([A-Za-z_]\\w*)\\.(java|class)\\b";

            target.listFiles(dirPaths, regex);
            // 取得した内容をconsole出力
            for (int i = 0; i < ftpFileList.size(); i++) {

                FTPFile ftpFile = ftpFileList.get(i);
                System.out.println(ftpFile.getName());
                assertEquals(FTPFile.FILE_TYPE, ftpFile.getType());

            }

        } catch (BfepStpException e) {

            fail("テストが異常です。" + e.getMessage());
        }
    }

    /**
     * 異常系　　ディレクトリ移動に失敗
     */
    @Test
    public void listFilesTestCase003() {

        List<FTPFile> ftpFileList = new ArrayList<FTPFile>();

        this.setConectionValue(true);

        // ファイルの絶対パス
        String dirPaths = "/home/test/listFiles";
        // テスト用正規表現
        String regex = "\\b([A-Za-z_]\\w*)\\.(java|class)\\b";

        // 実行
        target = new FTPClient(this.ipAddress, this.userName, this.password,
                this.isPSV);

        try {

            target.connect();

            ftpFileList = target.listFiles(dirPaths, regex);

        } catch (BfepStpException e) {

            // 設定値の確認

            assertEquals(0, ftpFileList.size());
            assertEquals(this.ipAddress, target.getIpAddress());
            assertEquals(this.userName, target.getUser());
            assertEquals(this.password, target.getPassword());
            assertTrue(target.isPassive());

            List<BfepStpTestLogger.Message> list = this.logger.getMessageList();
            BfepStpTestLogger.Message message = list.get(0);

            // 検証
            assertEquals(5004, message.no);
            assertEquals(1, message.args.length);
            assertEquals(dirPaths, message.args[0]);

        }
    }

    /**
     * 異常系　　ディレクトリ移動中に例外発生
     */
    @Test
    public void listFilesTestCase004() {

        List<FTPFile> ftpFileList = new ArrayList<FTPFile>();

        this.setConectionValue(true);

        // ファイルの絶対パス
        String dirPaths = "/home/test/listFiles";
        // テスト用正規表現
        String regex = "\\b([A-Za-z_]\\w*)\\.(java|class)\\b";

        // 実行
        target = new FTPClient(this.ipAddress, this.userName, this.password,
                this.isPSV);

        try {

            target.connect();

            ftpFileList = target.listFiles(dirPaths, regex);

        } catch (BfepStpException e) {

            // 設定値の確認
            assertEquals(0, ftpFileList.size());
            assertEquals(this.ipAddress, target.getIpAddress());
            assertEquals(this.userName, target.getUser());
            assertEquals(this.password, target.getPassword());
            assertTrue(target.isPassive());

            List<BfepStpTestLogger.Message> list = this.logger.getMessageList();
            BfepStpTestLogger.Message message = list.get(0);

            // 検証
            assertEquals(5004, message.no);
            assertEquals(2, message.args.length);
            assertEquals(dirPaths, message.args[0]);
            assertEquals(e.getMessage(), message.args[1]);

        }
    }

    /**
     * 準正常　　ファイルリストができない（ファイルなし）
     */
    @Test
    public void listFilesTestCase005() {

        List<FTPFile> ftpFileList = new ArrayList<FTPFile>();

        this.setConectionValue(true);

        // ファイルの絶対パス
        String dirPaths = "/home/test/listFiles";
        // テスト用正規表現
        String regex = "\\b([A-Za-z_]\\w*)\\.(java|class)\\b";

        // 実行
        target = new FTPClient(this.ipAddress, this.userName, this.password,
                this.isPSV);

        try {

            target.connect();

            ftpFileList = target.listFiles(dirPaths, regex);

            // 設定値の確認
            assertEquals(0, ftpFileList.size());

        } catch (BfepStpException e) {
            fail("準正常なので例外ではない");

        }
    }

    /**
     * 異常系　　ファイルリスト取得中に例外発生
     */
    @Test
    public void listFilesTestCase006() {

        List<FTPFile> ftpFileList = new ArrayList<FTPFile>();

        this.setConectionValue(true);

        // ファイルの絶対パス
        String dirPaths = "/home/test/listFiles";
        // テスト用正規表現
        String regex = "\\b([A-Za-z_]\\w*)\\.(java|class)\\b";

        // 実行
        target = new FTPClient(this.ipAddress, this.userName, this.password,
                this.isPSV);

        try {

            target.connect();

            ftpFileList = target.listFiles(dirPaths, regex);

            fail("正常系はあり得ない");

        } catch (BfepStpException e) {

            // 設定値の確認
            assertEquals(0, ftpFileList.size());
            assertEquals(this.ipAddress, target.getIpAddress());
            assertEquals(this.userName, target.getUser());
            assertEquals(this.password, target.getPassword());
            assertTrue(target.isPassive());

            List<BfepStpTestLogger.Message> list = this.logger.getMessageList();
            BfepStpTestLogger.Message message = list.get(0);

            // 検証
            assertEquals(5005, message.no);
            assertEquals(3, message.args.length);
            assertEquals(dirPaths, message.args[0]);
            assertEquals(e.getMessage(), message.args[2]);

        }
    }

    /**
     * 正常系　サーバに接続したのちファイル内容を取得
     */
    @Test
    public void getFileTestCase001() {

        byte[] byteArray = new byte[10240];

        this.setConectionValue(true);

        // 実行
        target = new FTPClient(this.ipAddress, this.userName, this.password,
                this.isPSV);

        String filePath = "/home/test/listFiles/getFile/memo.txt";

        try {

            target.connect();

            byteArray = target.getFile(filePath);

            for (int i = 1; i < byteArray.length; i++) {

                if (i % 1024 == 0) {
                    System.out.println();
                } else {
                    System.out.print(byteArray[i] + " ");

                }

            }

        } catch (BfepStpException e) {
            fail("テストが異常です。");
        }
    }

    /**
     * 正常系　切断状態からのファイル内容を取得
     */
    @Test
    public void getFileTestCase002() {

        byte[] byteArray = new byte[10240];

        this.setConectionValue(true);

        // 実行
        target = new FTPClient(this.ipAddress, this.userName, this.password,
                this.isPSV);

        String filePath = "/home/test/listFiles/getFile/memo.txt";

        try {

            byteArray = target.getFile(filePath);

            for (int i = 1; i < byteArray.length; i++) {

                if (i % 1024 == 0) {
                    System.out.println();
                } else {
                    System.out.print(byteArray[i] + " ");

                }

            }

        } catch (BfepStpException e) {
            fail("例外はあり得ない");
        }
    }

    /**
     * 異常系　ファイル読み込みが完了できなかった
     */
    @Test
    public void getFileTestCase003() {

        byte[] byteArray = new byte[10240];

        this.setConectionValue(true);

        // 実行
        target = new FTPClient(this.ipAddress, this.userName, this.password,
                this.isPSV);

        String filePath = "/home/test/listFiles/getFile/memo.txt";

        try {

            byteArray = target.getFile(filePath);

            fail("テストが異常です。");

        } catch (BfepStpException e) {

            assertEquals(0, byteArray.length);

            List<BfepStpTestLogger.Message> list = this.logger.getMessageList();
            BfepStpTestLogger.Message message = list.get(0);

            // 検証
            assertEquals(5006, message.no);
            assertEquals(2, message.args.length);
            assertEquals(this.ipAddress, message.args[0]);
            assertEquals(filePath, message.args[1]);
        }
    }

    /**
     * 異常系　ファイル読み込みで例外発生
     */
    @Test
    public void getFileTestCase004() {

        byte[] byteArray = new byte[10240];

        this.setConectionValue(true);

        // 実行
        target = new FTPClient(this.ipAddress, this.userName, this.password,
                this.isPSV);

        String filePath = "/home/test/listFiles/getFile/memo.txt";

        try {

            byteArray = target.getFile(filePath);

            fail("テストが異常です。");

        } catch (BfepStpException e) {

            assertEquals(0, byteArray.length);

            List<BfepStpTestLogger.Message> list = this.logger.getMessageList();
            BfepStpTestLogger.Message message = list.get(0);

            // 検証
            assertEquals(5007, message.no);
            assertEquals(3, message.args.length);
            assertEquals(this.ipAddress, message.args[0]);
            assertEquals(filePath, message.args[1]);
            assertEquals(e.getMessage(), message.args[2]);
        }
    }

    /**
     * 正常系　ファイル削除成功（切断状態から）
     */
    @Test
    public void deleteFileTestCase001() {

        List<FTPFile> ftpFileList = new ArrayList<FTPFile>();

        this.setConectionValue(true);

        // 実行
        target = new FTPClient(this.ipAddress, this.userName, this.password,
                this.isPSV);

        String filePath = "/home/test/listFiles/getFile/memo.txt";

        // ファイルの絶対パス
        String dirPaths = "/home/test/listFiles";
        // テスト用正規表現
        String regex = "\\b([A-Za-z_]\\w*)\\.(txt|csv)\\b";

        try {

            if (target.deleteFile(filePath)) {

                ftpFileList = target.listFiles(dirPaths, regex);

                // 取得した内容をconsole出力
                for (int i = 0; i < ftpFileList.size(); i++) {

                    FTPFile ftpFile = ftpFileList.get(i);
                    System.out.println(ftpFile.getName());
                    assertEquals(FTPFile.FILE_TYPE, ftpFile.getType());

                }

            } else {

                fail("ファイル削除失敗はしない。");

            }

        } catch (BfepStpException e) {

            fail("例外は発生しない。");
        }
    }

    /**
     * 正常系　ファイル削除成功（接続状態から）
     */
    @Test
    public void deleteFileTestCase00２() {

        List<FTPFile> ftpFileList = new ArrayList<FTPFile>();

        this.setConectionValue(true);

        // 実行
        target = new FTPClient(this.ipAddress, this.userName, this.password,
                this.isPSV);

        String filePath = "/home/test/listFiles/getFile/memo.txt";

        // ファイルの絶対パス
        String dirPaths = "/home/test/listFiles";
        // テスト用正規表現
        String regex = "\\b([A-Za-z_]\\w*)\\.(txt|csv)\\b";

        try {

            if (target.deleteFile(filePath)) {

                fail("ファイル削除成功はしない。");

            } else {

                ftpFileList = target.listFiles(dirPaths, regex);

                // 取得した内容をconsole出力
                for (int i = 0; i < ftpFileList.size(); i++) {

                    FTPFile ftpFile = ftpFileList.get(i);
                    System.out.println(ftpFile.getName());
                    assertEquals(FTPFile.FILE_TYPE, ftpFile.getType());
                }

                List<BfepStpTestLogger.Message> list = this.logger
                        .getMessageList();
                BfepStpTestLogger.Message message = list.get(0);

                // 検証
                assertEquals(5008, message.no);
                assertEquals(2, message.args.length);
                assertEquals(this.ipAddress, message.args[0]);
                assertEquals(filePath, message.args[1]);

            }

        } catch (BfepStpException e) {

            fail("例外は発生しない。");

        }
    }

    /**
     * 異常系　ファイル削除で例外発生
     */
    @Test
    public void deleteFileTestCase003() {

        List<FTPFile> ftpFileList = new ArrayList<FTPFile>();

        this.setConectionValue(true);

        // 実行
        target = new FTPClient(this.ipAddress, this.userName, this.password,
                this.isPSV);

        String filePath = "/home/test/listFiles/getFile/memo.txt";

        try {

            target.deleteFile(filePath);
            fail("ファイル削除成功はしない。");

        } catch (BfepStpException e) {

            List<BfepStpTestLogger.Message> list = this.logger.getMessageList();
            BfepStpTestLogger.Message message = list.get(0);

            // 検証
            assertEquals(5009, message.no);
            assertEquals(3, message.args.length);
            assertEquals(this.ipAddress, message.args[0]);
            assertEquals(filePath, message.args[1]);
            assertEquals(e.getMessage(), message.args[2]);
        }
    }
	
	
	
	
	    /**
     * 正常系　ファイル削除成功（切断状態から）
     */
    @Test
    public void deleteFileTestCase001() {

        List<FTPFile> ftpFileList = new ArrayList<FTPFile>();

        this.setConectionValue(true);

        // 実行
        target = new FTPClient(this.ipAddress, this.userName, this.password,
                this.isPSV);

        String filePath = "/home/test/listFiles/getFile/memo.txt";

        // ファイルの絶対パス
        String dirPaths = "/home/test/listFiles";
        // テスト用正規表現
        String regex = "\\b([A-Za-z_]\\w*)\\.(txt|csv)\\b";

        try {

            if (target.deleteFile(filePath)) {

                ftpFileList = target.listFiles(dirPaths, regex);

                // 取得した内容をconsole出力
                for (int i = 0; i < ftpFileList.size(); i++) {

                    FTPFile ftpFile = ftpFileList.get(i);
                    System.out.println(ftpFile.getName());
                    assertEquals(FTPFile.FILE_TYPE, ftpFile.getType());

                }

            } else {

                fail("ファイル削除失敗はしない。");

            }

        } catch (BfepStpException e) {

            fail("例外は発生しない。");
        }
    }

    /**
     * 正常系　ファイル削除成功（接続状態から1リスト）
     */
    @Test
    public void deleteFilesTestCase001() {

        List<FTPFile> ftpFileList = new ArrayList<FTPFile>();


        // 実行
        Exchange exchange = CreateExchange();
        
    	// リスト１件
    	ArrayList<String> filePathList = new ArryList<String>();
    	filePathList.add("/home/test/listFiles/getFile/memo.txt");
    	
    	
    	exchange.getIn().setHeader("jp.co.nttdg.bfep.stp.hub.ftp.deletePath", filePathList);
    	

        try {
        	target.deleteFiles(exchange);
        	assertFalse("jp.co.nttdg.bfep.stp.hub.ftp.ClientError");

        } catch (BfepStpException e) {
        	
        	List<BfepStpTestLogger.Message> list = this.logger
                        .getMessageList();
            BfepStpTestLogger.Message message = list.get(0);

            // 検証
            assertEquals(5008, message.no);
            assertEquals(2, message.args.length);
            assertEquals(this.ipAddress, message.args[0]);
            assertEquals(filePath, message.args[1]);

            fail("例外は発生しない。");

        }
    }

	
	
    /**
     * 正常系　ファイル削除成功（接続状態から3リスト）
     */
    @Test
    public void deleteFilesTestCase002() {

        List<FTPFile> ftpFileList = new ArrayList<FTPFile>();


        // 実行
        Exchange exchange = CreateExchange();
        
    	// リスト3件
    	ArrayList<String> filePathList = new ArryList<String>();
    	filePathList.add("/home/test/listFiles/getFile/memo.txt");
    	filePathList.add("/home/test/listFiles/getFile/memo.cvs");
    	filePathList.add("/home/test/listFiles/getFile/memo.xls");
    	
    	exchange.getIn().setHeader("jp.co.nttdg.bfep.stp.hub.ftp.deletePath", filePathList);
    	

        try {


        	target.deleteFiles(exchange);
        	assertFalse("jp.co.nttdg.bfep.stp.hub.ftp.ClientError");



        } catch (BfepStpException e) {
        	
        	List<BfepStpTestLogger.Message> list = this.logger
                        .getMessageList();
            BfepStpTestLogger.Message message = list.get(0);

            // 検証
            assertEquals(5008, message.no);
            assertEquals(2, message.args.length);
            assertEquals(this.ipAddress, message.args[0]);
            assertEquals(filePath, message.args[1]);

            fail("例外は発生しない。");

        }
    }

	
    /**
     * 正常系　ファイル削除成功（接続状態から5リスト）
     */
    @Test
    public void deleteFilesTestCase003() {

        List<FTPFile> ftpFileList = new ArrayList<FTPFile>();


        // 実行
        Exchange exchange = CreateExchange();
        
    	// リスト3件
    	ArrayList<String> filePathList = new ArryList<String>();
    	filePathList.add("/home/test/listFiles/getFile/memo.txt");
    	filePathList.add("/home/test/listFiles/getFile/memo.cvs");
    	filePathList.add("/home/test/listFiles/getFile/memo.xls");
    	filePathList.add("/home/test/listFiles/getFile/memo.doc");
    	filePathList.add("/home/test/listFiles/getFile/memo.xlsx");
    	
    	exchange.getIn().setHeader("jp.co.nttdg.bfep.stp.hub.ftp.deletePath", filePathList);
    	

        try {


        	target.deleteFiles(exchange);
        	assertFalse("jp.co.nttdg.bfep.stp.hub.ftp.ClientError");


        } catch (BfepStpException e) {
        	
        	List<BfepStpTestLogger.Message> list = this.logger
                        .getMessageList();
            BfepStpTestLogger.Message message = list.get(0);

            // 検証
            assertEquals(5008, message.no);
            assertEquals(2, message.args.length);
            assertEquals(this.ipAddress, message.args[0]);
            assertEquals(filePath, message.args[1]);

            fail("例外は発生しない。");

        }
    }
	
    /**
     * 異常系　ファイル削除成功（接続状態から0リスト）
     */
    @Test
    public void deleteFilesTestCase004() {

        List<FTPFile> ftpFileList = new ArrayList<FTPFile>();


        // 実行
        Exchange exchange = CreateExchange();
        
    	// リスト0件
    	ArrayList<String> filePathList = new ArryList<String>();
    	exchange.getIn().setHeader("jp.co.nttdg.bfep.stp.hub.ftp.deletePath", filePathList);
    	

        try {
        	target.deleteFiles(exchange);
        	fail("テストが異常です。");
        } catch (BfepStpException e) {
        	
			List<BfepStpTestLogger.Message> list = this.logger
                        .getMessageList();
            BfepStpTestLogger.Message message = list.get(0);

            // 検証
            assertEquals(5008, message.no);
            assertEquals(2, message.args.length);
            assertEquals(this.ipAddress, message.args[0]);
            assertEquals(filePath, message.args[1]);
        	
        	assertTrue(exchange.getIn().getHeader("jp.co.nttdg.bfep.stp.hub.ftp.ftpClientError"));

        }
    }
	
	
	
	
	
	
	
	
	

    /**
     * 引数代入
     * 
     * @param flag
     */
    private void setConectionValue(boolean flag) {

        if (flag) {
            this.ipAddress = "127.0.0.1";
            this.userName = "h_tozawa";
            this.password = "h_tozawa";
            this.isPSV = true;

        } else {

            this.ipAddress = "";
            this.userName = "";
            this.userName = "";
            this.isPSV = false;
        }

    }

}

