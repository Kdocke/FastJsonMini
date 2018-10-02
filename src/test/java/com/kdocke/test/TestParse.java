package com.kdocke.test;

/**
 * @author Kdocke[kdocked@gmail.com]
 * @create 2018/9/17 - 9:22
 */
public class TestParse {

    public static void main(String[] args) {

        System.out.println((int) 'f' + 1);
        System.out.println((int) '0');
        System.out.println((int) 'a');
        System.out.println((int) 'A');


        final int[] digits = new int[(int)'f' + 1];

        for (int i = '0'; i <= '9'; ++i) {
            digits[i] = i - '0';
        }

        for (int i = 'a'; i <= 'f'; ++i) {
            digits[i] = (i - 'a') + 10;
        }

        for (int i = 'A'; i <= 'F'; ++i) {
            digits[i] = (i - 'A') + 10;
        }

        for (int digit : digits) {
            System.out.println(digit);
        }

    }

}
