// Online Java Compiler
// Use this editor to write, compile and run your Java code online
import java.util.*;
public class test {
    public static void main(String[] args) {
        StringBuilder str = new StringBuilder("(()())()(()(()(())))");
        //                                     (()())((())())
        //                                     (()())(())(()(()))
        //                                     (()())(())(()(()))
        ArrayList<Integer> removeList = new ArrayList<>();
        int open=0;
        int close=0;

        for(int i =0;i<str.length();i++){
            if(i>0 && open==close && str.charAt(i-1)!=str.charAt(i)){
                removeList.add(i-1);
                removeList.add(i);
            }
            if(str.charAt(i)=='('){
                open++;
            }else{
                close++;
            }
        }
        for (Integer iteam : removeList) {
            str.deleteCharAt(iteam);
        }
        str.deleteCharAt(0);
        str.deleteCharAt(str.length()-1);
        System.out.print(str);

    }
}