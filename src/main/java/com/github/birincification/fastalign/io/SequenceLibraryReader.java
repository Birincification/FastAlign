package com.github.birincification.fastalign.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;

public class SequenceLibraryReader {

    long maxlength = 0;
    HashMap<String, char[]> seqMap;

    public SequenceLibraryReader(File seqLibFile) {
        readSeq(seqLibFile);
    }

    /**
     * Benchmarked at < 50 ms for a 5.4k sequences file
     * TODO further benchmarking
     * @param seqLibFIle file containing sequence ids and their sequences.
     *                   Sample line: "1a2xB00:EEKRNRAITARRQHLKSVMLQIAATELEKE"
     */
    private void readSeq (File seqLibFIle){
        seqMap = new HashMap<>();

        int one;
        String line;
        BufferedReader br = null;

        try { br = new BufferedReader(new FileReader(seqLibFIle)); } catch (FileNotFoundException e) { throw new RuntimeException(e); }
        try {
            while ((line = br.readLine()) != null) {
                one = line.indexOf(':');                                            //TODO could make this static as the position is always the same
                if (maxlength < line.substring(one+1).length()) { maxlength = line.substring(one+1).length(); }
                seqMap.put(line.substring(0, one), line.substring(one+1).toCharArray());
            }
            br.close();
        }
        catch (Exception e) { throw new RuntimeException(e); }
    }

    public long getMaxlength() { return maxlength; }

    public HashMap<String, char[]> getSeqMap() { return seqMap; }
}
