package com.fluid.stam;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;

/**
 * Created by kongo on 06.02.16.
 */
public class Stam {
    private int width, height;
    int oldMouseX = 1, oldMouseY = 1 ;
    int N = 64;
    int SIZE = (N+2)*(N+2);

    float force = 5.0f;
    float source = 100.0f;
    float visc = 0.0f;
    float diff = 0.0f;

    float[] u = new float[SIZE];
    float[] v = new float[SIZE];
    float[] u_prev = new float[SIZE];
    float[] v_prev = new float[SIZE];

    float[] dens = new float[SIZE];
    float[] dens_prev = new float[SIZE];

    float[] tmp;

    public Stam(int width, int height) {
        this.width = width;
        this.height = height;
    }


    int IX(int i, int j) {
        return (i + ((N+2) * j));
    }

    void addSource(float[] x, float[] s, float dt) {
        for(int i=0;i<SIZE;i++)
        {
            x[i] += dt*s[i];
        }
    }


    /**
     * Sets boundary for diffusion. It is bound vertically and horizontally in a box.
     **/
    void setBnd(int b, float[] x) {
        for(int i = 0; i<= N; i++) {
            x[IX(0,i)] = (b==1 ? -x[IX(1,i)] : x[IX(1,i)]);
            x[IX(N+1,i)] = (b==1 ? -x[IX(N,i)] : x[IX(N,i)]);
            x[IX(i,0)] = (b==2 ? -x[IX(i,1)] : x[IX(i,1)]);
            x[IX(i,N+1)] = (b==2 ? -x[IX(i,N)] : x[IX(i,N)]);
        }
        x[IX(0,0)] = 0.5f * (x[IX(1,0)] + x[IX(0,1)]);
        x[IX(0,N+1)] = 0.5f * (x[IX(1,N+1)] + x[IX(0,N)]);
        x[IX(N+1,0)] = 0.5f * (x[IX(N,0)] + x[IX(N+1,1)]);
        x[IX(N+1,N+1)] = 0.5f * (x[IX(N,N+1)] + x[IX(N+1 ,N)]);
    }

    /**
     * 2nd Step:
     * Diffusion at rate diff. Each cell will exchange density with direct neighbours.
     * Uses Gauss-Seidel relaxation.
     */
    void diffuse(int b, float[] x, float[] x0, float diff, float dt) {
        int i, j, k;
        float a = dt * diff*N*N;

        for(k=0 ; k<20; k++)
        {
            for(i=1;i<=N;i++)
            {
                for(j=1;j<=N;j++)
                {
                    x[IX(i,j)] = (x0[IX(i,j)] +
                            a * (x[IX(i - 1, j)] + x[IX(i + 1,j)] + x[IX(i, j - 1)] + x[IX(i,j + 1)])) / (1 + (4 * a));

                }
            }
            setBnd(b,x);
        }
    }


    /**
     * 3rd Step :
     * Calculating advections. This ensures that the density follows a given velocity field.
     **/
    void advect(int b, float[] d, float[] d0, float[] u, float[] v, float dt)
    {
        int i, j, i0, j0, i1, j1;
        float x, y, s0, t0, s1, t1, dt0;

        dt0 = dt*N;
        for(i=1 ; i<=N ; i++)
        {
            for(j=1 ; j<=N ; j++)
            {
                x = i - dt0 * u[IX(i,j)];
                y = j - dt0 * v[IX(i,j)];

                if(x < 0.5f)
                {
                    x = 0.5f;
                }
                if(x > (N+0.5f))
                {
                    x = N + 0.5f;
                }
                i0 = (int)x;
                i1 = i0 + 1;

                if(y < 0.5f)
                {
                    y = 0.5f;
                }
                if(y > (N+0.5f))
                {
                    y = N + 0.5f;
                }
                j0 = (int)y;
                j1 = j0 + 1;

                s1 = x - i0;
                s0 = 1 - s1;

                t1 = y - j0;
                t0 = 1 - t1;

                d[IX(i,j)] = s0 * (t0 * d0[IX(i0,j0)] + t1 * d0[IX(i0,j1)]) +
                        s1 * (t0 * d0[IX(i1,j0)] + t1 * d0[IX(i1,j1)]);
            }
        }
        setBnd(b, d);
    }


    void densStep(float[] x, float[] x0, float[] u, float[] v, float diff, float dt) {
        addSource(x, x0, dt);
        float[] temp = x0;
        x0 = x;
        x = temp;
        //   swap(x0, x);
        diffuse(0, x, x0, diff, dt);
        //   swap(x0, x);
        temp = x0;
        x0 = x;
        x = temp;
        advect(0, x, x0, u, v, dt);
    }


