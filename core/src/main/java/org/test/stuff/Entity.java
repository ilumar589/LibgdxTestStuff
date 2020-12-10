package org.test.stuff;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import java.util.UUID;

public class Entity {
    private static final String TAG = Entity.class.getSimpleName();
    private static final String DEFAULT_SPRITE_PATH = "sprites/characters/Warrior.png";
    private static final int FRAME_WIDTH = 16;
    private static final int FRAME_HEIGHT = 16;

    public enum State {
        IDLE, WALKING
    }

    public enum Direction {
        UP, RIGHT, DOWN, LEFT
    }


    private String entityId;
    private Vector2 velocity;
    private Direction currentDirection = Direction.LEFT;
    private Direction previousDirection = Direction.UP;
    private Animation<TextureRegion> walkLeftAnimation;
    private Animation<TextureRegion> walkRightAnimation;
    private Animation<TextureRegion> walkUpAnimation;
    private Animation<TextureRegion> walkDownAnimation;
    private Array<TextureRegion> walkLeftFrames;
    private Array<TextureRegion> walkRightFrames;
    private Array<TextureRegion> walkUpFrames;
    private Array<TextureRegion> walkDownFrames;
    public Rectangle boundingBox;
    protected Vector2 nextPlayerPosition;
    protected Vector2 currentPlayerPosition;
    protected State state = State.IDLE;
    protected float frameTime = 0f;
    protected Sprite frameSprite;
    protected TextureRegion currentFrame;

    public Entity() {
        initEntity();
    }

    public void initEntity() {
        this.entityId = UUID.randomUUID().toString();
        this.nextPlayerPosition = new Vector2();
        this.currentPlayerPosition = new Vector2();
        this.boundingBox = new Rectangle();
        this.velocity = new Vector2(2f, 2f);

        TextureRegion[][] textureRegions = getTextureByRegions();
        loadDefaultSprite(textureRegions);
        loadAllAnimations(textureRegions);
    }

    public void init(float startX, float startY){
        this.currentPlayerPosition.x = startX;
        this.currentPlayerPosition.y = startY;

        this.nextPlayerPosition.x = startX;
        this.nextPlayerPosition.y = startY;

        //Gdx.app.debug(TAG, "Calling INIT" );
    }

    public void update(float delta){
        frameTime = (frameTime + delta)%5; //Want to avoid overflow

        //Gdx.app.debug(TAG, "frametime: " + _frameTime );

        //We want the hitbox to be at the feet for a better feel
        setBoundingBoxSize(0f, 0.5f);
    }

    public void setBoundingBoxSize(float percentageWidthReduced, float percentageHeightReduced){
        //Update the current bounding box
        float width;
        float height;

        float widthReductionAmount = 1.0f - percentageWidthReduced; //.8f for 20% (1 - .20)
        float heightReductionAmount = 1.0f - percentageHeightReduced; //.8f for 20% (1 - .20)

        if( widthReductionAmount > 0 && widthReductionAmount < 1){
            width = FRAME_WIDTH * widthReductionAmount;
        }else{
            width = FRAME_WIDTH;
        }

        if( heightReductionAmount > 0 && heightReductionAmount < 1){
            height = FRAME_HEIGHT * heightReductionAmount;
        }else{
            height = FRAME_HEIGHT;
        }


        if( width == 0 || height == 0){
            Gdx.app.debug(TAG, "Width and Height are 0!! " + width + ":" + height);
        }

        //Need to account for the unitscale, since the map coordinates will be in pixels
        float minX;
        float minY;
        if( MapManager.UNIT_SCALE > 0 ) {
            minX = nextPlayerPosition.x / MapManager.UNIT_SCALE;
            minY = nextPlayerPosition.y / MapManager.UNIT_SCALE;
        }else{
            minX = nextPlayerPosition.x;
            minY = nextPlayerPosition.y;
        }

        boundingBox.set(minX, minY, width, height);
        //Gdx.app.debug(TAG, "SETTING Bounding Box: (" + minX + "," + minY + ")  width: " + width + " height: " + height);
    }

