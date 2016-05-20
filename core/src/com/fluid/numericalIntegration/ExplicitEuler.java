package com.fluid.numericalIntegration;

/**
 * Created by kongo on 02.02.16.
 */
public class ExplicitEuler {
    public ExplicitEuler(int n, boolean debug){
        float t = 0;
        float dt = 1;

        float velocity = 0;
        float position = 0;
        float force = 10;
        float mass = 1;

        // explicit euler
        while ( t < n ) {
            if(debug)
                System.out.println("t=" + t + ":   position="+ position + "   velocity=" + velocity);
            //s = s + v*dt gdzie v=dx/dt
            position = position + velocity * dt;
            //v = v + (f/m)*dt gdzie  a = F/m = dv/dt
            velocity = velocity + ( force / mass ) * dt;
            t = t + dt;
        }
        System.out.println("t=" + t + ":   position="+ position + "   velocity=" + velocity);
    }
}
