package com.ixhong.common.utils;

import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by shenhongxi on 15/7/18.
 */
public class Main {
    public static void main(String[] args) throws Exception{
        Set<String> set = new HashSet<String>();

        PrintWriter pw = new PrintWriter("/home/data/tmp.txt");
        pw.print("hello");
    }
}