    public void dispose() {
        Utility.unloadAsset(DEFAULT_SPRITE_PATH);
    }

    public void setState(State state) {
        this.state = state;
    }

    public Sprite getFrameSprite() {
        return frameSprite;
    }

    public TextureRegion getCurrentFrame() {
        return currentFrame;
    }

    public Vector2 getCurrentPlayerPosition() {
        return currentPlayerPosition;
    }

    public void setCurrentPlayerPosition(float currentPlayerPositionX, float currentPlayerPositionY) {
        frameSprite.setX(currentPlayerPositionX);
        frameSprite.setY(currentPlayerPositionY);
        this.currentPlayerPosition.x = currentPlayerPositionX;
        this.currentPlayerPosition.y = currentPlayerPositionY;
    }

    public void setDirection(Direction direction, float deltaTime) {
        this.previousDirection = currentDirection;
        this.currentDirection = direction;

        switch (currentDirection) {
            case DOWN:
                currentFrame = walkDownAnimation.getKeyFrame(frameTime);
                break;
            case LEFT:
                currentFrame = walkLeftAnimation.getKeyFrame(frameTime);
                break;
            case UP:
                currentFrame = walkUpAnimation.getKeyFrame(frameTime);
                break;
            case RIGHT:
                currentFrame = walkRightAnimation.getKeyFrame(frameTime);
                break;
        }
    }

    public void setNextPositionToCurrent() {
        setCurrentPlayerPosition(nextPlayerPosition.x, nextPlayerPosition.y);
    }

    public void calculateNextPosition(Direction direction, float deltaTime) {
        float x = currentPlayerPosition.x;
        float y = currentPlayerPosition.y;

        velocity.scl(deltaTime);

        switch (direction) {
            case LEFT:
                x -= velocity.x;
                break;
            case RIGHT:
                x += velocity.x;
                break;
            case UP:
                y += velocity.y;
                break;
            case DOWN:
                y -= velocity.y;
                break;
        }

        nextPlayerPosition.x = x;
        nextPlayerPosition.y = y;

        velocity.scl(1 / deltaTime);
    }


    public TextureRegion getFrame(){
        return currentFrame;
    }

    public Vector2 getCurrentPosition(){
        return currentPlayerPosition;
    }

    private TextureRegion[][] getTextureByRegions() {
        Utility.loadTextureAsset(DEFAULT_SPRITE_PATH);
        Texture texture = Utility.getTextureAsset(DEFAULT_SPRITE_PATH);

        assert texture != null;

        return TextureRegion.split(texture, FRAME_WIDTH, FRAME_HEIGHT);
    }

    private void loadAllAnimations(TextureRegion[][] textureRegions) {
        walkDownFrames = new Array<>(4); // a texture region represents 4/16 pixels
        walkLeftFrames = new Array<>(4);
        walkRightFrames = new Array<>(4);
        walkUpFrames = new Array<>(4);

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                TextureRegion region = textureRegions[i][j];

                assert region != null;

                switch (i) {
                    case 0:
                        walkDownFrames.insert(j, region);
                        break;
                    case 1:
                        walkLeftFrames.insert(j, region);
                        break;
                    case 2:
                        walkRightFrames.insert(j, region);
                        break;
                    case 3:
                        walkUpFrames.insert(j, region);
                        break;
                }
            }
        }

        walkDownAnimation = new Animation<>(0.25f, walkDownFrames, Animation.PlayMode.LOOP);
        walkLeftAnimation = new Animation<>(0.25f, walkLeftFrames, Animation.PlayMode.LOOP);
        walkRightAnimation = new Animation<>(0.25f, walkRightFrames, Animation.PlayMode.LOOP);
        walkUpAnimation = new Animation<>(0.25f, walkUpFrames, Animation.PlayMode.LOOP);
    }

    private void loadDefaultSprite(TextureRegion[][] textureRegions) {
        frameSprite = new Sprite(textureRegions[0][0].getTexture(), 0, 0, FRAME_WIDTH, FRAME_HEIGHT);
        currentFrame = textureRegions[0][0];
    }

}
