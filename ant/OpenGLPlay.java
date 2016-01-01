package org.example.ant;


public class OpenGLPlay extends Menu {


   @Override
    void prepareMenu() {
        addMenuItem("PLAY GAME", TextureGL.class);
        addMenuItem("OPTION", Prefs.class);
    }
}