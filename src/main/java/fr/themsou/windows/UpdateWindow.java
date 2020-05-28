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
import jfxtras.styles.jmetro.JMetro;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Pattern;

public class UpdateWindow extends Stage {

    public static String version = "Inconnue";
    public static String description = "Description introuvable";
    public static boolean newVersion = false;

    public static boolean checkVersion(){

        try{

            URL url = new URL("https://api.github.com/repos/clementgre/PDF4Teachers/releases/latest");

            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setDoOutput(true);
            con.setRequestProperty("Content-Type", "application/json; utf-8");

            JsonFactory jfactory = new JsonFactory();
            JsonParser jParser = jfactory.createParser(con.getInputStream());

            String parsedVersion = null;
            String parsedDesc = null;

            while(jParser.nextToken() != JsonToken.NOT_AVAILABLE){
                String fieldname = jParser.getCurrentName();

                if("tag_name".equals(fieldname)){
                    jParser.nextToken();
                    parsedVersion = jParser.getText();

                }else if("body".equals(fieldname)){
                    jParser.nextToken();
                    parsedDesc = jParser.getText().split(Pattern.quote("\r\n\r\n\r\n"))[0];
                    break;
                }
            }
            jParser.close();

            if(parsedVersion == null) return false;

            if(!parsedVersion.equals(Main.VERSION)){
                UpdateWindow.version = parsedVersion;
                UpdateWindow.description = parsedDesc;
                UpdateWindow.newVersion = true;
                return true;
            }

        }catch(IOException e){
            e.printStackTrace();
        }

        return false;
    }

    public UpdateWindow(){

        VBox root = new VBox();
        Scene scene = new Scene(root, 545, 720);

        initOwner(Main.window);
        initModality(Modality.WINDOW_MODAL);
        getIcons().add(new Image(getClass().getResource("/logo.png")+""));
        setWidth(545);
        setHeight(720);
        setResizable(false);
        setTitle(TR.tr("PDF4Teachers - Nouvelle Version"));
        setScene(scene);
        StyleManager.putStyle(root, Style.DEFAULT);

        setupPanel(root);
        show();
    }

    public void setupPanel(VBox root){

        Text info = new Text(TR.tr("Une nouvelle version de PDF4Teachers est disponible !"));

        Text version = new Text(TR.tr("Vous utilisez la version") + " " + Main.VERSION + " " + TR.tr("et la version") + " " + UpdateWindow.version + " " + TR.tr("est disponible.") + "\n\n" + TR.tr("Description :"));

        Label desc = new Label(new TextWrapper(UpdateWindow.description, FontUtils.getFont("Arial", false, false, 12.5),490).wrap());
        desc.setStyle("-fx-padding: 10; -fx-font-size: 12.5;");
        desc.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));


        Button maj = new Button(TR.tr("Faire la mise à jour"));
        maj.setOnAction(t -> Main.hostServices.showDocument("https://github.com/clementgre/PDF4Teachers/releases/latest"));
        maj.setStyle("-fx-background-color: #ba6800;");
        maj.setAlignment(Pos.BASELINE_CENTER);

        root.getChildren().addAll(info, version, desc, maj);
        root.setStyle("-fx-padding: 10;");

        VBox.setMargin(info, new Insets(40, 0, 40, 0));
        VBox.setMargin(version, new Insets(0, 0, 5, 0));

        VBox.setMargin(maj, new Insets(20, 0, 0, 0));

    }

}
