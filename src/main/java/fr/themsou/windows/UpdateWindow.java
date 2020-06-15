package fr.themsou.windows;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import fr.themsou.main.Main;
import fr.themsou.utils.FontUtils;
import fr.themsou.utils.TR;
import fr.themsou.utils.TextWrapper;
import fr.themsou.utils.style.Style;
import fr.themsou.utils.style.StyleManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Pattern;

public class UpdateWindow extends Stage {

    public static String version = "";
    public static String description = "";
    public static boolean newVersion = false;
    public static boolean newPre = false;
    public static boolean error = false;

    public static boolean checkVersion(){

        ////////// GET LAST RELEASE INCLUDING PRE //////////
        String parsedVersion = null;
        try{
            URL url = new URL("https://api.github.com/repos/clementgre/PDF4Teachers/tags");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setDoOutput(true);
            con.setRequestProperty("Content-Type", "application/json; utf-8");

            JsonFactory jfactory = new JsonFactory();
            JsonParser jParser = jfactory.createParser(con.getInputStream());

            JsonToken token;
            while((token = jParser.nextToken()) != null){
                String key = jParser.getCurrentName();

                if("name".equals(key)){
                    jParser.nextToken();
                    parsedVersion = jParser.getText();
                    break;
                }
            }
            jParser.close();

            if(parsedVersion.equals(Main.VERSION)){
                // Up to date with the last release
                return false;
            }

        }catch(IOException e){
            e.printStackTrace();
            error = true;
            return false;
        }

        ////////// GET LAST RELEASE DETAILS ////////// (isn't up to date -> search details about the last version)
        String parsedDescription = null;
        try{
            URL url = new URL("https://api.github.com/repos/clementgre/PDF4Teachers/releases/tags/" + parsedVersion);

            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setDoOutput(true);
            con.setRequestProperty("Content-Type", "application/json; utf-8");

            JsonFactory jfactory = new JsonFactory();
            JsonParser jParser = jfactory.createParser(con.getInputStream());

            boolean parsedPre = false;

            JsonToken token;
            while((token = jParser.nextToken()) != null){
                String fieldname = jParser.getCurrentName();

                if("prerelease".equals(fieldname)){
                    jParser.nextToken();
                    parsedPre = jParser.getBooleanValue();
                }else if("body".equals(fieldname)){
                    jParser.nextToken();
                    parsedDescription = jParser.getText().split(Pattern.quote("\r\n\r\n\r\n"))[0];
                    break;
                }
            }
            jParser.close();

            if(parsedDescription == null){
                error = true;
                return false;
            }

            if(!parsedPre){
                UpdateWindow.version = parsedVersion;
                UpdateWindow.description = parsedDescription;
                UpdateWindow.newVersion = true;
                return true;
            }
            // Is pre, verify if the user has already the last non-pre release before propose it.
            // Else, we will propose the last non-pre release

        }catch(IOException e){
            e.printStackTrace();
            error = true;
            return false;
        }

        ////////// GET LAST RELEASE WITHOUT PRE ////////// (isn't up to date && last is pre -> find the latest non-pre version)
        try{
            URL url = new URL("https://api.github.com/repos/clementgre/PDF4Teachers/releases/latest");

            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setDoOutput(true);
            con.setRequestProperty("Content-Type", "application/json; utf-8");

            JsonFactory jfactory = new JsonFactory();
            JsonParser jParser = jfactory.createParser(con.getInputStream());

            String parsedLatestVersion = null;
            String parsedLatestDescription = null;

            JsonToken token;
            while((token = jParser.nextToken()) != null){
                String fieldname = jParser.getCurrentName();

                if("tag_name".equals(fieldname)){
                    jParser.nextToken();
                    parsedLatestVersion = jParser.getText();

                }if("body".equals(fieldname)){
                    jParser.nextToken();
                    parsedLatestDescription = jParser.getText().split(Pattern.quote("\r\n\r\n\r\n"))[0];
                    break;
                }
            }
            jParser.close();

            if(parsedLatestVersion == null){
                error = true;
                return false;
            }

            if(parsedLatestVersion.equals(Main.VERSION)){
                // Up to date with the latest release -> let's propose the pre release
                UpdateWindow.version = parsedVersion;
                UpdateWindow.description = parsedDescription;
                UpdateWindow.newPre = true;
                return true;
            }else{
                // User don't have the latest release -> propose it before proposing the pre
                UpdateWindow.version = parsedLatestVersion;
                UpdateWindow.description = parsedLatestDescription;
                UpdateWindow.newVersion = true;
                return true;
            }
        }catch(IOException e){
            e.printStackTrace();
            error = true;
            return false;
        }
    }

