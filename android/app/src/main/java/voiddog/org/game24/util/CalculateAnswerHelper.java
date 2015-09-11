package voiddog.org.game24.util;

/**
 * 计算答案帮助类
 * Created by Dog on 2015/9/11.
 */
public class CalculateAnswerHelper {

    public static boolean judgeAnswer(int[] a){
        boolean [] used = new boolean[4];
        return dfs(a, used, 0);
    }

    static boolean dfs(int[] a, boolean[] used, int steps){
        if(steps == 3){
            for(int i = 0; i < 4 ; i++){
                if(!used[i]){
                    return a[i] == 24;
                }
            }
            return false;
        }

        int[] b  = {0,0,0,0};
        System.arraycopy(a, 0, b, 0, a.length);
        for(int i = 0 ; i < 4; i++){
            if(!used[i]){
                used[i] = true;
                for(int j = i+1; j < 4; j++){
                    if(!used[j]){
                        b[j] = a[i] + a[j];
                        if(dfs(b, used, steps+1)){
                            return true;
                        }
                        b[j] = a[i] * a[j];
                        if(dfs(b, used, steps+1)){
                            return true;
                        }
                        b[j] = a[i] - a[j];
                        if(b[j] < 0){
                            b[j] *= -1;
                        }
                        if(dfs(b, used, steps+1)){
                            return true;
                        }
                        if(a[j] != 0 && a[i] % a[j] == 0){
                            b[j] = a[i] / a[j];
                            if(dfs(b, used, steps+1)){
                                return true;
                            }
                        }
                        if(a[i] != 0 && a[j] % a[i]==0){
                            b[j] = a[j] / a[i];
                            if(dfs(b, used, steps+1)){
                                return true;
                            }
                        }
                        b[j] = a[j];
                    }
                }
                used[i] = false;
            }
        }
        return false;
    }
}
