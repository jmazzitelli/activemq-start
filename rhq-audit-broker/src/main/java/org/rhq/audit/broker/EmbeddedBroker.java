package org.rhq.audit.broker;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

import java.net.URI;
import java.util.Arrays;

import org.apache.activemq.broker.BrokerFactory;
import org.apache.activemq.broker.BrokerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides a slim wrapper around the message broker. You can simply provide a
 * config file (either a ActiveMQ .properties or .xml file) to the constructor,
 * then start/stop the broker.
 * 
 * You can start the broker on the command line if you want a standalone broker.
 * 
 * You can subclass this to provide additional functionality around
 * configuration and management of the broker.
 */
public class EmbeddedBroker {
    private final Logger log = LoggerFactory.getLogger(EmbeddedBroker.class);
    private InitializationParameters initialParameters;
    private BrokerService brokerService;

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            throw new IllegalArgumentException("Missing arguments. Please specify configuration properties file.");
        }

        EmbeddedBroker embeddedBroker = new EmbeddedBroker(args);
        embeddedBroker.startBroker();

        // go to sleep indefinitely
        synchronized (args) {
            args.wait();
        }
    }

    public EmbeddedBroker(String[] cmdlineArgs) throws Exception {
        InitializationParameters initParams = processArguments(cmdlineArgs);
        setInitializationParameters(initParams);
        initializeBrokerService();
    }

    public EmbeddedBroker(InitializationParameters initParams) throws Exception {
        setInitializationParameters(initParams);
        initializeBrokerService();
    }

    public void startBroker() throws Exception {
        BrokerService broker = getBrokerService();
        if (broker == null) {
            throw new IllegalStateException("Broker was not initialized");
        }
        broker.start();
        log.info("Started broker");
    }

    public void stopBroker() throws Exception {
        BrokerService broker = getBrokerService();
        if (broker == null) {
            return; // nothing to do
        }

        try {
            broker.stop();
            log.info("Stopped broker");
        } finally {
            setBrokerService(null);
        }
    }

    protected InitializationParameters getInitializationParameters() {
        return this.initialParameters;
    }

    protected void setInitializationParameters(InitializationParameters ip) {
        this.initialParameters = ip;
    }

    protected void initializeBrokerService() throws Exception {
        if (getBrokerService() != null) {
            throw new IllegalStateException("Broker is already initialized");
        }

        InitializationParameters initParams = getInitializationParameters();
        if (initParams == null) {
            throw new IllegalStateException("Missing initialization parameters");
        }

        BrokerService broker = BrokerFactory.createBroker(initParams.configFile, false);
        setBrokerService(broker);
        log.info("Initialized broker");
    }

    protected void setBrokerService(BrokerService broker) {
        brokerService = broker;
    }

    protected BrokerService getBrokerService() {
        return brokerService;
    }

    protected InitializationParameters processArguments(String[] cmdlineArgs) throws Exception {
        log.debug("Processing arguments: {}", Arrays.asList(cmdlineArgs));

        String configFileArg = null;

        String sopts = "-:hD:c:";
        LongOpt[] lopts = { new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h'), //
                new LongOpt("config", LongOpt.REQUIRED_ARGUMENT, null, 'c') };

        Getopt getopt = new Getopt("rhq-audit-broker", cmdlineArgs, sopts, lopts);
        int code;

        while ((code = getopt.getopt()) != -1) {
            switch (code) {
            case ':':
            case '?': {
                // for now both of these should exit
                displayUsage();
                throw new IllegalArgumentException("Invalid argument(s)");
            }

            case 1: {
                // this will catch non-option arguments (which we don't
                // currently care about)
                System.err.println("Unused argument: " + getopt.getOptarg());
                break;
            }

            case 'h': {
                displayUsage();
                throw new HelpException("Help displayed");
            }

            case 'D': {
                String sysprop = getopt.getOptarg();
                int i = sysprop.indexOf("=");
                String name;
                String value;

                if (i == -1) {
                    name = sysprop;
                    value = "true";
                } else {
                    name = sysprop.substring(0, i);
                    value = sysprop.substring(i + 1, sysprop.length());
                }

                System.setProperty(name, value);
                log.debug("System property set: {}={}", name, value);

                break;
            }

            case 'c': {
                configFileArg = getopt.getOptarg();
                break;
            }
            }
        }

        if (configFileArg == null) {
            throw new IllegalArgumentException("Missing configuration file (-c)");
        }

        InitializationParameters initParamsFromArguments = new InitializationParameters();

        // help the user out - if they gave a file without the proper prefix,
        // add the prefix for them
        if (configFileArg.endsWith(".properties") && !configFileArg.startsWith("properties:")) {
            configFileArg = "properties:" + configFileArg;
        } else if (configFileArg.endsWith(".xml") && !configFileArg.startsWith("xbean:")) {
            configFileArg = "xbean:" + configFileArg;
        }

        initParamsFromArguments.configFile = new URI(configFileArg);

        return initParamsFromArguments;
    }

    private void displayUsage() {
        log.info("Options:");
        log.info("\t--help, -h: Displays this help text.");
        log.info("\t-Dname=value: Sets a system property.");
        log.info("\t--config=<file>, -c: Specifies the file used to configure the broker.");
    }

    private class HelpException extends Exception {
        private static final long serialVersionUID = 1L;

        public HelpException(String msg) {
            super(msg);
        }
    }

    public static class InitializationParameters {
        public URI configFile;
    }
}
