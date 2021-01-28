/*
 * @(#)ReceiverYen.java
 *
 * Copyright (c) 2014, NTT DATA Getronics Corporation.
 */
package jp.co.nttdg.bfep.stp.bean.hub;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import jp.co.nttdg.bfep.stp.framework.BfepStpException;
import jp.co.nttdg.bfep.stp.framework.BfepStpLogger;
import jp.co.nttdg.bfep.stp.framework.BfepStpLoggerFactory;
import jp.co.nttdg.bfep.stp.framework.ByteArrayUtil;

import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 電文受信(円資金管理)Bean
 * 
 * @author h_tozawa
 * @version 1.0
 * 
 */
public class ReceiverYen {

    /** トレースロガー */
    private static final Logger logger = LoggerFactory
            .getLogger(ReceiverYen.class);

    /** B-FEP STPロガー */
    private static final BfepStpLogger stplogger = BfepStpLoggerFactory
            .getLogger(ReceiverYen.class);

    /** ファイル配置場所 */
    private final String dirPath;

    /** プレフィックス */
    private final String prefix;

    /** 拡張子 */
    private final String extension;

    /** データソース */
    private final DataSource datasource;

    /** DB用APLID */
    private final String aplId;

    /*
     * 定数定義
     */
    /** 重複チェックエラーステータス */
    private static final String SATAUS_ERROR_DUPLICATION = "T";

    /** 昇順チェックエラーステータス */
    private static final String SATAUS_ERROR_ASC = "S";

    /** 歯抜けチェックエラーステータス */
    private static final String SATAUS_ERROR_WHETHER_MMISSING = "H";

    /** 正常時(エラーなし)テータス */
    private static final String SATAUS_NORMAL = "0";

    /** ファイル通番 */
    private static final String PARAMETER_NAME_FILE_TSUBAN = "jp.co.nttdg.bfep.stp.hub.fileTsuban";

    /** ファイル名 */
    private static final String PARAMETER_NAME_FILE_NAME = "jp.co.nttdg.bfep.stp.hub.fileName";

    /** 業務日付 */
    private static final String PARAMETER_NAME_TODAY = "jp.co.nttdg.bfep.stp.today";

    /** システム区分コードFROM */
    private static final String PARAMETER_NAME_CD_FROM = "jp.co.nttdg.bfep.stp.hub.message.systemCdFrom";

    /**
     * コンストラクタ<BR>
     * 
     * @param dirPath
     *            ファイル配置場所<BR>
     * @param prefix
     *            プレフィックス<BR>
     * @param extension
     *            拡張子<BR>
     * @param datasource
     *            データソース<BR>
     * @param aplId
     *            DB用APLID<BR>
     * 
     * @throws BfepStpException
     *             STPソリューション固有例外<BR>
     * 
     */
    public ReceiverYen(String dirPath, String prefix, String extension,
            DataSource datasource, String aplId) throws BfepStpException {

        // メンバ変数の初期化
        this.dirPath = dirPath;
        this.prefix = prefix;
        this.extension = extension;
        this.datasource = datasource;
        this.aplId = aplId;

        // ディレクトリが存在しない
        if (!new File(dirPath).exists()) {
            stplogger.log(2008, dirPath);
            throw new BfepStpException();
        }
    }

    /**
     * @param exchange
     *            処理対象の Exchange<BR>
     * @throws BfepStpException
     *             STPソリューション固有例外<BR>
     */
    public void process(Exchange exchange) throws BfepStpException {

        // Exchangeを初期化する。
        exchange.getIn().setHeader(PARAMETER_NAME_FILE_TSUBAN, 0);
        exchange.getIn().setHeader(PARAMETER_NAME_FILE_NAME, "");

        // 正規表現のファイル名を編集(取得対象)
        StringBuilder regular = new StringBuilder(this.prefix);
        String date = (String) exchange.getIn().getHeader(PARAMETER_NAME_TODAY);
        regular.append(date);
        regular.append("\\d{4}\\.");
        regular.append(this.extension);

        String[] files = new String[0];

        // 一覧を取得するディレクトリのパスを指定する。
        File file = new File(this.dirPath);

        // 正規表現のファイルリストを取得
        files = file.list(getFileExtensionFilter(regular.toString()));
        List<String> fileList = Arrays.asList(files);

        // ファイルリストから、タイムスタンプの最も古い対象ファイル名を取得する。
        String targetFileName = this.getOldTargetFileName(fileList);

        // 対象が無い場合は、ENDファイル名を取得して処理を終了
        if ("".equals(targetFileName)) {

            // 正規表現のファイル名を編集(ENDファイル)
            StringBuilder endFileName = new StringBuilder(this.prefix);
            endFileName.append(date);
            endFileName.append("END.");
            endFileName.append(this.extension);

            // ENDファイル名が存在する時はexchangeを更新
            File endFiles = new File(this.dirPath, endFileName.toString());
            if (endFiles.exists()) {
                exchange.getIn().setHeader(PARAMETER_NAME_FILE_NAME,
                        endFileName.toString());
            }
            return;
        }

        // ファイル通番を取得
        int indexStart = targetFileName.lastIndexOf(date) + date.length();
        int indexEnd = targetFileName.lastIndexOf(".");

        String oldFileTsuban = targetFileName.substring(indexStart, indexEnd);

        // ファイル通番のチェック
        String status = checkFileTsuban(exchange,
                Integer.valueOf(oldFileTsuban));

        // チェック結果が重複エラー以外の時は以下の処理を実施
        if (!SATAUS_ERROR_DUPLICATION.equals(status)) {

            // パス名の取得
            File fullName = new File(this.dirPath, targetFileName);

            // Bodyにデータをセット
            byte[] bodyDataArray = getBodyData(fullName);
            exchange.getIn().setBody(bodyDataArray);

            // 引数で渡されたExchangeのin.headersにファイル通番を格納
            exchange.getIn().setHeader(PARAMETER_NAME_FILE_TSUBAN,
                    Integer.valueOf(oldFileTsuban));

        }

        // 取得ファイル名を設定
        exchange.getIn().setHeader(PARAMETER_NAME_FILE_NAME, targetFileName);

        // 電文ファイル管理テーブル追加
        addMessageFileManagementTable(exchange, status,
                Integer.valueOf(oldFileTsuban));
    }

