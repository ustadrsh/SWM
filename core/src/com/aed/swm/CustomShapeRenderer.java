package com.aed.swm;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * Created by Administrator on 21/05/2017.
 */
public class CustomShapeRenderer extends ShapeRenderer {
    public void roundedRect(float x, float y, float originX, float originY, float width, float height, float scaleX, float scaleY, float rotation, float radius){
        // rectangles (5 separate instead of 2 overlapping, so that it works with transparency)
        super.rect(x + radius, y + radius, originX, originY, width - 2*radius, height - 2*radius, scaleX, scaleY, rotation); //center
        super.rect(x, y + radius, originX, originY, radius, height - 2*radius, scaleX, scaleY, rotation); //left
        super.rect(x + width - radius, y + radius, originX, originY, radius, height - 2*radius, scaleX, scaleY, rotation); //right
        super.rect(x + radius, y, originX, originY, width - 2*radius, radius, scaleX, scaleY, rotation); //bottom
        super.rect(x + radius, y + height - radius, originX, originY, width - 2*radius, radius, scaleX, scaleY, rotation); //top

        // arcs
        super.arc(originX + (x + radius - originX) * scaleX, originY + (y + radius - originY) * scaleY, radius*(scaleX+scaleY)/2f, 180f, 90f); //bottom left
        super.arc(originX + (x + width - radius - originX) * scaleX, originY + (y + radius - originY) * scaleY, radius*(scaleX+scaleY)/2f, 270f, 90f); //bottom right
        super.arc(originX + (x + width - radius - originX) * scaleX, originY + (y + height - radius - originY) * scaleY, radius*(scaleX+scaleY)/2f, 0f, 90f); //top right
        super.arc(originX + (x + radius - originX) * scaleX, originY + (y + height - radius - originY) * scaleY, radius*(scaleX+scaleY)/2f, 90f, 90f); //top left
    }
}