package com.soumya.wwdablu.sceneform.animate;

import android.os.Bundle;

import com.google.ar.sceneform.ux.ArFragment;

import androidx.annotation.Nullable;

public class ArModelFragment extends ArFragment {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        getPlaneDiscoveryController().hide();
        getPlaneDiscoveryController().setInstructionView(null);
    }
}
