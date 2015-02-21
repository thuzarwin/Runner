package edu.umass.cs.runner.system.backend.known.mturk;

import edu.umass.cs.runner.Runner;
import edu.umass.cs.runner.system.Parameters;
import edu.umass.cs.runner.system.backend.AbstractLibrary;
import edu.umass.cs.surveyman.survey.Survey;

import java.io.*;
import java.util.Properties;

public class MturkLibrary extends AbstractLibrary {

    public String CONFIG = DIR + fileSep + "mturk_config";

    private static final String MTURK_SANDBOX_URL = "https://mechanicalturk.sandbox.amazonaws.com?Service=AWSMechanicalTurkRequester";
    private static final String MTURK_PROD_URL = "https://mechanicalturk.amazonaws.com?Service=AWSMechanicalTurkRequester";
    private static final String MTURK_SANDBOX_EXTERNAL_HIT = "https://workersandbox.mturk.com/mturk/externalSubmit";
    private static final String MTURK_PROD_EXTERNAL_HIT = "https://www.mturk.com/mturk/externalSubmit";

    public String MTURK_URL;
    public String EXTERNAL_HIT;

    public String getActionForm() {
        return EXTERNAL_HIT;
    }
    // editable stuff gets copied

    public MturkLibrary(Properties properties, Survey survey) {
        init();
        this.props = properties;
        Runner.LOGGER.info("Updated properties to " + properties.toString());
        this.props.setProperty("reward", Double.toString(Runner.basePay));
    }

    public MturkLibrary(String propertiesURL, String configURL){
        if (configURL!=null)
            this.CONFIG = configURL;
        if (propertiesURL!=null) {
            super.props = new Properties();
            try {
                super.props.load(new FileReader(propertiesURL));
            } catch (IOException e) {
                System.err.println(String.format("%s\nCould not load properties from %s. Proceeding to load from default..."
                        , e.getMessage()
                        , propertiesURL));
            }
        }
        init();
    }

    public MturkLibrary(){
        init();
    }

    public void init() {
        try {
            if (new File(AbstractLibrary.PARAMS).exists() && props==null){
                props = new Properties();
                Runner.LOGGER.info("Loading properties from " + AbstractLibrary.PARAMS);
                props.load(new FileReader(AbstractLibrary.PARAMS));
            }
        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
        } catch (IOException io) {
            io.printStackTrace();
        }

        Runner.LOGGER.info(props.toString());

        boolean sandbox = ! this.props.containsKey(Parameters.SANDBOX) ||
                Boolean.parseBoolean(this.props.getProperty(Parameters.SANDBOX));


        if (sandbox) {
            MTURK_URL = MTURK_SANDBOX_URL;
            EXTERNAL_HIT = MTURK_SANDBOX_EXTERNAL_HIT;
        } else {
            MTURK_URL = MTURK_PROD_URL;
            EXTERNAL_HIT = MTURK_PROD_EXTERNAL_HIT;
        }

        File cfile = new File(CONFIG);
        File alt = new File(CONFIG+".csv");
        if (! cfile.exists() ) {
            if (alt.exists())
                alt.renameTo(cfile);
            else Runner.LOGGER.warn("ERROR: You have not yet set up the surveyman directory nor AWS keys. Please see the project website for instructions.");
        } else {
            try {
                // make sure we have both names for the access keys in the config file
                Properties config = new Properties();
                config.load(new FileInputStream(CONFIG));
                if (config.containsKey("AWSAccessKeyId") && config.containsKey("AWSSecretKey")) {
                    BufferedWriter bw = new BufferedWriter(new FileWriter(CONFIG, true));
                    bw.newLine();
                    if (! config.containsKey("access_key")) {
                        bw.write("access_key=" + config.getProperty("AWSAccessKeyId"));
                        bw.newLine();
                    }
                    if (! config.containsKey("secret_key")) {
                        bw.write("secret_key=" + config.getProperty("AWSSecretKey"));
                        bw.newLine();
                    }
                    bw.close();
                } else if (config.containsKey("access_key") && config.containsKey("secret_key")) {
                    BufferedWriter bw = new BufferedWriter(new FileWriter(CONFIG, true));
                    bw.newLine();
                    if (! config.containsKey("AWSAccessKeyId")) {
                        bw.write("AWSAccessKeyId="+config.getProperty("access_key"));
                        bw.newLine();
                    }
                    if (! config.containsKey("AWSSecretKey")) {
                        bw.write("AWSSecretKey="+config.getProperty("secret_key"));
                        bw.newLine();
                    }
                    bw.close();
                }
            } catch (IOException io){
                Runner.LOGGER.trace(io);
            }
        }
    }
}


