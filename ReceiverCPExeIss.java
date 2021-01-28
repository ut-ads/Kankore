/*
 * @(#)ReceiverCPExeIss.java
 *
 * Copyright (c) 2014, NTT DATA Getronics Corporation.
 */
package jp.co.nttdg.bfep.stp.bean.hub;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import jp.co.nttdg.bfep.stp.framework.BfepStpException;
import jp.co.nttdg.bfep.stp.framework.BfepStpLogger;
import jp.co.nttdg.bfep.stp.framework.BfepStpLoggerFactory;

import org.apache.camel.Exchange;
import org.apache.commons.net.ftp.FTPFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 電文受信(電子CP(約定・銘柄))Bean
 * 
 * @author h_tozawa
 * @version 1.0
 * 
 */
public class ReceiverCPExeIss {

    /** トレースロガー */
    private static final Logger logger = LoggerFactory
            .getLogger(ReceiverCPExeIss.class);

    /** B-FEP STPロガー */
    private static final BfepStpLogger stplogger = BfepStpLoggerFactory
            .getLogger(ReceiverCPExeIss.class);

    /** FTPクライアントBean */
    private final FTPClient ftpClieant;

    /** ファイル配置場所 */
    private final String dirName;

    /** プレフィックスリスト */
    private final Set<String> prefixList;

    /** 拡張子リスト */
    private final List<String> extensionList;

    /** データソース */
    private final DataSource datasource;

    /** DB用APLID */
    private final String aplId;

    /** .ENDファイル拡張子インデックス */
    private static final int EXTENSION_END_FILE = 0;

    /** 取得対象ファイル拡張子インデックス */
    private static final int EXTENSION_TARGET_FILE = 1;

    /** 重複チェックエラーステータス */
    private static final String SATAUS_ERROR_DUPLICATION = "T";

    /** 昇順チェックエラーステータス */
    private static final String SATAUS_ERROR_ASC = "S";

    /** 歯抜けチェックエラーステータス */
    private static final String SATAUS_ERROR_WHETHER_MMISSING = "H";

    /** 正常時(エラーなし)テータス */
    private static final String SATAUS_NORMAL = "0";

    /*
     * 定数定義
     */
    /** ファイル通番 */
    private static final String PARAMETER_NAME_FILE_TSUBAN = "jp.co.nttdg.bfep.stp.hub.fileTsuban";

    /** 削除ファイルパスリスト */
    private static final String PARAMETER_NAME_FTP_DELETE_PATHS = "jp.co.nttdg.bfep.stp.hub.ftp.deletePaths";

    /** 業務日付 */
    private static final String PARAMETER_NAME_TODAY = "jp.co.nttdg.bfep.stp.today";

    /**
     * 
     * コンストラクタ
     * 
     * @param ftpClieant
     *            FTPクライアントBean<BR>
     * @param dirName
     *            ファイル配置場所<BR>
     * @param prefixList
     *            プレフィックスリスト<BR>
     * @param extensionList
     *            拡張子リスト<BR>
     * @param datasource
     *            データソース<BR>
     * @param aplId
     *            DB用APLID<BR>
     * @throws BfepStpException
     *             STPソリューション固有例外<BR>
     * 
     */
    public ReceiverCPExeIss(FTPClient ftpClieant, String dirName,
            List<String> prefixList, List<String> extensionList,
            DataSource datasource, String aplId) throws BfepStpException {

        // リストにプリフィックスが存在しない。
        if (prefixList.size() == 0) {

            stplogger.log(2100, "プリフィックスリスト");
            throw new BfepStpException();
        }

        this.prefixList = new LinkedHashSet<String>();
        this.prefixList.addAll(prefixList);

        // メンバ変数のプリフィックスと引数のプリフィックスリストの個数が一致しない。
        if (this.prefixList.size() != prefixList.size()) {

            stplogger.log(2100, "プリフィックスリスト");
            throw new BfepStpException();
        }

        // ENDファイル拡張子と対象ファイル拡張子がセットで存在してる。
        if (extensionList.size() != 2) {

            stplogger.log(2100, "拡張子リスト");
            throw new BfepStpException();
        }

        this.ftpClieant = ftpClieant;
        this.dirName = dirName;
        this.extensionList = extensionList;
        this.datasource = datasource;
        this.aplId = aplId;
    }

