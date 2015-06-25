/**
 * VSChmJanController.java
 */

package wiz.project.janbot.game;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import wiz.project.ircbot.IRCBOT;
import wiz.project.jan.Hand;
import wiz.project.jan.JanPai;
import wiz.project.jan.Wind;
import wiz.project.jan.util.JanPaiUtil;
import wiz.project.janbot.game.exception.JanException;



/**
 * 中国麻雀対戦用コントローラ
 */
class VSChmJanController implements JanController {
    
    /**
     * コンストラクタ
     */
    public VSChmJanController() {
    }
    
    
    
    /**
     * 和了 (ツモ)
     */
    public void completeTsumo(final JanInfo info) throws JanException {
        // TODO 未実装
    }
    
    /**
     * 打牌 (ツモ切り)
     */
    public void discard(final JanInfo info) throws JanException {
        discardCore(info, info.getActiveTsumo());
        next(info);
    }
    
    /**
     * 打牌 (手出し)
     */
    public void discard(final JanInfo info, final JanPai target) throws JanException {
        // TODO 未実装
    }
    
    /**
     * 次のプレイヤーの打牌へ
     */
    public void next(final JanInfo info) throws JanException {
        if (info == null) {
            throw new NullPointerException("Jan info is null.");
        }
        
        if (info.getRemainCount() == 0) {
            // TODO オブザーバパターンでGameMasterに通知する
            // 今はダミー実装ということで直接呼んだ
            GameMaster.getInstance().update(info, null);
            
            // ゲーム終了
            return;
        }
        
        info.setActiveWindToNext();
        onPhase(info);
    }
    
    /**
     * 開始
     */
    public void startGame(final JanInfo info, final List<String> playerNameList) throws JanException {
        if (info == null) {
            throw new NullPointerException("Jan info is null.");
        }
        if (playerNameList == null) {
            throw new NullPointerException("Player name list is null.");
        }
        if (playerNameList.isEmpty()) {
            throw new NullPointerException("Player name list is empty.");
        }
        
        // 現状席決めのみ
        
        // 風をシャッフル
        final List<Wind> windList = new ArrayList<>(Arrays.asList(Wind.values()));
        Collections.shuffle(windList, new SecureRandom());
        
        // プレイヤーを格納
        final Map<Wind, Player> playerTable = new TreeMap<>();
        for (final String playerName : playerNameList) {
            playerTable.put(windList.remove(0), new Player(playerName, PlayerType.HUMAN));
        }
        
        // 4人になるまでNPCで埋める
        final int limitCOM = 4 - playerNameList.size();
        for (int i = 0; i < limitCOM; i++) {
            playerTable.put(windList.remove(0), NPC_LIST.get(i));
        }
        
        info.setFieldWind(Wind.TON);
        info.setPlayerTable(playerTable);
    }
    
    /**
     * 局を開始
     */
    public void startRound(final JanInfo info) throws JanException {
        if (info == null) {
            throw new NullPointerException("Jan info is null.");
        }
        
        // 牌山を生成
        final List<JanPai> deck = createDeck();
        info.setDeck(deck);
        
        // 配牌
        info.setHand(Wind.TON, new Hand(new ArrayList<JanPai>(deck.subList( 0, 13))));
        info.setHand(Wind.NAN, new Hand(new ArrayList<JanPai>(deck.subList(13, 26))));
        info.setHand(Wind.SHA, new Hand(new ArrayList<JanPai>(deck.subList(26, 39))));
        info.setHand(Wind.PEI, new Hand(new ArrayList<JanPai>(deck.subList(39, 52))));
        info.setDeckIndex(13 * 4);
        info.setDeckWallIndex(34 * 4 - 1 - 1);
        info.setRemainCount(84);  // 中国麻雀は王牌がないため、残り枚数は84枚 ※花牌を除く
        
        // TODO 待ち牌リストの構築
        
        // 一巡目へ (親の14枚目はこの先でツモらせる)
        info.setActiveWind(Wind.TON);
        onPhase(info);
    }
    
    
    
    /**
     * 牌山を生成
     * 
     * @return 牌山。
     */
    private List<JanPai> createDeck() {
        final List<JanPai> deck = JanPaiUtil.createAllJanPaiList();
        Collections.shuffle(deck, new SecureRandom());
        return deck;
    }
    
    /**
     * 牌を切る
     * 
     * @param info ゲーム情報。
     * @param target 対象牌。
     */
    private void discardCore(final JanInfo info, final JanPai target) {
        final Wind activeWind = info.getActiveWind();
        info.addDiscard(activeWind, target);
        info.setActiveDiscard(target);
        
        // TODO 直接IRCBOTを叩かずにアナウンサーにやらせたい
        IRCBOT.getInstance().println(info.getActivePlayer().getName() + "捨牌： " + info.getActiveRiver().toString());
        
        // TODO 鳴き確認処理
    }
    
    /**
     * 牌をツモる
     * 
     * @param info ゲーム情報。
     * @return ツモ牌。
     */
    private JanPai getJanPaiFromDeck(final JanInfo info) {
        final JanPai pai = info.getJanPaiFromDeck();
        info.increaseDeckIndex();
        info.decreaseRemainCount();
        return pai;
    }
    
    /**
     * 巡目ごとの処理
     * 
     * @param info ゲーム情報。
     * @throws JanException ゲーム処理例外。
     */
    private void onPhase(final JanInfo info) throws JanException {
        // 牌をツモる
        final JanPai activeTsumo = getJanPaiFromDeck(info);
        info.setActiveTsumo(activeTsumo);
        
        // 打牌
        final Player activePlayer = info.getActivePlayer();
        switch (activePlayer.getType()) {
        case COM:
            // ツモ切り
            discard(info);
            break;
        case HUMAN:
            // TODO 直接IRCBOTを叩かずにアナウンサーにやらせたい
            
            // 肉入りがアクティブになったらオープンに「○○ のターン」を表示？
            IRCBOT.getInstance().println(activePlayer.getName() + " のターン！");
            
            // 入力待ちメッセージ
            IRCBOT.getInstance().talk(activePlayer.getName(), info.getActiveHand().toString() + " " + activeTsumo.toString());
            IRCBOT.getInstance().talk(activePlayer.getName(), "現状ツモ切りしかできません。");
            break;
        }
    }
    
    
    
    /**
     * NPCリスト
     */
    private static final List<Player> NPC_LIST =
        Collections.unmodifiableList(Arrays.asList(new Player("COM_01", PlayerType.COM),
                                                   new Player("COM_02", PlayerType.COM),
                                                   new Player("COM_03", PlayerType.COM),
                                                   new Player("COM_04", PlayerType.COM)));
    
}

