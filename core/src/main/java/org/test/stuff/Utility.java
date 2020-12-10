package org.test.stuff;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.TextureLoader;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;

public final class Utility {

    private Utility() {}

    private static final String TAG = Utility.class.getSimpleName();

    private static final AssetManager assetManager = new AssetManager();

    private static final InternalFileHandleResolver fileHandleResolver = new InternalFileHandleResolver();

    public static void unloadAsset(String assetFileNamePath) {
        if (assetManager.isLoaded(assetFileNamePath)) {
            assetManager.unload(assetFileNamePath);
        } else {
            Gdx.app.debug(TAG,"Asset is not loaded; Nothing to unload: " + assetFileNamePath);
        }
    }

    public static float loadProgress() {
        return assetManager.getProgress();
    }

    public static int numberOfAssetsQueued() {
        return assetManager.getQueuedAssets();
    }

    public static boolean updateAssetLoading() {
        return assetManager.update();
    }

    public static boolean isAssetLoaded(String fileName) {
        return assetManager.isLoaded(fileName);
    }

    public static void loadMapAsset(String mapFileNamePath) {
        if (mapFileNamePath == null || mapFileNamePath.isEmpty()) {
            Gdx.app.debug(TAG, "Map file name path is null of empty");
            return;
        }

        if (fileHandleResolver.resolve(mapFileNamePath).exists()) {
            assetManager.setLoader(TiledMap.class, new TmxMapLoader(fileHandleResolver));
            assetManager.load(mapFileNamePath, TiledMap.class);
            //Until we add loading screen, just block until we load the map
            assetManager.finishLoadingAsset(mapFileNamePath);
            Gdx.app.debug(TAG, "Map loaded!: " + mapFileNamePath);
        } else {
            Gdx.app.debug(TAG, "Map doesn't exist!: " + mapFileNamePath );
        }
    }

    public static TiledMap getMapAsset(String mapFileNamePath) {
        if (assetManager.isLoaded(mapFileNamePath)) {
            return assetManager.get(mapFileNamePath, TiledMap.class);
        }

        Gdx.app.debug(TAG, "Map is not loaded: " + mapFileNamePath);
        return null;
    }

    public static void loadTextureAsset(String textureFileNamePath) {
        if (textureFileNamePath == null || textureFileNamePath.isEmpty()) {
            Gdx.app.debug(TAG, "Texture file name path is null or empty");
            return;
        }
        //load asset
        if( fileHandleResolver.resolve(textureFileNamePath).exists() ){
            assetManager.setLoader(Texture.class, new TextureLoader(fileHandleResolver));
            assetManager.load(textureFileNamePath, Texture.class);
            //Until we add loading screen, just block until we load the map
            assetManager.finishLoadingAsset(textureFileNamePath);
        }
        else {
            Gdx.app.debug(TAG, "Texture doesn't exist!: " + textureFileNamePath );
        }
    }

    public static Texture getTextureAsset(String textureFileNamePath) {
        if (assetManager.isLoaded(textureFileNamePath)) {
            return assetManager.get(textureFileNamePath, Texture.class);
        }

        Gdx.app.debug(TAG, "Texture is not loaded: " + textureFileNamePath);
        return null;
    }
}
