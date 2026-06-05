package com.riprod.hexcode.core.common.drawing.utils;

import java.util.List;

import it.unimi.dsi.fastutil.floats.FloatArrayList;

public class DrawRasterizer {
    public static boolean[][] rasterize(FloatArrayList points, int gridSize, float minYaw, float maxYaw, float minPitch,
            float maxPitch) {
        boolean[][] grid = new boolean[gridSize][gridSize];

        float yawSpan = maxYaw - minYaw;
        float pitchSpan = maxPitch - minPitch;
        float maxSpan = Math.max(yawSpan, pitchSpan);
        if (maxSpan < 0.001f)
            return grid;

        // offset to center the shorter axis
        float yawOffset = (maxSpan - yawSpan) / 2f;
        float pitchOffset = (maxSpan - pitchSpan) / 2f;

        // normalize points to grid coordinates
        int[] prevCell = null;
        for (int i = 0; i < points.size(); i += 2) {
            int x = Math.round(((points.getFloat(i) - minYaw + yawOffset) / maxSpan) * (gridSize - 1));
            int y = Math.round(((points.getFloat(i + 1) - minPitch + pitchOffset) / maxSpan) * (gridSize - 1));
            x = Math.clamp(x, 0, gridSize - 1);
            y = Math.clamp(y, 0, gridSize - 1);

            if (prevCell != null) {
                // bresenham line from prevCell to (x, y) to fill gaps
                drawLine(grid, prevCell[0], prevCell[1], x, y);
            }
            grid[y][x] = true;
            prevCell = new int[] { x, y };
        }

        return grid;
    }

    public static void drawLine(boolean[][] grid, int x0, int y0, int x1, int y1) {
        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);
        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;
        int err = dx - dy;

        while (true) {
            grid[y0][x0] = true;
            if (x0 == x1 && y0 == y1)
                break;
            int err2 = err * 2;
            if (err2 > -dy) {
                err -= dy;
                x0 += sx;
            }
            if (err2 < dx) {
                err += dx;
                y0 += sy;
            }
        }
    }
}
