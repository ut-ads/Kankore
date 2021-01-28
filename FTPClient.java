/*
 * @(#)FTPClient.java
 *
 * Copyright (c) 2014, NTT DATA Getronics Corporation.
 */
package jp.co.nttdg.bfep.stp.bean.hub;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.co.nttdg.bfep.stp.framework.BfepStpException;
import jp.co.nttdg.bfep.stp.framework.BfepStpLogger;
import jp.co.nttdg.bfep.stp.framework.BfepStpLoggerFactory;
import jp.co.nttdg.bfep.stp.framework.ByteArrayUtil;

import org.apache.camel.Exchange;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * FTPクライアントBean<BR>
 *
 * @author h_tozawa
 * @version 1.0
 *
 */
public class FTPClient {

    /** トレースロガー */
    private static final Logger logger = LoggerFactory
            .getLogger(FTPClient.class);

    /** B-FEP STPロガー */
    private static final BfepStpLogger stplogger = BfepStpLoggerFactory
            .getLogger(FTPClient.class);

    /** FTPクライアント */
    private final org.apache.commons.net.ftp.FTPClient ftpClient;

    /** IPアドレス */
    private final String ipAddress;

    /** ログインユーザ */
    private final String user;

    /** ログインパスワード */
    private final String password;

    /** パッシブモード */
    private final boolean isPassive;

    /** Exchange */
    private Exchange exchange = null;

    /*
     * 定数定義
     */
    /** 削除ファイルパスリスト */
    private static final String PARAMETER_NAME_FTP_DELETE_PATHS = "jp.co.nttdg.bfep.stp.hub.ftp.deletePaths";

    /**
     * コンストラクタ
     *
     * @param ipAddress
     *            IPアドレス
     * @param user
     *            ユーザー名
     * @param password
     *            パスワード
     * @param isPassive
     *            パッシブモード
     */
    public FTPClient(String ipAddress, String user, String password,
            String passive) {

        // メンバ変数に引数の保存
        this.ipAddress = ipAddress;
        this.user = user;
        this.password = password;
        this.isPassive = Boolean.valueOf(passive);

        // インスタンス生成
        this.ftpClient = new org.apache.commons.net.ftp.FTPClient();
    }

    /**
     * FTPサーバへの接続を行う。FTPクライアントの設定もここで行う。
     *
     * @throws BfepStpException
     *            STPソリューション固有例外<BR>
     */
    public void connect() throws BfepStpException {

        // FTPサーバーに接続
        try {
            ftpClient.connect(this.ipAddress);
            int reply = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {

                // ログ出力
                stplogger.routeLog(5000, exchange, this.ipAddress);
                this.disconnect();
                throw new BfepStpException();
            }
        } catch (SocketException e) {

            // ログ出力
            logger.error("FTP接続失敗", e);
            stplogger.routeLog(5001, exchange, this.ipAddress, e.getMessage());
            this.disconnect();
            throw new BfepStpException(e.getMessage(), e);
        } catch (IOException e) {

            // ログ出力
            logger.error("FTP接続失敗", e);
            stplogger.routeLog(5001, exchange, this.ipAddress, e.getMessage());
            this.disconnect();
            throw new BfepStpException(e.getMessage(), e);
        }

        // ログイン
        try {
            if (!this.ftpClient.login(this.user, this.password)) {

                // ログ出力
                stplogger.routeLog(5002, exchange, this.ipAddress, this.user);

                // 切断処理
                this.disconnect();
                throw new BfepStpException();
            }

        } catch (IOException e) {

            // ログ出力
            logger.error("FTPログイン失敗", e);
            stplogger.routeLog(5003, exchange, this.ipAddress, this.user,
                    e.getMessage());

            // 切断処理
            this.disconnect();
            throw new BfepStpException(e.getMessage(), e);
        }

        // パッシブモードのON/OFFを設定
        if (isPassive) {
            this.ftpClient.enterLocalPassiveMode();
        } else {
            this.ftpClient.enterLocalActiveMode();
        }

        // ファイル転送モードの設定
        // バイナリモードに固定
        try {
            if (!this.ftpClient.setFileType(FTP.BINARY_FILE_TYPE)) {

                // ログ出力
                stplogger.routeLog(5010, exchange, FTP.BINARY_FILE_TYPE);

                // 切断処理
                this.disconnect();
                throw new BfepStpException();
            }
        } catch (IOException e) {

            // ログ出力
            logger.error("FTP転送モード変更失敗", e);
            stplogger.routeLog(5010, exchange, FTP.BINARY_FILE_TYPE,
                    e.getMessage());

            // 切断処理
            this.disconnect();
            throw new BfepStpException(e.getMessage(), e);
        }
    }