    /**
     * 取得対象ファイルについて、ファイル通番チェックの後、データを取得する。
     * 
     * @param exchange
     *            処理対象の Exchange<BR>
     * @return dataSetMap データセット<BR>
     * @throws BfepStpException
     *             STPソリューション固有例外<BR>
     */
    public Set<Map.Entry<String, byte[]>> process(Exchange exchange)
            throws BfepStpException {

        exchange.getIn().setHeader(PARAMETER_NAME_FILE_TSUBAN, 0);

        // in.headersに要素数0のリストをセット
        List<Object> deletePathsList = new ArrayList<Object>();
        exchange.getIn().setHeader(PARAMETER_NAME_FTP_DELETE_PATHS,
                deletePathsList);

        this.ftpClieant.setLogInfo(exchange);
        this.ftpClieant.connect();

        // FTPクライアントBeanのlistFilesメソッドを用いて、下記のFTPファイルリストを取得する。
        StringBuilder regular = new StringBuilder(".*");
        String date = (String) exchange.getIn().getHeader(PARAMETER_NAME_TODAY);
        regular.append(date);
        regular.append(".*");
        List<FTPFile> ftpFileList = this.ftpClieant.listFiles(this.dirName,
                regular.toString());

        // ファイルリストから、タイムスタンプの最も古い.ENDファイルの通番("nnnn"部)を取得する。
        String oldFileTsuban = this.getOldfileTsuban(ftpFileList);

        // .ENDファイルが一つも存在しないとき 要素数０のリストを返す。
        if ("".equals(oldFileTsuban)) {

            ftpClieant.disconnect();
            return new LinkedHashSet<Map.Entry<String, byte[]>>();
        }

        // ファイル通番を持つ.ENDファイルが1種類でも欠けている場合、要素数0のリストを返す。
        if (!this.checkFileList(ftpFileList, date, oldFileTsuban,
                this.extensionList.get(EXTENSION_END_FILE), false)) {

            ftpClieant.disconnect();
            return new LinkedHashSet<Map.Entry<String, byte[]>>();
        }

        // ファイル通番を持つ取得対象ファイルが1種類でも欠けている場合、要素数0のリストを返す。
        if (!this.checkFileList(ftpFileList, date, oldFileTsuban,
                this.extensionList.get(EXTENSION_TARGET_FILE), true)) {

            ftpClieant.disconnect();
            return new LinkedHashSet<Map.Entry<String, byte[]>>();
        }

        // ファイル通番のチェック
        String status = checkfileTsuban(exchange,
                Integer.valueOf(oldFileTsuban));

        // 取得対象ファイルリスト
        Set<Map.Entry<String, byte[]>> retList = null;

        // 重複エラー以外のステータス状態であれば、
        // プレフィックスリストのリスト順に処理を行う。
        if (!SATAUS_ERROR_DUPLICATION.equals(status)) {

            retList = makeTergetFileList(date, oldFileTsuban);
        } else {

            // 重複エラーが発生した場合は、空のリストを返却
            retList = new LinkedHashSet<Map.Entry<String, byte[]>>();
        }

        // 削除対象.ENDファイルパスのリストを作成
        List<String> endFilesList = makeEndFileList(date, oldFileTsuban);

        // ヘッダ部に作成した.ENDファイルリストを設定
        exchange.getIn().setHeader(PARAMETER_NAME_FTP_DELETE_PATHS,
                endFilesList);

        // 引数で渡されたExchangeのin.headersにファイル通番を格納
        exchange.getIn().setHeader(PARAMETER_NAME_FILE_TSUBAN,
                Integer.valueOf(oldFileTsuban));
        this.ftpClieant.disconnect();

        // 電文ファイル管理テーブル追加
        addMessageFileManagementTable(exchange, Integer.valueOf(oldFileTsuban),
                status);

        return retList;
    }

