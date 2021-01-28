/*
 * @(#)SplitterCP.java
 *
 * Copyright (c) 2014, NTT DATA Getronics Corporation.
 */
package jp.co.nttdg.bfep.stp.bean.hub;

import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import jp.co.nttdg.bfep.stp.framework.BfepStpException;
import jp.co.nttdg.bfep.stp.framework.BfepStpLogger;
import jp.co.nttdg.bfep.stp.framework.BfepStpLoggerFactory;

import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 電文分割(電子CP)Bean<BR>
 *
 * @author h_tozawa
 * @version 1.0
 *
 */
public class SplitterCP {

    /** トレースロガー */
    private static final Logger logger = LoggerFactory
            .getLogger(SplitterCP.class);

    /** B-FEP STPロガー */
    private static final BfepStpLogger stplogger = BfepStpLoggerFactory
            .getLogger(SplitterCP.class);

    /** データソース */
    private final DataSource datasource;

    /** DB用APLID */
    private final String aplId;

    /** キャラセット */
    private final Charset charset;

    /*
     * 定数定義
     */
    /** 分割用文字列（デリミタ） */
    private static final String SPLIT_DELIMITER = "<\\?xml";

    /** 追加用文字列（宣言部） */
    private static final String ADD_DECLARATION = "<?xml";

    /** デリミタエラーステータス */
    private static final String SATAUS_ERROR_DELIMITER_SPLIT = "D";

    /** 正常時(エラーなし)テータス */
    private static final String SATAUS_NORMAL = "0";

    /** システム区分コードFROM */
    private static final String PARAMETER_NAME_CD_FROM = "jp.co.nttdg.bfep.stp.hub.message.systemCdFrom";

    /** ファイル通番 */
    private static final String PARAMETER_NAME_FILE_TSUBAN = "jp.co.nttdg.bfep.stp.hub.fileTsuban";

    /** ファイル名 */
    private static final String PARAMETER_NAME_FILE_NAME = "jp.co.nttdg.bfep.stp.hub.fileName";

    /**
     * コンストラクタ
     *
     * @param charset
     *            キャラセット<BR>
     * @param datasource
     *            データソース<BR>
     * @param dbAplID
     *            DB用APLID<BR>
     * @throws BfepStpException
     *            STPソリューション固有例外<BR>
     */
    public SplitterCP(String charset, DataSource datasource, String dbAplID)
            throws BfepStpException {

        try {
            this.charset = Charset.forName(charset);
            this.datasource = datasource;
            this.aplId = dbAplID;

        } catch (Exception e) {

            logger.error("文字コード不正", e);
            stplogger.log(2119, charset, e.getMessage());
            throw new BfepStpException(e.getMessage(), e);
        }
    }

    /**
     * プロセス
     *
     * @param exchange
     *            処理対象の Exchange<BR>
     * @return
     *            分割データのバイト配列リスト
     * @throws BfepStpException
     *            STPソリューション固有例外<BR>
     */
    public List<byte[]> process(Exchange exchange) throws BfepStpException {

        List<byte[]> returnByteArray = new ArrayList<byte[]>();

        List<String> returnList = null;

        String status = SATAUS_NORMAL;

        // データの取得
        byte[] inBodyData = (byte[]) exchange.getIn().getBody();

        // データが存在する時
        if (inBodyData.length > 0) {

            // 文字列に変換して、分割
            String xml = new String(inBodyData, this.charset);
            returnList = xmlSprit(xml);

            // データが <?xml で始まらない
            if (returnList.size() < 1) {

                stplogger.routeLog(2106, exchange,
                        exchange.getIn().getHeader(PARAMETER_NAME_FILE_NAME));
                status = SATAUS_ERROR_DELIMITER_SPLIT;
            } else {
                // 戻り値に値を設定する。
                for (String item : returnList) {

                    byte[] bytesItem = item.getBytes(this.charset);
                    returnByteArray.add(bytesItem);
                }
            }
        }
        // ファイル管理テーブル追加
        addMessageFileManagementTable(exchange, status);

        // 戻り値として、バイト配列を戻す。
        return returnByteArray;
    }

    /**
     * XMLをデリミタ単位で分割
     *
     * @param xml
     *            XML<BR>
     * @return
     *            分割した値のリスト<BR>
     */
    private List<String> xmlSprit(String xml) {

        List<String> returnList = new ArrayList<String>();

        String[] xmlArray = xml.split(SPLIT_DELIMITER);

        for (int index = 0; index < xmlArray.length; index++) {

            String items = xmlArray[index];

            // 分割した最初の配列
            if (index == 0) {

                items = items.replaceAll("\n", "");
                items = items.replaceAll("\t", "");
                items = items.replaceAll("\\s", "");

                // データが <?xml で始まらない時は、分割リストは不要
                if (items.length() > 0) {
                    break;
                }
                continue;
            }

            returnList.add(ADD_DECLARATION + items);

        }

        return returnList;
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

