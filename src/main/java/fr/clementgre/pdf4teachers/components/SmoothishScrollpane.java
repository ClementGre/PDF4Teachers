package fr.clementgre.pdf4teachers.components;

import fr.clementgre.pdf4teachers.utils.StringUtils;
import javafx.animation.Animation.Status;
import javafx.animation.Interpolator;
import javafx.animation.Transition;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/**
 * Scrollpane with kinda smooth transition scrolling.
 *
 * @author Matt, edited by Clement Grennerat
 *
 * From https://gist.github.com/Col-E/7d31b6b8684669cf1997831454681b85
 */
public class SmoothishScrollpane extends ScrollPane {
    private final static int TRANSITION_DURATION = 200;
    private final static double SCROLL_FACTOR = 1.5;

    private boolean hasScrollStartEndEvents = false;

    /**
     * @param content Item to be wrapped in the scrollpane.
     */
    public SmoothishScrollpane(Node content) {
        // ease-of-access for inner class
        ScrollPane scroll = this;
        // set content in a wrapper
        VBox wrapper = new VBox(content);
        setContent(wrapper);
        // add scroll handling to wrapper

        wrapper.setOnScrollStarted(event -> hasScrollStartEndEvents = true);
        wrapper.setOnScrollFinished(event -> hasScrollStartEndEvents = false);

        wrapper.setOnScroll(new EventHandler<>() {
            private SmoothishTransition transition;
            @Override
            public void handle(ScrollEvent e){

                if(e.isInertia() || hasScrollStartEndEvents) return;

                ///// VERTICAL SCROLL /////
                
                double vShift = 0;
                if(Math.abs(e.getDeltaY()) > Math.abs(e.getDeltaX()) / 2){ // Accept vertical scrolling only if the scroll is not too horizontal (Trackpad support)
                    vShift = e.getDeltaY() * SCROLL_FACTOR / (scroll.getHeight() - scroll.getContent().getBoundsInLocal().getHeight());
                }
                
                if(vShift != 0){
                    final double finalVShift = vShift;
                    double vValue = scroll.getVvalue();
                    Interpolator vInterp = Interpolator.LINEAR;
                    
                    transition = new SmoothishTransition(transition, e.getDeltaY(), vValue, finalVShift) {
                        @Override
                        protected void interpolate(double frac){
                            double newVValue = StringUtils.clamp(vValue + getShift(), scroll.getVmin(), scroll.getVmax());
                            scroll.setVvalue(vInterp.interpolate(vValue, newVValue, frac));
                        }
                    };
                    transition.play();
                }
                
                ///// HORIZONTAL SCROLL /////
                
                double hShift = 0;
                if(Math.abs(e.getDeltaX()) > Math.abs(e.getDeltaY()) / 2){ // Accept horizontal scrolling only if the scroll is not too vertical (Trackpad support)
                    hShift = e.getDeltaX() * SCROLL_FACTOR / (scroll.getWidth() - scroll.getContent().getBoundsInLocal().getWidth());
                }
                
                if(hShift != 0){
                    final double finalHShift = hShift;
                    double hValue = scroll.getHvalue();
                    Interpolator hInterp = Interpolator.LINEAR;
                    
                    transition = new SmoothishTransition(transition, e.getDeltaX(), hValue, finalHShift) {
                        @Override
                        protected void interpolate(double frac){
                            double newHValue = StringUtils.clamp(hValue + getShift(), scroll.getHmin(), scroll.getHmax());
                            scroll.setHvalue(hInterp.interpolate(hValue, newHValue, frac));
                        }
                    };
                    transition.play();
                }
            }
        });
    }
    
    /**
     * @param t
     *            Transition to check.
     * @return {@code true} if transition is playing.
     */
    private static boolean playing(Transition t) {
        return t.getStatus() == Status.RUNNING;
    }
    
    /**
     * @param d1
     *            Value 1
     * @param d2
     *            Value 2.
     * @return {@code true} if values signes are matching.
     */
    private static boolean sameSign(double d1, double d2) {
        return (d1 > 0 && d2 > 0) || (d1 < 0 && d2 < 0);
    }
    
    /**
     * Transition with varying speed based on previously existing transitions.
     *
     * @author Matt
     */
    abstract class SmoothishTransition extends Transition {
        private final double mod;
        private final double delta;
        private final double currentValue;
        private double shift;
        
        public SmoothishTransition(SmoothishTransition old, double delta, double currentValue, double shift) {
            this.currentValue = currentValue;
            this.shift = shift;
            
            // if the last transition was moving in the same direction, and is still playing
            // then increment the modifer. This will boost the distance, thus looking faster
            // and seemingly consecutive.
            if (old != null && sameSign(delta, old.delta) && playing(old)) {
                mod = Math.min(old.getMod() + .3, 1.5);
                this.shift = (old.shift + shift - (currentValue - old.getCurrentValue())) * mod;
            } else {
                mod = 1;
            }
            this.delta = delta;
    
            double durationMod = (mod-1) * .5 + 1;
            setCycleDuration(Duration.millis(TRANSITION_DURATION * (1d/durationMod)));
            setCycleCount(0);
        }
        
        public double getMod() {
            return mod;
        }
        public double getShift() {
            return shift;
        }
        public double getCurrentValue(){
            return currentValue;
        }
        
        @Override
        public void play() {
            super.play();
            // Even with a linear interpolation, startup is visibly slower than the middle.
            // So skip a small bit of the animation to keep up with the speed of prior
            // animation. The value of 10 works and isn't noticeable unless you really pay
            // close attention. This works best on linear but also is decent for others.
            if (getMod() > 1) {
                jumpTo(getCycleDuration().divide(10));
            }
        }
    }
}