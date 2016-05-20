package com.fluid.birdson;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;

public class BirdsonSolver {
    private final int width;
    private final int height;
    int oldMouseX = 1, oldMouseY = 1 ;
    float force = 5.0f;
    float source = 100.0f;

    int n, size;
    float dt;

    float visc = 0.0f;
    float diff = 0.0f;

    float[] tmp;

    float[] d, dOld;
    float[] u, uOld;
    float[] v, vOld;

    public BirdsonSolver(int width, int height, int n, float dt){
        this.width = width;
        this.height = height;
        setup(n, dt);
    }

    public void setup(int n, float dt) {
        this.n = n;
        this.dt = dt;
        size = (n + 2) * (n + 2);

        reset();
    }

    public void reset()
    {
        d    = new float[size];
        dOld = new float[size];
        u    = new float[size];
        uOld = new float[size];
        v    = new float[size];
        vOld = new float[size];

        for (int i = 0; i < size; i++)
        {
            u[i] = uOld[i] = v[i] = vOld[i] = 0.0f;
            d[i] = dOld[i] = 0.0f;
        }

    }

    public void update(Vector3 mousePosition){
        for(int i=0;i<size;i++) {
            uOld[i] = 0.0f;
            vOld[i] = 0.0f;
            dOld[i] = 0.0f;
        }

        getForcesFromUI(mousePosition);
        velocitySolver();
        densitySolver();
    }

    void getForcesFromUI(Vector3 mousePosition) {
        int i, j;

        i = (int)((mousePosition.x / width)*n+1);
        j = (int)((mousePosition.y / height)*n+1);

        if( (i<1) || (i>n) || (j<1) || (j>n)) {
            return;
        }

        if(Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            uOld[I(i,j)] = force * (mousePosition.x-oldMouseX);
            vOld[I(i,j)] = -force * (oldMouseY-mousePosition.y);
        }

        if(Gdx.input.isButtonPressed(Input.Buttons.RIGHT)) {
            dOld[I(i,j)] = source; //Set density to initial value
        }

        oldMouseX = (int)mousePosition.x;
        oldMouseY = (int)mousePosition.y;
    }


    private void velocitySolver() {
        // add velocity that was input by mouse
        addSource(u, uOld);
        addSource(v, vOld);

        // swapping arrays for economical mem use
        // and calculating diffusion in velocity.
        swapU();
        diffuse(0, u, uOld, visc);

        swapV();
        diffuse(0, v, vOld, visc);

        // we create an incompressible field
        // for more effective advection.
        project(u, v, uOld, vOld);

        swapU(); swapV();

        // self advect velocities
        advect(1, u, uOld, uOld, vOld);
        advect(2, v, vOld, uOld, vOld);

        // make an incompressible field
        project(u, v, uOld, vOld);
    }


    private void densitySolver() {
        // add density inputted by mouse
        addSource(d, dOld);
        swapD();

        diffuse(0, d, dOld, diff);
        swapD();

        advect(0, d, dOld, u, v);
    }


    private void addSource(float[] x, float[] x0) {
        for (int i = 0; i < size; i++)
        {
            x[i] += dt * x0[i];
        }
    }


    /**
     * Calculate the input array after advection. We start with an
     * input array from the previous timestep and an and output array.
     * For all grid cells we need to calculate for the next timestep,
     * we trace the cell's center position backwards through the
     * velocity field. Then we interpolate from the grid of the previous
     * timestep and assign this value to the current grid cell.
     *
     * @param b Flag specifying how to handle boundries.
     * @param d Array to store the advected field.
     * @param d0 The array to advect.
     * @param du The x component of the velocity field.
     * @param dv The y component of the velocity field.
     **/

    private void advect(int b, float[] d, float[] d0, float[] du, float[] dv) {
        int i0, j0, i1, j1;
        float x, y, s0, t0, s1, t1, dt0;

        dt0 = dt * n;

        for (int i = 1; i <= n; i++)
        {
            for (int j = 1; j <= n; j++)
            {
                // go backwards through velocity field
                x = i - dt0 * du[I(i, j)];
                y = j - dt0 * dv[I(i, j)];

                // interpolate results
                if (x > n + 0.5) x = n + 0.5f;
                if (x < 0.5)     x = 0.5f;

                i0 = (int) x;
                i1 = i0 + 1;

                if (y > n + 0.5) y = n + 0.5f;
                if (y < 0.5)     y = 0.5f;

                j0 = (int) y;
                j1 = j0 + 1;

                s1 = x - i0;
                s0 = 1 - s1;
                t1 = y - j0;
                t0 = 1 - t1;

                d[I(i, j)] = s0 * (t0 * d0[I(i0, j0)] + t1 * d0[I(i0, j1)])
                        + s1 * (t0 * d0[I(i1, j0)] + t1 * d0[I(i1, j1)]);

            }
        }
        setBoundry(b, d);
    }



    /**
     * Recalculate the input array with diffusion effects.
     * Here we consider a stable method of diffusion by
     * finding the densities, which when diffused backward
     * in time yield the same densities we started with.
     * This is achieved through use of a linear solver to
     * solve the sparse matrix built from this linear system.
     *
     * @param b Flag to specify how boundries should be handled.
     * @param c The array to store the results of the diffusion
     * computation.
     * @param c0 The input array on which we should compute
     * diffusion.
     * @param diff The factor of diffusion.
     **/

