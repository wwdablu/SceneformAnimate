package com.soumya.wwdablu.sceneform.animate.modelhelpers;

import android.graphics.Point;
import android.net.Uri;

import com.google.ar.core.Anchor;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Trackable;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.animation.ModelAnimator;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.AnimationData;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;
import com.soumya.wwdablu.sceneform.animate.ArModelFragment;

import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;

public class ModelHelper {

    private ArFragment arFragment;
    private HashMap<String, ModelRenderable> renderableMap;
    private TransformableNode transformableNode;
    private ArModelFragment.ModelInteraction modelInteraction;

    private ModelHelper(ArFragment arFragment, ArModelFragment.ModelInteraction modelInteraction) {
        this.arFragment = arFragment;
        renderableMap = new HashMap<>();
        this.modelInteraction = modelInteraction;
    }

    public static ModelHelper with(@NonNull ArFragment arFragment, @NonNull ArModelFragment.ModelInteraction modelInteraction) {
        return new ModelHelper(arFragment, modelInteraction);
    }

    public void clean() {
        arFragment = null;
    }

    public synchronized void addObjectModel(@NonNull String object) {
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

    public synchronized void replaceObjectModel(@NonNull String modelName) {

        final Uri object = Uri.parse(modelName);
        ModelRenderable.builder()
                .setSource(arFragment.getContext(), object)
                .build()
                .thenAccept(modelRenderable -> {
                    renderableMap.put(modelName, modelRenderable);
                    transformableNode.setRenderable(modelRenderable);
                    animateModel(modelName, 0);
                })
                .exceptionally(throwable -> null);
    }

    public synchronized void animateModel(String modelName, int animIndex) {
        ModelRenderable modelRenderable = renderableMap.get(modelName);
        if(modelRenderable == null) {
            return;
        }

        AnimationData animData = modelRenderable.getAnimationData(animIndex);
        ModelAnimator modelAnimator = new ModelAnimator(animData, modelRenderable);

        modelAnimator.setRepeatCount(0);
        modelAnimator.start();
    }

    public int animCountOnModel(String modelName) {

        ModelRenderable modelRenderable = renderableMap.get(modelName);
        if(modelRenderable == null) {
            return 0;
        }

        int count = modelRenderable.getAnimationDataCount();
        AnimationData animationData = modelRenderable.getAnimationData(0);
        String name = modelRenderable.getAnimationData(0).getName();

        return modelRenderable.getAnimationDataCount();
    }

    private void placeObject(Anchor anchor, String modelName) {
        final Uri object = Uri.parse(modelName);
        ModelRenderable.builder()
            .setSource(arFragment.getContext(), object)
            .build()
            .thenAccept(modelRenderable -> {
                renderableMap.put(modelName, modelRenderable);
                addNodeToScene(anchor, modelRenderable, object);
            })
            .exceptionally(throwable -> null);
    }

    private void addNodeToScene(Anchor createAnchor, ModelRenderable renderable, Uri object) {
        AnchorNode anchorNode = new AnchorNode(createAnchor);
        transformableNode = new TransformableNode(arFragment.getTransformationSystem());
        transformableNode.setName(object.toString());
        transformableNode.setRenderable(renderable);
        transformableNode.setParent(anchorNode);

        transformableNode.setLocalRotation(Quaternion.axisAngle(new Vector3(0f, 1, 0), 180f));

        arFragment.getArSceneView().getScene().addChild(anchorNode);

        transformableNode.setOnTapListener((hitTestResult, motionEvent) -> {
            modelInteraction.onClick();
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
