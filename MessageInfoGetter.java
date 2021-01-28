/*
 * @(#)MessageInfoGetter.java
 *
 * Copyright (c) 2014, NTT DATA Getronics Corporation.
 */
package jp.co.nttdg.bfep.stp.bean.hub;

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import jp.co.nttdg.bfep.stp.framework.BfepStpException;
import jp.co.nttdg.bfep.stp.framework.BfepStpLogger;
import jp.co.nttdg.bfep.stp.framework.BfepStpLoggerFactory;
import jp.co.nttdg.bfep.stp.framework.BfepStpMain;

import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

/**
 * 電文情報取得Bean
 * 
 * @author h_tozawa
 * @version 1.0
 * 
 */
public class MessageInfoGetter {

    /** トレースロガー */
    private static final Logger logger = LoggerFactory
            .getLogger(MessageInfoGetter.class);

    /** B-FEP STPロガー */
    private static final BfepStpLogger stplogger = BfepStpLoggerFactory
            .getLogger(MessageInfoGetter.class);

    /** XPath状態フラグ */
    private final boolean isXPath;

    /** 電文情報取得クラスマップ */
    private Map<String, BaseMessageInfo> baseMmessageInfoMap = new LinkedHashMap<String, BaseMessageInfo>();

    /**
     * コンストラクタ
     * 
     * @param messageInfoList
     *            取得情報リスト<BR>
     * @throws BfepStpException
     *             STPソリューション固有例外<BR>
     */
    public MessageInfoGetter(List<String> messageInfoList)
            throws BfepStpException {

        isXPath = this.setMessageInfo(messageInfoList, null);
    }

    /**
     * コンストラクタ
     * 
     * @param encode
     *            文字コード<BR>
     * @param messageInfoList
     *            取得情報リスト<BR>
     * @throws BfepStpException
     *             STPソリューション固有例外<BR>
     */
    public MessageInfoGetter(String encode, List<String> messageInfoList)
            throws BfepStpException {

        Charset charSet = null;

        try {
            charSet = Charset.forName(encode);

        } catch (Exception e) {

            logger.error("文字コード不正", e);
            stplogger.log(2052, encode, e.getMessage());
            throw new BfepStpException(e.getMessage(), e);

        }

        isXPath = this.setMessageInfo(messageInfoList, charSet);
    }

    /**
     * 電文情報を Exchange のheader部に格納する
     * 
     * @param exchange
     *            処理対象の Exchange<BR>
     * @throws BfepStpException
     *             STPソリューション固有例外<BR>
     */
    public void process(Exchange exchange) throws BfepStpException {

        // 電文情報初期化
        for (String key : baseMmessageInfoMap.keySet()) {

            exchange.getIn().setHeader(key, "");
        }

        Object telegram = exchange.getIn().getBody();

        // XPathの場合
        boolean isXPathError = false;
        if (isXPath) {
            try {
                DocumentBuilderFactory factory = DocumentBuilderFactory
                        .newInstance();
                factory.setNamespaceAware(false);
                factory.setValidating(true);
                DocumentBuilder builder = factory.newDocumentBuilder();
                telegram = builder.parse(new ByteArrayInputStream(
                        (byte[]) telegram));
            } catch (Exception e) {

                logger.error("DOM変換失敗", e);
                stplogger.routeLog(2053, exchange, e.getMessage());
                isXPathError = true;
            }
        }

        // 電文情報取得
        for (String key : baseMmessageInfoMap.keySet()) {

            String value = null;
            try {
                value = baseMmessageInfoMap.get(key)
                        .getValue((Object) telegram);
            } catch (Exception e) {

                if (!isXPathError) {
                    logger.error("電文情報取得失敗", e);
                    stplogger.routeLog(2054, exchange, key, e.getMessage());
                }
                value = "";
            }

            exchange.getIn().setHeader(key, value);
        }
    }

