package GUI;

import Data.DataProcessor;
import DecisionEngine.DecisionEnginePlugin;

import java.util.*;

import javafx.util.Pair;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

//public class HIDS extends Observable implements Observer {
public class HIDS extends Observable{
    private static final String configPath =  new File("").getAbsolutePath() + "\\src\\main\\resources\\config.properties";
    private List<DecisionEnginePlugin> decisionEngines = new ArrayList<DecisionEnginePlugin>();
    private List<DataProcessor> dataModules = new ArrayList<DataProcessor>();
    private DecisionEnginePlugin currentDecisionEngine = null;
    private DataProcessor currentDataModule = null;

    private File srcFile = null;
    private File trgtFile = null;
    private File srcDir = null;
    private File trgtDir = null;
    private File loadModelFile = null;



    public File getSrcFile() {
        return srcFile;
    }

    public void setSrcFile(File srcFile) {
        this.srcFile = srcFile;
        setChanged();
        notifyObservers("srcFile");
    }

    public File getTrgtFile() {
        return trgtFile;
    }

    public void setTrgtFile(File trgtFile) {
        this.trgtFile = trgtFile;
        setChanged();
        notifyObservers("trgtFile");
    }

    public File getSrcDir() {
        return srcDir;
    }

    public void setSrcDir(File srcDir) {
        this.srcDir = srcDir;
        setChanged();
        notifyObservers("srcDir");
    }

    public File getTrgtDir() {
        return trgtDir;
    }

    public void setTrgtDir(File trgtDir) {
        this.trgtDir = trgtDir;
        setChanged();
        notifyObservers("trgtDir");
    }

    public File getLoadModelFile() {
        return loadModelFile;
    }

    public void setLoadModelFile(File loadModelFile) {
        this.loadModelFile = loadModelFile;
        setChanged();
        notifyObservers("loadModelFile");
    }



    /*@Override
    public void update(Observable o, Object arg) {

    }*/




    public static void main(String[] args) {

        HIDS hids = new HIDS();
        if(!hids.moduleInit()){
            //TODO - HANDLE HIDS INITIALISATION ERROR HERE + POPUP ERROR MSG + CALL INITIALISE AGAIN
            System.out.println("Initialisation unsuccessful!");
        }

        for (DecisionEnginePlugin de : hids.decisionEngines) {
            System.out.println("de in main: " + de.pluginName());
        }
        for(DataProcessor dp : hids.dataModules){
            System.out.println("dm in main: " + dp.getClass().getName());
        }
        System.out.println("current de: " + hids.currentDecisionEngine.getClass().getName());
        System.out.println("current dm: " + hids.currentDataModule.getClass().getName());





        /*hids.setSrcFile(new File(configPath));
        hids.setTrgtFile(new File(configPath + "1"));
        hids.setSrcDir(new File(configPath + "2"));
        hids.setTrgtDir(new File(configPath + "3"));
        hids.setLoadModelFile(new File(configPath + "4"));*/



        /*javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                startGUI();
            }
        });*/
    }

    private boolean moduleInit(){
        Properties props = this.loadProperties(configPath);
        if (props != null) {
          // return(loadModules(this, props) && loadDataModules(this, props));
            /*return(loadModules(this, props, "dePlugin") &&
            loadModules(this, props, "dataModule"));*/
            if(loadModules(this, props, "dePlugin") &&
                    loadModules(this, props, "dataModule")){
                currentDecisionEngine = decisionEngines.get(0);
                currentDataModule = dataModules.get(0);
                //TODO SUBSCRIBE ALL DES TO FILE CHANGES HERE
                //TODO SUBSCRIBE HIDS TO CHANGES IN DES
                for(DecisionEnginePlugin de : decisionEngines){
                    System.out.println("adding " + de.getClass().getName() + " as observer");
                    addObserver(de);
                }


                return true;
            } else {return false;}
        }else {return false;}
    }

    private Properties loadProperties(String path) {
        Properties prop = new Properties();
        InputStream input = null;
        try {
            input = new FileInputStream(path);
            prop.load(input);
            return prop;
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
    //TODO - EXCEPTION HANDLING!!!
    private boolean loadModules(HIDS hids, Properties props, String propName) {

        ClassLoader classLoader = HIDS.class.getClassLoader();
        try {
            String[] decisionEngines = props.getProperty(propName).trim().split("\\s*,\\s*");
            for (String de : decisionEngines) {
                //get constructor params
                if (props.containsKey(de)) {
                    String[] params = props.getProperty(de).trim().split("\\s*,\\s*");
                    if (params.length > 0) {
                      Pair<Constructor<?>, Object[]> pair = getMultParamConst(de,params, classLoader);
                      if(pair == null){
                          return false;
                      }
                        if(propName == "dePlugin") {
                            DecisionEnginePlugin plugin = (DecisionEnginePlugin) pair.getKey().newInstance(pair.getValue());
                            hids.decisionEngines.add(plugin);
                        }else if(propName == "dataModule"){
                            DataProcessor dataProcessor = (DataProcessor) pair.getKey().newInstance(pair.getValue());
                            hids.dataModules.add(dataProcessor);
                        }
                    }
                } else {
                    Class c = classLoader.loadClass(de);
                    if(propName == "dePlugin") {
                        DecisionEnginePlugin plugin = (DecisionEnginePlugin) c.newInstance();
                        hids.decisionEngines.add(plugin);
                    }else if(propName == "dataModule"){
                        DataProcessor dataProcessor = (DataProcessor)c.newInstance();
                        hids.dataModules.add(dataProcessor);
                    }
                }
            }

            if(propName == "dePlugin") {
                if (hids.decisionEngines.size() > 0) {
                    //hids.currentDecisionEngine = hids.decisionEngines.get(0);
                    return true;
                }
            }else if(propName == "dataModule"){
                if (hids.dataModules.size() > 0) {
                    //hids.currentDataModule = hids.dataModules.get(0);
                    return true;
                }
            }
            return false;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return false;
        } catch (InstantiationException e) {
            e.printStackTrace();
            return false;
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            return false;
        }
    }

    private Pair<Constructor<?>, Object[]> getMultParamConst(String deName, String[] params, ClassLoader classLoader){
        try {
            List<Class> classes = new ArrayList<>();
            List<Object> args = new ArrayList<>();
            for (String p : params) {
                Class pClass = classLoader.loadClass(p);
                classes.add(pClass);
                Object instance = pClass.newInstance();
                args.add(instance);
            }

            Class[] classArr = classes.toArray(new Class[classes.size()]);
            Object[] objArr = args.toArray(new Object[args.size()]);

            Class<?> c = classLoader.loadClass(deName);
            Constructor<?> ctor = c.getConstructor(classArr);
            return new Pair<>(ctor, objArr);
        }catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        } catch (InstantiationException e) {
            e.printStackTrace();
            return null;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return null;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }











    private static void startGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("HIDS");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 500);

        //Create the menu bar.  Make it have a green background.
        JMenuBar greenMenuBar = new JMenuBar();
        greenMenuBar.setOpaque(true);
        greenMenuBar.setBackground(new Color(154, 165, 127));
        greenMenuBar.setPreferredSize(new Dimension(800, 25));

        //Create a yellow label to put in the content pane.
        JLabel yellowLabel = new JLabel();
        yellowLabel.setOpaque(true);
        yellowLabel.setBackground(new Color(248, 213, 131));
        yellowLabel.setPreferredSize(new Dimension(800, 500));

        //Set the menu bar and add the label to the content pane.
        frame.setJMenuBar(greenMenuBar);
        frame.getContentPane().add(yellowLabel, BorderLayout.CENTER);

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }



}
