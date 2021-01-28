/**
 * 
 */
package jp.co.nttdg.bfep.stp.bean;

import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author h_tozawa
 * 
 */
public class PreRouter {

    /** トレースロガー */
    private static final Logger logger = LoggerFactory.getLogger(Router.class);

    // private static final BfepStpLogger stpLogger = BfepStpLoggerFactory
    // .getLogger(Router.class);

    /**
     * 初期化処理
     * 
     * @param routngFilePath
     *            経路情報ファイルのパス
     */
    public PreRouter(String routngFilePath) {
        logger.info(String.format("経路判定-スタブコンストラクタ参考情報  経路情報ファイルのパス=%s",
                routngFilePath));
    }

    /**
     * 経路判定前処理 - スタブ
     * 
     * @param exchange
     *            処理対象の Exchange
     */
    public void process(Exchange exchange) {
    }
}

