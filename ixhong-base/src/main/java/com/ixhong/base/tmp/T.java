package com.ixhong.base.tmp;

import java.io.BufferedReader;
import java.io.FileReader;

/**
 * Created by shenhongxi on 2015/11/9.
 */
public class T {
    public static void main(String[] args) throws Exception {
        String path = "E:/vote.txt";
        BufferedReader br = new BufferedReader(new FileReader(path));
        String line = null;
        while ((line = br.readLine()) != null) {
            System.out.println("'" + line + "',");
        }
        br.close();
    }
}
