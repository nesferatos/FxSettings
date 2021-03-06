package ru.nesferatos.fxsettings;

import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;

import static org.junit.Assert.*;

/**
 * Created by nesferatos on 17.09.2015.
 */
public class FxSettingsAppTest extends ApplicationTest {
    @Override
    public void start(Stage stage) {
        Scene scene = new Scene(new Pane(), 800, 600);
        stage.setScene(scene);
        stage.show();
    }

    @Test
    public void should_drag_file_into_trashcan() {
        // given:
        //rightClickOn("#desktop").moveTo("New").clickOn("Text Document");
        //write("myTextfile.txt").push(ENTER);

        // when:
        //drag(".file").dropTo("#trash-can");

        // then:
        //verifyThat("#desktop", hasChildren(0, ".file"));
    }
}