/**
 * 
 */
package jp.co.nttdg.bfep.stp.bean;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.co.nttdg.bfep.stp.framework.BfepStpException;
import jp.co.nttdg.bfep.stp.framework.BfepStpLogger;
import jp.co.nttdg.bfep.stp.framework.BfepStpLoggerFactory;

import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * * 経路判定２ スタブ
 * 
 * @author h_tozawa
 * @version 1.0
 * 
 */
public class Router2 {

    /** トレースロガー */
    private static final Logger logger = LoggerFactory.getLogger(Router2.class);

    private static final BfepStpLogger stpLogger = BfepStpLoggerFactory
            .getLogger(Router2.class);

    /**
     * 初期化処理
     * 
     * @param routngFilePath
     *            経路情報ファイルのパス
     * @throws BfepStpException
     *             経路判定エンジンの初期化失敗
     */
    public Router2(String routngFilePath) throws BfepStpException {
        logger.info(String.format("経路判定-スタブコンストラクタ参考情報  経路情報ファイルのパス=%s",
                routngFilePath));
    }

    /**
     * 経路判定処理 - スタブ
     * 
     * @param exchange
     *            処理対象の Exchange
     * @throws IOException
     */
    public void process(Exchange exchange) throws IOException {

        exchange.getIn().setHeader("jp.co.nttdg.bfep.stp.routingError", false);
        exchange.getIn()
                .setHeader("jp.co.nttdg.bfep.stp.fromMessageFormat", "");

        exchange.getIn().setHeader("jp.co.nttdg.bfep.stp.toMessageFormat", "");

        exchange.getIn().setHeader("jp.co.nttdg.bfep.stp.sendQueues",
                new ArrayList<String>());

        // 指定ファイルの読み込み
        StringBuilder regular = new StringBuilder("ROUTER2_");
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
            regular = new StringBuilder("ROUTER2_");
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

        // String[] files =
        // file.list(getFileExtensionFilter(regular.toString()));
        String fileName = "";

        if (files.length != 0) {
            fileName = files[0].substring(0, files[0].indexOf("#"));
        } else {
            regular = new StringBuilder("ROUTER2_");
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

        logger.info(String.format("経路判定ファイル取得 ファイル名=%s", fileName));

        String[] spritFileName = fileName.split("_");

        // 分割したファイル名にOKがあるときは、経路判定失敗とする。
        if ("NG".equals(spritFileName[1])) {
            exchange.getIn().setHeader("jp.co.nttdg.bfep.stp.routingError",
                    true);
            stpLogger.routeLog(2002, exchange, "");

            return;
        }
        // 変換元データIDは分割したファイル名の３番目
        exchange.getIn().setHeader("jp.co.nttdg.bfep.stp.fromMessageFormat",
                spritFileName[2]);
        // 変換後データIDは分割したファイル名の４番目
        exchange.getIn().setHeader("jp.co.nttdg.bfep.stp.toMessageFormat",
                spritFileName[3]);

        // キュー名は分割したファイル名の５番目
        if (spritFileName.length == 5) {

            if ("VOID".equals(spritFileName[4])) {
                exchange.getIn().setHeader("jp.co.nttdg.bfep.stp.sendQueues",
                        Arrays.asList(spritFileName[4]));

            } else {
                exchange.getIn().setHeader("jp.co.nttdg.bfep.stp.sendQueues",
                        Arrays.asList("jms:queue:" + spritFileName[4]));
            }

        }
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