    /**
     * 正規表現ファイルと一致したファイル名が返却するフィルタ
     * 
     * @param regular
     *            正規表現<BR>
     * @return 正規表現ファイルと一致したファイル名<BR>
     */
    private FilenameFilter getFileExtensionFilter(String regular) {
        final Pattern ptn = Pattern.compile(regular);

        return new FilenameFilter() {
            public boolean accept(File file, String name) {
                Matcher match = ptn.matcher(name);
                boolean ret = match.matches();
                return ret;
            }
        };
    }

    /**
     * 最古のファイル名を取得<BR>
     * 
     * @param fileList
     *            ファイル名リスト<BR>
     * @return 最古の更新日付を持つファイル名<BR>
     */
    private String getOldTargetFileName(List<String> fileList) {

        String returnFileName = "";

        Calendar oldTimeStanp = Calendar.getInstance(Locale.JAPANESE);
        // 基底時間を 西暦 「9999年12月31日 23時59分59秒」に設定
        oldTimeStanp.set(9999, 11, 31, 23, 59, 59);

        for (String fileName : fileList) {

            // 全てのファイル名より、タイムスタンプを取得
            File localFile = new File(this.dirPath, fileName);

            long timeStanp = localFile.lastModified();
            Calendar fileTimeStanp = Calendar.getInstance(Locale.JAPANESE);

            fileTimeStanp.setTime(new Date(timeStanp));

            // 前回と比較したときに、タイムスタンプが古ければその時のファイル名を保持しておく
            if (oldTimeStanp.compareTo(fileTimeStanp) > 0) {
                oldTimeStanp = fileTimeStanp;
                returnFileName = fileName;
            }
        }

        // ここまで来たときにファイルがなければ、空文字となっている。
        return returnFileName;
    }

    /**
     * ファイルのバイト配列を戻す
     * 
     * @param fullName
     *            ファイル名<BR>
     * @return ファイル内容のバイト配列<BR>
     * @throws BfepStpException
     *             STPソリューション固有例外<BR>
     */
    private byte[] getBodyData(File fullName) throws BfepStpException {

        List<Byte> byteList = new ArrayList<Byte>();
        BufferedInputStream buffInputStream = null;

        String canonicalPath = null;

        try {
            canonicalPath = fullName.getCanonicalPath();
            buffInputStream = new BufferedInputStream(new FileInputStream(
                    canonicalPath));

            int ch = 0;
            while ((ch = buffInputStream.read()) != -1) {
                byteList.add((byte) ch);
            }
        } catch (IOException e) {

            logger.error("ファイル読込み失敗", e);
            stplogger.log(5007, canonicalPath, e.getMessage());
            throw new BfepStpException(e.getMessage(), e);

        } finally {

            // クローズ処理
            if (buffInputStream != null) {
                try {
                    buffInputStream.close();
                } catch (IOException e) {
                    logger.error("ファイルクローズ失敗", e);
                    stplogger.log(5010, canonicalPath, e.getMessage());
                    throw new BfepStpException(e.getMessage(), e);
                }
            }
        }

        return ByteArrayUtil.toArray(byteList);
    }

    /**
     * ファイル通番チェック
     * 
     * @param exchange
     *            処理対象の Exchange<BR>
     * @param fileTsuban
     *            ファイル通番<BR>
     * @return ステータス<BR>
     * @throws BfepStpException
     *             STPソリューション固有例外<BR>
     */
    private String checkFileTsuban(Exchange exchange, int fileTsuban)
            throws BfepStpException {

        // 重複チェック 重複チェックエラーなら’T'を返します。
        String status = checkFileTsubanDuplication(exchange, fileTsuban);

        // 重複チェックでなけば、続けて昇順/歯抜けチェックを行う。
        if (SATAUS_NORMAL.equals(status)) {

            // 昇順/歯抜けチェック 昇順エラー'S' 歯抜けエラーは'H'を返します。
            status = checkFileTsubanAscAndWhether(exchange, fileTsuban);
        }

        return status;
    }

