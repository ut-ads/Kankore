/*
 * @(#)RouteLogger.java
 *
 * Copyright (c) 2014, NTT DATA Getronics Corporation.
 */
package jp.co.nttdg.bfep.stp.bean.hub;

import java.util.ArrayList;

import jp.co.nttdg.bfep.stp.framework.BfepStpLogger;
import jp.co.nttdg.bfep.stp.framework.BfepStpLoggerFactory;

import org.apache.camel.Exchange;

/**
 * ログ出力Bean
 *
 * @author h_tozawa
 * @version 1.0
 */
public class RouteLogger {

    /** B-FEP STPロガー */
    private static final BfepStpLogger stplogger = BfepStpLoggerFactory
            .getLogger(RouteLogger.class);

    /*
     * 定数定義
     */

    /** メッセージ番号 */
    private static final String PARAMETER_NAME_MESSAGE_NO = "jp.co.nttdg.bfep.stp.hub.log.messageNo";

    /** メッセージパラメータ */
    private static final String PARAMETER_NAME_MESSAGE_PARAM = "jp.co.nttdg.bfep.stp.hub.log.messageParam";

    /**
     * ルートログ出力処理
     *
     * @param exchange
     *            処理対象の Exchange
     * @throws Exception
     *            基底の例外<BR>
     */
    public void process(Exchange exchange) throws Exception {

        // メッセージ番号の取得
        int messageNo = (Integer) exchange.getIn().getHeader(
                PARAMETER_NAME_MESSAGE_NO);

        // 埋め込みパラメータの取得
        ArrayList<?> embeddingParamList = (ArrayList<?>) exchange.getIn()
                .getHeader(PARAMETER_NAME_MESSAGE_PARAM);

        // ArrayListをObject配列に変換する
        if (embeddingParamList == null) {
            embeddingParamList = new ArrayList<Object>();
        }

        Object[] embeddingParamArray = (Object[]) embeddingParamList
                .toArray(new Object[0]);

        // ログ出力
        stplogger.routeLog(messageNo, exchange, embeddingParamArray);
    }
}

