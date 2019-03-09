package com.soumya.wwdablu.sceneform.animate.modelhelpers;

import android.net.Uri;
import android.graphics.Point;

import com.google.ar.core.Anchor;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Trackable;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import java.util.List;

import androidx.annotation.NonNull;

public class ModelHelper {

    private ArFragment arFragment;

    private ModelHelper(ArFragment arFragment) {
        this.arFragment = arFragment;
    }

    public static ModelHelper with(@NonNull ArFragment arFragment) {
        return new ModelHelper(arFragment);
    }

    public void clean() {
        arFragment = null;
    }

    public synchronized void addObjectModel(Uri object) {
        Frame frame = arFragment.getArSceneView().getArFrame();
        Point center = getScreenCenter(arFragment);

        if(frame != null) {
            List<HitResult> result = frame.hitTest(center.x, center.y);
            for(HitResult hit : result) {
                Trackable trackable = hit.getTrackable();
                if (trackable instanceof Plane && ((Plane) trackable).isPoseInPolygon(hit.getHitPose())) {
                    placeObject(hit.createAnchor(), object);
                    break;
                }
            }
        }
    }

    private void placeObject(Anchor anchor, Uri object) {
        ModelRenderable.builder()
                .setSource(arFragment.getContext(), object)
                .build()
                .thenAccept(modelRenderable -> addNodeToScene(anchor, modelRenderable, object))
                .exceptionally(throwable -> null);
    }

    private void addNodeToScene(Anchor createAnchor, ModelRenderable renderable, Uri object) {
        AnchorNode anchorNode = new AnchorNode(createAnchor);
        TransformableNode transformableNode = new TransformableNode(arFragment.getTransformationSystem());
        transformableNode.setName(object.toString());
        transformableNode.setRenderable(renderable);
        transformableNode.setParent(anchorNode);

        arFragment.getArSceneView().getScene().addChild(anchorNode);

        transformableNode.setOnTapListener((hitTestResult, motionEvent) -> {
            //Perform callback action, like bark
        });
        transformableNode.select();
    }

    private Point getScreenCenter(ArFragment arFragment) {

        if(arFragment == null || arFragment.getView() == null) {
            return new android.graphics.Point(0,0);
        }

        int w = arFragment.getView().getWidth()/2;
        int h = arFragment.getView().getHeight()/2;
        return new Point(w, h);
    }
}