    /**
     * FTPサーバとの接続を解除する。
     *
     * @throws BfepStpException
     *            STPソリューション固有例外<BR>
     */
    public void disconnect() throws BfepStpException {

        // サーバーと接続が確立している時
        if (this.ftpClient.isAvailable()) {

            // ログアウトした後、接続を解除する。
            try {
                this.ftpClient.logout();
            } catch (IOException e) {

                // ログ出力
                logger.error("FTPログアウト失敗", e);
                stplogger.routeLog(5010, exchange, e.getMessage());
                throw new BfepStpException(e.getMessage(), e);
            }

            try {
                this.ftpClient.disconnect();
            } catch (IOException e) {

                // ログ出力
                logger.error("FTP接続解除失敗", e);
                stplogger.routeLog(5010, exchange, e.getMessage());
                throw new BfepStpException(e.getMessage(), e);
            }
        }
    }

    /**
     * 第一引数で与えられたディレクトリのパスから、 第二引数の正規表現に一致する名前のFTPFileオブジェクトをリストで取得する。
     *
     * @param dirPath
     *            ディレクトリパス
     * @param regular
     *            正規表現文字列
     * @return
     *            FTPFileリスト
     * @throws BfepStpException
     *            STPソリューション固有例外<BR>
     */
    public List<FTPFile> listFiles(String dirPath, String regular)
            throws BfepStpException {

        List<FTPFile> ftpFileList = new ArrayList<FTPFile>();

        boolean execConnect = false;

        // サーバーとの接続状態確認
        if (!this.ftpClient.isAvailable()) {
            this.connect();
            execConnect = true;
        }

        // カレントディレクトリを移動
        try {

            if (!this.ftpClient.changeWorkingDirectory(dirPath)) {

                // ログ出力
                stplogger.routeLog(5004, exchange, this.ipAddress, dirPath);

                // 本メソッド内で接続処理を行った
                if (execConnect) {
                    this.disconnect();
                }

                throw new BfepStpException();
            }

        } catch (IOException e) {

            // ログ出力
            logger.error("FTPカレントディレクトリ移動失敗", e);
            stplogger.routeLog(5004, exchange, dirPath, e.getMessage());

            // 本メソッド内で接続処理を行った
            if (execConnect) {
                this.disconnect();
            }

            throw new BfepStpException(e.getMessage(), e);
        }

        // ファイルリスト(ファイルの配列)を取得
        FTPFile[] list = null;
        try {
            list = ftpClient.listFiles(dirPath);
            if (list != null) {
                for (int index = 0; index < list.length; index++) {

                    // ファイル名取得
                    String name = list[index].getName();

                    // ファイルタイプ取得
                    int type = list[index].getType();

                    // ファイルであり、第二引数で与えられた正規表現に一致するファイル名
                    Pattern ptn = Pattern.compile(regular);
                    Matcher match = ptn.matcher(name);

                    if (FTPFile.FILE_TYPE == type && match.matches()) {
                        ftpFileList.add(list[index]);
                    }
                }
            }
        } catch (IOException e) {

            // ログ出力
            logger.error("FTPファイルリスト取得失敗", e);
            stplogger
                    .routeLog(5005, exchange, dirPath, regular, e.getMessage());

            // 本メソッド内で接続処理を行った
            if (execConnect) {
                this.disconnect();
            }

            throw new BfepStpException(e.getMessage(), e);
        }

        // 本メソッド内で接続処理を行った
        if (execConnect) {
            this.disconnect();
        }

        return ftpFileList;
    }

