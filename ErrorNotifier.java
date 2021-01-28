/*
 * @(#)ErrorNotifier.java
 *
 * Copyright (c) 2014, NTT DATA Getronics Corporation.
 */
package jp.co.nttdg.bfep.stp.bean.hub;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import jp.co.nttdg.bfep.stp.framework.BfepStpException;
import jp.co.nttdg.bfep.stp.framework.BfepStpLogger;
import jp.co.nttdg.bfep.stp.framework.BfepStpLoggerFactory;

import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * エラー通知Bean
 * 
 * @author h_tozawa
 * @version 1.0
 * 
 */
public class ErrorNotifier {

    /** トレースロガー */
    private static final Logger logger = LoggerFactory
            .getLogger(ErrorNotifier.class);

    /** B-FEP STPロガー */
    private static final BfepStpLogger stplogger = BfepStpLoggerFactory
            .getLogger(ErrorNotifier.class);

    /** エラー通知項目判定文字 */
    private final String decisionStr;

    /** エラー通知フォーマット */
    private final String format;

    /** エラー通知ディレクトリ名 */
    private final String dirName;

    /** エラー通知ファイル名 */
    private final String fileName;

    /** エラー通知ファイルキャラクタセット */
    private final Charset fileCharSet;

    /** エラー通知情報リスト */
    private final Set<String> errorInfoSet;

    /** エラー通知情報取得SQL文 */
    private final String sqlFormat;

    /** データソース */
    private final DataSource datasource;

    /*
     * 定数定義
     */
    /** カラム名 */
    private static final String COLUMN_NAME = "COLUMN";

    /** エラー通知エラー */
    private static final String PARAMETER_NAME_ERROR_TSUCHI_ERROR = "jp.co.nttdg.bfep.stp.hub.error.tsuchiError";

    /** エラー通知条件 */
    private static final String PARAMETER_NAME_ERROR_CONDITION = "jp.co.nttdg.bfep.stp.hub.error.condition";

    /**
     * コンストラクタ
     * 
     * @param tableName
     *            エラー通知テーブル名
     * @param conditional
     *            エラー通知条件
     * @param decisionStr
     *            エラー通知項目判定文字
     * @param formatFileName
     *            エラー通知フォーマット
     * @param formatCharSet
     *            エラー通知フォーマットキャラクタセット
     * @param dirName
     *            エラー通知ディレクトリ名
     * @param fileName
     *            エラー通知ファイル名
     * @param fileCharSet
     *            エラー通知ファイルキャラクタセット
     * @param datasource
     *            データソース
     * @throws BfepStpException
     *             STPソリューション固有例外<BR>
     */
    public ErrorNotifier(String tableName, String conditional,
            String decisionStr, String formatFileName, String formatCharSet,
            String dirName, String fileName, String fileCharSet,
            DataSource datasource) throws BfepStpException {

        // エラー通知項目判定文
        this.decisionStr = decisionStr;

        // エラー通知フォーマット読込
        String formatDetail = readFormat(formatFileName, formatCharSet);

        // エラー通知フォーマット内容のエラー判定項目の数を数えて、0か奇数個の時には例外を出す。
        if (this.isDecisionStrNumError(formatDetail, false, 2111,
                formatFileName)) {
            throw new BfepStpException();
        }
        this.format = formatDetail;

        // エラー通知ディレクトリ名のエラー判定項目の数を数えて、奇数個の時には例外を出す。
        if (this.isDecisionStrNumError(dirName, true, 2112, dirName)) {
            throw new BfepStpException();
        }
        this.dirName = dirName;

        // エラー通知ファイル名のエラー判定項目の数を数えて、0か奇数個の時には例外を出す。
        if (this.isDecisionStrNumError(fileName, false, 2112, fileName)) {
            throw new BfepStpException();
        }
        this.fileName = fileName;

        // エラー通知ファイルキャラセット
        try {

            this.fileCharSet = Charset.forName(fileCharSet);
        } catch (Exception e) {

            logger.error("文字コード不正", e);
            stplogger.log(2113, fileCharSet, e.getMessage());
            throw new BfepStpException(e.getMessage(), e);
        }

        // エラー通知情報リストにセットする
        this.errorInfoSet = new LinkedHashSet<String>();

        // エラー通知フォーマットの内容を判定文字で分割して、判定文字に挟まれた内容をリストに格納
        this.getElements(this.format);

        // エラー通知ディレクトリ名の内容を判定文字で分割して、判定文字に挟まれた内容をリストに格納
        this.getElements(this.dirName);

        // エラー通知ファイル名の内容を判定文字で分割して、判定文字に挟まれた内容をリストに格納
        this.getElements(this.fileName);

        // エラー通知情報取得SQL文生成
        this.sqlFormat = setSql(tableName, conditional);

        // データソース
        this.datasource = datasource;
    }

