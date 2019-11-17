package com.github.birincification.fastalign.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class SubstitutionMatrixReader {

    //TODO need initializer for this, 91 as default is fine, but it should also be variable
    short[][] submat = new short[91][91];

    public SubstitutionMatrixReader(File matrixFile) { readMatrix(matrixFile); }

    /**
     * @return short array containing the substitution scores for the ascii value positions of the amino acids in the most common case.
     * Example:
     *      The substitution score of A -> T is in the (int)A -> i=65 and (int)T -> j=84, so the score is in submat[65][84].
     *
     * Side note: Substitution matrices in this format are symmetric, meaning submat[i][j] = submat[j][i].
     */
    public short[][] getSubmat() { return submat; }

    /**
     * Benchmarked at < 20 ms per matrix file
     * @param matrixFIle matrix file in QUASAR format. Contains substitution scores.
     */
    private void readMatrix(File matrixFIle) {
        short counter = 0;
        String line, col = "", row = "", rowIndex = "ROWINDEX", colIndex = "COLINDEX", matrixIndex = "MATRIX";
        BufferedReader br = null;

        try { br = new BufferedReader(new FileReader(matrixFIle)); } catch (Exception e) { throw new RuntimeException(e); }
        try {
            while ((line = br.readLine()) != null) {
                String[] l = line.split("\\s+");

                if (line.charAt(0) == 'M') {
                    for (int i = 1; i < l.length; i++) {
                        submat[(int) (col.charAt(counter))][(int) (row.charAt(i - 1))] = (short) (Double.parseDouble(l[i]) * 10);
                        submat[(int) (row.charAt(i - 1))][(int) (col.charAt(counter))] = submat[(int) (col.charAt(counter))][(int) (row.charAt(i - 1))];
                    }
                    counter++;
                } else if (l[0].equals(rowIndex)) {
                    row = l[1];
                } else if (l[0].equals(colIndex)) {
                    col = l[1];
                }
            }
            br.close();
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    /**
     * Prints the matrix to standard system out.
     * TODO overload to also get a destination
     */
    public void printMatrix() {
        for (int i = 0; i < 91; i++) {
            for (int j = 0; j < 91; j++) {
                System.out.print(submat[i][j] + " ");
            }
            System.out.println();
        }
    }
}

