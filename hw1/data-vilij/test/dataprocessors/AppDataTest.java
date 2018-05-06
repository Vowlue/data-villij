package dataprocessors;

import org.junit.Test;

import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.*;

public class AppDataTest {
    @Test
    public void saveDataToTSD() throws IOException {
        TextArea textArea = new TextArea();
        textArea.setText("@a\ta\t1,1");
        saveData("yes", textArea.getText());
        assertEquals("@a\ta\t1,1", readFile("yes"));
    }
    @Test(expected = IOException.class)
    public void saveDataToInvalidPath() throws IOException {
        TextArea textArea = new TextArea();
        textArea.setText("@a\ta\t1,1");
        saveData("", textArea.getText());
        assertEquals("@a\ta\t1,1", readFile(""));
    }
    private void saveData(String dataPath, String data) throws IOException {
        Path path = new File(dataPath).toPath();
        PrintWriter writer = new PrintWriter(Files.newOutputStream(path));
        writer.write(data);
        writer.close();
    }
    private String readFile(String dataPath) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(dataPath));
        StringBuilder buffer;
        buffer = new StringBuilder();
        String line = reader.readLine();
        buffer.append(line);
        while((line = reader.readLine()) != null){
            buffer.append("\n").append(line);
        }
        return buffer.toString();
    }
}