    /**
     * 電文情報設定
     * 
     * @param messageInfoList
     *            取得情報リスト<BR>
     * @param charSet
     *            キャラセット<BR>
     * @throws BfepStpException
     *             STPソリューション固有例外<BR>
     */
    private boolean setMessageInfo(List<String> messageInfoList, Charset charSet)
            throws BfepStpException {

        int xPathNum = 0;
        for (int index = 0; index < messageInfoList.size(); index++) {

            String[] messageInfoArray = null;
            int pattern = -1;
            try {

                messageInfoArray = messageInfoList.get(index).split("\\|", 3);
                if (messageInfoArray.length != 3) {
                    stplogger.log(2050, index, messageInfoList.get(index));
                    throw new BfepStpException();
                }

                pattern = Integer.valueOf(messageInfoArray[0]);
                if (charSet == null) {

                    // 種別が固定長
                    if (pattern == 2) {
                        stplogger.log(2050, index, messageInfoList.get(index));
                        throw new BfepStpException();
                    }
                } else {

                    // 種別がXPath
                    if (pattern == 1) {
                        stplogger.log(2050, index, messageInfoList.get(index));
                        throw new BfepStpException();
                    }
                }

                // 重複チェック（パラメータ(Key)がすでに存在している）
                if (this.baseMmessageInfoMap.containsKey(messageInfoArray[1])) {
                    stplogger.log(2050, index, messageInfoList.get(index));
                    throw new BfepStpException();
                }

                // 種別ごとに、情報取得取クラスのインスタンスをセットする
                switch (pattern) {
                case 0:

                    baseMmessageInfoMap.put(messageInfoArray[1],
                            new FixedValueMessageInfo(messageInfoArray[2]));
                    break;

                case 1:

                    baseMmessageInfoMap.put(messageInfoArray[1],
                            new XPathMessageInfo(messageInfoArray[2]));
                    xPathNum++;
                    break;

                case 2:

                    baseMmessageInfoMap.put(messageInfoArray[1],
                            new FixedLengthMessageInfo(messageInfoArray[2],
                                    charSet));
                    break;
                default:
                    stplogger.log(2050, index, messageInfoList.get(index));
                    throw new BfepStpException();
                }
            } catch (BfepStpException be) {
                throw be;
            } catch (Exception e) {

                logger.error("電文情報設定失敗", e);
                stplogger.log(2051, index, messageInfoList.get(index),
                        e.getMessage());
                throw new BfepStpException(e.getMessage(), e);
            }
        }

        return xPathNum == 0 ? false : true;
    }

    /**
     * インナークラス 情報取得基底クラス
     * 
     */
    abstract class BaseMessageInfo {

        /**
         * 電文情報取得
         * 
         * @param data
         *            データ<BR>
         * @return 電文情報<BR>
         * @throws Exception
         *             基底例外<BR>
         */
        public abstract String getValue(Object data) throws Exception;
    }

    /** インナークラス 固定値情報取得基底クラス */
    class FixedValueMessageInfo extends BaseMessageInfo {

        /** 固定値 */
        private String fixedValue;

        /**
         * コンストラクタ
         * 
         * @param value
         *            値<BR>
         */
        public FixedValueMessageInfo(String value) {

            this.fixedValue = value;
        }

        /** 固定値電文情報取得 */
        @Override
        public String getValue(Object data) throws BfepStpException {

            return this.fixedValue;
        }
    }

    /** インナークラス XPath情報取得基底クラス */
    class XPathMessageInfo extends BaseMessageInfo {

        /** コンパイル済み XPath式 */
        XPathExpression expression;

        /**
         * コンストラクタ
         * 
         * @param value
         *            値<BR>
         * @throws Exception
         *             基底例外<BR>
         */
        public XPathMessageInfo(String value) throws Exception {

            // 渡された値をコンパイルしてメンバに保持
            String factoryClassName = System.getProperty(
                    BfepStpMain.XPATH_FACTORY_PROPERTY_NAME, "");
            XPathFactory factory;
            factory = XPathFactory.newInstance(
                    XPathFactory.DEFAULT_OBJECT_MODEL_URI, factoryClassName,
                    null);
            XPath xpath = factory.newXPath();
            this.expression = xpath.compile(value);
        }

        /**
         * XPath電文情報取得
         * 
         * @return XPath式<BR>
         * @throws Exception
         *             基底例外<BR>
         */
        @Override
        public String getValue(Object data) throws Exception {

            return expression.evaluate((Document) data);
        }
    }

    /** インナークラス 固定長情報取得基底クラス */
    class FixedLengthMessageInfo extends BaseMessageInfo {

        /** オフセット */
        private int offset;

        /** レングス */
        private int length;

        /** 文字コード */
        private Charset charSet = null;

        /**
         * コンストラクタ
         * 
         * @param value
         *            値
         * @param charSet
         *            文字コード
         * @throws Exception
         *             基底例外<BR>
         */
        public FixedLengthMessageInfo(String value, Charset charSet)
                throws Exception {

            String[] valueArray = value.split(",");

            // 値を２つに分割できないとき
            if (valueArray.length != 2) {
                throw new Exception();
            }

            // 数値型に変換
            int[] itemArray = new int[valueArray.length];
            for (int i = 0; i < valueArray.length; i++) {

                // NumberFormatExceptionはそのまま例外が投げられる
                itemArray[i] = Integer.valueOf(valueArray[i]);
                if (itemArray[i] < 0) {
                    // 値が負の整数のとき
                    throw new Exception();
                }
            }
            // それそれメンバに格納
            this.offset = itemArray[0];
            this.length = itemArray[1];

            // 文字コードをメンバに格納
            this.charSet = charSet;
        }

        /**
         * 固定長電文情報取得
         * 
         * @throws Exception
         *             基底例外<BR>
         */
        @Override
        public String getValue(Object data) throws Exception {
            return new String((byte[]) data, this.offset, this.length,
                    this.charSet);
        }
    }
}

