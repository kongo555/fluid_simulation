package com.fluid.numericalIntegration;

/**
 * Created by kongo on 02.02.16.
 */
public class Test {
    public static void main(String args[]){
        int n = 10;
        boolean debug = false;
        //ExplicitEuler explicitEuler = new ExplicitEuler(n, debug);
        RK2 rk2 = new RK2(n, debug);
        //RK4 rk4 = new RK4(n, debug);
    }
}
