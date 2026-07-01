package cromveil.combatnumbers.config;

import cromveil.combatnumbers.client.render.RenderOption;

/**
 * Add new configs here.
 * 
 * Defaults are set in the platform-specific implementations, be sure they are
 * consistent.
 * 
 * Localized name / descriptions in
 * `common/src/main/resources/assets/combatnumbers/lang/en_us.json`.
 */
public interface IConfigHelper {

    // Client
    boolean clientEnabled();
    String clientTheme();
    RenderOption renderMode();
    float baseFontSize();
    float nearFadeDistance();
    float maxRenderDistance();
    float distanceFalloffStart();
    float distanceFalloffEnd();
    float distanceMinScale();

    void setClientEnabled(boolean enabled);
    void setClientTheme(String theme);
    void setClientRenderMode(RenderOption mode);
    void setBaseFontSize(float size);
    void setNearFadeDistance(float distance);
    void setClientMaxRenderDistance(float distance);
    void setDistanceFalloffStart(float distance);
    void setDistanceFalloffEnd(float distance);
    void setDistanceMinScale(float scale);

    // Server
    boolean serverEnabled();
    float serverMaxRenderDistance();
}
