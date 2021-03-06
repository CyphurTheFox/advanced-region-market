package net.alex9849.arm.commands;

import net.alex9849.arm.AdvancedRegionMarket;
import net.alex9849.arm.exceptions.CmdSyntaxException;
import net.alex9849.arm.exceptions.InputException;
import net.alex9849.arm.exceptions.NoPermissionException;
import net.alex9849.arm.handler.CommandHandler;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CommandSplitter extends BasicArmCommand {

    private final CommandHandler commandHandler = new CommandHandler();

    public CommandSplitter(String rootCommand, AdvancedRegionMarket plugin, List<String> usage, String helpCommandPermission,
                           String helpCommandHeadline, Collection<BasicArmCommand> commands) {
        super(true, plugin, rootCommand, Arrays.asList("(?i)" + rootCommand + " [^;\n]+"),
                usage, Stream.concat(Stream.of(helpCommandPermission), commands.stream().flatMap(x -> x.getPermissions().stream())).collect(Collectors.toList()));
        this.commandHandler.addCommands(commands);
        this.commandHandler.addCommand(new HelpCommand(this.commandHandler, plugin, helpCommandHeadline,
                new String[]{rootCommand}, helpCommandPermission));
    }

    @Override
    protected boolean runCommandLogic(CommandSender sender, String command, String commandLabel) throws InputException, CmdSyntaxException, NoPermissionException {
        String[] args = command.split(" ");
        StringBuilder shorterCommand = new StringBuilder();

        //remove first word from command
        boolean passedFirst = false;
        boolean passedSecond = false;
        for(String arg : args) {
            if(!passedFirst) {
                passedFirst = true;
                continue;
            }
            if(passedSecond) {
                shorterCommand.append(" ");
            } else {
                passedSecond = true;
            }
            shorterCommand.append(arg);
        }

        try {
            return this.commandHandler.executeCommand(sender, shorterCommand.toString(), commandLabel);
        } catch (CmdSyntaxException cse) {
            //Append rootcommand to command syntax
            List<String> syntax = cse.getSyntax();
            for (int i = 0; i < syntax.size(); i++) {
                syntax.set(i, this.getRootCommand() + " " + syntax.get(i));
            }
            throw cse;
        }
    }

    @Override
    protected List<String> onTabCompleteArguments(Player player, String[] args) {
        List<String> returnme = new ArrayList<>();

        String[] newargs = new String[args.length - 1];

        for (int i = 1; i < args.length; i++) {
            newargs[i - 1] = args[i];
        }

        if (args.length == 1) {
            if (this.getRootCommand().startsWith(args[0])
                    && this.commandHandler.onTabComplete(player, new String[]{""}).size() != 0) {
                returnme.add(this.getRootCommand());
            }
        }
        if (args.length >= 2 && this.getRootCommand().equalsIgnoreCase(args[0])) {
            returnme.addAll(this.commandHandler.onTabComplete(player, newargs));
        }
        return returnme;
    }

}
