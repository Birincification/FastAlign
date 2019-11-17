package com.github.birincification.fastalign.alignment;


import com.github.birincification.fastalign.utils.Pair;

import java.io.Writer;
import java.util.HashMap;
import java.util.List;

public class Alignment {

//    TODO make single/multithread differentiation -> matrix inits and the likes

    HashMap<String, char[]> seqLib;     //TODO see alternative ways for dna/protein as the alphabet is way smaller

    short[][] submat;
    int[][] ins, del;
    int gapOpen, gapExtend;

    public Alignment(HashMap<String, char[]> seqLib, short[][] submat, int gapOpen, int gapExtend) {
        this.seqLib = seqLib;
        this.submat = submat;
        this.gapOpen = gapOpen;
        this.gapExtend = gapExtend;
    }

    public Alignment(HashMap<String, char[]> seqLib, short[][] submat) {
        this.seqLib = seqLib;
        this.submat = submat;
        this.gapOpen = gapOpen;
        this.gapExtend = gapExtend;
    }

    public Alignment(HashMap<String, char[]> seqLib, short[][] submat, int maxlen) {
        this.seqLib = seqLib;
        this.submat = submat;
        ins = new int[maxlen+1][maxlen+1];
        del = new int[maxlen+1][maxlen+1];
    }

    /**
     *
     * @param gapOpen
     * @param gapExtend
     * @param seqList
     * @return
     */
    public List<String> computeGLobalAlignment (int gapOpen, int gapExtend, List<Pair<String, String>> seqList) {   //FIXME make pair not specific for string ....
        return null;
    }

    public List<String> computeGLobalAlignment (List<Pair> seqList) {
        return null;
    }

    /**
     * Here i want to test what is better, have global matrices for insertion/deletion/addition or create new matrices for each seqPair and make it parallel.
     * I guess the most important part is for it to run quickly with or without much RAM -> so speed should be top priority.
     * TODO replace the forEach with the standard Iterator
     * @param writer
     * @param seqList
     */
//    CompletableFuture.supplyAsync(() -> seqList).thenApply(pair -> getAlignment(pair)).thenAccept(res -> System.out.println("Alignment: " + res));
    public void computeGLobalAlignment (Writer writer, List<Pair<String, String>> seqList) {    //TODO add constructor with gapOpen/Extend
        seqList.stream().parallel().forEach(pair -> {
            System.out.print(computeAli(pair.getFirst(), pair.getSecond()));
//            try { writer.write(computeAli(pair.getFirst(), pair.getSecond())); } catch (IOException e) { e.printStackTrace(); }
        });
    }

    private String computeAli (String seq1, String seq2) {
        char[] first = seqLib.get(seq1), second = seqLib.get(seq2);
        int f = first.length, s = second.length;
        int maxlength = (f > s) ? f : s;
        int[][] a = new int[maxlength+1][maxlength+1], ins = new int[maxlength+1][maxlength+1], del = new int[maxlength+1][maxlength+1];

        for(int k = 1; k <= maxlength; k++){
            a[k][0] = gapOpen + k * gapExtend;
            a[0][k] = gapOpen + k * gapExtend;
            del[k][0] = -2000000000;
            ins[0][k] = -2000000000;
        }
        for(int i = 1; i <= f; i++){
            for(int j = 1; j <= s; j++){
                ins[i][j] = Math.max(a[i-1][j] + gapOpen + gapExtend, ins[i-1][j] + gapExtend);
                del[i][j] =	Math.max(a[i][j-1] + gapOpen + gapExtend, del[i][j-1] + gapExtend);
//                a[i][j] = Math.max(a[i-1][j-1] + submat[((int)first[i-1])][((int)second[j-1])], Math.max(ins[i][j], del[i][j]));
                a[i][j] = Math.max(a[i-1][j-1] + submat[((int)first[i-1])][((int)second[j-1])], (ins[i][j] >= del[i][j]) ? ins[i][j] : del[i][j]);
            }
        }
        int alignmentScore = a[f][s];

        StringBuilder left = new StringBuilder(), up = new StringBuilder(), builder = new StringBuilder();
        int lremain = f, uremain = s;
        int i = f, j = s;
        while(true){        //TODO optimize and make nice
            int counter2 = 1;
            if(uremain == 0 || lremain == 0){break;}
            if(a[i][j] == ins[i][j]){
                while(true){
                    if(uremain == 0 || lremain == 0 || j==0 || i== 0){break;}
                    if((a[i-counter2][j] + gapOpen + gapExtend * counter2) == ins[i][j]){
                        left.append(first[i-counter2]);
                        up.append('-');
                        lremain--;
                        i -= counter2;
                        counter2=1;
                        break;
                    }
                    left.append(first[i-counter2]);
                    up.append('-');
                    lremain--;
                    counter2++;}}
            else if(a[i][j] == del[i][j]) {
                while(true){
                    if(uremain == 0 || lremain == 0 || j==0 || i== 0){ break; }
                    if((a[i][j-counter2] + gapOpen + gapExtend * counter2) == del[i][j]){
                        left.append('-');
                        up.append(second[j - counter2]);
                        uremain--;
                        j -= counter2;
                        counter2 = 1;
                        break;
                    }
                    left.append('-');
                    up.append(second[j-counter2]);
                    uremain--;
                    counter2++;}}
            else{
                left.append(first[i-1]);
                up.append(second[j-1]);
                uremain--;
                lremain--;
                i--;j--;}}
        if(lremain > 0){
            for(int k=lremain;k>0;k--){
                left.append(first[k-1]);
                up.append('-');}}
        else if(uremain > 0){
            for(int k=uremain;k>0;k--){
                left.append('-');
                up.append(second[k-1]);}}

        builder.append(">");
        builder.append(seq1);
        builder.append(" ");
        builder.append(seq2);
        builder.append(" ");
        builder.append((float)(alignmentScore)/10);
        builder.append("\n");
        builder.append(seq1);
        builder.append(": ");
        builder.append(left.reverse().toString());
        builder.append("\n");
        builder.append(seq2);
        builder.append(": ");
        builder.append(up.reverse().toString());
        builder.append("\n");

        return builder.toString();
    }

    private String getAlignment(List<Pair<String, String>> pair) {
        return "";
    }

    public List<String> computeLocalAlignment () {
        return null;
    }

    public void computeLocalAlignment (Writer writer) {

    }

    public List<String> computeFreeshiftAlignment () {
        return null;
    }

    public void computeFreeshiftAlignment (Writer writer) {

    }
}