    private void diffuse(int b, float[] c, float[] c0, float diff) {
        float a = dt * diff * n * n;
        linearSolver(b, c, c0, a, 1 + 4 * a);
    }


    /**
     * Use project() to make the velocity a mass conserving,
     * incompressible field. Achieved through a Hodge
     * decomposition. First we calculate the divergence field
     * of our velocity using the mean finite differnce approach,
     * and apply the linear solver to compute the Poisson
     * equation and obtain a "height" field. Now we subtract
     * the gradient of this field to obtain our mass conserving
     * velocity field.
     *
     * @param x The array in which the x component of our final
     * velocity field is stored.
     * @param y The array in which the y component of our final
     * velocity field is stored.
     * @param p A temporary array we can use in the computation.
     * @param div Another temporary array we use to hold the
     * velocity divergence field.
     *
     **/

    private void project(float[] x, float[] y, float[] p, float[] div) {
        for (int i = 1; i <= n; i++)
        {
            for (int j = 1; j <= n; j++)
            {
                div[I(i, j)] = (x[I(i+1, j)] - x[I(i-1, j)]
                        + y[I(i, j+1)] - y[I(i, j-1)])
                        * - 0.5f / n;
                p[I(i, j)] = 0;
            }
        }

        setBoundry(0, div);
        setBoundry(0, p);

        linearSolver(0, p, div, 1, 4);

        for (int i = 1; i <= n; i++)
        {
            for (int j = 1; j <= n; j++)
            {
                x[I(i, j)] -= 0.5f * n * (p[I(i+1, j)] - p[I(i-1, j)]);
                y[I(i, j)] -= 0.5f * n * (p[I(i, j+1)] - p[I(i, j-1)]);
            }
        }

        setBoundry(1, x);
        setBoundry(2, y);
    }


    /**
     * Iterative linear system solver using the Gauss-sidel
     * relaxation technique. Room for much improvement here...
     *
     **/

    private void linearSolver(int b, float[] x, float[] x0, float a, float c)
    {
        for (int k = 0; k < 20; k++)
        {
            for (int i = 1; i <= n; i++)
            {
                for (int j = 1; j <= n; j++)
                {
                    x[I(i, j)] = (a * ( x[I(i-1, j)] + x[I(i+1, j)]
                            +   x[I(i, j-1)] + x[I(i, j+1)])
                            +  x0[I(i, j)]) / c;
                }
            }
            setBoundry(b, x);
        }
    }


    // specifies simple boundry conditions.
    private void setBoundry(int b, float[] x) {
        for (int i = 1; i <= n; i++) {
            x[I(  0, i  )] = b == 1 ? -x[I(1, i)] : x[I(1, i)];
            x[I(n+1, i  )] = b == 1 ? -x[I(n, i)] : x[I(n, i)];
            x[I(  i, 0  )] = b == 2 ? -x[I(i, 1)] : x[I(i, 1)];
            x[I(  i, n+1)] = b == 2 ? -x[I(i, n)] : x[I(i, n)];
        }

        x[I(  0,   0)] = 0.5f * (x[I(1, 0  )] + x[I(  0, 1)]);
        x[I(  0, n+1)] = 0.5f * (x[I(1, n+1)] + x[I(  0, n)]);
        x[I(n+1,   0)] = 0.5f * (x[I(n, 0  )] + x[I(n+1, 1)]);
        x[I(n+1, n+1)] = 0.5f * (x[I(n, n+1)] + x[I(n+1, n)]);

    }

    public void render(ShapeRenderer shapeRenderer, boolean drawVel){
        drawDensity(shapeRenderer);
        if(drawVel)
            drawVelocity(shapeRenderer);

    }

    void drawDensity(ShapeRenderer shapeRenderer)
    {
        int i,j;
        float x, y;

        int cellWidth =  width / n;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for(i=0;i<=n;i++) {
            x = i * cellWidth;

            for(j=0;j<=n;j++) {
                y = j*cellWidth;

                shapeRenderer.setColor(d[I(i,j)],d[I(i,j)],d[I(i,j)], 1);
                shapeRenderer.rect(x,y,cellWidth,cellWidth);
            }
        }
        shapeRenderer.end();
    }

    void drawVelocity(ShapeRenderer shapeRenderer)
    {
        int i,j;
        float x, y, x2, y2;

        int cellWidth =  width / n;
        int scale = 1000;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.GREEN);
        for(i=1;i<=n;i++) {
            x = i * cellWidth;

            for(j=1;j<=n;j++) {
                y = j * cellWidth;

                x2 = x + (u[I(i,j)] * scale);
                y2 = y + (v[I(i,j)] * scale);

                shapeRenderer.line(x, y, x2, y2);
            }
        }
        shapeRenderer.end();
    }

    // util array swapping methods
    public void swapU(){ tmp = u; u = uOld; uOld = tmp; }
    public void swapV(){ tmp = v; v = vOld; vOld = tmp; }
    public void swapD(){ tmp = d; d = dOld; dOld = tmp; }

    // util method for indexing 1d arrays
    private int I(int i, int j){ return i + (n + 2) * j; }
}

