/**
 * JanInfo.java
 * 
 * @Author
 *   D-freak
 */

package wiz.project.janbot.game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.TreeMap;

import wiz.project.jan.ChmCompleteInfo;
import wiz.project.jan.Hand;
import wiz.project.jan.JanPai;
import wiz.project.jan.Wind;



/**
 * 麻雀ゲームの情報
 */
public final class JanInfo extends Observable implements Cloneable {
    
    /**
     * コンストラクタ
     */
    public JanInfo() {
        clear();
    }
    
    /**
     * コピーコンストラクタ
     * 
     * @param source 複製元。
     */
    public JanInfo(final JanInfo source) {
        if (source != null) {
            _status = source._status;
            setPlayerTable(source._playerTable);
            _deck = deepCopyList(source._deck);
            _deckIndex = source._deckIndex;
            _deckWallIndex = source._deckWallIndex;
            _wanPai = source._wanPai.clone();
            _fieldWind = source._fieldWind;
            _activeWind = source._activeWind;
            _remainCount = source._remainCount;
            _activeTsumo = source._activeTsumo;
            _activeDiscard = source._activeDiscard;
            _completeInfo = source._completeInfo;
            
            for (final Map.Entry<JanPai, Integer> entry : source._riverOuts.entrySet()) {
                _riverOuts.put(entry.getKey(), new Integer(entry.getValue()));
            }
            for (final Map.Entry<Wind, Hand> entry : source._handTable.entrySet()) {
                _handTable.put(entry.getKey(), entry.getValue().clone());
            }
            for (final Map.Entry<Wind, River> entry : source._riverTable.entrySet()) {
                _riverTable.put(entry.getKey(), entry.getValue().clone());
            }
            for (final Map.Entry<Wind, Map<CallType, List<JanPai>>> entry : source._waitTable.entrySet()) {
                _waitTable.put(entry.getKey(), deepCopyWaitTable(entry.getValue()));
            }
            for (final Map.Entry<Wind, List<CallType>> entry : source._callableTable.entrySet()) {
                _callableTable.put(entry.getKey(), deepCopyList(entry.getValue()));
            }
        }
    }
    
    
    
    /**
     * 捨て牌を追加
     * 
     * @param wind 風。
     * @param discard 捨て牌。
     */
    public void addDiscard(final Wind wind, final JanPai discard) {
        if (wind != null) {
            if (discard != null) {
                _riverTable.get(wind).add(discard);
            }
        }
    }
    
    /**
     * フィールドを全消去
     */
    public void clear() {
        _status = GameStatus.CLOSE;
        _deck.clear();
        _deckIndex = 0;
        _deckWallIndex = 0;
        _wanPai = new WanPai();
        _fieldWind = Wind.TON;
        _activeWind = Wind.TON;
        _remainCount = 0;
        _riverOuts.clear();
        _activeTsumo = JanPai.HAKU;
        _activeDiscard = JanPai.HAKU;
        _completeInfo = null;
        clearCallableTable();
        
        for (final Wind wind : Wind.values()) {
            _playerTable.put(wind, new Player());
            _handTable.put(wind, new Hand());
            _riverTable.put(wind, new River());
            final Map<CallType, List<JanPai>> waitTable = Collections.synchronizedMap(new TreeMap<CallType, List<JanPai>>());
            for (final CallType type : CallType.values()) {
                waitTable.put(type, Collections.synchronizedList(new ArrayList<JanPai>()));
            }
            _waitTable.put(wind, waitTable);
        }
    }
    
    /**
     * 鳴き可能テーブルを消去
     */
    public void clearCallableTable() {
        for (final Wind wind : Wind.values()) {
            _callableTable.put(wind, Collections.synchronizedList(new ArrayList<CallType>()));
        }
    }
    
    /**
     * オブジェクトを複製 (ディープコピー)
     * 
     * @return 複製結果。
     */
    @Override
    public JanInfo clone() {
        return new JanInfo(this);
    }
    
    /**
     * 残り枚数を減少
     */
    public void decreaseRemainCount() {
        if (_remainCount > 0) {
            _remainCount--;
        }
    }
    
