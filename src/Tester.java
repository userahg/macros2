
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import star.common.StarMacro;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author cd8unu
 */
public class Tester extends StarMacro {

    @Override
    public void execute() {
        TextFileLineAppender appender = new TextFileLineAppender(new File("C:\\Users\\cd8unu\\Desktop\\test.txt"));
        try {
            appender.replaceLine(2, "replaced");
            appender.appendToLine(1, " appended");
            appender.prependToLine(3, "prepended ");
        } catch (Exception ex) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);
            getActiveSimulation().println(sw.toString());
        }
    }

}

class TextFileLineAppender {

    private final File _input;
    private final String _tmpFileName;

    public TextFileLineAppender(File input) {
        _input = input;
        _tmpFileName = input.getName() + ".tmp";
    }

    public void replaceLine(int line, String text) throws FileNotFoundException {

        if (_input == null) {
            throw new NullPointerException("Unable to locate file " + _input.getAbsolutePath() + ".");
        }
        if (!_input.exists()) {
            throw new FileNotFoundException("Unable to locate file " + _input.getAbsolutePath() + ".");
        }

        try (BufferedReader br = new BufferedReader(new FileReader(_input.getAbsolutePath()))) {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(_input.getParent() + File.separator + _tmpFileName))) {
                String iline;
                int counter = 0;

                while ((iline = br.readLine()) != null) {
                    if (counter == line) {
                        bw.write(text);
                        bw.newLine();
                    } else {
                        bw.write(iline);
                        bw.newLine();
                    }
                    counter++;
                    bw.flush();
                }

            } catch (IOException ex) {
            }
        } catch (IOException ex) {
        }

        _input.delete();
        File newFile = new File(_input.getParent() + File.separator + _tmpFileName);
        newFile.renameTo(_input);
    }
    
    public void appendToLine(int line, String text) throws FileNotFoundException {

        if (_input == null) {
            throw new NullPointerException("Unable to locate file " + _input.getAbsolutePath() + ".");
        }
        if (!_input.exists()) {
            throw new FileNotFoundException("Unable to locate file " + _input.getAbsolutePath() + ".");
        }

        try (BufferedReader br = new BufferedReader(new FileReader(_input.getAbsolutePath()))) {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(_input.getParent() + File.separator + _tmpFileName))) {
                String iline;
                int counter = 0;

                while ((iline = br.readLine()) != null) {
                    if (counter == line) {
                        bw.write(iline + text);
                        bw.newLine();
                    } else {
                        bw.write(iline);
                        bw.newLine();
                    }
                    counter++;
                    bw.flush();
                }

            } catch (IOException ex) {
            }
        } catch (IOException ex) {
        }

        _input.delete();
        File newFile = new File(_input.getParent() + File.separator + _tmpFileName);
        newFile.renameTo(_input);
    }
    
    public void prependToLine(int line, String text) throws FileNotFoundException {

        if (_input == null) {
            throw new NullPointerException("Unable to locate file " + _input.getAbsolutePath() + ".");
        }
        if (!_input.exists()) {
            throw new FileNotFoundException("Unable to locate file " + _input.getAbsolutePath() + ".");
        }

        try (BufferedReader br = new BufferedReader(new FileReader(_input.getAbsolutePath()))) {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(_input.getParent() + File.separator + _tmpFileName))) {
                String iline;
                int counter = 0;

                while ((iline = br.readLine()) != null) {
                    if (counter == line) {
                        bw.write(text + iline);
                        bw.newLine();
                    } else {
                        bw.write(iline);
                        bw.newLine();
                    }
                    counter++;
                    bw.flush();
                }

            } catch (IOException ex) {
            }
        } catch (IOException ex) {
        }

        _input.delete();
        File newFile = new File(_input.getParent() + File.separator + _tmpFileName);
        newFile.renameTo(_input);
    }

}
