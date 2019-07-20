package com.soumya.wwdablu.sceneform.animate;

import android.os.Bundle;
import android.os.Handler;

import com.google.ar.core.Frame;
import com.google.ar.core.Plane;
import com.google.ar.sceneform.Scene;
import com.soumya.wwdablu.sceneform.animate.modelhelpers.ModelHelper;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements ArModelFragment.ModelInteraction {

    private ArModelFragment modelFragment;
    private ModelHelper modelHelper;

    String[] models = {"dance.sfb", "die.sfb", "jump.sfb", "look.sfb", "wave.sfb"};
    private int animIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        modelFragment = (ArModelFragment) getSupportFragmentManager()
                .findFragmentById(R.id.ar_frag_model);
    }

    @Override
    protected void onResume() {
        super.onResume();
        modelFragment.getArSceneView().getScene().addOnUpdateListener(updateListener);

        if(modelHelper == null) {
            modelHelper = ModelHelper.with(modelFragment, this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        removeUpdateListener();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        modelHelper.clean();
    }

    @Override
    public void onClick() {
        runOnUiThread(() -> {

            if(animIndex >= models.length) {
                animIndex = 0;
            }

            modelHelper.replaceObjectModel(models[animIndex]);
            animIndex++;
        });
    }

    private Scene.OnUpdateListener updateListener = frameTime -> {

        Frame frame = modelFragment.getArSceneView().getArFrame();
        if(frame == null) {
            return;
        }

        for(Plane plane : frame.getUpdatedTrackables(Plane.class)) {

            removeUpdateListener();
            modelHelper.addObjectModel("look.sfb");
            animIndex = 0;
            break;
        }
    };

    private void removeUpdateListener() {
        modelFragment.getArSceneView().getScene().removeOnUpdateListener(updateListener);
    }
}