    /**
     * 引数で与えられた情報から、バイト配列でファイル内容を取得する。
     *
     * @param filePath
     *            ファイル格納パス<BR>
     * @return
     *            byte[] FTPで取得したバイト配列<BR>
     * @throws BfepStpException
     *            STPソリューション固有例外<BR>
     */
    public byte[] getFile(String filePath) throws BfepStpException {

        // サーバーとの接続状態確認
        boolean execConnect = false;
        if (!this.ftpClient.isAvailable()) {
            this.connect();
            execConnect = true;
        }

        List<Byte> byteList = new ArrayList<Byte>();
        BufferedInputStream buffInputStream = null;

        try {
            // ファイルパスのファイルデータを一気に取得
            buffInputStream = new BufferedInputStream(
                    this.ftpClient.retrieveFileStream(filePath));

            int reply = this.ftpClient.getReplyCode();
            if (buffInputStream == null
                    || (!FTPReply.isPositivePreliminary(reply) && !FTPReply
                            .isPositiveCompletion(reply))) {
                stplogger.routeLog(5010, exchange, ipAddress, filePath);
                // 本メソッド内で接続処理を行った
                if (execConnect) {
                    this.disconnect();
                }
                throw new BfepStpException(this.ftpClient.getReplyString());
            }
            int ch = 0;

            while ((ch = buffInputStream.read()) != -1) {
                byteList.add((byte) ch);
            }

            // 読込処理完了
            if (!this.ftpClient.completePendingCommand()) {

                stplogger.routeLog(5006, exchange, ipAddress, filePath);
                // 本メソッド内で接続処理を行った
                if (execConnect) {
                    this.disconnect();
                }
                throw new BfepStpException();
            }

        } catch (IOException e) {

            // ログ出力
            logger.error("FTPファイル読込み失敗", e);
            stplogger.routeLog(5007, exchange, ipAddress, filePath,
                    e.getMessage());

            // 本メソッド内で接続処理を行った
            if (execConnect) {
                this.disconnect();
            }
            throw new BfepStpException(e.getMessage(), e);

        } finally {

            // クローズ処理
            if (buffInputStream != null) {
                try {
                    buffInputStream.close();
                } catch (IOException e) {

                    // ログ出力
                    logger.error("FTPファイルクローズ失敗", e);
                    stplogger.routeLog(5010, exchange, ipAddress, filePath,
                            e.getMessage());
                    throw new BfepStpException(e.getMessage(), e);
                }
            }

            // 本メソッド内で接続処理を行った
            if (execConnect) {
                this.disconnect();
            }
        }

        return ByteArrayUtil.toArray(byteList);
    }

    /**
     * 引数で与えられたパスのファイルを削除する。
     *
     * @param filePath
     *            削除対象ファイルパス
     * @throws BfepStpException
     *            STPソリューション固有例外<BR>
     */
    public void deleteFile(String filePath) throws BfepStpException {

        boolean execConnect = false;

        // サーバーとの接続状態確認
        if (!ftpClient.isAvailable()) {
            this.connect();
            execConnect = true;
        }

        try {

            // ファイルの削除に失敗した場合
            if (!ftpClient.deleteFile(filePath)) {

                // アプリログ出力
                stplogger.routeLog(5008, exchange, ipAddress, filePath);
                throw new BfepStpException();
            }

        } catch (IOException e) {

            logger.error("FTPファイル削除失敗", e);
            stplogger.routeLog(5009, exchange, ipAddress, filePath,
                    e.getMessage());
            // 本メソッド内で接続処理を行った
            if (execConnect) {
                this.disconnect();
            }
            throw new BfepStpException(e.getMessage(), e);
        } finally {

            // 本メソッド内で接続処理を行った
            if (execConnect) {
                this.disconnect();
            }
        }
    }

    /**
     * 引数に与えられたExchangeのheader部に格納されたリストに該当するアイルを削除する。
     *
     * @param exchange
     *            処理対象の Exchange<BR>
     * @throws BfepStpException
     *            STPソリューション固有例外<BR>
     */
    public void deleteFiles(Exchange exchange) throws BfepStpException {

        // 引数で渡されたExchangeオブジェクトをメンバ変数に設定
        this.setLogInfo(exchange);

        String filePath = null;

        // 削除対象ファイル名のリストを取得
        ArrayList<?> embeddingParamArray = (ArrayList<?>) exchange.getIn()
                .getHeader(PARAMETER_NAME_FTP_DELETE_PATHS);

        // nullは何もせず終了
        if (embeddingParamArray != null) {

            // 取得した各要素の値を引数にし、本BeanのdeleteFileメソッドを呼び出す。
            for (int i = 0; i < embeddingParamArray.size(); i++) {
                filePath = (String) embeddingParamArray.get(i);
                this.deleteFile(filePath);
            }
        }
    }

    /**
     * Exchangeを設定
     *
     * @param exchange
     *            処理対象の Exchange<BR>
     */
    public void setLogInfo(Exchange exchange) {
        this.exchange = exchange;
    }
}

