/**
 * JanController.java
 * 
 * @Author
 *   D-freak
 */

package wiz.project.janbot.game;

import java.util.List;

import wiz.project.jan.JanPai;
import wiz.project.janbot.game.exception.JanException;



/**
 * 麻雀コントローラ
 */
interface JanController {
    
    /**
     * 和了 (ツモ)
     * 
     * @param info ゲーム情報。
     * @throws JanException 例外イベント。
     */
    public void completeTsumo(final JanInfo info) throws JanException;
    
    /**
     * 打牌 (ツモ切り)
     * 
     * @param info ゲーム情報。
     * @throws JanException 例外イベント。
     */
    public void discard(final JanInfo info) throws JanException;
    
    /**
     * 打牌 (手出し)
     * 
     * @param info ゲーム情報。
     * @param target 捨て牌。
     * @throws JanException 例外イベント。
     */
    public void discard(final JanInfo info, final JanPai target) throws JanException;
    
    /**
     * 次のプレイヤーの打牌へ
     * 
     * @param info ゲーム情報。
     * @throws JanException 例外イベント。
     */
    public void next(final JanInfo info) throws JanException;
    
    /**
     * ゲーム開始
     * 
     * @param info ゲーム情報。
     * @param playerNameList プレイヤー名のリスト。
     * @throws JanException 例外イベント。
     */
    public void startGame(final JanInfo info, final List<String> playerNameList) throws JanException;
    
    /**
     * 局を開始
     * 
     * @param info ゲーム情報。
     * @throws JanException 例外イベント。
     */
    public void startRound(final JanInfo info) throws JanException;
    
}

