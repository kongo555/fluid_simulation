package com.fluid.numericalIntegration;

/**
 * Created by kongo on 02.02.16.
 */
public class RK4 {
    public RK4(int n, boolean debug){
        float t = 0;
        float dt = 1;

        float velocity = 0;
        float position = 0;
        float force = 10;
        float mass = 1;

        State state = new State();
        // explicit euler
        while ( t < n ) {
            if(debug)
                System.out.println("t=" + t + ":   position="+ state.x + "   velocity=" + state.v);
            integrate(state,t,dt);
            t = t + dt;
        }
        System.out.println("t=" + t + ":   position="+ state.x + "   velocity=" + state.v);
    }

    class State{
        public float x;      // position
        public float v;      // velocity
    }

    class Derivative
    {
        public float dx;      // dx/dt = velocity
        public float dv;      // dv/dt = acceleration
    }

    public Derivative evaluate(State initial, float t, float dt, Derivative d )
    {
        //Euler Integration
        State state = new State();
        state.x = initial.x + d.dx*dt;
        state.v = initial.v + d.dv*dt;

        Derivative output = new Derivative();
        output.dx = state.v;
        output.dv = acceleration(state, dt);
        return output;
    }

    //spring and damper force
    float acceleration( State state, float t )
    {
//        float k = 10;
//        float b = 1;
//        return -k * state.x - b*state.v;
        float force = 10;
        float mass = 1;
        return  ( force / mass );
    }

    void integrate( State state, float t, float dt )
    {
        Derivative a,b,c,d;

        a = evaluate( state, t, 0.0f, new Derivative() );
        b = evaluate( state, t, dt*0.5f, a );
        c = evaluate( state, t, dt*0.5f, b );
        d = evaluate( state, t, dt, c );

        float dxdt = 1.0f / 6.0f *
                ( a.dx + 2.0f*(b.dx + c.dx) + d.dx );

        float dvdt = 1.0f / 6.0f *
                ( a.dv + 2.0f*(b.dv + c.dv) + d.dv );

        state.x = state.x + dxdt * dt;
        state.v = state.v + dvdt * dt;
    }
}
