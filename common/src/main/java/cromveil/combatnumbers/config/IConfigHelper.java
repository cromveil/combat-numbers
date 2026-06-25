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

    RenderOption renderMode();

    float baseFontSize();

    float nearFadeDistance();

    float maxRenderDistance();

    float distanceFalloffStart();

    float distanceFalloffEnd();

    float distanceMinScale();

    // Server
    boolean serverEnabled();

    float serverMaxRenderDistance();
}
