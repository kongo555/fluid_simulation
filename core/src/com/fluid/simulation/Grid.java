package com.fluid.simulation;

import com.badlogic.gdx.math.MathUtils;

/**
 * Created by kongo on 02.02.16.
 */
public class Grid {
    private int width;
    private int height;
    private int n;
    private float dt;

    float[][] tmp;

    float[][] d, dOld;
    float[][] u, uOld;
    float[][] v, vOld;

    public Grid(int width, int height, int n, float dt){
        this.width = width;
        this.height = height;
        setup(n, dt);
    }

    private void setup(int n, float dt) {
        this.n = n;
        this.dt = dt;

        reset();
    }

    private void reset() {
        //+2 for boundries
        d    = new float[n+2][n+2];
        dOld = new float[n+2][n+2];

        //+1 for staggered grid
        u    = new float[n+1+2][n+2];
        uOld = new float[n+1+2][n+2];
        v    = new float[n+2][n+1+2];
        vOld = new float[n+2][n+1+2];

//        for (int i = 0; i < size; i++)
//        {
//            u[i] = uOld[i] = v[i] = vOld[i] = 0.0f;
//            d[i] = dOld[i] = 0.0f;
//        }

    }

    private void velocitySolver(float dt){
        // add velocity that was input by mouse
        addSource(u, uOld);
        addSource(v, vOld);

        // self advect velocities
        swapU();
        swapV();

        advectU(uOld, vOld);
        advectV(uOld, vOld);
        // TODO body forces(gravitation)

        // projection

    }

    //TODO chenge to RK2
    private void advect(float[][] d, float[][] d0) {
        float x, y;
        float dt0 = dt * n;

        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= n; j++) {
                // go backwards through velocity field
                x = i - dt0 * (u[i-1][j] + u[i][j])*0.5f;
                y = j - dt0 * (v[i][j-1] + v[i][j])*0.5f;

                // interpolate results
                interpolation(d, d0, i, j, x, y);
            }
        }
        //setBoundry(0, d);
    }

    private void advectU(float[][] du, float[][] dv) {
        float x, y;
        float dt0 = dt * n;

        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= n; j++) {
                // go backwards through velocity field
                x = i - dt0 *  du[i][j];
                y = j - dt0 * (dv[i][j-1] + dv[i][j] + dv[i+1][j-1] + dv[i+1][j])*0.25f;

                // interpolate results
                interpolation(u, uOld, i, j, x, y);
            }
        }
        //setBoundry(1, d);
    }

    private void advectV(float[][] du, float[][] dv) {
        float x, y;
        float dt0 = dt * n;

        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= n; j++) {
                // go backwards through velocity field
                x = i - dt0 * (du[i-1][j] + du[i][j] + du[i-1][j+1] + du[i][j+1])*0.25f;
                y = j - dt0 * dv[i][j];

                // interpolate results
                interpolation(v, vOld, i, j, x, y);
            }
        }
        //setBoundry(2, d);
    }

    //TODO try cubic(bicubic) interpolation
    private int i0, j0, i1, j1;
    private float s0, t0, s1, t1;
    private void interpolation(float[][] d, float d0[][], int i, int j, float x, float y){
        x = MathUtils.clamp(x, 0.5f, n + 0.5f);
        i0 = (int) x;
        i1 = i0 + 1;

        y = MathUtils.clamp(y, 0.5f, n + 0.5f);

        j0 = (int) y;
        j1 = j0 + 1;

        s1 = x - i0;
        s0 = 1 - s1;
        t1 = y - j0;
        t0 = 1 - t1;

        d[i][j] = s0 * (t0 * d0[i0][j0] + t1 * d0[i0][j1])
                + s1 * (t0 * d0[i1][j0] + t1 * d0[i1][j1]);
    }

    private void addSource(float[][] x, float[][] x0) {
        for (int i = 0; i < x.length; i++) {
            for (int j = 0; j < x[0].length; j++) {
                x[i][j] += dt * x0[i][j];
            }
        }
    }

    // util array swapping methods
    public void swapU(){ tmp = u; u = uOld; uOld = tmp; }
    public void swapV(){ tmp = v; v = vOld; vOld = tmp; }
    public void swapD(){ tmp = d; d = dOld; dOld = tmp; }

    private float getU(int i, int j){
        return (u[i][j] + u[i+1][j])*0.5f;
    }

    private float getV(int i, int j){
        return (v[i][j] + v[i+1][j])*0.5f;
    }
}