    /**
     * 直前の捨て牌を取得
     * 
     * @return 直前の捨て牌。
     */
    public JanPai getActiveDiscard() {
        return _activeDiscard;
    }
    
    /**
     * アクティブプレイヤーの手牌を取得
     * 
     * @return アクティブプレイヤーの手牌。
     */
    public Hand getActiveHand() {
        return getHand(_activeWind);
    }
    
    /**
     * アクティブプレイヤーを取得
     * 
     * @return アクティブプレイヤー。
     */
    public Player getActivePlayer() {
        return getPlayer(_activeWind);
    }
    
    /**
     * アクティブプレイヤーの捨て牌リストを取得
     * 
     * @return アクティブプレイヤーの捨て牌リスト。
     */
    public List<JanPai> getActiveRiver() {
        return getRiver(_activeWind).get();
    }
    
    /**
     * 直前のツモ牌を取得
     * 
     * @return 直前のツモ牌。
     */
    public JanPai getActiveTsumo() {
        return _activeTsumo;
    }
    
    /**
     * アクティブプレイヤーの風を取得
     * 
     * @return アクティブプレイヤーの風。
     */
    public Wind getActiveWind() {
        return _activeWind;
    }
    
    /**
     * 鳴き可能リストを取得
     * 
     * @param wind 風。
     * @return 鳴き可能リスト。
     */
    public List<CallType> getCallableList(final Wind wind) {
        if (wind != null) {
            return deepCopyList(_callableTable.get(wind));
        }
        else {
            return Collections.synchronizedList(new ArrayList<CallType>());
        }
    }
    
