/*
 * @(#)SplitterYen.java
 *
 * Copyright (c) 2014, NTT DATA Getronics Corporation.
 */
package jp.co.nttdg.bfep.stp.bean.hub;

import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;

import javax.sql.DataSource;

import jp.co.nttdg.bfep.stp.framework.BfepStpException;
import jp.co.nttdg.bfep.stp.framework.BfepStpLogger;
import jp.co.nttdg.bfep.stp.framework.BfepStpLoggerFactory;

import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 電文分割(円資金管理)Bean<BR>
 *
 * @author h_tozawa
 * @version 1.0
 *
 */
public class SplitterYen {

    /** トレースロガー */
    private static final Logger logger = LoggerFactory
            .getLogger(SplitterYen.class);

    /** B-FEP STPロガー */
    private static final BfepStpLogger stplogger = BfepStpLoggerFactory
            .getLogger(SplitterYen.class);

    /** 電文長 */
    private final int telegramLength;

    /** 共通情報終端位置 */
    private final int comInfoLastIndex;

    /** スタート取引電文終端位置 */
    private final int startTelegramLastIndex;

    /** エンド取引判定項目オフセット */
    private final int endTelegramOffset;

    /** エンド取引判定項目レングス */
    private final int endTelegramLength;

    /** データソース */
    private final DataSource datasource;

    /** DB用APLID */
    private final String aplId;

    /*
     * 定数定義
     */
    /** 電文長エラーステータス */
    private static final String SATAUS_ERROR_TELEGRAM_SIZE = "D";

    /** 正常時(エラーなし)テータス */
    private static final String SATAUS_NORMAL = "0";

    /** 結合用定数 「0」 */
    private static final char BIND_START_ONLY = '0';

    /** 結合用定数 「1」 */
    private static final char BIND_START = '1';

    /** 結合用定数 「2」 */
    private static final char BIND_END = '2';

    /** メッセージ スタート取引 */
    private static final String PARAMETER_NAME_MESSAGE_BODY_START = "jp.co.nttdg.bfep.stp.hub.message.body.start";

    /** 電文分割エラー */
    private static final String PARAMETER_NAME_SPLIT_ERROR = "jp.co.nttdg.bfep.stp.hub.splitError";

    /** メッセージ エンド取引 */
    private static final String PARAMETER_NAME_MESSAGE_BODY_END = "jp.co.nttdg.bfep.stp.hub.message.body.end";

    /** ファイル名 */
    private static final String PARAMETER_NAME_FILE_NAME = "jp.co.nttdg.bfep.stp.hub.fileName";

    /** システム区分コードFROM */
    private static final String PARAMETER_NAME_CD_FROM = "jp.co.nttdg.bfep.stp.hub.message.systemCdFrom";

    /** ファイル通番 */
    private static final String PARAMETER_NAME_FILE_TSUBAN = "jp.co.nttdg.bfep.stp.hub.fileTsuban";

    /**
     * コンストラクタ<BR>
     *
     * @param telegramSize
     *            電文長<BR>
     * @param comInfoLastIndex
     *            共通情報終端位置<BR>
     * @param startTelegramLastIndex
     *            スタート取引電文終端位置<BR>
     * @param endTelegramOffset
     *            エンド取引判定項目オフセット<BR>
     * @param endTelegramSize
     *            エンド取引判定項目レングス<BR>
     * @param datasource
     *            データソース<BR>
     * @param aplId
     *            DB用APLID<BR>
     * @throws BfepStpException
     *            STPソリューション固有例外<BR>
     */
    public SplitterYen(int telegramSize, int comInfoLastIndex,
            int startTelegramLastIndex, int endTelegramOffset,
            int endTelegramSize, DataSource datasource, String aplId)
            throws BfepStpException {

        this.telegramLength = telegramSize;
        this.comInfoLastIndex = comInfoLastIndex;
        this.startTelegramLastIndex = startTelegramLastIndex;
        this.endTelegramOffset = endTelegramOffset;
        this.endTelegramLength = endTelegramSize;
        this.datasource = datasource;
        this.aplId = aplId;

        // 電文サイズチェック
        // 共通情報終端位置が電文長より大きい
        if (this.telegramLength < this.comInfoLastIndex) {
            stplogger.log(2107, this.telegramLength, "共通情報終端位置",
                    this.comInfoLastIndex);
            throw new BfepStpException();
        }

        // スタート取引電文終端位置が電文長より大きい
        if (this.telegramLength < this.startTelegramLastIndex) {
            stplogger.log(2107, this.telegramLength, "スタート取引電文終端位置",
                    this.startTelegramLastIndex);
            throw new BfepStpException();
        }

        // エンド取引判定項目オフセットが電文長より大きい
        if (this.telegramLength < this.endTelegramOffset) {
            stplogger.log(2107, this.telegramLength, "エンド取引判定項目オフセット",
                    this.endTelegramOffset);
            throw new BfepStpException();
        }

        // エンド取引判定項目オフセット + エンド取引判定項目レングスが電文長より大きい
        if (this.telegramLength < (this.endTelegramOffset + this.endTelegramLength)) {
            stplogger.log(2107, this.telegramLength,
                    "エンド取引判定項目オフセット + エンド取引判定項目レングス",
                    (this.endTelegramOffset + this.endTelegramLength));
            throw new BfepStpException();
        }
    }

