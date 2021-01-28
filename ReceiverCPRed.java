/*
 * @(#)ReceiverCPRed.java
 *
 * Copyright (c) 2014, NTT DATA Getronics Corporation.
 */
package jp.co.nttdg.bfep.stp.bean.hub;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import jp.co.nttdg.bfep.stp.framework.BfepStpException;
import jp.co.nttdg.bfep.stp.framework.BfepStpLogger;
import jp.co.nttdg.bfep.stp.framework.BfepStpLoggerFactory;

import org.apache.camel.Exchange;
import org.apache.commons.net.ftp.FTPFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 電文受信(電子CP(償還))Bean
 *
 * @author h_tozawa
 * @version 1.0
 *
 */
public class ReceiverCPRed {

    /** トレースロガー */
    private static final Logger logger = LoggerFactory
            .getLogger(ReceiverCPRed.class);

    /** B-FEP STPロガー */
    private static final BfepStpLogger stplogger = BfepStpLoggerFactory
            .getLogger(ReceiverCPRed.class);

    /** FTPクライアントBean */
    private final FTPClient ftpClieant;

    /** ファイル配置場所 */
    private final String dirPath;

    /** プレフィックス */
    private final String prefix;

    /** 拡張子リスト */
    private final List<String> extensionList;

    /** データソース */
    private final DataSource datasource;

    /** DB用APLID */
    private final String aplId;

    /*
     * 定数定義
     */
    /** .ENDファイル拡張子インデックス */
    private static final int EXTENSION_END_FILE = 0;

    /** 取得対象ファイル拡張子インデックス */
    private static final int EXTENSION_TARGET_FILE = 1;

    /** File Not Found エラーステータス */
    private static final String SATAUS_ERROR_FILE_NOT_FOUND = "N";

    /** File Not Found エラーステータス */
    private static final String SATAUS_ERROR_END_FILE_DUPLICATION = "F";

    /** 正常時(エラーなし)テータス */
    private static final String SATAUS_NORMAL = "0";

    /** 分割用文字列（デリミタ） */
    private static final String SPLIT_DELIMITER = "\\.";

    /** ファイル通番 */
    private static final String PARAMETER_NAME_FILE_TSUBAN = "jp.co.nttdg.bfep.stp.hub.fileTsuban";

    /** システム区分コードFROM */
    private static final String PARAMETER_NAME_CD_FROM = "jp.co.nttdg.bfep.stp.hub.message.systemCdFrom";

    /** ファイル名 */
    private static final String PARAMETER_NAME_FILE_NAME = "jp.co.nttdg.bfep.stp.hub.fileName";

    /** 削除ファイルパスリスト */
    private static final String PARAMETER_NAME_FTP_DELETE_PATHS = "jp.co.nttdg.bfep.stp.hub.ftp.deletePaths";

    /** 受信エラー */
    private static final String PARAMETER_NAME_RECEIVE_ERROR = "jp.co.nttdg.bfep.stp.hub.receiveError";

    /** 業務日付 */
    private static final String PARAMETER_NAME_TODAY = "jp.co.nttdg.bfep.stp.today";

    /**
     * コンストラクタ
     *
     * @param ftpClieant
     *            FTPファイル操作Bean<BR>
     * @param dirPath
     *            ファイル配置場所<BR>
     * @param prefix
     *            プレフィックス<BR>
     * @param extensionList
     *            拡張子リスト<BR>
     * @param datasource
     *            データソース<BR>
     * @param aplId
     *            DB用APLID<BR>
     * @throws BfepStpException
     *            STPソリューション固有例外<BR>
     */
    public ReceiverCPRed(FTPClient ftpClieant, String dirPath, String prefix,
            List<String> extensionList, DataSource datasource, String aplId)
            throws BfepStpException {

        // 拡張子数が取得対象とENDファイル以外
        if (extensionList.size() != 2) {

            stplogger.log(2100, "拡張子リスト");
            throw new BfepStpException();
        }

        this.ftpClieant = ftpClieant;
        this.dirPath = dirPath;
        this.prefix = prefix;
        this.extensionList = new ArrayList<String>();
        this.extensionList.addAll(extensionList);
        this.datasource = datasource;
        this.aplId = aplId;
    }

