package voiddog.org.game24.util;

/**
 * 计算答案帮助类
 * Created by Dog on 2015/9/11.
 */
public class CalculateAnswerHelper {

    public static String judgeAnswer(int[] a){
        boolean [] used = new boolean[4];
        return dfs(a, used, 0);
    }

    static String dfs(int[] a, boolean[] used, int steps){
        if(steps == 3){
            for(int i = 0; i < 4 ; i++){
                if(!used[i] && a[i] == 24){
                    return "24";
                }
            }
            return null;
        }

        int[] b  = {0,0,0,0};
        System.arraycopy(a, 0, b, 0, a.length);
        for(int i = 0 ; i < 4; i++){
            if(!used[i]){
                used[i] = true;
                for(int j = i+1; j < 4; j++){
                    if(!used[j]){
                        String s;
                        b[j] = a[i] + a[j];
                        if((s = dfs(b, used, steps+1)) != null){
                            s = s.replaceFirst(Integer.toString(b[j]), String.format("(%d+%d)", a[i], a[j]));
                            return s;
                        }
                        b[j] = a[i] * a[j];
                        if((s = dfs(b, used, steps+1)) != null){
                            s = s.replaceFirst(Integer.toString(b[j]), String.format("(%d×%d)", a[i], a[j]));
                            return s;
                        }
                        if(a[i] > a[j]){
                            b[j] = a[i] - a[j];
                            if((s = dfs(b, used, steps+1)) != null){
                                s = s.replaceFirst(Integer.toString(b[j]), String.format("(%d-%d)", a[i], a[j]));
                                return s;
                            }
                        }
                        else{
                            b[j] = a[j] - a[i];
                            if((s = dfs(b, used, steps+1)) != null){
                                s = s.replaceFirst(Integer.toString(b[j]), String.format("(%d-%d)", a[j], a[i]));
                                return s;
                            }
                        }
                        if(a[j] != 0 && a[i] % a[j] == 0){
                            b[j] = a[i] / a[j];
                            if((s = dfs(b, used, steps+1)) != null){
                                s = s.replaceFirst(Integer.toString(b[j]), String.format("(%d÷%d)", a[i], a[j]));
                                return s;
                            }
                        }
                        if(a[i] != 0 && a[j] % a[i]==0){
                            b[j] = a[j] / a[i];
                            if((s = dfs(b, used, steps+1)) != null){
                                s = s.replaceFirst(Integer.toString(b[j]), String.format("(%d÷%d)", a[j], a[i]));
                                return s;
                            }
                        }
                        b[j] = a[j];
                    }
                }
                used[i] = false;
            }
        }
        return null;
    }
}