    public UpdateWindow(){

        VBox root = new VBox();
        Scene scene = new Scene(root);

        initOwner(Main.window);
        initModality(Modality.WINDOW_MODAL);
        getIcons().add(new Image(getClass().getResource("/logo.png")+""));
        setWidth(600);
        setResizable(false);
        setTitle(TR.tr("PDF4Teachers - Nouvelle Version"));
        setScene(scene);
        StyleManager.putStyle(root, Style.DEFAULT);

        setupPanel(root);
        show();
    }

    public void setupPanel(VBox root){

        Text info;
        Text version;
        if(newPre){
            info = new Text(TR.tr("Une nouvelle avant-première de PDF4Teachers est disponible !") + "\n" + TR.tr("Les avant-premières sont destinées aux utilisateurs qui souhaitent contribuer\nau projet PDF4Teachers en testant les versions avant leur sortie."));
            version = new Text(TR.tr("Vous utilisez la version") + " " + Main.VERSION + " " + TR.tr("et l'avant-première") + " " + UpdateWindow.version + " " + TR.tr("est disponible.") + "\n\n" + TR.tr("Description :"));
        }else{
            info = new Text(TR.tr("Une nouvelle version de PDF4Teachers est disponible !"));
            version = new Text(TR.tr("Vous utilisez la version") + " " + Main.VERSION + " " + TR.tr("et la version") + " " + UpdateWindow.version + " " + TR.tr("est disponible.") + "\n\n" + TR.tr("Description :"));
        }

        HBox buttons = new HBox();

        Label desc = new Label(new TextWrapper(UpdateWindow.description, FontUtils.getFont("Arial", false, false, 12.5),545).wrap());
        desc.setStyle("-fx-padding: 10; -fx-font-size: 12.5;");
        desc.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));

        Button see = new Button(TR.tr("Voir sur GitHub"));
        see.setOnAction(t -> Main.hostServices.showDocument("https://github.com/ClementGre/PDF4Teachers/releases/tag/" + UpdateWindow.version));
        //see.setStyle("-fx-background-color: #ba6800;");
        see.setAlignment(Pos.BASELINE_CENTER);

        String platform = "Linux";
        String extension = "deb";
        if(System.getProperty("os.name").toLowerCase().contains("win")){
            platform = "Windows"; extension = "msi";
        }else if(System.getProperty("os.name").toLowerCase().contains("os x")) {
            platform = "MacOSX"; extension = "dmg";
        }
        String url = "https://github.com/ClementGre/PDF4Teachers/releases/download/" + UpdateWindow.version + "/PDF4Teachers-" + platform + "-" + UpdateWindow.version + "." + extension;

        Button maj = new Button(TR.tr("Téléchargement direct"));
        maj.setOnAction(t -> Main.hostServices.showDocument(url));
        //maj.setStyle("-fx-background-color: #ba6800;");
        maj.setAlignment(Pos.BASELINE_CENTER);

        buttons.getChildren().addAll(see, maj);

        root.getChildren().addAll(info, version, desc, buttons);
        root.setStyle("-fx-padding: 10;");

        VBox.setMargin(info, new Insets(40, 10, 40, 10));

        HBox.setMargin(see, new Insets(0, 10, 0, 0));
        VBox.setMargin(buttons, new Insets(20, 0, 0, 0));

        maj.requestFocus();

    }

}
