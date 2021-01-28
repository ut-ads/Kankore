/**
 * 
 */
package jp.co.nttdg.bfep.stp.bean;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.co.nttdg.bfep.stp.framework.BfepStpException;
import jp.co.nttdg.bfep.stp.framework.BfepStpLogger;
import jp.co.nttdg.bfep.stp.framework.BfepStpLoggerFactory;
import jp.co.nttdg.bfep.stp.framework.ByteArrayUtil;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 電文変換 スタブ
 * 
 * @author h_tozawa
 * @version 1.0
 */
public class Translator {

    /** トレースロガー */
    private static final Logger logger = LoggerFactory
            .getLogger(Translator.class);

    /** B-FEP STPロガー */
    private static final BfepStpLogger stpLogger = BfepStpLoggerFactory
            .getLogger(Translator.class);

    /**
     * 本オブジェクトを構築する
     * 
     * @param dataFormat
     *            変換元情報のデータ形式
     * @param serviceName
     *            変換元情報のサービス名
     * @param releaseYear
     *            変換元情報のリリースバージョン
     */
    public Translator(String dataFormat, String serviceName, String releaseYear) {
        /*
         * 変換元情報
         */
        logger.info(String
                .format("電文変換-スタブコンストラクタ参考情報  変換元情報のデータ形式=%s 変換元情報のサービス名=%s 変換元情報のリリースバージョン=%s",
                        dataFormat, serviceName, releaseYear));

    }

    /**
     * 電文フォーマット変換
     * 
     * @param exchange
     * @throws IOException
     */
    public void process(Exchange exchange) throws IOException {
        Message in = exchange.getIn();
        byte[] body = (byte[]) in.getBody();

        in.setHeader("jp.co.nttdg.bfep.stp.translateError", false);

        // 指定ファイルの読み込み
        StringBuilder regular = new StringBuilder("TRANSLATOR_");
        regular.append(".*#");

        if ((String) exchange.getIn().getHeader(
                "jp.co.nttdg.bfep.stp.hub.message.systemCdFrom") != null) {

            regular.append((String) exchange.getIn().getHeader(
                    "jp.co.nttdg.bfep.stp.hub.message.systemCdFrom"));
        }
        regular.append("#");

        if ((String) exchange.getIn().getHeader(
                "jp.co.nttdg.bfep.stp.hub.message.kessaiDt") != null) {

            regular.append((String) exchange.getIn().getHeader(
                    "jp.co.nttdg.bfep.stp.hub.message.kessaiDt"));

        }

        File file = new File(new File("./").getCanonicalPath());

        String[] files = file.list(getFileExtensionFilter(regular.toString()));

        if (files.length == 0) {

            // 指定ファイルの読み込み

            regular = new StringBuilder("TRANSLATOR_");
            regular.append(".*#");
            if ((String) exchange.getIn().getHeader(
                    "jp.co.nttdg.bfep.stp.hub.message.systemCdFrom") != null) {

                regular.append((String) exchange.getIn().getHeader(
                        "jp.co.nttdg.bfep.stp.hub.message.systemCdFrom"));
            }
            regular.append("#");

            if ((String) exchange.getIn().getHeader(
                    "jp.co.nttdg.bfep.stp.hub.message.tsuban") != null) {

                regular.append((String) exchange.getIn().getHeader(
                        "jp.co.nttdg.bfep.stp.hub.message.tsuban"));

            }

            files = file.list(getFileExtensionFilter(regular.toString()));

        }

        logger.info(String.format("正規化ファイル名=%s", regular.toString()));

        String fileName = "";

        if (files.length != 0) {
            fileName = files[0].substring(0, files[0].indexOf("#"));
        } else {
            regular = new StringBuilder("TRANSLATOR_");
            regular.append(".*");
            file = new File(new File("./").getCanonicalPath());
            files = file.list(getFileExtensionFilter(regular.toString()));
            for (String item : files) {
                if (item.indexOf("#") == -1) {
                    fileName = item;
                    break;
                }
            }
        }

        logger.info(String.format("電文変換ファイル取得 ファイル名=%s", fileName));

        String[] spritFileName = fileName.split("_");

        // StringBuilder regular = new StringBuilder("TRANSLATOR_");
        // regular.append(".*");
        // String[] files = new String[0];
        // File file = new File(new File("./").getCanonicalPath());
        // files = file.list(getFileExtensionFilter(regular.toString()));
        //
        // logger.info(String.format("電文変換ファイル取得 ファイル名=%s", files[0]));
        //
        // String[] spritFileName = files[0].split("_");

        // 分割したファイル名にNGがあるときは、電文変換失敗とする。

        if (spritFileName.length > 1) {
            if ("NG".equals(spritFileName[1])) {

                exchange.getIn().setHeader(
                        "jp.co.nttdg.bfep.stp.translateError", true);
                stpLogger.routeLog(2005, exchange, "");
            }
        }

        // ボディの設定

        List<Byte> byteDatalist = null;

        if (spritFileName.length > 1) {

            String data = spritFileName[1] + ":";
            byte[] byteData = data.getBytes();
            byteDatalist = ByteArrayUtil.asList(byteData);

        } else {

            byteDatalist = new ArrayList<Byte>();

        }

        ByteArrayUtil.appendOfRange(byteDatalist, body, 0, body.length);
        in.setBody(ByteArrayUtil.toArray(byteDatalist));

        logger.info(String.format("変換前ボディ情報  バイト数=%d", body.length));
        logger.info(String.format("変換後ボディ情報  バイト数=%d",
                ByteArrayUtil.toArray(byteDatalist).length));
    }

    /**
     * 業務日付とMA通番の位置情報の設定
     * 
     * @param positions
     *            業務日付とMA通番の位置情報
     * @throws BfepStpException
     *             不正な位置情報<br>
     *             XMLフォーマットのときは不正なXPath式<br>
     *             固定長フォーマットのときは不正な整数値
     */
    public void setIdPosition(List<String> positions) throws BfepStpException {
    }

    /**
     * 変換先情報の設定
     * 
     * @param destination
     *            変換先情報（データ形式,サービス名,リリースバージョンの順）
     */
    public void setDestination(List<String> destination) {
    }

    /**
     * 　正規表現ファイルと一致したファイル名が返却するフィルタ
     * 
     * @param regular
     *            正規表現<BR>
     * @return 正規表現ファイルと一致したファイル名<BR>
     */
    private static FilenameFilter getFileExtensionFilter(String regular) {
        final Pattern ptn = Pattern.compile(regular);

        return new FilenameFilter() {
            public boolean accept(File file, String name) {
                Matcher match = ptn.matcher(name);
                boolean ret = match.matches();
                return ret;
            }
        };
    }

}

