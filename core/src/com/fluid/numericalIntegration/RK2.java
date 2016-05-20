package com.fluid.numericalIntegration;

/**
 * Created by kongo on 02.02.16.
 */
public class RK2 {
    public RK2(int n, boolean debug) {
        float t = 0;
        float dt = 1;

        float velocity = 0;
        float position = 0;

        State state = new State();
        state.x = position;
        state.v = velocity;

        float force = 10;
        float mass = 1;

        while (t < n) {
//            if(debug)
//                System.out.println("t=" + t + ":   position=" + position + "   velocity=" + velocity);
            //float velocityRK2 = velocity + 0.5f * dt * ( force / mass );
            // float midPosition = position + velocityRK2/2;
            // System.out.println(midPosition);
            //position = position + dt * velocityRK2;
            //velocity = velocity + dt * ( force / mass );

            rk2(state,dt);
            if(debug)
                System.out.println("t=" + t + ":   position=" + state.x + "   velocity=" + state.v);

            t = t + dt;
        }
        System.out.println("t=" + t + ":   position="+ position + "   velocity=" + velocity);
    }

    class State{
        public float x;      // position
        public float v;      // velocity
    }

    float acceleration( )
    {
        float force = 10;
        float mass = 1;
        return  ( force / mass );
    }

    public void rk2(State state, float dt){
        //f(q^n)
        float dxdt0 = state.v;
        float dvdt0 = acceleration();

        State halfState = new State();
        // q^(n+1/2) = q^n + 1/2 * dt * f(q^n)
        halfState.x = state.x + 0.5f * dt * dxdt0;
        halfState.v = state.v + 0.5f * dt * dvdt0;
        System.out.println("half:   position=" + halfState.x + "   velocity=" + halfState.v);

        //f(q^(n+1/2))
        float dxdt1 = halfState.v;
        float dvdt1 = acceleration();

        // q^(n+1) = q^n + dt * f(q^(n+1/2))
        state.x = state.x + dt * dxdt1;
        state.v = state.v + dt * dvdt1;
    }
}
