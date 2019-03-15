package com.soumya.wwdablu.sceneform.animate;

import android.os.Bundle;
import android.os.Handler;

import com.google.ar.core.Frame;
import com.google.ar.core.Plane;
import com.google.ar.sceneform.Scene;
import com.soumya.wwdablu.sceneform.animate.modelhelpers.ModelHelper;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private ArModelFragment modelFragment;
    private ModelHelper modelHelper;
    private Handler handler;
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
            modelHelper = ModelHelper.with(modelFragment);
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

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {

            runOnUiThread(() -> {

                int i = modelHelper.animCountOnModel("tpose.fbx");

                modelHelper.animateModel("tpose.sfb", animIndex);
                if(++animIndex != modelHelper.animCountOnModel("tpose.fbx")) {
                    handler.postDelayed(runnable, 30000);
                }
            });
        }
    };

    private Scene.OnUpdateListener updateListener = frameTime -> {

        Frame frame = modelFragment.getArSceneView().getArFrame();
        if(frame == null) {
            return;
        }

        for(Plane plane : frame.getUpdatedTrackables(Plane.class)) {
            modelHelper.addObjectModel("tpose.sfb");
            removeUpdateListener();

            handler = new Handler();
            animIndex = 0;
            handler.postDelayed(runnable, 5000);
            break;
        }
    };

    private void removeUpdateListener() {
        modelFragment.getArSceneView().getScene().removeOnUpdateListener(updateListener);
    }
}
