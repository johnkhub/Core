package za.co.imqs.coreservice;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;

public interface CliHandler {

    default Options getOptions() {
        return new Options();
    }

    default OptionGroup getOptionGroup() {
        return new OptionGroup();
    }

    boolean handle(CommandLine cmd, Options options);
}