    /**
     * DBを利用して重複チェックを行い、状態のステータスを返却
     * 
     * @param exchange
     *            処理対象の Exchange<BR>
     * @param fileTsuban
     *            ファイル通番<BR>
     * @return 重複なし(正常)："0" 重複あり:"T"<BR>
     * @throws BfepStpException
     *             STPソリューション固有例外<BR>
     */
    private String checkFileTsubanDuplication(Exchange exchange, int fileTsuban)
            throws BfepStpException {

        try {

            Connection conn = this.datasource.getConnection();
            try {

                PreparedStatement pstmt = conn
                        .prepareStatement("SELECT COUNT(*) AS CUNT FROM DENBUN_FILE_MGR WHERE TASK_NO = ? AND FILE_TSUBAN = ? AND DEL_FLG = '0'");
                try {

                    pstmt.setString(1, (String) exchange.getFromRouteId());
                    pstmt.setInt(2, fileTsuban);
                    ResultSet rs = pstmt.executeQuery();
                    try {

                        if (rs.next()) {
                            int count = rs.getInt("CUNT");

                            // 取得した値が0より大きい場合
                            if (count > 0) {

                                // 重複あり
                                stplogger.routeLog(2102, exchange, fileTsuban);
                                return SATAUS_ERROR_DUPLICATION;
                            }
                        }
                    } finally {
                        rs.close();
                    }
                } finally {
                    pstmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException e) {

            logger.error("SELECT失敗", e);
            stplogger
                    .routeLog(4000, exchange, e.getErrorCode(), e.getMessage());
            throw new BfepStpException(e.getMessage(), e);
        }

        return SATAUS_NORMAL;
    }

    /**
     * 昇順チェックと歯抜けチェックをおこない、状態のステータスを返却
     * 
     * @param exchange
     *            処理対象の Exchange<BR>
     * @param fileTsuban
     *            ファイル通番<BR>
     * @return 昇順チェックエラー:"S" 歯抜けチェックエラー:"H" エラーなし（正常）:"0"<BR>
     * @throws BfepStpException
     *             STPソリューション固有例外<BR>
     */
    private String checkFileTsubanAscAndWhether(Exchange exchange,
            int fileTsuban) throws BfepStpException {

        // 最大ファイル通番を取得
        int maxfileTsuban = getMaxFileTsuban(exchange);

        // 昇順チェック
        if (fileTsuban <= maxfileTsuban) {
            stplogger.routeLog(2103, exchange, fileTsuban, maxfileTsuban + 1);
            return SATAUS_ERROR_ASC;

        }

        // 歯抜けチェック
        if (fileTsuban != maxfileTsuban + 1) {

            stplogger.routeLog(2104, exchange, fileTsuban, maxfileTsuban + 1);
            return SATAUS_ERROR_WHETHER_MMISSING;
        }

        return SATAUS_NORMAL;
    }

    /**
     * DBを利用して最大通番を取得する
     * 
     * @param exchange
     *            処理対象の Exchange<BR>
     * @param fileTsuban
     *            ファイル通番<BR>
     * @return ファイル通番の最大値<BR>
     * @throws BfepStpException
     *             STPソリューション固有例外<BR>
     */
    private int getMaxFileTsuban(Exchange exchange) throws BfepStpException {

        // 戻り値の初期化
        int retMaxTsuban = -1;

        try {

            Connection conn = datasource.getConnection();
            try {

                PreparedStatement pstmt = conn
                        .prepareStatement("SELECT MAX(FILE_TSUBAN) FROM DENBUN_FILE_MGR WHERE TASK_NO = ? AND DEL_FLG = '0'");
                try {

                    // タスクナンバー
                    pstmt.setString(1, (String) exchange.getFromRouteId());

                    // SQL実行
                    ResultSet rs = pstmt.executeQuery();

                    try {

                        if (rs.next()) {
                            retMaxTsuban = rs.getInt("MAX(FILE_TSUBAN)");

                        }
                    } finally {
                        rs.close();
                    }

                } finally {
                    pstmt.close();
                }

            } finally {
                conn.close();
            }
        } catch (SQLException e) {

            logger.error("SELECT失敗", e);
            stplogger
                    .routeLog(4000, exchange, e.getErrorCode(), e.getMessage());
            throw new BfepStpException(e.getMessage(), e);
        }

        return retMaxTsuban;
    }

    /**
     * 電文ファイル管理テーブルに追加する。
     * 
     * @param exchange
     *            処理対象の Exchange<BR>
     * @param status
     *            ステータス<BR>
     * @param fileTsuban
     *            ファイル通番<BR>
     * @throws BfepStpException
     *             STPソリューション固有例外<BR>
     */
    private void addMessageFileManagementTable(Exchange exchange,
            String status, int fileTsuban) throws BfepStpException {

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
                    pstmt.setInt(3, fileTsuban);

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

