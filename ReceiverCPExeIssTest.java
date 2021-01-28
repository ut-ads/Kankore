/*
 * @(#)ReceiverCPExeIssTest.java
 *
 * Copyright (c) 2014, NTT DATA Getronics Corporation.
 */
package jp.co.nttdg.bfep.stp.bean.hub;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.sql.DataSource;

import jp.co.nttdg.bfep.stp.framework.BfepStpException;
import jp.co.nttdg.bfep.stp.framework.BfepStpLoggerFactory;
import jp.co.nttdg.bfep.stp.framework.BfepStpMain;
import jp.co.nttdg.bfep.stp.framework.BfepStpTestLogger;
import oracle.jdbc.pool.OracleDataSource;

import org.apache.camel.Exchange;
import org.apache.camel.test.junit4.ExchangeTestSupport;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author h_tozawa
 * @version 1.0
 * 
 */
public class ReceiverCPExeIssTest extends ExchangeTestSupport {

    /** テストケースの初期化処理 */
    @BeforeClass
    public static void propertySet() {
        System.setProperty(BfepStpLoggerFactory.TEST, "12");
        System.setProperty(BfepStpMain.XPATH_FACTORY_PROPERTY_NAME,
                "org.apache.xpath.jaxp.XPathFactoryImpl");
        System.setProperty(BfepStpMain.TRANSFORMER_FACTORY_PROPERTY_NAME,
                "com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl");

    }

    /** テストケース実行前にMessageListを初期化 */
    @Before
    public void cleartMessageList() {
        this.logger.clearMessageList();
    }

    /** テスト用ロガーインスタンス。 */
    BfepStpTestLogger logger = BfepStpTestLogger.getInstance();

    /** テストクラスの初期化 */
    ReceiverCPExeIss target = null;

    /**
     * 
     */
    public ReceiverCPExeIssTest() {
    }

    /**
     * 
     */
    private FTPClient otherServerConnectionInfo(OracleDataSource datasource) {

        // JDBCの書き方は用確認
        datasource.setURL("jdbc:oracle:thin:@192.168.45.37:1521:hubstp");
        datasource.setUser("hubstp");
        datasource.setPassword("hubstp");

        // FTPサーバ・ログイン情報
        String ipAddress = "127.0.0.1";
        String userName = "FTP";
        String password = "FTP";
        boolean isPSV = true;

        FTPClient ftpClient = new FTPClient(ipAddress, userName, password,
                isPSV);
        return ftpClient;
    }

