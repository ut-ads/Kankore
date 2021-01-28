/*
 * @(#)RooteLogger.java
 *
 * Copyright (c) 2014, NTT DATA Getronics Corporation.
 */
package jp.co.nttdg.bfep.stp.bean.hub;

import java.util.ArrayList;
import java.util.List;

import jp.co.nttdg.bfep.stp.bean.hub.RouteLogger;
import jp.co.nttdg.bfep.stp.framework.BfepStpException;
import jp.co.nttdg.bfep.stp.framework.BfepStpLoggerFactory;
import jp.co.nttdg.bfep.stp.framework.BfepStpTestLogger;

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
public class RouteLoggerTest extends ExchangeTestSupport {

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
    RouteLogger target = new RouteLogger();

    /**
     * 正常データテスト 埋め込みパラメータが１つ
     * 
     * @throws Exception
     * 
     * @throws BfepStpException
     */
    @Test
    public void testCase01() throws Exception {

        // Exchangeの設定

        Exchange exchange = createExchange();
        exchange.getIn().setHeader("jp.co.nttdg.bfep.stp.hub.log.messageNo",
                1000);

        ArrayList<Object> targetList = new ArrayList<Object>();

        String testMessages = "正常データテスト";

        targetList.add(testMessages);

        exchange.getIn().setHeader("jp.co.nttdg.bfep.stp.hub.log.messageParam",
                targetList);

        target.process(exchange);

        List<BfepStpTestLogger.Message> list = this.logger.getMessageList();
        BfepStpTestLogger.Message message = list.get(0);
        assertEquals(1000, message.no);
        assertEquals(1, message.args.length);

        int i = 0;
        for (Object obj : message.args) {
            assertEquals(targetList.get(i), (String) obj);
            i++;
        }

    }

    /**
     * 正常データテスト埋め込みパラメータが３つ
     * 
     * @throws Exception
     */
    @Test
    public void testCase02() throws Exception {

        // Exchangeの設定
        Exchange exchange = createExchange();
        exchange.getIn().setHeader("jp.co.nttdg.bfep.stp.hub.log.messageNo",
                1000);

        ArrayList<Object> targetList = new ArrayList<Object>();

        targetList.add("正常データリスト１");
        targetList.add("正常データリスト２");
        targetList.add("正常データリスト３");

        exchange.getIn().setHeader("jp.co.nttdg.bfep.stp.hub.log.messageParam",
                targetList);

        target.process(exchange);

        List<BfepStpTestLogger.Message> list = this.logger.getMessageList();
        BfepStpTestLogger.Message message = list.get(0);
        assertEquals(1000, message.no);
        assertEquals(targetList.size(), message.args.length);

        int i = 0;
        for (Object obj : message.args) {
            assertEquals(targetList.get(i), (String) obj);
            i++;
        }

    }

    /**
     * 正常データテスト埋め込みパラメータが５つ
     * 
     * @throws Exception
     */
    @Test
    public void testCase03() throws Exception {

        // Exchangeの設定
        Exchange exchange = createExchange();
        exchange.getIn().setHeader("jp.co.nttdg.bfep.stp.hub.log.messageNo",
                1000);

        ArrayList<Object> targetList = new ArrayList<Object>();

        targetList.add("埋め込みパラメータ１");
        targetList.add("埋め込みパラメータ２");
        targetList.add("埋め込みパラメータ３");
        targetList.add("埋め込みパラメータ４");
        targetList.add("埋め込みパラメータ５");

        exchange.getIn().setHeader("jp.co.nttdg.bfep.stp.hub.log.messageParam",
                targetList);

        target.process(exchange);

        List<BfepStpTestLogger.Message> list = this.logger.getMessageList();
        BfepStpTestLogger.Message message = list.get(0);
        assertEquals(1000, message.no);
        assertEquals(targetList.size(), message.args.length);

        int i = 0;
        for (Object obj : message.args) {
            assertEquals(targetList.get(i), (String) obj);
            i++;
        }

    }

    /**
     * 正常データテスト 埋め込みパラメータなし
     * 
     * @throws Exception
     */
    @Test
    public void testCase04() throws Exception {

        // Exchangeの設定
        Exchange exchange = createExchange();
        exchange.getIn().setHeader("jp.co.nttdg.bfep.stp.hub.log.messageNo",
                1000);

        ArrayList<Object> targetList = new ArrayList<Object>();

        exchange.getIn().setHeader("jp.co.nttdg.bfep.stp.hub.log.messageParam",
                targetList);

        target.process(exchange);

        List<BfepStpTestLogger.Message> list = this.logger.getMessageList();
        BfepStpTestLogger.Message message = list.get(0);
        assertEquals(1000, message.no);
        assertEquals(targetList.size(), message.args.length);

        int i = 0;
        for (Object obj : message.args) {
            assertEquals(targetList.get(i), (String) obj);
            i++;
        }

    }

    /**
     * 異常データテスト メッセージナンバープロパティなし
     */
    @Test
    public void testCase05() {

        // Exchangeの設定
        Exchange exchange = createExchange();
        exchange.getIn().setHeader("jp.co.nttdg.bfep.stp.hub.log.noMessageNo",
                1000);

        ArrayList<Object> targetList = new ArrayList<Object>();

        targetList.add("埋め込みパラメータ１");
        targetList.add("埋め込みパラメータ２");
        targetList.add("埋め込みパラメータ３");

        exchange.getIn().setHeader("jp.co.nttdg.bfep.stp.hub.log.messageParam",
                targetList);

        try {
            target.process(exchange);

        } catch (Exception e) {

            List<BfepStpTestLogger.Message> list = this.logger.getMessageList();
            assertEquals(0, list.size());

        }
    }

    /**
     * 正常データテスト リストプロパティなし
     */
    @Test
    public void testCase06() {

        // Exchangeの設定
        Exchange exchange = createExchange();
        exchange.getIn().setHeader("jp.co.nttdg.bfep.stp.hub.log.messageNo",
                1000);

        ArrayList<Object> targetList = new ArrayList<Object>();

        targetList.add("プロパティなしリスト");

        exchange.getIn().setHeader(
                "jp.co.nttdg.bfep.stp.hub.log.noPropertyParam", targetList);

        try {
            target.process(exchange);

        } catch (Exception e) {

            List<BfepStpTestLogger.Message> list = this.logger.getMessageList();
            assertEquals(0, list.size());

        }
    }

    /**
     * 異常データテスト　メッセージ番号が数字に変換できない。
     */
    @Test
    public void testCase07() {

        // Exchangeの設定
        Exchange exchange = createExchange();
        exchange.getIn().setHeader("jp.co.nttdg.bfep.stp.hub.log.messageNo",
                "100%");

        ArrayList<Object> targetList = new ArrayList<Object>();

        targetList.add("正常データリスト１");
        targetList.add("正常データリスト２");
        targetList.add("正常データリスト３");
        targetList.add("正常データリスト４");
        targetList.add("正常データリスト５");

        exchange.getIn().setHeader("jp.co.nttdg.bfep.stp.hub.log.messageParam",
                targetList);

        try {
            target.process(exchange);
        } catch (Exception e) {

            List<BfepStpTestLogger.Message> list = this.logger.getMessageList();
            assertEquals(0, list.size());

        }
    }
}

