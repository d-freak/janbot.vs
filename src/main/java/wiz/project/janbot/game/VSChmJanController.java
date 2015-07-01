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
import wiz.project.jan.MenTsu;
import wiz.project.jan.MenTsuType;
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
     * 吃
     */
    public void chi(final JanInfo info, final CallInfo call) throws JanException {
        if (info == null) {
            throw new NullPointerException("Jan info is null.");
        }
        if (call == null) {
            throw new NullPointerException("Call info is null.");
        }
        
        final JanPai target = call.getTargetPai();
        switch (target) {
        case MAN_8:
        case MAN_9:
        case PIN_8:
        case PIN_9:
        case SOU_8:
        case SOU_9:
        case TON:
        case NAN:
        case SHA:
        case PEI:
        case HAKU:
        case HATU:
        case CHUN:
            // チー不可
            throw new InvalidInputException("Can't chi.");
        default:
            break;
        }
        
        // 打牌したプレイヤーの風を記録
        final Wind activeWind = info.getActiveWind();
        try {
            // チー宣言したプレイヤーをアクティブ化
            info.setActivePlayer(call.getPlayerName());
            
            final List<JanPai> targetList = Arrays.asList(target, target.getNext(), target.getNext().getNext());
            final JanPai discard = info.getActiveDiscard();
            if (!targetList.contains(discard)) {
                // チー不可
                throw new InvalidInputException("Invalid chi parameter.");
            }
            
            // 直前の捨て牌を手牌に加える
            final Hand hand = info.getActiveHand();
            hand.addJanPai(discard);
            
            for (final JanPai targetPai : targetList) {
                if (hand.getMenZenJanPaiCount(targetPai) == 0) {
                    // チー不可
                    throw new InvalidInputException("Invalid chi parameter.");
                }
            }
            
            // チー対象牌を削除
            for (final JanPai targetPai : targetList) {
                hand.removeJanPai(targetPai);
            }
            
            // 固定面子を追加
            final MenTsu fix = new MenTsu(targetList, MenTsuType.CHI);
            hand.addFixedMenTsu(fix);
            
            // 手牌を更新
            info.setHand(info.getActiveWind(), hand);
            
            // 捨て牌選択
            final Player activePlayer = info.getActivePlayer();
            info.notifyObservers(new GameStatusParam(activePlayer, GameStatus.AFTER_CALL));
            info.notifyObservers(new AnnounceParam(activePlayer, ANNOUNCE_FLAG_HAND_AFTER_CALL));
        }
        catch (final Throwable e) {
            // 副露しない場合、アクティブプレイヤーを元に戻す
            info.setActiveWind(activeWind);
            throw e;
        }
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
        
        final Player activePlayer = info.getActivePlayer();
        info.notifyObservers(new GameStatusParam(activePlayer, GameStatus.END_ROUND));
        info.notifyObservers(new AnnounceParam(activePlayer, ANNOUNCE_FLAG_COMPLETE_TSUMO));
    }
    
    /**
     * 和了 (ロン)
     */
    public void completeRon(final JanInfo info, final CallInfo call) {
        if (info == null) {
            throw new NullPointerException("Jan info is null.");
        }
        if (call == null) {
            throw new NullPointerException("Call info is null.");
        }
        
        // ロン宣言したプレイヤーをアクティブ化
        info.setActivePlayer(call.getPlayerName());
        
        // 打点不足などによるチョンボを確認しない
        // 上位層(GameMaster)でJanInfoの中身を使って確認を終えている前提とする
        
        info.clearCallableTable();
        
        final Player activePlayer = info.getActivePlayer();
        info.notifyObservers(new GameStatusParam(activePlayer, GameStatus.END_ROUND));
        info.notifyObservers(new AnnounceParam(activePlayer, ANNOUNCE_FLAG_COMPLETE_RON));
    }
    
    /**
     * 打牌 (ツモ切り)
     */
    public void discard(final JanInfo info) {
        if (info == null) {
            throw new NullPointerException("Jan info is null.");
        }
        
        discardCore(info, info.getActiveTsumo());
        if (info.getCallablePlayerNameList().isEmpty()) {
            next(info);
        }
    }
    
    /**
     * 打牌 (手出し)
     */
    public void discard(final JanInfo info, final JanPai target) throws JanException {
        discard(info, target, false);
    }
    
    /**
     * 打牌 (手出し)
     */
    public void discard(final JanInfo info, final JanPai target, final boolean afterCall) throws JanException {
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
        
        if (!afterCall) {
            hand.addJanPai(activeTsumo);
        }
        
        // 手牌情報と待ち牌テーブルを更新
        final Wind activeWind = info.getActiveWind();
        info.setHand(activeWind, hand);
        updateWaitList(info, activeWind);
        
        // 牌を捨てる
        discardCore(info, target);
        if (info.getCallablePlayerNameList().isEmpty()) {
            next(info);
        }
    }
    
    /**
     * 大明槓
     */
    public void kanCall(final JanInfo info, final CallInfo call) {
        if (info == null) {
            throw new NullPointerException("Jan info is null.");
        }
        if (call == null) {
            throw new NullPointerException("Call info is null.");
        }
        
        // カン宣言したプレイヤーをアクティブ化
        info.setActivePlayer(call.getPlayerName());
        
        final JanPai target = call.getTargetPai();
        final Hand hand = info.getActiveHand();
        
        // カン対象牌を削除
        for (int i = 0; i < 3; i++) {
            hand.removeJanPai(target);
        }
        
        // 固定面子を追加
        final MenTsu kanLight = new MenTsu(Arrays.asList(target, target, target, target), MenTsuType.KAN_LIGHT);
        hand.addFixedMenTsu(kanLight);
        
        // 手牌を更新
        final Wind activeWind = info.getActiveWind();
        info.setHand(activeWind, hand);
        
        // 王牌操作
        postProcessKan(info, activeWind);
        
        // 捨て牌選択
        info.notifyObservers(new AnnounceParam(info.getActivePlayer(), ANNOUNCE_FLAG_HAND_TSUMO_FIELD_AFTER_CALL));
    }
    
    /**
     * 暗槓/加槓
     */
    public void kanHand(final JanInfo info, final JanPai target) throws JanException {
        if (info == null) {
            throw new NullPointerException("Jan info is null.");
        }
        if (target == null) {
            throw new NullPointerException("Kan target is null.");
        }
        
        // 直前のツモ牌を手牌に加える
        final Hand hand = info.getActiveHand();
        final JanPai activeTsumo = info.getActiveTsumo();
        hand.addJanPai(activeTsumo);
        
        final CallType kanType = getKanType(info, target);
        switch (kanType) {
        case KAN_DARK:
            {
                // カン対象牌を削除
                for (int i = 0; i < 4; i++) {
                    hand.removeJanPai(target);
                }
                
                // 固定面子を追加
                final MenTsu kanDark = new MenTsu(Arrays.asList(target, target, target, target), MenTsuType.KAN_DARK);
                hand.addFixedMenTsu(kanDark);
            }
            break;
        case KAN_ADD:
            {
                // カン対象牌を削除
                hand.removeJanPai(target);
                
                // 固定面子リストを更新
                final List<MenTsu> fixedMenTsuList = hand.getFixedMenTsuList();
                for (int i = 0; i < fixedMenTsuList.size(); i++) {
                    final MenTsu menTsu = fixedMenTsuList.get(i);
                    if (menTsu.getMenTsuType() == MenTsuType.PON) {
                        if (menTsu.getSource().get(0) == target) {
                            final MenTsu kanLight = new MenTsu(Arrays.asList(target, target, target, target), MenTsuType.KAN_LIGHT);
                            fixedMenTsuList.set(i, kanLight);
                            hand.setFixedMenTsuList(fixedMenTsuList);
                            break;
                        }
                    }
                }
            }
            break;
        default:
            break;
        }
        
        // 手牌を更新
        final Wind activeWind = info.getActiveWind();
        info.setHand(activeWind, hand);
        
        // 王牌操作
        postProcessKan(info, activeWind);
        
        // 捨て牌選択
        info.notifyObservers(new AnnounceParam(info.getActivePlayer(), ANNOUNCE_FLAG_HAND_TSUMO_FIELD_AFTER_CALL));
    }
    
    /**
     * 次のプレイヤーの打牌へ
     */
    public void next(final JanInfo info) {
        if (info == null) {
            throw new NullPointerException("Jan info is null.");
        }
        
        if (info.getRemainCount() <= 0) {
            // ゲーム終了
            info.notifyObservers(new AnnounceParam(info.getActivePlayer(), ANNOUNCE_FLAG_GAME_OVER));
            return;
        }
        
        info.clearCallableTable();
        info.setActiveWindToNext();
        onPhase(info);
    }
    
    /**
     * 碰
     */
    public void pon(final JanInfo info, final CallInfo call) {
        if (info == null) {
            throw new NullPointerException("Jan info is null.");
        }
        if (call == null) {
            throw new NullPointerException("Call info is null.");
        }
        
        // ポン宣言したプレイヤーをアクティブ化
        info.setActivePlayer(call.getPlayerName());
        
        final JanPai discard = info.getActiveDiscard();
        final Hand hand = info.getActiveHand();
        
        // ポン対象牌を削除
        for (int i = 0; i < 2; i++) {
            hand.removeJanPai(discard);
        }
        
        // 固定面子を追加
        final MenTsu pon = new MenTsu(Arrays.asList(discard, discard, discard), MenTsuType.PON);
        hand.addFixedMenTsu(pon);
        
        // 手牌を更新
        final Wind activeWind = info.getActiveWind();
        info.setHand(activeWind, hand);
        
        // 捨て牌選択
        final Player activePlayer = info.getActivePlayer();
        info.notifyObservers(new GameStatusParam(activePlayer, GameStatus.AFTER_CALL));
        info.notifyObservers(new AnnounceParam(activePlayer, ANNOUNCE_FLAG_HAND_AFTER_CALL));
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
        
        info.notifyObservers(new AnnounceParam(info.getActivePlayer(), ANNOUNCE_FLAG_GAME_START));
        
        // 一巡目へ (親の14枚目はこの先でツモらせる)
        info.setActiveWind(Wind.TON);
        onPhase(info);
    }
    
    
    
    /**
     * 鳴き可能リストを生成
     * 
     * @param waitTable 待ち牌テーブル。
     * @param target 確認対象牌。
     * @return 鳴き可能リスト。
     */
    private List<CallType> createCallableList(final Map<CallType, List<JanPai>> waitTable, final JanPai target) {
        final List<CallType> resultList = new ArrayList<>();
        for (final Map.Entry<CallType, List<JanPai>> entry : waitTable.entrySet()) {
            if (entry.getValue().contains(target)) {
                resultList.add(entry.getKey());
            }
        }
        return resultList;
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
        
        final Player activePlayer = info.getActivePlayer();
        info.notifyObservers(new AnnounceParam(activePlayer, ANNOUNCE_FLAG_DISCARD));
        
        if (info.getRemainCount() == 0) {
            // ラス牌は鳴けない
            return;
        }
        
        // 鳴き確認処理
        final List<Player> callerList = new ArrayList<>();
        for (final Wind wind : Wind.values()) {
            final Player player = info.getPlayer(wind);
            if (player.getType() != PlayerType.HUMAN) {
                // CPUは無視
                continue;
            }
            
            final List<CallType> callableList = createCallableList(info.getWaitTable(wind), target);
            if (callableList.isEmpty()) {
                // 鳴けない場合は無視
                continue;
            }
            
            // 確認メッセージを出すところまでで次のループに移るので、
            // マルチスレッド化の必要無し
            info.setCallableList(wind, callableList);
            callerList.add(player);
        }
        
        if (!callerList.isEmpty()) {
            info.notifyObservers(new GameStatusParam(activePlayer, GameStatus.IDLE_CALL));
            for (final Player caller : callerList) {
                info.notifyObservers(new AnnounceParam(caller, ANNOUNCE_FLAG_CONFIRM_CALL));
            }
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
     * 槓の種類を取得
     * 
     * @param info ゲーム情報。
     * @param target 槓対象牌。
     * @return 槓の種類。
     * @throws InvalidInputException 槓不可能。
     */
    private CallType getKanType(final JanInfo info, final JanPai target) throws InvalidInputException {
        final Hand hand = info.getActiveHand();
        final Wind activeWind = info.getActiveWind();
        final JanPai activeTsumo = info.getActiveTsumo();
        final Map<JanPai, Integer> count = getHandMap(info, activeWind, activeTsumo);
        if (count.containsKey(target) && count.get(target) >= 4) {
            // 指定牌を4枚持っている
            return CallType.KAN_DARK;
        }
        if (hasPonMenTsu(hand, target)) {
            // 指定牌のポン面子を持っている
            return CallType.KAN_ADD;
        }
        throw new InvalidInputException("Can't kan.");
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
     * 指定牌のポン面子を持っているか
     * 
     * @param sourceHand 確認元手牌。
     * @param target 確認対象牌。
     * @return 確認結果。
     */
    private boolean hasPonMenTsu(final Hand sourceHand, final JanPai target) {
        for (final MenTsu menTsu : sourceHand.getFixedMenTsuList()) {
            if (menTsu.getMenTsuType() == MenTsuType.PON) {
                if (menTsu.getSource().get(0) == target) {
                    return true;
                }
            }
        }
        return false;
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
     */
    private void onPhase(final JanInfo info) {
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
     * カンの後処理 (王牌操作)
     * 
     * @param info ゲーム情報。
     * @param activeWind アクティブプレイヤーの風。
     */
    private void postProcessKan(final JanInfo info, final Wind activeWind) {
        // ドラを追加
        final WanPai wanPai = info.getWanPai();
        wanPai.openNewDora();
        
        // 嶺上牌をツモる
        final JanPai activeTsumo = wanPai.getWall();
        info.setActiveTsumo(activeTsumo);
        info.decreaseRemainCount();
        info.setWanPai(wanPai);
        
        // 手変わりがあったので待ち判定更新
        updateWaitList(info, activeWind);
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
        if (info.getActiveWind().getNext() == wind) {
            waitTable.put(CallType.CHI, getChiWaitList(hand));
        }
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
    private static final EnumSet<AnnounceFlag> ANNOUNCE_FLAG_GAME_START =
        EnumSet.of(AnnounceFlag.FIELD_OPEN);
    private static final EnumSet<AnnounceFlag> ANNOUNCE_FLAG_GAME_OVER =
        EnumSet.of(AnnounceFlag.GAME_OVER, AnnounceFlag.FIELD_OPEN);
    private static final EnumSet<AnnounceFlag> ANNOUNCE_FLAG_COMPLETE_RON =
        EnumSet.of(AnnounceFlag.COMPLETE_RON, AnnounceFlag.FIELD_OPEN, AnnounceFlag.RIVER_SINGLE, AnnounceFlag.HAND_OPEN, AnnounceFlag.ACTIVE_TSUMO);
    private static final EnumSet<AnnounceFlag> ANNOUNCE_FLAG_COMPLETE_TSUMO =
        EnumSet.of(AnnounceFlag.COMPLETE_TSUMO, AnnounceFlag.FIELD_OPEN, AnnounceFlag.RIVER_SINGLE, AnnounceFlag.HAND_OPEN, AnnounceFlag.ACTIVE_TSUMO);
    private static final EnumSet<AnnounceFlag> ANNOUNCE_FLAG_HAND_TSUMO =
        EnumSet.of(AnnounceFlag.PLAYER_TURN, AnnounceFlag.HAND_TALK, AnnounceFlag.ACTIVE_TSUMO);
    private static final EnumSet<AnnounceFlag> ANNOUNCE_FLAG_HAND_AFTER_CALL =
        EnumSet.of(AnnounceFlag.HAND_TALK, AnnounceFlag.AFTER_CALL);
    private static final EnumSet<AnnounceFlag> ANNOUNCE_FLAG_HAND_TSUMO_FIELD_AFTER_CALL =
        EnumSet.of(AnnounceFlag.HAND_TALK, AnnounceFlag.ACTIVE_TSUMO, AnnounceFlag.FIELD_TALK, AnnounceFlag.AFTER_CALL);
    private static final EnumSet<AnnounceFlag> ANNOUNCE_FLAG_DISCARD =
        EnumSet.of(AnnounceFlag.DISCARD, AnnounceFlag.RIVER_SINGLE);
    private static final EnumSet<AnnounceFlag> ANNOUNCE_FLAG_CONFIRM_CALL =
        EnumSet.of(AnnounceFlag.CONFIRM_CALL, AnnounceFlag.FIELD_TALK, AnnounceFlag.HAND_TALK);
    
}