    /**
     * コンストラクタが正常に処理されることを確認 【正常】<br>
     * 引数の値がメンバ変数に設定されている事を確認<br>
     * （プリフィックスリスト：１件/拡張子リスト：２件）<br>
     */
    @Test
    public void constructorTestCase001() {

        // データソース
        OracleDataSource datasource;
        try {
            datasource = new OracleDataSource();

            FTPClient ftpClient = otherServerConnectionInfo(datasource);

            // ディレクトリ名
            String dirName = "/home/receiverCPExeIssTest";

            // プリフィックスリスト
            List<String> prefixList = new ArrayList<String>();
            prefixList.add("HUBSTP001");
            // prefixList.add("HUBSTP011");
            // prefixList.add("HUBSTP002");
            // prefixList.add("HUBSTP012");

            // 拡張子リスト
            List<String> extensionList = new ArrayList<String>();
            extensionList.add("end");
            extensionList.add("csv");

            target = new ReceiverCPExeIss(ftpClient, dirName, prefixList,
                    extensionList, datasource);

            FTPClient retFTPCliant = target.getftpClieant();

            // FTPクライアントが格納されているか確認
            assertTrue(retFTPCliant instanceof FTPClient);

            // 格納しているディレクトリ名と引数が一致してるか確認
            assertEquals(dirName, target.getDirName());

            // 格納しているプリフィックスリストと引数が一致しているか確認
            Set<String> setist = new TreeSet<String>();
            setist.addAll(prefixList);
            assertEquals(setist, target.getPrefixList());

            // 　リストの内容を確認
            int index = 0;
            for (String prefix : target.getPrefixList()) {
                assertEquals(prefixList.get(index), prefix);
                System.out.println(prefix);
                index++;
            }

            // 格納している拡張子リストと引数が一致しているか確認
            assertEquals(extensionList, target.getExtensionList());

            // 　リストの内容を確認
            index = 0;
            for (String extension : target.getExtensionList()) {
                assertEquals(extensionList.get(index), extension);
                System.out.println(extension);
                index++;
            }

            // データソースが格納されているか確認

            assertTrue(target.getDatasource() instanceof DataSource);

        } catch (BfepStpException e) {
            fail(e.getMessage());

        } catch (SQLException e) {
            fail(e.getMessage());

        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    /**
     * コンストラクタが正常に処理されることを確認【正常】<br>
     * 引数の値がメンバ変数に設定されている事を確認<br>
     * （プリフィックスリスト：４件/拡張子リスト：２件）<br>
     */
    @Test
    public void constructorTestCase002() {

        // データソース
        OracleDataSource datasource;
        try {
            datasource = new OracleDataSource();

            FTPClient ftpClient = otherServerConnectionInfo(datasource);

            // ディレクトリ名
            String dirName = "/home/receiverCPExeIssTest";

            // プリフィックスリスト
            List<String> prefixList = new ArrayList<String>();
            prefixList.add("HUBSTP001");
            prefixList.add("HUBSTP011");
            prefixList.add("HUBSTP002");
            prefixList.add("HUBSTP012");

            // 拡張子リスト
            List<String> extensionList = new ArrayList<String>();
            extensionList.add("end");
            extensionList.add("csv");

            target = new ReceiverCPExeIss(ftpClient, dirName, prefixList,
                    extensionList, datasource);

            FTPClient retFTPCliant = target.getftpClieant();

            // FTPクライアントが格納されているか確認
            assertTrue(retFTPCliant instanceof FTPClient);

            // 格納しているディレクトリ名と引数が一致してるか確認
            assertEquals(dirName, target.getDirName());

            // 格納しているプリフィックスリストと引数が一致しているか確認
            Set<String> setist = new TreeSet<String>();
            setist.addAll(prefixList);
            assertEquals(setist, target.getPrefixList());

            // 　リストの内容を確認
            int index = 0;
            for (String prefix : target.getPrefixList()) {
                assertEquals(prefixList.get(index), prefix);
                System.out.println(prefix);
                index++;
            }

            // 格納している拡張子リストと引数が一致しているか確認
            assertEquals(extensionList, target.getExtensionList());

            // 　リストの内容を確認
            index = 0;
            for (String extension : target.getExtensionList()) {
                assertEquals(extensionList.get(index), extension);
                System.out.println(extension);
                index++;
            }

            // データソースが格納されているか確認

            assertTrue(target.getDatasource() instanceof DataSource);

        } catch (BfepStpException e) {
            fail(e.getMessage());

        } catch (SQLException e) {
            fail(e.getMessage());

        } catch (Exception e) {
            fail(e.getMessage());
        }

    }

    /**
     * コンストラクタにて例外が発生することを確認【異常】<br>
     * （プリフィックスリスト：０件/拡張子リスト：２件）<br>
     */
    @Test
    public void constructorTestCase003() {

        // データソース
        OracleDataSource datasource;
        try {
            datasource = new OracleDataSource();

            FTPClient ftpClient = otherServerConnectionInfo(datasource);

            // ディレクトリ名
            String dirName = "/home/receiverCPExeIssTest";

            // プリフィックスリスト
            List<String> prefixList = new ArrayList<String>();

            // 拡張子リスト
            List<String> extensionList = new ArrayList<String>();
            extensionList.add("end");
            extensionList.add("csv");

            target = new ReceiverCPExeIss(ftpClient, dirName, prefixList,
                    extensionList, datasource);

            fail("TestFail");

        } catch (BfepStpException be) {

            List<BfepStpTestLogger.Message> list = this.logger.getMessageList();
            BfepStpTestLogger.Message message = list.get(0);

            // 検証
            assertEquals(2100, message.no);
            assertEquals(1, message.args.length);
            assertEquals("プリフィックスリスト", message.args[0]);

        } catch (Exception e) {
            fail(e.getMessage());
            e.printStackTrace();
        }

    }

    /**
     * コンストラクタにて例外が発生することを確認【異常】<br>
     * （プリフィックスリストの要素が１件重複している）<br>
     */
    @Test
    public void constructorTestCase004() {

        // データソース
        OracleDataSource datasource;
        try {
            datasource = new OracleDataSource();

            FTPClient ftpClient = otherServerConnectionInfo(datasource);

            // ディレクトリ名
            String dirName = "/home/receiverCPExeIssTest";

            // プリフィックスリスト
            List<String> prefixList = new ArrayList<String>();
            prefixList.add("HUBSTP001");
            prefixList.add("HUBSTP011");
            prefixList.add("HUBSTP002");
            prefixList.add("HUBSTP012");
            // 重複した要素
            prefixList.add("HUBSTP002");

            // 拡張子リスト
            List<String> extensionList = new ArrayList<String>();
            extensionList.add("end");
            extensionList.add("csv");

            target = new ReceiverCPExeIss(ftpClient, dirName, prefixList,
                    extensionList, datasource);

            fail("TestFail");

        } catch (BfepStpException be) {

            List<BfepStpTestLogger.Message> list = this.logger.getMessageList();
            BfepStpTestLogger.Message message = list.get(0);

            // 検証
            assertEquals(2100, message.no);
            assertEquals(1, message.args.length);
            assertEquals("プリフィックスリスト", message.args[0]);

        } catch (Exception e) {
            fail(e.getMessage());
            e.printStackTrace();
        }

    }

    /**
     * コンストラクタにて例外が発生することを確認【異常】<br>
     * （プリフィックスリスト：1件/拡張子リスト：１件）<br>
     */
    @Test
    public void constructorTestCase005() {

        // データソース
        OracleDataSource datasource;
        try {
            datasource = new OracleDataSource();

            FTPClient ftpClient = otherServerConnectionInfo(datasource);

            // ディレクトリ名
            String dirName = "/home/receiverCPExeIssTest";

            // プリフィックスリスト
            List<String> prefixList = new ArrayList<String>();
            prefixList.add("HUBSTP001");

            // 拡張子リスト
            List<String> extensionList = new ArrayList<String>();
            extensionList.add("csv");

            target = new ReceiverCPExeIss(ftpClient, dirName, prefixList,
                    extensionList, datasource);

            fail("TestFail");

        } catch (BfepStpException be) {

            List<BfepStpTestLogger.Message> list = this.logger.getMessageList();
            BfepStpTestLogger.Message message = list.get(0);

            // 検証
            assertEquals(2100, message.no);
            assertEquals(1, message.args.length);
            assertEquals("拡張子リスト", message.args[0]);

        } catch (Exception e) {
            fail(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * コンストラクタにて例外が発生することを確認【正常】<br>
     * （プリフィックスリスト：４件/拡張子リスト：０件）<br>
     */
    @Test
    public void constructorTestCase006() {

        // データソース
        OracleDataSource datasource;
        try {
            datasource = new OracleDataSource();

            FTPClient ftpClient = otherServerConnectionInfo(datasource);

            // ディレクトリ名
            String dirName = "/home/receiverCPExeIssTest";

            // プリフィックスリスト
            List<String> prefixList = new ArrayList<String>();
            prefixList.add("HUBSTP001");
            prefixList.add("HUBSTP002");
            prefixList.add("HUBSTP011");
            prefixList.add("HUBSTP012");

            // 拡張子リスト
            List<String> extensionList = new ArrayList<String>();
            extensionList.add("csv");

            target = new ReceiverCPExeIss(ftpClient, dirName, prefixList,
                    extensionList, datasource);

            fail("TestFail");

        } catch (BfepStpException be) {

            List<BfepStpTestLogger.Message> list = this.logger.getMessageList();
            BfepStpTestLogger.Message message = list.get(0);

            // 検証
            assertEquals(2100, message.no);
            assertEquals(1, message.args.length);
            assertEquals("拡張子リスト", message.args[0]);

        } catch (Exception e) {
            fail(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * コンストラクタにて例外が発生することを確認【異常】<br>
     * （プリフィックスリストの要素が４件重複している）<br>
     */
    @Test
    public void constructorTestCase007() {

        // データソース
        OracleDataSource datasource;
        try {
            datasource = new OracleDataSource();

            FTPClient ftpClient = otherServerConnectionInfo(datasource);

            // ディレクトリ名
            String dirName = "/home/receiverCPExeIssTest";

            // プリフィックスリスト
            List<String> prefixList = new ArrayList<String>();
            prefixList.add("HUBSTP001");
            prefixList.add("HUBSTP011");
            prefixList.add("HUBSTP002");
            prefixList.add("HUBSTP012");
            // 重複した要素
            prefixList.add("HUBSTP001");
            prefixList.add("HUBSTP011");
            prefixList.add("HUBSTP002");
            prefixList.add("HUBSTP012");

            // 拡張子リスト
            List<String> extensionList = new ArrayList<String>();
            extensionList.add("end");
            extensionList.add("csv");

            target = new ReceiverCPExeIss(ftpClient, dirName, prefixList,
                    extensionList, datasource);

            fail("TestFail");

        } catch (BfepStpException be) {

            List<BfepStpTestLogger.Message> list = this.logger.getMessageList();
            BfepStpTestLogger.Message message = list.get(0);

            // 検証
            assertEquals(2100, message.no);
            assertEquals(1, message.args.length);
            assertEquals("プリフィックスリスト", message.args[0]);

        } catch (Exception e) {
            fail(e.getMessage());
            e.printStackTrace();
        }

    }

    /**
     * 【正常】<br>
     * 戻り値の内容がFTPサーバにあるファイル内容と一致している事を確認<br>
     * DBのファイル通番が最新になっていることを確認<br>
     * Exchangeの整合性を確認<br>
     * （プリフィックスリスト：４件/拡張子リスト：２件）<br>
     */
    @SuppressWarnings("unchecked")
    @Test
    public void processTestCase001() {

        /*************************************************************************************/
        /* 【前提】 */
        /* タイムスタンプが一番古い.ENDファイルを HUBSTP001_20140422_0001.end とする */
        /*                                                                                   */
        /*************************************************************************************/

        String fileTsuban = "0001";

        // データソース
        OracleDataSource datasource;
        try {
            datasource = new OracleDataSource();

            // JDBCの書き方は用確認
            datasource.setURL("jdbc:oracle:thin:@192.168.45.37:1521:hubstp");
            datasource.setUser("hubstp");
            datasource.setPassword("hubstp");

            FTPClient ftpClient = otherServerConnectionInfo(datasource);

            // ディレクトリ名
            String dirName = "/home/receiverCPExeIssTest/listFiles";

            // プリフィックスリスト
            List<String> prefixList = new ArrayList<String>();
            prefixList.add("HUBSTP001");
            prefixList.add("HUBSTP011");
            prefixList.add("HUBSTP002");
            prefixList.add("HUBSTP012");

            // 拡張子リスト
            List<String> extensionList = new ArrayList<String>();
            extensionList.add("end");
            extensionList.add("csv");

            target = new ReceiverCPExeIss(ftpClient, dirName, prefixList,
                    extensionList, datasource);

            // exchangeの生成
            Exchange exchange = createExchange();

            // 通常の最大値をとれるタスク№
            exchange.setFromRouteId("01");

            exchange.getIn().setHeader(
                    "jp.co.nttdg.bfep.stp.hub.message.systemCdFrom", "03");

            exchange.getIn().setHeader("jp.co.nttdg.bfepstp.today", "20140422");

            // テスト実施
            Set<Map.Entry<String, byte[]>> returnSst = target.process(exchange);

            // リストの存在確認
            assertTrue("False:returnList not faund", (0 < returnSst.size()));

            for (Map.Entry<String, byte[]> returnMap : returnSst) {

                System.out.println(returnMap.getKey() + " : "
                        + new String((byte[]) returnMap.getValue()));

            }

            // Exchangeの内容確認

            // ヘッダ部に作成した.ENDファイルリスト(削除ファイルリスト)を取得
            List<?> endFilesList = (List<?>) exchange.getIn().getHeader(
                    "jp.co.nttdg.bfep.stp.hub.ftp.deletePaths");

            for (String value : (List<String>) endFilesList) {

                // 内容は目視
                System.out.println(value);
            }

            // 引数で渡されたExchangeのin.headersにファイル通番を確認
            assertEquals(
                    fileTsuban,
                    exchange.getIn().getHeader(
                            "jp.co.nttdg.bfep.stp.hub.fileTsuban"));

            Connection conn = target.getConnection();

            conn.commit();
            conn.setAutoCommit(true);
            conn.close();

        } catch (BfepStpException e) {
            e.printStackTrace();
            fail(e.getMessage());

        } catch (SQLException e) {
            e.printStackTrace();
            fail(e.getMessage());

        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());

        }

    }

    /**
     * 【準正常】<br>
     * FTPサーバーに.ENDが無い時の確認<br>
     */
    @Test
    public void processTestCase002() {

        // データソース
        OracleDataSource datasource;
        try {
            datasource = new OracleDataSource();

            FTPClient ftpClient = otherServerConnectionInfo(datasource);

            // ディレクトリ名
            String dirName = "/home/receiverCPExeIssTest/listFiles/notEndFile";

            // プリフィックスリスト
            List<String> prefixList = new ArrayList<String>();
            prefixList.add("HUBSTP001");
            prefixList.add("HUBSTP011");
            prefixList.add("HUBSTP002");
            prefixList.add("HUBSTP012");

            // 拡張子リスト
            List<String> extensionList = new ArrayList<String>();
            extensionList.add("end");
            extensionList.add("csv");

            target = new ReceiverCPExeIss(ftpClient, dirName, prefixList,
                    extensionList, datasource);

            // exchangeの生成
            Exchange exchange = createExchange();

            // 通常の最大値をとれるタスク№
            exchange.setFromRouteId("");

            exchange.getIn().setHeader("jp.co.nttdg.bfepstp.today", "20140425");

            // テスト実施
            Set<Map.Entry<String, byte[]>> returnSst = target.process(exchange);

            // 　要素ゼロのリストを取得
            assertEquals(0, returnSst.size());

        } catch (BfepStpException e) {
            fail(e.getMessage());

        } catch (SQLException e) {
            fail(e.getMessage());

        } catch (Exception e) {
            fail(e.getMessage());
        }

    }

    /**
     * 【準正常】<br>
     * FTPサーバーにプリフィックス名を持つ.ENDが無い時の確認<br>
     */
    @Test
    public void processTestCase003() {

        // データソース
        OracleDataSource datasource;
        try {
            datasource = new OracleDataSource();

            FTPClient ftpClient = otherServerConnectionInfo(datasource);

            // ディレクトリ名
            String dirName = "/home/receiverCPExeIssTest/listFiles/notEndFile";

            // プリフィックスリスト
            List<String> prefixList = new ArrayList<String>();
            prefixList.add("HUBSTP201");
            prefixList.add("HUBSTP211");
            prefixList.add("HUBSTP202");
            prefixList.add("HUBSTP212");

            // 拡張子リスト
            List<String> extensionList = new ArrayList<String>();
            extensionList.add("end");
            extensionList.add("csv");

            target = new ReceiverCPExeIss(ftpClient, dirName, prefixList,
                    extensionList, datasource);

            // exchangeの生成
            Exchange exchange = createExchange();

            // 通常の最大値をとれるタスク№
            exchange.setFromRouteId("");

            exchange.getIn().setHeader("jp.co.nttdg.bfepstp.today", "20140425");

            // テスト実施
            Set<Map.Entry<String, byte[]>> returnSst = target.process(exchange);

            // 　要素ゼロのリストを取得
            assertEquals(0, returnSst.size());

        } catch (BfepStpException e) {

            fail(e.getMessage());

        } catch (SQLException e) {
            fail(e.getMessage());

        } catch (Exception e) {
            fail(e.getMessage());
        }

    }

    /**
     * 【準正常】<データバリエーション> <br>
     * FTPサーバーにプリフィックス名を持つ.ENDが無い時の確認<br>
     * <br>
     * （FTPサーバにプリフィックス名を持つ.ENDがFTPリストに３個はあるが、 プリフィックスの１番目のファイル名はない。）<br>
     * 
     */
    @Test
    public void processTestCase004() {

        // データソース
        OracleDataSource datasource;
        try {
            datasource = new OracleDataSource();

            FTPClient ftpClient = otherServerConnectionInfo(datasource);

            // ディレクトリ名
            String dirName = "/home/receiverCPExeIssTest/listFiles/notEndFile/DataVariation/First";

            // プリフィックスリスト
            List<String> prefixList = new ArrayList<String>();
            prefixList.add("HUBSTP001");
            prefixList.add("HUBSTP011");
            prefixList.add("HUBSTP002");
            prefixList.add("HUBSTP012");

            // 拡張子リスト
            List<String> extensionList = new ArrayList<String>();
            extensionList.add("end");
            extensionList.add("csv");

            target = new ReceiverCPExeIss(ftpClient, dirName, prefixList,
                    extensionList, datasource);

            // exchangeの生成
            Exchange exchange = createExchange();

            // 通常の最大値をとれるタスク№
            exchange.setFromRouteId("");

            exchange.getIn().setHeader("jp.co.nttdg.bfepstp.today", "20140422");

            // テスト実施
            Set<Map.Entry<String, byte[]>> returnSst = target.process(exchange);

            // 　要素ゼロのリストを取得
            assertEquals(0, returnSst.size());

        } catch (BfepStpException e) {
            List<BfepStpTestLogger.Message> list = this.logger.getMessageList();
            BfepStpTestLogger.Message message = list.get(0);

            // 検証
            assertEquals(2101, message.no);
            assertEquals(1, message.args.length);
            assertEquals("プリフィックスリスト", message.args[0]);

        } catch (SQLException e) {
            fail(e.getMessage());

        } catch (Exception e) {
            fail(e.getMessage());
        }

    }

    /**
     * 【準正常】<データバリエーション> <br>
     * FTPサーバーにプリフィックス名を持つ.ENDが無い時の確認<br>
     * <br>
     * （FTPサーバにプリフィックス名を持つ.ENDがFTPリストに３個はあるが、プリフィックスの最後のファイル名はない。）<br>
     * 
     */
    @Test
    public void processTestCase005() {

        // データソース
        OracleDataSource datasource;
        try {
            datasource = new OracleDataSource();

            FTPClient ftpClient = otherServerConnectionInfo(datasource);

            // ディレクトリ名
            String dirName = "/home/receiverCPExeIssTest/listFiles/notEndFile/DataVariation/Last";

            // プリフィックスリスト
            List<String> prefixList = new ArrayList<String>();
            prefixList.add("HUBSTP201");
            prefixList.add("HUBSTP211");
            prefixList.add("HUBSTP202");
            prefixList.add("HUBSTP212");

            // 拡張子リスト
            List<String> extensionList = new ArrayList<String>();
            extensionList.add("end");
            extensionList.add("csv");

            target = new ReceiverCPExeIss(ftpClient, dirName, prefixList,
                    extensionList, datasource);

            // exchangeの生成
            Exchange exchange = createExchange();

            // 通常の最大値をとれるタスク№
            exchange.setFromRouteId("");

            exchange.getIn().setHeader("jp.co.nttdg.bfepstp.today", "20140422");

            // テスト実施
            Set<Map.Entry<String, byte[]>> returnSst = target.process(exchange);

            // 　要素ゼロのリストを取得
            assertEquals(0, returnSst.size());

        } catch (BfepStpException e) {
            List<BfepStpTestLogger.Message> list = this.logger.getMessageList();
            BfepStpTestLogger.Message message = list.get(0);

            // 検証
            assertEquals(2101, message.no);
            assertEquals(1, message.args.length);
            assertEquals("プリフィックスリスト", message.args[0]);

        } catch (SQLException e) {
            fail(e.getMessage());

        } catch (Exception e) {
            fail(e.getMessage());
        }

    }

    /**
     * 【準正常】<br>
     * FTPサーバーにプリフィックス名を持つ取得対象ファイルが無い時の確認<br>
     */
    @Test
    public void processTestCase006() {

        // データソース
        OracleDataSource datasource;
        try {
            datasource = new OracleDataSource();

            FTPClient ftpClient = otherServerConnectionInfo(datasource);

            // ディレクトリ名
            String dirName = "/home/receiverCPExeIssTest/listFiles/notTergetFile";

            // プリフィックスリスト
            List<String> prefixList = new ArrayList<String>();
            prefixList.add("HUBSTP001");
            prefixList.add("HUBSTP011");
            prefixList.add("HUBSTP002");
            prefixList.add("HUBSTP012");

            // 拡張子リスト
            List<String> extensionList = new ArrayList<String>();
            extensionList.add("end");
            extensionList.add("csv");

            target = new ReceiverCPExeIss(ftpClient, dirName, prefixList,
                    extensionList, datasource);

            // exchangeの生成
            Exchange exchange = createExchange();

            // 通常の最大値をとれるタスク№
            exchange.setFromRouteId("");

            exchange.getIn().setHeader("jp.co.nttdg.bfepstp.today", "20140422");

            // テスト実施
            Set<Map.Entry<String, byte[]>> returnSst = target.process(exchange);

            // 　要素ゼロのリストを取得
            assertEquals(0, returnSst.size());

            List<BfepStpTestLogger.Message> list = this.logger.getMessageList();

            for (int index = 0; index < list.size(); index++) {

                BfepStpTestLogger.Message message = list.get(0);
                // 検証
                // 検証
                assertEquals(2101, message.no);
                assertEquals(1, message.args.length);
                assertEquals("HUBSTP001_20140422_0001.csv", message.args[0]);

            }

        } catch (BfepStpException e) {
            List<BfepStpTestLogger.Message> list = this.logger.getMessageList();
            for (int index = 0; index < list.size(); index++) {

                BfepStpTestLogger.Message message = list.get(0);
                // 検証
                // 検証
                assertEquals(2101, message.no);
                assertEquals(1, message.args.length);
                assertEquals("HUBSTP001_20140422_0001.csv", message.args[0]);

            }

        } catch (SQLException e) {
            e.printStackTrace();
            fail(e.getMessage());

        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

    }

    /**
     * 【準正常】<br>
     * FTPサーバーにプリフィックス名を持つ取得対象ファイルが無い時の確認<br>
     * （FTPサーバにプリフィックス名を持つ取得対象がFTPリストに３個はあるが、プリフィックスの１番目のファイル名はない。）<br>
     */
    @Test
    public void processTestCase007() {

        // データソース
        OracleDataSource datasource;
        try {
            datasource = new OracleDataSource();

            FTPClient ftpClient = otherServerConnectionInfo(datasource);

            // ディレクトリ名
            String dirName = "/home/receiverCPExeIssTest/listFiles/notTergetFile/DataVariation/First";

            // プリフィックスリスト
            List<String> prefixList = new ArrayList<String>();
            prefixList.add("HUBSTP001");
            prefixList.add("HUBSTP011");
            prefixList.add("HUBSTP002");
            prefixList.add("HUBSTP012");

            // 拡張子リスト
            List<String> extensionList = new ArrayList<String>();
            extensionList.add("end");
            extensionList.add("csv");

            target = new ReceiverCPExeIss(ftpClient, dirName, prefixList,
                    extensionList, datasource);

            // exchangeの生成
            Exchange exchange = createExchange();

            // 通常の最大値をとれるタスク№
            exchange.setFromRouteId("");

            exchange.getIn().setHeader("jp.co.nttdg.bfepstp.today", "20140425");

            // テスト実施
            Set<Map.Entry<String, byte[]>> returnSst = target.process(exchange);

            // 　要素ゼロのリストを取得
            assertEquals(0, returnSst.size());

        } catch (BfepStpException e) {
            List<BfepStpTestLogger.Message> list = this.logger.getMessageList();
            BfepStpTestLogger.Message message = list.get(0);

            // 検証
            assertEquals(2101, message.no);
            assertEquals(1, message.args.length);
            assertEquals("プリフィックスリスト", message.args[0]);

        } catch (SQLException e) {
            fail(e.getMessage());

        } catch (Exception e) {
            fail(e.getMessage());
        }

    }

    /**
     * 【準正常】<br>
     * FTPサーバーにプリフィックス名を持つ取得対象ファイルが無い時の確認<br>
     * （FTPサーバにプリフィックス名を持つ取得対象がFTPリストに３個はあるが、プリフィックスの最後のファイル名はない。）<br>
     */
    @Test
    public void processTestCase008() {

        // データソース
        OracleDataSource datasource;
        try {
            datasource = new OracleDataSource();

            FTPClient ftpClient = otherServerConnectionInfo(datasource);

            // ディレクトリ名
            String dirName = "/home/receiverCPExeIssTest/listFiles/notTergetFile/DataVariation/Last";

            // プリフィックスリスト
            List<String> prefixList = new ArrayList<String>();
            prefixList.add("HUBSTP001");
            prefixList.add("HUBSTP011");
            prefixList.add("HUBSTP002");
            prefixList.add("HUBSTP012");

            // 拡張子リスト
            List<String> extensionList = new ArrayList<String>();
            extensionList.add("end");
            extensionList.add("csv");

            target = new ReceiverCPExeIss(ftpClient, dirName, prefixList,
                    extensionList, datasource);

            // exchangeの生成
            Exchange exchange = createExchange();
            // 通常の最大値をとれるタスク№
            exchange.setFromRouteId("");

            exchange.getIn().setHeader("jp.co.nttdg.bfepstp.today", "20140425");

            // テスト実施
            Set<Map.Entry<String, byte[]>> returnSst = target.process(exchange);

            // 　要素ゼロのリストを取得
            assertEquals(0, returnSst.size());

        } catch (BfepStpException e) {
            List<BfepStpTestLogger.Message> list = this.logger.getMessageList();
            BfepStpTestLogger.Message message = list.get(0);

            // 検証
            assertEquals(2101, message.no);
            assertEquals(1, message.args.length);
            assertEquals("プリフィックスリスト", message.args[0]);

        } catch (SQLException e) {
            fail(e.getMessage());

        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    /**
     * 【異常】<br>
     * 重複チェックエラー発生時の確認<br>
     * DBのファイル通番が最新になっていることを確認<br>
     * Exchangeの整合性を確認<br>
     * （プリフィックスリスト：４件/拡張子リスト：２件）<br>
     * 
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    @Test
    public void processTestCase009() throws Exception {

        /*************************************************************************************/
        /* 【前提】 */
        /* タイムスタンプが一番古い.ENDファイルを HUBSTP001_20140422_0000.end とする */
        /*                                                                                   */
        /*************************************************************************************/

        String fileTsuban = "0001";

        // データソース
        OracleDataSource datasource;
        try {
            datasource = new OracleDataSource();

            FTPClient ftpClient = otherServerConnectionInfo(datasource);

            // ディレクトリ名
            String dirName = "/home/receiverCPExeIssTest/listFiles";

            // プリフィックスリスト
            List<String> prefixList = new ArrayList<String>();
            prefixList.add("HUBSTP001");
            prefixList.add("HUBSTP011");
            prefixList.add("HUBSTP002");
            prefixList.add("HUBSTP012");

            // 拡張子リスト
            List<String> extensionList = new ArrayList<String>();
            extensionList.add("end");
            extensionList.add("csv");

            target = new ReceiverCPExeIss(ftpClient, dirName, prefixList,
                    extensionList, datasource);

            // exchangeの生成
            Exchange exchange = createExchange();

            // 通常の最大値をとれるタスク№
            exchange.setFromRouteId("01");

            exchange.getIn().setHeader(
                    "jp.co.nttdg.bfep.stp.hub.message.systemCdFrom", "03");

            exchange.getIn().setHeader("jp.co.nttdg.bfepstp.today", "20140422");

            // テスト実施
            Set<Map.Entry<String, byte[]>> returnSst = target.process(exchange);

            // リストの存在確認
            // assertTrue("False:returnList not faund", (0 < returnSst.size()));

            // Exchangeの内容確認

            // ヘッダ部に作成した.ENDファイルリスト(削除ファイルリスト)を取得
            List<?> endFilesList = (List<?>) exchange.getIn().getHeader(
                    "jp.co.nttdg.bfep.stp.hub.ftp.deletePaths");

            for (String value : (List<String>) endFilesList) {

                // 内容は目視
                System.out.println(value);
            }

            // 引数で渡されたExchangeのin.headersにファイル通番を確認
            assertEquals(
                    fileTsuban,
                    exchange.getIn().getHeader(
                            "jp.co.nttdg.bfep.stp.hub.fileTsuban"));

            Connection con = target.getConnection();

            con.commit();
            con.close();

        } catch (BfepStpException e) {

            e.printStackTrace();

            List<BfepStpTestLogger.Message> list = this.logger.getMessageList();
            BfepStpTestLogger.Message message = list.get(0);

            // 検証
            assertEquals(2102, message.no);
            assertEquals(1, message.args.length);
            assertEquals("プリフィックスリスト", message.args[0]);

            System.out.println(message.args[0]);

        } catch (SQLException e) {

            e.printStackTrace();
            fail(e.getMessage());

        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

    }

    /**
     * 【異常】<br>
     * 昇順チェックエラー発生時の確認<br>
     * DBのファイル通番が最新になっていることを確認<br>
     * Exchangeの整合性を確認<br>
     * （プリフィックスリスト：４件/拡張子リスト：２件）<br>
     */
    @SuppressWarnings("unchecked")
    @Test
    public void processTestCase010() {

        /*************************************************************************************/
        /* 【前提】 */
        /* タイムスタンプが一番古い.ENDファイルを HUBSTP001_20140422_0000.end とする */
        /*                                                                                   */
        /*************************************************************************************/

        String fileTsuban = "0000";

        // データソース
        OracleDataSource datasource;
        try {
            datasource = new OracleDataSource();

            FTPClient ftpClient = otherServerConnectionInfo(datasource);

            // ディレクトリ名
            String dirName = "/home/receiverCPExeIssTest/listFiles/ASC";

            // プリフィックスリスト
            List<String> prefixList = new ArrayList<String>();
            prefixList.add("HUBSTP001");
            prefixList.add("HUBSTP011");
            prefixList.add("HUBSTP002");
            prefixList.add("HUBSTP012");

            // 拡張子リスト
            List<String> extensionList = new ArrayList<String>();
            extensionList.add("end");
            extensionList.add("csv");

            target = new ReceiverCPExeIss(ftpClient, dirName, prefixList,
                    extensionList, datasource);

            // exchangeの生成
            Exchange exchange = createExchange();

            // 通常の最大値をとれるタスク№
            exchange.setFromRouteId("01");

            exchange.getIn().setHeader(
                    "jp.co.nttdg.bfep.stp.hub.message.systemCdFrom", "03");

            exchange.getIn().setHeader("jp.co.nttdg.bfepstp.today", "20140422");

            // テスト実施
            Set<Map.Entry<String, byte[]>> returnSst = target.process(exchange);

            // リストの存在確認
            // assertTrue("False:returnList not faund", (0 < returnSst.size()));

            // Exchangeの内容確認

            // ヘッダ部に作成した.ENDファイルリスト(削除ファイルリスト)を取得
            List<?> endFilesList = (List<?>) exchange.getIn().getHeader(
                    "jp.co.nttdg.bfep.stp.hub.ftp.deletePaths");

            for (String value : (List<String>) endFilesList) {

                // 内容は目視
                System.out.println(value);
            }

            // 引数で渡されたExchangeのin.headersにファイル通番を確認
            assertEquals(
                    fileTsuban,
                    exchange.getIn().getHeader(
                            "jp.co.nttdg.bfep.stp.hub.fileTsuban"));

            Connection con = target.getConnection();

            con.commit();
            con.close();

        } catch (BfepStpException e) {

            e.printStackTrace();

        } catch (SQLException e) {
            e.printStackTrace();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 【異常】<br>
     * 歯抜けチェックエラー発生時の確認<br>
     * DBのファイル通番が最新になっていることを確認<br>
     * Exchangeの整合性を確認<br>
     * （プリフィックスリスト：４件/拡張子リスト：２件）<br>
     */
    @SuppressWarnings("unchecked")
    @Test
    public void processTestCase011() {

        /*************************************************************************************/
        /* 【前提】 */
        /* タイムスタンプが一番古い.ENDファイルを HUBSTP001_20140422_0002.end とする */
        /*                                                                                   */
        /*************************************************************************************/

        String fileTsuban = "0002";

        // データソース
        OracleDataSource datasource;
        try {
            datasource = new OracleDataSource();

            FTPClient ftpClient = otherServerConnectionInfo(datasource);

            // ディレクトリ名
            String dirName = "/home/receiverCPExeIssTest/listFiles/WHETHER/INSONRY";

            // プリフィックスリスト
            List<String> prefixList = new ArrayList<String>();
            prefixList.add("HUBSTP001");
            prefixList.add("HUBSTP011");
            prefixList.add("HUBSTP002");
            prefixList.add("HUBSTP012");

            // 拡張子リスト
            List<String> extensionList = new ArrayList<String>();
            extensionList.add("end");
            extensionList.add("csv");

            target = new ReceiverCPExeIss(ftpClient, dirName, prefixList,
                    extensionList, datasource);

            // exchangeの生成
            Exchange exchange = createExchange();

            // 通常の最大値をとれるタスク№
            exchange.setFromRouteId("01");

            exchange.getIn().setHeader(
                    "jp.co.nttdg.bfep.stp.hub.message.systemCdFrom", "03");

            exchange.getIn().setHeader("jp.co.nttdg.bfepstp.today", "20140422");

            Set<Map.Entry<String, byte[]>> returnSst = target.process(exchange);

            Connection con = target.getConnection();

            con.commit();
            con.close();

            // ディレクトリ名
            dirName = "/home/receiverCPExeIssTest/listFiles/WHETHER";

            ftpClient = otherServerConnectionInfo(datasource);

            target = new ReceiverCPExeIss(ftpClient, dirName, prefixList,
                    extensionList, datasource);

            // 通常の最大値をとれるタスク№
            // exchange.setFromRouteId("01");

            // exchange.getIn().setHeader(
            // "jp.co.nttdg.bfep.stp.hub.message.systemCdFrom", "03");

            // exchange.getIn().setHeader("jp.co.nttdg.bfepstp.today",
            // "20140422");

            // テスト
            returnSst = target.process(exchange);

            // リストの存在確認
            // assertTrue("False:returnList not faund", (0 < returnSst.size()));

            // Exchangeの内容確認

            // ヘッダ部に作成した.ENDファイルリスト(削除ファイルリスト)を取得
            List<?> endFilesList = (List<?>) exchange.getIn().getHeader(
                    "jp.co.nttdg.bfep.stp.hub.ftp.deletePaths");

            for (String value : (List<String>) endFilesList) {

                // 内容は目視
                System.out.println(value);
            }

            // 引数で渡されたExchangeのin.headersにファイル通番を確認
            assertEquals(
                    fileTsuban,
                    exchange.getIn().getHeader(
                            "jp.co.nttdg.bfep.stp.hub.fileTsuban"));

            con = target.getConnection();

            con.commit();
            con.close();

        } catch (BfepStpException e) {

            e.printStackTrace();

        } catch (SQLException e) {
            e.printStackTrace();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 【正常】<br>
     * インサート時におけるエラー（一意制約違反）<br>
     */
    @SuppressWarnings({ "unchecked", "null" })
    @Test
    public void processTestCase012() {

        /*************************************************************************************/
        /* 【前提】 */
        /* タイムスタンプが一番古い.ENDファイルを HUBSTP001_20140422_0001.end とする */
        /*                                                                                   */
        /*************************************************************************************/

        // データソース
        OracleDataSource datasource;

        Set<Map.Entry<String, byte[]>> returnSst = new HashSet<Map.Entry<String, byte[]>>();
        try {
            datasource = new OracleDataSource();

            FTPClient ftpClient = otherServerConnectionInfo(datasource);

            // ディレクトリ名
            String dirName = "/home/receiverCPExeIssTest/listFiles";

            // プリフィックスリスト
            List<String> prefixList = new ArrayList<String>();
            prefixList.add("HUBSTP001");
            prefixList.add("HUBSTP011");
            prefixList.add("HUBSTP002");
            prefixList.add("HUBSTP012");

            // 拡張子リスト
            List<String> extensionList = new ArrayList<String>();
            extensionList.add("end");
            extensionList.add("csv");

            target = new ReceiverCPExeIss(ftpClient, dirName, prefixList,
                    extensionList, datasource);

            // exchangeの生成
            Exchange exchange = createExchange();

            // 通常の最大値をとれるタスク№
            exchange.setFromRouteId("02");

            exchange.getIn().setHeader("jp.co.nttdg.bfepstp.today", "20140422");
            // テスト実施
            returnSst = target.process(exchange);

        } catch (BfepStpException e) {
            assertEquals(0, returnSst.size());

            List<BfepStpTestLogger.Message> list = this.logger.getMessageList();
            BfepStpTestLogger.Message message = list.get(0);

            // 検証
            assertEquals(4000, message.no);
            assertEquals(2, message.args.length);
            assertEquals(1400, message.args[0]);

        } catch (SQLException e) {

            e.printStackTrace();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 【異常】<br>
     * FTPClientにて例外発生<br>
     * ディレクトリの移動に失敗<br>
     */
    @SuppressWarnings("unchecked")
    @Test
    public void processTestCase013() {

        /*************************************************************************************/
        /* 【前提】 */
        /* タイムスタンプが一番古い.ENDファイルを HUBSTP001_20140422_0000.end とする */
        /*                                                                                   */
        /*************************************************************************************/

        String fileTsuban = "0001";

        // データソース
        OracleDataSource datasource;

        // ディレクトリ名
        String dirName = "/home/receiverCPExeIssTest/getFiles";

        try {
            datasource = new OracleDataSource();

            FTPClient ftpClient = otherServerConnectionInfo(datasource);

            // プリフィックスリスト
            List<String> prefixList = new ArrayList<String>();
            prefixList.add("HUBSTP001");
            prefixList.add("HUBSTP011");
            prefixList.add("HUBSTP002");
            prefixList.add("HUBSTP012");

            // 拡張子リスト
            List<String> extensionList = new ArrayList<String>();
            extensionList.add("end");
            extensionList.add("csv");

            target = new ReceiverCPExeIss(ftpClient, dirName, prefixList,
                    extensionList, datasource);

            // exchangeの生成
            Exchange exchange = createExchange();

            // 通常の最大値をとれるタスク№
            exchange.setFromRouteId("");

            exchange.getIn().setHeader("jp.co.nttdg.bfepstp.today", "20140425");

            // テスト実施
            Set<Map.Entry<String, byte[]>> returnSst = target.process(exchange);

        } catch (BfepStpException e) {

            List<BfepStpTestLogger.Message> list = this.logger.getMessageList();
            BfepStpTestLogger.Message message = list.get(0);

            // 検証
            assertEquals(5004, message.no);
            assertEquals(1, message.args.length);
            assertEquals(dirName, message.args[0]);

        } catch (SQLException e) {
            fail(e.getMessage());

        } catch (Exception e) {
            fail(e.getMessage());
        }

    }
    // TODO
}