    /**
     * プロセス<BR>
     *
     * @param exchange
     *            処理対象の Exchange<BR>
     * @throws BfepStpException
     *            STPソリューション固有例外<BR>
     */
    public void process(Exchange exchange) throws BfepStpException {

        exchange.getIn().setHeader(PARAMETER_NAME_SPLIT_ERROR, false);

        byte[] bodyDataArray = new byte[0];

        exchange.getIn().setHeader(PARAMETER_NAME_MESSAGE_BODY_START,
                bodyDataArray);

        exchange.getIn().setHeader(PARAMETER_NAME_MESSAGE_BODY_END,
                bodyDataArray);

        bodyDataArray = (byte[]) exchange.getIn().getBody();
        int bodyDataSize = bodyDataArray.length;

        // 引数指定の電文長と実際の電文長が違う
        if (bodyDataSize != this.telegramLength) {

            String fileName = (String) exchange.getIn().getHeader(
                    PARAMETER_NAME_FILE_NAME);

            stplogger.routeLog(2108, exchange, fileName, bodyDataSize,
                    this.telegramLength);

            exchange.getIn().setHeader(PARAMETER_NAME_SPLIT_ERROR, true);

            // ファイル管理テーブル追加
            addMessageFileManagementTable(exchange, SATAUS_ERROR_TELEGRAM_SIZE);

            // 処理終了
            return;
        }

        // 判定項目の取得
        byte[] judgeItemsArray = Arrays.copyOfRange(bodyDataArray,
                this.endTelegramOffset,
                (this.endTelegramOffset + this.endTelegramLength));

        // 取引種別初期化
        char type;

        // 判定項目のチェック
        if (!this.existsEndTeleglam(judgeItemsArray)) {

            // スタート取引の末尾に種別を付与
            type = BIND_START_ONLY;
        } else {

            // スタート取引の末尾に種別を付与
            type = BIND_START;

            // エンド取引電文の末尾に「2」を付与し、Exchangeにセット
            // 共通情報
            byte[] comInfo = Arrays.copyOf(bodyDataArray, comInfoLastIndex + 1);

            // エンド取引電文生成
            int endTelegramStartIndex = this.startTelegramLastIndex + 1;
            byte[] endDataArray = Arrays.copyOfRange(bodyDataArray,
                    endTelegramStartIndex, bodyDataArray.length);

            ByteBuffer byteBuf = ByteBuffer.allocate(comInfo.length
                    + endDataArray.length + 1);
            byteBuf.put(comInfo);
            byteBuf.put(endDataArray);
            byteBuf.put((byte) BIND_END);
            byte[] endTelegramDataArray = byteBuf.array();

            exchange.getIn().setHeader(PARAMETER_NAME_MESSAGE_BODY_END,
                    endTelegramDataArray);
        }

        // スタート取引電文生成
        byte[] startTelegramDataArray = Arrays.copyOf(bodyDataArray,
                this.startTelegramLastIndex + 1);

        // スタート取引電文の末尾に「1」を付与し、Exchangeにセット
        ByteBuffer byteBuf = ByteBuffer
                .allocate(startTelegramDataArray.length + 1);
        byteBuf.put(startTelegramDataArray);
        byteBuf.put((byte) type);
        byte[] newBodyDataArray = byteBuf.array();
        exchange.getIn().setHeader(PARAMETER_NAME_MESSAGE_BODY_START,
                newBodyDataArray);

        // ファイル管理テーブル追加
        addMessageFileManagementTable(exchange, SATAUS_NORMAL);
    }

    /**
     * 判定項目が一文字でも半角スペースで無い時にTrueを返す
     *
     * @param judgeItemsArray
     *            判定項目<BR>
     * @return
     *            一文字でもスペースで無い時にはtrueを返す。<BR>
     */
    private boolean existsEndTeleglam(byte[] judgeItemsArray) {

        boolean isNotSpace = false;

        for (byte judgeItems : judgeItemsArray) {
            if (judgeItems != (byte) ' ') {
                isNotSpace = true;
                break;
            }
        }

        return isNotSpace;
    }

    /**
     * 電文ファイル管理テーブルに追加する。
     *
     * @param exchange
     *            処理対象の Exchange<BR>
     * @param fileTsuban
     *            ファイル通番<BR>
     * @param status
     *            ステータス<BR>
     * @throws BfepStpException
     *            STPソリューション固有例外<BR>
     */
    private void addMessageFileManagementTable(Exchange exchange, String status)
            throws BfepStpException {

        try {

            Connection conn = this.datasource.getConnection();
            try {

                PreparedStatement pstmt = conn
                        .prepareStatement("INSERT INTO DENBUN_FILE_MGR (TASK_NO, SYSTEM_CD, FILE_TSUBAN, FILE_NAME, STATUS, INS_TIMESTAMP, INS_APLID) "
                                + "VALUES (?, ?, ?, ?, ?, SYSDATE, ?)");
                try {

                    // タスク№
                    pstmt.setString(1, exchange.getFromRouteId());

                    // システム区分コード
                    pstmt.setString(
                            2,
                            (String) exchange.getIn().getHeader(
                                    PARAMETER_NAME_CD_FROM));

                    // ファイル通番
                    pstmt.setInt(
                            3,
                            (Integer) exchange.getIn().getHeader(
                                    PARAMETER_NAME_FILE_TSUBAN));

                    // ファイル名
                    pstmt.setString(
                            4,
                            (String) exchange.getIn().getHeader(
                                    PARAMETER_NAME_FILE_NAME));

                    // ステータス
                    pstmt.setString(5, status);

                    // 登録APLID
                    pstmt.setString(6, this.aplId);

                    // SQL実行
                    pstmt.executeUpdate();
                } finally {
                    pstmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException e) {

            logger.error("INSERT失敗", e);
            stplogger
                    .routeLog(4000, exchange, e.getErrorCode(), e.getMessage());
            throw new BfepStpException(e.getMessage(), e);
        }
    }
}

