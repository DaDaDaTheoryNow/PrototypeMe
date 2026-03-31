package com.dadadadev.prototype_me.erd.board.config

/**
 * Board-level configuration constants for the ERD board canvas.
 *
 * Shared by Presentation and UI layers to keep behavior thresholds consistent.
 */
internal object ErdBoardConfig {
    /** Minimum allowed zoom scale. */
    const val MIN_SCALE = 0.2f

    /** Maximum allowed zoom scale. */
    const val MAX_SCALE = 5f

    /** Maximum number of undo steps stored in the undo stack. */
    const val MAX_UNDO_STEPS = 50

    /** Board-unit offset applied to pasted nodes to visually separate them from originals. */
    const val PASTE_OFFSET_DP = 40f

    /** Hit-test radius (dp) for ports and edges — multiplied by density at runtime. */
    const val HIT_RADIUS_DP = 20f

    /** Frame-to-frame interpolation factor for remote node movement preview. */
    const val REMOTE_DRAG_LERP_FACTOR = 0.28f

    /** Distance threshold where remote drag preview snaps to target instead of continuing to lerp. */
    const val REMOTE_DRAG_LERP_EPSILON = 0.5f
}

/**
 * Edge rendering and interaction constants for the ERD board.
 */
internal object ErdEdgeConfig {
    /** Hysteresis threshold (in dp) to prevent edge side flipping during small moves. */
    const val SIDE_FLIP_HYSTERESIS_DP = 24f

    /** Number of line segments used to approximate a Bézier curve for hit-testing. */
    const val HIT_TEST_SEGMENTS = 18

    /** Snap-in distance multiplier — edge snaps to target port when pointer enters this range. */
    const val SNAP_IN_MULTIPLIER = 0.95f

    /** Snap-out distance multiplier — edge detaches from target port when pointer exits this range. */
    const val SNAP_OUT_MULTIPLIER = 1.20f

    /** Vertical pixel offset per stacked connection on the same target port. */
    const val MULTI_EDGE_SPREAD_PX = 5f

    /** Minimum dx for Bézier control-point calculation. */
    const val BEZIER_MIN_DX = 40f

    /** Control-point fraction of dx for Bézier curves. */
    const val BEZIER_CTRL_FRACTION = 0.45f

    /** Maximum control-point offset for Bézier curves. */
    const val BEZIER_MAX_CTRL = 250f

    /** Stroke width for selected edges (px). */
    const val STROKE_SELECTED = 2.5f

    /** Stroke width for normal edges (px). */
    const val STROKE_DEFAULT = 1.5f

    /** Stroke width for rubber-band free-draw line (px). */
    const val STROKE_RUBBER_BAND = 2f

    /** Radius for endpoint dots on edges (px). */
    const val ENDPOINT_DOT_RADIUS = 3f

    /** Radius for midpoint/drag-handle dots (px). */
    const val HANDLE_DOT_RADIUS = 5f

    /** Dash segment length for rubber-band free line (px). */
    const val DASH_ON = 8f

    /** Dash gap length for rubber-band free line (px). */
    const val DASH_OFF = 5f
}