    /**
     * プロセス
     * 
     * @param exchange
     *            処理対象の Exchange<BR>
     * @throws BfepStpException
     *             STPソリューション固有例外<BR>
     */
    public void process(Exchange exchange) throws BfepStpException {

        exchange.getIn().setHeader(PARAMETER_NAME_ERROR_TSUCHI_ERROR, false);

        // SQL実行
        List<String> errorInfoList = this.getErrorInfoList(exchange);

        // レコード件数が０件ならば、処理終了
        if (errorInfoList == null) {

            exchange.getIn().setHeader(PARAMETER_NAME_ERROR_TSUCHI_ERROR, true);
            return;
        }

        // エラー通知ディレクトリの作成する。
        String newDirName = this.makeErrorDir(errorInfoList, this.dirName,
                exchange);

        // エラー通知フォーマットのファイルを作成する。
        this.makeErrorFile(errorInfoList, newDirName, this.fileName,
                this.format, exchange);
    }

    /**
     * エラー通知項目判定文字の数が不正[0(acceptZeroがtrueの場合は許容)か奇数]か判定する。
     * 
     * @param element
     *            比較対象<BR>
     * @param acceptZero
     *            trueであれば、エラー通知項目判定文字数が0を許容する<BR>
     * @param logMessage
     *            ログ出力情報<BR>
     * @param messageNum
     *            メッセージ<BR>
     * @return 不正ならtrueを返す<BR>
     */
    private boolean isDecisionStrNumError(String element, boolean acceptZero,
            int messageNum, String logMessage) {

        // 判定項目数は正しい
        boolean isError = false;

        // 正規化された判定項目で、対象を分割する
        String[] elementArray = element.split(Matcher
                .quoteReplacement(this.decisionStr));

        // 判定項目数
        int decisionStrNum = elementArray.length - 1;
        if (element.endsWith(this.decisionStr)) {
            decisionStrNum += 1;
        }

        // 判定項目数が0(acceptZeroがtrueの場合は許容)か奇数
        if ((!acceptZero && decisionStrNum == 0) || decisionStrNum % 2 == 1) {

            // ログ出力
            stplogger.log(messageNum, logMessage, decisionStrNum);
            isError = true;
        }

        return isError;
    }

    /**
     * 引数の文字列を判定文字で分割したときに判定文字で挟まれた値をリストに格納する。<BR>
     * （インデックス値が０は偶数の扱い。）
     * 
     * @param terget
     *            対象文字列<BR>
     */
    private void getElements(String terget) {

        String[] elementArray = terget.split(Matcher
                .quoteReplacement(this.decisionStr));
        for (int index = 0; index < elementArray.length; index++) {
            if (index % 2 == 1) {
                this.errorInfoSet.add(elementArray[index]);
            }
        }
    }

    /**
     * SQL文作成
     * 
     * @param tableName
     *            検索テーブル名<BR>
     * @param conditional
     *            SQL検索条件<BR>
     * @return SQL文<BR>
     */
    private String setSql(String tableName, String conditional) {

        // SQL生成
        StringBuilder sql = new StringBuilder("SELECT ");

        int columnNumber = 1;
        for (String setItem : this.errorInfoSet) {

            if (columnNumber > 1) {
                sql.append(", ");
            }
            sql.append(setItem);
            sql.append(" AS ");
            sql.append(COLUMN_NAME);
            sql.append(columnNumber);
            columnNumber++;
        }
        sql.append(" FROM ");
        sql.append(tableName);

        // 値が存在しているときのみWHERE句を連結する
        if (conditional != null && !"".equals(conditional)) {
            sql.append(" WHERE ");
            sql.append(conditional);
        }

        // 作成済みのSQLを確認
        logger.info("エラー通知情報取得SQL:" + sql.toString());
        return sql.toString();
    }

    /**
     * エラー通知フォーマット（ファイル）の読み込み
     * 
     * @param formatFileName
     *            エラー通知フォーマットファイル名<BR>
     * @param formatCharSet
     *            エラー通知フォーマットファイル文字コード<BR>
     * @return エラー通知フォーマット内容<BR>
     * @throws BfepStpException
     *             STPソリューション固有例外<BR>
     */
    private String readFormat(String formatFileName, String formatCharSet)
            throws BfepStpException {

        // エラー通知フォーマット
        String errNotificationFormat = "";
        try {

            BufferedReader br = new BufferedReader(new InputStreamReader(
                    new FileInputStream(new File(formatFileName)),
                    formatCharSet));
            try {

                int data;
                while ((data = br.read()) != -1) {
                    errNotificationFormat += String.valueOf((char) data);
                }
            } finally {

                br.close();
            }
        } catch (FileNotFoundException e) {

            logger.error("エラー通知フォーマット不存在", e);
            stplogger.log(2109, formatFileName, e.getMessage());
            throw new BfepStpException(e.getMessage(), e);
        } catch (UnsupportedEncodingException e) {

            logger.error("エラー通知フォーマット文字コード不正", e);
            stplogger.log(2110, formatFileName, e.getMessage());
            throw new BfepStpException(e.getMessage(), e);
        } catch (Exception e) {

            logger.error("エラー通知フォーマット読込み失敗", e);
            stplogger.log(2118, formatFileName, e.getMessage());
            throw new BfepStpException(e.getMessage(), e);
        }

        return errNotificationFormat;
    }