    /**
     * 取得対象ファイルリスト作成
     * 
     * @param date
     *            業務日付<BR>
     * @param oldFileTsuban
     *            ファイル通番<BR>
     * @return 取得対象リスト<BR>
     * @throws BfepStpException
     *             STPソリューション固有例外<BR>
     */
    private Set<Map.Entry<String, byte[]>> makeTergetFileList(String date,
            String oldFileTsuban) throws BfepStpException {

        // 返却用リストの初期化
        Map<String, byte[]> returnMap = new LinkedHashMap<String, byte[]>();

        // エントリ(ファイル名、データ)を生成し、戻り値用セットに追加する。
        // プリフィックスリストに存在するプリフィックス名分の対象取得ファイルを作成する。
        for (String prifixName : this.prefixList) {

            // ファイル名作成
            String targetFileName = makeFileName(prifixName, date,
                    oldFileTsuban, extensionList.get(EXTENSION_TARGET_FILE));

            // フルパスファイル名取得
            File file = new File(dirName, targetFileName);

            returnMap.put(targetFileName,
                    this.ftpClieant.getFile(file.getPath()));
        }

        return returnMap.entrySet();
    }

    /**
     * .ENDファイルリスト作成
     * 
     * @param exchange
     *            処理対象の Exchange<BR>
     * @param date
     *            業務日付<BR>
     * @param oldFileTsuban
     *            最古ファイル通番<BR>
     * @return ENDファイルリスト<BR>
     */
    private List<String> makeEndFileList(String date, String oldFileTsuban) {

        // 戻り値の初期化
        List<String> retFileList = new ArrayList<String>();

        for (String prifixName : this.prefixList) {

            // ファイル名作成
            String targetFileName = makeFileName(prifixName, date,
                    oldFileTsuban, extensionList.get(EXTENSION_END_FILE));

            // フルパス名を取得してリストにセット
            File file = new File(dirName, targetFileName);
            retFileList.add(file.getPath());

        }

        return retFileList;
    }

    /**
     * 最古通番取得
     * 
     * @param ftpFileList
     *            FTPファイルリスト<BR>
     * @return 最古通番<BR>
     */
    private String getOldfileTsuban(List<FTPFile> ftpFileList) {

        String retOldfileTsuban = "";

        String extensionName = this.extensionList.get(EXTENSION_END_FILE);

        Calendar oldTimeStanp = Calendar.getInstance(Locale.JAPANESE);
        // 基底時間を 西暦 「9999年12月31日 23時59分59秒」に設定
        oldTimeStanp.set(9999, 11, 31, 23, 59, 59);
        for (int index = 0; index < ftpFileList.size(); index++) {

            FTPFile ftpFile = ftpFileList.get(index);

            // 全てのファイル名より、.ENDファイルの拡張子を含むファイルのタイムスタンプを取得して、
            if (ftpFile.getName().indexOf(extensionName) != -1) {

                // 前回と比較したときに、古ければその時の通番を保持しておく
                if (oldTimeStanp.compareTo(ftpFile.getTimestamp()) > 0) {
                    oldTimeStanp = ftpFile.getTimestamp();

                    int indexEnd = ftpFile.getName().lastIndexOf(".");
                    int indexStart = ftpFile.getName().lastIndexOf("_") + 1;

                    // 通番
                    retOldfileTsuban = ftpFile.getName().substring(indexStart,
                            indexEnd);
                }
            }
        }

        // ここまで来たときに拡張子リストをもつファイルがなければ、空文字となっている。
        // もっとも古いタイムスタンプをもつ.ENDファイルの連番を返却()
        return retOldfileTsuban;
    }

    /**
     * ファイル存在チェック
     * 
     * @param ftpFileList
     *            FTPファイルリスト<BR>
     * @param date
     *            業務日付<BR>
     * @param fileTsuban
     *            ファイル通番<BR>
     * @param extension
     *            ファイルの拡張子<BR>
     * @param isLogs
     *            ログ出力をするときにはTRUE<BR>
     * @return 引数の情報から作ったファイル名がFTPリストに存在すれば TRUE<BR>
     */
    private boolean checkFileList(List<FTPFile> ftpFileList, String date,
            String fileTsuban, String extension, boolean isLogs) {

        boolean ret = true;

        for (String prefixName : this.prefixList) {

            // プレフィックスを含むファイル名を作成
            String localFileName = this.makeFileName(prefixName, date,
                    fileTsuban, extension);

            // FTPFileListを総なめして上記のファイル名が存在しないときは、false
            if (!this.cheakFTPFileList(ftpFileList, localFileName)) {

                // ログ出力が必要となる場合
                if (isLogs) {

                    stplogger.log(2101, localFileName);
                }
                ret = false;
            }
        }

        return ret;
    }

