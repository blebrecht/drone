package com.example.rosdron;

import android.widget.Toast;

public class ErrorManager {

    private MainActivity mainActivity;
    public ErrorManager(MainActivity mainActivity){
        this.mainActivity = mainActivity;
    }

    public void mostrarError(String error){
        Toast errorToast = Toast.makeText(mainActivity, error, Toast.LENGTH_SHORT);

        errorToast.show();
    }
}