    void project(float[] u, float[] v, float[] p, float[] div) {
        int i,j,k;
        float h = 1.0f/N;
        for(i=1; i<=N ;i++)
        {
            for(j=1;j<=N;j++)
            {
                div[IX(i,j)] = -0.5f*h*(u[IX(i+1,j)] - u[IX(i-1,j)] +
                        v[IX(i,j+1)] - v[IX(i,j-1)]);
                p[IX(i,j)] = 0;
            }
        }
        setBnd(0, div);
        setBnd(0, p);

        for(k=0;k<20;k++)
        {
            for(i=1;i<=N;i++)
            {
                for(j=1;j<=N;j++)
                {
                    p[IX(i,j)] = (div[IX(i,j)]+p[IX(i-1,j)]+p[IX(i+1,j)]+
                            p[IX(i,j-1)]+p[IX(i,j+1)]) / 4;
                }
            }
            setBnd(0, p);
        }

        for(i=1;i<=N;i++)
        {
            for(j=1;j<=N;j++)
            {
                u[IX(i,j)] -= 0.5f*(p[IX(i+1,j)]-p[IX(i-1,j)])/h;
                v[IX(i,j)] -= 0.5f*(p[IX(i,j+1)]-p[IX(i,j-1)])/h;
            }
        }
        setBnd(1, u);
        setBnd(2, v);
    }

    void velStep(float[] u, float[] v, float[] u0, float[] v0, float visc, float dt) {
        addSource(u, u0, dt);
        addSource(v, v0, dt);

        //    swap(u0, u);
        float[] temp = u0;
        u0 = u;
        u = temp;

        diffuse(1, u, u0, visc, dt);

        //    swap(v0, v);
        temp = v0;
        v0 = v;
        v = temp;

        diffuse(2, v, v0, visc, dt);
        project(u, v, u0, v0);

        //    swap(u0, u);
        temp = u0;
        u0 = u;
        u = temp;

        //    swap(v0, v);
        temp = v0;
        v0 = v;
        v = temp;

        advect(1, u, u0, u0, v0, dt);
        advect(2, v, v0, u0, v0, dt);
        project(u, v, u0, v0);
    }

    public void update(float dt, Vector3 mousePosition){
        getForcesFromUI(mousePosition, dens_prev, u_prev, v_prev);
        velStep(u, v, u_prev, v_prev, visc, dt);
        densStep(dens, dens_prev, u, v, diff, dt);
    }

    void getForcesFromUI(Vector3 mousePosition, float[]d, float[] u, float[] v) {
        int i, j;

        for(i=0;i<SIZE;i++) {
            u[i] = 0.0f;
            v[i] = 0.0f;
            d[i] = 0.0f;
        }

        i = (int)((mousePosition.x / width)*N+1);
        j = (int)((mousePosition.y / height)*N+1);

        if( (i<1) || (i>N) || (j<1) || (j>N)) {
            return;
        }

        if(Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            u[IX(i,j)] = force * (mousePosition.x-oldMouseX);
            v[IX(i,j)] = -force * (oldMouseY-mousePosition.y);
        }

        if(Gdx.input.isButtonPressed(Input.Buttons.RIGHT)) {
            d[IX(i,j)] = source; //Set density to initial value
        }

        oldMouseX = (int)mousePosition.x;
        oldMouseY = (int)mousePosition.y;
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

        int cellWidth =  width / N;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for(i=0;i<=N;i++) {
            x = i * cellWidth;

            for(j=0;j<=N;j++) {
                y = j*cellWidth;

                shapeRenderer.setColor(dens[IX(i,j)],dens[IX(i,j)],dens[IX(i,j)], 1);
                shapeRenderer.rect(x,y,cellWidth,cellWidth);
            }
        }
        shapeRenderer.end();
    }

    void drawVelocity(ShapeRenderer shapeRenderer)
    {
        int i,j;
        float x, y, x2, y2;

        int cellWidth =  width / N;
        int scale = 1000;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.GREEN);
        for(i=1;i<=N;i++) {
            x = i * cellWidth;

            for(j=1;j<=N;j++) {
                y = j * cellWidth;

                x2 = x + (u[IX(i,j)] * scale);
                y2 = y + (v[IX(i,j)] * scale);

                shapeRenderer.line(x, y, x2, y2);
            }
        }
        shapeRenderer.end();
    }


}
