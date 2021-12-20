package com.example.rosdron;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    //FALTA:
    /*
    Agregar excepcion cuando ROS se cae en el Raspberry


     */

    private boolean botonConexionBloqueado;
    private ConexionManager conexionManager;
    private ErrorManager errorManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        botonConexionBloqueado = false;
        conexionManager = new ConexionManager(this);
        errorManager = new ErrorManager(this);

        ProgressBar progressBar = getProgressBar();
        progressBar.setVisibility(View.INVISIBLE);
    }

    public void conectar(View view){
        if(botonConexionBloqueado){
            return;
        }
        botonConexionBloqueado = true;
        Button button = getBoton();
        button.setText("Conectando");
        getProgressBar().setVisibility(View.VISIBLE);
        conexionManager.iniciar();
        //getProgressBar().setVisibility(View.INVISIBLE);
    }

    public void reiniciarBotonConectar(){
        botonConexionBloqueado = false;
        Button button = getBoton();
        button.setText("Conectar");
        getProgressBar().setVisibility(View.INVISIBLE);
    }

    public void bloquearBotonConectar(){
        Button button = getBoton();
        button.setText("Conectado");
        getProgressBar().setVisibility(View.INVISIBLE);
    }

    public Button getBoton(){
        return findViewById(R.id.boton_conectar);
    }

    public ProgressBar getProgressBar(){
        return findViewById(R.id.progress_bar_conectar);
    }

    public TextView getTextView(){
        return findViewById(R.id.textView);
    }

    public TextView getTextView2(){
        return findViewById(R.id.textView2);
    }

    public ErrorManager getErrorManager(){
        return this.errorManager;
    }

    public ConexionManager getConexionManager(){
        return this.conexionManager;
    }
}