    /**
     * 鳴き可能なプレイヤー名リストを取得
     * 
     * @return 鳴き可能なプレイヤー名リスト。
     */
    public List<String> getCallablePlayerNameList() {
        final List<String> resultList = new ArrayList<>();
        for (final Map.Entry<Wind, List<CallType>> entry : _callableTable.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                resultList.add(_playerTable.get(entry.getKey()).getName());
            }
        }
        return resultList;
    }
    
    /**
     * 和了情報を取得
     * 
     * @return 和了情報。
     */
    public ChmCompleteInfo getCompleteInfo() {
        return _completeInfo;
    }
    
    /**
     * 和了牌の残り枚数を取得
     * 
     * @param isRon ロン和了か。
     * @return 和了牌の残り枚数。
     */
    public int getCompleteOuts(boolean isRon) {
        if (isRon) {
            return _riverOuts.get(getActiveDiscard());
        }
        else {
            final Map<JanPai, Integer> outs = deepCopyMap(_riverOuts);
            if (getActiveTsumo() != null && outs.get(getActiveTsumo()) != null) {
                outs.put(getActiveTsumo(), outs.get(getActiveTsumo()) - 1);
            }
            return outs.get(getActiveTsumo());
        }
    }
    
    /**
     * 牌山を取得
     * 
     * @return 牌山。
     */
    public List<JanPai> getDeck() {
        return deepCopyList(_deck);
    }
    
    /**
     * 牌山インデックスを取得
     * 
     * @return 牌山インデックス。
     */
    public int getDeckIndex() {
        return _deckIndex;
    }
    
    /**
     * 場風を取得
     * 
     * @return 場風。
     */
    public Wind getFieldWind() {
        return _fieldWind;
    }
    
    /**
     * ゲームの状態を取得
     * 
     * @return ゲームの状態。
     */
    public GameStatus getStatus() {
        return _status;
    }
    
    /**
     * 手牌を取得
     * 
     * @param playerName プレイヤー名。
     * @return 手牌。
     */
    public Hand getHand(final String playerName) {
        if (playerName != null) {
            try {
                final Wind wind = getWind(playerName);
                return _handTable.get(wind).clone();
            }
            catch (final IllegalArgumentException e) {
                // 指定のプレイヤーが存在しなかった場合のエラーを無視
            }
        }
        return new Hand();
    }
    
    /**
     * 手牌を取得
     * 
     * @param wind 風。
     * @return 手牌。
     */
    public Hand getHand(final Wind wind) {
        if (wind != null) {
            return _handTable.get(wind).clone();
        }
        else {
            return new Hand();
        }
    }
    
    /**
     * 牌山から牌を取得
     * 
     * @return インデックスの指す牌。
     */
    public JanPai getJanPaiFromDeck() {
        return _deck.get(_deckIndex);
    }
    
    /**
     * 牌山から嶺上牌を取得(中国麻雀用)
     * 
     * @return インデックスの指す牌。
     */
    public JanPai getJanPaiFromDeckWall() {
        return _deck.get(_deckWallIndex);
    }
    
    /**
     * 残り枚数テーブルを取得
     * 
     * @param wind 風。
     * @return 残り枚数テーブル。
     */
    public Map<JanPai, Integer> getOuts(final Wind wind) {
        Map<JanPai, Integer> outs = getOutsOnConfirm(wind);
        if (getActiveTsumo() != null && outs.get(getActiveTsumo()) != null) {
            outs.put(getActiveTsumo(), outs.get(getActiveTsumo()) - 1);
        }
        return outs;
    }
    
    /**
     * 残り枚数テーブルを取得(確認メッセージ用)
     * 
     * @param wind 風。
     * @return 残り枚数テーブル。
     */
    public Map<JanPai, Integer> getOutsOnConfirm(final Wind wind) {
        final Map<JanPai, Integer> outs = deepCopyMap(_riverOuts);
        
        for (final JanPai pai : outs.keySet()) {
            int visibleCount = 0;
            visibleCount += getHand(wind).getAllJanPaiMap().get(pai);
            outs.put(pai, outs.get(pai) - visibleCount);
        }
        return outs;
    }
    
    /**
     * プレイヤーを取得
     * 
     * @param playerName プレイヤー名。
     * @return プレイヤー。
     */
    public Player getPlayer(final String playerName) {
        if (playerName != null) {
            for (final Player player : _playerTable.values()) {
                if (player.getName().equals(playerName)) {
                    return player;
                }
            }
        }
        return new Player();
    }
    
    /**
     * プレイヤーを取得
     * 
     * @param wind 風。
     * @return プレイヤー。
     */
    public Player getPlayer(final Wind wind) {
        if (wind != null) {
            return _playerTable.get(wind);
        }
        else {
            return new Player();
        }
    }
    
    /**
     * プレイヤーテーブルを取得
     * 
     * @return プレイヤーテーブル。
     */
    public Map<Wind, Player> getPlayerTable() {
        return deepCopyMap(_playerTable);
    }
    
    /**
     * 残り枚数を取得
     * 
     * @return 残り枚数。
     */
    public int getRemainCount() {
        return _remainCount;
    }
    
    /**
     * 捨て牌リストを取得
     * 
     * @param wind 風。
     * @return 捨て牌リスト。
     */
    public River getRiver(final Wind wind) {
        if (wind != null) {
            return _riverTable.get(wind).clone();
        }
        else {
            return new River();
        }
    }
    
    /**
     * 待ち牌テーブルを取得
     * 
     * @param wind 風。
     * @return 待ち牌テーブル。
     */
    public Map<CallType, List<JanPai>> getWaitTable(final Wind wind) {
        if (wind != null) {
            return deepCopyWaitTable(_waitTable.get(wind));
        }
        else {
            return deepCopyWaitTable(new HashMap<CallType, List<JanPai>>());
        }
    }
    
    /**
     * 王牌を取得
     * 
     * @return 王牌。
     */
    public WanPai getWanPai() {
        return _wanPai.clone();
    }
    
    /**
     * 風を取得
     * 
     * @param playerName プレイヤー名。
     * @return 風。
     */
    public Wind getWind(final String playerName) {
        if (playerName != null) {
            for (final Map.Entry<Wind, Player> entry : _playerTable.entrySet()) {
                if (entry.getValue().getName().equals(playerName)) {
                    return entry.getKey();
                }
            }
        }
        throw new IllegalArgumentException("Invalid player name : " + playerName);
    }
    
    /**
     * 牌山インデックスを増加
     */
    public void increaseDeckIndex() {
        if (isSameIndex()) {
            moveDeckWallIndex();
        }
        
        setDeckIndex(_deckIndex + 1);
        
        if (isLastShitatsumo()) {
            setDeckIndex(_deckIndex + 1);
        }
    }
    
    /**
     * アクティブプレイヤーか
     * 
     * @param playerName プレイヤー名。
     * @return 判定結果。
     */
    public boolean isActivePlayer(final String playerName) {
        if (playerName == null) {
            return false;
        }
        return getActivePlayer().getName().equals(playerName);
    }
    
    /**
     * ゲームに参加中のプレイヤーか
     * 
     * @param playerName プレイヤー名。
     * @return 判定結果。
     */
    public boolean isValidPlayer(final String playerName) {
        if (playerName == null) {
            return false;
        }
        for (final Player player : _playerTable.values()) {
            if (player.getName().equals(playerName)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 牌山の嶺上インデックスを移動(中国麻雀用)
     */
    public void moveDeckWallIndex() {
        if (isUwatsumo()) {
        	setDeckWallIndex(_deckWallIndex + 1);
        }
        else {
            setDeckWallIndex(_deckWallIndex - 3);
        }
    }
    
    /**
     * 監視者に状態を通知 (強制)
     * 
     * @param param 通知パラメータ。
     */
    @Override
    public void notifyObservers(final Object param) {
        setChanged();
        super.notifyObservers(param);
    }
    
    /**
     * 直前の捨て牌を設定
     * 
     * @param pai 直前の捨て牌。
     */
    public void setActiveDiscard(final JanPai pai) {
        if (pai != null) {
            _activeDiscard = pai;
        }
        else {
            _activeDiscard = JanPai.HAKU;
        }
    }
    
    /**
     * アクティブプレイヤーを設定
     * 
     * @param playerName プレイヤー名。
     */
    public void setActivePlayer(final String playerName) {
        if (playerName != null) {
            for (final Map.Entry<Wind, Player> entry : _playerTable.entrySet()) {
                if (entry.getValue().getName().equals(playerName)) {
                    setActiveWind(entry.getKey());
                }
            }
        }
    }
    
    /**
     * 直前のツモ牌を設定
     * 
     * @param pai 直前のツモ牌。
     */
    public void setActiveTsumo(final JanPai pai) {
        _activeTsumo = pai;
    }
    
    /**
     * アクティブプレイヤーの風を設定
     * 
     * @param wind アクティブプレイヤーの風。
     */
    public void setActiveWind(final Wind wind) {
        if (wind != null) {
            _activeWind = wind;
        }
        else {
            _activeWind = Wind.TON;
        }
    }
    
    /**
     * アクティブプレイヤーの風を次に移す
     */
    public void setActiveWindToNext() {
        _activeWind = _activeWind.getNext();
    }
    
    /**
     * 鳴き可能リストを設定
     * 
     * @param wind 風。
     * @param callableList 鳴き可能リスト。
     */
    public void setCallableList(final Wind wind, final List<CallType> callableList) {
        if (wind != null) {
            if (callableList != null) {
                _callableTable.put(wind, deepCopyList(callableList));
            }
            else {
                _callableTable.put(wind, Collections.synchronizedList(new ArrayList<CallType>()));
            }
        }
    }
    
    /**
     * 被副露牌インデックスを設定
     * 
     * @param wind 副露された風。
     */
    public void setCalledIndex(final Wind wind) {
        if (wind != null) {
            _riverTable.get(wind).setCalledIndex();
            // 副露後の捨て牌選択時の残り枚数確認で、
            // _activeTsumoをカウントしないようnullを設定
            setActiveTsumo(null);
        }
    }
    
    /**
     * 和了情報を設定
     * 
     * @param completeInfo 和了情報。
     */
    public void setCompleteInfo(final ChmCompleteInfo completeInfo) {
        _completeInfo = completeInfo;
    }
    
    /**
     * 牌山を設定
     * 
     * @param deck 牌山。
     */
    public void setDeck(final List<JanPai> deck) {
        if (deck != null) {
            _deck = deepCopyList(deck);
        }
        else {
            _deck.clear();
        }
    }
    
    /**
     * 牌山インデックスを設定
     * 
     * @param index 牌山インデックス。
     */
    public void setDeckIndex(final int index) {
        if (index > 0) {
            final int deckSize = _deck.size();
            if (index < deckSize) {
                _deckIndex = index;
            }
            else {
                _deckIndex = deckSize - 1;
            }
        }
        else {
            _deckIndex = 0;
        }
    }
    
    /**
     * 牌山の嶺上インデックスを設定(中国麻雀用)
     * 
     * @param index 嶺上牌山インデックス。
     */
    public void setDeckWallIndex(final int index) {
        if (index > 0) {
            _deckWallIndex = index;
        }
        else {
            _deckWallIndex = 0;
        }
    }
    
    /**
     * 場風を設定
     * 
     * @param wind 場風。
     */
    public void setFieldWind(final Wind wind) {
        if (wind != null) {
            _fieldWind = wind;
        }
        else {
            _fieldWind = Wind.TON;
        }
    }
    
    /**
     * 手牌を設定
     * 
     * @param wind 風。
     * @param hand 手牌。
     */
    public void setHand(final Wind wind, final Hand hand) {
        if (wind != null) {
            if (hand != null) {
                _handTable.put(wind, hand.clone());
            }
            else {
                _handTable.put(wind, new Hand());
            }
        }
    }
    
    /**
     * プレイヤーテーブルを設定
     * 
     * @param playerTable プレイヤーテーブル。
     */
    public void setPlayerTable(final Map<Wind, Player> playerTable) {
        _playerTable.clear();
        if (playerTable != null) {
            _playerTable.putAll(deepCopyMap(playerTable));
        }
    }
    
    /**
     * 残り枚数を設定
     * 
     * @param remainCount 残り枚数。
     */
    public void setRemainCount(final int remainCount) {
        if (remainCount > 0) {
            _remainCount = remainCount;
        }
        else {
            _remainCount = 0;
        }
    }
    
    /**
     * 捨て牌リストを設定
     * 
     * @param wind 風。
     * @param river 捨て牌リスト。
     */
    public void setRiver(final Wind wind, final List<JanPai> river) {
        if (wind != null) {
            if (river != null) {
                _riverTable.put(wind, new River(river));
            }
            else {
                _riverTable.put(wind, new River());
            }
        }
    }
    
    /**
     * 河での残り枚数テーブルを設定
     * 
     * @param targetList 残り枚数を確認したい牌リスト。
     */
    public void setRiverOuts(final List<JanPai> targetList) {
        for (final JanPai pai : targetList) {
            final List<JanPai> paiList = Arrays.asList(pai);
            int visibleCount = 0;
            
            for (final Wind wind : Wind.values()) {
                final List<JanPai> river = getRiver(wind).get();
                river.retainAll(paiList);
                visibleCount += river.size();
                for (final Integer index : getRiver(wind).getCalledIndexList()) {
                    if (pai.equals(getRiver(wind).get().get(index - 1))) {
                        visibleCount--;
                    }
                }
            }
            _riverOuts.put(pai, 4 - visibleCount);
        }
    }
    
    /**
     * ゲームの状態を設定
     * 
     * @param status ゲームの状態。
     */
    public void setStatus(final GameStatus status) {
        if (status != null) {
            _status = status;
        }
        else {
            _status = GameStatus.CLOSE;
        }
        notifyObservers();
    }
    
    /**
     * 待ち牌テーブルを設定
     * 
     * @param wind 風。
     * @param table 待ち牌テーブル。
     */
    public void setWaitTable(final Wind wind, final Map<CallType, List<JanPai>> table) {
        if (wind != null) {
            if (table != null) {
                _waitTable.put(wind, deepCopyWaitTable(table));
            }
            else {
                _waitTable.put(wind, deepCopyWaitTable(new HashMap<CallType, List<JanPai>>()));
            }
        }
    }
    
    /**
     * 王牌を設定
     * 
     * @param wanPai 王牌。
     */
    public void setWanPai(final WanPai wanPai) {
        if (wanPai != null) {
            _wanPai = wanPai.clone();
        }
        else {
            _wanPai = new WanPai();
        }
    }
    
    
    
    /**
     * リストをディープコピー
     * 
     * @param sourceList 複製元。
     * @return 複製結果。
     */
    private <E> List<E> deepCopyList(final List<E> sourceList) {
        return Collections.synchronizedList(new ArrayList<E>(sourceList));
    }
    
    /**
     * マップをディープコピー
     * 
     * @param source 複製元。
     * @return 複製結果。
     */
    private <S, T> Map<S, T> deepCopyMap(final Map<S, T> source) {
        return Collections.synchronizedMap(new TreeMap<S, T>(source));
    }
    
    /**
     * 待ち牌テーブルをディープコピー
     * 
     * @param source 複製元。
     * @return 複製結果。
     */
    private Map<CallType, List<JanPai>> deepCopyWaitTable(final Map<CallType, List<JanPai>> source) {
        final Map<CallType, List<JanPai>> result = Collections.synchronizedMap(new TreeMap<CallType, List<JanPai>>());
        for (final CallType type : CallType.values()) {
            if (source.containsKey(type)) {
                result.put(type, deepCopyList(source.get(type)));
            }
            else {
                result.put(type, Collections.synchronizedList(new ArrayList<JanPai>()));
            }
        }
        return result;
    }
    
    /**
     * 最後の下ヅモか(中国麻雀用)
     * @return 判定結果。
     */
    private boolean isLastShitatsumo() {
        if (_deckIndex != _deckWallIndex - 1) {
            return false;
        }
        if (isUwatsumo()) {
            return false;
        }
        return true;
    }
    
    /**
     * 牌山インデックスが嶺上インデックスと同じか(中国麻雀用)
     * @return 判定結果。
     */
    private boolean isSameIndex() {
        return _deckIndex == _deckWallIndex;
    }
    
    /**
     * 嶺上牌が上ヅモか(中国麻雀用)
     * @return 判定結果。
     */
    private boolean isUwatsumo() {
        if (_deckWallIndex % 2 == 0) {
            return true;
        }
        else {
            return false;
        }
    }
    
    
    
    /**
     * ゲームの状態
     */
    private GameStatus _status = GameStatus.CLOSE;
    
    /**
     * プレイヤーテーブル
     */
    private final Map<Wind, Player> _playerTable = Collections.synchronizedMap(new TreeMap<Wind, Player>());
    
    /**
     * 待ち牌テーブル
     */
    private final Map<Wind, Map<CallType, List<JanPai>>> _waitTable = Collections.synchronizedMap(new TreeMap<Wind, Map<CallType, List<JanPai>>>());
    
    /**
     * 鳴き可能テーブル
     * 
     * 待ち牌テーブルから算出可能だが、ロン可否の判定コストが重いため
     * キャッシュも兼ねて別メンバとして持つ
     */
    private final Map<Wind, List<CallType>> _callableTable = Collections.synchronizedMap(new TreeMap<Wind, List<CallType>>());
    
    /**
     * 牌山
     */
    private List<JanPai> _deck = new ArrayList<>();
    
    /**
     * 牌山インデックス
     */
    private int _deckIndex = 0;
    
    /**
     * 牌山の嶺上インデックス(中国麻雀用)
     */
    private int _deckWallIndex = 0;
    
    /**
     * 王牌
     */
    private WanPai _wanPai = new WanPai();
    
    /**
     * 場風
     */
    private Wind _fieldWind = Wind.TON;
    
    /**
     * アクティブな風
     */
    private Wind _activeWind = Wind.TON;
    
    /**
     * 残り枚数
     */
    private int _remainCount = 0;
    
    /**
     * 河での残り枚数テーブル
     */
    private Map<JanPai, Integer> _riverOuts = new TreeMap<>();
    
    /**
     * 手牌テーブル
     */
    private Map<Wind, Hand> _handTable = new TreeMap<>();
    
    /**
     * 捨て牌テーブル
     */
    private Map<Wind, River> _riverTable = new TreeMap<>();
    
    /**
     * 直前のツモ牌
     */
    private JanPai _activeTsumo = JanPai.HAKU;
    
    /**
     * 直前の捨て牌
     */
    private JanPai _activeDiscard = JanPai.HAKU;
    
    /**
     * 和了情報
     */
    private ChmCompleteInfo _completeInfo = null;
    
}

