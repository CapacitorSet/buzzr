
import java.awt.Menu;
import java.io.*;
import java.nio.file.*;

public class SetupReader {

    void readSetupFile(Buzzr sim, String str, String title) {
        sim.t = 0;
        try {
            byte[] bytes = Files.readAllBytes(Paths.get("circuits/" + str));
            sim.readSetup(bytes, bytes.length, false);
        } catch (Exception e1) {
            try {
                byte[] bytes;
                bytes = InputStreamToBytes(getClass().getClassLoader().getResourceAsStream("circuits/" + str));
                sim.readSetup(bytes, bytes.length, false);
            } catch (Exception e) {
                e.printStackTrace();
                sim.stop("Unable to read " + str + "!", null);
            }
        }
        sim.titleLabel.setText(title);
    }
    
    public void getSetupList(Buzzr sim, Menu menu, boolean retry) {
        Menu stack[] = new Menu[6];
        int stackptr = 0;
        stack[stackptr++] = menu;
        try {
            // hausen: if setuplist.txt does not exist in the same
            // directory, try reading from the jar file
            byte bytes[];
            try {
                bytes = Files.readAllBytes(Paths.get("setuplist.txt"));
            } catch (Exception e) {
                try {
                    bytes = InputStreamToBytes(getClass().getClassLoader().getResourceAsStream("setuplist.txt"));
                } catch (Exception f) {
                    System.out.println("Couldn't read the setup list");
                    return;
                }
            }
            int p;
            if (bytes.length == 0 || bytes[0] != '#') {
                // got a redirect, try again
                getSetupList(sim, menu, true);
                return;
            }
            for (p = 0; p < bytes.length;) {
                int l;
                for (l = 0; l != bytes.length - p; l++) {
                    if (bytes[l + p] == '\n') {
                        l++;
                        break;
                    }
                }
                String line = new String(bytes, p, l - 1);
                if (line.charAt(0) == '#') {
                } else if (line.charAt(0) == '+') {
                    Menu n = new Menu(line.substring(1));
                    menu.add(n);
                    menu = stack[stackptr++] = n;
                } else if (line.charAt(0) == '-') {
                    menu = stack[--stackptr - 1];
                } else {
                    int i = line.indexOf(' ');
                    if (i > 0) {
                        String title = line.substring(i + 1);
                        boolean first = false;
                        if (line.charAt(0) == '>') {
                            first = true;
                        }
                        String file = line.substring(first ? 1 : 0, i);
                        menu.add(sim.getMenuItem(title, "setup " + file));
                        if (first && sim.startCircuit == null) {
                            sim.startCircuit = file;
                            sim.startLabel = title;
                        }
                    }
                }
                p += l;
            }
        } catch (Exception e) {
            System.out.println("Error while reading the setuplist!");
            e.printStackTrace();
        }
    }
   

    byte[] InputStreamToBytes(InputStream i) throws IOException {
        // Fuck you, Java.
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        int nRead;
        byte[] data = new byte[16384];

        while ((nRead = i.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }

        return buffer.toByteArray();
    }
}