    /**
     * @param exchange
     *            処理対象の Exchange<BR>
     * @throws BfepStpException
     *            STPソリューション固有例外<BR>
     */
    public void process(Exchange exchange) throws BfepStpException {

        String status = SATAUS_NORMAL;

        exchange.getIn().setHeader(PARAMETER_NAME_FILE_TSUBAN, 0);

        exchange.getIn().setHeader(PARAMETER_NAME_FILE_NAME, "");

        exchange.getIn().setHeader(PARAMETER_NAME_FTP_DELETE_PATHS,
                new ArrayList<Object>());

        exchange.getIn().setHeader(PARAMETER_NAME_RECEIVE_ERROR, false);

        // FTPクライアントの起動
        this.ftpClieant.setLogInfo(exchange);

        this.ftpClieant.connect();

        String date = (String) exchange.getIn().getHeader(PARAMETER_NAME_TODAY);

        // リテラルパターン編集
        String pattern = Pattern.quote(this.prefix);
        StringBuilder regular = new StringBuilder(pattern);
        regular.append(".*");

        // リスト取得
        List<FTPFile> ftpFileList = this.ftpClieant.listFiles(this.dirPath,
                regular.toString());

        // リストよりENDファイルの有無をチェックしファイル名か状態を返却する。
        String endFileStatus = checkEndFile(ftpFileList, date);

        // ENDリストが複数ある場合は、処理終了
        if (SATAUS_ERROR_END_FILE_DUPLICATION.equals(endFileStatus)) {

            StringBuilder localFileName = new StringBuilder(this.prefix);
            localFileName.append(".*.end");

            stplogger.routeLog(2120, exchange, localFileName.toString());
            status = SATAUS_ERROR_END_FILE_DUPLICATION;

            exchange.getIn().setHeader(PARAMETER_NAME_RECEIVE_ERROR, true);

            // FTPサーバ切断
            this.ftpClieant.disconnect();

            // 電文ファイル管理テーブル追加
            addMessageFileManagementTable(exchange, status);
            return;
        }

        // .ENDファイルが一つも存在しないときは、ログを出力する。
        if ("".equals(endFileStatus)) {

            StringBuilder localEndFileName = new StringBuilder(this.prefix);
            localEndFileName.append("_");
            localEndFileName.append(date);
            localEndFileName.append("_nnnn.");
            localEndFileName.append(this.extensionList.get(EXTENSION_END_FILE));

            stplogger.routeLog(2105, exchange, localEndFileName.toString());
            status = SATAUS_ERROR_FILE_NOT_FOUND;

            exchange.getIn().setHeader(PARAMETER_NAME_RECEIVE_ERROR, true);

            // FTPサーバ切断
            this.ftpClieant.disconnect();

            // 電文ファイル管理テーブル追加
            addMessageFileManagementTable(exchange, status);
            return;
        }

        // ENDファイルを取得
        String endFileName = endFileStatus;

        // ファイル通番を格納
        int indexEnd = endFileName.lastIndexOf(".");
        int indexUnderscore = endFileName.lastIndexOf("_");

        // アンダースコアのインデックス番号の次が取りたい値のスタート位置
        int indexStart = indexUnderscore + 1;

        // ファイル通番分解
        String fileThuban = endFileName.substring(indexStart, indexEnd);

        // Exchangeのheader部にファイル通番を保持
        exchange.getIn().setHeader(PARAMETER_NAME_FILE_TSUBAN,
                Integer.valueOf(fileThuban));

        // ENDファイルが存在したときには、対象ファイルを取得
        String targetFileName = getTergetFile(ftpFileList, endFileName);

        // 対象ファイルが存在しないときはログを出力する。
        if ("".equals(targetFileName)) {

            // 存在してほしかったファイル名を作成
            String[] fileName = endFileName.split(SPLIT_DELIMITER);

            StringBuilder localTargetFileName = new StringBuilder(fileName[0]);
            localTargetFileName.append(".");
            localTargetFileName.append(this.extensionList
                    .get(EXTENSION_TARGET_FILE));

            stplogger.routeLog(2105, exchange, localTargetFileName.toString());

            status = SATAUS_ERROR_FILE_NOT_FOUND;

            exchange.getIn().setHeader(PARAMETER_NAME_RECEIVE_ERROR, true);

            // FTPサーバ切断
            this.ftpClieant.disconnect();

            // 電文ファイル管理テーブル追加
            addMessageFileManagementTable(exchange, status);

            return;
        }

        File targetFile = new File(this.dirPath, targetFileName);

        // 対象ファイルのバイトデータを取得
        byte[] byteArrayData = this.ftpClieant.getFile(targetFile.getPath());

        exchange.getIn().setBody(byteArrayData);

        // 削除対象.ENDファイルパスのリストをセットする。
        File endFile = new File(this.dirPath, endFileName);

        List<Object> endFileList = new ArrayList<Object>();

        endFileList.add(endFile.getPath());

        exchange.getIn()
                .setHeader(PARAMETER_NAME_FTP_DELETE_PATHS, endFileList);

        exchange.getIn().setHeader(PARAMETER_NAME_FILE_NAME, targetFileName);

        this.ftpClieant.disconnect();

        // 電文ファイル管理テーブル追加
        addMessageFileManagementTable(exchange, status);
    }

