package voiddog.org.game24.event;

import voiddog.org.game24.data.GameMode;

/**
 * 游戏结束事件
 * Created by Dog on 2015/9/11.
 */
public class GameOverEvent {
    public GameMode gameMode;   //游戏模式
    public int score;           //分数
    public String name;         //玩家名字

    public GameOverEvent(GameMode gameMode, String name, int score){
        this.gameMode = gameMode;
        this.name = name;
        this.score = score;
    }
}
