package fr.themsou.utils;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;

public class StringDrawing {

    public static int[] alignRightString(Graphics g, int maxX, int minY, int maxY, String s, Font font){


        FontRenderContext frc = new FontRenderContext(null, true, true);

        Rectangle2D r2D = font.getStringBounds(s, frc);
        int rWidth = (int) Math.round(r2D.getWidth());
        int rHeight = (int) Math.round(r2D.getHeight());
        int rY = (int) Math.round(r2D.getY());

        int b = ((maxY - minY) / 2) - (rHeight / 2) - rY;

        g.setFont(font);
        g.drawString(s, maxX - rWidth, minY + b);

        return new int[]{rWidth, rHeight};
    }
    public static int[] alignLeftString(Graphics g, int minX, int minY, int maxY, String s, Font font) {


        FontRenderContext frc = new FontRenderContext(null, true, true);

        Rectangle2D r2D = font.getStringBounds(s, frc);
        int rWidth = (int) Math.round(r2D.getWidth());
        int rHeight = (int) Math.round(r2D.getHeight());
        int rY = (int) Math.round(r2D.getY());

        int b = ((maxY - minY) / 2) - (rHeight / 2) - rY;

        g.setFont(font);
        g.drawString(s, minX, minY + b);


        return new int[]{rWidth, rHeight};
    }

    public static int[] fullCenterString(Graphics g, int minX, int maxX, int minY, int maxY, String s, Font font) {

        FontRenderContext frc = new FontRenderContext(null, true, true);

        Rectangle2D r2D = font.getStringBounds(s, frc);
        int rWidth = (int) Math.round(r2D.getWidth());
        int rHeight = (int) Math.round(r2D.getHeight());
        int rX = (int) Math.round(r2D.getX());
        int rY = (int) Math.round(r2D.getY());

        int a = ((maxX - minX) / 2) - (rWidth / 2) - rX;
        int b = ((maxY - minY) / 2) - (rHeight / 2) - rY;

        g.setFont(font);
        g.drawString(s, minX + a, minY + b);

        return new int[]{rWidth, rHeight};
    }

    public static int[] centerString(Graphics g, int X, int minY, int maxY, String s, Font font) {


        FontRenderContext frc = new FontRenderContext(null, true, true);

        Rectangle2D r2D = font.getStringBounds(s, frc);
        int rWidth = (int) Math.round(r2D.getWidth());
        int rHeight = (int) Math.round(r2D.getHeight());
        int rY = (int) Math.round(r2D.getY());

        int b = ((maxY - minY) / 2) - (rHeight / 2) - rY;

        g.setFont(font);
        g.drawString(s, X, minY + b);

        int retur[] = { rWidth, rHeight };

        return retur;
    }
}
