package fr.clementgre.pdf4teachers.interfaces;

import fr.clementgre.pdf4teachers.Main;
import javafx.stage.Window;

public class OSXTouchBarManager {

    static {
        String libPath = System.getProperty("java.library.path");
        System.out.println("java.library.path=" + libPath);
    }

    public OSXTouchBarManager(Window window){

        if(Main.isOSX()) setup(window);

    }

    public void setup(Window window){

        /*JTouchBar jTouchBar = new JTouchBar();
        jTouchBar.setCustomizationIdentifier("MyJavaFXJavaTouchBar");

// button
        TouchBarButton touchBarButtonImg = new TouchBarButton();
        touchBarButtonImg.setTitle("Button 1");
        touchBarButtonImg.setAction(view -> System.out.println("Clicked Button_1."));

        Image image = new Image(ImageName.NSImageNameTouchBarColorPickerFill, false);
        touchBarButtonImg.setImage(image);

        jTouchBar.addItem(new TouchBarItem("Button_1", touchBarButtonImg, true));

// fixed space
        jTouchBar.addItem(new TouchBarItem(TouchBarItem.NSTouchBarItemIdentifierFixedSpaceSmall));

// label
        TouchBarTextField touchBarTextField = new TouchBarTextField();
        touchBarTextField.setStringValue("TextField 1");

        jTouchBar.addItem(new TouchBarItem("TextField_1", touchBarTextField, true));

// flexible space
        jTouchBar.addItem(new TouchBarItem(TouchBarItem.NSTouchBarItemIdentifierFlexibleSpace));

// scrubber
        TouchBarScrubber touchBarScrubber = new TouchBarScrubber();
        touchBarScrubber.setActionListener((scrubber, index) -> System.out.println("Selected Scrubber Index: " + index));
        touchBarScrubber.setDataSource(new ScrubberDataSource() {
            @Override
            public ScrubberView getViewForIndex(TouchBarScrubber scrubber, long index) {
                if(index == 0) {
                    ScrubberTextItemView textItemView = new ScrubberTextItemView();
                    textItemView.setIdentifier("ScrubberItem_1");
                    textItemView.setStringValue("Scrubber TextItem");

                    return textItemView;
                }
                else {
                    ScrubberImageItemView imageItemView = new ScrubberImageItemView();
                    imageItemView.setIdentifier("ScrubberItem_2");
                    imageItemView.setImage(new Image(ImageName.NSImageNameTouchBarAlarmTemplate, false));
                    imageItemView.setAlignment(ImageAlignment.CENTER);

                    return imageItemView;
                }
            }

            @Override
            public int getNumberOfItems(TouchBarScrubber scrubber) {
                return 2;
            }
        });

        jTouchBar.addItem(new TouchBarItem("Scrubber_1", touchBarScrubber, true));

        JTouchBarJavaFX.show(jTouchBar, window);

        //jTouchBar.show(MainWindow.frame);*/

    }

}
