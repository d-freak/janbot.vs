/**
 * VSChmJanController.java
 * 
 * @Author
 *   D-freak
 */

package wiz.project.janbot.game;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import wiz.project.jan.Hand;
import wiz.project.jan.JanPai;
import wiz.project.jan.Wind;
import wiz.project.jan.util.HandCheckUtil;
import wiz.project.jan.util.JanPaiUtil;
import wiz.project.janbot.game.exception.BoneheadException;
import wiz.project.janbot.game.exception.InvalidInputException;
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
        if (info == null) {
            throw new NullPointerException("Jan info is null.");
        }
        
        final Map<JanPai, Integer> handWithTsumo = getHandMap(info, info.getActiveWind(), info.getActiveTsumo());
        if (!HandCheckUtil.isComplete(handWithTsumo)) {
            // チョンボ
            throw new BoneheadException("Not completed.");
        }
        
        info.notifyObservers(new AnnounceParam(info.getActivePlayer(), ANNOUNCE_FLAG_COMPLETE_TSUMO));
    }
    
    /**
     * 打牌 (ツモ切り)
     */
    public void discard(final JanInfo info) throws JanException {
        if (info == null) {
            throw new NullPointerException("Jan info is null.");
        }
        
        discardCore(info, info.getActiveTsumo());
        next(info);
    }
    
    /**
     * 打牌 (手出し)
     */
    public void discard(final JanInfo info, final JanPai target) throws JanException {
        if (info == null) {
            throw new NullPointerException("Jan info is null.");
        }
        if (target == null) {
            throw new NullPointerException("Discard target is null.");
        }
        
        final JanPai activeTsumo = info.getActiveTsumo();
        final Hand hand = info.getActiveHand();
        if (hand.getMenZenMap().get(target) <= 0) {
            if (target == activeTsumo) {
                // 牌が指定されたがツモ切りだった
                discard(info);
                return;
            }
            // ツモ牌を含め、手牌に存在しない牌が指定された
            throw new InvalidInputException("Invalid discard target - " + target);
        }
        
        hand.removeJanPai(target);
        
        // TODO 鳴き直後の打牌の場合、ここで手牌に加えてはならない
        hand.addJanPai(activeTsumo);
        
        // JanInfoの内部情報を更新
        final Wind activeWind = info.getActiveWind();
        info.setHand(activeWind, hand);
        updateWaitList(info, activeWind);
        
        // 牌を捨てる
        discardCore(info, target);
        next(info);
    }
    
    /**
     * 次のプレイヤーの打牌へ
     */
    public void next(final JanInfo info) throws JanException {
        if (info == null) {
            throw new NullPointerException("Jan info is null.");
        }
        
        if (info.getRemainCount() <= 0) {
            // ゲーム終了
            info.notifyObservers(new AnnounceParam(info.getActivePlayer(), ANNOUNCE_FLAG_GAME_OVER));
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
            throw new IllegalArgumentException("Player name list is empty.");
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
        
        // 待ち牌リストを更新
        for (final Map.Entry<Wind, Player> entry : info.getPlayerTable().entrySet()) {
            if (entry.getValue().getType() == PlayerType.HUMAN) {
                updateWaitList(info, entry.getKey());
            }
        }
        
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
        
        info.notifyObservers(new AnnounceParam(info.getActivePlayer(), ANNOUNCE_FLAG_DISCARD));
        
        // 鳴き確認処理
        for (final Wind wind : Wind.values()) {
            final List<AnnounceFlag> callableList = new ArrayList<>();
            final Map<CallType, List<JanPai>> table = info.getWaitTable(wind);
            for (final Map.Entry<CallType, List<JanPai>> entry : table.entrySet()) {
                if (entry.getValue().contains(target)) {
                    switch (entry.getKey()) {
                    case CHI:
                        callableList.add(AnnounceFlag.CALLABLE_CHI);
                        break;
                    case PON:
                        callableList.add(AnnounceFlag.CALLABLE_PON);
                        break;
                    case KAN_LIGHT:
                        callableList.add(AnnounceFlag.CALLABLE_KAN);
                        break;
                    case RON:
                        callableList.add(AnnounceFlag.CALLABLE_RON);
                        break;
                    default:
                        break;
                    }
                }
            }
            // TODO このcallableListはJanInfoにテーブル形式で持たせて上まで回そうと思う
            
            // マルチスレッド化しなくてもいいはず
            info.notifyObservers(new AnnounceParam(info.getPlayer(wind), EnumSet.copyOf(callableList)));
        }
    }
    
    /**
     * チーの待ち牌リストを取得
     * 
     * @param hand クリーン済みの手牌マップ。
     * @return チーの待ち牌リスト。
     */
    private List<JanPai> getChiWaitList(final Map<JanPai, Integer> hand) {
        
        // TODO JanLIBに移す
        
        final List<JanPai> resultList = new ArrayList<>();
        for (final JanPai pai : JanPai.values()) {
            if (isCallableChi(hand, pai)) {
                resultList.add(pai);
            }
        }
        return resultList;
    }
    
    /**
     * プレイヤーの手牌マップを取得
     * 
     * @param info ゲーム情報。
     * @param wind プレイヤーの風。
     * @return プレイヤーの手牌マップ。
     */
    private Map<JanPai, Integer> getHandMap(final JanInfo info, final Wind wind) {
        final Map<JanPai, Integer> hand = info.getHand(wind).getMenZenMap();
        JanPaiUtil.cleanJanPaiMap(hand);
        return hand;
    }
    
    /**
     * 指定牌込みでプレイヤーの手牌マップを取得
     * 
     * @param info ゲーム情報。
     * @param wind プレイヤーの風。
     * @param source 手牌に追加する牌。
     * @return プレイヤーの手牌マップ。
     */
    private Map<JanPai, Integer> getHandMap(final JanInfo info, final Wind wind, final JanPai source) {
        final Map<JanPai, Integer> hand = info.getHand(wind).getMenZenMap();
        JanPaiUtil.addJanPai(hand, source, 1);
        JanPaiUtil.cleanJanPaiMap(hand);
        return hand;
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
     * 明槓の待ち牌リストを取得
     * 
     * @param hand クリーン済みの手牌マップ。
     * @return 明槓の待ち牌リスト。
     */
    private List<JanPai> getKanLightWaitList(final Map<JanPai, Integer> hand) {
        final List<JanPai> resultList = new ArrayList<>();
        for (final Map.Entry<JanPai, Integer> entry : hand.entrySet()) {
            if (entry.getValue() >= 3) {
                resultList.add(entry.getKey());
            }
        }
        return resultList;
    }
    
    /**
     * ポンの待ち牌リストを取得
     * 
     * @param hand クリーン済みの手牌マップ。
     * @return ポンの待ち牌リスト。
     */
    private List<JanPai> getPonWaitList(final Map<JanPai, Integer> hand) {
        
        // TODO JanLIBに移す
        
        final List<JanPai> resultList = new ArrayList<>();
        for (final Map.Entry<JanPai, Integer> entry : hand.entrySet()) {
            if (entry.getValue() >= 2) {
                resultList.add(entry.getKey());
            }
        }
        return resultList;
    }
    
    /**
     * チー可能か
     * 
     * @param hand クリーン済みの手牌マップ。
     * @param discard 捨て牌。
     * @return 判定結果。
     */
    private boolean isCallableChi(final Map<JanPai, Integer> hand, final JanPai discard) {
        
        // TODO JanLIBに移す
        
        if (discard.isJi()) {
            return false;
        }
        
        switch (discard) {
        case MAN_1:
        case PIN_1:
        case SOU_1:
            return hand.containsKey(discard.getNext()) && hand.containsKey(discard.getNext().getNext());
        case MAN_2:
        case PIN_2:
        case SOU_2:
            return (hand.containsKey(discard.getNext()) && hand.containsKey(discard.getNext().getNext())) ||
                   (hand.containsKey(discard.getPrev()) && hand.containsKey(discard.getNext()));
        case MAN_8:
        case PIN_8:
        case SOU_8:
            return (hand.containsKey(discard.getPrev()) && hand.containsKey(discard.getNext())) ||
                   (hand.containsKey(discard.getPrev()) && hand.containsKey(discard.getPrev().getPrev()));
        case MAN_9:
        case PIN_9:
        case SOU_9:
            return hand.containsKey(discard.getPrev()) && hand.containsKey(discard.getPrev().getPrev());
        default:
            return (hand.containsKey(discard.getNext()) && hand.containsKey(discard.getNext().getNext())) ||
                   (hand.containsKey(discard.getPrev()) && hand.containsKey(discard.getNext())) ||
                   (hand.containsKey(discard.getPrev()) && hand.containsKey(discard.getPrev().getPrev()));
        }
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
            // 入力待ちメッセージ
            info.notifyObservers(new AnnounceParam(activePlayer, ANNOUNCE_FLAG_HAND_TSUMO));
            break;
        }
    }
    
    /**
     * 待ち判定を更新
     * 
     * @param info ゲーム情報。
     * @param wind 風。
     */
    private void updateWaitList(final JanInfo info, final Wind wind) {
        final Map<JanPai, Integer> hand = getHandMap(info, wind);
        final Map<CallType, List<JanPai>> waitTable = info.getWaitTable(wind);
        waitTable.put(CallType.RON, HandCheckUtil.getCompletableJanPaiList(hand));
        waitTable.put(CallType.CHI, getChiWaitList(hand));
        waitTable.put(CallType.PON, getPonWaitList(hand));
        waitTable.put(CallType.KAN_LIGHT, getKanLightWaitList(hand));
        info.setWaitTable(wind, waitTable);
    }
    
    
    
    /**
     * NPCリスト
     */
    private static final List<Player> NPC_LIST =
        Collections.unmodifiableList(Arrays.asList(new Player("COM_01", PlayerType.COM),
                                                   new Player("COM_02", PlayerType.COM),
                                                   new Player("COM_03", PlayerType.COM),
                                                   new Player("COM_04", PlayerType.COM)));
    
    /**
     * 実況フラグ
     */
    private static final EnumSet<AnnounceFlag> ANNOUNCE_FLAG_GAME_OVER =
        EnumSet.of(AnnounceFlag.GAME_OVER, AnnounceFlag.FIELD_OPEN);
    private static final EnumSet<AnnounceFlag> ANNOUNCE_FLAG_COMPLETE_TSUMO =
        EnumSet.of(AnnounceFlag.COMPLETE_TSUMO, AnnounceFlag.FIELD_OPEN, AnnounceFlag.RIVER_SINGLE, AnnounceFlag.HAND_OPEN, AnnounceFlag.ACTIVE_TSUMO);
    private static final EnumSet<AnnounceFlag> ANNOUNCE_FLAG_HAND_TSUMO =
        EnumSet.of(AnnounceFlag.PLAYER_TURN, AnnounceFlag.HAND_TALK, AnnounceFlag.ACTIVE_TSUMO);
    private static final EnumSet<AnnounceFlag> ANNOUNCE_FLAG_DISCARD =
        EnumSet.of(AnnounceFlag.RIVER_SINGLE);
    
}

