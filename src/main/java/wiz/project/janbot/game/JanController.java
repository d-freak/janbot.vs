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
     * 吃
     * 
     * @param info ゲーム情報。
     * @param call 鳴き情報。
     * @throws JanException 例外イベント。
     */
    public void chi(final JanInfo info, final CallInfo call) throws JanException;
    
    /**
     * 和了 (ツモ)
     * 
     * @param info ゲーム情報。
     * @throws JanException 例外イベント。
     */
    public void completeTsumo(final JanInfo info) throws JanException;
    
    /**
     * 和了 (ロン)
     * 
     * @param info ゲーム情報。
     * @param call 鳴き情報。
     */
    public void completeRon(final JanInfo info, final CallInfo call);
    
    /**
     * 打牌 (ツモ切り)
     * 
     * @param info ゲーム情報。
     */
    public void discard(final JanInfo info);
    
    /**
     * 打牌 (手出し)
     * 
     * @param info ゲーム情報。
     * @param target 捨て牌。
     * @throws JanException 例外イベント。
     */
    public void discard(final JanInfo info, final JanPai target) throws JanException;
    
    /**
     * 打牌 (手出し)
     * 
     * @param info ゲーム情報。
     * @param target 捨て牌。
     * @param afterCall 鳴き直後の打牌か。
     * @throws JanException 例外イベント。
     */
    public void discard(final JanInfo info, final JanPai target, final boolean afterCall) throws JanException;
    
    /**
     * 大明槓
     * 
     * @param info ゲーム情報。
     * @param call 鳴き情報。
     */
    public void kanCall(final JanInfo info, final CallInfo call);
    
    /**
     * 暗槓/加槓
     * 
     * @param info ゲーム情報。
     * @param target 対象牌。
     */
    public void kanHand(final JanInfo info, final JanPai target) throws JanException;
    
    /**
     * 次のプレイヤーの打牌へ
     * 
     * @param info ゲーム情報。
     */
    public void next(final JanInfo info);
    
    /**
     * 碰
     * 
     * @param info ゲーム情報。
     * @param call 鳴き情報。
     */
    public void pon(final JanInfo info, final CallInfo call);
    
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