    /**
     * 引数から命名フォーマットに合わせたファイル名を作成する。
     * 
     * @param prefix
     *            プリフィックス<BR>
     * @param date
     *            業務日付<BR>
     * @param fileTsuban
     *            ファイル通番<BR>
     * @param extension
     *            拡張子<BR>
     * @return ファイル名
     */
    private String makeFileName(String prefix, String date, String fileTsuban,
            String extension) {

        StringBuilder fileNameBuffer = new StringBuilder(prefix);
        fileNameBuffer.append("_");
        fileNameBuffer.append(date);
        fileNameBuffer.append("_");
        fileNameBuffer.append(fileTsuban);
        fileNameBuffer.append(".");
        fileNameBuffer.append(extension);

        return fileNameBuffer.toString();
    }

    /**
     * ファイル存在チェック
     * 
     * @param ftpFileList
     *            FTPファイルリスト<BR>
     * @param fileName
     *            ファイル名<BR>
     * @return ファイルが一件でも存在すればTRUE
     */
    private boolean cheakFTPFileList(List<FTPFile> ftpFileList, String fileName) {

        boolean retFlag = false;

        for (int index = 0; index < ftpFileList.size(); index++) {
            FTPFile ftpFile = ftpFileList.get(index);

            // ファイル名が存在した時
            if (ftpFile.getName().indexOf(fileName) != -1) {
                retFlag = true;
                break;
            }
        }

        return retFlag;
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
    private String checkfileTsuban(Exchange exchange, int fileTsuban)
            throws BfepStpException {

        // 重複チェック 重複チェックエラーなら’T'を返します。
        String status = checkFileTsubanDuplication(exchange, fileTsuban);

        // 重複チェックでなけば、続けて昇順/歯抜けチェックを行う。
        if (SATAUS_NORMAL.equals(status)) {

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
     * @return 重複なし(正常)："0" 重複あり:"T"
     * @throws BfepStpException
     *             STPソリューション固有例外<BR>
     */
    private String checkFileTsubanDuplication(Exchange exchange, int fileTsuban)
            throws BfepStpException {

        String taskNo = exchange.getFromRouteId();

        try {

            Connection conn = this.datasource.getConnection();

            try {
                PreparedStatement pstmt = conn
                        .prepareStatement("SELECT COUNT(*) AS CUNT FROM DENBUN_FILE_MGR WHERE TASK_NO = ? AND FILE_TSUBAN = ? AND DEL_FLG = '0'");
                try {

                    pstmt.setString(1, taskNo);
                    pstmt.setInt(2, fileTsuban);
                    ResultSet rs = pstmt.executeQuery();

                    try {
                        if (rs.next()) {

                            int count = rs.getInt("CUNT");

                            // 取得した値が0より大きい場合は重複あり
                            if (count > 0) {

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
     * 
     * @param exchange
     *            処理対象の Exchange<BR>
     * @param fileTsuban
     *            ファイル通番<BR>
     * @return 昇順チェックエラー:"S" 歯抜けチェックエラー:"H" エラーなし（正常）:"0"
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
     * @return 最大通番値<BR>
     * @throws BfepStpException
     *             STPソリューション固有例外<BR>
     */
    private int getMaxFileTsuban(Exchange exchange) throws BfepStpException {

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
     * @param fileTsuban
     *            ファイル通番<BR>
     * @param status
     *            ステータス<BR>
     * @throws BfepStpException
     *             STPソリューション固有例外<BR>
     */
    private void addMessageFileManagementTable(Exchange exchange,
            int fileTsuban, String status) throws BfepStpException {

        try {

            Connection conn = datasource.getConnection();
            try {

                PreparedStatement pstmt = conn
                        .prepareStatement("INSERT INTO DENBUN_FILE_MGR (TASK_NO, FILE_TSUBAN, STATUS, INS_TIMESTAMP, INS_APLID) "
                                + "VALUES (?, ?, ?, SYSDATE, ?)");
                try {

                    // タスク№
                    pstmt.setString(1, exchange.getFromRouteId());

                    // 取得対象ファイル通番
                    pstmt.setInt(2, fileTsuban);

                    // ステータス
                    pstmt.setString(3, status);

                    // APLID
                    pstmt.setString(4, this.aplId);

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

