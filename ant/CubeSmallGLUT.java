package org.example.ant;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

public class CubeSmallGLUT extends SmallGLUT {

    private float mSize;
    
    private FloatBuffer mVertexBuffer;
    private FloatBuffer mNormalBuffer;
    private ByteBuffer mIndexBuffer;
    
    CubeSmallGLUT(float size) throws IllegalArgumentException {
	if (size < 0.0f) {
	    throw new IllegalArgumentException("Only use positive size values to CubeSmallGLUT");
	}
	// scale unit cube to this size
	mSize = size;
	
	if (mSize != 1.0f) {
	    for (int vertex = 0; vertex < vertices1.length; vertex++ ) {
		vertices1[vertex] *= mSize;
	    }
	}
	
	mVertexBuffer = getFloatBufferFromFloatArray(vertices1);
	mNormalBuffer = getFloatBufferFromFloatArray(normals1);
	mIndexBuffer = getByteBufferFromByteArray(indices1);
    }
    
    // center our unit cube around the origin so translations make sense
    float vertices[] = {
	    // front
	    -0.5f, 0.5f, 0.5f,   0.5f, 0.5f, 0.5f,   0.5f,-0.5f, 0.5f,  -0.5f,-0.5f, 0.5f,
	    
	    // back
	     0.5f, 0.5f,-0.5f,  -0.5f, 0.5f,-0.5f,  -0.5f,-0.5f,-0.5f,   0.5f,-0.5f,-0.5f,
	    
	    // top
	    -0.5f, 0.5f,-0.5f,   0.5f, 0.5f,-0.5f,   0.5f, 0.5f, 0.5f,  -0.5f, 0.5f, 0.5f,
	    
	    // bottom
	    -0.5f,-0.5f, 0.5f,   0.5f,-0.5f, 0.5f,   0.5f,-0.5f,-0.5f,  -0.5f,-0.5f,-0.5f,
	    
	    // right
	     0.5f, 0.5f, 0.5f,   0.5f, 0.5f,-0.5f,   0.5f,-0.5f,-0.5f,   0.5f,-0.5f, 0.5f,
	     
	    // left
	    -0.5f, 0.5f,-0.5f, -0.5f, 0.5f, 0.5f,  -0.5f,-0.5f, 0.5f,  -0.5f,-0.5f,-0.5f,
    };
    
    float vertices1[] = {
            -2, 6, 4,  2, 6, 4,  2, -6, 4,  -2, -6, 4,
            4, 6, 2,  4, 6, -2,  4, -6, -2,  4, -6, 2,
            2, 6, -4,  -2, 6, -4,  -2, -6, -4,  2, -6, -4,
            -4, 6, -2,  -4, 6, 2,  -4, -6, 2,  -4, -6, -2,
    };
    
    
    byte indices[] = {
	    //front
	    0,1,2, 2,3,0,
	    //back
	    4,5,6, 6,7,4,
	    // top
	    8,9,10, 10,11,8,
	    // bottom
	    12,13,14,  14,15,12,
	    //right
	    16,17,18,  18,19,16,
	    //left
	    20,21,22,  22,23,20,
    };
    
    byte indices1[] = {
    		0,1,2,  2,3,0,  1,4,7,  7,2,1,  4,5,6, 6,7,4, 5,8,11, 11,6,5, 8,9,10, 10,11,8,  9,12,15,
    		15,10,9,  12,13,14,  14,15,12,  13,0,3,  3,14,13 
    };
    
    float normals[] = {
    // front
            0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1,
            // back
            0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1,
            // top
            0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0,
            // bottom
            0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0,
            // right
            1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0,
            // left
            -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, };
    
    float normals1[] = {
    	    // front
    	    0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1,
            // back
    	    0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1,
    	    // right
    	    1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0,
    	    // left
    	    -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0,
    	    // right
    	    1, 0, 1, 1, 0, 1, 1, 0, 1, 1, 0, 1,
    	    // left
    	    -1, 0, -1, -1, 0, -1, -1, 0, -1, -1, 0, -1,
    	    // right
    	    1, 0, -1, 1, 0, -1, 1, 0, -1, 1, 0, -1,
    	    // left
    	    -1, 0, 1, -1, 0, 1, -1, 0, 1, -1, 0, 1,};
    
    
    void draw(GL10 gl) {
	// need for correct culling
	gl.glFrontFace(GL10.GL_CW);
	       
	// make sure our state is correct
        gl.glEnableClientState(GL10.GL_NORMAL_ARRAY);

        // set our vertexes
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mVertexBuffer);

        // set our normals for correct lighting
        gl.glNormalPointer(GL10.GL_FLOAT, 0, mNormalBuffer);

        // draw the elements
        gl.glDrawElements(GL10.GL_TRIANGLES, indices1.length,
                GL10.GL_UNSIGNED_BYTE, mIndexBuffer);
	
	// GLUT disables the client state -- we don't. Why does it? .. 
	// TODO: consider a preDraw, draw, postDraw ...
	
    }
    
    void drawAgain(GL10 gl) {
	// used to draw again when the vertex and normal pointers haven't changed -- won't work otherwise

	// draw the elements -- assuming all of the above prep has taken place
	gl.glDrawElements(GL10.GL_TRIANGLES, indices.length, GL10.GL_UNSIGNED_BYTE, mIndexBuffer);
    }
    
    void drawSimpleCube (GL10 gl) {
        float vertices[] = {
                -1,1,1, 1,1,1, 1,-1,1, -1,-1,1,
                1,1,-1, -1,1,-1, -1,-1,-1, 1,-1,-1
        };
        float vertices1[] = {
                -2, 6, 4,  2, 6, 4,  2, -6, 4,  -2, -6, 4,
                4, 6, 2,  4, 6, -2,  4, -6, -2,  4, -6, 2,
                2, 6, -4,  -2, 6, -4,  -2, -6, -4,  2, -6, -4,
                -4, 6, -2,  -4, 6, 2,  -4, -6, 2,  -4, -6, -2,
        };
        byte indices[] = {
                0,1,2, 2,3,0,  1,4,7, 7,2,1,  0,3,6, 6,5,0,
                3,2,7, 7,6,3,  0,1,4, 4,5,0,  5,6,7, 7,4,5
        };
        
        byte indices1[] = {
        		0,1,2,  2,3,0,  1,4,7,  7,2,1,  4,5,6, 6,7,4, 5,8,11, 11,6,5, 8,9,10, 10,11,8,  9,12,15,
        		15,10,9,  12,13,14,  14,15,12,  13,0,3,  3,14,13 
        };
        FloatBuffer vertexBuffer = getFloatBufferFromFloatArray(vertices1);
        ByteBuffer indexBuffer = getByteBufferFromByteArray(indices1);
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);
        gl.glDrawElements(GL10.GL_TRIANGLES, indices1.length, GL10.GL_UNSIGNED_BYTE, indexBuffer);
        //gl.glDrawElements(GL10.GL_LINE_LOOP, indices1.length, GL10.GL_UNSIGNED_BYTE, indexBuffer);
        
    }
   
    
}