    /**
     * 存在するすべてのENDファイル名のリストを取得する。
     *
     * @param ftpFileList
     *            FTPファイルリスト<BR>
     * @return
     *            ENDファイル名のリスト<BR>
     */
    private String checkEndFile(List<FTPFile> ftpFileList, String date) {

        String extensionName = this.extensionList.get(EXTENSION_END_FILE);

        String returnEndFileList = "";

        int count = 0;

        for (int index = 0; index < ftpFileList.size(); index++) {

            FTPFile ftpFile = ftpFileList.get(index);

            // 全てのファイル名より、.ENDファイルを取得。複数存在しない事が前提なので、
            // 毎回ファイル名を取得しても問題ない。
            if (ftpFile.getName().indexOf(extensionName) != -1) {
                returnEndFileList = ftpFile.getName();
                count++;
            }
        }

        // .ENDファイルが複数ある時は、重複エラーステータスを返す
        if (count > 1) {
            returnEndFileList = SATAUS_ERROR_END_FILE_DUPLICATION;
        } else {
            // １件以下の時に対象日付が含まれていなければ""文字とする。
            if (returnEndFileList.indexOf(date) == -1) {
                returnEndFileList = "";
            }
        }

        return returnEndFileList;
    }

    /**
     * 対象ファイルを取得する。
     *
     * @param ftpFileList
     *            FTPファイルリスト<BR>
     * @param endFileName
     *            .ENDファイル名<BR>
     * @return
     *            取得対象ファイル名<BR>
     */
    private String getTergetFile(List<FTPFile> ftpFileList, String endFileName) {

        String returnFileName = "";

        // 取得対象ファイルファイル拡張子
        String extensionName = this.extensionList.get(EXTENSION_TARGET_FILE);

        // .ENDファイルをドットを境に２分する。
        String[] fileName = endFileName.split(SPLIT_DELIMITER);

        for (int index = 0; index < ftpFileList.size(); index++) {

            FTPFile ftpFile = ftpFileList.get(index);

            // 取得対象ファイルの拡張子を含むファイル名に
            // エンドファイルの拡張子より前方がふくまれていれる。
            if (ftpFile.getName().indexOf(fileName[0]) != -1) {

                // 全てのファイル名より、取得対象ファイルの拡張子を含むファイルを取得
                if (ftpFile.getName().indexOf(extensionName) != -1) {

                    returnFileName = ftpFile.getName();
                    break;
                }
            }
        }

        // ここまで来たときに拡張子をもつファイルがなければ、空文字となっている。
        return returnFileName;
    }

    /**
     * 電文ファイル管理テーブルに追加する。
     *
     * @param exchange
     *            処理対象の Exchange<BR>
     * @param fileTsuban
     *            ファイル通番
     * @param status
     *            状態
     * @throws BfepStpException
     *            STPソリューション固有例外<BR>
     */
    private void addMessageFileManagementTable(Exchange exchange, String status)
            throws BfepStpException {

        // 電文ファイル管理テーブルに追加
        try {

            Connection conn = datasource.getConnection();
            try {

                PreparedStatement pstmt = conn
                        .prepareStatement("INSERT INTO DENBUN_FILE_MGR (TASK_NO, SYSTEM_CD, FILE_TSUBAN, STATUS, INS_TIMESTAMP, INS_APLID) "
                                + "VALUES (?, ?, ?, ?, SYSDATE, ?)");
                try {

                    // タスク№
                    pstmt.setString(1, exchange.getFromRouteId());

                    // システム区分コード
                    pstmt.setString(
                            2,
                            (String) exchange.getIn().getHeader(
                                    PARAMETER_NAME_CD_FROM));

                    // 取得対象ファイル通番
                    pstmt.setInt(
                            3,
                            (Integer) exchange.getIn().getHeader(
                                    PARAMETER_NAME_FILE_TSUBAN));

                    // ステータス
                    pstmt.setString(4, status);

                    // APLID
                    pstmt.setString(5, this.aplId);

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

