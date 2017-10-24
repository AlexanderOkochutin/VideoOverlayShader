package com.videohelper.utils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.SurfaceTexture;
import android.opengl.*;
import android.util.Log;


/**
 * Code for rendering a texture onto a surface using OpenGL ES 2.0.
 */
class TextureRender {
    private static final String TAG = "TextureRender";

    private static final int FLOAT_SIZE_BYTES = 4;
    private static final int TRIANGLE_VERTICES_DATA_STRIDE_BYTES = 5 * FLOAT_SIZE_BYTES;
    private static final int TRIANGLE_VERTICES_DATA_POS_OFFSET = 0;
    private static final int TRIANGLE_VERTICES_DATA_UV_OFFSET = 3;
    private final float[] mTriangleVerticesData = {
            // X, Y, Z, U, V
            -1.0f, -1.0f, 0, 0.f, 0.f,
            1.0f, -1.0f, 0, 1.f, 0.f,
            -1.0f,  1.0f, 0, 0.f, 1.f,
            1.0f,  1.0f, 0, 1.f, 1.f,
    };

    private FloatBuffer mTriangleVertices;

    private static final String VERTEX_SHADER =
            "uniform mat4 uMVPMatrix;\n" +
                    "uniform mat4 uSTMatrix;\n" +
                    "uniform float uFrame;\n" +
                    "attribute vec4 aPosition;\n" +
                    "attribute vec4 aTextureCoord;\n" +
                    "varying vec2 vTextureCoord;\n" +
                    "varying float vFrame;\n" +
                    "void main() {\n" +
                    "  gl_Position = uMVPMatrix * aPosition;\n" +
                    "  vTextureCoord = (uSTMatrix * aTextureCoord).xy;\n" +
                    "  vFrame = uFrame;\n" +
                    "}\n";

    private static final String FRAGMENT_SHADER =
            "#extension GL_OES_EGL_image_external : require\n" +
                    "precision mediump float;\n" +      // highp here doesn't seem to matter
                    "varying vec2 vTextureCoord;\n" +
                    "varying float vFrame;\n" +
                    "uniform samplerExternalOES sTexture;\n" +
                    "uniform sampler2D sampler2d1;\n" +
                    "uniform sampler2D sampler2d2;\n" +
                    "uniform sampler2D sampler2d3;\n" +
                    "uniform sampler2D sampler2d4;\n" +
                    "uniform sampler2D sampler2d5;\n" +
                    "uniform sampler2D sampler2d6;\n" +
                    "uniform sampler2D sampler2d7;\n" +
                    "uniform sampler2D sampler2d8;\n" +
                    "uniform sampler2D sampler2d9;\n" +
                    "uniform sampler2D sampler2d10;\n" +
                    "void main() {\n" +
                    "vec4 colorSample1 = texture2D(sTexture, vTextureCoord);\n"+
                    "vec4 colorSample2 = vec4(0.0);\n"+
                    "float alpha = 0.0;\n"+
                    "vec4 colorSample3 = vec4(0.0);\n"+
                    "if(vFrame == 0.0) {\n"+
                    "colorSample2 = texture2D(sampler2d1, vTextureCoord);\n"+
                    "}\n"+
                    "else if(vFrame == 1.0){\n"+
                    "colorSample2 = texture2D(sampler2d2, vTextureCoord);\n"+
                    "}\n"+
                    "else if(vFrame == 2.0){\n"+
                    "colorSample2 = texture2D(sampler2d3, vTextureCoord);\n"+
                    "}\n"+
                    "else if(vFrame == 3.0){\n"+
                    "colorSample2 = texture2D(sampler2d4, vTextureCoord);\n"+
                    "}\n"+
                    "else if(vFrame == 4.0){\n"+
                    "colorSample2 = texture2D(sampler2d5, vTextureCoord);\n"+
                    "}\n"
                    +"else if(vFrame == 5.0){\n"+
                    "colorSample2 = texture2D(sampler2d6, vTextureCoord);\n"+
                    "}\n"
                    +"else if(vFrame == 6.0){\n"+
                    "colorSample2 = texture2D(sampler2d7, vTextureCoord);\n"+
                    "}\n"
                    +"else if(vFrame == 7.0){\n"+
                    "colorSample2 = texture2D(sampler2d8, vTextureCoord);\n"+
                    "}\n"
                    +"else if(vFrame == 8.0){\n"+
                    "colorSample2 = texture2D(sampler2d9, vTextureCoord);\n"+
                    "}\n"+
                    "else if(vFrame == 9.0){\n"+
                    "colorSample2 = texture2D(sampler2d10, vTextureCoord);\n"+
                    "}\n"+
                    "alpha = 1.0 - colorSample2.a;\n"+
                    "colorSample3 = vec4(colorSample1.r*alpha+colorSample2.r,colorSample1.g*alpha+colorSample2.g,colorSample1.b*alpha+colorSample2.b, 1.0);\n"+
                    " gl_FragColor =  colorSample3;\n"+
                    "}\n";