    /**
     * 動的に生成したSQLを実行する。
     * 
     * @param exchange
     *            処理対象の Exchange<BR>
     * @return DB検索結果<BR>
     * @throws BfepStpException
     *             STPソリューション固有例外<BR>
     */
    private List<String> getErrorInfoList(Exchange exchange)
            throws BfepStpException {

        List<String> returnList = null;
        List<?> conditionList = (List<?>) exchange.getIn().getHeader(
                PARAMETER_NAME_ERROR_CONDITION);

        try {

            Connection conn = this.datasource.getConnection();
            try {

                PreparedStatement pstmt = conn.prepareStatement(this.sqlFormat);
                try {

                    int prmIndex = 0;
                    for (int index = 0; index < conditionList.size(); index++) {
                        prmIndex = index + 1;
                        pstmt.setString(prmIndex,
                                (String) conditionList.get(index));
                    }

                    ResultSet rs = pstmt.executeQuery();
                    try {

                        // 一件も取得できない
                        if (!rs.next()) {
                            stplogger.routeLog(2114, exchange,
                                    (String) exchange.getIn().getMessageId());
                            return returnList;
                        }

                        // 一行目ならば返却リストに内容を設定
                        if (rs.getRow() == 1) {
                            returnList = new ArrayList<String>();

                            for (int index = 0; index < this.errorInfoSet
                                    .size(); index++) {

                                returnList.add(rs.getString(index + 1));
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

        return returnList;
    }

    /**
     * エラー通知ファイル格納先を作成する。
     * 
     * @param errorInfoList
     *            エラー通知情報リスト<BR>
     * @param dirName
     *            置換前エラー通知ファイル格納先<BR>
     * @param exchange
     *            処理対象の Exchange<BR>
     * @return 置換後エラー通知ファイル格納先<BR>
     * @throws BfepStpException
     *             STPソリューション固有例外<BR>
     */
    private String makeErrorDir(List<String> errorInfoList, String dirName,
            Exchange exchange) throws BfepStpException {

        // 判定項目置換処理
        String replaceDirName = this.elementReplace(errorInfoList, dirName);

        // エラーディレクトリの作成
        try {

            // 指定ディレクトリが無い場合には、新規にディレクトリを作成
            File dirs = new File(replaceDirName);
            if (!dirs.exists()) {

                // ディレクトリの作成失敗の時
                if (!dirs.mkdirs()) {
                    stplogger.routeLog(2115, exchange, replaceDirName);
                    throw new BfepStpException();
                }
            }
        } catch (SecurityException e) {

            logger.error("ディレクトリ作成失敗", e);
            stplogger.routeLog(2116, exchange, replaceDirName, e.getMessage());
            throw new BfepStpException(e.getMessage(), e);
        }

        return replaceDirName;
    }

    /**
     * エラーファイルを作成する。
     * 
     * @param errorInfoList
     *            エラー通知情報リスト<BR>
     * @param dirName
     *            エラー通知ファイル格納先<BR>
     * @param fileName
     *            置換前エラー通知ファイル名<BR>
     * @param format
     *            置換前エラー通知ファイル内容<BR>
     * @param exchange
     *            処理対象の Exchange<BR>
     * @throws BfepStpException
     *             STPソリューション固有例外<BR>
     */
    private void makeErrorFile(List<String> errorInfoList, String dirName,
            String fileName, String format, Exchange exchange)
            throws BfepStpException {

        // フォーマットの判定項目置換処理
        String replaceFormat = this.elementReplace(errorInfoList, format);

        // ファイル名の判定項目置換処理
        String replaceFileName = this.elementReplace(errorInfoList, fileName);

        try {

            BufferedWriter fp = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(new File(dirName, replaceFileName)),
                    this.fileCharSet));
            try {

                fp.write(replaceFormat);
                fp.flush();
            } finally {

                fp.close();
            }
        } catch (Exception e) {

            logger.error("エラー通知ファイル作成失敗", e);
            File errorPath = new File(dirName, replaceFileName);
            stplogger.routeLog(2117, exchange, errorPath.getPath(),
                    e.getMessage());
            throw new BfepStpException(e.getMessage(), e);
        }
    }

    /**
     * エラー通知フォーマット等の置換を行う。
     * 
     * @param errorInfoList
     *            エラー通知情報リスト<BR>
     * @param element
     *            置換前対象<BR>
     * @return 置換後対象<BR>
     */
    private String elementReplace(List<String> errorInfoList, String element) {

        // 初期化
        String replaceElement = element;
        String newElement = "";

        // 置換基文字列の取得と置換
        int index = 0;
        for (String baseElement : this.errorInfoSet) {

            // 置換基文字列を置換対象文字列にするため、判定文字を前後に付与
            String targetElement = this.decisionStr + baseElement
                    + this.decisionStr;

            String value = "";
            if (errorInfoList.get(index) != null) {
                value = errorInfoList.get(index);
            }

            // 連続置換
            newElement = replaceElement.replaceAll(
                    Pattern.quote(targetElement), value);
            replaceElement = newElement;
            index++;
        }

        return newElement;
    }
}