    private float[] mMVPMatrix = new float[16];
    private float[] mSTMatrix = new float[16];

    private int mProgram;
    private int mTextureID = -12345;
    private int mBitmapId = -15151;
    private int muMVPMatrixHandle;
    private int muSTMatrixHandle;
    private int maPositionHandle;
    private int maTextureHandle;
    private int muFrameNumberHandler;
    private int frameTextureHandler = -1516;
    int[] bitmapTextureHandlers = new int[10];
    int[] bitmapIds = new int[10];
    int[] bitmapTexturesIds = new int[10];
    public TextureRender() {
        mTriangleVertices = ByteBuffer.allocateDirect(
                mTriangleVerticesData.length * FLOAT_SIZE_BYTES)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mTriangleVertices.put(mTriangleVerticesData).position(0);

        Matrix.setIdentityM(mSTMatrix, 0);
    }

    public int getTextureId() {
        return mTextureID;
    }

    int frame = 0;
    public void drawFrame(SurfaceTexture st, int frameNumber) {

        int trigger = frameNumber%3;
        if(trigger == 0 && frameNumber!=0){
            frame = (frame+1)%10;
        }


        checkGlError("onDrawFrame start");
        st.getTransformMatrix(mSTMatrix);

        GLES20.glClearColor(0.0f, 1.0f, 0.0f, 1.0f);
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

        GLES20.glUseProgram(mProgram);
        checkGlError("glUseProgram");

        //GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        //GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mBitmapId);

        //GLUtils.texImage2D(GLES20.GL_TEXTURE_2D,0,MainActivity.bitmap,0);
        float floatFrame = frame;
        //GLES20.glUniform1i(bitmapTextureHandler,1);
        for(int i = 0; i < 10; i++) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE1 + i);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, bitmapIds[i]);
            GLES20.glUniform1i(bitmapTextureHandlers[i], i+1);
        }

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mTextureID);
        GLES20.glUniform1i(frameTextureHandler,0);

        GLES20.glUniform1f(muFrameNumberHandler,floatFrame);
        checkGlError("glVertexAttribPointer maPosition");

        mTriangleVertices.position(TRIANGLE_VERTICES_DATA_POS_OFFSET);
        GLES20.glVertexAttribPointer(maPositionHandle, 3, GLES20.GL_FLOAT, false,
                TRIANGLE_VERTICES_DATA_STRIDE_BYTES, mTriangleVertices);
        checkGlError("glVertexAttribPointer maPosition");
        GLES20.glEnableVertexAttribArray(maPositionHandle);
        checkGlError("glEnableVertexAttribArray maPositionHandle");

        mTriangleVertices.position(TRIANGLE_VERTICES_DATA_UV_OFFSET);
        GLES20.glVertexAttribPointer(maTextureHandle, 2, GLES20.GL_FLOAT, false,
                TRIANGLE_VERTICES_DATA_STRIDE_BYTES, mTriangleVertices);
        checkGlError("glVertexAttribPointer maTextureHandle");
        GLES20.glEnableVertexAttribArray(maTextureHandle);
        checkGlError("glEnableVertexAttribArray maTextureHandle");

        Matrix.setIdentityM(mMVPMatrix, 0);
        GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, mMVPMatrix, 0);
        GLES20.glUniformMatrix4fv(muSTMatrixHandle, 1, false, mSTMatrix, 0);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        checkGlError("glDrawArrays");
        GLES20.glFinish();
    }


    /**
     * Initializes GL state.  Call this after the EGL surface has been created and made current.
     */
    public void surfaceCreated() {
        mProgram = createProgram(VERTEX_SHADER, FRAGMENT_SHADER);
        if (mProgram == 0) {
            throw new RuntimeException("failed creating program");
        }

        maPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
        checkGlError("glGetAttribLocation aPosition");
        if (maPositionHandle == -1) {
            throw new RuntimeException("Could not get attrib location for aPosition");
        }
        maTextureHandle = GLES20.glGetAttribLocation(mProgram, "aTextureCoord");
        checkGlError("glGetAttribLocation aTextureCoord");
        if (maTextureHandle == -1) {
            throw new RuntimeException("Could not get attrib location for aTextureCoord");
        }

        muMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        checkGlError("glGetUniformLocation uMVPMatrix");
        if (muMVPMatrixHandle == -1) {
            throw new RuntimeException("Could not get attrib location for uMVPMatrix");
        }

        muSTMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uSTMatrix");
        checkGlError("glGetUniformLocation uSTMatrix");
        if (muSTMatrixHandle == -1) {
            throw new RuntimeException("Could not get attrib location for uSTMatrix");
        }

        muFrameNumberHandler = GLES20.glGetUniformLocation(mProgram,"uFrame");
        frameTextureHandler =  GLES20.glGetUniformLocation(mProgram,"sTexture");

        int baseTextureID = GLES20.GL_TEXTURE1;


        for(int i = 0; i < 10; i++){
            String str = "sampler2d"+(i+1);
            bitmapTextureHandlers[i] = GLES20.glGetUniformLocation(mProgram,str);
            bitmapTexturesIds[i] = baseTextureID + i;
        }

        checkGlError("glGetUniformLocation frame");
        if (muFrameNumberHandler == -1) {
            throw new RuntimeException("Could not get attrib location for frame");
        }

        int[] texture = new int[1];
        GLES20.glGenTextures(1, texture, 0);
        mTextureID = texture[0];

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mTextureID);

        checkGlError("glBindTexture mTextureID");

        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S,
                GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T,
                GLES20.GL_CLAMP_TO_EDGE);

        checkGlError("glBindTexture mTextureID");

        GLES20.glGenTextures(10,bitmapIds,0);

        for(int i = 0; i < 10; i++){
            GLES20.glActiveTexture(bitmapTexturesIds[i]);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,bitmapIds[i]);
            GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
            GLES20.glEnable(GLES20.GL_BLEND);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_WRAP_S,GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_WRAP_T,GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_MIN_FILTER,GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_MAG_FILTER,GLES20.GL_LINEAR);
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D,0,ExtractDecodeEditEncodeMuxTest.bitmap[i],0);
            ExtractDecodeEditEncodeMuxTest.bitmap[i].recycle();
        }
        checkGlError("glTexParameter");
    }

    /**
     * Replaces the fragment shader.
     */
    public void changeFragmentShader(String fragmentShader) {
        GLES20.glDeleteProgram(mProgram);
        mProgram = createProgram(VERTEX_SHADER, fragmentShader);
        if (mProgram == 0) {
            throw new RuntimeException("failed creating program");
        }
    }

    private int loadShader(int shaderType, String source) {
        int shader = GLES20.glCreateShader(shaderType);
        checkGlError("glCreateShader type=" + shaderType);
        GLES20.glShaderSource(shader, source);
        GLES20.glCompileShader(shader);
        int[] compiled = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0) {
            Log.e(TAG, "Could not compile shader " + shaderType + ":");
            Log.e(TAG, " " + GLES20.glGetShaderInfoLog(shader));
            GLES20.glDeleteShader(shader);
            shader = 0;
        }
        return shader;
    }

    private int createProgram(String vertexSource, String fragmentSource) {
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource);
        if (vertexShader == 0) {
            return 0;
        }
        int pixelShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
        if (pixelShader == 0) {
            return 0;
        }

        int program = GLES20.glCreateProgram();
        checkGlError("glCreateProgram");
        if (program == 0) {
            Log.e(TAG, "Could not create program");
        }
        GLES20.glAttachShader(program, vertexShader);
        checkGlError("glAttachShader");
        GLES20.glAttachShader(program, pixelShader);
        checkGlError("glAttachShader");
        GLES20.glLinkProgram(program);
        int[] linkStatus = new int[1];
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
        if (linkStatus[0] != GLES20.GL_TRUE) {
            Log.e(TAG, "Could not link program: ");
            Log.e(TAG, GLES20.glGetProgramInfoLog(program));
            GLES20.glDeleteProgram(program);
            program = 0;
        }
        return program;
    }

    public void checkGlError(String op) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, op + ": glError " + error);
            throw new RuntimeException(op + ": glError " + error);
        }
    }
}
